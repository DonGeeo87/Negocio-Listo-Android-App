package com.negociolisto.app.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.negociolisto.app.ui.theme.NegocioListoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🧪 PRUEBAS DE FLUJO DE AUTENTICACIÓN
 * 
 * Estas pruebas verifican que las pantallas de autenticación funcionen correctamente
 * y que los usuarios puedan navegar entre ellas sin problemas.
 * 
 * Incluye pruebas para:
 * - Pantalla de bienvenida
 * - Pantalla de login
 * - Pantalla de registro
 * - Navegación entre pantallas
 * - Validación de formularios
 */
@RunWith(AndroidJUnit4::class)
class AuthenticationFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==========================================
    // PRUEBAS DE WELCOME SCREEN
    // ==========================================

    @Test
    fun welcomeScreen_displaysAllElements() {
        composeTestRule.setContent {
            NegocioListoTheme {
                WelcomeScreen(
                    onLoginClick = { },
                    onRegisterClick = { },
                    onGuestModeClick = { }
                )
            }
        }

        // Verificar que se muestran los elementos principales
        composeTestRule.onNodeWithText("NegocioListo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tu negocio en tus manos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Crear cuenta gratis").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ya tengo cuenta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Probar sin crear cuenta").assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_buttonsAreClickable() {
        var loginClicked = false
        var registerClicked = false
        var guestClicked = false

        composeTestRule.setContent {
            NegocioListoTheme {
                WelcomeScreen(
                    onLoginClick = { loginClicked = true },
                    onRegisterClick = { registerClicked = true },
                    onGuestModeClick = { guestClicked = true }
                )
            }
        }

        // Probar botón de registro
        composeTestRule.onNodeWithText("Crear cuenta gratis").performClick()
        assert(registerClicked)

        // Probar botón de login
        composeTestRule.onNodeWithText("Ya tengo cuenta").performClick()
        assert(loginClicked)

        // Probar botón de modo invitado
        composeTestRule.onNodeWithText("Probar sin crear cuenta").performClick()
        assert(guestClicked)
    }

    @Test
    fun welcomeScreen_showsBenefits() {
        composeTestRule.setContent {
            NegocioListoTheme {
                WelcomeScreen(
                    onLoginClick = { },
                    onRegisterClick = { },
                    onGuestModeClick = { }
                )
            }
        }

        // Verificar que se muestran los beneficios
        composeTestRule.onNodeWithText("¿Por qué crear una cuenta?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sincronización en la nube").assertIsDisplayed()
        composeTestRule.onNodeWithText("Respaldo automático").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reportes avanzados").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notificaciones").assertIsDisplayed()
    }

    // ==========================================
    // PRUEBAS DE LOGIN SCREEN
    // ==========================================

    @Test
    fun loginScreen_displaysAllElements() {
        composeTestRule.setContent {
            NegocioListoTheme {
                LoginScreen(
                    onBackClick = { },
                    onLoginSuccess = { },
                    onForgotPasswordClick = { },
                    onCreateAccountClick = { }
                )
            }
        }

        // Verificar elementos principales
        composeTestRule.onNodeWithText("¡Bienvenido de vuelta!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿No tienes cuenta?").assertIsDisplayed()
    }

    @Test
    fun loginScreen_formValidation() {
        composeTestRule.setContent {
            NegocioListoTheme {
                LoginScreen(
                    onBackClick = { },
                    onLoginSuccess = { },
                    onForgotPasswordClick = { },
                    onCreateAccountClick = { }
                )
            }
        }

        // El botón debe estar deshabilitado inicialmente
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsNotEnabled()

        // Llenar email
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        
        // El botón sigue deshabilitado sin contraseña
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsNotEnabled()

        // Llenar contraseña
        composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
        
        // Ahora el botón debe estar habilitado
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsEnabled()
    }

    @Test
    fun loginScreen_passwordVisibilityToggle() {
        composeTestRule.setContent {
            NegocioListoTheme {
                LoginScreen(
                    onBackClick = { },
                    onLoginSuccess = { },
                    onForgotPasswordClick = { },
                    onCreateAccountClick = { }
                )
            }
        }

        // Llenar contraseña
        composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
        
        // Buscar y hacer clic en el botón de visibilidad
        composeTestRule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        
        // Verificar que el botón cambió
        composeTestRule.onNodeWithContentDescription("Ocultar contraseña").assertExists()
    }

    @Test
    fun loginScreen_navigationButtons() {
        var backClicked = false
        var forgotPasswordClicked = false
        var createAccountClicked = false

        composeTestRule.setContent {
            NegocioListoTheme {
                LoginScreen(
                    onBackClick = { backClicked = true },
                    onLoginSuccess = { },
                    onForgotPasswordClick = { forgotPasswordClicked = true },
                    onCreateAccountClick = { createAccountClicked = true }
                )
            }
        }

        // Probar botón de volver
        composeTestRule.onNodeWithContentDescription("Volver").performClick()
        assert(backClicked)

        // Probar enlace de contraseña olvidada
        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?").performClick()
        assert(forgotPasswordClicked)

        // Probar enlace de crear cuenta
        composeTestRule.onNodeWithText("Crear cuenta gratis").performClick()
        assert(createAccountClicked)
    }

    // ==========================================
    // PRUEBAS DE REGISTER SCREEN
    // ==========================================

    @Test
    fun registerScreen_displaysAllElements() {
        composeTestRule.setContent {
            NegocioListoTheme {
                RegisterScreen(
                    onBackClick = { },
                    onRegisterSuccess = { },
                    onLoginClick = { }
                )
            }
        }

        // Verificar elementos principales
        composeTestRule.onNodeWithText("Crear cuenta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Información personal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre completo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirmar contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Información del negocio (opcional)").assertIsDisplayed()
    }

    @Test
    fun registerScreen_formValidation() {
        composeTestRule.setContent {
            NegocioListoTheme {
                RegisterScreen(
                    onBackClick = { },
                    onRegisterSuccess = { },
                    onLoginClick = { }
                )
            }
        }

        // El botón debe estar deshabilitado inicialmente
        composeTestRule.onNodeWithText("Crear cuenta").assertIsNotEnabled()

        // Llenar campos requeridos
        composeTestRule.onNodeWithText("Nombre completo").performTextInput("Juan Pérez")
        composeTestRule.onNodeWithText("Email").performTextInput("juan@example.com")
        composeTestRule.onAllNodesWithText("Contraseña")[0].performTextInput("password123")
        composeTestRule.onNodeWithText("Confirmar contraseña").performTextInput("password123")
        
        // Ahora el botón debe estar habilitado
        composeTestRule.onNodeWithText("Crear cuenta").assertIsEnabled()
    }

    @Test
    fun registerScreen_passwordMismatchValidation() {
        composeTestRule.setContent {
            NegocioListoTheme {
                RegisterScreen(
                    onBackClick = { },
                    onRegisterSuccess = { },
                    onLoginClick = { }
                )
            }
        }

        // Llenar contraseñas diferentes
        composeTestRule.onAllNodesWithText("Contraseña")[0].performTextInput("password123")
        composeTestRule.onNodeWithText("Confirmar contraseña").performTextInput("different")
        
        // Hacer clic fuera del campo para activar validación
        composeTestRule.onNodeWithText("Teléfono").performClick()
        
        // Verificar que se muestra error
        composeTestRule.onNodeWithText("Las contraseñas no coinciden").assertIsDisplayed()
    }

    @Test
    fun registerScreen_businessTypeSelector() {
        composeTestRule.setContent {
            NegocioListoTheme {
                RegisterScreen(
                    onBackClick = { },
                    onRegisterSuccess = { },
                    onLoginClick = { }
                )
            }
        }

        // Hacer clic en el selector de tipo de negocio
        composeTestRule.onNodeWithText("Tipo de negocio").performClick()
        
        // Verificar que se muestran opciones
        composeTestRule.onNodeWithText("Tienda/Retail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restaurante").assertIsDisplayed()
        composeTestRule.onNodeWithText("Servicios").assertIsDisplayed()
        
        // Seleccionar una opción
        composeTestRule.onNodeWithText("Tienda/Retail").performClick()
        
        // Verificar que se seleccionó
        composeTestRule.onNodeWithText("Tienda/Retail").assertIsDisplayed()
    }

    @Test
    fun registerScreen_navigationButtons() {
        var backClicked = false
        var loginClicked = false

        composeTestRule.setContent {
            NegocioListoTheme {
                RegisterScreen(
                    onBackClick = { backClicked = true },
                    onRegisterSuccess = { },
                    onLoginClick = { loginClicked = true }
                )
            }
        }

        // Probar botón de volver
        composeTestRule.onNodeWithContentDescription("Volver").performClick()
        assert(backClicked)

        // Probar enlace de iniciar sesión
        composeTestRule.onNodeWithText("Iniciar sesión").performClick()
        assert(loginClicked)
    }

    // ==========================================
    // PRUEBAS DE ACCESIBILIDAD
    // ==========================================

    @Test
    fun authScreens_haveProperContentDescriptions() {
        // Probar WelcomeScreen
        composeTestRule.setContent {
            NegocioListoTheme {
                WelcomeScreen(
                    onLoginClick = { },
                    onRegisterClick = { },
                    onGuestModeClick = { }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Logo NegocioListo").assertExists()

        // Probar LoginScreen
        composeTestRule.setContent {
            NegocioListoTheme {
                LoginScreen(
                    onBackClick = { },
                    onLoginSuccess = { },
                    onForgotPasswordClick = { },
                    onCreateAccountClick = { }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Volver").assertExists()
        composeTestRule.onNodeWithContentDescription("Mostrar contraseña").assertExists()
    }

    @Test
    fun authScreens_buttonsHaveMinimumTouchTarget() {
        composeTestRule.setContent {
            NegocioListoTheme {
                WelcomeScreen(
                    onLoginClick = { },
                    onRegisterClick = { },
                    onGuestModeClick = { }
                )
            }
        }

        // Los botones deben tener al menos 48dp de altura (tamaño mínimo de toque)
        composeTestRule.onNodeWithText("Crear cuenta gratis")
            .assertHeightIsAtLeast(48.dp)
        
        composeTestRule.onNodeWithText("Ya tengo cuenta")
            .assertHeightIsAtLeast(48.dp)
    }
}