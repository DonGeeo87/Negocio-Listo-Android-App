package com.negociolisto.app.ui.components

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.negociolisto.app.ui.design.*
import com.negociolisto.app.ui.design.ShadowTokens

/**
 * 🍞 COMPONENTE DE NOTIFICACIÓN TOAST
 * 
 * Componente para mostrar notificaciones toast con diferentes tipos:
 * - Success (éxito)
 * - Error (error)
 * - Warning (advertencia)
 * - Info (información)
 */
@Composable
fun ToastNotification(
    message: String,
    type: ToastType = ToastType.INFO,
    isVisible: Boolean = true,
    duration: Long = 3000L,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showToast by remember { mutableStateOf(isVisible) }
    
    // Auto-dismiss después del tiempo especificado
    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(duration)
            showToast = false
            onDismiss()
        }
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (showToast) 1f else 0f,
        animationSpec = tween(300),
        label = "toastAlpha"
    )
    
    if (showToast) {
        androidx.compose.ui.window.Popup(
            alignment = androidx.compose.ui.Alignment.BottomCenter,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(0.9f)
                    .alpha(alpha)
                    .shadow(ShadowTokens.extraLarge, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = when (type) {
                        ToastType.SUCCESS -> BrandColors.turquoise
                        ToastType.ERROR -> MaterialTheme.colorScheme.error
                        ToastType.WARNING -> MaterialTheme.colorScheme.tertiary
                        ToastType.INFO -> BrandColors.blueLilac
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icono del tipo de toast
                    Icon(
                        imageVector = when (type) {
                            ToastType.SUCCESS -> Icons.Filled.CheckCircle
                            ToastType.ERROR -> Icons.Filled.Error
                            ToastType.WARNING -> Icons.Filled.Warning
                            ToastType.INFO -> Icons.Filled.Info
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    // Mensaje
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Botón de cerrar
                    IconButton(
                        onClick = {
                            showToast = false
                            onDismiss()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🎯 TIPOS DE TOAST
 */
enum class ToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

/**
 * 🍞 TOAST SIMPLE
 * 
 * Versión simplificada para casos básicos.
 */
@Composable
fun SimpleToast(
    message: String,
    type: ToastType = ToastType.INFO,
    modifier: Modifier = Modifier
) {
    ToastNotification(
        message = message,
        type = type,
        modifier = modifier
    )
}

/**
 * 🎨 TOAST PERSONALIZADO
 * 
 * Versión con más opciones de personalización.
 */
@Composable
fun CustomToast(
    message: String,
    icon: ImageVector? = null,
    backgroundColor: Color = BrandColors.blueLilac,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    duration: Long = 3000L,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showToast by remember { mutableStateOf(true) }
    
    // Auto-dismiss después del tiempo especificado
    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(duration)
            showToast = false
            onDismiss()
        }
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (showToast) 1f else 0f,
        animationSpec = tween(300),
        label = "toastAlpha"
    )
    
    if (showToast) {
        androidx.compose.ui.window.Popup(
            alignment = androidx.compose.ui.Alignment.BottomCenter,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(0.9f)
                    .alpha(alpha)
                    .shadow(ShadowTokens.extraLarge, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icono personalizado
                    icon?.let { iconVector ->
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Mensaje
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Botón de cerrar
                    IconButton(
                        onClick = {
                            showToast = false
                            onDismiss()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Toast Notifications**: Notificaciones temporales que aparecen en la parte inferior
 * 2. **Auto-dismiss**: Se cierran automáticamente después de un tiempo
 * 3. **Manual dismiss**: Se pueden cerrar manualmente con el botón X
 * 4. **Type-based styling**: Diferentes estilos según el tipo de mensaje
 * 5. **Animation**: Transiciones suaves de entrada y salida
 * 
 * ANALOGÍA:
 * 
 * ToastNotification es como una "notificación push" en el teléfono:
 * 
 * 1. **Aparece automáticamente**: Cuando hay algo importante que comunicar
 * 2. **Se desvanece solo**: Después de un tiempo para no molestar
 * 3. **Se puede cerrar**: Si el usuario quiere cerrarla antes
 * 4. **Diferentes tipos**: Verde para éxito, rojo para error, etc.
 * 5. **No bloquea la UI**: Permite continuar usando la app
 * 
 * CASOS DE USO REALES:
 * - ✅ "Producto guardado exitosamente"
 * - ❌ "Error al conectar con el servidor"
 * - ⚠️ "Stock bajo en producto X"
 * - ℹ️ "Sincronización completada"
 * 
 * CARACTERÍSTICAS:
 * ✅ Auto-dismiss configurable
 * ✅ Diferentes tipos visuales
 * ✅ Animaciones suaves
 * ✅ Botón de cerrar manual
 * ✅ Posicionamiento en bottom center
 * ✅ Responsive y accesible
 * ✅ Personalizable
 */

