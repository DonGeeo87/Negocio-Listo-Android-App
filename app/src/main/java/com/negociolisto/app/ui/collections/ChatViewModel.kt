package com.negociolisto.app.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.ChatMessage
import com.negociolisto.app.domain.model.SenderType
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * 💬 VIEWMODEL DE CHAT
 * 
 * Soporta chat centralizado por cliente (customers/{customerId}/messages)
 * y chat por colección (collections/{collectionId}/messages) como fallback.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val collectionRepository: com.negociolisto.app.domain.repository.CollectionRepository
) : ViewModel() {
    
    private var currentCollectionId: String? = null
    private var currentCustomerId: String? = null
    private var messagesJob: kotlinx.coroutines.Job? = null
    
    private val _messages = kotlinx.coroutines.flow.MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(1000),
        emptyList()
    )
    
    fun loadMessages(collectionId: String) {
        // Si es la misma colección, no recargar
        if (currentCollectionId == collectionId && messagesJob?.isActive == true) {
            return
        }
        
        currentCollectionId = collectionId

        // Cancelar suscripción previa para evitar colecciones duplicadas
        messagesJob?.cancel()

        messagesJob = viewModelScope.launch {
            // Obtener el customerId de la colección (usar el primero si hay múltiples)
            val collection = try {
                collectionRepository.getById(collectionId)
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error obteniendo colección: ${e.message}")
                null
            }
            currentCustomerId = collection?.associatedCustomerIds?.firstOrNull()
            
            android.util.Log.d("ChatViewModel", "Cargando mensajes - CollectionId: $collectionId, CustomerId: $currentCustomerId")
            
            // Usar chat centralizado por cliente si hay customerId, sino usar chat por colección
            chatRepository.getMessages(currentCustomerId, collectionId)
                .map { msgs ->
                    // Ordenar por timestamp (más antiguo primero) y luego invertir
                    // para que el más reciente quede al final
                    // Con reverseLayout = true en LazyColumn, el más reciente aparecerá abajo
                    msgs.sortedBy { msg ->
                        try {
                            msg.timestamp.toInstant(TimeZone.currentSystemDefault())
                        } catch (e: Exception) {
                            // Si falla la conversión, usar un timestamp muy antiguo para que aparezca al principio
                            Instant.fromEpochMilliseconds(0)
                        }
                    }.reversed() // Invertir para que el más reciente quede al final
                }
                .collect { sortedMsgs ->
                    _messages.value = sortedMsgs
                }
        }
    }
    
    fun sendMessage(messageText: String) {
        val collectionId = currentCollectionId ?: return
        val customerId = currentCustomerId
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val message = ChatMessage(
                collectionId = collectionId,
                senderType = SenderType.BUSINESS,
                senderId = user.id,
                senderName = user.name,
                message = messageText,
                timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
            // Usar chat centralizado por cliente si hay customerId
            chatRepository.sendMessage(message, customerId)
        }
    }
}
