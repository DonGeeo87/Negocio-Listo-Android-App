package com.negociolisto.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/**
 * 🎨 DIÁLOGOS MODERNOS
 * 
 * Componentes de diálogos modernos con animaciones suaves y diseño atractivo.
 * Incluye:
 * - AlertDialog moderno
 * - ImagePickerDialog
 * - ConfirmationDialog
 * - LoadingDialog
 * - SuccessDialog
 * - ErrorDialog
 */

// 🚨 DIÁLOGO DE ALERTA MODERNO
@Composable
fun ModernAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String = "Confirmar",
    dismissButtonText: String? = null,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    icon: String = "⚠️",
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "dialog_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "dialog_alpha"
    )
    
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha)
                .scale(scale)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Icono
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                iconColor.copy(alpha = 0.1f),
                                RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = icon,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                    
                    // Título
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    // Texto
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                    
                    // Botones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (dismissButtonText != null) {
                            ModernDialogButton(
                                text = dismissButtonText,
                                onClick = {
                                    onDismiss()
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(1f),
                                isSecondary = true
                            )
                        }
                        
                        ModernDialogButton(
                            text = confirmButtonText,
                            onClick = {
                                onConfirm()
                                onDismissRequest()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// 📸 DIÁLOGO DE SELECCIÓN DE IMAGEN MODERNO
@Composable
fun ModernImagePickerDialog(
    onDismissRequest: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "dialog_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "dialog_alpha"
    )
    
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha)
                .scale(scale)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Icono principal
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📸",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                    
                    // Título
                    Text(
                        text = "Seleccionar Imagen",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    // Texto descriptivo
                    Text(
                        text = "¿Cómo quieres cambiar tu imagen?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    // Botones de opción
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernImageOptionButton(
                            text = "Galería",
                            icon = "🖼️",
                            onClick = {
                                onGalleryClick()
                                onDismissRequest()
                            }
                        )
                        
                        ModernImageOptionButton(
                            text = "Cámara",
                            icon = "📷",
                            onClick = {
                                onCameraClick()
                                onDismissRequest()
                            }
                        )
                    }
                    
                    // Botón cancelar
                    ModernDialogButton(
                        text = "Cancelar",
                        onClick = onDismissRequest,
                        isSecondary = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ✅ DIÁLOGO DE CONFIRMACIÓN MODERNO
@Composable
fun ModernConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    cancelText: String = "Cancelar",
    onConfirm: () -> Unit,
    onCancel: () -> Unit = {},
    icon: String = "❓",
    iconColor: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    ModernAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        confirmButtonText = confirmText,
        dismissButtonText = cancelText,
        onConfirm = onConfirm,
        onDismiss = onCancel,
        icon = icon,
        iconColor = iconColor,
        modifier = modifier
    )
}

// ⏳ DIÁLOGO DE CARGA MODERNO
@Composable
fun ModernLoadingDialog(
    message: String = "Cargando...",
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// 🎉 DIÁLOGO DE ÉXITO MODERNO
@Composable
fun ModernSuccessDialog(
    onDismissRequest: () -> Unit,
    title: String = "¡Éxito!",
    message: String,
    buttonText: String = "Continuar",
    onButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModernAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        confirmButtonText = buttonText,
        dismissButtonText = null,
        onConfirm = onButtonClick,
        icon = "✅",
        iconColor = Color(0xFF4CAF50),
        modifier = modifier
    )
}

// ❌ DIÁLOGO DE ERROR MODERNO
@Composable
fun ModernErrorDialog(
    onDismissRequest: () -> Unit,
    title: String = "Error",
    message: String,
    buttonText: String = "Entendido",
    onButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModernAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        confirmButtonText = buttonText,
        dismissButtonText = null,
        onConfirm = onButtonClick,
        icon = "❌",
        iconColor = MaterialTheme.colorScheme.error,
        modifier = modifier
    )
}

// 🔧 BOTÓN MODERNO PARA DIÁLOGOS
@Composable
private fun ModernDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSecondary: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSecondary) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSecondary) {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    }
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSecondary) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 🖼️ BOTÓN DE OPCIÓN DE IMAGEN MODERNO
@Composable
private fun ModernImageOptionButton(
    text: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
