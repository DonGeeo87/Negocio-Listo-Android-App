package com.negociolisto.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.data.service.GoogleSignInService
import com.negociolisto.app.data.service.DataSyncManager
import com.negociolisto.app.data.service.LoginTrackingService
import com.negociolisto.app.data.analytics.AnalyticsHelper
import com.negociolisto.app.data.analytics.CrashlyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🔐 VIEWMODEL DE AUTENTICACIÓN
 * 
 * Maneja el estado y la lógica de autenticación.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInService: GoogleSignInService,
    private val loginTrackingService: LoginTrackingService,
    private val analyticsHelper: AnalyticsHelper,
    private val crashlyticsHelper: CrashlyticsHelper,
    private val dataSyncManager: DataSyncManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()
    
    // Estado de autenticación desde el repositorio
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    // Estado de autenticación de Google (para sincronización)
    val isGoogleAuthenticated: StateFlow<Boolean> = googleSignInService.isAuthenticated
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }
    
    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }
    
    fun updateBusinessName(businessName: String) {
        _uiState.value = _uiState.value.copy(businessName = businessName)
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }
    
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }
    
    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }
    
    fun login() {
        val currentState = _uiState.value
        if (!currentState.isLoginFormValid()) return
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            authRepository.login(currentState.email, currentState.password)
                .onSuccess { user ->
                    // Registrar el login exitoso
                    loginTrackingService.recordLogin()
                    
                    // Analytics
                    analyticsHelper.logLogin("email")
                    analyticsHelper.setUserId(user.id)
                    
                    // Crashlytics
                    crashlyticsHelper.setUserId(user.id)
                    crashlyticsHelper.setCustomKey("user_email", user.email)
                    
                    _uiState.value = currentState.copy(isLoading = false)
                    triggerCloudSync()
                }
                .onFailure { error ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = "Error al iniciar sesión: ${error.message}"
                    )
                    crashlyticsHelper.recordException(error)
                    crashlyticsHelper.log("Error en login: ${error.message}")
                }
        }
    }
    
    fun register() {
        val currentState = _uiState.value
        if (!currentState.isRegistrationFormValid()) return
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            authRepository.register(
                email = currentState.email,
                password = currentState.password,
                name = currentState.name,
                phone = currentState.phone.takeIf { it.isNotBlank() },
                businessName = currentState.businessName.takeIf { it.isNotBlank() }
            )
                .onSuccess { user ->
                    // Analytics
                    analyticsHelper.logSignUp("email")
                    user?.let {
                        analyticsHelper.setUserId(it.id)
                        crashlyticsHelper.setUserId(it.id)
                        crashlyticsHelper.setCustomKey("user_email", it.email)
                    }
                    
                    _uiState.value = currentState.copy(isLoading = false)
                }
                .onFailure { error ->
                    println("AuthViewModel register error: ${error::class.simpleName} - ${error.message}")
                    val friendlyMessage = when (error) {
                        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con este correo. Usa Iniciar sesión o recupera tu contraseña."
                        else -> "Error al crear cuenta: ${error.message}"
                    }
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = friendlyMessage
                    )
                    crashlyticsHelper.recordException(error)
                    crashlyticsHelper.log("Error en registro: ${error.message}")
                }
        }
    }
    
    fun sendPasswordResetEmail() {
        val currentState = _uiState.value
        if (currentState.email.isBlank()) {
            _uiState.value = currentState.copy(error = "Ingresa tu email")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            authRepository.sendPasswordResetEmail(currentState.email)
                .onSuccess {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = "Email de recuperación enviado"
                    )
                }
                .onFailure { error ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = "Error al enviar email: ${error.message}"
                    )
                }
        }
    }
    
    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Ingresa tu email")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = true,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al enviar email de recuperación"
                    )
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun triggerCloudSync() {
        viewModelScope.launch {
            dataSyncManager.syncUserData()
                .onFailure { error ->
                    println("⚠️ Sync post login falló: ${error.message}")
                }
        }
    }
    
    /**
     * 🧹 FORZAR LOGOUT COMPLETO
     * 
     * Método para forzar el logout cuando hay problemas de persistencia
     */
    fun forceLogout() {
        viewModelScope.launch {
            println("🧹 DEBUG AuthViewModel: Iniciando logout forzado...")
            
            // Cerrar sesión en Google también
            googleSignInService.signOut()
            
            // Cerrar sesión en Firebase
            authRepository.logout()
            
            // Limpiar estado local
            _uiState.value = AuthUiState()
            
            println("✅ DEBUG AuthViewModel: Logout forzado completado")
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // 🔐 MÉTODOS DE GOOGLE SIGN-IN
    
    /**
     * 🚀 OBTENER INTENT DE GOOGLE SIGN-IN
     */
    fun getGoogleSignInIntent(): android.content.Intent {
        return googleSignInService.getSignInIntentForcedSelector()
    }
    
    /**
     * 🔄 OBTENER INTENT CON SELECTOR DE CUENTAS
     */
    fun getGoogleSignInIntentWithAccountSelector(): android.content.Intent {
        return googleSignInService.getSignInIntentWithAccountSelector()
    }
    
    /**
     * ✅ PROCESAR RESULTADO DE GOOGLE SIGN-IN
     */
    fun handleGoogleSignInResult(data: android.content.Intent?) {
        println("🔍 DEBUG AuthViewModel: handleGoogleSignInResult() llamado con data: ${data != null}")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            println("🔍 DEBUG AuthViewModel: Estado actualizado a loading=true")
            
            val result = googleSignInService.handleSignInResult(data)
            println("🔍 DEBUG AuthViewModel: Resultado de GoogleSignInService: ${result.isSuccess}")
            
            if (result.isSuccess) {
                println("✅ DEBUG AuthViewModel: Autenticación exitosa, registrando login...")
                // Registrar el login exitoso con Google
                loginTrackingService.recordLogin()
                
                // Analytics
                analyticsHelper.logLogin("google")
                // Obtener usuario actual para setUserId
                currentUser.value?.let { user ->
                    analyticsHelper.setUserId(user.id)
                    crashlyticsHelper.setUserId(user.id)
                    crashlyticsHelper.setCustomKey("user_email", user.email)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null // Limpiar error para que no se muestre
                )
                println("✅ DEBUG AuthViewModel: Estado actualizado a loading=false, error=null")
                triggerCloudSync()
                
                // Verificar el estado de autenticación después de un breve delay
                kotlinx.coroutines.delay(1000)
                val authState = isAuthenticated.value
                println("🔍 DEBUG AuthViewModel: Estado de autenticación después de 1 segundo: $authState")
                
                // La navegación se manejará automáticamente por el estado de autenticación
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = error?.message ?: "Error en autenticación con Google"
                println("❌ DEBUG AuthViewModel: Error en autenticación: $errorMessage")
                
                // Crashlytics
                error?.let { crashlyticsHelper.recordException(it) }
                crashlyticsHelper.log("Error en Google Sign In: $errorMessage")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }
    
    /**
     * 🔄 VERIFICAR ESTADO DE GOOGLE SIGN-IN
     */
    fun checkGoogleAuthStatus() {
        googleSignInService.checkAuthStatus()
    }
}

/**
 * 📋 ESTADO DE LA UI DE AUTENTICACIÓN
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val phone: String = "",
    val businessName: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
) {
    fun isLoginFormValid(): Boolean {
        return email.isNotBlank() && 
               email.contains("@") && 
               password.length >= 6
    }
    
    fun isRegistrationFormValid(): Boolean {
        return email.isNotBlank() && 
               email.contains("@") && 
               password.length >= 8 && 
               name.isNotBlank() && 
               password == confirmPassword
    }
}