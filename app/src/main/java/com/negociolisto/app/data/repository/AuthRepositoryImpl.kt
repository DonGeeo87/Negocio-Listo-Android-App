package com.negociolisto.app.data.repository

import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.negociolisto.app.domain.model.UserPreferences
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔐 IMPLEMENTACIÓN DEL REPOSITORIO DE AUTENTICACIÓN
 * 
 * Por ahora es una implementación mock para desarrollo.
 * TODO: Integrar con Firebase Auth cuando esté listo.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
    
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()
    
    init {
        // Cargar usuario guardado al inicializar
        _currentUser.value = loadUserFromPrefs()
    }
    
    private fun loadUserFromPrefs(): User? {
        return try {
            // Verificar si hay un usuario guardado
            val name = prefs.getString("user_name", "") ?: ""
            val email = prefs.getString("user_email", "") ?: ""
            val userId = prefs.getString("user_id", "") ?: ""
            
            // Solo cargar usuario si tiene ID válido (indica autenticación real)
            if (name.isNotEmpty() && email.isNotEmpty() && userId.isNotEmpty()) {
                println("🔄 DEBUG: Cargando usuario desde SharedPreferences: $name ($email)")
                
                val businessName = prefs.getString("business_name", null)
                val businessAddress = prefs.getString("business_address", null)
                val businessDescription = prefs.getString("business_description", null)
                val businessRut = prefs.getString("business_rut", null)
                val businessPhone = prefs.getString("business_phone", null)
                val businessEmail = prefs.getString("business_email", null)
                val profilePhotoUrl = prefs.getString("profile_photo_url", null)
                val businessLogoUrl = prefs.getString("business_logo_url", null)
                
                User(
                    id = prefs.getString("user_id", "user_${System.currentTimeMillis()}") ?: "user_${System.currentTimeMillis()}",
                    name = name,
                    email = email,
                    phone = prefs.getString("user_phone", null),
                    businessName = businessName,
                    businessType = null, // Se puede agregar después
                    businessRut = businessRut,
                    businessAddress = businessAddress,
                    businessDescription = businessDescription,
                    businessPhone = businessPhone,
                    businessEmail = businessEmail,
                    businessSocialMedia = null, // Se puede agregar después
                    businessLogoUrl = businessLogoUrl,
                    profilePhotoUrl = profilePhotoUrl,
                    isEmailVerified = prefs.getBoolean("email_verified", false),
                    createdAt = null,
                    updatedAt = null,
                    lastLoginAt = null,
                    isCloudSyncEnabled = false,
                    preferences = UserPreferences()
                )
            } else {
                println("❌ DEBUG: No hay usuario guardado en SharedPreferences")
                null
            }
        } catch (e: Exception) {
            println("❌ DEBUG: Error cargando usuario: ${e.message}")
            null
        }
    }
    
    private fun saveUserToPrefs(user: User) {
        println("💾 DEBUG: Guardando usuario en SharedPreferences: ${user.name} (${user.email})")
        
        prefs.edit().apply {
            putString("user_id", user.id)
            putString("user_name", user.name)
            putString("user_email", user.email)
            putString("user_phone", user.phone)
            putString("business_name", user.businessName)
            putString("business_address", user.businessAddress)
            putString("business_description", user.businessDescription)
            putString("business_rut", user.businessRut)
            putString("business_phone", user.businessPhone)
            putString("business_email", user.businessEmail)
            putString("profile_photo_url", user.profilePhotoUrl)
            putString("business_logo_url", user.businessLogoUrl)
            putBoolean("email_verified", user.isEmailVerified)
            apply()
        }
        
        println("✅ DEBUG: Usuario guardado exitosamente en SharedPreferences")
    }
    
    override val isAuthenticated: Flow<Boolean> = currentUser.map { it != null }
    
    override suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String?,
        businessName: String?
    ): Result<User> {
        return try {
            // Simulamos un registro exitoso
            val user = User(
                id = "user_${System.currentTimeMillis()}",
                email = email,
                name = name,
                phone = phone,
                businessName = businessName,
                businessType = null,
                businessRut = null,
                businessAddress = null,
                businessPhone = null,
                businessEmail = null,
                businessSocialMedia = null,
                businessLogoUrl = null,
                profilePhotoUrl = null,
                isEmailVerified = false,
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                lastLoginAt = null,
                isCloudSyncEnabled = false,
                preferences = UserPreferences()
            )
            
            _currentUser.value = user
            saveUserToPrefs(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Simulamos un login exitoso
            val user = User(
                id = "user_mock",
                email = email,
                name = "Usuario Demo",
                phone = null,
                businessName = "Mi Negocio Demo",
                businessType = null,
                businessRut = null,
                businessAddress = null,
                businessPhone = null,
                businessEmail = null,
                businessSocialMedia = null,
                businessLogoUrl = null,
                profilePhotoUrl = null,
                isEmailVerified = true,
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                lastLoginAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                isCloudSyncEnabled = false,
                preferences = UserPreferences()
            )
            
            _currentUser.value = user
            saveUserToPrefs(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout() {
        println("🚪 DEBUG: Cerrando sesión - limpiando usuario y preferencias")
        _currentUser.value = null
        prefs.edit().clear().apply()
        println("✅ DEBUG: Sesión cerrada exitosamente")
    }
    
    /**
     * 🧹 LIMPIAR COMPLETAMENTE LOS DATOS DE AUTENTICACIÓN
     * 
     * Función para limpiar todos los datos persistentes de autenticación
     * Útil para debugging o cuando hay problemas de sesión
     */
    suspend fun clearAllAuthData() {
        try {
            println("🧹 DEBUG: Limpiando completamente todos los datos de autenticación...")
            
            // Limpiar SharedPreferences
            prefs.edit().clear().apply()
            
            // Limpiar usuario actual
            _currentUser.value = null
            
            println("✅ DEBUG: Todos los datos de autenticación limpiados")
        } catch (e: Exception) {
            println("❌ DEBUG: Error limpiando datos de autenticación: ${e.message}")
        }
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            // Simulamos envío exitoso
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateProfile(user: User): Result<User> {
        return try {
            val updatedUser = user.copy(updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
            _currentUser.value = updatedUser
            saveUserToPrefs(updatedUser)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            // En AuthRepositoryImpl no hay implementación real de verificación de email
            // Esta implementación se usa solo para desarrollo/testing
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun checkEmailVerification(): Boolean {
        return try {
            // En AuthRepositoryImpl, asumimos que el email siempre está verificado
            // Esta implementación se usa solo para desarrollo/testing
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getTotalUserCount(): Int {
        // En AuthRepositoryImpl (mock), retornamos 0 para permitir registros
        // Esta implementación se usa solo para desarrollo/testing
        // En producción se usa FirebaseAuthRepository que consulta Firestore
        return 0
    }
}