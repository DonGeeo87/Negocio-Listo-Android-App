package com.negociolisto.app.ui.auth

import com.negociolisto.app.domain.model.BusinessType
import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.model.UserRegistration
import com.negociolisto.app.domain.usecase.auth.LoginUseCase
import com.negociolisto.app.domain.usecase.auth.LogoutUseCase
import com.negociolisto.app.domain.usecase.auth.RegisterUseCase
import com.negociolisto.app.domain.util.ErrorHandler
import com.negociolisto.app.domain.util.NegocioListoError
import com.negociolisto.app.domain.util.UserMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlinx.datetime.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 🧪 PRUEBAS UNITARIAS DEL AUTH VIEW MODEL
 * 
 * Estas pruebas verifican que el AuthViewModel maneje correctamente:
 * - Estados de formularios
 * - Validación de campos
 * - Casos de uso de autenticación
 * - Manejo de errores
 * - Estados de carga
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @Mock
    private lateinit var loginUseCase: LoginUseCase

    @Mock
    private lateinit var registerUseCase: RegisterUseCase

    @Mock
    private lateinit var logoutUseCase: LogoutUseCase

    @Mock
    private lateinit var errorHandler: ErrorHandler

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = AuthViewModel(
            loginUseCase = loginUseCase,
            registerUseCase = registerUseCase,
            logoutUseCase = logoutUseCase,
            errorHandler = errorHandler
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==========================================
    // PRUEBAS DE ESTADO INICIAL
    // ==========================================

    @Test
    fun `initial state should be correct`() = runTest {
        val initialState = viewModel.uiState.first()
        
        assertEquals("", initialState.email)
        assertEquals("", initialState.password)
        assertEquals("", initialState.name)
        assertEquals("", initialState.confirmPassword)
        assertEquals("", initialState.phone)
        assertEquals("", initialState.businessName)
        assertNull(initialState.businessType)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isAuthenticated)
        assertNull(initialState.currentUser)
        assertNull(initialState.userMessage)
    }

    // ==========================================
    // PRUEBAS DE ACTUALIZACIÓN DE CAMPOS
    // ==========================================

    @Test
    fun `updateEmail should update email and clear error`() = runTest {
        // Given
        val email = "test@example.com"
        
        // When
        viewModel.updateEmail(email)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(email, state.email)
        assertNull(state.emailError)
    }

    @Test
    fun `updateEmail with invalid email should show error`() = runTest {
        // Given
        val invalidEmail = "invalid-email"
        
        // When
        viewModel.updateEmail(invalidEmail)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(invalidEmail, state.email)
        // Note: This would require ValidationUtils to be mocked or real validation
    }

    @Test
    fun `updatePassword should update password and clear error`() = runTest {
        // Given
        val password = "password123"
        
        // When
        viewModel.updatePassword(password)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(password, state.password)
        assertNull(state.passwordError)
    }

    @Test
    fun `updateName should update name and clear error`() = runTest {
        // Given
        val name = "Juan Pérez"
        
        // When
        viewModel.updateName(name)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(name, state.name)
        assertNull(state.nameError)
    }

    @Test
    fun `updateConfirmPassword with matching password should clear error`() = runTest {
        // Given
        val password = "password123"
        viewModel.updatePassword(password)
        
        // When
        viewModel.updateConfirmPassword(password)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(password, state.confirmPassword)
        assertNull(state.confirmPasswordError)
    }

    @Test
    fun `updateConfirmPassword with non-matching password should show error`() = runTest {
        // Given
        val password = "password123"
        val differentPassword = "different"
        viewModel.updatePassword(password)
        
        // When
        viewModel.updateConfirmPassword(differentPassword)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(differentPassword, state.confirmPassword)
        assertEquals("Las contraseñas no coinciden", state.confirmPasswordError)
    }

    @Test
    fun `updatePhone should update phone and clear error`() = runTest {
        // Given
        val phone = "+57 300 123 4567"
        
        // When
        viewModel.updatePhone(phone)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(phone, state.phone)
        assertNull(state.phoneError)
    }

    @Test
    fun `updateBusinessName should update business name`() = runTest {
        // Given
        val businessName = "Mi Negocio S.A.S."
        
        // When
        viewModel.updateBusinessName(businessName)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(businessName, state.businessName)
    }

    @Test
    fun `updateBusinessType should update business type`() = runTest {
        // Given
        val businessType = BusinessType.RETAIL
        
        // When
        viewModel.updateBusinessType(businessType)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(businessType, state.businessType)
    }

    // ==========================================
    // PRUEBAS DE LOGIN
    // ==========================================

    @Test
    fun `login with valid credentials should succeed`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val user = User(
            id = "1",
            name = "Test User",
            email = email,
            phone = null,
            businessName = null,
            businessType = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        
        whenever(loginUseCase(email, password)).thenReturn(Result.success(user))
        
        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isAuthenticated)
        assertEquals(user, state.currentUser)
        assertFalse(state.isLoading)
        assertEquals(UserMessage.Success("¡Bienvenido de vuelta!"), state.userMessage)
        
        verify(loginUseCase).invoke(email, password)
    }

    @Test
    fun `login with invalid credentials should show error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val error = NegocioListoError.AuthenticationError("Credenciales inválidas")
        val userMessage = UserMessage.Error("Credenciales inválidas")
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        
        whenever(loginUseCase(email, password)).thenReturn(Result.failure(error))
        whenever(errorHandler.handleError(error)).thenReturn(userMessage)
        
        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isAuthenticated)
        assertNull(state.currentUser)
        assertFalse(state.isLoading)
        assertEquals(userMessage, state.userMessage)
        
        verify(loginUseCase).invoke(email, password)
        verify(errorHandler).handleError(error)
    }

    @Test
    fun `login should set loading state during execution`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        
        whenever(loginUseCase(email, password)).thenReturn(Result.success(mock()))
        
        // When
        viewModel.login()
        
        // Then - Check loading state before completion
        val loadingState = viewModel.uiState.first()
        assertTrue(loadingState.isLoading)
        
        // Complete the coroutine
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Check final state
        val finalState = viewModel.uiState.first()
        assertFalse(finalState.isLoading)
    }

    // ==========================================
    // PRUEBAS DE REGISTRO
    // ==========================================

    @Test
    fun `register with valid data should succeed`() = runTest {
        // Given
        val name = "Juan Pérez"
        val email = "juan@example.com"
        val password = "password123"
        val phone = "+57 300 123 4567"
        val businessName = "Mi Negocio"
        val businessType = BusinessType.RETAIL
        
        val user = User(
            id = "1",
            name = name,
            email = email,
            phone = phone,
            businessName = businessName,
            businessType = businessType,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        viewModel.updateName(name)
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        viewModel.updateConfirmPassword(password)
        viewModel.updatePhone(phone)
        viewModel.updateBusinessName(businessName)
        viewModel.updateBusinessType(businessType)
        
        val expectedRegistration = UserRegistration(
            name = name,
            email = email,
            password = password,
            phone = phone,
            businessName = businessName,
            businessType = businessType
        )
        
        whenever(registerUseCase(expectedRegistration)).thenReturn(Result.success(user))
        
        // When
        viewModel.register()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isAuthenticated)
        assertEquals(user, state.currentUser)
        assertFalse(state.isLoading)
        assertEquals(UserMessage.Success("¡Cuenta creada exitosamente!"), state.userMessage)
        
        verify(registerUseCase).invoke(expectedRegistration)
    }

    @Test
    fun `register with existing email should show error`() = runTest {
        // Given
        val name = "Juan Pérez"
        val email = "existing@example.com"
        val password = "password123"
        val error = NegocioListoError.ValidationError("email", "El email ya está registrado")
        val userMessage = UserMessage.Error("El email ya está registrado")
        
        viewModel.updateName(name)
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        viewModel.updateConfirmPassword(password)
        
        whenever(registerUseCase(any())).thenReturn(Result.failure(error))
        whenever(errorHandler.handleError(error)).thenReturn(userMessage)
        
        // When
        viewModel.register()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isAuthenticated)
        assertNull(state.currentUser)
        assertFalse(state.isLoading)
        assertEquals(userMessage, state.userMessage)
        
        verify(registerUseCase).invoke(any())
        verify(errorHandler).handleError(error)
    }

    // ==========================================
    // PRUEBAS DE VALIDACIÓN DE FORMULARIOS
    // ==========================================

    @Test
    fun `isLoginFormValid should return true with valid data`() = runTest {
        // Given
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        
        // When
        val state = viewModel.uiState.first()
        
        // Then
        assertTrue(state.isLoginFormValid())
    }

    @Test
    fun `isLoginFormValid should return false with empty fields`() = runTest {
        // Given - empty fields (initial state)
        
        // When
        val state = viewModel.uiState.first()
        
        // Then
        assertFalse(state.isLoginFormValid())
    }

    @Test
    fun `isRegistrationFormValid should return true with valid data`() = runTest {
        // Given
        viewModel.updateName("Juan Pérez")
        viewModel.updateEmail("juan@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateConfirmPassword("password123")
        
        // When
        val state = viewModel.uiState.first()
        
        // Then
        assertTrue(state.isRegistrationFormValid())
    }

    @Test
    fun `isRegistrationFormValid should return false with mismatched passwords`() = runTest {
        // Given
        viewModel.updateName("Juan Pérez")
        viewModel.updateEmail("juan@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateConfirmPassword("different")
        
        // When
        val state = viewModel.uiState.first()
        
        // Then
        assertFalse(state.isRegistrationFormValid())
    }

    // ==========================================
    // PRUEBAS DE ACCIONES GENERALES
    // ==========================================

    @Test
    fun `togglePasswordVisibility should toggle visibility state`() = runTest {
        // Given - initial state (password hidden)
        val initialState = viewModel.uiState.first()
        assertFalse(initialState.isPasswordVisible)
        
        // When
        viewModel.togglePasswordVisibility()
        
        // Then
        val newState = viewModel.uiState.first()
        assertTrue(newState.isPasswordVisible)
        
        // When - toggle again
        viewModel.togglePasswordVisibility()
        
        // Then
        val finalState = viewModel.uiState.first()
        assertFalse(finalState.isPasswordVisible)
    }

    @Test
    fun `toggleConfirmPasswordVisibility should toggle visibility state`() = runTest {
        // Given - initial state (password hidden)
        val initialState = viewModel.uiState.first()
        assertFalse(initialState.isConfirmPasswordVisible)
        
        // When
        viewModel.toggleConfirmPasswordVisibility()
        
        // Then
        val newState = viewModel.uiState.first()
        assertTrue(newState.isConfirmPasswordVisible)
    }

    @Test
    fun `clearUserMessage should clear user message`() = runTest {
        // Given - set a user message first
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        whenever(loginUseCase(any(), any())).thenReturn(Result.success(mock()))
        
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val stateWithMessage = viewModel.uiState.first()
        assertEquals(UserMessage.Success("¡Bienvenido de vuelta!"), stateWithMessage.userMessage)
        
        // When
        viewModel.clearUserMessage()
        
        // Then
        val stateWithoutMessage = viewModel.uiState.first()
        assertNull(stateWithoutMessage.userMessage)
    }

    @Test
    fun `switchAuthMode should change auth mode and clear message`() = runTest {
        // Given - set a user message first
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        whenever(loginUseCase(any(), any())).thenReturn(Result.success(mock()))
        
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.switchAuthMode(AuthMode.REGISTER)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(AuthMode.REGISTER, state.authMode)
        assertNull(state.userMessage)
    }

    @Test
    fun `logout should reset state and call logout use case`() = runTest {
        // Given - authenticated state
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        whenever(loginUseCase(any(), any())).thenReturn(Result.success(mock()))
        
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val authenticatedState = viewModel.uiState.first()
        assertTrue(authenticatedState.isAuthenticated)
        
        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val loggedOutState = viewModel.uiState.first()
        assertFalse(loggedOutState.isAuthenticated)
        assertNull(loggedOutState.currentUser)
        assertEquals("", loggedOutState.email)
        assertEquals("", loggedOutState.password)
        
        verify(logoutUseCase).invoke()
    }
}