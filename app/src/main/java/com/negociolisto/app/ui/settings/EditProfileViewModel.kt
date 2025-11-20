package com.negociolisto.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.service.ImageService
import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * 📝 VIEWMODEL PARA EDICIÓN DE PERFIL
 * 
 * Maneja el estado y la lógica para editar el perfil del usuario.
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val imageService: ImageService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                println("🔄 DEBUG EditProfileViewModel: Iniciando carga de usuario...")
                
                // Verificar Firebase Auth primero
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                println("🔍 DEBUG: Firebase Auth user: ${firebaseUser?.uid ?: "null"}")
                
                if (firebaseUser != null) {
                    // Si es FirebaseAuthRepository, cargar datos completos desde Firestore
                    if (authRepository is com.negociolisto.app.data.remote.firebase.FirebaseAuthRepository) {
                        try {
                            val fullUserData = authRepository.loadUserFromFirestore(firebaseUser)
                            val currentState = _uiState.value
                            _uiState.value = currentState.copy(
                                currentUser = fullUserData,
                                name = fullUserData.name,
                                email = fullUserData.email,
                                phone = fullUserData.phone ?: currentState.phone,
                                isLoading = false
                            )
                            println("✅ DEBUG EditProfileViewModel: Datos completos cargados desde Firestore")
                        } catch (e: Exception) {
                            println("⚠️ DEBUG: Error cargando desde Firestore, usando datos básicos: ${e.message}")
                            // Fallback a usuario básico de Firebase Auth
                            val basicUser = com.negociolisto.app.domain.model.User(
                                id = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                name = firebaseUser.displayName ?: "",
                                phone = null,
                                businessName = null,
                                businessType = null,
                                businessRut = null,
                                businessAddress = null,
                                businessPhone = null,
                                businessEmail = null,
                                businessSocialMedia = null,
                                businessLogoUrl = null,
                                profilePhotoUrl = firebaseUser.photoUrl?.toString(),
                                isEmailVerified = firebaseUser.isEmailVerified,
                                createdAt = null,
                                updatedAt = null,
                                lastLoginAt = null,
                                isCloudSyncEnabled = true,
                                preferences = com.negociolisto.app.domain.model.UserPreferences()
                            )
                            val currentState = _uiState.value
                            _uiState.value = currentState.copy(
                                currentUser = basicUser,
                                name = basicUser.name,
                                email = basicUser.email,
                                phone = basicUser.phone ?: currentState.phone,
                                isLoading = false
                            )
                            println("✅ DEBUG EditProfileViewModel: Usuario básico creado desde Firebase Auth")
                        }
                    } else {
                        // Para otros repositorios, usar datos del repositorio
                        authRepository.currentUser.collect { user ->
                            val currentState = _uiState.value
                            _uiState.value = currentState.copy(
                                currentUser = user,
                                name = user?.name ?: currentState.name,
                                email = user?.email ?: currentState.email,
                                phone = user?.phone ?: currentState.phone,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    // No hay usuario autenticado
                    println("❌ DEBUG: No hay usuario autenticado en Firebase")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No hay usuario autenticado"
                    )
                }
            } catch (e: Exception) {
                println("❌ DEBUG EditProfileViewModel: Error cargando datos: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error cargando datos del usuario"
                )
            }
        }
    }

    fun updateName(name: String) {
        try {
            _uiState.value = _uiState.value.copy(
                name = name,
                nameError = null, // No mostrar error mientras se escribe
                hasChanges = true
            )
        } catch (e: Exception) {
            println("Error actualizando nombre: ${e.message}")
        }
    }

    fun updatePhone(phone: String) {
        try {
            _uiState.value = _uiState.value.copy(
                phone = phone,
                phoneError = null, // No mostrar error mientras se escribe
                hasChanges = true
            )
        } catch (e: Exception) {
            println("Error actualizando teléfono: ${e.message}")
        }
    }

    fun selectImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            hasChanges = true
        )
        
        // Copiar imagen al caché para backup/restore
        viewModelScope.launch {
            try {
                val fileName = "profile_${System.currentTimeMillis()}.jpg"
                val imageFile = File(context.cacheDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    imageFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                println("👤 DEBUG: Imagen de perfil guardada en caché: ${imageFile.absolutePath}")
            } catch (e: Exception) {
                println("❌ DEBUG: Error guardando imagen de perfil en caché: ${e.message}")
            }
        }
    }

    fun showImagePicker() {
        _uiState.value = _uiState.value.copy(
            shouldShowImagePicker = true
        )
    }

    fun hideImagePicker() {
        _uiState.value = _uiState.value.copy(
            shouldShowImagePicker = false
        )
    }

    fun prepareCameraUri(context: Context): Uri? {
        return try {
            val imageFile = File(context.cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            _uiState.value = _uiState.value.copy(
                cameraUri = uri
            )
            uri
        } catch (e: Exception) {
            println("Error preparando URI de cámara: ${e.message}")
            null
        }
    }

    fun saveProfile(context: Context) {
        if (!isFormValid()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Verificar autenticación desde Firebase Auth directamente
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No hay usuario autenticado"
                    )
                    return@launch
                }
                
                // Obtener usuario actual del repositorio o crear uno básico si es necesario
                val currentUser = _uiState.value.currentUser ?: run {
                    println("⚠️ DEBUG: currentUser es null, creando usuario básico desde Firebase")
                    com.negociolisto.app.domain.model.User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: _uiState.value.name,
                        phone = _uiState.value.phone,
                        businessName = null,
                        businessType = null,
                        businessRut = null,
                        businessAddress = null,
                        businessPhone = null,
                        businessEmail = null,
                        businessSocialMedia = null,
                        businessLogoUrl = null,
                        profilePhotoUrl = null,
                        isEmailVerified = firebaseUser.isEmailVerified,
                        createdAt = null,
                        updatedAt = null,
                        lastLoginAt = null,
                        isCloudSyncEnabled = true,
                        preferences = com.negociolisto.app.domain.model.UserPreferences()
                    )
                }
                
                // Crear usuario actualizado
                val updatedUser = currentUser.copy(
                    name = _uiState.value.name.trim(),
                    phone = _uiState.value.phone.takeIf { it.isNotBlank() }
                )

                // Guardar imagen localmente si se seleccionó una nueva
                var profilePhotoUrl = currentUser.profilePhotoUrl
                _uiState.value.selectedImageUri?.let { imageUri ->
                    try {
                        // Intentar procesar la imagen
                        val result = imageService.saveImageLocally(
                            context,
                            imageUri,
                            "profile_photo_${System.currentTimeMillis()}.jpg"
                        )
                        result.onSuccess { file ->
                            profilePhotoUrl = file.absolutePath
                        }.onFailure { error ->
                            println("Error guardando imagen: ${error.message}")
                            // Continuar sin la imagen si falla
                        }
                    } catch (e: Exception) {
                        println("Error procesando imagen: ${e.message}")
                        // Continuar sin la imagen si falla
                    }
                }

                val finalUser = updatedUser.copy(profilePhotoUrl = profilePhotoUrl)

                // Actualizar en el repositorio
                authRepository.updateProfile(finalUser)
                    .onSuccess {
                        // Invalidar caché de imágenes para refrescar visualmente
                        com.negociolisto.app.ui.components.ImageRefreshBus.bump()
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSaved = true,
                            hasChanges = false,
                            selectedImageUri = null
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al guardar: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                println("Error inesperado en saveProfile: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    private fun isFormValid(): Boolean {
        val currentState = _uiState.value
        var isValid = true

        // Validar nombre
        if (currentState.name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                nameError = "El nombre es obligatorio"
            )
            isValid = false
        }

        // Validar teléfono si se proporciona
        if (currentState.phone.isNotBlank() && !isValidPhone(currentState.phone)) {
            _uiState.value = _uiState.value.copy(
                phoneError = "Formato de teléfono inválido"
            )
            isValid = false
        }

        return isValid
    }

    private fun isValidPhone(phone: String): Boolean {
        // Validación básica de teléfono (solo números, guiones, espacios y paréntesis)
        return phone.matches(Regex("[\\d\\s\\-\\(\\)\\+]+")) && phone.length >= 7
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            nameError = null,
            phoneError = null
        )
    }
}

/**
 * 📊 ESTADO DE LA UI DE EDICIÓN DE PERFIL
 */
data class EditProfileUiState(
    val currentUser: User? = null,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val selectedImageUri: Uri? = null,
    val cameraUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val hasChanges: Boolean = false,
    val shouldShowImagePicker: Boolean = false,
    val nameError: String? = null,
    val phoneError: String? = null,
    val errorMessage: String? = null
)
