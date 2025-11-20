package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * 💬 REPOSITORIO DE CHAT
 * 
 * Define las operaciones para gestionar mensajes del chat interno
 * entre cliente y negocio. Soporta chat centralizado por cliente
 * (customers/{customerId}/messages) y chat por colección
 * (collections/{collectionId}/messages) como fallback.
 */
interface ChatRepository {
    /**
     * 📋 OBTENER MENSAJES (CHAT CENTRALIZADO POR CLIENTE)
     * 
     * Si customerId no es null, lee de customers/{customerId}/messages (chat centralizado).
     * Si customerId es null, lee de collections/{collectionId}/messages (fallback).
     * 
     * @param customerId ID del cliente (prioritario para chat centralizado)
     * @param collectionId ID de la colección (usado como fallback o para mantener collectionId en el mensaje)
     * @return Flow con la lista de mensajes en tiempo real
     */
    fun getMessages(customerId: String?, collectionId: String): Flow<List<ChatMessage>>
    
    /**
     * ➕ ENVIAR MENSAJE (CHAT CENTRALIZADO POR CLIENTE)
     * 
     * Si customerId no es null, envía a customers/{customerId}/messages (chat centralizado).
     * Si customerId es null, envía a collections/{collectionId}/messages (fallback).
     * 
     * @param message Mensaje a enviar (debe contener collectionId y customerId si está disponible)
     * @param customerId ID del cliente (prioritario para chat centralizado)
     */
    suspend fun sendMessage(message: ChatMessage, customerId: String?)
    
    /**
     * ✅ MARCAR MENSAJES COMO LEÍDOS
     * 
     * @param customerId ID del cliente (si es chat centralizado)
     * @param collectionId ID de la colección (si es chat por colección)
     * @param messageIds IDs de los mensajes a marcar como leídos
     */
    suspend fun markMessagesAsRead(customerId: String?, collectionId: String, messageIds: List<String>)
}
