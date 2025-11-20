package com.negociolisto.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.service.ImageService
import com.negociolisto.app.domain.model.BusinessType
import com.negociolisto.app.domain.model.BusinessSocialMedia
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
 * 🏢 VIEWMODEL PARA EDICIÓN DE EMPRESA
 * 
 * Maneja el estado y la lógica para editar la información de la empresa.
 */
@HiltViewModel
class EditCompanyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val imageService: ImageService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditCompanyUiState())
    val uiState: StateFlow<EditCompanyUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                // Si es FirebaseAuthRepository, cargar datos completos desde Firestore
                if (authRepository is com.negociolisto.app.data.remote.firebase.FirebaseAuthRepository) {
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        val fullUserData = authRepository.loadUserFromFirestore(firebaseUser)
                        _uiState.value = _uiState.value.copy(
                            currentUser = fullUserData,
                            businessName = fullUserData.businessName ?: "",
                            selectedBusinessType = fullUserData.businessType,
                            businessAddress = fullUserData.businessAddress ?: "",
                            businessPhone = fullUserData.businessPhone ?: "",
                            businessEmail = fullUserData.businessEmail ?: "",
                            businessRut = fullUserData.businessRut ?: "",
                            businessSocialMedia = fullUserData.businessSocialMedia ?: BusinessSocialMedia(),
                            selectedLogoUri = fullUserData.businessLogoUrl?.let { Uri.parse(it) },
                            isLoading = false
                        )
                        println("✅ DEBUG EditCompanyViewModel: Datos completos cargados desde Firestore")
                        println("✅ DEBUG EditCompanyViewModel: businessName = ${fullUserData.businessName}")
                        println("✅ DEBUG EditCompanyViewModel: businessType = ${fullUserData.businessType}")
                    } else {
                        // Fallback a datos básicos si no hay usuario de Firebase
                        authRepository.currentUser.collect { user ->
                            _uiState.value = _uiState.value.copy(
                                currentUser = user,
                                businessName = user?.businessName ?: "",
                                selectedBusinessType = user?.businessType,
                                businessAddress = user?.businessAddress ?: "",
                                businessPhone = user?.businessPhone ?: "",
                                businessEmail = user?.businessEmail ?: "",
                                businessRut = user?.businessRut ?: "",
                                businessSocialMedia = user?.businessSocialMedia ?: BusinessSocialMedia(),
                                selectedLogoUri = user?.businessLogoUrl?.let { Uri.parse(it) },
                                isLoading = false
                            )
                        }
                    }
                } else {
                    // Para otros repositorios, usar datos básicos
                    authRepository.currentUser.collect { user ->
                        _uiState.value = _uiState.value.copy(
                            currentUser = user,
                            businessName = user?.businessName ?: "",
                            selectedBusinessType = user?.businessType,
                            businessAddress = user?.businessAddress ?: "",
                            businessPhone = user?.businessPhone ?: "",
                            businessEmail = user?.businessEmail ?: "",
                            businessRut = user?.businessRut ?: "",
                            businessSocialMedia = user?.businessSocialMedia ?: BusinessSocialMedia(),
                            selectedLogoUri = user?.businessLogoUrl?.let { Uri.parse(it) },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                println("❌ DEBUG EditCompanyViewModel: Error cargando datos: ${e.message}")
                // Fallback a datos básicos en caso de error
                authRepository.currentUser.collect { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        businessName = user?.businessName ?: "",
                        selectedBusinessType = user?.businessType,
                        businessAddress = user?.businessAddress ?: "",
                        businessPhone = user?.businessPhone ?: "",
                        businessEmail = user?.businessEmail ?: "",
                        businessRut = user?.businessRut ?: "",
                        businessSocialMedia = user?.businessSocialMedia ?: BusinessSocialMedia(),
                        selectedLogoUri = user?.businessLogoUrl?.let { Uri.parse(it) },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateBusinessName(businessName: String) {
        _uiState.value = _uiState.value.copy(
            businessName = businessName,
            businessNameError = null, // Solo validar al guardar
            hasChanges = true
        )
    }

    fun selectBusinessType(businessType: BusinessType) {
        _uiState.value = _uiState.value.copy(
            selectedBusinessType = businessType,
            isBusinessTypeDropdownExpanded = false,
            hasChanges = true
        )
    }


    fun updateBusinessAddress(address: String) {
        _uiState.value = _uiState.value.copy(
            businessAddress = address,
            hasChanges = true
        )
    }

    fun updateBusinessPhone(phone: String) {
        _uiState.value = _uiState.value.copy(
            businessPhone = phone,
            businessPhoneError = null, // Solo validar al guardar
            hasChanges = true
        )
    }

    fun updateBusinessEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            businessEmail = email,
            businessEmailError = null, // Solo validar al guardar
            hasChanges = true
        )
    }

    fun updateBusinessRut(rut: String) {
        _uiState.value = _uiState.value.copy(
            businessRut = rut,
            businessRutError = null, // Solo validar al guardar
            hasChanges = true
        )
    }

    fun updateSocialMedia(socialMedia: BusinessSocialMedia) {
        _uiState.value = _uiState.value.copy(
            businessSocialMedia = socialMedia,
            hasChanges = true
        )
    }

    fun toggleBusinessTypeDropdown() {
        _uiState.value = _uiState.value.copy(
            isBusinessTypeDropdownExpanded = !_uiState.value.isBusinessTypeDropdownExpanded
        )
    }

    fun hideBusinessTypeDropdown() {
        _uiState.value = _uiState.value.copy(
            isBusinessTypeDropdownExpanded = false
        )
    }

    fun selectLogo(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedLogoUri = uri,
            hasChanges = true
        )
        
        // Copiar imagen al caché para backup/restore
        viewModelScope.launch {
            try {
                val fileName = "company_logo_${System.currentTimeMillis()}.jpg"
                val imageFile = File(context.cacheDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    imageFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                // Actualizar el estado con la nueva URL del archivo local
                _uiState.value = _uiState.value.copy(
                    selectedLogoUri = Uri.fromFile(imageFile)
                )
                
                println("🏢 DEBUG: Logo de empresa guardado en caché: ${imageFile.absolutePath}")
            } catch (e: Exception) {
                println("❌ DEBUG: Error guardando logo de empresa en caché: ${e.message}")
            }
        }
    }

    fun showLogoPicker() {
        _uiState.value = _uiState.value.copy(
            shouldShowLogoPicker = true
        )
    }

    fun hideLogoPicker() {
        _uiState.value = _uiState.value.copy(
            shouldShowLogoPicker = false
        )
    }

    fun prepareCameraUri(context: Context): Uri? {
        return try {
            val imageFile = File(context.cacheDir, "company_logo_${System.currentTimeMillis()}.jpg")
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
            null
        }
    }

    fun saveCompany(context: Context) {
        println("🔍 DEBUG: Iniciando guardado de empresa...")
        
        if (!isFormValid()) {
            println("❌ DEBUG: Validación falló, no se puede guardar")
            return
        }

        println("✅ DEBUG: Validación pasó, procediendo con el guardado")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Verificar autenticación desde Firebase Auth directamente
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser == null) {
                    println("❌ DEBUG: No hay usuario autenticado en Firebase")
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
                }
                
                println("👤 DEBUG: Usuario actual: ${currentUser.name}")
                
                // Crear usuario actualizado
                val updatedUser = currentUser.copy(
                    businessName = _uiState.value.businessName.trim(),
                    businessType = _uiState.value.selectedBusinessType,
                    businessRut = _uiState.value.businessRut.trim().takeIf { it.isNotBlank() },
                    businessAddress = _uiState.value.businessAddress.trim().takeIf { it.isNotBlank() },
                    businessPhone = _uiState.value.businessPhone.trim().takeIf { it.isNotBlank() },
                    businessEmail = _uiState.value.businessEmail.trim().takeIf { it.isNotBlank() },
                    businessSocialMedia = _uiState.value.businessSocialMedia.takeIf { it.hasAnySocialMedia() },
                    businessLogoUrl = _uiState.value.selectedLogoUri?.toString() ?: currentUser.businessLogoUrl,
                )

                println("📝 DEBUG: Datos a guardar:")
                println("  - Nombre: ${updatedUser.businessName}")
                println("  - Tipo: ${updatedUser.businessType}")
                println("  - Dirección: ${updatedUser.businessAddress}")
                println("  - Teléfono: ${updatedUser.businessPhone}")
                println("  - Email: ${updatedUser.businessEmail}")
                println("  - RUT: ${updatedUser.businessRut}")
                println("  - Logo: ${updatedUser.businessLogoUrl}")
                
                // Actualizar en el repositorio
                val result = authRepository.updateProfile(updatedUser)
                
                result.onSuccess { savedUser ->
                    println("✅ DEBUG: Usuario guardado exitosamente")
                    
                    // Invalidar caché de imágenes para refrescar visualmente
                    com.negociolisto.app.ui.components.ImageRefreshBus.bump()
                    
                    // Guardar en SharedPreferences para respaldo
                    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putString("user_name", savedUser.name)
                    editor.putString("user_email", savedUser.email)
                    editor.putString("user_phone", savedUser.phone)
                    editor.putString("business_name", savedUser.businessName)
                    editor.putString("business_type", savedUser.businessType?.name)
                    editor.putString("business_rut", savedUser.businessRut)
                    editor.putString("business_address", savedUser.businessAddress)
                    editor.putString("business_phone", savedUser.businessPhone)
                    editor.putString("business_email", savedUser.businessEmail)
                    editor.putString("business_logo_url", savedUser.businessLogoUrl)
                    editor.apply()
                    println("✅ DEBUG: Información guardada en SharedPreferences")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true,
                        hasChanges = false,
                        currentUser = savedUser,
                        selectedLogoUri = savedUser.businessLogoUrl?.let { Uri.parse(it) }
                    )
                    println("✅ DEBUG: isSaved establecido a true")
                }
                result.onFailure { error ->
                    println("❌ DEBUG: Error al guardar: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al guardar: ${error.message}"
                    )
                }
            } catch (e: Exception) {
                println("❌ DEBUG: Excepción: ${e.message}")
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

        // Solo validar nombre del negocio como obligatorio
        if (currentState.businessName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                businessNameError = "El nombre del negocio es obligatorio"
            )
            isValid = false
        }

        // Validar teléfono del negocio si se proporciona (pero no bloquear el guardado)
        if (currentState.businessPhone.isNotBlank() && !isValidPhone(currentState.businessPhone)) {
            _uiState.value = _uiState.value.copy(
                businessPhoneError = "Formato de teléfono inválido"
            )
            // No marcar como inválido, solo mostrar advertencia
        }

        // Validar email del negocio si se proporciona (pero no bloquear el guardado)
        if (currentState.businessEmail.isNotBlank() && !isValidEmail(currentState.businessEmail)) {
            _uiState.value = _uiState.value.copy(
                businessEmailError = "Formato de email inválido"
            )
            // No marcar como inválido, solo mostrar advertencia
        }

        return isValid
    }

    private fun isValidPhone(phone: String): Boolean {
        // Validación básica de teléfono (solo números, guiones, espacios y paréntesis)
        return phone.matches(Regex("[\\d\\s\\-\\(\\)\\+]+")) && phone.length >= 7
    }

    private fun isValidEmail(email: String): Boolean {
        // Validación básica de email
        return email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
    }

    private fun isValidRut(rut: String): Boolean {
        // Validación básica de RUT chileno
        val cleanRut = rut.replace(Regex("[^0-9kK]"), "")
        if (cleanRut.length < 8) return false
        
        val rutNumber = cleanRut.dropLast(1)
        val checkDigit = cleanRut.last().toString()
        
        // Validar que el número del RUT sea válido
        if (!rutNumber.all { it.isDigit() }) return false
        
        // Validar dígito verificador
        val calculatedCheckDigit = calculateRutCheckDigit(rutNumber)
        return checkDigit.equals(calculatedCheckDigit, ignoreCase = true)
    }

    private fun calculateRutCheckDigit(rutNumber: String): String {
        var sum = 0
        var multiplier = 2
        
        for (i in rutNumber.reversed()) {
            sum += i.toString().toInt() * multiplier
            multiplier = if (multiplier == 7) 2 else multiplier + 1
        }
        
        val remainder = sum % 11
        val checkDigit = 11 - remainder
        
        return when (checkDigit) {
            11 -> "0"
            10 -> "K"
            else -> checkDigit.toString()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            businessNameError = null,
            businessPhoneError = null,
            businessEmailError = null,
            businessRutError = null
        )
    }
}

/**
 * 📊 ESTADO DE LA UI DE EDICIÓN DE EMPRESA
 */
data class EditCompanyUiState(
    val currentUser: User? = null,
    val businessName: String = "",
    val selectedBusinessType: BusinessType? = null,
    val businessAddress: String = "",
    val businessPhone: String = "",
    val businessEmail: String = "",
    val businessRut: String = "",
    val businessSocialMedia: BusinessSocialMedia = BusinessSocialMedia(),
    val selectedLogoUri: Uri? = null,
    val cameraUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val hasChanges: Boolean = false,
    val shouldShowLogoPicker: Boolean = false,
    val isBusinessTypeDropdownExpanded: Boolean = false,
    val businessNameError: String? = null,
    val businessPhoneError: String? = null,
    val businessEmailError: String? = null,
    val businessRutError: String? = null,
    val errorMessage: String? = null
)
