package com.negociolisto.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 🎯 TOP APP BAR MODERNO (LEGACY)
 * 
 * Wrappers para mantener compatibilidad con el código existente.
 * Usan los nuevos UnifiedTopAppBar internamente.
 */

// 🏠 TOP APP BAR PRINCIPAL (Dashboard)
@Composable
fun ModernMainTopAppBar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedDashboardTopAppBar(
        title = "🏠 NegocioListo",
        subtitle = "Panel de Control",
        modifier = modifier,
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menú",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}


// 📋 TOP APP BAR PARA LISTAS
@Composable
fun ModernListTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    UnifiedListTopAppBar(
        title = title,
        modifier = modifier,
        onNavigationClick = onBackClick,
        actions = actions
    )
}

// 📝 TOP APP BAR PARA FORMULARIOS
@Composable
fun ModernFormTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onSaveClick: (() -> Unit)? = null,
    saveEnabled: Boolean = true,
    onHelpClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    UnifiedFormTopAppBar(
        title = title,
        modifier = modifier,
        onNavigationClick = onBackClick,
        onSaveClick = onSaveClick,
        saveEnabled = saveEnabled,
        onHelpClick = onHelpClick,
        onMenuClick = onMenuClick,
        actions = actions
    )
}

// 🎨 TOP APP BAR CON GRADIENTE
@Composable
fun ModernGradientTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    UnifiedGradientTopAppBar(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        onNavigationClick = onBackClick,
        actions = actions
    )
}