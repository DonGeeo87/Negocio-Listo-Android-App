package com.negociolisto.app.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.service.DataExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 📊 VIEWMODEL DE EXPORTACIÓN DE DATOS
 * 
 * Maneja el estado y la lógica de la pantalla de exportación de datos.
 */
@HiltViewModel
class DataExportViewModel @Inject constructor(
    private val dataExportService: DataExportService
) : ViewModel() {
    
    // Estados de la UI
    private val _uiState = MutableStateFlow(DataExportUiState())
    val uiState = _uiState.asStateFlow()
    
    // Estados de progreso de exportación
    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress = _exportProgress.asStateFlow()
    
    private val _exportStatus = MutableStateFlow("")
    val exportStatus = _exportStatus.asStateFlow()
    
    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage = _exportMessage.asStateFlow()
    
    private val _exportUri = MutableStateFlow<Uri?>(null)
    val exportUri = _exportUri.asStateFlow()
    
    /**
     * 🚀 EXPORTAR DATOS
     * 
     * Inicia el proceso de exportación con la configuración especificada.
     */
    fun exportData(config: DataExportService.ExportConfig) {
        viewModelScope.launch {
            try {
                // Actualizar estado inicial
                _uiState.value = _uiState.value.copy(isExporting = true)
                _exportMessage.value = null
                _exportUri.value = null
                
                // Iniciar exportación y observar progreso
                dataExportService.exportData(config).collect { progress ->
                    _exportProgress.value = progress.progress
                    _exportStatus.value = progress.currentStep
                    
                    // Si hay error, mostrarlo
                    progress.error?.let { error ->
                        _exportMessage.value = "Error: $error"
                        _uiState.value = _uiState.value.copy(isExporting = false)
                        return@collect
                    }
                    
                    // Si está completo, obtener el URI del archivo
                    if (progress.isComplete) {
                        _exportMessage.value = "✅ Exportación completada exitosamente"
                        _uiState.value = _uiState.value.copy(isExporting = false)
                        
                        // Aquí se obtendría el URI del archivo exportado
                        // Por ahora simulamos que se completó
                        _exportUri.value = null // Se establecería con el URI real
                    }
                }
                
            } catch (e: Exception) {
                _exportMessage.value = "Error durante la exportación: ${e.message}"
                _uiState.value = _uiState.value.copy(isExporting = false)
            }
        }
    }
    
    /**
     * 🧹 LIMPIAR MENSAJE DE EXPORTACIÓN
     */
    fun clearExportMessage() {
        _exportMessage.value = null
        _exportUri.value = null
        _exportProgress.value = 0f
        _exportStatus.value = ""
    }
    
    /**
     * 📊 ESTADO DE LA UI DE EXPORTACIÓN
     */
    data class DataExportUiState(
        val isExporting: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
