package com.negociolisto.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.GradientTokens
import com.negociolisto.app.ui.design.AnimationTokens
import com.negociolisto.app.ui.design.UnifiedButton
import com.negociolisto.app.ui.design.UnifiedOutlinedButton

/**
 * 🎨 BOTONES UNIFICADOS CON DESIGN SYSTEM
 * 
 * Componentes de botones que siguen el design system unificado
 * para mantener consistencia visual en toda la aplicación.
 */

/**
 * 🚀 BOTÓN PRIMARIO CON GRADIENTE DE MARCA
 */
@Composable
fun UnifiedPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (enabled && !loading) 1f else 0.95f,
        animationSpec = tween(AnimationTokens.shortDuration),
        label = "buttonScale"
    )
    
    Box(
        modifier = modifier
            .height(DesignTokens.buttonHeight)
            .shadow(
                elevation = if (enabled) DesignTokens.buttonElevation else 0.dp,
                shape = DesignTokens.buttonShape
            )
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(GradientTokens.brandGradient())
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                },
                shape = DesignTokens.buttonShape
            )
            .clip(DesignTokens.buttonShape)
            .clickable(enabled = enabled && !loading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = BrandColors.white,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = BrandColors.white,
                        modifier = Modifier.size(DesignTokens.iconSize)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.white,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 🎯 BOTÓN SECUNDARIO CON GRADIENTE TURQUESA
 */
@Composable
fun UnifiedSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .height(DesignTokens.buttonHeight)
            .shadow(
                elevation = if (enabled) DesignTokens.buttonElevation else 0.dp,
                shape = DesignTokens.buttonShape
            )
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(GradientTokens.secondaryGradient())
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                },
                shape = DesignTokens.buttonShape
            )
            .clip(DesignTokens.buttonShape)
            .clickable(enabled = enabled && !loading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = BrandColors.white,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = BrandColors.white,
                        modifier = Modifier.size(DesignTokens.iconSize)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.white,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 📝 BOTÓN DE CONTORNO (OUTLINE)
 */
@Composable
fun UnifiedOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(DesignTokens.buttonHeight),
        shape = DesignTokens.buttonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = DesignTokens.borderWidth
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(DesignTokens.iconSize)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 🔗 BOTÓN DE TEXTO (TEXT BUTTON)
 */
@Composable
fun UnifiedTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(DesignTokens.buttonHeight),
        shape = DesignTokens.buttonShape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(DesignTokens.iconSize)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * ➕ BOTÓN FLOTANTE UNIFICADO
 */
@Composable
fun UnifiedFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add,
    text: String? = null,
    extended: Boolean = text != null
) {
    if (extended && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier.shadow(
                elevation = DesignTokens.fabElevation,
                shape = DesignTokens.fabShape
            ),
            shape = DesignTokens.fabShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(DesignTokens.fabIconSize)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier
                .size(DesignTokens.fabSize)
                .shadow(
                    elevation = DesignTokens.fabElevation,
                    shape = DesignTokens.fabShape
                ),
            shape = DesignTokens.fabShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(DesignTokens.fabIconSize)
            )
        }
    }
}

/**
 * 📌 BARRA INFERIOR FIJA (FIXED BOTTOM BAR)
 * 
 * Componente reutilizable para botones de acción fijos en la parte inferior
 * de las pantallas. No es scrolleable y no interfiere con la navegación.
 * 
 * Usar en pantallas que requieren acciones principales visibles siempre.
 */
@Composable
fun FixedBottomBar(
    primaryButtonText: String,
    primaryButtonOnClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryButtonIcon: ImageVector? = null,
    secondaryButtonText: String? = null,
    secondaryButtonOnClick: (() -> Unit)? = null,
    secondaryButtonIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = if (secondaryButtonText != null) {
                Arrangement.spacedBy(12.dp)
            } else {
                Arrangement.Center
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón secundario (opcional, a la izquierda)
            secondaryButtonText?.let { text ->
                UnifiedOutlinedButton(
                    text = text,
                    onClick = secondaryButtonOnClick ?: {},
                    icon = secondaryButtonIcon,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Botón primario (siempre presente)
            UnifiedButton(
                text = primaryButtonText,
                onClick = primaryButtonOnClick,
                icon = primaryButtonIcon,
                isEnabled = enabled,
                modifier = if (secondaryButtonText != null) {
                    Modifier.weight(1f)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
        }
    }
}