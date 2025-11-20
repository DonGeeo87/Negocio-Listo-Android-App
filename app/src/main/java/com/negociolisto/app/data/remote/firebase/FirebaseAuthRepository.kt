package com.negociolisto.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.model.UserPreferences
import com.negociolisto.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.negociolisto.app.data.remote.firebase.FirebaseBackupRepository
import com.negociolisto.app.data.service.UsageLimitsService
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

/**
 * 🔥 Implementación de AuthRepository usando Firebase Authentication
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseBackupRepository: FirebaseBackupRepository, // ✅ Inyectado para restauración automática
    private val usageLimitsService: Lazy<UsageLimitsService> // ✅ Lazy injection para romper ciclo de dependencias
) : AuthRepository {

        private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val currentUserState = MutableStateFlow<User?>(null)
        override val currentUser: Flow<User?> = currentUserState.asStateFlow()
        private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            println("🔍 DEBUG FirebaseAuthRepository: AuthStateListener activado")
            println("🔍 DEBUG FirebaseAuthRepository: Firebase user presente: ${firebaseUser != null}")
            println("🔍 DEBUG FirebaseAuthRepository: Firebase user email: ${firebaseUser?.email}")
            println("🔍 DEBUG FirebaseAuthRepository: Firebase user UID: ${firebaseUser?.uid}")
            println("🔍 DEBUG FirebaseAuthRepository: Firebase user verified: ${firebaseUser?.isEmailVerified}")
            
            if (firebaseUser == null) {
                currentUserState.value = null
            } else {
                repositoryScope.launch {
                    val user = loadUserFromFirestore(firebaseUser)
                    println("🔍 DEBUG FirebaseAuthRepository: Usuario convertido: ${user.email}")
                    currentUserState.value = user
                }
            }
        }

        init {
            firebaseAuth.addAuthStateListener(authStateListener)
            firebaseAuth.currentUser?.let { firebaseUser ->
                repositoryScope.launch {
                    currentUserState.value = loadUserFromFirestore(firebaseUser)
                }
            }
        }

    override val isAuthenticated: Flow<Boolean> = currentUser.map { it != null }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Error al iniciar sesión"))
            
            val user = loadUserFromFirestore(firebaseUser)
            currentUserState.value = user
            
            // ✅ NOTA: La restauración de datos ahora se hace de forma visible
            // en la pantalla DataRestorationScreen después del login
            // No se hace aquí para evitar bloquear el login y mostrar progreso al usuario
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String, 
        password: String, 
        name: String, 
        phone: String?, 
        businessName: String?
    ): Result<User> {
        return try {
            // ✅ VERIFICAR CAPACIDAD DE STORAGE ANTES DE CREAR USUARIO
            val userCount = getTotalUserCount()
            val storageCheck = usageLimitsService.get().checkStorageCapacityForNewUser(userCount)
            
            if (!storageCheck.canAdd) {
                return Result.failure(
                    Exception(storageCheck.message ?: "No hay capacidad para nuevos usuarios en este momento.")
                )
            }
            
            // Continuar con el registro normal si hay capacidad
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Error al crear usuario"))
            
            // Actualizar perfil con el nombre
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            
            firebaseUser.updateProfile(profileUpdates).await()
            
            val user = convertToUser(firebaseUser)
            currentUserState.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        override suspend fun logout() {
            try {
                println("🚪 DEBUG: Iniciando logout completo...")
                
                // Limpiar datos de Firestore si existe
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    try {
                        // Limpiar datos locales del usuario en Firestore (opcional)
                        println("🗑️ DEBUG: Limpiando datos del usuario: ${currentUser.uid}")
                    } catch (e: Exception) {
                        println("⚠️ DEBUG: Error limpiando datos de Firestore: ${e.message}")
                    }
                }
                
                // Cerrar sesión de Firebase Auth
                firebaseAuth.signOut()
                
                // Verificar que la sesión se cerró correctamente
                val userAfterLogout = firebaseAuth.currentUser
                if (userAfterLogout == null) {
                    println("✅ DEBUG: Logout exitoso - usuario eliminado de Firebase Auth")
                    currentUserState.value = null
                } else {
                    println("❌ DEBUG: Error en logout - usuario aún presente: ${userAfterLogout.uid}")
                }
                
            } catch (e: Exception) {
                println("❌ DEBUG: Error durante logout: ${e.message}")
                // Intentar cerrar sesión de todas formas
                try {
                    firebaseAuth.signOut()
                } catch (e2: Exception) {
                    println("❌ DEBUG: Error crítico en logout: ${e2.message}")
                }
            }
        }
        
        /**
         * 🧹 FORZAR LOGOUT COMPLETO
         * 
         * Método para forzar el logout completo cuando hay problemas de persistencia
         */
        suspend fun forceLogout() {
            try {
                println("🧹 DEBUG: Forzando logout completo...")
                
                // Cerrar sesión múltiples veces para asegurar limpieza
                firebaseAuth.signOut()
                kotlinx.coroutines.delay(100)
                firebaseAuth.signOut()
                
                // Verificar estado después del logout forzado
                val userAfterForceLogout = firebaseAuth.currentUser
                if (userAfterForceLogout == null) {
                    println("✅ DEBUG: Logout forzado exitoso - usuario eliminado")
                } else {
                    println("❌ DEBUG: Usuario persistente después de logout forzado: ${userAfterForceLogout.uid}")
                }
                
            } catch (e: Exception) {
                println("❌ DEBUG: Error en logout forzado: ${e.message}")
            }
        }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(user: User): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No hay usuario autenticado"))
            
            // Actualizar perfil básico en Firebase Auth
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(user.name)
                .build()
            
            firebaseUser.updateProfile(profileUpdates).await()
            
            // Guardar datos completos en Firestore
            val userData = mapOf(
                "id" to user.id,
                "email" to user.email,
                "name" to user.name,
                "phone" to user.phone,
                "businessName" to user.businessName,
                "businessType" to user.businessType?.name,
                "businessRut" to user.businessRut,
                "businessAddress" to user.businessAddress,
                "businessPhone" to user.businessPhone,
                "businessEmail" to user.businessEmail,
                "businessSocialMedia" to user.businessSocialMedia?.let { socialMedia ->
                    mapOf(
                        "instagram" to socialMedia.instagram,
                        "facebook" to socialMedia.facebook,
                        "twitter" to socialMedia.twitter,
                        "linkedin" to socialMedia.linkedin,
                        "website" to socialMedia.website
                    )
                },
                "businessLogoUrl" to user.businessLogoUrl,
                "profilePhotoUrl" to user.profilePhotoUrl,
                "isEmailVerified" to user.isEmailVerified,
                "isCloudSyncEnabled" to user.isCloudSyncEnabled,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()
            
            println("✅ DEBUG: Usuario guardado en Firestore: ${user.name}")
            
            // Devolver el usuario actualizado con datos de Firestore
            val updatedUser = loadUserFromFirestore(firebaseUser)
            currentUserState.value = updatedUser
            Result.success(updatedUser)
        } catch (e: Exception) {
            println("❌ DEBUG: Error guardando en Firestore: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Cargar datos completos del usuario desde Firestore
     */
    suspend fun loadUserFromFirestore(firebaseUser: FirebaseUser): User {
        return try {
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            
            if (userDoc.exists()) {
                convertToUserFromFirestore(firebaseUser, userDoc.data ?: emptyMap())
            } else {
                convertToUser(firebaseUser)
            }
        } catch (e: Exception) {
            println("❌ DEBUG: Error cargando datos de Firestore: ${e.message}")
            convertToUser(firebaseUser)
        }
    }

    private fun convertToUser(firebaseUser: FirebaseUser): User {
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            name = firebaseUser.displayName ?: "",
            phone = null, // Firebase no proporciona esta info directamente
            businessName = null, // Se puede agregar a Firestore después
            businessType = null,
            businessRut = null,
            businessAddress = null,
            businessPhone = null,
            businessEmail = null,
            businessSocialMedia = null,
            businessLogoUrl = null,
            profilePhotoUrl = firebaseUser.photoUrl?.toString(),
            isEmailVerified = firebaseUser.isEmailVerified,
            createdAt = null, // Firebase no proporciona esta info directamente
            updatedAt = null,
            lastLoginAt = null, // Firebase no proporciona esta info directamente
            isCloudSyncEnabled = true, // Firebase = sincronización en la nube
            preferences = UserPreferences() // Preferencias por defecto
        )
    }
    
    private fun convertToUserFromFirestore(firebaseUser: FirebaseUser, data: Map<String, Any?>): User {
        val socialMediaData = data["businessSocialMedia"] as? Map<String, Any?>
        val socialMedia = if (socialMediaData != null) {
            com.negociolisto.app.domain.model.BusinessSocialMedia(
                instagram = socialMediaData["instagram"] as? String,
                facebook = socialMediaData["facebook"] as? String,
                twitter = socialMediaData["twitter"] as? String,
                linkedin = socialMediaData["linkedin"] as? String,
                website = socialMediaData["website"] as? String
            )
        } else null
        
        return User(
            id = data["id"] as? String ?: firebaseUser.uid,
            email = data["email"] as? String ?: firebaseUser.email ?: "",
            name = data["name"] as? String ?: firebaseUser.displayName ?: "",
            phone = data["phone"] as? String,
            businessName = data["businessName"] as? String,
            businessType = (data["businessType"] as? String)?.let { typeString ->
                try {
                    com.negociolisto.app.domain.model.BusinessType.valueOf(typeString)
                } catch (e: Exception) {
                    null
                }
            },
            businessRut = data["businessRut"] as? String,
            businessAddress = data["businessAddress"] as? String,
            businessPhone = data["businessPhone"] as? String,
            businessEmail = data["businessEmail"] as? String,
            businessSocialMedia = socialMedia,
            businessLogoUrl = data["businessLogoUrl"] as? String,
            profilePhotoUrl = data["profilePhotoUrl"] as? String ?: firebaseUser.photoUrl?.toString(),
            isEmailVerified = data["isEmailVerified"] as? Boolean ?: firebaseUser.isEmailVerified,
            createdAt = null,
            updatedAt = null,
            lastLoginAt = null,
            isCloudSyncEnabled = data["isCloudSyncEnabled"] as? Boolean ?: true,
            preferences = UserPreferences()
        )
    }
    
    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun checkEmailVerification(): Boolean {
        return try {
            FirebaseAuth.getInstance().currentUser?.reload()?.await()
            FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 👥 OBTENER TOTAL DE USUARIOS
     * 
     * Obtiene el número total de usuarios registrados en Firestore.
     * Se usa para verificar capacidad de Storage antes de permitir nuevos registros.
     */
    override suspend fun getTotalUserCount(): Int {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.size()
        } catch (e: Exception) {
            // Si falla, retornar 0 para permitir registro (fallback conservador)
            println("⚠️ DEBUG: Error obteniendo conteo de usuarios: ${e.message}")
            0
        }
    }
}
