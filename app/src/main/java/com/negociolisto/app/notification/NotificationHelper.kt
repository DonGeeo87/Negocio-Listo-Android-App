package com.negociolisto.app.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.negociolisto.app.MainActivity
import com.negociolisto.app.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔔 HELPER DE NOTIFICACIONES
 * 
 * Clase centralizada para crear y mostrar notificaciones locales.
 * Unifica la lógica de creación de notificaciones para toda la app.
 */
@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context,
    private val notificationChannelManager: NotificationChannelManager
) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * 📦 MOSTRAR NOTIFICACIÓN DE NUEVO PEDIDO
     */
    fun showNewOrderNotification(
        orderId: String,
        clientName: String,
        collectionId: String,
        itemCount: Int,
        total: Double
    ) {
        val title = "🛒 Nuevo Pedido"
        val body = "$clientName realizó un pedido de $itemCount producto${if (itemCount != 1) "s" else ""} por $${total.toInt()}"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificationType", "order")
            putExtra("collectionId", collectionId)
            putExtra("responseId", orderId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ORDERS_ID)
            .setSmallIcon(R.drawable.icon_negociolisto)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
        
        notificationManager.notify(orderId.hashCode(), notification)
    }
    
    /**
     * 💬 MOSTRAR NOTIFICACIÓN DE MENSAJE DE CHAT
     */
    fun showChatNotification(
        customerId: String?,
        customerName: String,
        message: String,
        collectionId: String
    ) {
        val title = "💬 Mensaje de $customerName"
        val body = if (message.length > 100) {
            message.take(100) + "..."
        } else {
            message
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificationType", "chat")
            putExtra("collectionId", collectionId)
            customerId?.let { putExtra("customerId", it) }
        }
        
        val notificationId = (customerId ?: collectionId).hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_CHAT_ID)
            .setSmallIcon(R.drawable.icon_negociolisto)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * ⚠️ MOSTRAR NOTIFICACIÓN DE LÍMITE DE USO
     */
    fun showUsageLimitNotification(
        limitType: String,
        currentCount: Int,
        maxLimit: Int,
        percentage: Int,
        isCritical: Boolean
    ) {
        val title = if (isCritical) {
            "🚨 Límite Crítico Alcanzado"
        } else {
            "⚠️ Límite de Uso Cercano"
        }
        
        val body = when {
            isCritical -> "Has alcanzado el ${percentage}% de tu límite de $limitType ($currentCount/$maxLimit). Considera actualizar tu plan."
            else -> "Has usado el ${percentage}% de tu límite de $limitType ($currentCount/$maxLimit)."
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificationType", "limit")
            putExtra("limitType", limitType)
        }
        
        val notificationId = "limit_$limitType".hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = NotificationChannelManager.CHANNEL_LIMITS_ID
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon_negociolisto)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * 📦 MOSTRAR NOTIFICACIÓN DE STOCK BAJO
     */
    fun showLowStockNotification(
        productName: String,
        currentStock: Int,
        minimumStock: Int,
        productId: String
    ) {
        val title = "⚠️ Stock Bajo"
        val body = "$productName tiene solo $currentStock unidades (mínimo: $minimumStock)"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificationType", "stock")
            putExtra("productId", productId)
        }
        
        val notificationId = productId.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = NotificationChannelManager.CHANNEL_STOCK_ID
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon_negociolisto)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * 📊 MOSTRAR NOTIFICACIÓN DE RESUMEN DE STOCK BAJO
     */
    fun showLowStockSummaryNotification(lowStockCount: Int) {
        val title = "⚠️ Productos con Stock Bajo"
        val body = "Tienes $lowStockCount producto${if (lowStockCount != 1) "s" else ""} con stock bajo. Revisa tu inventario."
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificationType", "stock_summary")
        }
        
        val notificationId = "stock_summary".hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = NotificationChannelManager.CHANNEL_STOCK_ID
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon_negociolisto)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
}

