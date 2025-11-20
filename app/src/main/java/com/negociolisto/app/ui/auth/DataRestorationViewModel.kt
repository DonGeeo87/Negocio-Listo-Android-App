package com.negociolisto.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.remote.firebase.FirebaseBackupRepository
import com.negociolisto.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🔄 VIEWMODEL PARA RESTAURACIÓN DE DATOS
 * 
 * Maneja la restauración automática de datos desde Firebase
 * después de que el usuario inicia sesión.
 */
@HiltViewModel
class DataRestorationViewModel @Inject constructor(
    private val firebaseBackupRepository: FirebaseBackupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    data class RestorationState(
        val isRestoring: Boolean = false,
        val progress: Float = 0f,
        val statusMessage: String = "Preparando restauración...",
        val isComplete: Boolean = false,
        val hasError: Boolean = false,
        val errorMessage: String = ""
    )
    
    private val _restorationState = MutableStateFlow(RestorationState())
    val restorationState: StateFlow<RestorationState> = _restorationState.asStateFlow()
    
    /**
     * 🚀 INICIAR RESTAURACIÓN AUTOMÁTICA
     * 
     * Restaura todos los datos del usuario desde Firebase
     */
    fun startRestoration() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser.first()
            if (currentUser == null) {
                _restorationState.value = RestorationState(
                    hasError = true,
                    errorMessage = "No hay usuario autenticado",
                    isComplete = true
                )
                return@launch
            }
            
            _restorationState.value = RestorationState(
                isRestoring = true,
                progress = 0f,
                statusMessage = "Verificando datos en la nube...",
                isComplete = false,
                hasError = false
            )
            
            try {
                // Verificar si hay datos en Firebase para restaurar
                println("🔄 DataRestorationViewModel: Verificando datos en Firebase para usuario ${currentUser.id}")
                val backupStatusResult = firebaseBackupRepository.getBackupStatus(currentUser.id)
                
                if (backupStatusResult.isSuccess) {
                    val backupStatus = backupStatusResult.getOrNull()
                    println("🔄 DataRestorationViewModel: Estado de backup: $backupStatus")
                    
                    // Verificar tanto el status como si hay datos reales
                    val status = backupStatus?.get("status") as? String
                    val hasData = backupStatus?.get("hasData") as? Boolean ?: false
                    // Hay backup si el status no es "no_backup" O si hay datos reales
                    val hasBackup = (status != "no_backup" && status != null) || hasData
                    
                    println("🔄 DataRestorationViewModel: status=$status, hasData=$hasData, hasBackup=$hasBackup")
                    
                    if (!hasBackup) {
                        // No hay datos para restaurar, completar inmediatamente
                        println("ℹ️ DataRestorationViewModel: No hay datos para restaurar")
                        _restorationState.value = RestorationState(
                            isRestoring = false,
                            progress = 1f,
                            statusMessage = "No hay datos para restaurar",
                            isComplete = true,
                            hasError = false
                        )
                        return@launch
                    }
                    
                    // Restaurar datos con progreso
                    println("🔄 DataRestorationViewModel: Iniciando restauración de datos...")
                    _restorationState.value = _restorationState.value.copy(
                        statusMessage = "Restaurando datos desde Firebase..."
                    )
                    
                    val restoreResult = firebaseBackupRepository.restoreFromBackup(
                        userId = currentUser.id,
                        onProgress = { progress, progressStatus ->
                            println("📥 DataRestorationViewModel: Progreso $progress% - $progressStatus")
                            _restorationState.value = _restorationState.value.copy(
                                progress = progress / 100f,
                                statusMessage = progressStatus
                            )
                        }
                    )
                    
                    println("🔄 DataRestorationViewModel: Resultado de restauración: ${restoreResult.isSuccess}")
                    
                    if (restoreResult.isSuccess) {
                        _restorationState.value = RestorationState(
                            isRestoring = false,
                            progress = 1f,
                            statusMessage = "✅ Datos restaurados exitosamente",
                            isComplete = true,
                            hasError = false
                        )
                    } else {
                        val error = restoreResult.exceptionOrNull()?.message ?: "Error desconocido"
                        _restorationState.value = RestorationState(
                            isRestoring = false,
                            progress = _restorationState.value.progress,
                            statusMessage = "⚠️ Restauración completada con advertencias",
                            isComplete = true,
                            hasError = true,
                            errorMessage = error
                        )
                    }
                } else {
                    // Error al verificar estado del backup
                    val error = backupStatusResult.exceptionOrNull()?.message ?: "Error al verificar datos"
                    _restorationState.value = RestorationState(
                        isRestoring = false,
                        progress = 0f,
                        statusMessage = "No se pudieron verificar los datos",
                        isComplete = true,
                        hasError = true,
                        errorMessage = error
                    )
                }
            } catch (e: Exception) {
                _restorationState.value = RestorationState(
                    isRestoring = false,
                    progress = _restorationState.value.progress,
                    statusMessage = "Error durante la restauración",
                    isComplete = true,
                    hasError = true,
                    errorMessage = e.message ?: "Error desconocido"
                )
            }
        }
    }
}

