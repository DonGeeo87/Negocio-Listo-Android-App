package com.negociolisto.app.data.sync

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔄 COLA DE SINCRONIZACIÓN
 * 
 * Maneja una cola de operaciones pendientes de sincronización
 * cuando no hay conexión a internet o falla la sincronización.
 */
@Singleton
class SyncQueue @Inject constructor() {
    
    private val _pendingOperations = MutableStateFlow<List<SyncOperation>>(emptyList())
    val pendingOperations: StateFlow<List<SyncOperation>> = _pendingOperations.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Agregar operación a la cola
     */
    fun enqueue(operation: SyncOperation) {
        val currentList = _pendingOperations.value.toMutableList()
        currentList.add(operation)
        _pendingOperations.value = currentList
    }
    
    /**
     * Procesar todas las operaciones pendientes
     */
    suspend fun processQueue() {
        if (_isProcessing.value) return
        
        _isProcessing.value = true
        
        try {
            val operations = _pendingOperations.value.toList()
            for (operation in operations) {
                try {
                    operation.execute()
                    removeOperation(operation.id)
                } catch (e: Exception) {
                    // Si falla, mantener en la cola para reintentar después
                    println("❌ Error sincronizando operación ${operation.id}: ${e.message}")
                }
            }
        } finally {
            _isProcessing.value = false
        }
    }
    
    /**
     * Remover operación de la cola
     */
    private fun removeOperation(operationId: String) {
        val currentList = _pendingOperations.value.toMutableList()
        currentList.removeAll { it.id == operationId }
        _pendingOperations.value = currentList
    }
    
    /**
     * Limpiar toda la cola
     */
    fun clearQueue() {
        _pendingOperations.value = emptyList()
    }
    
    /**
     * Obtener número de operaciones pendientes
     */
    fun getPendingCount(): Int = _pendingOperations.value.size
}

/**
 * 🔄 OPERACIÓN DE SINCRONIZACIÓN
 * 
 * Representa una operación que necesita ser sincronizada
 */
data class SyncOperation(
    val id: String,
    val type: SyncOperationType,
    val data: Any,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val execute: suspend () -> Unit
)

/**
 * 📋 TIPOS DE OPERACIONES DE SINCRONIZACIÓN
 */
enum class SyncOperationType {
    CREATE_PRODUCT,
    UPDATE_PRODUCT,
    DELETE_PRODUCT,
    UPDATE_STOCK,
    CREATE_MOVEMENT
}
