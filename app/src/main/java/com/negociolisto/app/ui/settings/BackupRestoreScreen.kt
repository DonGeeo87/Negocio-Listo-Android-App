package com.negociolisto.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.ui.components.UnifiedGradientHeaderCard
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import androidx.compose.material3.MaterialTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🗃️ PANTALLA DE BACKUP Y RESTAURACIÓN - FUNCIONAL
 * 
 * Pantalla que permite gestionar backups manuales y restauraciones,
 * además de mostrar el estado del sistema de backup automático.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backupStatus by viewModel.backupStatus.collectAsStateWithLifecycle()
    val backupProgress by viewModel.backupProgress.collectAsStateWithLifecycle()
    
    var showRestoreConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ModernFormTopAppBar(
                title = "🗃️ Backup & Respaldo",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = DesignTokens.cardPadding)
                .padding(vertical = DesignTokens.itemSpacing),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
        ) {
            // Estado del sistema
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Estado del Sistema",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                                ) {
                                    Icon(
                                        imageVector = if (state.isBackupActive) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                        contentDescription = null,
                                        tint = if (state.isBackupActive) BrandColors.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(DesignTokens.smallIconSize)
                                    )
                                    Text(
                                        text = if (state.isBackupActive) "Backup activo" else "Backup inactivo",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
                                Text(
                                    text = "Último backup: ${formatDate(state.lastBackupDate ?: 0)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Mensaje de estado actual (si hay)
            if (backupStatus.isNotBlank()) {
                item {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.cardPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                        ) {
                            Icon(
                                imageVector = when {
                                    backupStatus.contains("✅") || backupStatus.contains("🎉") -> Icons.Filled.CheckCircle
                                    backupStatus.contains("❌") || backupStatus.contains("Error") -> Icons.Filled.Error
                                    else -> Icons.Filled.Info
                                },
                                contentDescription = null,
                                tint = when {
                                    backupStatus.contains("✅") || backupStatus.contains("🎉") -> BrandColors.primary
                                    backupStatus.contains("❌") || backupStatus.contains("Error") -> MaterialTheme.colorScheme.error
                                    else -> BrandColors.primary
                                },
                                modifier = Modifier.size(DesignTokens.iconSize)
                            )
                            Text(
                                text = backupStatus,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Progreso del backup
            if (state.isBackingUp && backupProgress > 0f) {
                item {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignTokens.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                        ) {
                            Text(
                                text = "Respaldando datos...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            LinearProgressIndicator(
                                progress = { backupProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = BrandColors.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${(backupProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Progreso de restauración
            if (state.isRestoring && state.restoreProgress > 0f) {
                item {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignTokens.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                        ) {
                            Text(
                                text = "Restaurando datos...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            LinearProgressIndicator(
                                progress = { state.restoreProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = BrandColors.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${(state.restoreProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Acciones: Backup manual
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Backup,
                                contentDescription = null,
                                tint = BrandColors.primary,
                                modifier = Modifier.size(DesignTokens.iconSize)
                            )
                            Text(
                                text = "Backup Manual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Crea una copia de seguridad manual de todos tus datos en Firebase.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { viewModel.performManualBackup() },
                            enabled = !state.isBackingUp && !state.isRestoring,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.primary
                            )
                        ) {
                            if (state.isBackingUp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(DesignTokens.smallIconSize),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                            }
                            Icon(
                                imageVector = Icons.Filled.Backup,
                                contentDescription = null,
                                modifier = Modifier.size(DesignTokens.smallIconSize)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                            Text(if (state.isBackingUp) "Respaldando..." else "Crear Backup Ahora")
                        }
                    }
                }
            }

            // Acciones: Restaurar
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restore,
                                contentDescription = null,
                                tint = BrandColors.secondary,
                                modifier = Modifier.size(DesignTokens.iconSize)
                            )
                            Text(
                                text = "Restaurar Datos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(DesignTokens.smallIconSize)
                            )
                            Text(
                                text = "La restauración reemplazará todos los datos locales con los datos del backup.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Button(
                            onClick = { showRestoreConfirmation = true },
                            enabled = !state.isBackingUp && !state.isRestoring,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            if (state.isRestoring) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(DesignTokens.smallIconSize),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                                Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                            }
                            Icon(
                                imageVector = Icons.Filled.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(DesignTokens.smallIconSize)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                            Text(if (state.isRestoring) "Restaurando..." else "Restaurar desde Firebase")
                        }
                    }
                }
            }

            // Información
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = BrandColors.primary,
                                modifier = Modifier.size(DesignTokens.iconSize)
                            )
                            Text(
                                text = "Información Importante",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        CompactInfoItem(
                            icon = Icons.Filled.Cloud,
                            text = "Los datos se respaldan automáticamente en Firebase"
                        )
                        
                        CompactInfoItem(
                            icon = Icons.Filled.Restore,
                            text = "La restauración reemplaza todos los datos locales"
                        )
                        
                        CompactInfoItem(
                            icon = Icons.Filled.PhotoCamera,
                            text = "Las fotos se incluyen en el backup automáticamente"
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de restauración
    if (showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmation = false },
            title = {
                Text(
                    text = "¿Restaurar Datos?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Esta acción reemplazará todos los datos locales con los datos del último backup en Firebase. Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmation = false
                        viewModel.restoreFromFirebase()
                    }
                ) {
                    Text("Restaurar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreConfirmation = false }
                ) {
                    Text("Cancelar")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }

    // Mostrar errores
    state.error?.let { error ->
        LaunchedEffect(error) {
            // El error se muestra en el mensaje de estado
        }
    }
}

/**
 * 🔧 COMPONENTES AUXILIARES
 */

@Composable
private fun CompactInfoItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandColors.primary,
            modifier = Modifier.size(DesignTokens.smallIconSize)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(timestamp: Long): String {
    return if (timestamp > 0) {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(Date(timestamp))
    } else {
        "Nunca"
    }
}