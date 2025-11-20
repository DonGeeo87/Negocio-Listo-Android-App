package com.negociolisto.app.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.service.ExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Date
import android.net.Uri
import com.negociolisto.app.utils.AppLogger

@HiltViewModel
class DataExportViewModel @Inject constructor(
    private val exportService: ExportService
) : ViewModel() {
    
    // Estados de exportación
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting
    
    private val _exportProgress = MutableStateFlow(0)
    val exportProgress: StateFlow<Int> = _exportProgress
    
    private val _exportStatus = MutableStateFlow("")
    val exportStatus: StateFlow<String> = _exportStatus
    
    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage
    
    private val _exportUri = MutableStateFlow<android.net.Uri?>(null)
    val exportUri: StateFlow<android.net.Uri?> = _exportUri
    
    private val _exportSuccess = MutableStateFlow(false)
    val exportSuccess: StateFlow<Boolean> = _exportSuccess
    
    /**
     * Exportar datos con filtros y opciones
     */
    fun exportData(
        dataTypes: List<String>,
        format: String,
        singlePdfMode: Boolean,
        dateRange: Pair<Date?, Date?>
    ) {
        viewModelScope.launch {
            AppLogger.d("DataExportViewModel", "Iniciando exportación: tipos=$dataTypes, formato=$format, singlePdf=$singlePdfMode")
            _isExporting.value = true
            _exportMessage.value = null
            _exportProgress.value = 0
            _exportStatus.value = "Iniciando exportación..."
            _exportSuccess.value = false
            
            // Delay inicial para mostrar el estado
            delay(500)
            
            try {
                when (format) {
                    "PDF" -> {
                        if (singlePdfMode && dataTypes.size > 1) {
                            // PDF único con múltiples páginas
                            exportSinglePdfWithMultiplePages(dataTypes, dateRange)
                        } else {
                            // PDFs separados
                            exportMultiplePdfs(dataTypes, dateRange)
                        }
                    }
                    "CSV" -> exportMultipleCsvs(dataTypes, dateRange)
                    "Excel" -> exportMultipleExcels(dataTypes, dateRange)
                }
            } catch (e: Exception) {
                AppLogger.e("DataExportViewModel", "Error en exportación", e)
                _exportMessage.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isExporting.value = false
                _exportProgress.value = 100
                _exportStatus.value = "Exportación completada"
            }
        }
    }
    
    /**
     * Exportar un solo PDF con múltiples páginas
     */
    private suspend fun exportSinglePdfWithMultiplePages(
        dataTypes: List<String>,
        dateRange: Pair<Date?, Date?>
    ) {
        _exportStatus.value = "Generando PDF único con múltiples páginas..."
        _exportProgress.value = 10
        delay(800)
        
        // Obtener datos de todos los tipos
        val result = exportService.getExportData(dataTypes) { progress, status ->
            _exportProgress.value = 20 + (progress / 3)
            _exportStatus.value = status
        }
        
        result.onSuccess { dataMap ->
            // Aplicar filtros de fecha si están configurados
            val filteredDataMap = if (dateRange.first != null || dateRange.second != null) {
                dataMap.mapValues { (_, data) ->
                    exportService.filterDataByDateRange(data, dateRange)
                }
            } else {
                dataMap
            }
            
            // Exportar como PDF único usando el nuevo método
            _exportProgress.value = 60
            _exportStatus.value = "Generando archivo PDF..."
            delay(1000)
            
            val pdfResult = exportService.exportToSinglePDF(dataTypes, filteredDataMap) { progress, status ->
                _exportProgress.value = 60 + (progress / 2)
                _exportStatus.value = status
            }
            
            pdfResult.onSuccess { uri ->
                AppLogger.d("DataExportViewModel", "PDF único generado exitosamente: $uri")
                _exportProgress.value = 95
                _exportStatus.value = "Finalizando exportación..."
                delay(500)
                
                _exportUri.value = uri
                _exportMessage.value = "✅ PDF único generado con ${dataTypes.size} secciones"
                _exportStatus.value = "✅ Exportación exitosa"
                _exportProgress.value = 100
                _exportSuccess.value = true
                delay(300) // Delay final para mostrar el resultado
            }.onFailure { error ->
                AppLogger.e("DataExportViewModel", "Error generando PDF único", error)
                _exportMessage.value = "❌ Error generando PDF: ${error.message}"
                _exportStatus.value = "❌ Error en exportación"
            }
        }.onFailure { error ->
            _exportMessage.value = "❌ Error obteniendo datos: ${error.message}"
        }
    }
    
    /**
     * Exportar múltiples PDFs separados
     */
    private suspend fun exportMultiplePdfs(
        dataTypes: List<String>,
        dateRange: Pair<Date?, Date?>
    ) {
        _exportStatus.value = "Generando PDFs separados..."
        
        val result = exportService.getExportData(dataTypes) { progress, status ->
            _exportProgress.value = progress
            _exportStatus.value = status
        }
        
        result.onSuccess { dataMap ->
            var successCount = 0
            dataTypes.forEach { type ->
                dataMap[type]?.let { data ->
                    // Aplicar filtros de fecha
                    val filteredData = if (dateRange.first != null || dateRange.second != null) {
                        exportService.filterDataByDateRange(data, dateRange)
                    } else {
                        data
                    }
                    
                    val pdfResult = exportService.exportToPDF(type, filteredData) { _, _ -> }
                    if (pdfResult.isSuccess) successCount++
                }
            }
            
            AppLogger.d("DataExportViewModel", "Múltiples PDFs generados: $successCount de ${dataTypes.size}")
            _exportMessage.value = "✅ $successCount PDFs generados exitosamente"
            _exportStatus.value = "✅ Exportación exitosa"
            _exportProgress.value = 100
            _exportSuccess.value = successCount > 0
        }.onFailure { error ->
            AppLogger.e("DataExportViewModel", "Error obteniendo datos para múltiples PDFs", error)
            _exportMessage.value = "❌ Error obteniendo datos: ${error.message}"
            _exportStatus.value = "❌ Error en exportación"
        }
    }
    
    /**
     * Exportar múltiples CSVs
     */
    private suspend fun exportMultipleCsvs(
        dataTypes: List<String>,
        dateRange: Pair<Date?, Date?>
    ) {
        _exportStatus.value = "Generando archivo CSV único..."
        _exportProgress.value = 10
        delay(500)
        
        val result = exportService.getExportData(dataTypes) { progress, status ->
            _exportProgress.value = 10 + (progress / 3)
            _exportStatus.value = status
        }
        
        result.onSuccess { dataMap ->
            // Combinar todos los datos en una sola lista
            val combinedData = mutableListOf<Map<String, Any>>()
            
            dataTypes.forEach { type ->
                dataMap[type]?.let { data ->
                    val filteredData = if (dateRange.first != null || dateRange.second != null) {
                        exportService.filterDataByDateRange(data, dateRange)
                    } else {
                        data
                    }
                    
                    // Agregar columna Tipo_Datos para identificar el origen
                    val dataWithType = filteredData.map { row ->
                        val mutableRow = row.toMutableMap()
                        mutableRow["Tipo_Datos"] = type
                        mutableRow.toMap()
                    }
                    combinedData.addAll(dataWithType)
                }
            }
            
            delay(800)
            _exportProgress.value = 60
            _exportStatus.value = "Generando archivo CSV estructurado..."
            delay(1000)
            
            // Exportar usando la función existente
            val csvResult = exportService.exportToCSV("datos_combinados", combinedData) { _, _ -> }
            
            delay(500)
            _exportProgress.value = 95
            _exportStatus.value = "Finalizando exportación CSV..."
            delay(300)
            
            if (csvResult.isSuccess) {
                val totalRecords = combinedData.size
                AppLogger.d("DataExportViewModel", "Archivo CSV estructurado generado con $totalRecords registros")
                _exportMessage.value = "✅ Archivo CSV estructurado generado con $totalRecords registros"
                _exportStatus.value = "✅ Exportación CSV exitosa"
                _exportProgress.value = 100
                _exportSuccess.value = true
                
                // Guardar el URI del archivo único
                csvResult.getOrNull()?.let { uri: Uri ->
                    _exportUri.value = uri
                }
            } else {
                _exportMessage.value = "❌ Error generando archivo CSV: ${csvResult.exceptionOrNull()?.message}"
                _exportStatus.value = "❌ Error en exportación CSV"
            }
        }.onFailure { error ->
            AppLogger.e("DataExportViewModel", "Error obteniendo datos para CSV", error)
            _exportMessage.value = "❌ Error obteniendo datos: ${error.message}"
            _exportStatus.value = "❌ Error en exportación CSV"
        }
    }
    
    /**
     * Exportar múltiples archivos Excel
     */
    private suspend fun exportMultipleExcels(
        dataTypes: List<String>,
        dateRange: Pair<Date?, Date?>
    ) {
        _exportStatus.value = "Generando reporte amigable para el usuario..."
        _exportProgress.value = 5
        delay(500)
        
        // Usar la nueva función que crea datos amigables para el usuario
        val excelResult = exportService.exportUserFriendlyExcel(dataTypes) { progress, status ->
            _exportProgress.value = progress
            _exportStatus.value = status
        }
        
        delay(500)
        _exportProgress.value = 95
        _exportStatus.value = "Finalizando reporte amigable..."
        delay(300)
        
        if (excelResult.isSuccess) {
            AppLogger.d("DataExportViewModel", "Archivo Excel amigable generado para el usuario")
            _exportMessage.value = "✅ Reporte amigable generado con ${dataTypes.size} secciones organizadas"
            _exportStatus.value = "✅ Exportación Excel exitosa"
            _exportProgress.value = 100
            _exportSuccess.value = true
            
            // Guardar el URI del archivo único
            excelResult.getOrNull()?.let { uri: Uri ->
                _exportUri.value = uri
            }
        } else {
            _exportMessage.value = "❌ Error generando Excel profesional: ${excelResult.exceptionOrNull()?.message}"
            _exportStatus.value = "❌ Error en exportación Excel"
        }
    }
    
    fun clearExportMessage() {
        _exportMessage.value = null
        _exportUri.value = null
        _exportSuccess.value = false
    }
}
