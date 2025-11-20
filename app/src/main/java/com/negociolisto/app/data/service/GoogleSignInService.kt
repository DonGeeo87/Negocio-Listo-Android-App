package com.negociolisto.app.data.service

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @deprecated Usar GoogleAuthService.kt en su lugar
 * Este archivo se mantiene solo para compatibilidad temporal
 * 
 * 🔐 SERVICIO DE AUTENTICACIÓN CON GOOGLE
 * 
 * Este servicio maneja la autenticación con Google usando Firebase Auth.
 * Se integra perfectamente con el sistema de autenticación existente.
 */
@Singleton
class GoogleSignInService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _currentUser = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(null)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()
    
    // Cliente de Google Sign-In
    private val googleSignInClient: GoogleSignInClient by lazy {
        println("🔍 DEBUG GoogleSignInService: Inicializando GoogleSignInClient...")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.negociolisto.app.R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        
        println("🔍 DEBUG GoogleSignInService: GoogleSignInOptions creado con client_id: ${context.getString(com.negociolisto.app.R.string.default_web_client_id)}")
        val client = GoogleSignIn.getClient(context, gso)
        println("🔍 DEBUG GoogleSignInService: GoogleSignInClient creado exitosamente")
        client
    }
    
    // Cliente de Google Sign-In con selector de cuentas
    private val googleSignInClientWithSelector: GoogleSignInClient by lazy {
        println("🔍 DEBUG GoogleSignInService: Inicializando GoogleSignInClient con selector...")
        val gso = GoogleSignInOptions.Builder()
            .requestIdToken(context.getString(com.negociolisto.app.R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        
        println("🔍 DEBUG GoogleSignInService: GoogleSignInOptions con selector creado")
        val client = GoogleSignIn.getClient(context, gso)
        println("🔍 DEBUG GoogleSignInService: GoogleSignInClient con selector creado exitosamente")
        client
    }
    
    init {
        // Verificar si ya hay un usuario autenticado
        checkAuthStatus()
    }
    
    /**
     * 🚀 OBTENER INTENT DE AUTENTICACIÓN
     * 
     * Obtiene el Intent necesario para iniciar la autenticación con Google
     * Ahora usa el selector de cuentas por defecto
     */
    fun getSignInIntent(): android.content.Intent {
        println("🔍 DEBUG GoogleSignInService: getSignInIntent() llamado")
        try {
            // Usar el cliente con selector para mostrar todas las cuentas
            val intent = googleSignInClientWithSelector.signInIntent
            println("🔍 DEBUG GoogleSignInService: Intent con selector creado exitosamente")
            return intent
        } catch (e: Exception) {
            println("❌ DEBUG GoogleSignInService: Error creando Intent: ${e.message}")
            throw e
        }
    }
    
    /**
     * 🔄 OBTENER INTENT CON SELECTOR DE CUENTAS
     * 
     * Fuerza a mostrar el selector de cuentas incluso si hay una cuenta principal
     */
    fun getSignInIntentWithAccountSelector(): android.content.Intent {
        println("🔍 DEBUG GoogleSignInService: getSignInIntentWithAccountSelector() llamado")
        try {
            // Crear un cliente completamente nuevo para forzar el selector
            val gso = GoogleSignInOptions.Builder()
                .requestIdToken(context.getString(com.negociolisto.app.R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()
            
            val tempClient = GoogleSignIn.getClient(context, gso)
            
            // Cerrar sesión primero
            tempClient.signOut()
            
            val intent = tempClient.signInIntent
            println("🔍 DEBUG GoogleSignInService: Intent con selector creado exitosamente")
            return intent
        } catch (e: Exception) {
            println("❌ DEBUG GoogleSignInService: Error creando Intent con selector: ${e.message}")
            throw e
        }
    }
    
    /**
     * 🔄 OBTENER INTENT CON SELECTOR FORZADO
     * 
     * Método alternativo que fuerza el selector usando una estrategia diferente
     */
    fun getSignInIntentForcedSelector(): android.content.Intent {
        println("🔍 DEBUG GoogleSignInService: getSignInIntentForcedSelector() llamado")
        try {
            // Crear cliente temporal sin DEFAULT_SIGN_IN
            val gso = GoogleSignInOptions.Builder()
                .requestIdToken(context.getString(com.negociolisto.app.R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()
            
            val tempClient = GoogleSignIn.getClient(context, gso)
            
            // Intentar revocar acceso primero para limpiar completamente
            tempClient.revokeAccess()
            
            val intent = tempClient.signInIntent
            println("🔍 DEBUG GoogleSignInService: Intent con selector forzado creado exitosamente")
            return intent
        } catch (e: Exception) {
            println("❌ DEBUG GoogleSignInService: Error creando Intent con selector forzado: ${e.message}")
            throw e
        }
    }
    
    /**
     * ✅ PROCESAR RESULTADO DE AUTENTICACIÓN
     * 
     * Procesa el resultado de la autenticación con Google y crea la cuenta en Firebase
     */
    suspend fun handleSignInResult(data: android.content.Intent?): Result<String> {
        println("🔍 DEBUG GoogleSignInService: handleSignInResult() llamado con data: ${data != null}")
        return try {
            println("🔍 DEBUG GoogleSignInService: Obteniendo cuenta de Google...")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            println("🔍 DEBUG GoogleSignInService: Task obtenido: ${task != null}")
            
            val account = task.getResult(ApiException::class.java)
            println("🔍 DEBUG GoogleSignInService: Cuenta obtenida: ${account != null}")
            
            if (account != null) {
                println("🔍 DEBUG GoogleSignInService: Email de la cuenta: ${account.email}")
                println("🔍 DEBUG GoogleSignInService: Nombre: ${account.displayName}")
                println("🔍 DEBUG GoogleSignInService: ID Token presente: ${account.idToken != null}")
                println("🔍 DEBUG GoogleSignInService: ID Token: ${account.idToken?.take(20)}...")
                
                // Autenticar con Firebase usando el token de Google
                println("🔍 DEBUG GoogleSignInService: Creando credencial de Firebase...")
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                println("🔍 DEBUG GoogleSignInService: Credencial creada, autenticando con Firebase...")
                
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                println("🔍 DEBUG GoogleSignInService: Resultado de Firebase Auth: ${authResult.user != null}")
                
                if (authResult.user != null) {
                    _currentUser.value = authResult.user
                    _isAuthenticated.value = true
                    
                    println("✅ DEBUG GoogleSignInService: Usuario autenticado con Google: ${authResult.user?.email}")
                    println("✅ DEBUG GoogleSignInService: UID: ${authResult.user?.uid}")
                    println("✅ DEBUG GoogleSignInService: Estado actualizado - isAuthenticated=true")
                    
                    // El FirebaseAuthRepository ya detectará automáticamente el cambio en Firebase Auth
                    // No necesitamos sincronización manual
                    Result.success("Autenticación exitosa con Google")
                } else {
                    println("❌ DEBUG GoogleSignInService: Error al crear cuenta en Firebase")
                    Result.failure(Exception("Error al crear cuenta en Firebase"))
                }
            } else {
                println("❌ DEBUG GoogleSignInService: No se pudo obtener la cuenta de Google")
                Result.failure(Exception("No se pudo obtener la cuenta de Google"))
            }
        } catch (e: ApiException) {
            println("❌ DEBUG GoogleSignInService: ApiException en autenticación con Google: ${e.message}")
            println("❌ DEBUG GoogleSignInService: Código de error: ${e.statusCode}")
            println("❌ DEBUG GoogleSignInService: Stack trace: ${e.stackTraceToString()}")
            Result.failure(Exception("Error de autenticación: ${e.message}"))
        } catch (e: Exception) {
            println("❌ DEBUG GoogleSignInService: Error inesperado: ${e.message}")
            println("❌ DEBUG GoogleSignInService: Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
    
    /**
     * 🚪 CERRAR SESIÓN
     * 
     * Cierra la sesión actual de Google y Firebase
     */
    suspend fun signOut() {
        try {
            println("🚪 DEBUG: Iniciando cierre de sesión completo...")
            
            // Cerrar sesión en Firebase
            firebaseAuth.signOut()
            println("✅ DEBUG: Firebase Auth cerrado")
            
            // Cerrar sesión en Google (esto debería limpiar la cuenta persistente)
            googleSignInClient.signOut().await()
            println("✅ DEBUG: Google Sign-In cerrado")
            
            // Intentar revocar acceso (opcional, más agresivo)
            try {
                googleSignInClient.revokeAccess().await()
                println("✅ DEBUG: Acceso de Google revocado")
            } catch (e: Exception) {
                println("⚠️ DEBUG: No se pudo revocar acceso: ${e.message}")
            }
            
            _isAuthenticated.value = false
            _currentUser.value = null
            
            println("✅ DEBUG: Sesión cerrada completamente")
        } catch (e: Exception) {
            println("❌ DEBUG: Error cerrando sesión: ${e.message}")
        }
    }
    
    /**
     * 🔄 VERIFICAR ESTADO DE AUTENTICACIÓN
     * 
     * Verifica si el usuario ya está autenticado
     */
    fun checkAuthStatus() {
        val user = firebaseAuth.currentUser
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
        
        // Solo considerar autenticado si hay usuario en Firebase Y cuenta de Google
        if (user != null && googleAccount != null) {
            _currentUser.value = user
            _isAuthenticated.value = true
            println("✅ DEBUG: Usuario ya autenticado: ${user.email}")
        } else {
            // Si hay inconsistencia, limpiar todo
            if (user != null && googleAccount == null) {
                println("⚠️ DEBUG: Inconsistencia detectada - Firebase user sin Google account")
                firebaseAuth.signOut()
            }
            _isAuthenticated.value = false
            _currentUser.value = null
            println("🔍 DEBUG: No hay usuario autenticado")
        }
    }
    
    /**
     * 📧 OBTENER EMAIL DEL USUARIO
     * 
     * Obtiene el email del usuario autenticado
     */
    fun getUserEmail(): String? {
        return _currentUser.value?.email
    }
    
    /**
     * 👤 OBTENER NOMBRE DEL USUARIO
     * 
     * Obtiene el nombre del usuario autenticado
     */
    fun getUserName(): String? {
        return _currentUser.value?.displayName
    }
    
    /**
     * 🆔 OBTENER ID DEL USUARIO
     * 
     * Obtiene el ID único del usuario autenticado
     */
    fun getUserId(): String? {
        return _currentUser.value?.uid
    }
    
    /**
     * 🖼️ OBTENER FOTO DEL USUARIO
     * 
     * Obtiene la URL de la foto de perfil del usuario
     */
    fun getUserPhotoUrl(): String? {
        return _currentUser.value?.photoUrl?.toString()
    }
    
    /**
     * 🔗 OBTENER CREDENCIALES DE GOOGLE
     * 
     * Obtiene las credenciales necesarias para acceder a Google Drive (para futuras funcionalidades)
     * (compatible con el servicio de backup)
     */
    fun getGoogleCredentials(): com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        
        return com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential.usingOAuth2(
            context,
            listOf("https://www.googleapis.com/auth/drive.file")
        ).apply {
            selectedAccount = account.account
        }
    }
}
