package com.negociolisto.app.data.repository

import com.negociolisto.app.data.local.dao.UserDao
import com.negociolisto.app.data.local.entity.UserEntity
import com.negociolisto.app.data.local.preferences.SecureSessionStorage
import com.negociolisto.app.domain.model.UserRegistration
import com.negociolisto.app.domain.model.BusinessType
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * 🧪 PRUEBAS DE INTEGRACIÓN PARA AUTH REPOSITORY
 * 
 * Esta clase contiene pruebas para verificar que la implementación
 * del repositorio de autenticación funcione correctamente con sus
 * dependencias (UserDao y SecureSessionStorage).
 * 
 * Son pruebas de "integración" porque prueban cómo trabajan juntos
 * múltiples componentes, no solo uno aislado.
 */
class AuthRepositoryImplTest {
    
    @Mock
    private lateinit var userDao: UserDao
    
    @Mock
    private lateinit var sessionStorage: SecureSessionStorage
    
    private lateinit var authRepository: AuthRepositoryImpl
    
    // Usuario de prueba
    private val testUserEntity = UserEntity(
        id = "user123",
        name = "Juan Pérez",
        email = "juan@test.com",
        phone = "3001234567",
        businessName = "Tienda Juan",
        businessType = "RETAIL",
        profilePhotoUrl = null,
        isEmailVerified = true,
        isGuest = false,
        createdAt = "2024-01-01T10:00:00",
        updatedAt = "2024-01-01T10:00:00",
        isCloudSyncEnabled = false,
        prefDarkTheme = false,
        prefNotifications = true,
        prefCurrency = "COP",
        prefLanguage = "es",
        prefAdvancedDashboard = false,
        prefAutoSync = true,
        prefLowStockAlerts = true,
        prefBiometricLogin = false
    )
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepositoryImpl(userDao, sessionStorage)
    }
    
    /**
     * ✅ PRUEBA DE LOGIN EXITOSO
     */
    @Test
    fun `login with valid credentials should return success and save session`() = runTest {
        // Arrange
        val email = "juan@test.com"
        val password = "password123"
        
        whenever(userDao.getUserByEmail(email)).thenReturn(testUserEntity)
        
        // Act
        val result = authRepository.login(email, password)
        
        // Assert
        assertTrue("Login should be successful", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull("User should not be null", user)
        assertEquals("Email should match", email, user?.email)
        assertEquals("Name should match", "Juan Pérez", user?.name)
        
        // Verify that session was saved
        verify(sessionStorage).saveSession(any())
        verify(userDao).getUserByEmail(email)
    }
    
    /**
     * ❌ PRUEBA DE LOGIN CON USUARIO NO ENCONTRADO
     */
    @Test
    fun `login with non-existent user should return failure`() = runTest {
        // Arrange
        val email = "noexiste@test.com"
        val password = "password123"
        
        whenever(userDao.getUserByEmail(email)).thenReturn(null)
        
        // Act
        val result = authRepository.login(email, password)
        
        // Assert
        assertTrue("Login should fail", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Should contain 'User not found'", 
            exception?.message?.contains("User not found") == true)
        
        // Verify that session was not saved
        verify(sessionStorage, never()).saveSession(any())
        verify(userDao).getUserByEmail(email)
    }
    
    /**
     * ✅ PRUEBA DE REGISTRO EXITOSO
     */
    @Test
    fun `register with valid data should create user and save session`() = runTest {
        // Arrange
        val userRegistration = UserRegistration(
            name = "María García",
            email = "maria@test.com",
            password = "password123",
            phone = "3009876543",
            businessName = "Restaurante María",
            businessType = BusinessType.RESTAURANT
        )
        
        whenever(userDao.emailExists(userRegistration.email)).thenReturn(false)
        
        // Act
        val result = authRepository.register(userRegistration)
        
        // Assert
        assertTrue("Registration should be successful", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull("User should not be null", user)
        assertEquals("Email should match", userRegistration.email, user?.email)
        assertEquals("Name should match", userRegistration.name, user?.name)
        assertEquals("Business name should match", userRegistration.businessName, user?.businessName)
        assertEquals("Business type should match", userRegistration.businessType, user?.businessType)
        assertFalse("Email should not be verified initially", user?.isEmailVerified == true)
        
        // Verify that user was inserted and session was saved
        verify(userDao).insertUser(any())
        verify(sessionStorage).saveSession(any())
        verify(userDao).emailExists(userRegistration.email)
    }
    
    /**
     * ❌ PRUEBA DE REGISTRO CON EMAIL DUPLICADO
     */
    @Test
    fun `register with existing email should return failure`() = runTest {
        // Arrange
        val userRegistration = UserRegistration(
            name = "Pedro López",
            email = "juan@test.com", // Email que ya existe
            password = "password123"
        )
        
        whenever(userDao.emailExists(userRegistration.email)).thenReturn(true)
        
        // Act
        val result = authRepository.register(userRegistration)
        
        // Assert
        assertTrue("Registration should fail", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Should contain 'Email already exists'", 
            exception?.message?.contains("Email already exists") == true)
        
        // Verify that user was not inserted
        verify(userDao, never()).insertUser(any())
        verify(sessionStorage, never()).saveSession(any())
        verify(userDao).emailExists(userRegistration.email)
    }
    
    /**
     * ✅ PRUEBA DE LOGOUT
     */
    @Test
    fun `logout should clear session`() = runTest {
        // Act
        val result = authRepository.logout()
        
        // Assert
        assertTrue("Logout should be successful", result.isSuccess)
        
        // Verify that session was cleared
        verify(sessionStorage).clearSession()
    }
    
    /**
     * ✅ PRUEBA DE OBTENER USUARIO ACTUAL
     */
    @Test
    fun `getCurrentUser should return user when session exists`() = runTest {
        // Arrange
        val userId = "user123"
        whenever(sessionStorage.getCurrentUserId()).thenReturn(userId)
        whenever(userDao.getUserById(userId)).thenReturn(testUserEntity)
        
        // Act
        val user = authRepository.getCurrentUser()
        
        // Assert
        assertNotNull("User should not be null", user)
        assertEquals("User ID should match", userId, user?.id)
        assertEquals("Email should match", testUserEntity.email, user?.email)
        
        verify(sessionStorage).getCurrentUserId()
        verify(userDao).getUserById(userId)
    }
    
    /**
     * ❌ PRUEBA DE OBTENER USUARIO ACTUAL SIN SESIÓN
     */
    @Test
    fun `getCurrentUser should return null when no session exists`() = runTest {
        // Arrange
        whenever(sessionStorage.getCurrentUserId()).thenReturn(null)
        
        // Act
        val user = authRepository.getCurrentUser()
        
        // Assert
        assertNull("User should be null", user)
        
        verify(sessionStorage).getCurrentUserId()
        verify(userDao, never()).getUserById(any())
    }
    
    /**
     * ✅ PRUEBA DE VERIFICAR SESIÓN ACTIVA
     */
    @Test
    fun `isUserLoggedIn should return true when session and user exist`() = runTest {
        // Arrange
        whenever(sessionStorage.hasActiveSession()).thenReturn(true)
        whenever(userDao.hasLoggedInUser()).thenReturn(true)
        
        // Act
        val isLoggedIn = authRepository.isUserLoggedIn()
        
        // Assert
        assertTrue("Should be logged in", isLoggedIn)
        
        verify(sessionStorage).hasActiveSession()
        verify(userDao).hasLoggedInUser()
    }
    
    /**
     * ❌ PRUEBA DE VERIFICAR SESIÓN ACTIVA SIN USUARIO
     */
    @Test
    fun `isUserLoggedIn should return false when no user exists`() = runTest {
        // Arrange
        whenever(sessionStorage.hasActiveSession()).thenReturn(true)
        whenever(userDao.hasLoggedInUser()).thenReturn(false)
        
        // Act
        val isLoggedIn = authRepository.isUserLoggedIn()
        
        // Assert
        assertFalse("Should not be logged in", isLoggedIn)
        
        verify(sessionStorage).hasActiveSession()
        verify(userDao).hasLoggedInUser()
    }
    
    /**
     * ✅ PRUEBA DE MODO INVITADO
     */
    @Test
    fun `startGuestMode should create guest user and session`() = runTest {
        // Act
        val result = authRepository.startGuestMode()
        
        // Assert
        assertTrue("Guest mode should start successfully", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull("User should not be null", user)
        assertEquals("Name should be guest", "Usuario Invitado", user?.name)
        assertTrue("Email should be guest email", user?.email?.contains("guest") == true)
        
        // Verify that guest user was inserted and session was saved
        verify(userDao).insertUser(argThat { isGuest })
        verify(sessionStorage).saveSession(any())
    }
    
    /**
     * ✅ PRUEBA DE MIGRACIÓN DE USUARIO INVITADO
     */
    @Test
    fun `migrateGuestData should convert guest to regular user`() = runTest {
        // Arrange
        val guestUserId = "guest123"
        val guestUserEntity = testUserEntity.copy(
            id = guestUserId,
            isGuest = true,
            email = "guest@temp.local"
        )
        
        val userRegistration = UserRegistration(
            name = "Carlos Migrado",
            email = "carlos@test.com",
            password = "password123"
        )
        
        whenever(sessionStorage.getCurrentUserId()).thenReturn(guestUserId)
        whenever(userDao.getUserById(guestUserId)).thenReturn(guestUserEntity)
        whenever(userDao.emailExists(userRegistration.email)).thenReturn(false)
        
        val migratedUserEntity = guestUserEntity.copy(
            isGuest = false,
            email = userRegistration.email,
            name = userRegistration.name
        )
        whenever(userDao.getUserById(guestUserId)).thenReturn(migratedUserEntity)
        
        // Act
        val result = authRepository.migrateGuestData(userRegistration)
        
        // Assert
        assertTrue("Migration should be successful", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull("User should not be null", user)
        assertEquals("Email should be updated", userRegistration.email, user?.email)
        assertEquals("Name should be updated", userRegistration.name, user?.name)
        
        // Verify migration operations
        verify(userDao).migrateGuestToUser(
            userId = guestUserId,
            email = userRegistration.email,
            name = userRegistration.name,
            isEmailVerified = false,
            updatedAt = any()
        )
        verify(sessionStorage).saveSession(any())
    }
    
    /**
     * ✅ PRUEBA DE VERIFICAR USUARIO INVITADO
     */
    @Test
    fun `isGuestUser should return true for guest user`() = runTest {
        // Arrange
        val guestUserId = "guest123"
        val guestUserEntity = testUserEntity.copy(id = guestUserId, isGuest = true)
        
        whenever(sessionStorage.getCurrentUserId()).thenReturn(guestUserId)
        whenever(userDao.getUserById(guestUserId)).thenReturn(guestUserEntity)
        
        // Act
        val isGuest = authRepository.isGuestUser()
        
        // Assert
        assertTrue("Should be guest user", isGuest)
        
        verify(sessionStorage).getCurrentUserId()
        verify(userDao).getUserById(guestUserId)
    }
    
    /**
     * ✅ PRUEBA DE ACTUALIZAR PERFIL
     */
    @Test
    fun `updateUserProfile should update user data`() = runTest {
        // Arrange
        val userId = "user123"
        val updatedUser = testUserEntity.toDomainModel().copy(
            name = "Juan Carlos Pérez",
            phone = "3001111111",
            businessName = "Nueva Tienda Juan"
        )
        
        whenever(sessionStorage.getCurrentUserId()).thenReturn(userId)
        whenever(userDao.getUserById(userId)).thenReturn(
            testUserEntity.copy(
                name = updatedUser.name,
                phone = updatedUser.phone,
                businessName = updatedUser.businessName
            )
        )
        
        // Act
        val result = authRepository.updateUserProfile(updatedUser)
        
        // Assert
        assertTrue("Update should be successful", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull("User should not be null", user)
        assertEquals("Name should be updated", updatedUser.name, user?.name)
        assertEquals("Phone should be updated", updatedUser.phone, user?.phone)
        assertEquals("Business name should be updated", updatedUser.businessName, user?.businessName)
        
        // Verify update operation
        verify(userDao).updateUserProfile(
            userId = userId,
            name = updatedUser.name,
            phone = updatedUser.phone,
            businessName = updatedUser.businessName,
            businessType = updatedUser.businessType?.name,
            updatedAt = any()
        )
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES DE TESTING DE REPOSITORIOS:
 * 
 * 1. Integration Testing: Pruebas que verifican la interacción entre componentes
 * 2. Mock Coordination: Coordinación de múltiples mocks para simular escenarios
 * 3. State Verification: Verificación de que el estado se actualiza correctamente
 * 4. Side Effect Testing: Verificación de efectos secundarios (guardar sesión, etc.)
 * 5. Error Scenario Testing: Pruebas de casos de error y manejo de excepciones
 * 6. Data Transformation Testing: Verificación de conversiones entre capas
 * 
 * TIPOS DE PRUEBAS INCLUIDAS:
 * 
 * 1. **Happy Path**: Casos donde todo funciona correctamente
 *    - Login exitoso con credenciales válidas
 *    - Registro exitoso con datos válidos
 *    - Migración exitosa de usuario invitado
 * 
 * 2. **Error Cases**: Casos donde esperamos errores
 *    - Login con usuario inexistente
 *    - Registro con email duplicado
 *    - Operaciones sin sesión activa
 * 
 * 3. **State Management**: Verificación de gestión de estado
 *    - Creación y limpieza de sesiones
 *    - Verificación de estado de login
 *    - Transiciones entre estados (invitado → usuario)
 * 
 * 4. **Data Flow**: Verificación del flujo de datos
 *    - Conversión entre entidades y modelos de dominio
 *    - Persistencia de datos en múltiples capas
 *    - Coordinación entre DAO y almacenamiento seguro
 * 
 * BENEFICIOS DE ESTAS PRUEBAS:
 * - Verifican que los componentes trabajen bien juntos
 * - Detectan problemas de integración antes de producción
 * - Documentan el comportamiento esperado del repositorio
 * - Facilitan refactoring sin miedo a romper funcionalidad
 * - Aseguran que el manejo de sesiones sea robusto
 */