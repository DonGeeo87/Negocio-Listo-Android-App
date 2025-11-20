package com.negociolisto.app.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.negociolisto.app.domain.model.InspirationTip
import com.negociolisto.app.domain.model.TimeOfDay
import com.negociolisto.app.domain.model.TipCategory
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.GradientTokens

/**
 * 🎁 CAJA SORPRESA DE TIPS E INSPIRACIÓN
 * 
 * Componente interactivo que muestra tips motivacionales con gradientes
 * dinámicos según la hora del día y animaciones elegantes.
 */
@Composable
fun InspirationBox(
    currentTip: InspirationTip?,
    currentTimeOfDay: TimeOfDay,
    isLoading: Boolean,
    onTipRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var swipeOffset by remember { mutableStateOf(0f) }
    var isDismissing by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Animación de escala para el efecto de presión
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isDragging) 0.95f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )
    
    // Animación de fade-out al descartar
    val dismissAlpha by animateFloatAsState(
        targetValue = if (isDismissing) 0f else 1f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "dismissAlpha"
    )
    
    // Animación suave del offset del swipe
    val animatedOffset by animateFloatAsState(
        targetValue = swipeOffset,
        animationSpec = tween(200, easing = EaseInOut),
        label = "swipeOffset"
    )
    
    // Obtener gradiente según la hora del día
    val gradient = when (currentTimeOfDay) {
        TimeOfDay.DAWN -> GradientTokens.dawnGradient()
        TimeOfDay.MORNING -> GradientTokens.morningGradient()
        TimeOfDay.AFTERNOON -> GradientTokens.afternoonGradient()
        TimeOfDay.NIGHT -> GradientTokens.nightGradient()
    }
    
    // Resetear estados cuando cambia el tip
    LaunchedEffect(currentTip?.id) {
        swipeOffset = 0f
        isDismissing = false
        isDragging = false
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp) // Altura fija para evitar pestañeos
            .scale(scale)
            .offset(x = animatedOffset.dp)
            .alpha(dismissAlpha)
            .pointerInput(currentTip?.id, isLoading) {
                if (currentTip != null && !isLoading) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            val currentOffset = swipeOffset
                            // Si se deslizó más de 100px, descartar
                            if (kotlin.math.abs(currentOffset) > 100f) {
                                isDismissing = true
                                // Esperar a que termine la animación antes de solicitar nuevo tip
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(300)
                                    onTipRequested()
                                }
                            } else {
                                // Resetear posición con animación
                                swipeOffset = 0f
                            }
                        }
                    ) { change, dragAmount ->
                        swipeOffset += dragAmount
                        // Limitar el desplazamiento máximo
                        swipeOffset = swipeOffset.coerceIn(-200f, 200f)
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isDragging && !isDismissing
            ) {
                if (!isDismissing && !isDragging) {
                    isPressed = true
                    onTipRequested()
                }
            },
        shape = DesignTokens.cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.cardElevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    brush = Brush.linearGradient(gradient)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.cardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con icono y badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono de sorpresa
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Caja sorpresa",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(DesignTokens.iconSize)
                    )
                    
                    // Badges de categoría y horario (categoría a la izquierda)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentTip != null) {
                            CategoryBadge(category = currentTip.category)
                        }
                        TimeOfDayBadge(currentTimeOfDay = currentTimeOfDay)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Contenido principal con animación
                if (isLoading) {
                    LoadingIndicator()
                } else {
                    AnimatedContent(
                        targetState = currentTip,
                        transitionSpec = {
                            // Transición más suave sin saltos bruscos
                            slideInHorizontally(
                                initialOffsetX = { full -> full / 3 },
                                animationSpec = tween(durationMillis = 300, easing = EaseInOut)
                            ) + fadeIn(animationSpec = tween(300, easing = EaseInOut)) togetherWith
                            slideOutHorizontally(
                                targetOffsetX = { full -> -full / 3 },
                                animationSpec = tween(durationMillis = 300, easing = EaseInOut)
                            ) + fadeOut(animationSpec = tween(300, easing = EaseInOut))
                        },
                        label = "tip_content"
                    ) { tip ->
                        if (tip != null) {
                            TipContent(tip = tip)
                        } else {
                            EmptyTipContent()
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Footer con instrucción
                Text(
                    text = if (currentTip != null) "Desliza o toca para nueva inspiración ✨" else "Toca para nueva inspiración ✨",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 📝 CONTENIDO DEL TIP
 */
@Composable
private fun TipContent(
    tip: InspirationTip,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp), // Altura aumentada para texto más grande
        contentAlignment = Alignment.Center
    ) {
        // Texto del tip (más grande y prominente)
        Text(
            text = tip.content,
            style = MaterialTheme.typography.bodyLarge, // Vuelto a bodyLarge para texto más grande
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            maxLines = 3, // Máximo 3 líneas para consistencia
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth() // Asegurar que ocupe todo el ancho disponible
        )
    }
}

/**
 * 📝 CONTENIDO VACÍO
 */
@Composable
private fun EmptyTipContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp), // Misma altura que TipContent
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "¡Preparando tu inspiración diaria!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth() // Asegurar que ocupe todo el ancho disponible
        )
    }
}

/**
 * ⏰ BADGE DE HORARIO
 */
@Composable
private fun TimeOfDayBadge(
    currentTimeOfDay: TimeOfDay,
    modifier: Modifier = Modifier
) {
    val (icon, text) = when (currentTimeOfDay) {
        TimeOfDay.DAWN -> "🌅" to "Madrugada"
        TimeOfDay.MORNING -> "🌞" to "Mañana"
        TimeOfDay.AFTERNOON -> "☀️" to "Tarde"
        TimeOfDay.NIGHT -> "🌙" to "Noche"
    }
    
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 🏷️ BADGE DE CATEGORÍA
 */
@Composable
private fun CategoryBadge(
    category: TipCategory,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.icon,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * ⏳ INDICADOR DE CARGA
 */
@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Cargando inspiración...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}
