package com.negociolisto.app.data.hybrid

import com.negociolisto.app.data.remote.firebase.FirebaseInventoryRepository
import com.negociolisto.app.data.repository.InventoryRepositoryImpl
import com.negociolisto.app.data.sync.SyncQueue
import com.negociolisto.app.data.sync.SyncOperation
import com.negociolisto.app.data.sync.SyncOperationType
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.StockMovement
import com.negociolisto.app.domain.model.StockMovementSummary
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.CategoryStats
import com.negociolisto.app.domain.repository.ProductSalesStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔄 REPOSITORIO HÍBRIDO DE INVENTARIO
 * 
 * Combina datos locales (Room) con sincronización en la nube (Firestore)
 * para proporcionar funcionalidad offline-first.
 */
@Singleton
class HybridInventoryRepository @Inject constructor(
    private val localRepository: InventoryRepositoryImpl,
    private val cloudRepository: FirebaseInventoryRepository,
    private val syncQueue: SyncQueue
) : InventoryRepository {

    override fun getAllProducts(): Flow<List<Product>> = flow {
        // Intentar obtener datos de la nube primero
        val cloudProducts = try {
            cloudRepository.getAllProducts().first()
        } catch (e: Exception) {
            emptyList()
        }
        
        if (cloudProducts.isNotEmpty()) {
            emit(cloudProducts)
        } else {
            // Si no hay datos en la nube, usar datos locales
            emit(localRepository.getAllProducts().first())
        }
    }.catch {
        // En caso de error en el Flow, usar datos locales
        emit(localRepository.getAllProducts().first())
    }

    override suspend fun getProductById(productId: String): Product? {
        return try {
            // Intentar obtener de la nube primero
            cloudRepository.getProductById(productId) ?: localRepository.getProductById(productId)
        } catch (e: Exception) {
            localRepository.getProductById(productId)
        }
    }

    override suspend fun getProductBySku(sku: String): Product? {
        return try {
            cloudRepository.getProductBySku(sku) ?: localRepository.getProductBySku(sku)
        } catch (e: Exception) {
            localRepository.getProductBySku(sku)
        }
    }

    override suspend fun addProduct(product: Product): String {
        return try {
            // Agregar localmente primero
            val localId = localRepository.addProduct(product)
            
            // Intentar sincronizar con la nube
            try {
                cloudRepository.addProduct(product)
            } catch (e: Exception) {
                // Si falla, agregar a la cola de sincronización
                syncQueue.enqueue(
                    SyncOperation(
                        id = "add_product_${product.id}_${System.currentTimeMillis()}",
                        type = SyncOperationType.CREATE_PRODUCT,
                        data = product,
                        execute = { cloudRepository.addProduct(product) }
                    )
                )
            }
            
            localId
        } catch (e: Exception) {
            throw Exception("Error al agregar producto: ${e.message}")
        }
    }

    override suspend fun updateProduct(product: Product) {
        try {
            // Actualizar localmente
            localRepository.updateProduct(product)
            
            // Intentar sincronizar con la nube
            try {
                cloudRepository.updateProduct(product)
            } catch (e: Exception) {
                // Si falla, agregar a la cola de sincronización
                syncQueue.enqueue(
                    SyncOperation(
                        id = "update_product_${product.id}_${System.currentTimeMillis()}",
                        type = SyncOperationType.UPDATE_PRODUCT,
                        data = product,
                        execute = { cloudRepository.updateProduct(product) }
                    )
                )
            }
        } catch (e: Exception) {
            throw Exception("Error al actualizar producto: ${e.message}")
        }
    }

    override suspend fun deleteProduct(productId: String) {
        try {
            // Eliminar localmente
            localRepository.deleteProduct(productId)
            
            // Intentar sincronizar con la nube
            try {
                cloudRepository.deleteProduct(productId)
            } catch (e: Exception) {
                // Si falla, agregar a la cola de sincronización
                syncQueue.enqueue(
                    SyncOperation(
                        id = "delete_product_${productId}_${System.currentTimeMillis()}",
                        type = SyncOperationType.DELETE_PRODUCT,
                        data = productId,
                        execute = { cloudRepository.deleteProduct(productId) }
                    )
                )
            }
        } catch (e: Exception) {
            throw Exception("Error al eliminar producto: ${e.message}")
        }
    }

    override fun searchProducts(query: String): Flow<List<Product>> = flow {
        try {
            val localResults = localRepository.searchProducts(query).first()
            val cloudResults = cloudRepository.searchProducts(query).first()
            
            // Combinar resultados, priorizando la nube
            if (cloudResults.isNotEmpty()) {
                emit(cloudResults)
            } else {
                emit(localResults)
            }
        } catch (e: Exception) {
            emit(localRepository.searchProducts(query).first())
        }
    }

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> = flow {
        try {
            val localResults = localRepository.getProductsByCategory(categoryId).first()
            val cloudResults = cloudRepository.getProductsByCategory(categoryId).first()
            
            if (cloudResults.isNotEmpty()) {
                emit(cloudResults)
            } else {
                emit(localResults)
            }
        } catch (e: Exception) {
            emit(localRepository.getProductsByCategory(categoryId).first())
        }
    }

    override fun getLowStockProducts(): Flow<List<Product>> = flow {
        try {
            val localResults = localRepository.getLowStockProducts().first()
            val cloudResults = cloudRepository.getLowStockProducts().first()
            
            if (cloudResults.isNotEmpty()) {
                emit(cloudResults)
            } else {
                emit(localResults)
            }
        } catch (e: Exception) {
            emit(localRepository.getLowStockProducts().first())
        }
    }

    override fun getProductsBySupplier(supplier: String): Flow<List<Product>> = flow {
        try {
            val localResults = localRepository.getProductsBySupplier(supplier).first()
            val cloudResults = cloudRepository.getProductsBySupplier(supplier).first()
            
            if (cloudResults.isNotEmpty()) {
                emit(cloudResults)
            } else {
                emit(localResults)
            }
        } catch (e: Exception) {
            emit(localRepository.getProductsBySupplier(supplier).first())
        }
    }

    override fun getOutOfStockProducts(): Flow<List<Product>> = flow {
        try {
            val localResults = localRepository.getOutOfStockProducts().first()
            val cloudResults = cloudRepository.getOutOfStockProducts().first()
            
            if (cloudResults.isNotEmpty()) {
                emit(cloudResults)
            } else {
                emit(localResults)
            }
        } catch (e: Exception) {
            emit(localRepository.getOutOfStockProducts().first())
        }
    }

    override suspend fun updateProductStock(
        productId: String,
        newQuantity: Int,
        reason: String,
        description: String?
    ) {
        try {
            // Actualizar localmente
            localRepository.updateProductStock(productId, newQuantity, reason, description)
            
            // Intentar sincronizar con la nube
            try {
                cloudRepository.updateProductStock(productId, newQuantity, reason, description)
            } catch (e: Exception) {
                // Si falla, agregar a la cola de sincronización
                syncQueue.enqueue(
                    SyncOperation(
                        id = "update_stock_${productId}_${System.currentTimeMillis()}",
                        type = SyncOperationType.UPDATE_STOCK,
                        data = mapOf(
                            "productId" to productId,
                            "newQuantity" to newQuantity,
                            "reason" to reason,
                            "description" to description
                        ),
                        execute = { 
                            cloudRepository.updateProductStock(productId, newQuantity, reason, description) 
                        }
                    )
                )
            }
        } catch (e: Exception) {
            throw Exception("Error al actualizar stock: ${e.message}")
        }
    }

    override suspend fun recordStockMovement(movement: StockMovement) {
        try {
            // Agregar localmente
            localRepository.recordStockMovement(movement)
            
            // Intentar sincronizar con la nube
            try {
                cloudRepository.recordStockMovement(movement)
            } catch (e: Exception) {
                // Si falla, agregar a la cola de sincronización
                syncQueue.enqueue(
                    SyncOperation(
                        id = "create_movement_${movement.id}_${System.currentTimeMillis()}",
                        type = SyncOperationType.CREATE_MOVEMENT,
                        data = movement,
                        execute = { cloudRepository.recordStockMovement(movement) }
                    )
                )
            }
        } catch (e: Exception) {
            throw Exception("Error al registrar movimiento de stock: ${e.message}")
        }
    }

    override fun getStockMovements(productId: String): Flow<List<StockMovement>> = flow {
        try {
            val localResults = localRepository.getStockMovements(productId).first()
            val cloudResults = cloudRepository.getStockMovements(productId).first()
            
            if (cloudResults.isNotEmpty()) {
                emit(cloudResults)
            } else {
                emit(localResults)
            }
        } catch (e: Exception) {
            emit(localRepository.getStockMovements(productId).first())
        }
    }

    override suspend fun getStockMovementSummary(
        productId: String,
        startDate: String?,
        endDate: String?
    ): StockMovementSummary? {
        return try {
            cloudRepository.getStockMovementSummary(productId, startDate, endDate)
        } catch (e: Exception) {
            localRepository.getStockMovementSummary(productId, startDate, endDate)
        }
    }

    override suspend fun getCategoryStatistics(): Map<String, CategoryStats> {
        return try {
            cloudRepository.getCategoryStatistics()
        } catch (e: Exception) {
            localRepository.getCategoryStatistics()
        }
    }

    override suspend fun getTopSellingProducts(limit: Int): List<ProductSalesStats> {
        return try {
            cloudRepository.getTopSellingProducts(limit)
        } catch (e: Exception) {
            localRepository.getTopSellingProducts(limit)
        }
    }

    override suspend fun getLeastSellingProducts(limit: Int): List<ProductSalesStats> {
        return try {
            cloudRepository.getLeastSellingProducts(limit)
        } catch (e: Exception) {
            localRepository.getLeastSellingProducts(limit)
        }
    }

    override suspend fun isSkuExists(sku: String, excludeProductId: String?): Boolean {
        return try {
            cloudRepository.isSkuExists(sku, excludeProductId) || 
            localRepository.isSkuExists(sku, excludeProductId)
        } catch (e: Exception) {
            localRepository.isSkuExists(sku, excludeProductId)
        }
    }

    override suspend fun isStockAvailable(productId: String, quantity: Int): Boolean {
        return try {
            cloudRepository.isStockAvailable(productId, quantity)
        } catch (e: Exception) {
            localRepository.isStockAvailable(productId, quantity)
        }
    }

    override suspend fun getTotalProductCount(): Int {
        return try {
            cloudRepository.getTotalProductCount()
        } catch (e: Exception) {
            localRepository.getTotalProductCount()
        }
    }

    override suspend fun getTotalInventoryValue(): Double {
        return try {
            cloudRepository.getTotalInventoryValue()
        } catch (e: Exception) {
            localRepository.getTotalInventoryValue()
        }
    }

    override suspend fun getTotalSaleValue(): Double {
        return try {
            cloudRepository.getTotalSaleValue()
        } catch (e: Exception) {
            localRepository.getTotalSaleValue()
        }
    }

    override suspend fun syncWithCloud(): Boolean {
        return try {
            // Sincronizar datos locales con la nube
            val localProducts = localRepository.getAllProducts().first()
            
            for (product in localProducts) {
                try {
                    cloudRepository.addProduct(product)
                } catch (e: Exception) {
                    // Continuar con el siguiente producto si falla uno
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 🔄 RESTAURAR DATOS DESDE LA NUBE
     */
    suspend fun restoreFromCloud(): Boolean {
        return try {
            // Obtener datos de la nube
            val cloudProducts = cloudRepository.getAllProducts().first()
            
            // Guardar en base de datos local
            for (product in cloudProducts) {
                try {
                    localRepository.addProduct(product)
                } catch (e: Exception) {
                    // Continuar con el siguiente producto si falla uno
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 🔄 PROCESAR COLA DE SINCRONIZACIÓN
     * 
     * Procesa todas las operaciones pendientes de sincronización
     */
    suspend fun processSyncQueue() {
        syncQueue.processQueue()
    }
    
    /**
     * 📊 OBTENER ESTADO DE LA COLA
     */
    fun getSyncQueueState() = syncQueue.pendingOperations
    
    /**
     * 🔄 OBTENER NÚMERO DE OPERACIONES PENDIENTES
     */
    fun getPendingSyncCount(): Int = syncQueue.getPendingCount()
}
