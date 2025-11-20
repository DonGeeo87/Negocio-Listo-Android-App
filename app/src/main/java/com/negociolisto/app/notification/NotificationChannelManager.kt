package com.negociolisto.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📢 ADMINISTRADOR DE CANALES DE NOTIFICACIÓN
 * 
 * Crea y gestiona los canales de notificación para organizar las notificaciones
 * por tipo (chat, aprobaciones, pedidos, general).
 */
@Singleton
class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val CHANNEL_CHAT_ID = "chat_notifications"
        const val CHANNEL_APPROVALS_ID = "approval_notifications"
        const val CHANNEL_ORDERS_ID = "order_notifications"
        const val CHANNEL_GENERAL_ID = "general_notifications"
        const val CHANNEL_STOCK_ID = "stock_notifications"
        const val CHANNEL_LIMITS_ID = "limits_notifications"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Crea todos los canales de notificación necesarios
     */
    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_CHAT_ID,
                    "Mensajes de Chat",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de mensajes nuevos en el chat de colecciones"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_APPROVALS_ID,
                    "Aprobaciones",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones cuando hay aprobaciones pendientes o completadas"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_ORDERS_ID,
                    "Nuevos Pedidos",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de nuevos pedidos recibidos"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_GENERAL_ID,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones generales del sistema"
                },
                NotificationChannel(
                    CHANNEL_STOCK_ID,
                    "Stock Bajo",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones cuando productos tienen stock bajo"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_LIMITS_ID,
                    "Límites de Uso",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones cuando te acercas a los límites de uso"
                    enableVibration(true)
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
