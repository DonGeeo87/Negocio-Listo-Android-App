package com.negociolisto.app.data.repository

import com.negociolisto.app.data.local.dao.CustomCategoryDao
import com.negociolisto.app.domain.model.CustomCategory
import com.negociolisto.app.domain.model.CustomCategoryFactory
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📂 IMPLEMENTACIÓN DEL REPOSITORIO DE CATEGORÍAS PERSONALIZADAS
 * 
 * Implementa las operaciones de base de datos para categorías personalizadas.
 */
@Singleton
class CustomCategoryRepositoryImpl @Inject constructor(
    private val customCategoryDao: CustomCategoryDao
) : CustomCategoryRepository {
    
    override fun getActiveCategoriesByUser(userId: String): Flow<List<CustomCategory>> {
        return customCategoryDao.getActiveCategoriesByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAllCategoriesByUser(userId: String): Flow<List<CustomCategory>> {
        return customCategoryDao.getAllCategoriesByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getCategoryById(categoryId: String): CustomCategory? {
        return customCategoryDao.getCategoryById(categoryId)?.toDomain()
    }
    
    override suspend fun getCategoryByName(userId: String, name: String): CustomCategory? {
        return customCategoryDao.getCategoryByName(userId, name)?.toDomain()
    }
    
    override suspend fun addCategory(category: CustomCategory): String {
        // Verificar que no exista una categoría con el mismo nombre (case-insensitive)
        val existing = getCategoryByName(category.userId, category.name)
        if (existing != null) {
            throw IllegalArgumentException("Ya existe una categoría con el nombre '${category.name}'")
        }
        
        // Validación adicional: verificar por nombre en minúsculas
        val existingByName = customCategoryDao.getCategoryByNameIgnoreCase(category.userId, category.name.lowercase())
        if (existingByName != null) {
            throw IllegalArgumentException("Ya existe una categoría con un nombre similar: '${existingByName.name}'")
        }
        
        // Obtener el siguiente orden disponible
        val currentCount = getActiveCategoryCount(category.userId)
        val categoryWithOrder = category.copy(sortOrder = currentCount)
        
        customCategoryDao.insertCategory(com.negociolisto.app.data.local.entity.CustomCategoryEntity.fromDomain(categoryWithOrder))
        return categoryWithOrder.id
    }
    
    override suspend fun updateCategory(category: CustomCategory) {
        // Verificar que no exista otra categoría con el mismo nombre
        val existing = getCategoryByName(category.userId, category.name)
        if (existing != null && existing.id != category.id) {
            throw IllegalArgumentException("Ya existe una categoría con el nombre '${category.name}'")
        }
        
        customCategoryDao.updateCategory(com.negociolisto.app.data.local.entity.CustomCategoryEntity.fromDomain(category))
    }
    
    override suspend fun deleteCategory(categoryId: String) {
        customCategoryDao.deactivateCategory(categoryId)
    }

    override suspend fun deleteAllCategoriesByUser(userId: String) {
        customCategoryDao.deleteAllCategoriesByUser(userId)
    }
    
    override suspend fun getActiveCategoryCount(userId: String): Int {
        return customCategoryDao.getActiveCategoryCount(userId)
    }
    
    override suspend fun updateCategoryOrder(categoryId: String, newOrder: Int) {
        customCategoryDao.updateSortOrder(categoryId, newOrder)
    }
    
    // Método eliminado: No se crean categorías predeterminadas
    // El usuario debe crear sus propias categorías
}
