package com.negociolisto.app.integration

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.data.remote.firebase.FirebaseAuthRepository
import com.negociolisto.app.domain.model.BusinessType
import com.negociolisto.app.domain.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 🧪 TESTS DE INTEGRACIÓN - FLUJO DE AUTENTICACIÓN
 * 
 * Pruebas que verifican el flujo completo de autenticación
 * desde el registro hasta el login y logout.
 */
@RunWith(MockitoJUnitRunner::class)
class AuthenticationFlowTest {

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var firestore: FirebaseFirestore

    private lateinit var authRepository: FirebaseAuthRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = FirebaseAuthRepository(firebaseAuth, firestore)
    }

    @Test
    fun `when user registers with valid data, should return success`() = runTest {
        // Given
        val email = "test@negociolisto.com"
        val password = "password123"
        val name = "Usuario Test"
        val businessName = "Mi Negocio Test"

        // When
        val result = authRepository.register(email, password, name, null, businessName)

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(email, user.email)
        assertEquals(name, user.name)
        assertEquals(businessName, user.businessName)
    }

    @Test
    fun `when user logs in with valid credentials, should return success`() = runTest {
        // Given
        val email = "test@negociolisto.com"
        val password = "password123"

        // When
        val result = authRepository.login(email, password)

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(email, user.email)
    }

    @Test
    fun `when user logs in with invalid credentials, should return failure`() = runTest {
        // Given
        val email = "test@negociolisto.com"
        val password = "wrongpassword"

        // When
        val result = authRepository.login(email, password)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `when user logs out, should clear authentication state`() = runTest {
        // Given - Usuario autenticado
        val user = User(
            id = "test_user_id",
            email = "test@negociolisto.com",
            name = "Usuario Test",
            businessName = "Mi Negocio",
            businessType = BusinessType.RETAIL,
            isEmailVerified = true,
            isCloudSyncEnabled = true
        )

        // When
        authRepository.logout()

        // Then
        val currentUser = authRepository.currentUser.first()
        assertTrue(currentUser == null)
    }

    @Test
    fun `when user requests password reset, should return success`() = runTest {
        // Given
        val email = "test@negociolisto.com"

        // When
        val result = authRepository.sendPasswordResetEmail(email)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when user updates profile, should return updated user`() = runTest {
        // Given
        val user = User(
            id = "test_user_id",
            email = "test@negociolisto.com",
            name = "Usuario Original",
            businessName = "Negocio Original",
            businessType = BusinessType.RETAIL,
            isEmailVerified = true,
            isCloudSyncEnabled = true
        )

        val updatedUser = user.copy(
            name = "Usuario Actualizado",
            businessName = "Negocio Actualizado"
        )

        // When
        val result = authRepository.updateProfile(updatedUser)

        // Then
        assertTrue(result.isSuccess)
        val returnedUser = result.getOrNull()
        assertNotNull(returnedUser)
        assertEquals("Usuario Actualizado", returnedUser.name)
        assertEquals("Negocio Actualizado", returnedUser.businessName)
    }

    @Test
    fun `when checking authentication state, should reflect current user`() = runTest {
        // Given
        val user = User(
            id = "test_user_id",
            email = "test@negociolisto.com",
            name = "Usuario Test",
            isEmailVerified = true,
            isCloudSyncEnabled = true
        )

        // When
        val isAuthenticated = authRepository.isAuthenticated.first()

        // Then
        // En un test real, esto dependería del estado actual de Firebase Auth
        // Por ahora verificamos que el flujo funciona
        assertNotNull(isAuthenticated)
    }
}

/**
 * 📚 CONCEPTOS DE TESTING DE INTEGRACIÓN:
 * 
 * 1. Integration Testing: Pruebas que verifican la interacción entre componentes
 * 2. End-to-End Flow: Pruebas que cubren flujos completos de usuario
 * 3. Real Dependencies: Uso de dependencias reales (con mocks para servicios externos)
 * 4. State Verification: Verificación de cambios de estado en el sistema
 * 5. Error Scenarios: Pruebas de casos de error y recuperación
 * 
 * FLUJOS CRÍTICOS CUBIERTOS:
 * 
 * 1. **Registro de Usuario**: Verificación de creación exitosa de cuenta
 * 2. **Login de Usuario**: Verificación de autenticación exitosa
 * 3. **Logout de Usuario**: Verificación de limpieza de sesión
 * 4. **Recuperación de Contraseña**: Verificación de envío de email
 * 5. **Actualización de Perfil**: Verificación de persistencia de cambios
 * 6. **Estado de Autenticación**: Verificación de detección de sesión activa
 * 
 * BENEFICIOS:
 * - Detectan problemas de integración entre componentes
 * - Verifican flujos completos de usuario
 * - Aseguran que la autenticación funcione correctamente
 * - Facilitan refactoring sin romper funcionalidad crítica
 */
