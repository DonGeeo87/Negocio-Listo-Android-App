package com.negociolisto.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * 🎨 COMPONENTE DE ANIMACIÓN DE CONTENIDO
 * 
 * Proporciona animaciones sutiles de entrada para el contenido de las pantallas,
 * eliminando la duplicidad de títulos y mejorando la experiencia visual.
 */
@Composable
fun AnimatedContent(
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        label = "contentAlpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        label = "contentScale"
    )
    
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        label = "contentOffsetY"
    )
    
    Box(
        modifier = modifier
            .alpha(alpha)
            .scale(scale)
            .offset(y = offsetY)
    ) {
        content()
    }
}

/**
 * 🎯 TÍTULO DE SECCIÓN ANIMADO
 * 
 * Título contextual que aparece con animación de entrada,
 * reemplazando títulos duplicados en el contenido.
 */
@Composable
fun AnimatedSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    AnimatedContent(
        visible = visible,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

/**
 * 📊 RESUMEN ANIMADO
 * 
 * Card de resumen que aparece con animación de entrada,
 * proporcionando contexto visual sin duplicar títulos.
 */
@Composable
fun AnimatedSummaryCard(
    title: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    AnimatedContent(
        visible = visible,
        modifier = modifier
    ) {
        AnimatedCard(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                content()
            }
        }
    }
}
