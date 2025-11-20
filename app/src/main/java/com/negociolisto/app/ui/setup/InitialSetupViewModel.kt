package com.negociolisto.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.local.UiPreferencesStore
import com.negociolisto.app.data.service.ImageService
import com.negociolisto.app.domain.model.BusinessType
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.util.UUID
import javax.inject.Inject
import android.net.Uri
import android.content.Context

/**
 * 🚀 VIEWMODEL PARA CONFIGURACIÓN INICIAL
 * 
 * Maneja el estado y la navegación del flujo de configuración inicial
 * que incluye múltiples pasos: bienvenida, categorías, perfil, empresa, clientes.
 */
@HiltViewModel
class InitialSetupViewModel @Inject constructor(
    private val uiPreferencesStore: UiPreferencesStore,
    private val authRepository: AuthRepository,
    private val customerRepository: CustomerRepository,
    private val imageService: ImageService
) : ViewModel() {

    private val _uiState = MutableStateFlow(InitialSetupUiState())
    val uiState: StateFlow<InitialSetupUiState> = _uiState.asStateFlow()

    private val _currentStep = MutableStateFlow(InitialSetupStep.WELCOME)
    val currentStep: StateFlow<InitialSetupStep> = _currentStep.asStateFlow()

    // Obtener el userId actual del usuario autenticado
    private val currentUserIdFlow: StateFlow<String?> = authRepository.currentUser
        .map { it?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // Verificar si el usuario ya completó el setup inicial
        viewModelScope.launch {
            currentUserIdFlow.collect { userId ->
                if (userId != null) {
                    val isSetupCompleted = uiPreferencesStore.initialSetupCompletedForUser(userId).first()
                    // Solo ir al paso final si está verdaderamente completado
                    if (isSetupCompleted && _currentStep.value != InitialSetupStep.COMPLETE) {
                        _currentStep.value = InitialSetupStep.COMPLETE
                        _uiState.value = _uiState.value.copy(isSetupCompleted = true)
                    }
                }
            }
        }

        // Prefill de datos provenientes del registro / perfil del usuario
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            userName = state.userName.takeIf { it.isNotBlank() } ?: it.name,
                            userPhone = state.userPhone.takeIf { it.isNotBlank() } ?: (it.phone ?: ""),
                            businessName = state.businessName.takeIf { it.isNotBlank() } ?: (it.businessName ?: ""),
                            businessPhone = state.businessPhone.takeIf { it.isNotBlank() } ?: (it.businessPhone ?: ""),
                            businessEmail = state.businessEmail.takeIf { it.isNotBlank() } ?: (it.businessEmail ?: ""),
                            businessAddress = state.businessAddress.takeIf { it.isNotBlank() } ?: (it.businessAddress ?: ""),
                            businessRut = state.businessRut.takeIf { it.isNotBlank() } ?: (it.businessRut ?: "")
                        )
                    }
                }
            }
        }
    }

    /**
     * Avanzar al siguiente paso
     */
    fun nextStep() {
        val currentStepValue = _currentStep.value
        val nextStep = when (currentStepValue) {
            InitialSetupStep.WELCOME -> InitialSetupStep.CATEGORIES
            InitialSetupStep.CATEGORIES -> InitialSetupStep.PROFILE
            InitialSetupStep.PROFILE -> InitialSetupStep.COMPANY
            InitialSetupStep.COMPANY -> InitialSetupStep.CUSTOMERS
            InitialSetupStep.CUSTOMERS -> InitialSetupStep.COMPLETE
            InitialSetupStep.COMPLETE -> InitialSetupStep.COMPLETE
        }
        
        _currentStep.value = nextStep
    }

    /**
     * Retroceder al paso anterior
     */
    fun previousStep() {
        val currentStepValue = _currentStep.value
        val previousStep = when (currentStepValue) {
            InitialSetupStep.WELCOME -> InitialSetupStep.WELCOME
            InitialSetupStep.CATEGORIES -> InitialSetupStep.WELCOME
            InitialSetupStep.PROFILE -> InitialSetupStep.CATEGORIES
            InitialSetupStep.COMPANY -> InitialSetupStep.PROFILE
            InitialSetupStep.CUSTOMERS -> InitialSetupStep.COMPANY
            InitialSetupStep.COMPLETE -> InitialSetupStep.CUSTOMERS
        }
        
        _currentStep.value = previousStep
    }

    /**
     * Ir a un paso específico
     */
    fun goToStep(step: InitialSetupStep) {
        _currentStep.value = step
    }

    /**
     * Marcar el setup inicial como completado para este usuario
     */
    private fun markSetupAsCompleted() {
        viewModelScope.launch {
            val userId = currentUserIdFlow.value
            if (userId != null) {
                uiPreferencesStore.setInitialSetupCompletedForUser(userId, true)
                _uiState.value = _uiState.value.copy(
                    isSetupCompleted = true
                )
            }
        }
    }

    /**
     * Obtener el progreso actual (0.0 a 1.0)
     */
    fun getProgress(): Float {
        return (_currentStep.value.ordinal + 1).toFloat() / InitialSetupStep.values().size.toFloat()
    }

    /**
     * Verificar si se puede avanzar desde el paso actual
     */
    fun canProceedFromCurrentStep(): Boolean {
        return when (_currentStep.value) {
            InitialSetupStep.WELCOME -> true
            InitialSetupStep.CATEGORIES -> true // Se valida en InitialCategorySetupViewModel
            InitialSetupStep.PROFILE -> true
            InitialSetupStep.COMPANY -> true
            InitialSetupStep.CUSTOMERS -> true
            InitialSetupStep.COMPLETE -> true
        }
    }

    /**
     * Obtener el título del paso actual
     */
    fun getCurrentStepTitle(): String {
        return when (_currentStep.value) {
            InitialSetupStep.WELCOME -> "Bienvenida"
            InitialSetupStep.CATEGORIES -> "Categorías"
            InitialSetupStep.PROFILE -> "Perfil"
            InitialSetupStep.COMPANY -> "Empresa"
            InitialSetupStep.CUSTOMERS -> "Clientes"
            InitialSetupStep.COMPLETE -> "Completado"
        }
    }

    /**
     * Obtener la descripción del paso actual
     */
    fun getCurrentStepDescription(): String {
        return when (_currentStep.value) {
            InitialSetupStep.WELCOME -> "Configuración inicial de tu aplicación"
            InitialSetupStep.CATEGORIES -> "Organiza tus productos por categorías"
            InitialSetupStep.PROFILE -> "Información personal básica"
            InitialSetupStep.COMPANY -> "Datos de tu negocio"
            InitialSetupStep.CUSTOMERS -> "Agrega tus primeros clientes"
            InitialSetupStep.COMPLETE -> "¡Configuración completada!"
        }
    }

    // ==================== FUNCIONES DE ACTUALIZACIÓN DE CAMPOS ====================

    /**
     * Actualizar nombre de usuario
     */
    fun updateUserName(name: String) {
        _uiState.value = _uiState.value.copy(userName = name)
    }

    /**
     * Actualizar teléfono de usuario
     */
    fun updateUserPhone(phone: String) {
        _uiState.value = _uiState.value.copy(userPhone = phone)
    }

    /**
     * Actualizar foto de perfil
     */
    fun updateProfilePhoto(photoUri: String?) {
        _uiState.value = _uiState.value.copy(profilePhotoUri = photoUri)
    }

    /**
     * Actualizar nombre de empresa
     */
    fun updateBusinessName(name: String) {
        _uiState.value = _uiState.value.copy(businessName = name)
    }

    /**
     * Actualizar RUT de empresa
     */
    fun updateBusinessRut(rut: String) {
        _uiState.value = _uiState.value.copy(businessRut = rut)
    }

    /**
     * Actualizar dirección de empresa
     */
    fun updateBusinessAddress(address: String) {
        _uiState.value = _uiState.value.copy(businessAddress = address)
    }

    /**
     * Actualizar teléfono de empresa
     */
    fun updateBusinessPhone(phone: String) {
        _uiState.value = _uiState.value.copy(businessPhone = phone)
    }

    /**
     * Actualizar email de empresa
     */
    fun updateBusinessEmail(email: String) {
        _uiState.value = _uiState.value.copy(businessEmail = email)
    }

    /**
     * Actualizar logo de empresa
     */
    fun updateBusinessLogo(logoUri: String?) {
        _uiState.value = _uiState.value.copy(businessLogoUri = logoUri)
    }

    /**
     * Actualizar tipo de negocio
     */
    fun updateBusinessType(type: BusinessType?) {
        _uiState.value = _uiState.value.copy(businessType = type)
    }

    /**
     * Agregar cliente
     */
    fun addCustomer(customer: CustomerData) {
        val currentCustomers = _uiState.value.customers.toMutableList()
        // Verificar si no excede el límite y si el cliente no está duplicado
        if (currentCustomers.size < 3 && !currentCustomers.contains(customer)) {
            currentCustomers.add(customer)
            _uiState.value = _uiState.value.copy(customers = currentCustomers)
        }
    }
    
    /**
     * Agregar múltiples clientes a la vez
     */
    fun addCustomers(customers: List<CustomerData>) {
        val currentCustomers = _uiState.value.customers.toMutableList()
        val remainingSlots = 3 - currentCustomers.size
        
        if (remainingSlots > 0) {
            val customersToAdd = customers
                .filterNot { currentCustomers.contains(it) }
                .take(remainingSlots)
            
            currentCustomers.addAll(customersToAdd)
            _uiState.value = _uiState.value.copy(customers = currentCustomers)
        }
    }

    /**
     * Eliminar cliente
     */
    fun removeCustomer(index: Int) {
        val currentCustomers = _uiState.value.customers.toMutableList()
        if (index in 0 until currentCustomers.size) {
            currentCustomers.removeAt(index)
            _uiState.value = _uiState.value.copy(customers = currentCustomers)
        }
    }

    // ==================== FUNCIONES DE GUARDADO ====================

    /**
     * Guardar todos los datos del setup inicial
     */
    suspend fun saveAllData(context: Context) = coroutineScope {
        _uiState.update { it.copy(isLoading = true, hasError = false, errorMessage = null) }

        try {
            val userId = currentUserIdFlow.value ?: throw Exception("Usuario no autenticado")
            val currentUser = authRepository.currentUser.first()
                ?: throw Exception("No se pudo obtener información del usuario")

            val profilePhotoDeferred = async { uploadProfilePhotoIfNeeded(context, userId) }
            val businessLogoDeferred = async { uploadBusinessLogoIfNeeded(context, userId) }

            val profilePhotoUrl = profilePhotoDeferred.await() ?: currentUser.profilePhotoUrl
            val businessLogoUrl = businessLogoDeferred.await() ?: currentUser.businessLogoUrl

            val updatedUser = currentUser.copy(
                name = _uiState.value.userName.takeIf { it.isNotBlank() } ?: currentUser.name,
                phone = _uiState.value.userPhone.takeIf { it.isNotBlank() } ?: currentUser.phone,
                profilePhotoUrl = profilePhotoUrl,
                businessName = _uiState.value.businessName.takeIf { it.isNotBlank() } ?: currentUser.businessName,
                businessRut = _uiState.value.businessRut.takeIf { it.isNotBlank() } ?: currentUser.businessRut,
                businessAddress = _uiState.value.businessAddress.takeIf { it.isNotBlank() } ?: currentUser.businessAddress,
                businessPhone = _uiState.value.businessPhone.takeIf { it.isNotBlank() } ?: currentUser.businessPhone,
                businessEmail = _uiState.value.businessEmail.takeIf { it.isNotBlank() } ?: currentUser.businessEmail,
                businessLogoUrl = businessLogoUrl,
                businessType = _uiState.value.businessType ?: currentUser.businessType
            )

            val updateResult = authRepository.updateProfile(updatedUser)
            if (updateResult.isSuccess) {
                saveCustomers(userId)
            } else {
                val errorMessage = updateResult.exceptionOrNull()?.message ?: "Error al guardar perfil"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasError = true,
                        errorMessage = "Error al guardar perfil: $errorMessage"
                    )
                }
            }
        } catch (e: Exception) {
            println("❌ DEBUG InitialSetupViewModel: Error en saveAllData: ${e.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasError = true,
                    errorMessage = e.message ?: "Error desconocido"
                )
            }
        }
    }

    /**
     * Guardar clientes en el repositorio
     */
    private suspend fun saveCustomers(userId: String) = coroutineScope {
        try {
            val jobs = _uiState.value.customers.map { customerData ->
                async {
                    val customer = Customer(
                        id = UUID.randomUUID().toString(),
                        name = customerData.name,
                        phone = customerData.phone,
                        email = customerData.email,
                        address = customerData.address,
                        notes = null,
                        createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        totalPurchases = 0.0,
                        lastPurchaseDate = null
                    )
                    customerRepository.addCustomer(customer)
                }
            }

            jobs.awaitAll()
            uiPreferencesStore.setInitialSetupCompletedForUser(userId, true)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasError = false,
                    errorMessage = null,
                    isSetupCompleted = true
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasError = true,
                    errorMessage = "Error al guardar clientes: ${e.message}"
                )
            }
        }
    }

    private suspend fun uploadProfilePhotoIfNeeded(context: Context, userId: String): String? {
        val uriString = _uiState.value.profilePhotoUri ?: return null
        val remotePath = "users/$userId/profile_photo_${System.currentTimeMillis()}.jpg"
        return uploadImage(context, uriString, "profile_photo", remotePath)
    }

    private suspend fun uploadBusinessLogoIfNeeded(context: Context, userId: String): String? {
        val uriString = _uiState.value.businessLogoUri ?: return null
        val remotePath = "users/$userId/business_logo_${System.currentTimeMillis()}.jpg"
        return uploadImage(context, uriString, "business_logo", remotePath)
    }

    private suspend fun uploadImage(
        context: Context,
        uriString: String,
        cachePrefix: String,
        remotePath: String
    ): String? {
        return try {
            val uri = Uri.parse(uriString)
            val localFileName = "${cachePrefix}_${System.currentTimeMillis()}.jpg"
            val localFile = imageService.saveImageLocally(context, uri, localFileName).getOrNull()
            if (localFile != null) {
                imageService.uploadImageToCloud(localFile, remotePath).getOrNull()
            } else {
                println("❌ DEBUG InitialSetupViewModel: Error guardando $cachePrefix localmente")
                null
            }
        } catch (e: Exception) {
            println("❌ DEBUG InitialSetupViewModel: Error subiendo $cachePrefix: ${e.message}")
            null
        }
    }
}

/**
 * 📊 ESTADO DE LA CONFIGURACIÓN INICIAL
 */
data class InitialSetupUiState(
    // Perfil
    val userName: String = "",
    val userPhone: String = "",
    val profilePhotoUri: String? = null,
    
    // Empresa
    val businessName: String = "",
    val businessRut: String = "",
    val businessAddress: String = "",
    val businessPhone: String = "",
    val businessEmail: String = "",
    val businessLogoUri: String? = null,
    val businessType: BusinessType? = null,
    
    // Clientes
    val customers: List<CustomerData> = emptyList(),
    
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val isSetupCompleted: Boolean = false
)

/**
 * 📋 DATOS DE CLIENTE PARA CONFIGURACIÓN INICIAL
 */
data class CustomerData(
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)


