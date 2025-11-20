package com.negociolisto.app.notification

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎫 ADMINISTRADOR DE TOKENS FCM
 * 
 * Gestiona los tokens de Firebase Cloud Messaging:
 * - Obtiene el token del dispositivo
 * - Guarda el token en Firestore asociado al usuario
 * - Actualiza el token cuando cambia
 */
@Singleton
class NotificationTokenManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Obtiene el token FCM actual y lo guarda en Firestore
     * @param userId ID del usuario autenticado
     */
    suspend fun saveTokenForUser(userId: String): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            
            // Guardar token en Firestore en la colección de usuarios
            firestore.collection("users")
                .document(userId)
                .set(
                    mapOf(
                        "fcmToken" to token,
                        "fcmTokenUpdatedAt" to com.google.firebase.Timestamp.now()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()
            
            println("✅ Token FCM guardado para usuario $userId")
            token
        } catch (e: Exception) {
            println("❌ Error guardando token FCM: ${e.message}")
            null
        }
    }

    /**
     * Actualiza el token FCM para un usuario
     * @param userId ID del usuario autenticado
     * @param newToken Nuevo token FCM
     */
    suspend fun updateTokenForUser(userId: String, newToken: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "fcmToken" to newToken,
                        "fcmTokenUpdatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            println("✅ Token FCM actualizado para usuario $userId")
        } catch (e: Exception) {
            println("❌ Error actualizando token FCM: ${e.message}")
        }
    }

    /**
     * Elimina el token FCM de un usuario (al cerrar sesión)
     * @param userId ID del usuario
     */
    suspend fun removeTokenForUser(userId: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", com.google.firebase.firestore.FieldValue.delete())
                .await()
            
            println("✅ Token FCM eliminado para usuario $userId")
        } catch (e: Exception) {
            println("❌ Error eliminando token FCM: ${e.message}")
        }
    }

    /**
     * Obtiene el token FCM del dispositivo sin guardarlo
     */
    suspend fun getCurrentToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            println("❌ Error obteniendo token FCM: ${e.message}")
            null
        }
    }
}
