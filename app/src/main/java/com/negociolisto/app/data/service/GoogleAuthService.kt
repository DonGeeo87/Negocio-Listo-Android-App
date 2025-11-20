package com.negociolisto.app.data.service

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔐 SERVICIO DE AUTENTICACIÓN CON GOOGLE
 * 
 * Este servicio maneja la autenticación con Google (para futuras funcionalidades)
 * y hacer backups automáticos de los datos del usuario.
 */
@Singleton
class GoogleAuthService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _currentAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val currentAccount: StateFlow<GoogleSignInAccount?> = _currentAccount.asStateFlow()
    
    // Cliente de Google Sign-In
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * 🚀 INICIAR SESIÓN CON GOOGLE
     * 
     * Inicia el proceso de autenticación con Google
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }
    
    /**
     * ✅ PROCESAR RESULTADO DE AUTENTICACIÓN
     * 
     * Procesa el resultado de la autenticación con Google
     */
    suspend fun handleSignInResult(data: Intent?): Boolean {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.await()
            
            if (account != null) {
                _currentAccount.value = account
                _isAuthenticated.value = true
                println("✅ DEBUG: Autenticación exitosa con Google: ${account.email}")
                true
            } else {
                println("❌ DEBUG: Error en autenticación con Google")
                false
            }
        } catch (e: Exception) {
            println("❌ DEBUG: Error en autenticación: ${e.message}")
            _isAuthenticated.value = false
            _currentAccount.value = null
            false
        }
    }
    
    /**
     * 🔑 OBTENER CREDENCIALES DE GOOGLE
     * 
     * Obtiene las credenciales necesarias para acceder a Google Drive (para futuras funcionalidades)
     */
    fun getCredentials(): GoogleAccountCredential? {
        val account = _currentAccount.value ?: return null
        
        return GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccount = account.account
        }
    }
    
    /**
     * 🚪 CERRAR SESIÓN
     * 
     * Cierra la sesión actual de Google
     */
    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
            _isAuthenticated.value = false
            _currentAccount.value = null
            println("✅ DEBUG: Sesión cerrada exitosamente")
        } catch (e: Exception) {
            println("❌ DEBUG: Error cerrando sesión: ${e.message}")
        }
    }
    
    /**
     * 🔄 VERIFICAR ESTADO DE AUTENTICACIÓN
     * 
     * Verifica si el usuario ya está autenticado
     */
    suspend fun checkAuthStatus() {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                _currentAccount.value = account
                _isAuthenticated.value = true
                println("✅ DEBUG: Usuario ya autenticado: ${account.email}")
            } else {
                _isAuthenticated.value = false
                _currentAccount.value = null
            }
        } catch (e: Exception) {
            println("❌ DEBUG: Error verificando estado de autenticación: ${e.message}")
            _isAuthenticated.value = false
            _currentAccount.value = null
        }
    }
    
    /**
     * 📧 OBTENER EMAIL DEL USUARIO
     * 
     * Obtiene el email del usuario autenticado
     */
    fun getUserEmail(): String? {
        return _currentAccount.value?.email
    }
    
    /**
     * 👤 OBTENER NOMBRE DEL USUARIO
     * 
     * Obtiene el nombre del usuario autenticado
     */
    fun getUserName(): String? {
        return _currentAccount.value?.displayName
    }
}
