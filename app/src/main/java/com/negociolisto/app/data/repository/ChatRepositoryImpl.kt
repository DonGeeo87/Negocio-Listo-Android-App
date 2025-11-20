package com.negociolisto.app.data.repository

import com.negociolisto.app.data.remote.firebase.FirebaseChatRepository
import com.negociolisto.app.domain.model.ChatMessage
import com.negociolisto.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 💬 IMPLEMENTACIÓN DEL REPOSITORIO DE CHAT
 * 
 * Delega a FirebaseChatRepository para tiempo real con Firestore.
 * Soporta chat centralizado por cliente y chat por colección como fallback.
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firebaseChatRepository: FirebaseChatRepository
) : ChatRepository {
    
    override fun getMessages(customerId: String?, collectionId: String): Flow<List<ChatMessage>> {
        return firebaseChatRepository.getMessages(customerId, collectionId)
    }
    
    override suspend fun sendMessage(message: ChatMessage, customerId: String?) {
        firebaseChatRepository.sendMessage(message, customerId)
    }
    
    override suspend fun markMessagesAsRead(customerId: String?, collectionId: String, messageIds: List<String>) {
        firebaseChatRepository.markMessagesAsRead(customerId, collectionId, messageIds)
    }
}
