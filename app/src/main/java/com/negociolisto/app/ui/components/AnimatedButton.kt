package com.negociolisto.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 🎬 BOTÓN ANIMADO
 * 
 * Componente de botón con micro-animaciones que incluye:
 * - Animación de escala al presionar
 * - Animación de elevación
 * - Transición suave de colores
 * - Efecto de "ripple" mejorado
 */
@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String? = null,
    icon: ImageVector? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    var isPressed by remember { mutableStateOf(false) }
    
    // Animación de escala
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(
            durationMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )
    
    // Animación de elevación
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = tween(
            durationMillis = 150,
            easing = EaseOutCubic
        ),
        label = "elevation"
    )
    
    Button(
        onClick = {
            onClick()
        },
        modifier = modifier
            .scale(scale),
        enabled = enabled,
        colors = colors,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            text?.let {
                Text(text = it)
            }
        }
    }
}

/**
 * 🎬 BOTÓN FLOTANTE ANIMADO
 * 
 * FAB con animaciones suaves y efectos visuales atractivos.
 */
@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary
) {
    var isPressed by remember { mutableStateOf(false) }
    
    // Animación de escala más pronunciada para FAB
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fab_scale"
    )
    
    // Animación de rotación sutil
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 5f else 0f,
        animationSpec = tween(200),
        label = "fab_rotation"
    )
    
    FloatingActionButton(
        onClick = {
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .rotate(rotation),
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 🎬 CHIP ANIMADO
 * 
 * Chip con animaciones sutiles para mejor feedback visual.
 */
@Composable
fun AnimatedChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "chip_scale"
    )
    
    AssistChip(
        onClick = {
            onClick()
        },
        modifier = modifier.scale(scale),
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled
    )
}

/**
 * 🎬 TARJETA ANIMADA
 * 
 * Card con animaciones de elevación y escala para mejor interacción.
 */
@Composable
fun AnimatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "card_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = tween(150),
        label = "card_elevation"
    )
    
    Card(
        onClick = {
            if (enabled) onClick()
        },
        modifier = modifier.scale(scale),
        enabled = enabled,
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
            pressedElevation = 2.dp
        )
    ) {
        content()
    }
}
