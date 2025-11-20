package com.negociolisto.app.domain.util

/**
 * 🛠️ MANEJADOR DE ERRORES DE NEGOCIO LISTO
 * 
 * Clase para manejar y procesar errores de manera centralizada.
 */
class ErrorHandler {
    
    /**
     * Procesa un error y devuelve un mensaje amigable para el usuario
     */
    fun handleError(error: Throwable): String {
        return when (error) {
            is NegocioListoError.AuthenticationError -> "Error de autenticación: ${error.message}"
            is NegocioListoError.ValidationError -> "Error de validación: ${error.message}"
            is NegocioListoError.NetworkError -> "Error de conexión: ${error.message}"
            is NegocioListoError.BusinessRuleError -> "Error de negocio: ${error.message}"
            is NegocioListoError.UnknownError -> "Error desconocido: ${error.message}"
            else -> "Error inesperado: ${error.message}"
        }
    }
    
    /**
     * Procesa un error y devuelve un UserMessage
     */
    fun handleErrorAsUserMessage(error: Throwable): com.negociolisto.app.domain.model.UserMessage {
        return com.negociolisto.app.domain.model.UserMessage(
            message = handleError(error),
            type = com.negociolisto.app.domain.model.MessageType.ERROR
        )
    }
}

