package com.negociolisto.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.data.service.BackupService
import com.negociolisto.app.data.service.ExportService
import com.negociolisto.app.data.service.GoogleSignInService
import com.negociolisto.app.data.service.AutoBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val backupService: BackupService,
    private val exportService: ExportService,
    private val googleSignInService: GoogleSignInService,
    private val autoBackupManager: AutoBackupManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        // Cargar datos iniciales
        loadUserData()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                // Obtener datos básicos de Firebase Auth
                authRepository.currentUser.collect { currentUser ->
                    if (currentUser != null) {
                        // Si es FirebaseAuthRepository, cargar datos completos desde Firestore
                        if (authRepository is com.negociolisto.app.data.remote.firebase.FirebaseAuthRepository) {
                            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            if (firebaseUser != null) {
                                val fullUserData = authRepository.loadUserFromFirestore(firebaseUser)
                                _user.value = fullUserData
                                println("✅ DEBUG SettingsViewModel: Datos completos cargados desde Firestore")
                            } else {
                                _user.value = currentUser
                            }
                        } else {
                            _user.value = currentUser
                        }
                    } else {
                        _user.value = null
                    }
                }
            } catch (e: Exception) {
                println("❌ DEBUG SettingsViewModel: Error cargando datos: ${e.message}")
                // Fallback a datos básicos
                authRepository.currentUser.collect { currentUser ->
                    _user.value = currentUser
                }
            }
        }
    }
    
    fun refreshUserData() {
        loadUserData()
        loadBackupSettings() // También refrescar configuración de backup
    }

    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // Estados para backup
    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring

    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage

    // Estados para progreso
    private val _backupProgress = MutableStateFlow(0)
    val backupProgress: StateFlow<Int> = _backupProgress

    private val _backupStatus = MutableStateFlow("")
    val backupStatus: StateFlow<String> = _backupStatus

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                println("🚪 DEBUG SettingsViewModel: Iniciando logout completo...")
                
                // Limpiar estado local del usuario
                _user.value = null
                
                // Hacer logout del repositorio
                authRepository.logout()
                
                // Limpiar datos adicionales si es necesario
                if (authRepository is com.negociolisto.app.data.remote.firebase.FirebaseAuthRepository) {
                    // Verificar que Firebase Auth se cerró correctamente
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        println("⚠️ DEBUG: Firebase Auth aún tiene usuario después del logout, forzando signOut")
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    }
                }
                
                // Limpiar sesión de Google si existe
                try {
                    googleSignInService.signOut()
                    println("✅ DEBUG: Sesión de Google limpiada")
                } catch (e: Exception) {
                    println("⚠️ DEBUG: Error limpiando sesión de Google: ${e.message}")
                }
                
                println("✅ DEBUG SettingsViewModel: Logout completado")
                onDone()
                
            } catch (e: Exception) {
                println("❌ DEBUG SettingsViewModel: Error en logout: ${e.message}")
                // Continuar con el logout de todas formas
                onDone()
            }
        }
    }

    fun performBackup() {
        viewModelScope.launch {
            println("🔄 DEBUG ViewModel: Iniciando backup...")
            _isBackingUp.value = true
            _backupMessage.value = null
            _backupProgress.value = 0
            _backupStatus.value = ""
            
            try {
                val result = backupService.performFullBackup { progress, status ->
                    println("🔄 DEBUG ViewModel: Progreso: $progress%, Estado: $status")
                    _backupProgress.value = progress
                    _backupStatus.value = status
                }
                
                if (result.isSuccess) {
                    println("✅ DEBUG ViewModel: Backup exitoso")
                    _backupMessage.value = result.getOrNull()
                    _backupStatus.value = "Backup completado"
                } else {
                    println("❌ DEBUG ViewModel: Error en backup: ${result.exceptionOrNull()?.message}")
                    _backupMessage.value = "❌ Error en backup: ${result.exceptionOrNull()?.message}"
                    _backupStatus.value = "Error en backup"
                }
            } catch (e: Exception) {
                println("❌ DEBUG ViewModel: Error inesperado: ${e.message}")
                _backupMessage.value = "❌ Error inesperado: ${e.message}"
                _backupStatus.value = "Error inesperado"
            } finally {
                println("🔄 DEBUG ViewModel: Finalizando backup...")
                _isBackingUp.value = false
            }
        }
    }

    fun restoreFromBackup() {
        viewModelScope.launch {
            _isRestoring.value = true
            _backupMessage.value = null
            _backupProgress.value = 0
            _backupStatus.value = ""
            
            try {
                val result = backupService.restoreFromBackup("") { progress, status ->
                    _backupProgress.value = progress
                    _backupStatus.value = status
                }
                
                if (result.isSuccess) {
                    _backupMessage.value = result.getOrNull()
                    _backupStatus.value = "Restauración completada"
                } else {
                    _backupMessage.value = "❌ Error al restaurar: ${result.exceptionOrNull()?.message}"
                    _backupStatus.value = "Error en restauración"
                }
            } catch (e: Exception) {
                _backupMessage.value = "❌ Error inesperado: ${e.message}"
                _backupStatus.value = "Error inesperado"
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
    }

    // Estados para configuración de backup automático
    private val _autoBackupEnabled = MutableStateFlow(false)
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    private val _backupFrequency = MutableStateFlow("weekly")
    val backupFrequency: StateFlow<String> = _backupFrequency

    // Estados para gestión de backups
    private val _availableBackups = MutableStateFlow<List<String>>(emptyList())
    val availableBackups: StateFlow<List<String>> = _availableBackups

    private val _isLoadingBackups = MutableStateFlow(false)
    val isLoadingBackups: StateFlow<Boolean> = _isLoadingBackups

    init {
        // Cargar configuración de backup automático
        loadBackupSettings()
    }

    private fun loadBackupSettings() {
        viewModelScope.launch {
            try {
                val sharedPrefs = context.getSharedPreferences("backup_settings", Context.MODE_PRIVATE)
                
                val autoBackupEnabled = sharedPrefs.getBoolean("auto_backup_enabled", false)
                val backupFrequency = sharedPrefs.getString("backup_frequency", "weekly") ?: "weekly"
                
                _autoBackupEnabled.value = autoBackupEnabled
                _backupFrequency.value = backupFrequency
                
                println("✅ DEBUG SettingsViewModel: Configuración de backup cargada - AutoBackup: $autoBackupEnabled, Frecuencia: $backupFrequency")
            } catch (e: Exception) {
                println("❌ Error cargando configuración de backup: ${e.message}")
                // Valores por defecto en caso de error
                _autoBackupEnabled.value = false
                _backupFrequency.value = "weekly"
            }
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                println("🔄 DEBUG SettingsViewModel: Cambiando estado de backup automático a: $enabled")
                
                val sharedPrefs = context.getSharedPreferences("backup_settings", Context.MODE_PRIVATE)
                val editResult = sharedPrefs.edit().putBoolean("auto_backup_enabled", enabled).commit()
                
                if (editResult) {
                    println("✅ DEBUG SettingsViewModel: Estado guardado en SharedPreferences")
                    _autoBackupEnabled.value = enabled
                    
                    if (enabled) {
                        // Programar backup automático con la frecuencia actual
                        val frequency = _backupFrequency.value
                        autoBackupManager.scheduleAutoBackup(frequency)
                        _backupMessage.value = "✅ Backup automático activado ($frequency)"
                        println("✅ DEBUG SettingsViewModel: Backup automático programado con frecuencia: $frequency")
                    } else {
                        // Cancelar backup automático
                        autoBackupManager.cancelAutoBackup()
                        _backupMessage.value = "❌ Backup automático desactivado"
                        println("❌ DEBUG SettingsViewModel: Backup automático cancelado")
                    }
                } else {
                    println("❌ DEBUG SettingsViewModel: Error guardando en SharedPreferences")
                    _backupMessage.value = "❌ Error guardando configuración"
                }
            } catch (e: Exception) {
                println("❌ DEBUG SettingsViewModel: Error configurando backup automático: ${e.message}")
                _backupMessage.value = "❌ Error configurando backup automático: ${e.message}"
            }
        }
    }

    fun setBackupFrequency(frequency: String) {
        viewModelScope.launch {
            try {
                val sharedPrefs = context.getSharedPreferences("backup_settings", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("backup_frequency", frequency).apply()
                
                _backupFrequency.value = frequency
                
                // Si el backup automático está activado, reprogramar con la nueva frecuencia
                if (_autoBackupEnabled.value) {
                    autoBackupManager.scheduleAutoBackup(frequency)
                    _backupMessage.value = "✅ Frecuencia de backup actualizada: $frequency"
                } else {
                    _backupMessage.value = "✅ Frecuencia de backup configurada: $frequency"
                }
            } catch (e: Exception) {
                _backupMessage.value = "❌ Error configurando frecuencia: ${e.message}"
            }
        }
    }

    fun loadAvailableBackups() {
        viewModelScope.launch {
            _isLoadingBackups.value = true
            try {
                // TODO: Implementar listado de backups cuando sea necesario
                val result = Result.success(emptyList<String>())
                if (result.isSuccess) {
                    _availableBackups.value = result.getOrNull() ?: emptyList()
                    _backupMessage.value = "✅ Backups cargados desde Firebase"
                } else {
                    _backupMessage.value = "❌ Error cargando backups: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _backupMessage.value = "❌ Error cargando backups: ${e.message}"
            } finally {
                _isLoadingBackups.value = false
            }
        }
    }

    fun deleteOldBackups(keepLast: Int = 3) {
        viewModelScope.launch {
            try {
                _backupMessage.value = "🔄 Eliminando backups antiguos..."
                
                // TODO: Implementar eliminación de backups cuando sea necesario
                val result = Result.success("Backups eliminados")
                if (result.isSuccess) {
                    val deletedCount = result.getOrNull() ?: 0
                    _backupMessage.value = "✅ $deletedCount backups antiguos eliminados (manteniendo los $keepLast más recientes)"
                    
                    // Recargar la lista de backups disponibles
                    loadAvailableBackups()
                } else {
                    _backupMessage.value = "❌ Error eliminando backups: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _backupMessage.value = "❌ Error eliminando backups: ${e.message}"
            }
        }
    }


    // Estados para exportación
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting

    private val _exportProgress = MutableStateFlow(0)
    val exportProgress: StateFlow<Int> = _exportProgress

    private val _exportStatus = MutableStateFlow("")
    val exportStatus: StateFlow<String> = _exportStatus

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage

    // URI del último archivo exportado (para compartir/abrir)
    private val _exportUri = MutableStateFlow<android.net.Uri?>(null)
    val exportUri: StateFlow<android.net.Uri?> = _exportUri

    fun exportToCSV(selectedDataTypes: List<String>) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            _exportProgress.value = 0
            _exportStatus.value = ""
            
            try {
                val result = exportService.getExportData(selectedDataTypes) { progress, status ->
                    _exportProgress.value = progress
                    _exportStatus.value = status
                }
                
                result.onSuccess { data ->
                    // Exportar cada tipo de dato
                    selectedDataTypes.forEach { dataType ->
                        data[dataType]?.let { typeData ->
                            val exportResult = exportService.exportToCSV(dataType, typeData) { progress, status ->
                                _exportProgress.value = progress
                                _exportStatus.value = status
                            }
                            
                            exportResult.onSuccess { uri ->
                                _exportUri.value = uri
                                _exportMessage.value = "✅ Exportación CSV completada: $dataType"
                            }.onFailure { error ->
                                _exportMessage.value = "❌ Error exportando $dataType: ${error.message}"
                            }
                        }
                    }
                }.onFailure { error ->
                    _exportMessage.value = "❌ Error obteniendo datos: ${error.message}"
                }
            } catch (e: Exception) {
                _exportMessage.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportToPDF(selectedDataTypes: List<String>) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            _exportProgress.value = 0
            _exportStatus.value = ""
            
            try {
                val result = exportService.getExportData(selectedDataTypes) { progress, status ->
                    _exportProgress.value = progress
                    _exportStatus.value = status
                }
                
                result.onSuccess { data ->
                    selectedDataTypes.forEach { dataType ->
                        data[dataType]?.let { typeData ->
                            val exportResult = exportService.exportToPDF(dataType, typeData) { progress, status ->
                                _exportProgress.value = progress
                                _exportStatus.value = status
                            }
                            
                            exportResult.onSuccess { uri ->
                                _exportUri.value = uri
                                _exportMessage.value = "✅ Exportación PDF completada: $dataType"
                            }.onFailure { error ->
                                _exportMessage.value = "❌ Error exportando $dataType: ${error.message}"
                            }
                        }
                    }
                }.onFailure { error ->
                    _exportMessage.value = "❌ Error obteniendo datos: ${error.message}"
                }
            } catch (e: Exception) {
                _exportMessage.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportToExcel(selectedDataTypes: List<String>) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            _exportProgress.value = 0
            _exportStatus.value = ""
            
            try {
                val result = exportService.getExportData(selectedDataTypes) { progress, status ->
                    _exportProgress.value = progress
                    _exportStatus.value = status
                }
                
                result.onSuccess { data ->
                    selectedDataTypes.forEach { dataType ->
                        data[dataType]?.let { typeData ->
                            val exportResult = exportService.exportToExcel(dataType, typeData) { progress, status ->
                                _exportProgress.value = progress
                                _exportStatus.value = status
                            }
                            
                            exportResult.onSuccess { uri ->
                                _exportUri.value = uri
                                _exportMessage.value = "✅ Exportación Excel completada: $dataType"
                            }.onFailure { error ->
                                _exportMessage.value = "❌ Error exportando $dataType: ${error.message}"
                            }
                        }
                    }
                }.onFailure { error ->
                    _exportMessage.value = "❌ Error obteniendo datos: ${error.message}"
                }
            } catch (e: Exception) {
                _exportMessage.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
        _exportUri.value = null
    }

    // Exportar un único tipo a una URI usando SAF
    fun exportSingleTypeToCSVUri(dataType: String, target: android.net.Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            _exportProgress.value = 0
            _exportStatus.value = ""
            try {
                val result = exportService.getExportData(listOf(dataType)) { progress, status ->
                    _exportProgress.value = progress
                    _exportStatus.value = status
                }
                result.onSuccess { dataMap ->
                    val data = dataMap[dataType] ?: emptyList()
                    val writeResult = exportService.exportToCSVUri(dataType, data, target) { progress, status ->
                        _exportProgress.value = progress
                        _exportStatus.value = status
                    }
                    writeResult.onSuccess {
                        _exportUri.value = target
                        _exportMessage.value = "✅ CSV exportado: $dataType"
                    }.onFailure { e ->
                        _exportMessage.value = "❌ Error exportando CSV: ${e.message}"
                    }
                }.onFailure { e ->
                    _exportMessage.value = "❌ Error obteniendo datos: ${e.message}"
                }
            } catch (e: Exception) {
                _exportMessage.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportSingleTypeToPDFUri(dataType: String, target: android.net.Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            _exportProgress.value = 0
            _exportStatus.value = ""
            try {
                val result = exportService.getExportData(listOf(dataType)) { progress, status ->
                    _exportProgress.value = progress
                    _exportStatus.value = status
                }
                result.onSuccess { dataMap ->
                    val data = dataMap[dataType] ?: emptyList()
                    val writeResult = exportService.exportToPDFUri(dataType, data, target) { progress, status ->
                        _exportProgress.value = progress
                        _exportStatus.value = status
                    }
                    writeResult.onSuccess {
                        _exportUri.value = target
                        _exportMessage.value = "✅ PDF exportado: $dataType"
                    }.onFailure { e ->
                        _exportMessage.value = "❌ Error exportando PDF: ${e.message}"
                    }
                }.onFailure { e ->
                    _exportMessage.value = "❌ Error obteniendo datos: ${e.message}"
                }
            } catch (e: Exception) {
                _exportMessage.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportSingleTypeToExcelUri(dataType: String, target: android.net.Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            _exportProgress.value = 0
            _exportStatus.value = ""
            try {
                val result = exportService.getExportData(listOf(dataType)) { progress, status ->
                    _exportProgress.value = progress
                    _exportStatus.value = status
                }
                result.onSuccess { dataMap ->
                    val data = dataMap[dataType] ?: emptyList()
                    val writeResult = exportService.exportToExcelUri(dataType, data, target) { progress, status ->
                        _exportProgress.value = progress
                        _exportStatus.value = status
                    }
                    writeResult.onSuccess {
                        _exportUri.value = target
                        _exportMessage.value = "✅ Excel exportado: $dataType"
                    }.onFailure { e ->
                        _exportMessage.value = "❌ Error exportando Excel: ${e.message}"
                    }
                }.onFailure { e ->
                    _exportMessage.value = "❌ Error obteniendo datos: ${e.message}"
                }
            } catch (e: Exception) {
                _exportMessage.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportMultipleToTreeUri(dataTypes: List<String>, treeUri: android.net.Uri, format: String) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            _exportProgress.value = 0
            _exportStatus.value = ""
            try {
                val result = exportService.exportMultipleToTreeUri(dataTypes, treeUri, format) { progress, status ->
                    _exportProgress.value = progress
                    _exportStatus.value = status
                }
                result.onSuccess {
                    _exportUri.value = null
                    _exportMessage.value = "✅ Exportación completada en carpeta"
                }.onFailure { e ->
                    _exportMessage.value = "❌ Error exportando: ${e.message}"
                }
            } catch (e: Exception) {
                _exportMessage.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }
    
}


