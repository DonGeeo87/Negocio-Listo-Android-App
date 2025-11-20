package com.negociolisto.app.ui.auth

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import com.negociolisto.app.ui.design.AnimationTokens
import kotlinx.coroutines.delay

/**
 * 🔄 PANTALLA DE RESTAURACIÓN DE DATOS
 * 
 * Muestra una animación de carga mientras se restauran los datos del usuario
 * desde Firebase después de iniciar sesión.
 */
@Composable
fun DataRestorationScreen(
    onRestorationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataRestorationViewModel = hiltViewModel()
) {
    val restorationState by viewModel.restorationState.collectAsState()
    
    // Iniciar restauración automáticamente
    LaunchedEffect(Unit) {
        viewModel.startRestoration()
    }
    
    // Navegar cuando la restauración esté completa
    LaunchedEffect(restorationState.isComplete) {
        if (restorationState.isComplete && !restorationState.hasError) {
            delay(500) // Breve pausa antes de navegar
            onRestorationComplete()
        }
    }
    
    // Animación de pulso suave
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
    
    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { 
        delay(100)
        visible = true 
    }
    
    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(AnimationTokens.mediumDuration, easing = AnimationTokens.decelerateEasing),
        label = "logo_scale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(AnimationTokens.mediumDuration, easing = AnimationTokens.decelerateEasing),
        label = "logo_alpha"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(AnimationTokens.longDuration, delayMillis = 150, easing = AnimationTokens.decelerateEasing),
        label = "content_alpha"
    )

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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Logo con efecto de brillo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Efecto de brillo de fondo
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
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Título
            Text(
                text = "Restaurando tus datos",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mensaje de estado
            Text(
                text = restorationState.statusMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Barra de progreso
            LinearProgressIndicator(
                progress = { restorationState.progress },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(8.dp)
                    .alpha(contentAlpha),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Porcentaje de progreso
            Text(
                text = "${(restorationState.progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(contentAlpha)
            )
            
            // Mensaje de error si existe
            if (restorationState.hasError) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠️ ${restorationState.errorMessage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón para continuar aunque haya error
                Button(
                    onClick = onRestorationComplete,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Continuar de todas formas")
                }
            }
        }
    }
}

