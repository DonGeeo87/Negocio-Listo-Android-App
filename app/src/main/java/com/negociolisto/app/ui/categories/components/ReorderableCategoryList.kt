package com.negociolisto.app.ui.categories.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.domain.model.CustomCategory
import com.negociolisto.app.ui.design.DesignTokens

/**
 * 📋 LISTA REORDENABLE DE CATEGORÍAS
 * 
 * Permite reordenar las categorías mediante drag & drop.
 */
@Composable
fun ReorderableCategoryList(
    categories: List<CustomCategory>,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onEditCategory: (CustomCategory) -> Unit,
    onDeleteCategory: (CustomCategory) -> Unit,
    onCategorySelected: (CustomCategory) -> Unit = {},
    allowSelection: Boolean = false,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current
    
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = categories,
            key = { it.id }
        ) { category ->
            val index = categories.indexOf(category)
            val isDragging = draggedIndex == index
            
            // Animaciones para el drag & drop
            val scale by animateFloatAsState(
                targetValue = if (isDragging) 1.05f else 1f,
                animationSpec = tween(200),
                label = "scale"
            )
            
            val alpha by animateFloatAsState(
                targetValue = if (isDragging) 0.8f else 1f,
                animationSpec = tween(200),
                label = "alpha"
            )
            
            ReorderableCategoryItem(
                category = category,
                isDragging = isDragging,
                onDragStart = {
                    draggedIndex = index
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDragEnd = {
                    draggedIndex = null
                },
                onEdit = { onEditCategory(category) },
                onDelete = { onDeleteCategory(category) },
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
                    .graphicsLayer {
                        rotationZ = if (isDragging) 2f else 0f
                    }
            )
        }
    }
}

/**
 * 🎯 ITEM REORDENABLE DE CATEGORÍA
 */
@Composable
fun ReorderableCategoryItem(
    category: CustomCategory,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(category.id) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDrag = { _, _ -> 
                        // El drag se maneja en el nivel superior
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Handle de arrastre
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Arrastrar para reordenar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .alpha(if (isDragging) 1f else 0.6f)
            )
            
            // Icono de la categoría
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(category.getColorInt()).copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Información de la categoría
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (category.description?.isNotBlank() == true) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Orden: ${category.sortOrder}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar categoría",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar categoría",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 🎨 INDICADOR DE POSICIÓN DE ARRASTRE
 */
@Composable
fun DragDropIndicator(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(1.dp)
                )
        )
    }
}

/**
 * 📋 LISTA SIMPLE DE CATEGORÍAS (SIN REORDENAMIENTO)
 * 
 * Muestra las categorías ordenadas alfabéticamente, sin opciones de arrastre.
 */
@Composable
fun SimpleCategoryList(
    categories: List<CustomCategory>,
    onEditCategory: (CustomCategory) -> Unit,
    onDeleteCategory: (CustomCategory) -> Unit,
    onCategorySelected: (CustomCategory) -> Unit = {},
    allowSelection: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = categories,
            key = { it.id }
        ) { category ->
            SimpleCategoryItem(
                category = category,
                onEdit = { onEditCategory(category) },
                onDelete = { onDeleteCategory(category) },
                onClick = { onCategorySelected(category) },
                isClickable = allowSelection,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 🎯 ITEM SIMPLE DE CATEGORÍA (SIN ICONO DE REORDENAMIENTO)
 */
@Composable
fun SimpleCategoryItem(
    category: CustomCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {},
    isClickable: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isClickable) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono de la categoría
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(category.getColorInt()).copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Información de la categoría
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (category.description?.isNotBlank() == true) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar categoría",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar categoría",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 📊 CONTADOR DE CATEGORÍAS
 */
@Composable
fun CategoryCounter(
    totalCategories: Int,
    activeCategories: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total de categorías: $totalCategories",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Activas: $activeCategories",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
