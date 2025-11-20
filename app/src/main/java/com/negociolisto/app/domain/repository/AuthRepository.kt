package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 🔐 REPOSITORIO DE AUTENTICACIÓN
 * 
 * Define las operaciones de autenticación de usuarios.
 */
interface AuthRepository {
    
    /**
     * Estado de autenticación del usuario actual
     */
    val currentUser: Flow<User?>
    
    /**
     * Verifica si hay un usuario autenticado
     */
    val isAuthenticated: Flow<Boolean>
    
    /**
     * Registra un nuevo usuario
     */
    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String? = null,
        businessName: String? = null
    ): Result<User>
    
    /**
     * Inicia sesión con email y contraseña
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<User>
    
    /**
     * Cierra la sesión del usuario actual
     */
    suspend fun logout()
    
    /**
     * Envía email de recuperación de contraseña
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    /**
     * Actualiza el perfil del usuario
     */
    suspend fun updateProfile(user: User): Result<User>
    
    /**
     * 📧 ENVIAR VERIFICACIÓN DE EMAIL
     */
    suspend fun sendEmailVerification(): Result<Unit>
    
    /**
     * ✅ VERIFICAR EMAIL
     */
    suspend fun checkEmailVerification(): Boolean
    
    /**
     * 👥 OBTENER TOTAL DE USUARIOS
     * 
     * Obtiene el número total de usuarios registrados en el sistema.
     * Útil para verificar capacidad de Storage antes de permitir nuevos registros.
     */
    suspend fun getTotalUserCount(): Int
}