package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.CustomCategory
import kotlinx.coroutines.flow.Flow

/**
 * 📂 REPOSITORIO DE CATEGORÍAS PERSONALIZADAS
 * 
 * Define las operaciones para gestionar categorías personalizadas del usuario.
 */
interface CustomCategoryRepository {
    
    /**
     * 📋 OBTENER CATEGORÍAS ACTIVAS DEL USUARIO
     * 
     * @param userId ID del usuario
     * @return Flow con la lista de categorías activas ordenadas
     */
    fun getActiveCategoriesByUser(userId: String): Flow<List<CustomCategory>>
    
    /**
     * 📋 OBTENER TODAS LAS CATEGORÍAS DEL USUARIO
     * 
     * @param userId ID del usuario
     * @return Flow con todas las categorías (activas e inactivas)
     */
    fun getAllCategoriesByUser(userId: String): Flow<List<CustomCategory>>
    
    /**
     * 🔍 OBTENER CATEGORÍA POR ID
     * 
     * @param categoryId ID de la categoría
     * @return La categoría o null si no existe
     */
    suspend fun getCategoryById(categoryId: String): CustomCategory?
    
    /**
     * 🔍 OBTENER CATEGORÍA POR NOMBRE
     * 
     * @param userId ID del usuario
     * @param name Nombre de la categoría
     * @return La categoría o null si no existe
     */
    suspend fun getCategoryByName(userId: String, name: String): CustomCategory?
    
    /**
     * ➕ AGREGAR NUEVA CATEGORÍA
     * 
     * @param category Categoría a agregar
     * @return ID de la categoría creada
     */
    suspend fun addCategory(category: CustomCategory): String
    
    /**
     * ✏️ ACTUALIZAR CATEGORÍA
     * 
     * @param category Categoría actualizada
     */
    suspend fun updateCategory(category: CustomCategory)
    
    /**
     * 🗑️ ELIMINAR CATEGORÍA (ELIMINACIÓN LÓGICA)
     * 
     * @param categoryId ID de la categoría a eliminar
     */
    suspend fun deleteCategory(categoryId: String)
    
    /**
     * 🗑️ ELIMINAR TODAS LAS CATEGORÍAS DEL USUARIO
     * 
     * @param userId ID del usuario
     */
    suspend fun deleteAllCategoriesByUser(userId: String)
    
    /**
     * 📊 OBTENER CANTIDAD DE CATEGORÍAS ACTIVAS
     * 
     * @param userId ID del usuario
     * @return Número de categorías activas
     */
    suspend fun getActiveCategoryCount(userId: String): Int
    
    /**
     * 🔄 ACTUALIZAR ORDEN DE CATEGORÍAS
     * 
     * @param categoryId ID de la categoría
     * @param newOrder Nuevo orden
     */
    suspend fun updateCategoryOrder(categoryId: String, newOrder: Int)
    
    /**
     * 🏭 INICIALIZAR CATEGORÍAS PREDEFINIDAS
     * 
     * Crea las categorías por defecto para un nuevo usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de categorías creadas
     */
    // Método eliminado: No se crean categorías predeterminadas
}
