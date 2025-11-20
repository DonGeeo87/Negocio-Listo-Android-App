package com.negociolisto.app.domain.usecase.auth

import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.model.UserPreferences
import com.negociolisto.app.domain.model.BusinessType
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.util.NegocioListoError
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any

/**
 * 🧪 PRUEBAS UNITARIAS PARA LOGIN USE CASE
 * 
 * Esta clase contiene pruebas para verificar que el caso de uso de login
 * funcione correctamente en todos los escenarios posibles.
 * 
 * ¿Por qué probar los casos de uso?
 * - Verificar que la lógica de negocio sea correcta
 * - Asegurar que se manejen todos los casos de error
 * - Documentar el comportamiento esperado
 * - Facilitar refactoring sin romper funcionalidad
 * - Detectar regresiones en el futuro
 * 
 * Usamos Mockito para simular el AuthRepository y controlar
 * exactamente qué respuestas devuelve en cada prueba.
 */
class LoginUseCaseTest {
    
    // Mock del repositorio de autenticación
    @Mock
    private lateinit var authRepository: AuthRepository
    
    // El caso de uso que vamos a probar
    private lateinit var loginUseCase: LoginUseCase
    
    // Usuario de prueba que usaremos en varios tests
    private val testUser = User(
        id = "user123",
        name = "Juan Pérez",
        email = "juan@test.com",
        phone = "3001234567",
        businessName = "Tienda Juan",
        businessType = BusinessType.RETAIL,
        profilePhotoUrl = null,
        isEmailVerified = true,
        createdAt = LocalDateTime.parse("2024-01-01T10:00:00"),
        updatedAt = LocalDateTime.parse("2024-01-01T10:00:00"),
        isCloudSyncEnabled = false,
        preferences = UserPreferences()
    )
    
