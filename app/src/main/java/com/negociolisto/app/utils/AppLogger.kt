package com.negociolisto.app.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Utilidad centralizada para logging en la aplicación.
 * 
 * Proporciona logging consistente tanto en logcat como en Firebase Crashlytics.
 * 
 * Uso:
 * ```
 * AppLogger.d("MainActivity", "Usuario inició sesión")
 * AppLogger.e("Repository", "Error al cargar datos", exception)
 * ```
 * 
 * Desarrollador: Giorgio Interdonato Palacios
 * GitHub: @DonGeeo87
 */
object AppLogger {
    
    const val DEFAULT_TAG = "NegocioListo"
    private const val MAX_TAG_LENGTH = 23 // Límite de Android para tags
    
    /**
     * Log de DEBUG - Información detallada para desarrollo
     */
    fun d(tag: String, message: String) {
        val safeTag = sanitizeTag(tag)
        Log.d(safeTag, message)
    }
    
    /**
     * Log de INFO - Información general del flujo de la app
     */
    fun i(tag: String, message: String) {
        val safeTag = sanitizeTag(tag)
        Log.i(safeTag, message)
    }
    
    /**
     * Log de WARNING - Situaciones que requieren atención pero no son críticas
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val safeTag = sanitizeTag(tag)
        if (throwable != null) {
            Log.w(safeTag, message, throwable)
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } else {
            Log.w(safeTag, message)
        }
    }
    
    /**
     * Log de ERROR - Errores que deben ser investigados
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val safeTag = sanitizeTag(tag)
        if (throwable != null) {
            Log.e(safeTag, message, throwable)
            FirebaseCrashlytics.getInstance().recordException(throwable)
            FirebaseCrashlytics.getInstance().log("$safeTag: $message")
        } else {
            Log.e(safeTag, message)
            FirebaseCrashlytics.getInstance().log("$safeTag: $message")
        }
    }
    
    /**
     * Log de VERBOSE - Información muy detallada (solo en builds de debug)
     */
    fun v(tag: String, message: String) {
        val safeTag = sanitizeTag(tag)
        Log.v(safeTag, message)
    }
    
    /**
     * Log de métricas o eventos importantes
     */
    fun logEvent(tag: String, eventName: String, parameters: Map<String, String>? = null) {
        val safeTag = sanitizeTag(tag)
        val paramsStr = parameters?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
        val message = if (paramsStr.isNotEmpty()) {
            "$eventName | $paramsStr"
        } else {
            eventName
        }
        Log.i(safeTag, message)
        FirebaseCrashlytics.getInstance().log("$safeTag: $message")
    }
    
    /**
     * Sanitiza el tag para cumplir con las limitaciones de Android
     */
    private fun sanitizeTag(tag: String): String {
        return if (tag.length > MAX_TAG_LENGTH) {
            tag.take(MAX_TAG_LENGTH)
        } else {
            tag
        }
    }
    
    /**
     * Helper para logging con contexto de clase
     */
    inline fun <reified T> T.logd(message: String) {
        AppLogger.d(T::class.simpleName ?: DEFAULT_TAG, message)
    }
    
    inline fun <reified T> T.logi(message: String) {
        AppLogger.i(T::class.simpleName ?: DEFAULT_TAG, message)
    }
    
    inline fun <reified T> T.logw(message: String, throwable: Throwable? = null) {
        AppLogger.w(T::class.simpleName ?: DEFAULT_TAG, message, throwable)
    }
    
    inline fun <reified T> T.loge(message: String, throwable: Throwable? = null) {
        AppLogger.e(T::class.simpleName ?: DEFAULT_TAG, message, throwable)
    }
}

