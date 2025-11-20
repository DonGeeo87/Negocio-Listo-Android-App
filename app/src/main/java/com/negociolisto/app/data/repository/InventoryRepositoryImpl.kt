package com.negociolisto.app.data.repository

import com.negociolisto.app.data.local.dao.ProductDao
import com.negociolisto.app.data.local.dao.StockMovementDao
import com.negociolisto.app.data.local.entity.toDomain
import com.negociolisto.app.data.local.entity.toEntity
import com.negociolisto.app.data.local.entity.StockMovementEntity
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.CustomCategory
import com.negociolisto.app.domain.model.StockMovement
import com.negociolisto.app.domain.model.StockMovementSummary
import com.negociolisto.app.domain.model.StockMovementType
import com.negociolisto.app.domain.model.StockMovementReason
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.CategoryStats
import com.negociolisto.app.domain.repository.ProductSalesStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📦 IMPLEMENTACIÓN DEL REPOSITORIO DE INVENTARIO
 * 
 * Maneja los datos de productos usando Room Database.
 */
@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao,
    private val customCategoryRepository: CustomCategoryRepository,
    private val authRepository: AuthRepository
) : InventoryRepository {
    
    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            // Cargar categorías personalizadas para la conversión
            val customCategories = try {
                kotlinx.coroutines.runBlocking {
                    // Usar el UID real del usuario autenticado
                    val userId = authRepository.currentUser.first()?.id
                    if (userId != null && userId.isNotEmpty()) {
                        customCategoryRepository.getActiveCategoriesByUser(userId).first()
                    } else {
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                println("❌ ERROR cargando categorías personalizadas en getAllProducts: ${e.message}")
                emptyList()
            }
            
            println("🔥 DEBUG InventoryRepositoryImpl.getAllProducts:")
            println("🔥   - Total entities: ${entities.size}")
            println("🔥   - Custom categories loaded: ${customCategories.size}")
            
            entities.map { entity ->
                val product = entity.toDomain()
                println("🔥   - Product: ${product.name}, customCategoryId: ${product.customCategoryId}")
                product
            }
        }
    }
    
    override suspend fun getProductById(id: String): Product? {
        val entity = productDao.getProductById(id) ?: return null
        println("🔥 DEBUG InventoryRepositoryImpl.getProductById:")
        println("🔥   - Entity ID: ${entity.id}")
        println("🔥   - Entity customCategoryId: ${entity.customCategoryId}")
        
        // Cargar categorías personalizadas para la conversión
        val customCategories = try {
            kotlinx.coroutines.runBlocking {
                // Usar el UID real del usuario autenticado
                val userId = authRepository.currentUser.first()?.id
                if (userId != null && userId.isNotEmpty()) {
                    customCategoryRepository.getActiveCategoriesByUser(userId).first()
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            println("❌ ERROR cargando categorías personalizadas: ${e.message}")
            emptyList()
        }
        
        println("🔥   - Custom categories loaded: ${customCategories.size}")
        customCategories.forEach { cat ->
            println("🔥     - ${cat.name} (${cat.id})")
        }
        
        val product = entity.toDomain()
        println("🔥   - Final product customCategoryId: ${product.customCategoryId}")
        return product
    }
    
    override suspend fun getProductBySku(sku: String): Product? {
        return productDao.getProductBySku(sku)?.toDomain()
    }
    
    override suspend fun addProduct(product: Product): String {
        val entity = product.toEntity()
        println("🔥 DEBUG InventoryRepositoryImpl.addProduct:")
        println("🔥   - Product ID: ${product.id}")
        println("🔥   - Product name: ${product.name}")
        println("🔥   - Product customCategoryId: ${product.customCategoryId}")
        println("🔥   - Entity customCategoryId: ${entity.customCategoryId}")
        productDao.insertProduct(entity)
        return product.id
    }
    
    override suspend fun updateProduct(product: Product) {
        val entity = product.toEntity()
        println("🔥 DEBUG InventoryRepositoryImpl.updateProduct:")
        println("🔥   - Product ID: ${product.id}")
        println("🔥   - Product name: ${product.name}")
        println("🔥   - Product customCategoryId: ${product.customCategoryId}")
        println("🔥   - Entity customCategoryId: ${entity.customCategoryId}")
        productDao.updateProduct(entity)
    }
    
    override suspend fun deleteProduct(productId: String) {
        productDao.deleteProductById(productId)
    }
    
    // Implementaciones de búsqueda y filtrado
    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.filter { 
                it.name.contains(query, ignoreCase = true) ||
                it.sku.contains(query, ignoreCase = true) ||
                it.description?.contains(query, ignoreCase = true) == true
            }.map { it.toDomain() }
        }
    }
    
    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> {
        // Buscar productos por customCategoryId
        return productDao.getAllProducts().map { entities ->
            entities.filter { it.customCategoryId == categoryId }
                .map { it.toDomain() }
        }
    }
    
    override fun getProductsBySupplier(supplier: String): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.filter { it.supplier?.contains(supplier, ignoreCase = true) == true }
                .map { it.toDomain() }
        }
    }
    
    override fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getOutOfStockProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.filter { it.stockQuantity <= 0 }.map { it.toDomain() }
        }
    }
    override suspend fun recordStockMovement(movement: StockMovement) {
        // Insertar el movimiento
        stockMovementDao.insertMovement(movement.toEntity())
        
        // Actualizar el stock del producto
        val product = getProductById(movement.productId)
        if (product != null) {
            val updatedProduct = product.copy(
                stockQuantity = movement.newStock,
                updatedAt = movement.timestamp
            )
            updateProduct(updatedProduct)
        }
    }
    
    override suspend fun updateProductStock(productId: String, newQuantity: Int, reason: String, description: String?) {
        val product = getProductById(productId) ?: return
        val previousStock = product.stockQuantity
        val quantityChange = newQuantity - previousStock
        
        // Mapear el motivo string al enum StockMovementReason
        val movementReason = try {
            StockMovementReason.valueOf(reason)
        } catch (e: IllegalArgumentException) {
            // Si no se encuentra el motivo, usar uno por defecto según el tipo de movimiento
            if (quantityChange > 0) {
                StockMovementReason.ADJUSTMENT_INCREASE
            } else {
                StockMovementReason.ADJUSTMENT_DECREASE
            }
        }
        
        // Crear movimiento de stock
        val movement = StockMovement(
            productId = productId,
            movementType = if (quantityChange > 0) StockMovementType.IN else StockMovementType.OUT,
            quantity = kotlin.math.abs(quantityChange),
            reason = movementReason,
            description = description,
            previousStock = previousStock,
            newStock = newQuantity,
            timestamp = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            referenceId = null,
            unitCost = null,
            userId = null,
            notes = null
        )
        
        recordStockMovement(movement)
    }
    
    override fun getStockMovements(productId: String): Flow<List<StockMovement>> {
        return stockMovementDao.getMovementsByProduct(productId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getStockMovementSummary(productId: String, startDate: String?, endDate: String?): StockMovementSummary? {
        val startTimestamp = startDate?.let { 
            kotlinx.datetime.LocalDateTime.parse(it).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        } ?: 0L
        val endTimestamp = endDate?.let {
            kotlinx.datetime.LocalDateTime.parse(it).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        } ?: System.currentTimeMillis()
        
        val summary = if (startDate != null || endDate != null) {
            stockMovementDao.getMovementSummaryByDateRange(productId, startTimestamp, endTimestamp)
        } else {
            stockMovementDao.getMovementSummary(productId)
        }
        
        return summary?.let {
            StockMovementSummary(
                productId = productId,
                totalIn = it.totalIn,
                totalOut = it.totalOut,
                totalValueIn = it.totalValueIn,
                totalValueOut = it.totalValueOut,
                lastMovementDate = null,
                movementCount = it.movementCount
            )
        }
    }
    override suspend fun getTotalProductCount(): Int {
        return productDao.getTotalProductCount()
    }
    
    override suspend fun getTotalInventoryValue(): Double {
        return productDao.getAllProducts().map { products ->
            products.sumOf { it.purchasePrice * it.stockQuantity }
        }.let { flow ->
            var value = 0.0
            flow.collect { value = it }
            value
        }
    }
    
    override suspend fun getTotalSaleValue(): Double {
        return productDao.getAllProducts().map { products ->
            products.sumOf { it.salePrice * it.stockQuantity }
        }.let { flow ->
            var value = 0.0
            flow.collect { value = it }
            value
        }
    }
    
    override suspend fun getCategoryStatistics(): Map<String, CategoryStats> {
        // TODO: Implementar estadísticas por categoría usando customCategoryId
        // Por ahora retornar mapa vacío hasta que se implemente correctamente
        return emptyMap()
    }
    
    override suspend fun getTopSellingProducts(limit: Int): List<ProductSalesStats> {
        // Implementación simplificada - en una implementación real se usarían datos de ventas
        return productDao.getAllProducts().map { entities ->
            entities.take(limit).map { entity ->
                ProductSalesStats(
                    product = entity.toDomain(),
                    totalSold = 0,
                    totalSalesValue = 0.0,
                    transactionCount = 0,
                    lastSaleDate = null
                )
            }
        }.let { flow ->
            var stats = emptyList<ProductSalesStats>()
            flow.collect { stats = it }
            stats
        }
    }
    
    override suspend fun getLeastSellingProducts(limit: Int): List<ProductSalesStats> {
        // Implementación simplificada
        return getTopSellingProducts(limit)
    }
    
    override suspend fun isSkuExists(sku: String, excludeProductId: String?): Boolean {
        val existingProduct = getProductBySku(sku)
        return existingProduct != null && existingProduct.id != excludeProductId
    }
    
    override suspend fun isStockAvailable(productId: String, quantity: Int): Boolean {
        val product = getProductById(productId)
        return product?.stockQuantity?.let { it >= quantity } ?: false
    }
    
    override suspend fun syncWithCloud(): Boolean {
        // Implementación simplificada - en una implementación real se sincronizaría con Firebase
        return true
    }
}