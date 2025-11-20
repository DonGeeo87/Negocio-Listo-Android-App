package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime
import java.util.UUID

/**
 * 💬 MODELO DE MENSAJE DE CHAT
 * 
 * Representa un mensaje en el sistema de chat interno entre cliente y negocio.
 * Permite comunicación bidireccional en tiempo real.
 */
data class ChatMessage(
    /**
     * 🆔 ID ÚNICO DEL MENSAJE
     */
    val id: String = UUID.randomUUID().toString(),
    
    /**
     * 📚 ID DE LA COLECCIÓN
     * Colección a la que pertenece este chat
     */
    val collectionId: String,
    
    /**
     * 👤 TIPO DE REMITENTE
     * Si es BUSINESS (negocio) o CLIENT (cliente)
     */
    val senderType: SenderType,
    
    /**
     * 🆔 ID DEL REMITENTE
     * userId para BUSINESS o "client-{phone/email}" para CLIENT
     */
    val senderId: String,
    
    /**
     * 👤 NOMBRE DEL REMITENTE
     * Nombre que se muestra en el chat
     */
    val senderName: String,
    
    /**
     * 💬 CONTENIDO DEL MENSAJE
     */
    val message: String,
    
    /**
     * 📅 TIMESTAMP DEL MENSAJE
     */
    val timestamp: LocalDateTime,
    
    /**
     * ✅ SI EL MENSAJE FUE LEÍDO
     */
    val read: Boolean = false,
    
    /**
     * 📎 ARCHIVOS ADJUNTOS (FUTURO)
     * URLs de imágenes u otros archivos
     */
    val attachments: List<String> = emptyList()
)

/**
 * 👤 TIPO DE REMITENTE
 */
enum class SenderType {
    BUSINESS,  // Mensaje del negocio (desde la app Android)
    CLIENT     // Mensaje del cliente (desde la mini-web)
}
