package com.negociolisto.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * 🎨 SIDEBAR MODERNO
 * 
 * Sidebar deslizable con animaciones suaves y diseño moderno.
 * Incluye:
 * - Animación de deslizamiento
 * - Gradiente de fondo
 * - Iconos con estados
 * - Efectos de hover/press
 * - Categorización de elementos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSidebar(
    isOpen: Boolean,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    val slideOffset by animateDpAsState(
        targetValue = if (isOpen) 0.dp else (-280).dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseOutCubic
        ),
        label = "sidebar_slide"
    )
    
    @Suppress("UNUSED_VARIABLE")
    val alpha by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = tween(200),
        label = "sidebar_alpha"
    )
    
    // Overlay para cerrar el sidebar
    if (isOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                .clickable { onClose() }
                .zIndex(1f)
        )
    }
    
    // Sidebar
    Card(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .offset(x = slideOffset)
            .zIndex(2f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 16.dp
        ),
        shape = RoundedCornerShape(
            topEnd = 0.dp,
            bottomEnd = 24.dp,
            topStart = 0.dp,
            bottomStart = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // Header del sidebar
            ModernSidebarHeader(
                onClose = onClose,
                modifier = Modifier.padding(top = 16.dp, start = 12.dp, end = 12.dp, bottom = 6.dp)
            )
            
            // Navegación principal
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                // Sección principal
                item {
                    SidebarSection(
                        title = "Principal",
                        items = listOf(
                            SidebarItem(
                                route = "dashboard",
                                title = "Dashboard",
                                icon = Icons.Filled.Dashboard,
                                selectedIcon = Icons.Filled.Dashboard
                            )
                        ),
                        currentRoute = currentRoute,
                        onNavigate = onNavigate
                    )
                }
                
                // Sección de gestión
                item {
                    SidebarSection(
                        title = "Gestión",
                        items = listOf(
                            SidebarItem(
                                route = "inventory",
                                title = "Inventario",
                                icon = Icons.Outlined.Inventory2,
                                selectedIcon = Icons.Filled.Inventory2
                            ),
                            SidebarItem(
                                route = "customers",
                                title = "Clientes",
                                icon = Icons.Outlined.Groups,
                                selectedIcon = Icons.Filled.Groups
                            ),
                            SidebarItem(
                                route = "collections",
                                title = "Colecciones",
                                icon = Icons.Outlined.PhotoLibrary,
                                selectedIcon = Icons.Filled.PhotoLibrary
                            )
                        ),
                        currentRoute = currentRoute,
                        onNavigate = onNavigate
                    )
                }
                
                // Sección financiera
                item {
                    SidebarSection(
                        title = "Finanzas",
                        items = listOf(
                            SidebarItem(
                                route = "sales",
                                title = "Ventas",
                                icon = Icons.Outlined.PointOfSale,
                                selectedIcon = Icons.Filled.PointOfSale
                            ),
                            SidebarItem(
                                route = "invoices",
                                title = "Facturas",
                                icon = Icons.Outlined.Description,
                                selectedIcon = Icons.Filled.Description
                            ),
                            SidebarItem(
                                route = "expenses",
                                title = "Gastos",
                                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                                selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong
                            ),
                            SidebarItem(
                                route = "reports",
                                title = "Reportes",
                                icon = Icons.Outlined.Assessment,
                                selectedIcon = Icons.Filled.Assessment
                            ),
                            SidebarItem(
                                route = "tools",
                                title = "Herramientas",
                                icon = Icons.Outlined.Build,
                                selectedIcon = Icons.Filled.Build
                            )
                        ),
                        currentRoute = currentRoute,
                        onNavigate = onNavigate
                    )
                }
                
                // Sección de organización
                item {
                    SidebarSection(
                        title = "Organización",
                        items = listOf(
                            SidebarItem(
                                route = "settings",
                                title = "Ajustes",
                                icon = Icons.Outlined.Settings,
                                selectedIcon = Icons.Filled.Settings
                            )
                        ),
                        currentRoute = currentRoute,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernSidebarHeader(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏢 NegocioListo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Gestión Empresarial",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SidebarSection(
    title: String,
    items: List<SidebarItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        
        items.forEach { item ->
            ModernSidebarItem(
                item = item,
                isSelected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

@Composable
private fun ModernSidebarItem(
    item: SidebarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150),
        label = "sidebar_item_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "sidebar_item_bg"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "sidebar_item_icon"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "sidebar_item_text"
    )
    
    Card(
        onClick = {
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (isSelected) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.icon,
                    contentDescription = item.title,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

data class SidebarItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)
