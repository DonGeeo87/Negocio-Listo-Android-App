package com.negociolisto.app.data.local.dao

import androidx.room.*
import com.negociolisto.app.data.local.entity.CustomCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📂 DAO DE CATEGORÍAS PERSONALIZADAS
 * 
 * Define las operaciones de base de datos para categorías personalizadas.
 */
@Dao
interface CustomCategoryDao {
    
    @Query("SELECT * FROM custom_categories WHERE userId = :userId AND isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun getActiveCategoriesByUser(userId: String): Flow<List<CustomCategoryEntity>>
    
    @Query("SELECT * FROM custom_categories WHERE userId = :userId ORDER BY sortOrder ASC, name ASC")
    fun getAllCategoriesByUser(userId: String): Flow<List<CustomCategoryEntity>>
    
    @Query("SELECT * FROM custom_categories WHERE id = :id")
    suspend fun getCategoryById(id: String): CustomCategoryEntity?
    
    @Query("SELECT * FROM custom_categories WHERE userId = :userId AND name = :name AND isActive = 1")
    suspend fun getCategoryByName(userId: String, name: String): CustomCategoryEntity?
    
    @Query("SELECT * FROM custom_categories WHERE userId = :userId AND LOWER(name) = LOWER(:name) AND isActive = 1")
    suspend fun getCategoryByNameIgnoreCase(userId: String, name: String): CustomCategoryEntity?
    
    @Query("SELECT COUNT(*) FROM custom_categories WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveCategoryCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CustomCategoryEntity>)
    
    @Update
    suspend fun updateCategory(category: CustomCategoryEntity)
    
    @Query("UPDATE custom_categories SET isActive = 0 WHERE id = :id")
    suspend fun deactivateCategory(id: String)
    
    @Query("UPDATE custom_categories SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)
    
    @Query("UPDATE custom_categories SET sortOrder = sortOrder + 1 WHERE userId = :userId AND sortOrder >= :sortOrder")
    suspend fun incrementSortOrderFrom(userId: String, sortOrder: Int)
    
    @Delete
    suspend fun deleteCategory(category: CustomCategoryEntity)
    
    @Query("DELETE FROM custom_categories WHERE userId = :userId")
    suspend fun deleteAllCategoriesByUser(userId: String)
}
