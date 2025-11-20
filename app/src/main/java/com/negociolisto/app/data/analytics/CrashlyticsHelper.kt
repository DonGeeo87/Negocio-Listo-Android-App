package com.negociolisto.app.data.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔥 Helper para Firebase Crashlytics
 * 
 * Centraliza el manejo de crashes y logs no fatales
 */
@Singleton
class CrashlyticsHelper @Inject constructor() {
    
    private val crashlytics: FirebaseCrashlytics = Firebase.crashlytics
    
    /**
     * Registra un log no fatal
     */
    fun log(message: String) {
        crashlytics.log(message)
    }
    
    /**
     * Registra una excepción no fatal
     */
    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
    
    /**
     * Establece un atributo personalizado
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
    
    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }
    
    fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }
    
    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }
    
    fun setCustomKey(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }
    
    /**
     * Establece el ID de usuario
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
    
    /**
     * Habilita o deshabilita la recolección de Crashlytics
     */
    fun setCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }
}

