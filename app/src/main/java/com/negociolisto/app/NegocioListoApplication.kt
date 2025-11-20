package com.negociolisto.app

import android.app.Application
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.FirebasePerformance
import com.negociolisto.app.notification.NotificationChannelManager
import com.negociolisto.app.notification.NotificationTokenManager
import com.negociolisto.app.utils.AppLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 🚀 APLICACIÓN PRINCIPAL DE NEGOCIO LISTO
 * 
 * Clase principal de la aplicación Android con Hilt configurado.
 */
@HiltAndroidApp
class NegocioListoApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        // Hilt se inicializa automáticamente
        
        // 📝 Inicializar logging
        AppLogger.i("NegocioListoApplication", "🚀 Aplicación iniciada")
        
        // 🔥 Configurar Firebase Crashlytics
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(true)
        AppLogger.d("NegocioListoApplication", "Firebase Crashlytics habilitado")
        
        // 📊 Configurar Firebase Analytics
        val analytics = FirebaseAnalytics.getInstance(this)
        analytics.setAnalyticsCollectionEnabled(true)
        AppLogger.d("NegocioListoApplication", "Firebase Analytics habilitado")
        
        // ⚡ Configurar Firebase Performance Monitoring
        val performance = FirebasePerformance.getInstance()
        performance.isPerformanceCollectionEnabled = true
        AppLogger.d("NegocioListoApplication", "Firebase Performance Monitoring habilitado")
        
        com.negociolisto.app.ui.invoices.InvoiceSettingsStore.init(this)
        
        // Inicializar servicios de notificaciones manualmente (no podemos usar @Inject aquí)
        val notificationChannelManager = NotificationChannelManager(this)
        val tokenManager = NotificationTokenManager(FirebaseFirestore.getInstance())
        
        // Crear canales de notificación
        applicationScope.launch {
            try {
                notificationChannelManager.createChannels()
                AppLogger.d("NegocioListoApplication", "Canales de notificación creados")
            } catch (e: Exception) {
                AppLogger.e("NegocioListoApplication", "Error al crear canales de notificación", e)
            }
        }
        
        // Inicializar token FCM si hay usuario autenticado
        applicationScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    tokenManager.saveTokenForUser(firebaseUser.uid)
                    AppLogger.d("NegocioListoApplication", "Token FCM guardado para usuario: ${firebaseUser.uid}")
                } else {
                    AppLogger.d("NegocioListoApplication", "No hay usuario autenticado al iniciar")
                }
            } catch (e: Exception) {
                AppLogger.e("NegocioListoApplication", "Error al inicializar token FCM", e)
            }
        }
        
        // Escuchar cambios de autenticación para actualizar token
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            applicationScope.launch {
                try {
                    val user = auth.currentUser
                    if (user != null) {
                        tokenManager.saveTokenForUser(user.uid)
                        AppLogger.d("NegocioListoApplication", "Token FCM actualizado para usuario: ${user.uid}")
                    }
                } catch (e: Exception) {
                    AppLogger.e("NegocioListoApplication", "Error al actualizar token FCM", e)
                }
            }
        }
        
        AppLogger.i("NegocioListoApplication", "✅ Inicialización completada")
    }
}