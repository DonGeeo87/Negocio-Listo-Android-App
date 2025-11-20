package com.negociolisto.app.data.remote.googledrive

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔐 SERVICIO DE AUTENTICACIÓN CON GOOGLE DRIVE - IMPLEMENTACIÓN REAL
 * 
 * Maneja la autenticación OAuth2 con Google Drive API real,
 * incluyendo conexión, desconexión y estado de autenticación.
 */
@Singleton
class GoogleDriveAuthService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _currentAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val currentAccount: StateFlow<GoogleSignInAccount?> = _currentAccount.asStateFlow()
    
    private var googleSignInClient: GoogleSignInClient? = null
    private var driveService: Drive? = null
    
    init {
        initializeGoogleSignIn()
    }
    
    /**
     * 🔧 INICIALIZAR GOOGLE SIGN-IN CON SCOPES DE DRIVE
     * 
     * Configura el cliente de Google Sign-In con los permisos necesarios
     * para acceder a Google Drive.
     */
    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
            
        googleSignInClient = GoogleSignIn.getClient(context, gso)
        
        // Verificar si ya está conectado
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            _currentAccount.value = account
            _isConnected.value = true
            initializeDriveService(account)
        }
    }
    
    /**
     * 🔧 INICIALIZAR SERVICIO DE DRIVE REAL
     * 
     * Configura el servicio de Google Drive API con las credenciales
     * del usuario autenticado usando AndroidHttp y GsonFactory.
     */
    private fun initializeDriveService(account: GoogleSignInAccount) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            
            driveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
            .setApplicationName("NegocioListo")
            .build()
            
            android.util.Log.d("GoogleDriveAuth", "✅ Drive service inicializado correctamente")
        } catch (e: Exception) {
            android.util.Log.e("GoogleDriveAuth", "❌ Error al inicializar Drive service: ${e.message}", e)
            driveService = null
        }
    }
    
    /**
     * 📱 OBTENER INTENT DE SIGN-IN
     * 
     * Retorna el Intent necesario para iniciar el proceso de autenticación.
     */
    fun getSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }
    
    /**
     * ✅ MANEJAR RESULTADO DE SIGN-IN
     * 
     * Procesa el resultado de la autenticación y actualiza el estado.
     */
    fun handleSignInResult(result: Intent) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result)
            if (task.isSuccessful) {
                val account = task.result
                _currentAccount.value = account
                _isConnected.value = true
                initializeDriveService(account)
                android.util.Log.d("GoogleDriveAuth", "✅ Usuario autenticado: ${account.email}")
            } else {
                android.util.Log.e("GoogleDriveAuth", "❌ Error en autenticación: ${task.exception?.message}")
            }
        } catch (e: Exception) {
            android.util.Log.e("GoogleDriveAuth", "❌ Excepción en handleSignInResult: ${e.message}", e)
        }
    }
    
    /**
     * 📁 OBTENER SERVICIO DE DRIVE REAL
     * 
     * Retorna el servicio de Google Drive configurado, o null si no está autenticado.
     */
    fun getDriveService(): Drive? = driveService
    
    /**
     * 🚪 CERRAR SESIÓN
     * 
     * Desconecta al usuario y limpia el estado de autenticación.
     */
    fun signOut() {
        googleSignInClient?.signOut()?.addOnCompleteListener {
            _currentAccount.value = null
            _isConnected.value = false
            driveService = null
            android.util.Log.d("GoogleDriveAuth", "🚪 Usuario desconectado")
        }
    }
    
    /**
     * 🔄 VERIFICAR ESTADO DE CONEXIÓN
     * 
     * Verifica si el usuario sigue autenticado y actualiza el estado.
     */
    fun checkConnectionStatus() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null && _isConnected.value) {
            // Usuario sigue conectado
            return
        } else if (account == null && _isConnected.value) {
            // Usuario se desconectó externamente
            _currentAccount.value = null
            _isConnected.value = false
            driveService = null
        }
    }
}
