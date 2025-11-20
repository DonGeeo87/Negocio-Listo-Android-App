package com.negociolisto.app.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.data.service.UsageLimitsService
import com.negociolisto.app.domain.model.ChatMessage
import com.negociolisto.app.domain.model.CollectionResponse
import com.negociolisto.app.domain.model.OrderStatus
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.SenderType
import com.negociolisto.app.domain.repository.ChatRepository
import com.negociolisto.app.domain.repository.CollectionResponseRepository
import com.negociolisto.app.domain.repository.InventoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔔 SERVICIO DE DETECCIÓN DE EVENTOS PARA NOTIFICACIONES
 * 
 * Escucha eventos en tiempo real y dispara notificaciones locales:
 * - Nuevos pedidos desde la web
 * - Mensajes de chat del cliente
 * - Límites de uso alcanzados
 * - Stock bajo en productos
 */
@Singleton
class NotificationTriggerService @Inject constructor(
    private val notificationHelper: NotificationHelper,
    private val collectionResponseRepository: CollectionResponseRepository,
    private val chatRepository: ChatRepository,
    private val inventoryRepository: InventoryRepository,
    private val usageLimitsService: UsageLimitsService,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isActive = false
    private val processedOrderIds = mutableSetOf<String>()
    private val processedMessageIds = mutableSetOf<String>()
    private val notifiedLowStockProducts = mutableSetOf<String>()
    private var lastLimitCheck: Map<String, Int> = emptyMap()
    
    /**
     * 🚀 INICIAR MONITOREO DE EVENTOS
     */
    fun startMonitoring(userId: String) {
        if (isActive) {
            println("⚠️ NotificationTriggerService ya está activo")
            return
        }
        
        isActive = true
        println("🔔 Iniciando monitoreo de notificaciones para usuario: $userId")
        
        // Limpiar datos previos
        processedOrderIds.clear()
        processedMessageIds.clear()
        notifiedLowStockProducts.clear()
        lastLimitCheck = emptyMap()
        
        // Iniciar monitoreo de pedidos
        startOrderMonitoring(userId)
        
        // Iniciar monitoreo de chat
        startChatMonitoring(userId)
        
        // Iniciar monitoreo de stock bajo
        startStockMonitoring()
        
        // Iniciar monitoreo de límites de uso
        startUsageLimitsMonitoring()
    }
    
    /**
     * 🛑 DETENER MONITOREO
     */
    fun stopMonitoring() {
        if (!isActive) return
        
        isActive = false
        println("🔔 Deteniendo monitoreo de notificaciones")
        serviceScope.cancel()
    }
    
    /**
     * 📦 MONITOREAR NUEVOS PEDIDOS
     */
    private fun startOrderMonitoring(userId: String) {
        // Obtener todas las colecciones del usuario
        serviceScope.launch {
            try {
                val collectionsSnapshot = firestore.collection("collections")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                collectionsSnapshot.documents.forEach { collectionDoc ->
                    val collectionId = collectionDoc.id
                    
                    // Escuchar nuevos pedidos en esta colección
                    collectionResponseRepository.getResponses(collectionId)
                        .onEach { responses ->
                            // Detectar nuevos pedidos
                            responses.forEach { response ->
                                // Solo notificar pedidos nuevos (APPROVED) que no hemos procesado
                                if (response.status == OrderStatus.APPROVED && 
                                    !processedOrderIds.contains(response.id)) {
                                    
                                    processedOrderIds.add(response.id)
                                    
                                    // Notificar solo si el pedido fue creado recientemente (últimos 5 minutos)
                                    val now = Clock.System.now()
                                    val createdAt = response.createdAt.toInstant(TimeZone.currentSystemDefault())
                                    val minutesAgo = (now.toEpochMilliseconds() - createdAt.toEpochMilliseconds()) / (1000 * 60)
                                    
                                    if (minutesAgo <= 5) {
                                        notificationHelper.showNewOrderNotification(
                                            orderId = response.id,
                                            clientName = response.clientName,
                                            collectionId = collectionId,
                                            itemCount = response.itemCount,
                                            total = response.subtotal
                                        )
                                        println("🔔 Notificación de nuevo pedido: ${response.id}")
                                    }
                                }
                            }
                        }
                        .launchIn(serviceScope)
                }
            } catch (e: Exception) {
                println("❌ Error monitoreando pedidos: ${e.message}")
            }
        }
    }
    
    /**
     * 💬 MONITOREAR MENSAJES DE CHAT DEL CLIENTE
     */
    private fun startChatMonitoring(userId: String) {
        serviceScope.launch {
            try {
                // Obtener todos los clientes del usuario
                val customersSnapshot = firestore.collection("customers")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                customersSnapshot.documents.forEach { customerDoc ->
                    val customerId = customerDoc.id
                    val customerName = customerDoc.getString("name") ?: "Cliente"
                    
                    // Obtener collectionId de alguna colección asociada o usar un valor por defecto
                    // Por ahora, escuchamos en el chat centralizado del cliente
                    val collectionId = customerDoc.getString("lastCollectionId") ?: customerId
                    
                    // Escuchar mensajes del cliente
                    chatRepository.getMessages(customerId, collectionId)
                        .onEach { messages ->
                            messages.forEach { message ->
                                // Solo notificar mensajes nuevos del cliente que no hemos procesado
                                if (message.senderType == SenderType.CLIENT && 
                                    !processedMessageIds.contains(message.id)) {
                                    
                                    processedMessageIds.add(message.id)
                                    
                                    // Notificar solo si el mensaje fue creado recientemente (últimos 2 minutos)
                                    val now = Clock.System.now()
                                    val messageTime = message.timestamp.toInstant(TimeZone.currentSystemDefault())
                                    val minutesAgo = (now.toEpochMilliseconds() - messageTime.toEpochMilliseconds()) / (1000 * 60)
                                    
                                    if (minutesAgo <= 2) {
                                        notificationHelper.showChatNotification(
                                            customerId = customerId,
                                            customerName = customerName,
                                            message = message.message,
                                            collectionId = message.collectionId
                                        )
                                        println("🔔 Notificación de chat: ${message.id}")
                                    }
                                }
                            }
                        }
                        .launchIn(serviceScope)
                }
            } catch (e: Exception) {
                println("❌ Error monitoreando chat: ${e.message}")
            }
        }
    }
    
    /**
     * 📦 MONITOREAR STOCK BAJO
     */
    private fun startStockMonitoring() {
        serviceScope.launch {
            inventoryRepository.getLowStockProducts()
                .onEach { lowStockProducts ->
                    lowStockProducts.forEach { product ->
                        // Notificar solo una vez por producto cuando pasa a stock bajo
                        if (!notifiedLowStockProducts.contains(product.id) && 
                            product.stockQuantity <= product.minimumStock) {
                            
                            notifiedLowStockProducts.add(product.id)
                            
                            notificationHelper.showLowStockNotification(
                                productName = product.name,
                                currentStock = product.stockQuantity,
                                minimumStock = product.minimumStock,
                                productId = product.id
                            )
                            println("🔔 Notificación de stock bajo: ${product.name}")
                        }
                    }
                    
                    // Limpiar productos que ya no están en stock bajo
                    val currentLowStockIds = lowStockProducts.map { product -> product.id }.toSet()
                    notifiedLowStockProducts.removeAll { productId -> productId !in currentLowStockIds }
                }
                .launchIn(serviceScope)
        }
    }
    
    /**
     * ⚠️ MONITOREAR LÍMITES DE USO
     */
    private fun startUsageLimitsMonitoring() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val stats = usageLimitsService.getUsageStatistics()
                    
                    // Verificar límite de productos
                    checkLimitAndNotify(
                        limitType = "productos",
                        currentCount = stats.products.current,
                        maxLimit = stats.products.limit,
                        percentage = stats.products.percentage,
                        status = stats.products.status
                    )
                    
                    // Verificar límite de clientes
                    checkLimitAndNotify(
                        limitType = "clientes",
                        currentCount = stats.customers.current,
                        maxLimit = stats.customers.limit,
                        percentage = stats.customers.percentage,
                        status = stats.customers.status
                    )
                    
                    // Verificar límite de colecciones
                    checkLimitAndNotify(
                        limitType = "colecciones",
                        currentCount = stats.collections.current,
                        maxLimit = stats.collections.limit,
                        percentage = stats.collections.percentage,
                        status = stats.collections.status
                    )
                    
                    // Verificar cada 30 minutos
                    delay(30 * 60 * 1000)
                } catch (e: Exception) {
                    println("❌ Error monitoreando límites: ${e.message}")
                    delay(5 * 60 * 1000) // Reintentar en 5 minutos si hay error
                }
            }
        }
    }
    
    /**
     * ✅ VERIFICAR LÍMITE Y NOTIFICAR SI ES NECESARIO
     */
    private suspend fun checkLimitAndNotify(
        limitType: String,
        currentCount: Int,
        maxLimit: Int,
        percentage: Int,
        status: com.negociolisto.app.data.service.LimitStatus
    ) {
        val key = limitType
        val lastPercentage = lastLimitCheck[key] ?: 0
        
        // Solo notificar si:
        // 1. Está en WARNING o CRITICAL
        // 2. El porcentaje cambió significativamente (más de 5%)
        if ((status == com.negociolisto.app.data.service.LimitStatus.WARNING || 
             status == com.negociolisto.app.data.service.LimitStatus.CRITICAL) &&
            kotlin.math.abs(percentage - lastPercentage) >= 5) {
            
            notificationHelper.showUsageLimitNotification(
                limitType = limitType,
                currentCount = currentCount,
                maxLimit = maxLimit,
                percentage = percentage,
                isCritical = status == com.negociolisto.app.data.service.LimitStatus.CRITICAL
            )
            
            println("🔔 Notificación de límite: $limitType al $percentage%")
        }
        
        lastLimitCheck = lastLimitCheck + (key to percentage)
    }
}


