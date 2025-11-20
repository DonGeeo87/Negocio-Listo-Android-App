package com.negociolisto.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.remote.googledrive.GoogleDriveAuthService
import com.negociolisto.app.data.remote.googledrive.ImageBackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 📸 VIEWMODEL PARA RESPALDO DE IMÁGENES
 * 
 * Maneja el estado y la lógica de negocio para el respaldo automático
 * de imágenes de productos en Google Drive.
 */
@HiltViewModel
class ImageBackupViewModel @Inject constructor(
    private val googleDriveAuthService: GoogleDriveAuthService,
    private val imageBackupService: ImageBackupService
) : ViewModel() {
    
    private val _state = MutableStateFlow(ImageBackupState())
    val state: StateFlow<ImageBackupState> = _state.asStateFlow()
    
    init {
        // Observar estado de conexión con Google Drive
        viewModelScope.launch {
            googleDriveAuthService.isConnected.collect { isConnected ->
                _state.value = _state.value.copy(
                    isConnected = isConnected,
                    error = null
                )
            }
        }
        
        // Observar cuenta actual
        viewModelScope.launch {
            googleDriveAuthService.currentAccount.collect { account ->
                _state.value = _state.value.copy(
                    currentAccount = account,
                    userEmail = account?.email
                )
            }
        }
    }
    
    /**
     * 🔗 CONECTAR CON GOOGLE DRIVE
     * 
     * Inicia el proceso de autenticación con Google Drive.
     */
    fun connectGoogleDrive() {
        _state.value = _state.value.copy(
            isConnecting = true,
            error = null
        )
    }
    
    /**
     * ✅ MANEJAR RESULTADO DE SIGN-IN
     * 
     * Procesa el resultado de la autenticación con Google.
     */
    fun handleSignInResult(result: android.content.Intent) {
        googleDriveAuthService.handleSignInResult(result)
        _state.value = _state.value.copy(isConnecting = false)
    }
    
    /**
     * 🚪 DESCONECTAR DE GOOGLE DRIVE
     * 
     * Cierra la sesión y limpia el estado de autenticación.
     */
    fun disconnectGoogleDrive() {
        googleDriveAuthService.signOut()
        _state.value = _state.value.copy(
            isConnecting = false,
            error = null
        )
    }
    
    /**
     * 📤 SUBIR IMAGEN DE PRODUCTO
     * 
     * Sube una imagen de producto a Google Drive y retorna la URL pública.
     * 
     * @param imageUri URI de la imagen local
     * @param productId ID único del producto
     * @param productName Nombre del producto
     * @return Result con la URL pública de la imagen o error
     */
    suspend fun uploadProductImage(
        imageUri: android.net.Uri,
        productId: String,
        productName: String
    ): Result<String> {
        return try {
            _state.value = _state.value.copy(isUploading = true, error = null)
            
            val result = imageBackupService.uploadProductImage(
                imageUri = imageUri,
                productId = productId,
                productName = productName
            )
            
            _state.value = _state.value.copy(isUploading = false)
            result
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isUploading = false,
                error = "Error al subir imagen: ${e.message}"
            )
            Result.failure(e)
        }
    }
    
    /**
     * 🗑️ ELIMINAR IMAGEN DE PRODUCTO
     * 
     * Elimina una imagen de producto de Google Drive.
     * 
     * @param imageUrl URL pública de la imagen en Google Drive
     * @return Result indicando éxito o fallo
     */
    suspend fun deleteProductImage(imageUrl: String): Result<Unit> {
        return try {
            _state.value = _state.value.copy(isDeleting = true, error = null)
            
            val result = imageBackupService.deleteProductImage(imageUrl)
            
            _state.value = _state.value.copy(isDeleting = false)
            result
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isDeleting = false,
                error = "Error al eliminar imagen: ${e.message}"
            )
            Result.failure(e)
        }
    }
    
    /**
     * 📊 VERIFICAR ESPACIO DISPONIBLE
     * 
     * Verifica el espacio disponible en Google Drive del usuario.
     * 
     * @return Result con el espacio disponible en bytes
     */
    suspend fun checkAvailableSpace(): Result<Long> {
        return try {
            val result = imageBackupService.checkAvailableSpace()
            result
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "Error al verificar espacio: ${e.message}"
            )
            Result.failure(e)
        }
    }
    
    /**
     * 🔄 VERIFICAR ESTADO DE CONEXIÓN
     * 
     * Verifica si el usuario sigue autenticado y actualiza el estado.
     */
    fun checkConnectionStatus() {
        googleDriveAuthService.checkConnectionStatus()
    }
    
    /**
     * 🧹 LIMPIAR ERROR
     * 
     * Limpia cualquier mensaje de error del estado.
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    /**
     * 📋 OBTENER INTENT DE SIGN-IN
     * 
     * Retorna el Intent necesario para iniciar la autenticación.
     * 
     * @return Intent para Google Sign-In o null si hay error
     */
    fun getSignInIntent(): android.content.Intent? {
        return googleDriveAuthService.getSignInIntent()
    }
}

/**
 * 📊 ESTADO DEL RESPALDO DE IMÁGENES
 * 
 * Contiene toda la información del estado actual del respaldo de imágenes.
 */
data class ImageBackupState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isUploading: Boolean = false,
    val isDeleting: Boolean = false,
    val currentAccount: com.google.android.gms.auth.api.signin.GoogleSignInAccount? = null,
    val userEmail: String? = null,
    val error: String? = null,
    val lastUploadTime: Long? = null,
    val totalImagesBackedUp: Int = 0
)











