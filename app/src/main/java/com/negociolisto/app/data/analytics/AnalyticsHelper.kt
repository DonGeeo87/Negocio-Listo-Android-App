package com.negociolisto.app.data.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📊 Helper para Firebase Analytics
 * 
 * Centraliza el tracking de eventos de Analytics en la aplicación
 */
@Singleton
class AnalyticsHelper @Inject constructor() {
    
    private val analytics: FirebaseAnalytics = Firebase.analytics
    
    /**
     * Registra un evento de Analytics
     */
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        try {
            // Logging para debug (solo en desarrollo)
            android.util.Log.d("Analytics", "📊 Enviando evento: $eventName con params: $params")
            
            // Usar la API KTX de Firebase Analytics
            Firebase.analytics.logEvent(eventName) {
                params.forEach { (key, value) ->
                    param(key, value.toString())
                }
            }
            
            android.util.Log.d("Analytics", "✅ Evento $eventName enviado exitosamente")
        } catch (e: Exception) {
            android.util.Log.e("Analytics", "❌ Error enviando evento $eventName: ${e.message}", e)
        }
    }
    
    /**
     * Eventos de Inventario
     */
    fun logProductAdded(productName: String, category: String) {
        logEvent("product_added", mapOf(
            "product_name" to productName,
            "category" to category
        ))
    }
    
    fun logProductUpdated(productName: String) {
        logEvent("product_updated", mapOf(
            "product_name" to productName
        ))
    }
    
    fun logProductDeleted(productName: String) {
        logEvent("product_deleted", mapOf(
            "product_name" to productName
        ))
    }
    
    /**
     * Eventos de Ventas
     */
    fun logSaleCreated(total: Double, itemCount: Int) {
        logEvent("sale_created", mapOf(
            "total" to total,
            "item_count" to itemCount
        ))
    }
    
    fun logInvoiceGenerated(invoiceNumber: String) {
        logEvent("invoice_generated", mapOf(
            "invoice_number" to invoiceNumber
        ))
    }
    
    /**
     * Eventos de Clientes
     */
    fun logCustomerAdded(customerName: String) {
        logEvent("customer_added", mapOf(
            "customer_name" to customerName
        ))
    }
    
    /**
     * Eventos de Colecciones
     */
    fun logCollectionShared(collectionId: String, template: String) {
        logEvent("collection_shared", mapOf(
            "collection_id" to collectionId,
            "template" to template
        ))
    }
    
    fun logOrderCreated(collectionId: String, orderValue: Double) {
        logEvent("order_created", mapOf(
            "collection_id" to collectionId,
            "order_value" to orderValue
        ))
    }
    
    /**
     * Eventos de Autenticación
     */
    fun logLogin(method: String = "email") {
        logEvent(FirebaseAnalytics.Event.LOGIN, mapOf(
            "method" to method
        ))
    }
    
    fun logSignUp(method: String = "email") {
        logEvent(FirebaseAnalytics.Event.SIGN_UP, mapOf(
            "method" to method
        ))
    }
    
    /**
     * Eventos de Navegación
     */
    fun logScreenView(screenName: String) {
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, mapOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName
        ))
    }
    
    /**
     * Establece propiedades del usuario
     */
    fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }
    
    /**
     * Establece el ID de usuario
     */
    fun setUserId(userId: String) {
        analytics.setUserId(userId)
    }
}

