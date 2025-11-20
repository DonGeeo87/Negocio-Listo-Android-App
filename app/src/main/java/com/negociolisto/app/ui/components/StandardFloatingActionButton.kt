package com.negociolisto.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 🎯 BOTÓN FLOTANTE ESTÁNDAR (LEGACY)
 * 
 * Wrapper para mantener compatibilidad con el código existente.
 * Usa el nuevo UnifiedFloatingActionButton internamente.
 */
@Composable
fun StandardFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add,
    text: String? = null,
    containerColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.primary
) {
    UnifiedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        text = text,
        extended = text != null
    )
}