package com.negociolisto.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import com.negociolisto.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import com.negociolisto.app.ui.design.AnimationTokens
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@HiltViewModel
class SplashViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel()

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isAuthenticated by viewModel.authRepository.isAuthenticated.collectAsStateWithLifecycle(initialValue = false)
    val currentUser by viewModel.authRepository.currentUser.collectAsStateWithLifecycle(initialValue = null)

    // Animación de pulso suave para el efecto de brillo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = AnimationTokens.decelerateEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Animación sutil de entrada únicamente (evita doble splash con el nativo)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(AnimationTokens.mediumDuration, easing = AnimationTokens.decelerateEasing),
        label = "logo_scale_once"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(AnimationTokens.mediumDuration, easing = AnimationTokens.decelerateEasing),
        label = "logo_alpha_once"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(AnimationTokens.longDuration, delayMillis = 150, easing = AnimationTokens.decelerateEasing),
        label = "text_alpha_once"
    )
    val textScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(AnimationTokens.longDuration, delayMillis = 150, easing = AnimationTokens.decelerateEasing),
        label = "text_scale_once"
    )

    // Navegar tan pronto tengamos estado, con breve espera mínima para pre-12
    LaunchedEffect(isAuthenticated) {
        delay(300)
        
        println("🔍 DEBUG SplashScreen: isAuthenticated=$isAuthenticated")
        println("🔍 DEBUG SplashScreen: currentUser=$currentUser")
        
        if (isAuthenticated) {
            println("✅ DEBUG SplashScreen: Usuario autenticado, navegando a main")
            onNavigateToMain()
        } else {
            println("❌ DEBUG SplashScreen: No hay usuario autenticado, navegando a welcome")
            onNavigateToWelcome()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo con efecto de brillo suave - sin sombra
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Efecto de brillo de fondo (glow)
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale * logoScale)
                        .alpha(logoAlpha * 0.3f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Logo principal
                Image(
                    painter = painterResource(id = com.negociolisto.app.R.drawable.logo_negociolisto),
                    contentDescription = "Logo NegocioListo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .graphicsLayer {
                            // Efecto de brillo sutil en el logo
                            alpha = logoAlpha
                        }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Título con animación
            Text(
                text = "NegocioListo",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha)
                    .scale(textScale)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tu negocio en tus manos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha)
                    .scale(textScale)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Indicador de carga
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
}



