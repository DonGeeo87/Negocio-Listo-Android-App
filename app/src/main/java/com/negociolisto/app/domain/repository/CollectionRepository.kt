package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.Collection
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun getCollections(): Flow<List<Collection>>
    fun searchCollections(query: String): Flow<List<Collection>>
    suspend fun getById(id: String): Collection?
    suspend fun addCollection(collection: Collection)
    suspend fun updateCollection(collection: Collection)
    suspend fun deleteCollection(id: String)
    
    /**
     * 🎨 ACTUALIZAR TEMPLATE GLOBALMENTE POR CLIENTE
     * 
     * Actualiza el template de todas las colecciones asociadas a un cliente específico.
     * Esto asegura que todas las colecciones del mismo cliente usen el mismo template
     * en el portal del cliente.
     * 
     * @param customerId ID del cliente
     * @param template Template a aplicar a todas las colecciones del cliente
     */
    suspend fun updateTemplateForCustomer(customerId: String, template: com.negociolisto.app.domain.model.CollectionWebTemplate)
    
    /**
     * 📊 OBTENER TOTAL DE COLECCIONES
     * 
     * Cuenta el total de colecciones en el sistema.
     * 
     * @return Número total de colecciones
     */
    suspend fun getTotalCollectionCount(): Int
}


