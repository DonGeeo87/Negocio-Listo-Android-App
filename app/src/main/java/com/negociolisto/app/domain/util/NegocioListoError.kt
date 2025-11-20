package com.negociolisto.app.domain.util

/**
 * 🚨 TIPOS DE ERRORES DE NEGOCIO LISTO
 * 
 * Definición de los diferentes tipos de errores que puede manejar la aplicación.
 */
sealed class NegocioListoError(message: String) : Exception(message) {
    
    /**
     * Error de autenticación (login, registro, etc.)
     */
    class AuthenticationError(message: String) : NegocioListoError(message)
    
    /**
     * Error de validación de datos
     */
    class ValidationError(field: String, message: String) : NegocioListoError("$field: $message")
    
    /**
     * Error de red o conexión
     */
    class NetworkError(message: String) : NegocioListoError(message)
    
    /**
     * Error de regla de negocio
     */
    class BusinessRuleError(message: String) : NegocioListoError(message)
    
    /**
     * Error de stock insuficiente
     */
    class InsufficientStockError(
        val productId: String,
        val productName: String,
        val requestedQuantity: Int,
        val availableStock: Int
    ) : NegocioListoError(
        "Stock insuficiente para ${productName}. Solicitado: $requestedQuantity, Disponible: $availableStock"
    )
    
    /**
     * Error desconocido
     */
    class UnknownError(message: String) : NegocioListoError(message)
}











