package com.negociolisto.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.domain.model.ChatMessage
import com.negociolisto.app.domain.model.SenderType
import com.negociolisto.app.domain.repository.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 💬 IMPLEMENTACIÓN DEL REPOSITORIO DE CHAT CON FIRESTORE
 * 
 * Gestiona los mensajes del chat en tiempo real usando Firestore.
 * Soporta chat centralizado por cliente (customers/{customerId}/messages)
 * y chat por colección (collections/{collectionId}/messages) como fallback.
 */
@Singleton
class FirebaseChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    /**
     * Obtener referencia a mensajes del chat centralizado por cliente
     */
    private fun getCustomerMessagesRef(customerId: String) =
        firestore.collection("customers").document(customerId).collection("messages")

    /**
     * Obtener referencia a mensajes del chat por colección (fallback)
     */
    private fun getCollectionMessagesRef(collectionId: String) =
        firestore.collection("collections").document(collectionId).collection("messages")

    override fun getMessages(customerId: String?, collectionId: String): Flow<List<ChatMessage>> = callbackFlow {
        // Priorizar chat centralizado por cliente si customerId está disponible
        val messagesRef = if (customerId != null) {
            getCustomerMessagesRef(customerId)
        } else {
            getCollectionMessagesRef(collectionId)
        }
        
        val listener = messagesRef
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        // Manejar timestamp: puede ser string ISO o Timestamp de Firestore
                        val timestamp = when {
                            doc.get("timestamp") is com.google.firebase.Timestamp -> {
                                val firestoreTimestamp = doc.getTimestamp("timestamp")
                                val instant = Instant.fromEpochMilliseconds(firestoreTimestamp?.toDate()?.time ?: 0)
                                instant.toLocalDateTime(TimeZone.currentSystemDefault())
                            }
                            else -> {
                                val timestampStr = doc.getString("timestamp") ?: return@mapNotNull null
                                try {
                                    val timestampInstant = Instant.parse(timestampStr)
                                    timestampInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                                } catch (e: Exception) {
                                    // Si falla el parseo, usar timestamp actual como fallback
                                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                }
                            }
                        }

                        val senderTypeStr = doc.getString("senderType") ?: "BUSINESS"
                        val senderType = try {
                            SenderType.valueOf(senderTypeStr)
                        } catch (e: Exception) {
                            // Si el senderType no es válido, asumir BUSINESS
                            SenderType.BUSINESS
                        }

                        // Obtener collectionId del mensaje si está presente, sino usar el collectionId pasado como parámetro
                        val messageCollectionId = doc.getString("collectionId") ?: collectionId
                        
                        ChatMessage(
                            id = doc.id,
                            collectionId = messageCollectionId,
                            senderType = senderType,
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = timestamp,
                            read = doc.getBoolean("read") ?: false,
                            attachments = (doc.get("attachments") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        )
                    } catch (e: Exception) {
                        // Log del error pero no detener el flujo
                        android.util.Log.e("FirebaseChatRepository", "Error parsing message ${doc.id}: ${e.message}")
                        null
                    }
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(message: ChatMessage, customerId: String?) {
        try {
            // Usar serverTimestamp() para consistencia con la mini-web
            val data = hashMapOf(
                "collectionId" to message.collectionId,
                "senderType" to message.senderType.name,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "message" to message.message,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "read" to message.read,
                "attachments" to message.attachments
            )

            // Priorizar chat centralizado por cliente si customerId está disponible
            val messagesRef = if (customerId != null) {
                getCustomerMessagesRef(customerId)
            } else {
                getCollectionMessagesRef(message.collectionId)
            }

            messagesRef
                .document(message.id)
                .set(data)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al enviar mensaje: ${e.message}")
        }
    }

    override suspend fun markMessagesAsRead(customerId: String?, collectionId: String, messageIds: List<String>) {
        try {
            val batch = firestore.batch()
            // Priorizar chat centralizado por cliente si customerId está disponible
            val messagesRef = if (customerId != null) {
                getCustomerMessagesRef(customerId)
            } else {
                getCollectionMessagesRef(collectionId)
            }

            messageIds.forEach { messageId ->
                val messageRef = messagesRef.document(messageId)
                batch.update(messageRef, "read", true)
            }

            batch.commit().await()
        } catch (e: Exception) {
            throw Exception("Error al marcar mensajes como leídos: ${e.message}")
        }
    }
}
