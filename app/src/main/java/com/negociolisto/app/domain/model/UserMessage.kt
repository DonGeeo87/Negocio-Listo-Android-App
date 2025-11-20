package com.negociolisto.app.domain.model

/**
 * 💬 MENSAJE DE USUARIO
 * 
 * Representa un mensaje que se muestra al usuario en la interfaz.
 * Puede ser de diferentes tipos (información, éxito, error, advertencia).
 */
data class UserMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    INFO,
    SUCCESS,
    ERROR,
    WARNING
}










