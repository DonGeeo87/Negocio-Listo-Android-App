package com.negociolisto.app.ui.setup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.local.UiPreferencesStore
import com.negociolisto.app.domain.model.BusinessType
import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🏗️ VIEWMODEL PARA CONFIGURACIÓN DE PERFILES
 * 
 * Gestiona el estado de la configuración de perfil personal y empresarial
 * durante el onboarding inicial.
 */
@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val uiPreferencesStore: UiPreferencesStore
) : ViewModel() {
    
    data class ProfileSetupUiState(
        // Perfil personal
        val personalName: String = "",
        val personalPhone: String = "",
        val personalPhotoUri: String? = null,
        
        // Perfil empresarial
        val businessName: String = "",
        val businessType: BusinessType? = null,
        val businessRut: String = "",
        val businessAddress: String = "",
        val businessPhone: String = "",
        val businessEmail: String = "",
        val businessLogoUri: String? = null,
        
        // Estados de validación
        val personalNameError: String = "",
        val personalPhoneError: String = "",
        val businessNameError: String = "",
        
        // Estados de UI
        val isSaving: Boolean = false,
        val isValid: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentUser()
        validateForm()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            personalName = it.name,
                            personalPhone = it.phone ?: "",
                            businessName = it.businessName ?: "",
                            businessRut = it.businessRut ?: "",
                            businessAddress = it.businessAddress ?: "",
                            businessPhone = it.businessPhone ?: "",
                            businessEmail = it.businessEmail ?: "",
                            businessType = it.businessType,
                            personalPhotoUri = it.profilePhotoUrl,
                            businessLogoUri = it.businessLogoUrl
                        )
                    }
                    validateForm()
                }
            }
        }
    }
    
    private fun validateForm() {
        val currentState = _uiState.value
        val personalNameValid = currentState.personalName.isNotBlank()
        val personalPhoneValid = currentState.personalPhone.isEmpty() || 
                                 currentState.personalPhone.matches(Regex("^\\+?[1-9]\\d{1,14}$"))
        
        _uiState.update { state ->
            state.copy(
                isValid = personalNameValid && personalPhoneValid,
                personalNameError = if (personalNameValid) "" else "El nombre es requerido",
                personalPhoneError = if (personalPhoneValid) "" else "Formato de teléfono inválido"
            )
        }
    }
    
    fun updatePersonalName(name: String) {
        _uiState.update { it.copy(personalName = name) }
        validateForm()
    }
    
    fun updatePersonalPhone(phone: String) {
        _uiState.update { it.copy(personalPhone = phone) }
        validateForm()
    }
    
    fun updatePersonalPhoto(photoUri: String?) {
        _uiState.update { it.copy(personalPhotoUri = photoUri) }
    }
    
    fun updateBusinessName(name: String) {
        _uiState.update { it.copy(businessName = name) }
        validateForm()
    }
    
    fun updateBusinessType(type: BusinessType?) {
        _uiState.update { it.copy(businessType = type) }
    }
    
    fun updateBusinessRut(rut: String) {
        _uiState.update { it.copy(businessRut = rut) }
    }
    
    fun updateBusinessAddress(address: String) {
        _uiState.update { it.copy(businessAddress = address) }
    }
    
    fun updateBusinessPhone(phone: String) {
        _uiState.update { it.copy(businessPhone = phone) }
    }
    
    fun updateBusinessEmail(email: String) {
        _uiState.update { it.copy(businessEmail = email) }
    }
    
    fun updateBusinessLogo(logoUri: String?) {
        _uiState.update { it.copy(businessLogoUri = logoUri) }
    }
    
    fun saveProfiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val currentState = _uiState.value
                
                // Obtener usuario actual
                val currentUser = authRepository.currentUser.first()
                
                if (currentUser != null) {
                    // Crear usuario actualizado
                    val updatedUser = currentUser.copy(
                        name = currentState.personalName,
                        phone = currentState.personalPhone.takeIf { it.isNotEmpty() },
                        businessName = currentState.businessName.takeIf { it.isNotEmpty() },
                        businessType = currentState.businessType,
                        businessRut = currentState.businessRut.takeIf { it.isNotEmpty() },
                        businessAddress = currentState.businessAddress.takeIf { it.isNotEmpty() },
                        businessPhone = currentState.businessPhone.takeIf { it.isNotEmpty() },
                        businessEmail = currentState.businessEmail.takeIf { it.isNotEmpty() },
                        profilePhotoUrl = currentState.personalPhotoUri,
                        businessLogoUrl = currentState.businessLogoUri
                    )
                    
                    // Actualizar usuario en el repositorio
                    // Nota: Esto requiere implementar updateUser en AuthRepository
                    // authRepository.updateUser(updatedUser)
                    
                    // Marcar como completado en preferencias
                    uiPreferencesStore.setProfileSetupCompleted(true)
                    
                    if (currentState.businessName.isNotEmpty()) {
                        uiPreferencesStore.setCompanySetupCompleted(true)
                    }
                    
                    _uiState.update { it.copy(isSaving = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                // TODO: Mostrar error al usuario
            }
        }
    }
}










