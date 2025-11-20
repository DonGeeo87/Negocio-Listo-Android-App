package com.negociolisto.app.data.local.dao

import androidx.room.*
import com.negociolisto.app.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📦 DAO DE PRODUCTOS
 * 
 * Define las operaciones de base de datos para productos.
 */
@Dao
interface ProductDao {
    
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?
    
    @Query("SELECT * FROM products WHERE sku = :sku AND isActive = 1")
    suspend fun getProductBySku(sku: String): ProductEntity?
    
    @Query("SELECT * FROM products WHERE stockQuantity <= minimumStock AND isActive = 1")
    fun getLowStockProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE customCategoryId = :categoryId AND isActive = 1 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<ProductEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
    
    @Update
    suspend fun updateProduct(product: ProductEntity)
    
    @Query("UPDATE products SET stockQuantity = stockQuantity + :quantity WHERE id = :productId")
    suspend fun addStock(productId: String, quantity: Int)
    
    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId AND stockQuantity >= :quantity")
    suspend fun reduceStock(productId: String, quantity: Int): Int
    
    @Query("UPDATE products SET isActive = 0 WHERE id = :id")
    suspend fun deactivateProduct(id: String)
    
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getTotalProductCount(): Int
    
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)
    
    @Query("DELETE FROM products")
    suspend fun clearAllProducts()
}