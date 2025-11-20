package com.negociolisto.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.negociolisto.app.MainActivity
import com.negociolisto.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🔔 SERVICIO DE NOTIFICACIONES FCM
 * 
 * Maneja las notificaciones push recibidas desde Firebase Cloud Messaging.
 * Se activa automáticamente cuando llega una notificación.
 */
@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager
    
    @Inject
    lateinit var tokenManager: NotificationTokenManager
    
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notificationChannelManager.createChannels()
    }

    /**
     * Se llama cuando se recibe un mensaje FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Priorizar datos del payload "data" si están disponibles
        // Esto permite más control sobre la notificación
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "NegocioListo"
            val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: ""
            val type = remoteMessage.data["type"] ?: "general"
            val collectionId = remoteMessage.data["collectionId"]
            val responseId = remoteMessage.data["responseId"]

            showNotification(
                title = title,
                body = body,
                type = type,
                collectionId = collectionId,
                responseId = responseId
            )
        } else {
            // Si no hay datos, usar el payload de notificación estándar
            remoteMessage.notification?.let { notification ->
                showNotification(
                    title = notification.title ?: "NegocioListo",
                    body = notification.body ?: "",
                    type = "general"
                )
            }
        }
    }

    /**
     * Se llama cuando se obtiene un nuevo token FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("🔔 Nuevo token FCM: $token")
        
        // Guardar token automáticamente si hay usuario autenticado
        serviceScope.launch {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                tokenManager.updateTokenForUser(userId, token)
            } else {
                println("⚠️ No hay usuario autenticado, token no guardado")
            }
        }
    }

    /**
     * Muestra una notificación local
     */
    private fun showNotification(
        title: String,
        body: String,
        type: String,
        collectionId: String? = null,
        responseId: String? = null
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Determinar canal según el tipo
        val channelId = when (type) {
            "chat" -> NotificationChannelManager.CHANNEL_CHAT_ID
            "approval" -> NotificationChannelManager.CHANNEL_APPROVALS_ID
            "order" -> NotificationChannelManager.CHANNEL_ORDERS_ID
            else -> NotificationChannelManager.CHANNEL_GENERAL_ID
        }

        // Intent para abrir la app cuando se toca la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            collectionId?.let { putExtra("collectionId", it) }
            responseId?.let { putExtra("responseId", it) }
            putExtra("notificationType", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon_negociolisto)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Mostrar notificación
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }
}
