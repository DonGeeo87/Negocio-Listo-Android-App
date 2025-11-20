package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime
import java.util.UUID

/**
 * 📋 MODELO DE RESPUESTA DE COLECCIÓN (PEDIDO)
 * 
 * Representa un pedido realizado por un cliente desde la mini-web pública.
 * Incluye información del cliente, productos solicitados, métodos de pago y entrega.
 */
data class CollectionResponse(
    /**
     * 🆔 ID ÚNICO DEL PEDIDO
     */
    val id: String = UUID.randomUUID().toString(),
    
    /**
     * 📚 ID DE LA COLECCIÓN
     * Colección desde la que se realizó el pedido
     */
    val collectionId: String,
    
    // ========== INFORMACIÓN DEL CLIENTE ==========
    
    /**
     * 👤 ID DEL CLIENTE (OPCIONAL)
     * ID del cliente registrado en la app, si existe
     */
    val customerId: String? = null,
    
    /**
     * 🔑 TOKEN DE ACCESO (OPCIONAL)
     * Token único usado para acceder al portal del cliente
     */
    val accessToken: String? = null,
    
    /**
     * 👤 NOMBRE DEL CLIENTE
     */
    val clientName: String,
    
    /**
     * 📧 EMAIL DEL CLIENTE
     */
    val clientEmail: String,
    
    /**
     * 📞 TELÉFONO DEL CLIENTE
     */
    val clientPhone: String,
    
    // ========== DATOS DEL PEDIDO ==========
    
    /**
     * 🚚 MÉTODO DE ENTREGA
     * "retiro" | "despacho" | "evento"
     */
    val deliveryMethod: String,
    
    /**
     * 🏠 DIRECCIÓN DE ENTREGA
     */
    val address: String?,
    
    /**
     * 💳 MÉTODO DE PAGO
     * "efectivo" | "transferencia" | "link"
     */
    val paymentMethod: String,
    
    /**
     * 📅 FECHA DESEADA DE ENTREGA
     */
    val desiredDate: LocalDateTime?,
    
    /**
     * ⚠️ PEDIDO URGENTE
     */
    val urgent: Boolean = false,
    
    /**
     * 📝 OBSERVACIONES GENERALES
     */
    val observations: String?,
    
    // ========== ITEMS DEL PEDIDO ==========
    
    /**
     * 📦 ITEMS DEL PEDIDO
     * Mapa de productId -> información del item
     */
    val items: Map<String, OrderItem>,
    
    // ========== TOTALES ==========
    
    /**
     * 💰 SUBTOTAL
     */
    val subtotal: Double,
    
    /**
     * 🔢 CANTIDAD DE ITEMS
     */
    val itemCount: Int,
    
    // ========== ESTADO ACTUAL ==========
    
    /**
     * 📊 ESTADO DEL PEDIDO
     */
    val status: OrderStatus,
    
    // ========== FEEDBACK Y OBSERVACIONES ==========
    
    /**
     * 💬 COMENTARIOS DEL CLIENTE
     */
    val feedbackComments: String?,
    
    /**
     * ✅ CONSENTIMIENTO DE CONTACTO
     */
    val consentToContact: Boolean = false,
    
    /**
     * 📝 NOTAS INTERNAS DEL NEGOCIO
     */
    val businessNotes: String?,
    
    // ========== METADATOS ==========
    
    /**
     * 📍 UBICACIÓN
     */
    val location: OrderLocation?,
    
    /**
     * 🏷️ ETIQUETAS
     */
    val tags: List<String> = emptyList(),
    
    /**
     * 📅 FECHA DE CREACIÓN
     */
    val createdAt: LocalDateTime,
    
    /**
     * 🔄 FECHA DE ÚLTIMA ACTUALIZACIÓN
     */
    val updatedAt: LocalDateTime
)

/**
 * 📦 ITEM DEL PEDIDO
 */
data class OrderItem(
    /**
     * 🔢 CANTIDAD
     */
    val quantity: Int,
    
    /**
     * ⭐ CALIFICACIÓN (1-5 estrellas)
     */
    val rating: Int?,
    
    /**
     * 📝 NOTAS DEL CLIENTE
     */
    val notes: String?,
    
    /**
     * 🎨 PERSONALIZACIÓN
     */
    val customization: String?
)

/**
 * 📊 ESTADO DEL PEDIDO
 */
enum class OrderStatus(val displayName: String) {
    APPROVED("Aprobado"),
    IN_PRODUCTION("En Producción"),
    READY_FOR_DELIVERY("Listo para Entrega"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");
    
    /**
     * 🎨 COLOR PARA EL ESTADO
     */
    fun getColor(): String {
        return when (this) {
            APPROVED -> "#28A745"                    // Verde
            IN_PRODUCTION -> "#17A2B8"               // Azul
            READY_FOR_DELIVERY -> "#007BFF"          // Azul oscuro
            DELIVERED -> "#6C757D"                   // Gris
            CANCELLED -> "#DC3545"                   // Rojo
        }
    }
}

/**
 * 📍 UBICACIÓN DEL PEDIDO
 */
data class OrderLocation(
    /**
     * 🏙️ CIUDAD
     */
    val city: String?,
    
    /**
     * 🗺️ REGIÓN
     */
    val region: String?
)
