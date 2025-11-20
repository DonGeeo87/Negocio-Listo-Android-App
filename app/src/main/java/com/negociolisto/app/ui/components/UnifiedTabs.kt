package com.negociolisto.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens

/**
 * 🎯 SISTEMA DE TABS UNIFICADO
 * 
 * Componentes de tabs que siguen el design system unificado
 * para mantener consistencia visual en toda la aplicación.
 */

/**
 * 📑 TAB ROW UNIFICADO SIMPLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedTabRow(
    tabs: List<UnifiedTabData>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.padding(DesignTokens.smallSpacing),
                text = { 
                    Text(
                        text = tab.title,
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                icon = tab.icon?.let { 
                    { 
                        Icon(
                            imageVector = it, 
                            contentDescription = null,
                            modifier = Modifier.size(DesignTokens.iconSize)
                        ) 
                    } 
                }
            )
        }
    }
}

/**
 * 📋 CONTENEDOR DE TABS CON CONTENIDO
 */
@Composable
fun UnifiedTabContainer(
    tabs: List<UnifiedTabData>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    Column(modifier = modifier) {
        // Renderizar tabs
        UnifiedTabRow(
            tabs = tabs,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected
        )
        
        // Contenido del tab seleccionado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content(selectedTabIndex)
        }
    }
}

/**
 * 📊 DATOS PARA TABS UNIFICADOS
 */
data class UnifiedTabData(
    val title: String,
    val icon: ImageVector? = null,
    val badge: String? = null
)