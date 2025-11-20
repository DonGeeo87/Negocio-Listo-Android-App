package com.negociolisto.app.data.remote.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.StockMovement
import com.negociolisto.app.domain.model.StockMovementSummary
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.CategoryStats
import com.negociolisto.app.domain.repository.ProductSalesStats
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔥 IMPLEMENTACIÓN DE INVENTORY REPOSITORY CON FIRESTORE
 * 
 * Esta clase implementa el InventoryRepository usando Firebase Firestore
 * para sincronización en la nube.
 */
@Singleton
class FirebaseInventoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : InventoryRepository {

    private val productsCollection = firestore.collection("products")
    private val stockMovementsCollection = firestore.collection("stock_movements")
    
    /**
     * 🔄 CONVERTIR DOCUMENTO DE FIRESTORE A PRODUCT
     * 
     * Convierte manualmente un DocumentSnapshot de Firestore a un objeto Product,
     * manejando valores faltantes y conversión de fechas.
     */
    private fun DocumentSnapshot.toProduct(): Product? {
        return try {
            val data = this.data ?: return null
            
            // Obtener valores requeridos
            val name = data["name"] as? String ?: return null
            val sku = data["sku"] as? String ?: return null
            val purchasePrice = (data["purchasePrice"] as? Number)?.toDouble() ?: return null
            val salePrice = (data["salePrice"] as? Number)?.toDouble() ?: return null
            val stockQuantity = (data["stockQuantity"] as? Number)?.toInt() ?: return null
            val customCategoryId = data["customCategoryId"] as? String ?: return null
            
            // Valores opcionales
            val description = data["description"] as? String
            val supplier = data["supplier"] as? String
            val photoUrl = data["photoUrl"] as? String
            val thumbnailUrl = data["thumbnailUrl"] as? String
            val imageBackupUrl = data["imageBackupUrl"] as? String
            val minimumStock = (data["minimumStock"] as? Number)?.toInt() ?: 5
            val isActive = data["isActive"] as? Boolean ?: true
            
            // Convertir fechas de Timestamp a LocalDateTime
            val createdAt = when (val createdAtValue = data["createdAt"]) {
                is Timestamp -> {
                    Instant.fromEpochMilliseconds(createdAtValue.toDate().time)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                }
                is Long -> {
                    Instant.fromEpochMilliseconds(createdAtValue)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                }
                else -> {
                    // Si no hay fecha, usar fecha actual
                    kotlinx.datetime.Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                }
            }
            
            val updatedAt = when (val updatedAtValue = data["updatedAt"]) {
                is Timestamp -> {
                    Instant.fromEpochMilliseconds(updatedAtValue.toDate().time)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                }
                is Long -> {
                    Instant.fromEpochMilliseconds(updatedAtValue)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                }
                else -> createdAt
            }
            
            Product(
                id = this.id,
                name = name,
                description = description,
                sku = sku,
                purchasePrice = purchasePrice,
                salePrice = salePrice,
                stockQuantity = stockQuantity,
                minimumStock = minimumStock,
                customCategoryId = customCategoryId,
                supplier = supplier,
                photoUrl = photoUrl,
                thumbnailUrl = thumbnailUrl,
                imageBackupUrl = imageBackupUrl,
                createdAt = createdAt,
                updatedAt = updatedAt,
                isActive = isActive
            )
        } catch (e: Exception) {
            println("⚠️ Error al convertir documento a Product: ${e.message}")
            null
        }
    }

    override fun getAllProducts(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toProduct()
                } ?: emptyList()

                trySend(products)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getProductById(productId: String): Product? {
        return try {
            val doc = productsCollection.document(productId).get().await()
            doc.toProduct()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getProductBySku(sku: String): Product? {
        return try {
            val query = productsCollection
                .whereEqualTo("sku", sku)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()

            query.documents.firstOrNull()?.let { doc ->
                doc.toProduct()
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addProduct(product: Product): String {
        return try {
            val docRef = productsCollection.add(product).await()
            docRef.id
        } catch (e: Exception) {
            throw Exception("Error al agregar producto: ${e.message}")
        }
    }

    override suspend fun updateProduct(product: Product) {
        try {
            productsCollection.document(product.id).set(product).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar producto: ${e.message}")
        }
    }

    override suspend fun deleteProduct(productId: String) {
        try {
            // Soft delete - marcar como inactivo
            productsCollection.document(productId).update("isActive", false).await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar producto: ${e.message}")
        }
    }

    override fun searchProducts(query: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val allProducts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Product>()?.copy(id = doc.id)
                } ?: emptyList()

                val filteredProducts = allProducts.filter { product ->
                    product.name.contains(query, ignoreCase = true) ||
                    product.sku.contains(query, ignoreCase = true) ||
                    product.description?.contains(query, ignoreCase = true) == true
                }

                trySend(filteredProducts)
            }

        awaitClose { listener.remove() }
    }

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("customCategoryId", categoryId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toProduct()
                } ?: emptyList()

                trySend(products)
            }

        awaitClose { listener.remove() }
    }

    override fun getLowStockProducts(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val allProducts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Product>()?.copy(id = doc.id)
                } ?: emptyList()

                val lowStockProducts = allProducts.filter { product ->
                    product.stockQuantity <= product.minimumStock
                }

                trySend(lowStockProducts)
            }

