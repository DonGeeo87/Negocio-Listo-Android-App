package com.negociolisto.app.ui.auth

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import com.negociolisto.app.R
import com.negociolisto.app.ui.components.UnifiedPrimaryButton
import com.negociolisto.app.ui.components.UnifiedSecondaryButton
import com.negociolisto.app.ui.components.UnifiedTextButton
import com.negociolisto.app.ui.components.GoogleSignInButton
import com.negociolisto.app.ui.components.GoogleSignUpButton
import com.negociolisto.app.ui.theme.NegocioListoTheme
import com.negociolisto.app.ui.design.AnimationTokens
import com.negociolisto.app.ui.design.ShadowTokens
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.domain.repository.AuthRepository
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onAlreadyLoggedIn: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    onGoogleSignUp: () -> Unit = {},
    modifier: Modifier = Modifier,
    authRepository: AuthRepository = hiltViewModel<WelcomeViewModel>().authRepository
) {
    val isAuthenticated by authRepository.isAuthenticated.collectAsStateWithLifecycle(initialValue = false)
    
    // NO verificar autenticación aquí - dejar que MainActivity maneje la lógica
    // Esto evita que se salte el flujo de registro cuando el usuario presiona "Crear cuenta"
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(AnimationTokens.longDuration, easing = AnimationTokens.decelerateEasing),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(AnimationTokens.extraLongDuration, easing = AnimationTokens.decelerateEasing),
        label = "fadeIn"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .offset(y = slideInOffset)
            .alpha(fadeInAlpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo sin fondo
            Image(
                painter = painterResource(id = R.drawable.logo_negociolisto),
                contentDescription = "Logo NegocioListo",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Negocio Listo",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tu negocio en tus manos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Descripción con look premium
            Card(
                modifier = Modifier
                .fillMaxWidth()
                .shadow(ShadowTokens.extraLarge, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con gradiente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF312783),
                                    Color(0xFF009FE3)
                                )
                            )
                        )
                        .padding(DesignTokens.cardPadding)
                ) {
                    Text(
                        text = "🚀 Gestiona tu negocio de manera fácil y eficiente",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Controla inventario, ventas, gastos y clientes desde tu celular.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Botones de acción con look premium
            Card(
                modifier = Modifier
                .fillMaxWidth()
                .shadow(ShadowTokens.large, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón principal con gradiente
                Button(
                    onClick = {
                        println("🔍 DEBUG WelcomeScreen: Botón 'Crear cuenta gratis' presionado")
                        onRegisterClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DesignTokens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF009FE3),
                                        Color(0xFF312783)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(DesignTokens.mediumIconSize)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Crear cuenta gratis",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                
                // Botón secundario
                OutlinedButton(
                    onClick = {
                        println("🔍 DEBUG WelcomeScreen: Botón 'Ya tengo cuenta' presionado")
                        onLoginClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(DesignTokens.mediumIconSize)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ya tengo cuenta",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Separador
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "o",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                
                // Botones de Google
                GoogleSignInButton(
                    onClick = onGoogleSignIn,
                    text = "Iniciar sesión con Google",
                    modifier = Modifier.fillMaxWidth()
                )
                
                GoogleSignUpButton(
                    onClick = onGoogleSignUp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Frase inspiracional con mejor contraste
            Text(
                text = "App hecha con amor por un Emprendedor para Emprendedores",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    NegocioListoTheme {
        WelcomeScreen(
            onLoginClick = { },
            onRegisterClick = { },
            onAlreadyLoggedIn = { }
        )
    }
}

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel()