    /**
     * 🏗️ CONFIGURACIÓN ANTES DE CADA PRUEBA
     */
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        loginUseCase = LoginUseCase(authRepository)
    }
    
    /**
     * ✅ PRUEBA DE LOGIN EXITOSO
     */
    @Test
    fun `login with valid credentials should return success`() = runTest {
        // Arrange (Preparar): Configurar el mock para devolver éxito
        val email = "juan@test.com"
        val password = "password123"
        whenever(authRepository.login(email, password)).thenReturn(Result.success(testUser))
        
        // Act (Actuar): Ejecutar el login
        val result = loginUseCase(email, password)
        
        // Assert (Verificar): Comprobar que fue exitoso
        assertTrue("El login debería ser exitoso", result.isSuccess)
        assertEquals("Debería devolver el usuario correcto", testUser, result.getOrNull())
        verify(authRepository).login(email, password)
    }
    
    /**
     * ❌ PRUEBA DE LOGIN CON EMAIL INVÁLIDO
     */
    @Test
    fun `login with invalid email should return validation error`() = runTest {
        // Arrange: Email con formato inválido
        val invalidEmail = "email-invalido"
        val password = "password123"
        
        // Act: Intentar login con email inválido
        val result = loginUseCase(invalidEmail, password)
        
        // Assert: Debería fallar con error de validación
        assertTrue("El login debería fallar", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Debería ser error de validación", exception is NegocioListoError.ValidationError)
        
        // Verificar que no se llamó al repositorio
        verify(authRepository, org.mockito.kotlin.never()).login(any(), any())
    }
    
    /**
     * ❌ PRUEBA DE LOGIN CON CONTRASEÑA VACÍA
     */
    @Test
    fun `login with empty password should return validation error`() = runTest {
        // Arrange: Contraseña vacía
        val email = "juan@test.com"
        val password = ""
        
        // Act: Intentar login con contraseña vacía
        val result = loginUseCase(email, password)
        
        // Assert: Debería fallar con error de validación
        assertTrue("El login debería fallar", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Debería ser error de validación", exception is NegocioListoError.ValidationError)
        
        // Verificar que no se llamó al repositorio
        verify(authRepository, org.mockito.kotlin.never()).login(any(), any())
    }
    
    /**
     * ❌ PRUEBA DE LOGIN CON CREDENCIALES INCORRECTAS
     */
    @Test
    fun `login with wrong credentials should return authentication error`() = runTest {
        // Arrange: Configurar el mock para devolver error de autenticación
        val email = "juan@test.com"
        val password = "wrong-password"
        val authException = Exception("Invalid credentials")
        whenever(authRepository.login(email, password)).thenReturn(Result.failure(authException))
        
        // Act: Intentar login con credenciales incorrectas
        val result = loginUseCase(email, password)
        
        // Assert: Debería fallar con error de autenticación
        assertTrue("El login debería fallar", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Debería ser error de autenticación", exception is NegocioListoError.AuthenticationError)
        
        verify(authRepository).login(email, password)
    }
    
    /**
     * 🌐 PRUEBA DE LOGIN SIN CONEXIÓN
     */
    @Test
    fun `login without network should return network error`() = runTest {
        // Arrange: Configurar el mock para simular error de red
        val email = "juan@test.com"
        val password = "password123"
        val networkException = Exception("Network connection failed")
        whenever(authRepository.login(email, password)).thenReturn(Result.failure(networkException))
        
        // Act: Intentar login sin conexión
        val result = loginUseCase(email, password)
        
        // Assert: Debería fallar con error de red
        assertTrue("El login debería fallar", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Debería ser error de red", exception is NegocioListoError.NetworkError)
        
        verify(authRepository).login(email, password)
    }
    
    /**
     * 🧹 PRUEBA DE LIMPIEZA DE DATOS
     */
    @Test
    fun `login should clean and normalize email`() = runTest {
        // Arrange: Email con espacios y mayúsculas
        val dirtyEmail = "  JUAN@TEST.COM  "
        val cleanEmail = "juan@test.com"
        val password = "password123"
        whenever(authRepository.login(cleanEmail, password)).thenReturn(Result.success(testUser))
        
        // Act: Login con email "sucio"
        val result = loginUseCase(dirtyEmail, password)
        
        // Assert: Debería limpiar el email antes de enviarlo al repositorio
        assertTrue("El login debería ser exitoso", result.isSuccess)
        verify(authRepository).login(cleanEmail, password) // Verifica que se llamó con email limpio
    }
    
    /**
     * 🧹 PRUEBA DE LIMPIEZA DE CONTRASEÑA
     */
    @Test
    fun `login should trim password spaces`() = runTest {
        // Arrange: Contraseña con espacios
        val email = "juan@test.com"
        val dirtyPassword = "  password123  "
        val cleanPassword = "password123"
        whenever(authRepository.login(email, cleanPassword)).thenReturn(Result.success(testUser))
        
        // Act: Login con contraseña con espacios
        val result = loginUseCase(email, dirtyPassword)
        
        // Assert: Debería limpiar la contraseña
        assertTrue("El login debería ser exitoso", result.isSuccess)
        verify(authRepository).login(email, cleanPassword)
    }
    
    /**
     * 👤 PRUEBA DE USUARIO NO ENCONTRADO
     */
    @Test
    fun `login with non-existent user should return validation error`() = runTest {
        // Arrange: Simular usuario no encontrado
        val email = "noexiste@test.com"
        val password = "password123"
        val userNotFoundException = Exception("User not found")
        whenever(authRepository.login(email, password)).thenReturn(Result.failure(userNotFoundException))
        
        // Act: Intentar login con usuario inexistente
        val result = loginUseCase(email, password)
        
        // Assert: Debería fallar con error de validación
        assertTrue("El login debería fallar", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Debería ser error de validación", exception is NegocioListoError.ValidationError)
        
        val validationError = exception as NegocioListoError.ValidationError
        assertTrue("El mensaje debería mencionar que no existe la cuenta", 
            validationError.message.contains("No existe una cuenta"))
        
        verify(authRepository).login(email, password)
    }
    
    /**
     * 🚫 PRUEBA DE CUENTA DESHABILITADA
     */
    @Test
    fun `login with disabled account should return business rule error`() = runTest {
        // Arrange: Simular cuenta deshabilitada
        val email = "deshabilitado@test.com"
        val password = "password123"
        val disabledException = Exception("Account is disabled")
        whenever(authRepository.login(email, password)).thenReturn(Result.failure(disabledException))
        
        // Act: Intentar login con cuenta deshabilitada
        val result = loginUseCase(email, password)
        
        // Assert: Debería fallar con error de regla de negocio
        assertTrue("El login debería fallar", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Debería ser error de regla de negocio", exception is NegocioListoError.BusinessRuleError)
        
        val businessError = exception as NegocioListoError.BusinessRuleError
        assertTrue("El mensaje debería mencionar que está deshabilitada", 
            businessError.message.contains("deshabilitada"))
        
        verify(authRepository).login(email, password)
    }
    
    /**
     * 📧 PRUEBA DE EMAIL NO VERIFICADO
     */
    @Test
    fun `login with unverified email should return business rule error`() = runTest {
        // Arrange: Simular email no verificado
        val email = "noverificado@test.com"
        val password = "password123"
        val unverifiedException = Exception("Email not verified")
        whenever(authRepository.login(email, password)).thenReturn(Result.failure(unverifiedException))
        
        // Act: Intentar login con email no verificado
        val result = loginUseCase(email, password)
        
        // Assert: Debería fallar con error de regla de negocio
        assertTrue("El login debería fallar", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Debería ser error de regla de negocio", exception is NegocioListoError.BusinessRuleError)
        
        val businessError = exception as NegocioListoError.BusinessRuleError
        assertTrue("El mensaje debería mencionar verificación de email", 
            businessError.message.contains("verificar tu email"))
        
        verify(authRepository).login(email, password)
    }
    
    /**
     * ❓ PRUEBA DE ERROR DESCONOCIDO
     */
    @Test
    fun `login with unknown error should return unknown error`() = runTest {
        // Arrange: Simular error desconocido
        val email = "juan@test.com"
        val password = "password123"
        val unknownException = Exception("Something weird happened")
        whenever(authRepository.login(email, password)).thenReturn(Result.failure(unknownException))
        
        // Act: Login con error desconocido
        val result = loginUseCase(email, password)
        
        // Assert: Debería fallar con error desconocido
        assertTrue("El login debería fallar", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Debería ser error desconocido", exception is NegocioListoError.UnknownError)
        
        verify(authRepository).login(email, password)
    }
    
    /**
     * 🔄 PRUEBA DE EXCEPCIÓN INESPERADA
     */
    @Test
    fun `login with unexpected exception should return unknown error`() = runTest {
        // Arrange: Configurar el mock para lanzar excepción
        val email = "juan@test.com"
        val password = "password123"
        whenever(authRepository.login(email, password)).thenThrow(RuntimeException("Unexpected error"))
        
        // Act: Login que lanza excepción inesperada
        val result = loginUseCase(email, password)
        
        // Assert: Debería capturar la excepción y devolver error desconocido
        assertTrue("El login debería fallar", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Debería ser error desconocido", exception is NegocioListoError.UnknownError)
        
        verify(authRepository).login(email, password)
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES DE TESTING:
 * 
 * 1. Unit Testing: Pruebas aisladas de una unidad de código
 * 2. Mocking: Simular dependencias para controlar el comportamiento
 * 3. Test Cases: Casos específicos que queremos verificar
 * 4. Arrange-Act-Assert: Patrón para organizar pruebas
 * 5. Edge Cases: Casos límite o de error
 * 6. Verification: Verificar que se llamaron los métodos correctos
 * 
 * TIPOS DE PRUEBAS INCLUIDAS:
 * 
 * 1. **Happy Path**: Casos donde todo funciona correctamente
 *    - Login exitoso con credenciales válidas
 *    - Limpieza automática de datos de entrada
 * 
 * 2. **Validation Errors**: Casos de datos inválidos
 *    - Email con formato incorrecto
 *    - Contraseña vacía o muy corta
 * 
 * 3. **Authentication Errors**: Casos de autenticación fallida
 *    - Credenciales incorrectas
 *    - Usuario no encontrado
 *    - Cuenta deshabilitada
 *    - Email no verificado
 * 
 * 4. **Network Errors**: Casos de problemas de conectividad
 *    - Sin conexión a internet
 *    - Timeout de servidor
 * 
 * 5. **Edge Cases**: Casos especiales
 *    - Errores inesperados
 *    - Excepciones no manejadas
 * 
 * BENEFICIOS DE ESTAS PRUEBAS:
 * - Detectan errores antes de que lleguen a producción
 * - Documentan todos los casos de uso posibles
 * - Facilitan refactoring sin miedo a romper funcionalidad
 * - Verifican que el mapeo de errores funcione correctamente
 * - Aseguran que la validación de datos sea robusta
 * 
 * MOCKITO PATTERNS USADOS:
 * - `whenever().thenReturn()`: Configurar respuesta del mock
 * - `whenever().thenThrow()`: Configurar excepción del mock
 * - `verify()`: Verificar que se llamó un método
 * - `never()`: Verificar que NO se llamó un método
 * - `any()`: Matcher para cualquier parámetro
 */