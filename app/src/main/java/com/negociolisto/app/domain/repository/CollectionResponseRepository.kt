package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.CollectionResponse
import kotlinx.coroutines.flow.Flow

/**
 * 📋 REPOSITORIO DE RESPUESTAS DE COLECCIÓN (PEDIDOS)
 * 
 * Define las operaciones para gestionar pedidos de clientes.
 */
interface CollectionResponseRepository {
    /**
     * 📋 OBTENER RESPUESTAS DE UNA COLECCIÓN
     * 
     * @param collectionId ID de la colección
     * @return Flow con la lista de respuestas/pedidos
     */
    fun getResponses(collectionId: String): Flow<List<CollectionResponse>>
    
    /**
     * 🔍 OBTENER RESPUESTA POR ID
     * 
     * @param responseId ID de la respuesta
     * @return La respuesta o null si no existe
     */
    suspend fun getResponseById(responseId: String): CollectionResponse?
    
    /**
     * ➕ CREAR RESPUESTA (PEDIDO)
     * 
     * @param response Respuesta/pedido a crear
     */
    suspend fun addResponse(response: CollectionResponse)
    
    /**
     * ✏️ ACTUALIZAR RESPUESTA
     * 
     * @param response Respuesta actualizada
     */
    suspend fun updateResponse(response: CollectionResponse)
    
    /**
     * 📊 ACTUALIZAR ESTADO DEL PEDIDO
     * 
     * @param responseId ID de la respuesta
     * @param status Nuevo estado
     */
    suspend fun updateStatus(responseId: String, status: com.negociolisto.app.domain.model.OrderStatus)
}
