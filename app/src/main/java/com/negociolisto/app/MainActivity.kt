package com.negociolisto.app

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import com.negociolisto.app.ui.auth.WelcomeScreen
import com.negociolisto.app.ui.auth.AuthViewModel
import com.negociolisto.app.ui.auth.LoginScreen
import com.negociolisto.app.ui.auth.RegisterScreen
import com.negociolisto.app.ui.main.MainScreen
import com.negociolisto.app.ui.theme.NegocioListoTheme
import com.negociolisto.app.ui.splash.SplashScreen
import com.negociolisto.app.ui.onboarding.OnboardingScreen
import com.negociolisto.app.ui.setup.InitialSetupScreen
import com.negociolisto.app.ui.auth.AfterRegisterScreen
import com.negociolisto.app.ui.auth.ForgotPasswordScreen
import com.negociolisto.app.ui.auth.DataRestorationScreen
import com.negociolisto.app.data.local.UiPreferencesStore
import com.negociolisto.app.ui.invoices.InvoiceSettingsStore
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import com.negociolisto.app.data.remote.firebase.FirebaseBackupRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 🏠 ACTIVIDAD PRINCIPAL DE NEGOCIO LISTO
 * 
 * Esta es la pantalla principal de nuestra app. Es como la "puerta de entrada"
 * por donde los usuarios acceden a todas las funcionalidades.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var categoryRepository: CustomCategoryRepository
    
    @Inject
    lateinit var firebaseBackupRepository: FirebaseBackupRepository
    
    /**
     * 🧭 NAVEGACIÓN INTELIGENTE POST-AUTENTICACIÓN
     * 
     * Determina a dónde navegar después de que un usuario se autentica,
     * basándose en si es usuario nuevo o existente.
     * 
     * Un usuario existente se identifica por tener categorías en la base de datos,
     * lo que indica que ya completó el setup anteriormente.
     */
    private suspend fun navigateAfterAuthentication(
        authViewModel: AuthViewModel,
        uiPrefs: UiPreferencesStore,
        navController: androidx.navigation.NavController,
        currentRoute: String
    ) {
        // Esperar a que el usuario esté disponible usando el Flow directamente
        // Esto es más confiable que usar .value que puede estar desactualizado
        val currentUser = authViewModel.currentUser.first { it != null }
        val userId = currentUser?.id
        
        if (userId != null) {
            // PRIMERO: Verificar si hay datos en Firebase para restaurar
            // Esto es crítico porque si hay datos en Firebase, el usuario es existente
            val hasBackupData = try {
                val backupStatusResult = firebaseBackupRepository.getBackupStatus(userId)
                if (backupStatusResult.isSuccess) {
                    val backupStatus = backupStatusResult.getOrNull()
                    // Verificar tanto el status como si hay datos reales
                    val status = backupStatus?.get("status") as? String
                    val hasData = backupStatus?.get("hasData") as? Boolean ?: false
                    // Hay backup si el status no es "no_backup" O si hay datos reales
                    (status != "no_backup" && status != null) || hasData
                } else {
                    false
                }
            } catch (e: Exception) {
                println("⚠️ DEBUG MainActivity: Error al verificar backup: ${e.message}")
                false
            }
            
            // SEGUNDO: Verificar si el usuario tiene categorías existentes localmente
            // (después de verificar Firebase, porque si hay datos en Firebase, primero hay que restaurarlos)
            val hasExistingCategories = try {
                val categories = categoryRepository.getActiveCategoriesByUser(userId).first()
                categories.isNotEmpty()
            } catch (e: Exception) {
                println("⚠️ DEBUG MainActivity: Error al verificar categorías: ${e.message}")
                false
            }
            
            val onboardingSeen = uiPrefs.onboardingSeenForUser(userId).first()
            val setupCompleted = uiPrefs.initialSetupCompletedForUser(userId).first()
            
            println("🔍 DEBUG MainActivity: Usuario $userId - hasBackup=$hasBackupData, hasCategories=$hasExistingCategories, onboardingSeen=$onboardingSeen, setupCompleted=$setupCompleted")
            
            when {
                // ✅ PRIORIDAD 1: Si hay datos en Firebase, restaurar primero (usuario existente)
                // Esto incluye usuarios que tienen datos en Firebase aunque no tengan categorías locales aún
                hasBackupData -> {
                    println("🔍 DEBUG MainActivity: Usuario con datos en Firebase, navegando a restauración...")
                    navController.navigate("data_restoration") {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
                // ✅ PRIORIDAD 2: Si tiene categorías existentes localmente, es usuario existente
                hasExistingCategories -> {
                    println("🔍 DEBUG MainActivity: Usuario existente con categorías locales, navegando a app principal...")
                    navController.navigate("main") {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
                // ✅ PRIORIDAD 3: Si el setup está completado, también es usuario existente
                setupCompleted -> {
                    println("🔍 DEBUG MainActivity: Usuario con setup completado, navegando a app principal...")
                    navController.navigate("main") {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
                // ✅ PRIORIDAD 4: Si no ha visto el onboarding, es usuario nuevo - mostrar onboarding
                !onboardingSeen -> {
                    println("🔍 DEBUG MainActivity: Usuario nuevo sin datos, navegando a onboarding...")
                    navController.navigate("onboarding") {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
                // ✅ PRIORIDAD 5: Si ya vio el onboarding pero no completó el setup, mostrar configuración inicial
                !setupCompleted -> {
                    println("🔍 DEBUG MainActivity: Usuario que vio onboarding pero no completó setup, navegando a configuración inicial...")
                    navController.navigate("initial_setup") {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
                // ✅ Caso por defecto: ir a main
                else -> {
                    println("🔍 DEBUG MainActivity: Usuario completo, navegando a app principal...")
                    navController.navigate("main") {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
            }
        } else {
            println("❌ DEBUG MainActivity: No se pudo obtener userId, navegando a onboarding por defecto...")
            navController.navigate("onboarding") {
                popUpTo(currentRoute) { inclusive = true }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Comentado para evitar doble splash screen - usamos SplashScreen.kt de Compose
        // installSplashScreen()
        enableEdgeToEdge()
        
        // Ocultar topbar del sistema para pantalla completa
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Inicializar configuraciones globales
        InvoiceSettingsStore.init(this)
        
        setContent {
            NegocioListoTheme {
                // Controlador de navegación para moverse entre pantallas
                val navController = rememberNavController()
                val uiPrefs = remember { UiPreferencesStore(this@MainActivity) }
                
                // Escala global de UI desde DataStore - aplicada a TODAS las pantallas
                val appScale by uiPrefs.appScale.collectAsStateWithLifecycle(initialValue = 1.0)
                val baseDensity = LocalDensity.current
                
                // 🔍 DETECTAR ZOOM DEL SISTEMA (fontScale)
                // fontScale > 1.0 significa que el usuario tiene zoom aumentado por accesibilidad
                val systemFontScale = baseDensity.fontScale
                
                // 🎯 CALCULAR ESCALA FINAL COMPENSANDO ZOOM DEL SISTEMA
                // Si el sistema tiene zoom aumentado, aplicamos un escalado proporcional
                // para mantener la proporción entre texto (sp) y elementos (dp)
                val finalDensityScale = if (systemFontScale > 1.0f) {
                    // Usuario con zoom del sistema: aplicar escala proporcional
                    // Reducimos la densidad para compensar el zoom y mantener proporciones
                    appScale.toFloat() / systemFontScale.coerceIn(1.0f, 2.0f)
                } else {
                    // Usuario sin zoom: usar escala normal del usuario
                    appScale.toFloat()
                }
                
                val scaledDensity = androidx.compose.ui.unit.Density(
                    density = baseDensity.density * finalDensityScale,
                    fontScale = baseDensity.fontScale // Mantener fontScale del sistema para respetar accesibilidad
                )
                
                // Aplicar densidad escalada globalmente a todas las pantallas
                CompositionLocalProvider(
                    LocalDensity provides scaledDensity
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Configuración de navegación entre pantallas
                        NavHost(
                            navController = navController,
                            startDestination = "splash"
                        ) {
                        // 🌟 Pantalla de splash
                        composable("splash") {
                            SplashScreen(
                                onNavigateToMain = {
                                    navController.navigate("main") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onNavigateToWelcome = {
                                    navController.navigate("welcome") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        // 🔄 Pantalla de restauración de datos (para usuarios existentes)
                        composable("data_restoration") {
                            DataRestorationScreen(
                                onRestorationComplete = {
                                    // Después de restaurar, ir al dashboard
                                    navController.navigate("main") {
                                        popUpTo("data_restoration") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        // 🎯 Pantalla de onboarding (para usuarios nuevos)
                        composable("onboarding") {
                            val authViewModel: AuthViewModel = hiltViewModel()
                            
                            OnboardingScreen(
                                onComplete = {
                                    // Marcar onboarding como visto para este usuario específico
                                    lifecycleScope.launch {
                                        val userId = authViewModel.currentUser.value?.id
                                        if (userId != null) {
                                            uiPrefs.setOnboardingSeenForUser(userId, true)
                                        }
                                        // También marcar el onboarding global como visto
                                        uiPrefs.setOnboardingSeen(true)
                                    }
                                    // Ir a configuración inicial
                                    navController.navigate("initial_setup") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        // 🚀 Configuración inicial completa
                        composable("initial_setup") {
                            val authViewModel: AuthViewModel = hiltViewModel()
                            
                            InitialSetupScreen(
                                onComplete = {
                                    // Marcar setup como completado para este usuario
                                    lifecycleScope.launch {
                                        val userId = authViewModel.currentUser.value?.id
                                        if (userId != null) {
                                            uiPrefs.setInitialSetupCompletedForUser(userId, true)
                                        }
                                    }
                                    // Ir a la app principal
                                    navController.navigate("main") {
                                        popUpTo("initial_setup") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        // 🎉 Pantalla de bienvenida (primera pantalla)
                        composable("welcome") {
                            val authViewModel: AuthViewModel = hiltViewModel()
                            
                            // Launcher para Google Sign-In
                            val googleSignInLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult()
                            ) { result ->
                                lifecycleScope.launch {
                                    authViewModel.handleGoogleSignInResult(result.data)
                                }
                            }
                            
                            // Navegación automática cuando el usuario se autentique
                            LaunchedEffect(authViewModel.isAuthenticated) {
                                authViewModel.isAuthenticated.collect { isAuthenticated ->
                                    if (isAuthenticated) {
                                        println("🔍 DEBUG MainActivity: Usuario autenticado desde welcome, navegando inteligentemente...")
                                        navigateAfterAuthentication(authViewModel, uiPrefs, navController, "welcome")
                                    }
                                }
                            }
                            
                            // Aplicar zoom del 75% a la pantalla de bienvenida
                            val currentDensity = LocalDensity.current
                            val authScreenScale = 0.75f
                            val authScaledDensity = androidx.compose.ui.unit.Density(
                                density = currentDensity.density * authScreenScale,
                                fontScale = currentDensity.fontScale
                            )
                            
                            CompositionLocalProvider(LocalDensity provides authScaledDensity) {
                                WelcomeScreen(
                                    onLoginClick = {
                                        navController.navigate("login")
                                    },
                                    onRegisterClick = {
                                        println("🔍 DEBUG MainActivity: Botón 'Crear cuenta gratis' presionado")
                                        navController.navigate("register")
                                    },
                                    onGoogleSignIn = {
                                        println("🔍 DEBUG MainActivity: Botón Google Sign-In presionado")
                                        try {
                                            val intent = authViewModel.getGoogleSignInIntent()
                                            println("🔍 DEBUG MainActivity: Intent obtenido, lanzando launcher...")
                                            googleSignInLauncher.launch(intent)
                                            println("🔍 DEBUG MainActivity: Launcher lanzado exitosamente")
                                        } catch (e: Exception) {
                                            println("❌ DEBUG MainActivity: Error en Google Sign-In: ${e.message}")
                                        }
                                    },
                                    onGoogleSignUp = {
                                        println("🔍 DEBUG MainActivity: Botón Google Sign-Up presionado")
                                        try {
                                            val intent = authViewModel.getGoogleSignInIntent()
                                            println("🔍 DEBUG MainActivity: Intent obtenido, lanzando launcher...")
                                            googleSignInLauncher.launch(intent)
                                            println("🔍 DEBUG MainActivity: Launcher lanzado exitosamente")
                                        } catch (e: Exception) {
                                            println("❌ DEBUG MainActivity: Error en Google Sign-Up: ${e.message}")
                                        }
                                    },
                                    onAlreadyLoggedIn = {
                                        // Usuario ya está logueado, ir directamente a la app principal
                                        navController.navigate("main") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                        
                        // 🔐 Pantalla de inicio de sesión
                        composable("login") {
                            val authViewModel: AuthViewModel = hiltViewModel()
                            
                            // Navegación automática cuando el usuario se autentique
                            LaunchedEffect(authViewModel.isAuthenticated) {
                                authViewModel.isAuthenticated.collect { isAuthenticated ->
                                    if (isAuthenticated) {
                                        println("🔍 DEBUG MainActivity: Usuario autenticado desde login, navegando inteligentemente...")
                                        navigateAfterAuthentication(authViewModel, uiPrefs, navController, "login")
                                    }
                                }
                            }
                            
                            // Aplicar zoom del 75% a la pantalla de inicio de sesión
                            val currentDensity = LocalDensity.current
                            val authScreenScale = 0.75f
                            val authScaledDensity = androidx.compose.ui.unit.Density(
                                density = currentDensity.density * authScreenScale,
                                fontScale = currentDensity.fontScale
                            )
                            
                            CompositionLocalProvider(LocalDensity provides authScaledDensity) {
                                LoginScreen(
                                    onBackClick = {
                                        navController.popBackStack()
                                    },
                                    onLoginSuccess = {
                                        // La navegación se maneja en el LaunchedEffect arriba
                                    },
                                    onForgotPasswordClick = {
                                        navController.navigate("forgot-password")
                                    },
                                    onCreateAccountClick = {
                                        navController.navigate("register")
                                    }
                                )
                            }
                        }
                        
                        // 📝 Pantalla de registro
                        composable("register") {
                            // Aplicar zoom del 75% a la pantalla de registro
                            val currentDensity = LocalDensity.current
                            val authScreenScale = 0.75f
                            val authScaledDensity = androidx.compose.ui.unit.Density(
                                density = currentDensity.density * authScreenScale,
                                fontScale = currentDensity.fontScale
                            )
                            
                            CompositionLocalProvider(LocalDensity provides authScaledDensity) {
                                RegisterScreen(
                                    onBackClick = {
                                        navController.popBackStack()
                                    },
                                    onRegisterSuccess = {
                                        // Ir a pantalla de bienvenida/carga tras registro
                                        navController.navigate("after_register") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    },
                                    onLoginClick = {
                                        navController.navigate("login")
                                    }
                                )
                            }
                        }
                        
                        // 🔑 Pantalla de recuperación de contraseña
                        composable("forgot-password") {
                            ForgotPasswordScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onEmailSent = {
                                    navController.navigate("login") {
                                        popUpTo("forgot-password") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        // Pantalla intermedia después de registrarse
                        composable("after_register") {
                            val authViewModel: AuthViewModel = hiltViewModel()
                            
                            AfterRegisterScreen(
                                onContinue = {
                                    // Usar la lógica de navegación inteligente que verifica onboarding
                                    lifecycleScope.launch {
                                        navigateAfterAuthentication(
                                            authViewModel = authViewModel,
                                            uiPrefs = uiPrefs,
                                            navController = navController,
                                            currentRoute = "after_register"
                                        )
                                    }
                                }
                            )
                        }
                        
                        // 🏠 Pantalla principal de la app
                        composable("main") {
                            MainScreen(
                                onLoggedOut = {
                                    navController.navigate("welcome") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
                }
            }
        }
    }
}