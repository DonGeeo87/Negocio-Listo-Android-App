package com.negociolisto.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.repository.BackupRepository
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.data.hybrid.HybridInventoryRepository
import com.negociolisto.app.data.service.BackupService
import com.negociolisto.app.data.service.DataSyncManager
import com.negociolisto.app.data.local.database.NegocioListoDatabase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🗃️ VIEWMODEL PARA BACKUP Y RESTAURACIÓN
 * 
 * Maneja el estado y la lógica de negocio para backup y restauración
 * de datos desde Firebase.
 */
@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val hybridInventoryRepository: HybridInventoryRepository,
    private val imageService: com.negociolisto.app.data.service.ImageService,
    private val backupService: BackupService,
    private val dataSyncManager: DataSyncManager,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state.asStateFlow()
    
    private val _backupStatus = MutableStateFlow("")
    val backupStatus: StateFlow<String> = _backupStatus.asStateFlow()
    
    private val _backupProgress = MutableStateFlow(0f)
    val backupProgress: StateFlow<Float> = _backupProgress.asStateFlow()

    init {
        loadBackupInfo()
    }

    /**
     * 📊 CARGAR INFORMACIÓN DEL BACKUP
     */
    fun loadBackupInfo() {
        viewModelScope.launch {
            try {
                val backupInfo = backupRepository.getLastBackupInfo()
                _state.value = _state.value.copy(
                    isBackupActive = backupInfo.isActive,
                    isAutomatic = backupInfo.isAutomatic,
                    lastBackupDate = backupInfo.lastBackupDate,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al cargar información del backup: ${e.message}"
                )
            }
        }
    }

    /**
     * 🚨 RESTAURACIÓN URGENTE DE PRODUCTOS DESDE FIREBASE
     * 
     * Restaura productos directamente desde Firebase usando HybridInventoryRepository
     * para casos donde BackupRepository no está completamente implementado.
     */
    fun restoreProductsUrgent() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRestoring = true,
                restoreProgress = 0f,
                error = null
            )
            
            try {
                _backupStatus.value = "🔄 Restaurando productos desde Firebase..."
                _state.value = _state.value.copy(restoreProgress = 0.1f)
                
                // Restaurar productos desde Firebase
                val success = hybridInventoryRepository.restoreFromCloud()
                
                if (success) {
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        restoreProgress = 1f,
                        error = null
                    )
                    _backupStatus.value = "✅ ¡Productos restaurados exitosamente!"
                    
                    // Actualizar información del backup
                    delay(2000)
                    _backupStatus.value = ""
                    loadBackupInfo()
                } else {
                    throw Exception("Error al restaurar productos desde Firebase")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRestoring = false,
                    error = "Error al restaurar productos: ${e.message}"
                )
                _backupStatus.value = "❌ Error: ${e.message}"
                delay(3000)
                _backupStatus.value = ""
            }
        }
    }

    /**
     * 📥 RESTAURAR DATOS DESDE FIREBASE
     */
    fun restoreFromFirebase() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRestoring = true,
                restoreProgress = 0f,
                error = null
            )

            try {
                _backupStatus.value = "🔄 Sincronizando datos desde la nube..."
                val result = dataSyncManager.syncUserData { progress, status ->
                    _backupProgress.value = progress / 100f
                    _backupStatus.value = status
                    _state.value = _state.value.copy(restoreProgress = progress / 100f)
                }

                if (result.isSuccess) {
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        restoreProgress = 1f,
                        error = null
                    )
                    _backupStatus.value = "✅ Datos restaurados correctamente"
                } else {
                    throw result.exceptionOrNull() ?: Exception("Error desconocido al restaurar")
                }

                // Actualizar información del backup
                loadBackupInfo()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRestoring = false,
                    error = "Error al restaurar datos: ${e.message}"
                )
                _backupStatus.value = "❌ Error al restaurar datos"
            }
        }
    }

    /**
     * 📤 HACER BACKUP MANUAL
     * 
     * Ejecuta el backup real usando BackupService que llama a FirebaseBackupRepository
     */
    fun performManualBackup() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isBackingUp = true,
                error = null
            )
            
            // Limpiar estado anterior
            _backupStatus.value = ""
            _backupProgress.value = 0f

            try {
                // ✅ EJECUTAR BACKUP REAL
                val result = backupService.performFullBackup { progress, status ->
                    // Actualizar progreso y estado en tiempo real
                    _backupProgress.value = progress / 100f
                    _backupStatus.value = status
                }
                
                if (result.isSuccess) {
                    // Actualizar timestamp del último backup
                    val currentTime = System.currentTimeMillis()
                    _state.value = _state.value.copy(
                        isBackingUp = false,
                        lastBackupDate = currentTime
                    )
                    
                    // Mensaje de éxito
                    _backupStatus.value = "🎉 ¡Datos respaldados exitosamente!"
                    _backupProgress.value = 1.0f
                    
                    // Actualizar información del backup
                    loadBackupInfo()
                    
                    delay(2000)
                    
                    // Limpiar estado
                    _backupStatus.value = ""
                    _backupProgress.value = 0f
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                    _backupStatus.value = "❌ Error durante el backup"
                    _state.value = _state.value.copy(
                        isBackingUp = false,
                        error = "Error al hacer backup: $errorMessage"
                    )
                    delay(3000)
                    _backupStatus.value = ""
                    _backupProgress.value = 0f
                }
                
            } catch (e: Exception) {
                _backupStatus.value = "❌ Error durante el backup"
                _state.value = _state.value.copy(
                    isBackingUp = false,
                    error = "Error al hacer backup: ${e.message}"
                )
                delay(3000)
                _backupStatus.value = ""
                _backupProgress.value = 0f
            }
        }
    }


    /**
     * 🗑️ LIMPIAR DATOS LOCALES
     */
    private suspend fun clearLocalData() {
        backupRepository.clearLocalData()
    }

    /**
     * 💾 INSERTAR DATOS RESTAURADOS
     */
    private suspend fun insertRestoredData(data: FirebaseBackupData) {
        backupRepository.insertRestoredData(data)
    }

    /**
     * 🔄 REFRESCAR INFORMACIÓN
     */
    fun refreshBackupInfo() {
        loadBackupInfo()
    }

    /**
     * 🔎 VERIFICAR BACKUP Y RESTORE (no destructivo)
     *
     * 1) Ejecuta backup manual
     * 2) Verifica datos locales (ya sincronizados con Firebase)
     * 3) Reporta estado en backupStatus/State
     */
    fun verifyBackupAndRestore() {
        viewModelScope.launch {
            try {
                _backupStatus.value = "🧪 Iniciando verificación de backup..."
                _backupProgress.value = 0.1f
                
                // Paso 1: Backup manual (sincronizado con estado visual)
                performManualBackup()

                _backupStatus.value = "🔍 Verificando datos locales..."
                _backupProgress.value = 0.6f
                
                // Paso 2: Verificar datos locales (ya sincronizados con Firebase después del backup)
                val products = hybridInventoryRepository.getAllProducts()
                    .catch { emit(emptyList()) }
                    .first()
                
                val productCount = products.size
                val productsWithImages = products.count { 
                    !it.photoUrl.isNullOrBlank() && !imageService.isLocalUrl(it.photoUrl)
                }
                
                if (productCount == 0) {
                    _backupStatus.value = "ℹ️ No hay productos para verificar"
                    _backupProgress.value = 1.0f
                    delay(1500)
                    _backupStatus.value = ""
                    _backupProgress.value = 0f
                    return@launch
                }

                val statusMsg = if (productsWithImages > 0) {
                    "✅ Verificación exitosa: $productCount productos respaldados ($productsWithImages con imágenes en Storage)"
                } else {
                    "⚠️ $productCount productos respaldados, pero las imágenes aún son locales"
                }
                
                _backupStatus.value = statusMsg
                _backupProgress.value = 1.0f
                delay(2000)
                _backupStatus.value = ""
                _backupProgress.value = 0f
                loadBackupInfo()
            } catch (e: Exception) {
                _backupStatus.value = "❌ Verificación fallida: ${e.message}"
                _state.value = _state.value.copy(error = e.message)
                delay(2000)
                _backupStatus.value = ""
                _backupProgress.value = 0f
            }
        }
    }

    /**
     * 🧪 Verificar backup/restore de imágenes (productos)
     * - Recorre productos en la nube/local y valida que sus URLs de imagen existan en Storage
     */
    fun verifyImageBackups() {
        viewModelScope.launch {
            try {
                _backupStatus.value = "🧪 Verificando imágenes de productos..."
                _backupProgress.value = 0.1f

                val products = try {
                    hybridInventoryRepository.getAllProducts()
                        .catch {
                            // Si hay error en el Flow, retornar lista vacía
                            emit(emptyList())
                        }
                        .first()
                } catch (e: Exception) {
                    emptyList()
                }
                
                if (products.isEmpty()) {
                    _backupStatus.value = "ℹ️ No hay productos para verificar"
                    delay(1200)
                    _backupStatus.value = ""
                    _backupProgress.value = 0f
                    return@launch
                }

                var checked = 0
                var ok = 0
                var localImages = 0
                for (p in products) {
                    try {
                        val urls = listOfNotNull(p.photoUrl, p.thumbnailUrl).filter { !it.isNullOrBlank() }
                        var productOk = true
                        var hasLocal = false
                        for (url in urls) {
                            if (imageService.isLocalUrl(url)) {
                                hasLocal = true
                                localImages++
                                productOk = false // Las imágenes locales no están en Storage
                            } else {
                                val exists = imageService.imageExistsAtUrl(url)
                                if (!exists) productOk = false
                            }
                        }
                        checked++
                        if (productOk && !hasLocal) ok++
                        _backupProgress.value = 0.1f + (0.8f * checked / products.size)
                    } catch (e: Exception) {
                        // Continuar con el siguiente producto si hay error
                        checked++
                    }
                }

                val statusMsg = if (localImages > 0) {
                    "⚠️ $ok/${products.size} productos con imágenes en Storage. $localImages imágenes locales (no subidas)"
                } else {
                    "✅ Imágenes OK: $ok/${products.size} productos"
                }
                _backupStatus.value = statusMsg
                _backupProgress.value = 1.0f
                delay(1500)
                _backupStatus.value = ""
                _backupProgress.value = 0f
            } catch (e: Exception) {
                _backupStatus.value = "❌ Verificación de imágenes fallida: ${e.message}"
                delay(1500)
                _backupStatus.value = ""
                _backupProgress.value = 0f
            }
        }
    }

    /**
     * 🧹 LIMPIAR ERROR
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    /**
     * 🚨 LIMPIAR DATOS MEZCLADOS Y RESTAURAR SOLO DEL USUARIO ACTUAL
     * 
     * Esta función corrige el problema de datos mezclados de diferentes usuarios.
     * Limpia todos los clientes locales y restaura solo los del usuario autenticado.
     */
    fun cleanMixedDataAndRestore() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRestoring = true,
                restoreProgress = 0f,
                error = null
            )
            
            try {
                // 1. Obtener userId del usuario actual
                _backupStatus.value = "🔍 Verificando usuario autenticado..."
                _state.value = _state.value.copy(restoreProgress = 0.1f)
                
                val currentUser = authRepository.currentUser.first()
                val userId = currentUser?.id ?: FirebaseAuth.getInstance().currentUser?.uid
                
                if (userId == null) {
                    throw Exception("No hay usuario autenticado. Por favor inicia sesión.")
                }
                
                // 2. Limpiar todos los clientes locales (datos mezclados)
                _backupStatus.value = "🧹 Limpiando datos mezclados..."
                _state.value = _state.value.copy(restoreProgress = 0.2f)
                
                val database = NegocioListoDatabase.getDatabase(context)
                database.customerDao().clearAllCustomers()
                
                // 3. Restaurar solo los clientes del usuario actual desde Firebase
                _backupStatus.value = "📥 Restaurando datos del usuario actual..."
                _state.value = _state.value.copy(restoreProgress = 0.3f)
                
                val restoreResult = dataSyncManager.syncUserData { progress, status ->
                    _backupProgress.value = progress / 100f
                    _backupStatus.value = status
                    _state.value = _state.value.copy(restoreProgress = progress / 100f)
                }
                
                if (restoreResult.isSuccess) {
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        restoreProgress = 1f,
                        error = null
                    )
                    _backupStatus.value = "✅ Datos limpiados y restaurados correctamente"
                    _backupProgress.value = 1f
                    
                    // Actualizar información del backup
                    delay(2000)
                    loadBackupInfo()
                    _backupStatus.value = ""
                    _backupProgress.value = 0f
                } else {
                    throw restoreResult.exceptionOrNull() ?: Exception("Error desconocido al restaurar")
                }
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRestoring = false,
                    error = "Error al limpiar y restaurar datos: ${e.message}"
                )
                _backupStatus.value = "❌ Error: ${e.message}"
                delay(3000)
                _backupStatus.value = ""
                _backupProgress.value = 0f
            }
        }
    }
    
}

/**
 * 📊 ESTADO DEL BACKUP
 */
data class BackupState(
    val isBackupActive: Boolean = true,
    val isAutomatic: Boolean = true,
    val lastBackupDate: Long? = null,
    val isRestoring: Boolean = false,
    val isBackingUp: Boolean = false,
    val restoreProgress: Float = 0f,
    val error: String? = null
)

/**
 * 🔥 DATOS DE BACKUP DE FIREBASE
 */
data class FirebaseBackupData(
    val products: List<Any> = emptyList(),
    val sales: List<Any> = emptyList(),
    val customers: List<Any> = emptyList(),
    val expenses: List<Any> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 📋 INFORMACIÓN DEL BACKUP
 */
data class BackupInfo(
    val isActive: Boolean = true,
    val isAutomatic: Boolean = true,
    val lastBackupDate: Long? = null,
    val totalItems: Int = 0
)
