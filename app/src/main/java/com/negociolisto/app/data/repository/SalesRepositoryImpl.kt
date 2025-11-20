package com.negociolisto.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.data.local.dao.SaleDao
import com.negociolisto.app.data.local.entity.toDomain
import com.negociolisto.app.data.local.entity.toEntity
import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.domain.model.StockMovement
import com.negociolisto.app.domain.model.StockMovementType
import com.negociolisto.app.domain.model.StockMovementReason
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.SalesRepository
import com.negociolisto.app.domain.repository.CustomerRepository
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.util.NegocioListoError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 💰 IMPLEMENTACIÓN DEL REPOSITORIO DE VENTAS
 * 
 * Maneja los datos de ventas usando Room Database con sincronización automática a Firebase.
 */
@Singleton
class SalesRepositoryImpl @Inject constructor(
    private val saleDao: SaleDao,
    private val customerRepository: CustomerRepository,
    private val inventoryRepository: InventoryRepository,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : SalesRepository {
    
    override fun getSales(): Flow<List<Sale>> {
        return saleDao.getAllSales().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getSaleById(id: String): Sale? {
        return saleDao.getSaleById(id)?.toDomain()
    }
    
    override suspend fun recordSale(sale: Sale) {
        // ⚠️ VALIDAR STOCK ANTES DE REGISTRAR LA VENTA
        sale.items.forEach { item ->
            val product = inventoryRepository.getProductById(item.productId)
            if (product != null) {
                val currentStock = product.stockQuantity
                // Validar que haya suficiente stock disponible
                if (currentStock < item.quantity) {
                    throw NegocioListoError.InsufficientStockError(
                        productId = item.productId,
                        productName = item.productName,
                        requestedQuantity = item.quantity,
                        availableStock = currentStock
                    )
                }
            } else {
                // Producto no encontrado
                throw NegocioListoError.BusinessRuleError(
                    "Producto no encontrado: ${item.productName} (ID: ${item.productId})"
                )
            }
        }
        
        // Guardar localmente después de validar
        saleDao.insertSale(sale.toEntity())
        
        // Actualizar stock de productos: descontar cantidad vendida de cada producto
        sale.items.forEach { item ->
            val product = inventoryRepository.getProductById(item.productId)
            if (product != null) {
                val currentStock = product.stockQuantity
                val newStock = currentStock - item.quantity
                
                // Crear movimiento de stock directamente para incluir referenceId
                val movement = StockMovement(
                    productId = item.productId,
                    movementType = StockMovementType.OUT,
                    quantity = item.quantity,
                    reason = StockMovementReason.SALE,
                    description = "Venta ${sale.id} - ${item.productName} (${item.quantity} unidades)",
                    referenceId = sale.id,
                    unitCost = item.unitPrice,
                    previousStock = currentStock,
                    newStock = newStock,
                    timestamp = sale.date,
                    userId = null,
                    notes = "Venta registrada el ${sale.date}"
                )
                
                // Registrar el movimiento de stock
                inventoryRepository.recordStockMovement(movement)
            }
        }
        
        // Actualizar total de compras del cliente si tiene customerId
        sale.customerId?.let { customerId ->
            // Obtener el cliente actual
            val customer = customerRepository.getCustomerById(customerId)
            if (customer != null) {
                // Calcular nuevo total sumando la venta actual
                val newTotalPurchases = customer.totalPurchases + sale.total
                val newLastPurchaseDate = sale.date
                
                // Actualizar el cliente con el nuevo total
                customerRepository.updateCustomerPurchases(customerId, newTotalPurchases, newLastPurchaseDate)
            }
        }
        
        // Sincronizar automáticamente con Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val saleEntity = sale.toEntity()
                    val saleMap = hashMapOf<String, Any?>(
                        "id" to saleEntity.id,
                        "customerId" to saleEntity.customerId,
                        "items" to saleEntity.items, // String serializado
                        "total" to saleEntity.total,
                        "date" to saleEntity.date,
                        "paymentMethod" to saleEntity.paymentMethod,
                        "note" to saleEntity.note,
                        "status" to saleEntity.status,
                        "canceledAt" to saleEntity.canceledAt,
                        "canceledReason" to saleEntity.canceledReason
                    )
                    
                    firestore.collection("users/${currentUser.id}/sales")
                        .document(saleEntity.id)
                        .set(saleMap)
                        .await()
                    
                    println("✅ Venta ${sale.id} sincronizada automáticamente con Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error sincronizando venta con Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }

    override suspend fun updateSale(sale: Sale) {
        // Obtener la venta anterior para revertir efectos
        val existingSaleEntity = saleDao.getSaleById(sale.id)
        val existingSale = existingSaleEntity?.toDomain()
        
        if (existingSale != null && existingSale.status == com.negociolisto.app.domain.model.SaleStatus.ACTIVE) {
            // Revertir stock de productos de la venta anterior: aumentar existencias
            existingSale.items.forEach { item ->
                val product = inventoryRepository.getProductById(item.productId)
                if (product != null) {
                    val currentStock = product.stockQuantity
                    val newStock = currentStock + item.quantity
                    
                    // Crear movimiento de stock para la reversión
                    val movement = StockMovement(
                        productId = item.productId,
                        movementType = StockMovementType.IN,
                        quantity = item.quantity,
                        reason = StockMovementReason.RETURN_FROM_CUSTOMER,
                        description = "Reversión por edición de venta ${sale.id} - ${item.productName} (${item.quantity} unidades)",
                        referenceId = sale.id,
                        unitCost = item.unitPrice,
                        previousStock = currentStock,
                        newStock = newStock,
                        timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        userId = null,
                        notes = "Reversión por edición de venta ${sale.id}"
                    )
                    
                    // Registrar el movimiento de stock
                    inventoryRepository.recordStockMovement(movement)
                }
            }
            
            // Revertir total de compras del cliente de la venta anterior
            existingSale.customerId?.let { customerId ->
                val customer = customerRepository.getCustomerById(customerId)
                if (customer != null) {
                    // Restar el total de la venta anterior
                    val newTotalPurchases = (customer.totalPurchases - existingSale.total).coerceAtLeast(0.0)
                    customerRepository.updateCustomerPurchases(customerId, newTotalPurchases, customer.lastPurchaseDate)
                }
            }
        }
        
        // ⚠️ VALIDAR STOCK DESPUÉS DE REVERTIR LA VENTA ANTERIOR
        // Primero calculamos el stock disponible después de revertir
        val stockAfterRevert = mutableMapOf<String, Int>()
        sale.items.forEach { item ->
            val product = inventoryRepository.getProductById(item.productId)
            if (product != null) {
                val currentStock = product.stockQuantity
                // Si había una venta anterior, ya revertimos el stock, así que usamos el stock actual
                // Si no había venta anterior, usamos el stock actual directamente
                val availableStock = if (existingSale != null && existingSale.status == com.negociolisto.app.domain.model.SaleStatus.ACTIVE) {
                    // Ya revertimos, así que el stock actual ya incluye la reversión
                    currentStock
                } else {
                    currentStock
                }
                stockAfterRevert[item.productId] = availableStock
                
                // Validar que haya suficiente stock disponible
                if (availableStock < item.quantity) {
                    throw NegocioListoError.InsufficientStockError(
                        productId = item.productId,
                        productName = item.productName,
                        requestedQuantity = item.quantity,
                        availableStock = availableStock
                    )
                }
            } else {
                throw NegocioListoError.BusinessRuleError(
                    "Producto no encontrado: ${item.productName} (ID: ${item.productId})"
                )
            }
        }
        
        // Actualizar la venta en la base de datos después de validar
        saleDao.updateSale(sale.toEntity())
        
        // Aplicar efectos de la nueva venta: descontar stock
        sale.items.forEach { item ->
            val product = inventoryRepository.getProductById(item.productId)
            if (product != null) {
                val currentStock = product.stockQuantity
                val newStock = currentStock - item.quantity
                
                // Crear movimiento de stock para la nueva venta
                val movement = StockMovement(
                    productId = item.productId,
                    movementType = StockMovementType.OUT,
                    quantity = item.quantity,
                    reason = StockMovementReason.SALE,
                    description = "Venta editada ${sale.id} - ${item.productName} (${item.quantity} unidades)",
                    referenceId = sale.id,
                    unitCost = item.unitPrice,
                    previousStock = currentStock,
                    newStock = newStock,
                    timestamp = sale.date,
                    userId = null,
                    notes = "Venta editada el ${sale.date}"
                )
                
                // Registrar el movimiento de stock
                inventoryRepository.recordStockMovement(movement)
            }
        }
        
        // Actualizar total de compras del cliente con la nueva venta
        sale.customerId?.let { customerId ->
            val customer = customerRepository.getCustomerById(customerId)
            if (customer != null) {
                // Calcular nuevo total sumando la venta actual
                val newTotalPurchases = customer.totalPurchases + sale.total
                val newLastPurchaseDate = sale.date
                
                // Actualizar el cliente con el nuevo total
                customerRepository.updateCustomerPurchases(customerId, newTotalPurchases, newLastPurchaseDate)
            }
        }
        
        // Sincronizar automáticamente con Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val saleEntity = sale.toEntity()
                    val saleMap = hashMapOf<String, Any?>(
                        "id" to saleEntity.id,
                        "customerId" to saleEntity.customerId,
                        "items" to saleEntity.items, // String serializado
                        "total" to saleEntity.total,
                        "date" to saleEntity.date,
                        "paymentMethod" to saleEntity.paymentMethod,
                        "note" to saleEntity.note,
                        "status" to saleEntity.status,
                        "canceledAt" to saleEntity.canceledAt,
                        "canceledReason" to saleEntity.canceledReason
                    )
                    
                    firestore.collection("users/${currentUser.id}/sales")
                        .document(saleEntity.id)
                        .set(saleMap)
                        .await()
                    
                    println("✅ Venta ${sale.id} actualizada automáticamente en Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error sincronizando actualización de venta con Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }

    override suspend fun cancelSale(id: String, reason: String?) {
        // Obtener la venta antes de cancelarla para revertir efectos
        val saleEntity = saleDao.getSaleById(id)
        val sale = saleEntity?.toDomain()
        
        if (sale != null && sale.status == com.negociolisto.app.domain.model.SaleStatus.ACTIVE) {
            // Revertir stock de productos: aumentar existencias
            sale.items.forEach { item ->
                val product = inventoryRepository.getProductById(item.productId)
                if (product != null) {
                    val currentStock = product.stockQuantity
                    val newStock = currentStock + item.quantity
                    
                    // Crear movimiento de stock para la reversión
                    val movement = StockMovement(
                        productId = item.productId,
                        movementType = StockMovementType.IN,
                        quantity = item.quantity,
                        reason = StockMovementReason.RETURN_FROM_CUSTOMER,
                        description = "Reversión por anulación de venta ${sale.id} - ${item.productName} (${item.quantity} unidades)",
                        referenceId = sale.id,
                        unitCost = item.unitPrice,
                        previousStock = currentStock,
                        newStock = newStock,
                        timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        userId = null,
                        notes = "Reversión por anulación de venta ${sale.id}"
                    )
                    
                    // Registrar el movimiento de stock
                    inventoryRepository.recordStockMovement(movement)
                }
            }
            
            // Revertir total de compras del cliente si tiene customerId
            sale.customerId?.let { customerId ->
                val customer = customerRepository.getCustomerById(customerId)
                if (customer != null) {
                    // Restar el total de la venta cancelada
                    val newTotalPurchases = (customer.totalPurchases - sale.total).coerceAtLeast(0.0)
                    // Mantener la última fecha de compra si hay otras ventas activas
                    // O usar null si no hay más ventas (esto se puede mejorar consultando otras ventas)
                    val newLastPurchaseDate = customer.lastPurchaseDate
                    
                    // Actualizar el cliente con el nuevo total
                    customerRepository.updateCustomerPurchases(customerId, newTotalPurchases, newLastPurchaseDate)
                }
            }
        }
        
        // Marcar la venta como cancelada
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        saleDao.cancelSale(id, com.negociolisto.app.domain.model.SaleStatus.CANCELED.name, now, reason)
        
        // Sincronizar cancelación con Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val updateData = hashMapOf<String, Any?>(
                        "status" to com.negociolisto.app.domain.model.SaleStatus.CANCELED.name,
                        "canceledAt" to now,
                        "canceledReason" to (reason ?: "")
                    )
                    
                    firestore.collection("users/${currentUser.id}/sales")
                        .document(id)
                        .update(updateData)
                        .await()
                    
                    println("✅ Venta $id cancelada automáticamente en Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error sincronizando cancelación de venta con Firebase: ${e.message}")
            }
        }
    }

    override suspend fun deleteSale(id: String) {
        // Eliminar localmente primero
        saleDao.deleteSaleById(id)
        
        // Eliminar de Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    firestore.collection("users/${currentUser.id}/sales")
                        .document(id)
                        .delete()
                        .await()
                    
                    println("✅ Venta $id eliminada automáticamente de Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error eliminando venta de Firebase: ${e.message}")
            }
        }
    }
}