        awaitClose { listener.remove() }
    }

    override fun getProductsBySupplier(supplier: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("supplier", supplier)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toProduct()
                } ?: emptyList()

                trySend(products)
            }

        awaitClose { listener.remove() }
    }

    override fun getOutOfStockProducts(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("currentStock", 0)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toProduct()
                } ?: emptyList()

                trySend(products)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateProductStock(
        productId: String,
        newQuantity: Int,
        reason: String,
        description: String?
    ) {
        try {
            productsCollection.document(productId).update("currentStock", newQuantity).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar stock: ${e.message}")
        }
    }

    override suspend fun recordStockMovement(movement: StockMovement) {
        try {
            stockMovementsCollection.add(movement).await()
        } catch (e: Exception) {
            throw Exception("Error al registrar movimiento: ${e.message}")
        }
    }

    override fun getStockMovements(productId: String): Flow<List<StockMovement>> = callbackFlow {
        val listener = stockMovementsCollection
            .whereEqualTo("productId", productId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val movements = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<StockMovement>()?.copy(id = doc.id)
                } ?: emptyList()

                trySend(movements)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getStockMovementSummary(
        productId: String,
        startDate: String?,
        endDate: String?
    ): StockMovementSummary? {
        return try {
            val results = stockMovementsCollection
                .whereEqualTo("productId", productId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val movements = results.documents.mapNotNull { doc ->
                doc.toObject<StockMovement>()?.copy(id = doc.id)
            }
            
            val filteredMovements = if (startDate != null && endDate != null) {
                movements.filter { movement ->
                    movement.timestamp.toString() >= startDate && movement.timestamp.toString() <= endDate
                }
            } else {
                movements
            }

            if (filteredMovements.isEmpty()) return null

            val totalIn = filteredMovements.filter { it.movementType.name == "IN" }.sumOf { it.quantity }
            val totalOut = filteredMovements.filter { it.movementType.name == "OUT" }.sumOf { it.quantity }
            val totalValueIn = filteredMovements.filter { it.movementType.name == "IN" }.sumOf { it.quantity * (it.unitCost ?: 0.0) }
            val totalValueOut = filteredMovements.filter { it.movementType.name == "OUT" }.sumOf { it.quantity * (it.unitCost ?: 0.0) }

            StockMovementSummary(
                productId = productId,
                totalIn = totalIn,
                totalOut = totalOut,
                totalValueIn = totalValueIn,
                totalValueOut = totalValueOut,
                movementCount = filteredMovements.size,
                lastMovementDate = filteredMovements.maxByOrNull { it.timestamp }?.timestamp
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTotalProductCount(): Int {
        return try {
            val results = productsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()
            results.size()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getTotalInventoryValue(): Double {
        return try {
            val results = productsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()

            results.documents.sumOf { doc ->
                val product = doc.toProduct()
                (product?.stockQuantity ?: 0) * (product?.purchasePrice ?: 0.0)
            }
        } catch (e: Exception) {
            0.0
        }
    }

    override suspend fun getTotalSaleValue(): Double {
        return try {
            val results = productsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()

            results.documents.sumOf { doc ->
                val product = doc.toProduct()
                (product?.stockQuantity ?: 0) * (product?.salePrice ?: 0.0)
            }
        } catch (e: Exception) {
            0.0
        }
    }

    override suspend fun getCategoryStatistics(): Map<String, CategoryStats> {
        return try {
            val results = productsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val products = results.documents.mapNotNull { doc ->
                doc.toProduct()
            }

            products.groupBy { it.customCategoryId }.mapValues { (categoryId, categoryProducts) ->
                CategoryStats(
                    categoryId = categoryId,
                    productCount = categoryProducts.size,
                    totalStock = categoryProducts.sumOf { it.stockQuantity },
                    totalValue = categoryProducts.sumOf { it.stockQuantity * it.purchasePrice },
                    totalSaleValue = categoryProducts.sumOf { it.stockQuantity * it.salePrice },
                    lowStockCount = categoryProducts.count { it.stockQuantity <= it.minimumStock }
                )
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override suspend fun getTopSellingProducts(limit: Int): List<ProductSalesStats> {
        // Implementación simplificada - en un caso real necesitarías datos de ventas
        return try {
            val results = productsCollection
                .whereEqualTo("isActive", true)
                .limit(limit.toLong())
                .get()
                .await()

            results.documents.mapNotNull { doc ->
                val product = doc.toProduct()
                product?.let {
                    ProductSalesStats(
                        product = it,
                        totalSold = 0, // Necesitarías datos reales de ventas
                        totalSalesValue = 0.0,
                        transactionCount = 0,
                        lastSaleDate = null
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getLeastSellingProducts(limit: Int): List<ProductSalesStats> {
        return getTopSellingProducts(limit) // Implementación simplificada
    }

    override suspend fun isSkuExists(sku: String, excludeProductId: String?): Boolean {
        return try {
            val query = productsCollection
                .whereEqualTo("sku", sku)
                .whereEqualTo("isActive", true)

            val results = query.get().await()
            
            results.documents.any { doc ->
                doc.id != excludeProductId
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun isStockAvailable(productId: String, quantity: Int): Boolean {
        return try {
            val doc = productsCollection.document(productId).get().await()
            val product = doc.toProduct()
            product?.stockQuantity ?: 0 >= quantity
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun syncWithCloud(): Boolean {
        // Para Firebase, la sincronización es automática
        return true
    }
}

