package com.negociolisto.app.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🍞 VIEWMODEL PARA NOTIFICACIONES TOAST
 * 
 * Maneja el estado global de las notificaciones toast en la aplicación.
 * Permite mostrar toasts desde cualquier parte de la app.
 */
@Singleton
class ToastViewModel @Inject constructor() : ViewModel() {
    
    private val _toastMessage = MutableStateFlow<ToastData?>(null)
    val toastMessage: StateFlow<ToastData?> = _toastMessage.asStateFlow()
    
    /**
     * 📤 MOSTRAR TOAST DE ÉXITO
     */
    fun showSuccess(message: String, duration: Long = 3000L) {
        showToast(
            message = message,
            type = ToastType.SUCCESS,
            duration = duration
        )
    }
    
    /**
     * ❌ MOSTRAR TOAST DE ERROR
     */
    fun showError(message: String, duration: Long = 4000L) {
        showToast(
            message = message,
            type = ToastType.ERROR,
            duration = duration
        )
    }
    
    /**
     * ⚠️ MOSTRAR TOAST DE ADVERTENCIA
     */
    fun showWarning(message: String, duration: Long = 3500L) {
        showToast(
            message = message,
            type = ToastType.WARNING,
            duration = duration
        )
    }
    
    /**
     * ℹ️ MOSTRAR TOAST DE INFORMACIÓN
     */
    fun showInfo(message: String, duration: Long = 3000L) {
        showToast(
            message = message,
            type = ToastType.INFO,
            duration = duration
        )
    }
    
    /**
     * 🍞 MOSTRAR TOAST PERSONALIZADO
     */
    fun showToast(
        message: String,
        type: ToastType = ToastType.INFO,
        duration: Long = 3000L
    ) {
        viewModelScope.launch {
            _toastMessage.value = ToastData(
                message = message,
                type = type,
                duration = duration
            )
        }
    }
    
    /**
     * 🗑️ OCULTAR TOAST
     */
    fun hideToast() {
        viewModelScope.launch {
            _toastMessage.value = null
        }
    }
}

/**
 * 📊 DATOS DEL TOAST
 */
data class ToastData(
    val message: String,
    val type: ToastType,
    val duration: Long = 3000L
)

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Global State**: Estado global accesible desde cualquier pantalla
 * 2. **Singleton**: Una sola instancia para toda la app
 * 3. **Type Safety**: Tipos específicos para cada tipo de toast
 * 4. **Duration Control**: Control de duración por tipo de mensaje
 * 5. **Coroutines**: Manejo asíncrono del estado
 * 
 * ANALOGÍA:
 * 
 * ToastViewModel es como un "sistema de megafonía" en una tienda:
 * 
 * 1. **Centralizado**: Un solo lugar para enviar mensajes
 * 2. **Accesible**: Cualquier empleado puede usarlo
 * 3. **Tipado**: Diferentes tipos de anuncios (urgente, informativo, etc.)
 * 4. **Temporal**: Los mensajes se desvanecen automáticamente
 * 5. **No intrusivo**: No interrumpe las actividades normales
 * 
 * CASOS DE USO REALES:
 * - ✅ Desde repositorios: "Datos sincronizados"
 * - ❌ Desde ViewModels: "Error al guardar"
 * - ⚠️ Desde servicios: "Conexión perdida"
 * - ℹ️ Desde UI: "Cambios guardados"
 * 
 * VENTAJAS:
 * ✅ Centralizado y consistente
 * ✅ Fácil de usar desde cualquier lugar
 * ✅ Tipado y seguro
 * ✅ Configurable por tipo
 * ✅ No bloquea la UI
 * ✅ Manejo automático del estado
 */
