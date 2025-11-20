package com.negociolisto.app.data.service

import android.content.Context
import com.negociolisto.app.domain.model.CollectionResponse
import com.negociolisto.app.domain.repository.CollectionRepository
import com.negociolisto.app.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📧 SERVICIO DE EMAIL PARA PEDIDOS
 * 
 * Maneja el envío automático de emails cuando se crea un pedido.
 */
@Singleton
class OrderEmailService @Inject constructor(
    private val communicationService: CommunicationService,
    private val collectionRepository: CollectionRepository,
    private val inventoryRepository: InventoryRepository
) {
    
    /**
     * 📧 ENVIAR EMAIL DE CONFIRMACIÓN DE PEDIDO
     * 
     * Envía un email automático al cliente cuando se crea un nuevo pedido.
     * 
     * @param context Contexto de Android
     * @param response El pedido (CollectionResponse) que se acaba de crear
     */
    suspend fun sendOrderConfirmationEmail(
        context: Context,
        response: CollectionResponse
    ) {
        // Validar que el cliente tenga email
        val clientEmail = response.clientEmail
        if (clientEmail.isNullOrBlank()) {
            println("⚠️ Cliente no tiene email, no se envía confirmación")
            return
        }
        
        try {
            // Obtener información de la colección
            val collection = collectionRepository.getById(response.collectionId)
            val collectionName = collection?.name ?: "Colección"
            
            // Generar el contenido del email
            val subject = "✅ Confirmación de Pedido - $collectionName"
            val body = generateOrderEmailBody(response, collectionName)
            
            // Enviar email usando CommunicationService
            communicationService.sendEmail(context, clientEmail, subject, body)
            
            println("✅ Email de confirmación enviado a: $clientEmail")
        } catch (e: Exception) {
            println("❌ Error enviando email de confirmación: ${e.message}")
            // No lanzar excepción para no interrumpir el flujo de creación del pedido
        }
    }
    
    /**
     * 📧 GENERAR CUERPO DEL EMAIL
     * 
     * Genera el contenido HTML del email con los detalles del pedido.
     */
    private suspend fun generateOrderEmailBody(
        response: CollectionResponse,
        collectionName: String
    ): String {
        val itemsDetails = buildString {
            val products = inventoryRepository.getAllProducts().first()
            
            // Iterar sobre el Map items: (productId, orderItem)
            response.items.forEach { (productId, orderItem) ->
                val product = products.find { it.id == productId }
                val productName = product?.name ?: "Producto desconocido"
                val unitPrice = product?.salePrice ?: 0.0
                val quantity = orderItem.quantity
                val lineTotal = unitPrice * quantity
                
                appendLine("  • $productName")
                appendLine("    Cantidad: $quantity")
                appendLine("    Precio unitario: ${formatClp(unitPrice)}")
                appendLine("    Subtotal: ${formatClp(lineTotal)}")
                
                if (!orderItem.notes.isNullOrBlank()) {
                    appendLine("    Notas: ${orderItem.notes}")
                }
                appendLine()
            }
        }
        
        val statusMessage = when (response.status) {
            com.negociolisto.app.domain.model.OrderStatus.APPROVED -> 
                "Tu pedido ha sido recibido y está listo para procesar."
            com.negociolisto.app.domain.model.OrderStatus.IN_PRODUCTION -> 
                "Tu pedido está en producción. Pronto estará listo."
            com.negociolisto.app.domain.model.OrderStatus.READY_FOR_DELIVERY -> 
                "Tu pedido está listo para entrega."
            com.negociolisto.app.domain.model.OrderStatus.DELIVERED -> 
                "Tu pedido ha sido entregado."
            com.negociolisto.app.domain.model.OrderStatus.CANCELLED -> 
                "Tu pedido ha sido cancelado."
        }
        
        return """
Hola ${response.clientName},

¡Gracias por tu pedido!

Detalles del pedido:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Colección: $collectionName
Pedido ID: ${response.id}
Fecha: ${formatDate(response.createdAt)}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Productos solicitados:
$itemsDetails

Resumen:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Subtotal: ${formatClp(response.subtotal)}
Total: ${formatClp(response.subtotal)}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

$statusMessage

${if (!response.address.isNullOrBlank()) "Dirección de entrega:\n${response.address}\n" else ""}
${if (!response.observations.isNullOrBlank()) "Tus notas:\n${response.observations}\n" else ""}

Estamos procesando tu pedido y te mantendremos informado sobre su estado.

Saludos,
Tu equipo de NegocioListo
        """.trimIndent()
    }
    
    /**
     * 💰 FORMATEAR MONTO EN CLP
     */
    private fun formatClp(amount: Double): String {
        return "$ ${amount.toInt()}"
    }
    
    /**
     * 📅 FORMATEAR FECHA
     */
    private fun formatDate(dateTime: kotlinx.datetime.LocalDateTime): String {
        return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
    }
}
