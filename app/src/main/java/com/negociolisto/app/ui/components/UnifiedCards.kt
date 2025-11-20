package com.negociolisto.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.GradientTokens
import com.negociolisto.app.ui.design.AnimationTokens

/**
 * 🎨 TARJETAS UNIFICADAS CON DESIGN SYSTEM
 * 
 * Componentes de tarjetas que siguen el design system unificado
 * para mantener consistencia visual en toda la aplicación.
 */

/**
 * 📋 TARJETA ESTÁNDAR UNIFICADA
 */
@Composable
fun UnifiedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (enabled) DesignTokens.cardElevation else 2.dp,
        animationSpec = tween(AnimationTokens.shortDuration),
        label = "cardElevation"
    )
    
    Card(
        modifier = modifier
            .shadow(
                elevation = animatedElevation,
                shape = DesignTokens.cardShape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled) { onClick() }
                } else Modifier
            ),
        shape = DesignTokens.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            content = content
        )
    }
}

/**
 * 🎯 TARJETA CON GRADIENTE DE ENCABEZADO
 */
@Composable
fun UnifiedGradientHeaderCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    headerIcon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = DesignTokens.cardElevation,
                shape = DesignTokens.cardShape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        shape = DesignTokens.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Header con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(GradientTokens.brandGradient())
                    )
                    .padding(DesignTokens.cardPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        headerIcon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = BrandColors.white,
                                modifier = Modifier.size(DesignTokens.iconSize)
                            )
                        }
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.white,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            subtitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BrandColors.white.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                        content = actions
                    )
                }
            }
            
            // Contenido
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                content = content
            )
        }
    }
}

/**
 * 📊 TARJETA DE ESTADÍSTICA
 */
@Composable
fun UnifiedStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    UnifiedCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo circular
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            iconColor.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(DesignTokens.iconSize)
                    )
                }
            }
            
            // Contenido de la estadística
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * ⚠️ TARJETA DE ALERTA
 */
@Composable
fun UnifiedAlertCard(
    title: String,
    message: String,
    alertType: AlertType = AlertType.WARNING,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val (backgroundColor, iconColor, textColor) = when (alertType) {
        AlertType.INFO -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onSurface
        )
        AlertType.SUCCESS -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.1f),
            Color(0xFF4CAF50),
            MaterialTheme.colorScheme.onSurface
        )
        AlertType.WARNING -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.1f),
            Color(0xFFFF9800),
            MaterialTheme.colorScheme.onSurface
        )
        AlertType.ERROR -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onSurface
        )
    }
    
    Card(
        modifier = modifier
            .shadow(
                elevation = DesignTokens.cardElevation,
                shape = DesignTokens.cardShape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        shape = DesignTokens.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                    content = actions
                )
            }
        }
    }
}

/**
 * 🏷️ TIPOS DE ALERTA
 */
enum class AlertType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

/**
 * 📱 TARJETA DE LISTA DE ELEMENTOS
 */
@Composable
fun UnifiedListItemCard(
    title: String,
    subtitle: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = DesignTokens.cardShape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled) { onClick() }
                } else Modifier
            ),
        shape = DesignTokens.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.itemSpacing),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenido inicial (ícono, imagen, etc.)
            leadingContent?.invoke()
            
            // Contenido principal
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Contenido final (precio, acciones, etc.)
            trailingContent?.invoke()
        }
    }
}