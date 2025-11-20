package com.negociolisto.app.ui.setup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.ui.components.UnifiedPrimaryButton
import com.negociolisto.app.ui.components.UnifiedOutlineButton
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.components.UnifiedGradientHeaderCard
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.GradientTokens

/**
 * 📧 PANTALLA DE VERIFICACIÓN DE EMAIL
 * 
 * Pantalla que permite al usuario verificar su email después del registro.
 * Es opcional - el usuario puede saltarla y continuar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    onVerified: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmailVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono de email moderno con gradiente
            var isAnimating by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isAnimating) 1.1f else 1f,
                animationSpec = tween(1000, delayMillis = 500),
                label = "emailScale"
            )
            
            LaunchedEffect(Unit) {
                isAnimating = true
            }
            
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(70.dp))
                    .scale(scale)
                    .background(
                        brush = Brush.horizontalGradient(GradientTokens.brandGradient()),
                        shape = RoundedCornerShape(70.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                    tint = BrandColors.white
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Título moderno
            UnifiedGradientHeaderCard(
                title = "Verifica tu Email",
                subtitle = "Te enviamos un correo de verificación a:",
                modifier = Modifier.fillMaxWidth()
            ) {}
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email del usuario moderno
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.email,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Información moderna sobre la verificación
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "¿Por qué verificar?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "La verificación te permite recuperar tu cuenta si olvidas la contraseña y acceder a funciones avanzadas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Botones modernos de acción
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón principal moderno - Ya verifiqué
                UnifiedPrimaryButton(
                    text = if (uiState.isChecking) "Verificando..." else "Ya verifiqué mi correo",
                    onClick = {
                        viewModel.checkVerification(onVerified)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isChecking,
                    loading = uiState.isChecking,
                    icon = Icons.Filled.CheckCircle
                )
                
                // Botón secundario moderno - Reenviar
                UnifiedOutlineButton(
                    text = if (uiState.isSending) "Enviando..." else "Reenviar correo",
                    onClick = {
                        viewModel.resendVerificationEmail()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSending,
                    loading = uiState.isSending,
                    icon = Icons.Filled.Refresh
                )
                
                // Botón terciario moderno - Omitir
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Verificar después",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mensaje de estado moderno
            if (uiState.message.isNotEmpty()) {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isError) Icons.Filled.Error else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (uiState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
