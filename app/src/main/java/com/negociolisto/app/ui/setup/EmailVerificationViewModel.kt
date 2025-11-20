package com.negociolisto.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.local.UiPreferencesStore
import com.negociolisto.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 📧 VIEWMODEL PARA VERIFICACIÓN DE EMAIL
 * 
 * Gestiona el estado de la verificación de email durante el onboarding.
 */
@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val uiPreferencesStore: UiPreferencesStore
) : ViewModel() {
    
    data class EmailVerificationUiState(
        val email: String = "",
        val isVerified: Boolean = false,
        val isSending: Boolean = false,
        val isChecking: Boolean = false,
        val message: String = "",
        val isError: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            email = it.email,
                            isVerified = it.isEmailVerified
                        )
                    }
                }
            }
        }
    }
    
    fun checkVerification(onVerified: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, message = "") }
            
            try {
                val isVerified = authRepository.checkEmailVerification()
                
                if (isVerified) {
                    uiPreferencesStore.setEmailVerified(true)
                    _uiState.update { 
                        it.copy(
                            isVerified = true,
                            isChecking = false,
                            message = "¡Email verificado exitosamente!",
                            isError = false
                        )
                    }
                    onVerified()
                } else {
                    _uiState.update { 
                        it.copy(
                            isChecking = false,
                            message = "El email aún no ha sido verificado. Revisa tu bandeja de entrada.",
                            isError = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isChecking = false,
                        message = "Error al verificar el email: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }
    
    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, message = "") }
            
            try {
                val result = authRepository.sendEmailVerification()
                
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                isSending = false,
                                message = "Correo de verificación reenviado exitosamente. Revisa tu bandeja de entrada.",
                                isError = false
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isSending = false,
                                message = "Error al reenviar el correo: ${error.message}",
                                isError = true
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSending = false,
                        message = "Error inesperado: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }
}










