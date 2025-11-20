package com.negociolisto.app.ui.components

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 🔐 MANEJADOR DE PERMISOS DINÁMICOS
 * 
 * Utilidad para solicitar permisos de manera dinámica cuando el usuario
 * intenta usar funciones específicas que los requieren.
 */

/**
 * Composable que maneja la solicitud de un permiso específico
 */
@Composable
fun rememberPermissionHandler(
    permission: String,
    rationaleTitle: String,
    rationaleMessage: String,
    rationaleIcon: ImageVector = Icons.Filled.Security,
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onGranted()
        } else {
            onDenied()
        }
    }
    
    if (showRationale) {
        PermissionRationaleDialog(
            title = rationaleTitle,
            message = rationaleMessage,
            icon = rationaleIcon,
            onConfirm = {
                showRationale = false
                launcher.launch(permission)
            },
            onDismiss = {
                showRationale = false
                onDenied()
            }
        )
    }
    
    return {
        when {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                permission
            ) -> {
                showRationale = true
            }
            else -> {
                launcher.launch(permission)
            }
        }
    }
}

/**
 * Diálogo que explica por qué se necesita el permiso
 */
@Composable
private fun PermissionRationaleDialog(
    title: String,
    message: String,
    icon: ImageVector,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Permitir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ahora no")
            }
        }
    )
}

/**
 * Utilidades específicas para permisos comunes
 */
object PermissionUtils {
    
    /**
     * Manejador para permiso de cámara
     */
    @Composable
    fun rememberCameraPermissionHandler(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {}
    ): () -> Unit {
        return rememberPermissionHandler(
            permission = Manifest.permission.CAMERA,
            rationaleTitle = "Permiso de Cámara",
            rationaleMessage = "Necesitamos acceso a la cámara para tomar fotos de tus productos. Esto nos ayuda a gestionar mejor tu inventario.",
            rationaleIcon = Icons.Filled.PhotoCamera,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }
    
    /**
     * Manejador para permiso de almacenamiento
     */
    @Composable
    fun rememberStoragePermissionHandler(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {}
    ): () -> Unit {
        return rememberPermissionHandler(
            permission = Manifest.permission.READ_MEDIA_IMAGES,
            rationaleTitle = "Permiso de Almacenamiento",
            rationaleMessage = "Necesitamos acceso al almacenamiento para guardar y cargar imágenes de tus productos.",
            rationaleIcon = Icons.Filled.FolderOpen,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }
    
    /**
     * Manejador para permiso de contactos
     */
    @Composable
    fun rememberContactsPermissionHandler(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {}
    ): () -> Unit {
        return rememberPermissionHandler(
            permission = Manifest.permission.READ_CONTACTS,
            rationaleTitle = "Permiso de Contactos",
            rationaleMessage = "Necesitamos acceso a tus contactos para importar clientes existentes y facilitar la gestión de tu negocio.",
            rationaleIcon = Icons.Filled.Contacts,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }
    
    /**
     * Manejador para permiso de notificaciones
     */
    @Composable
    fun rememberNotificationPermissionHandler(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {}
    ): () -> Unit {
        return rememberPermissionHandler(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            rationaleTitle = "Permiso de Notificaciones",
            rationaleMessage = "Necesitamos enviarte notificaciones para alertarte sobre stock bajo, recordatorios importantes y actualizaciones de tu negocio.",
            rationaleIcon = Icons.Filled.Notifications,
            onGranted = onGranted,
            onDenied = onDenied
        )
    }
}

/**
 * Componente que muestra información sobre permisos faltantes
 */
@Composable
fun PermissionInfoCard(
    title: String,
    message: String,
    icon: ImageVector,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Permitir")
            }
        }
    }
}










