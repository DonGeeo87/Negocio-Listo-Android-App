package com.negociolisto.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.GradientTokens
import com.negociolisto.app.ui.design.AnimationTokens
import kotlinx.coroutines.delay

/**
 * 🎨 ESTADOS UNIFICADOS CON DESIGN SYSTEM
 * 
 * Componentes para estados de carga, vacío y error que siguen
 * el design system unificado para mantener consistencia visual.
 */

/**
 * ⏳ ESTADO DE CARGA UNIFICADO
 */
@Composable
fun UnifiedLoadingState(
    message: String = "Cargando...",
    modifier: Modifier = Modifier,
    showSkeleton: Boolean = false,
    skeletonCount: Int = 3
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(AnimationTokens.mediumDuration),
        label = "fadeIn"
    )
    
    if (showSkeleton) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .alpha(fadeInAlpha),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
        ) {
            repeat(skeletonCount) {
                UnifiedSkeletonItem()
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(DesignTokens.largeSpacing)
                .alpha(fadeInAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
        ) {
            // Indicador de carga con gradiente
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(
                            colors = GradientTokens.brandGradient()
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = BrandColors.white,
                    strokeWidth = 3.dp
                )
            }
            
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 📦 ESTADO VACÍO UNIFICADO
 */
@Composable
fun UnifiedEmptyState(
    title: String,
    message: String,
    icon: String = "📦",
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(AnimationTokens.longDuration, easing = EaseOutCubic),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(AnimationTokens.extraLongDuration, easing = EaseOutCubic),
        label = "fadeIn"
    )
    
    UnifiedCard(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = slideInOffset)
            .alpha(fadeInAlpha)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
        ) {
            // Icono con fondo gradiente (reducido de 100dp a 60dp)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BrandColors.lightLilac,
                                BrandColors.lightLilacVariant
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
                
                UnifiedPrimaryButton(
                    text = actionText,
                    onClick = onActionClick,
                    icon = Icons.Filled.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * ❌ ESTADO DE ERROR UNIFICADO
 */
@Composable
fun UnifiedErrorState(
    title: String = "¡Ups! Algo salió mal",
    message: String,
    actionText: String = "Reintentar",
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(AnimationTokens.mediumDuration),
        label = "fadeIn"
    )
    
    UnifiedAlertCard(
        title = title,
        message = message,
        alertType = AlertType.ERROR,
        modifier = modifier
            .fillMaxWidth()
            .alpha(fadeInAlpha),
        actions = {
            if (onActionClick != null) {
                UnifiedTextButton(
                    text = actionText,
                    onClick = onActionClick,
                    icon = Icons.Filled.Refresh
                )
            }
        }
    )
}

/**
 * 🦴 SKELETON ITEM UNIFICADO
 */
@Composable
private fun UnifiedSkeletonItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    
    UnifiedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar skeleton
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                        CircleShape
                    )
            )
            
            // Content skeleton
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                            DesignTokens.buttonShape
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.7f),
                            DesignTokens.buttonShape
                        )
                )
            }
            
            // Trailing skeleton
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(20.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                        DesignTokens.buttonShape
                    )
            )
        }
    }
}

/**
 * 📊 SKELETON PARA LISTAS DE PRODUCTOS
 */
@Composable
fun UnifiedProductSkeletonList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
    ) {
        repeat(itemCount) {
            UnifiedSkeletonItem()
        }
    }
}

/**
 * 🔄 ESTADO DE RECARGA (PULL TO REFRESH)
 */
@Composable
fun UnifiedRefreshIndicator(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    if (isRefreshing) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(DesignTokens.itemSpacing),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Actualizando...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}