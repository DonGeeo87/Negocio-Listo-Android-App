package com.negociolisto.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * 🎬 TRANSICIONES DE PANTALLA
 * 
 * Componentes para transiciones suaves entre pantallas y elementos.
 */

/**
 * Transición de desvanecimiento con escala
 */
@Composable
fun FadeScaleTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300, easing = EaseOutCubic)
        ),
        exit = fadeOut(
            animationSpec = tween(200, easing = EaseInCubic)
        ) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200, easing = EaseInCubic)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Transición de deslizamiento desde abajo
 */
@Composable
fun SlideUpTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(400, easing = EaseOutCubic)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(300, easing = EaseInCubic)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Transición de deslizamiento desde la derecha
 */
@Composable
fun SlideInFromRightTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(350, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(350, easing = EaseOutCubic)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(250, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(250, easing = EaseInCubic)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Animación de carga con pulso
 */
@Composable
fun PulsingLoader(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .alpha(alpha)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 3.dp
        )
    }
}

/**
 * Animación de éxito con checkmark
 */
@Composable
fun SuccessAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {}
) {
    var showCheckmark by remember { mutableStateOf(false) }
    
    LaunchedEffect(visible) {
        if (visible) {
            kotlinx.coroutines.delay(200)
            showCheckmark = true
            kotlinx.coroutines.delay(1000)
            onAnimationComplete()
        } else {
            showCheckmark = false
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        exit = scaleOut(
            targetScale = 0f,
            animationSpec = tween(200)
        ) + fadeOut(
            animationSpec = tween(200)
        ),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showCheckmark) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Éxito",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "¡Operación exitosa!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Animación de lista con stagger
 */
@Composable
fun <T> AnimatedList(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: T, index: Int) -> Unit
) {
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            var visible by remember(index) { mutableStateOf(false) }
            LaunchedEffect(index) {
                kotlinx.coroutines.delay(index * 80L)
                visible = true
            }
            FadeScaleTransition(
                visible = visible,
                modifier = Modifier.fillMaxWidth()
            ) {
                itemContent(item, index)
            }
        }
    }
}
/**
 * 🎨 TRANSICIONES UNIFICADAS CON DESIGN SYSTEM
 */

/**
 * ✨ TRANSICIÓN DE PANTALLA UNIFICADA
 */
@Composable
fun UnifiedScreenTransition(
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    delayMillis: Long = 100,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(visible) {
        if (visible) {
            kotlinx.coroutines.delay(delayMillis)
            isVisible = true
        } else {
            isVisible = false
        }
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(
            durationMillis = com.negociolisto.app.ui.design.AnimationTokens.longDuration,
            easing = com.negociolisto.app.ui.design.AnimationTokens.decelerateEasing
        ),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = com.negociolisto.app.ui.design.AnimationTokens.extraLongDuration,
            easing = com.negociolisto.app.ui.design.AnimationTokens.decelerateEasing
        ),
        label = "fadeIn"
    )
    
    Box(
        modifier = modifier
            .offset(y = slideInOffset)
            .alpha(fadeInAlpha)
    ) {
        content()
    }
}

/**
 * 🎯 ANIMACIÓN STAGGERED UNIFICADA
 */
@Composable
fun <T> UnifiedStaggeredList(
    items: List<T>,
    modifier: Modifier = Modifier,
    staggerDelayMillis: Long = 80L,
    itemContent: @Composable (item: T, index: Int, animatedModifier: Modifier) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(com.negociolisto.app.ui.design.DesignTokens.itemSpacing)
    ) {
        items.forEachIndexed { index, item ->
            var visible by remember(index) { mutableStateOf(false) }
            
            LaunchedEffect(index) {
                kotlinx.coroutines.delay(index * staggerDelayMillis)
                visible = true
            }
            
            val slideInOffset by animateDpAsState(
                targetValue = if (visible) 0.dp else 20.dp,
                animationSpec = tween(
                    durationMillis = com.negociolisto.app.ui.design.AnimationTokens.mediumDuration,
                    easing = com.negociolisto.app.ui.design.AnimationTokens.decelerateEasing
                ),
                label = "staggerSlide$index"
            )
            
            val fadeInAlpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(
                    durationMillis = com.negociolisto.app.ui.design.AnimationTokens.longDuration,
                    easing = com.negociolisto.app.ui.design.AnimationTokens.decelerateEasing
                ),
                label = "staggerFade$index"
            )
            
            itemContent(
                item,
                index,
                Modifier
                    .offset(y = slideInOffset)
                    .alpha(fadeInAlpha)
            )
        }
    }
}