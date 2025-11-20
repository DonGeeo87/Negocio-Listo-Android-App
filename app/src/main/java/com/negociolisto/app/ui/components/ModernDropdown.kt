package com.negociolisto.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * 🎨 ModernDropdown - Componente personalizado elegante para reemplazar ExposedDropdownMenu
 * 
 * Características:
 * - Diseño moderno con sombras y animaciones suaves
 * - Búsqueda integrada para listas largas
 * - Animaciones de entrada/salida elegantes
 * - Diseño consistente con el resto de la app
 * - Soporte para iconos y texto personalizado
 */

@Composable
fun <T> ModernDropdown(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemText: (T) -> String,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "Seleccionar...",
    enabled: Boolean = true,
    searchEnabled: Boolean = true,
    maxHeight: Int = 300,
    itemIcon: ((T) -> @Composable (() -> Unit))? = null,
    errorMessage: String? = null,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { item ->
                itemText(item).contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Column(modifier = modifier) {
        // Campo de entrada principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { 
                    if (enabled) expanded = !expanded 
                }
                .border(
                    width = if (isError) 2.dp else 1.dp,
                    color = when {
                        isError -> MaterialTheme.colorScheme.error
                        expanded -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) 
                    MaterialTheme.colorScheme.surface 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (expanded) 8.dp else 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono del item seleccionado
                selectedItem?.let { item ->
                    itemIcon?.invoke(item)?.invoke() ?: run {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                } ?: run {
                    Spacer(modifier = Modifier.size(24.dp))
                }
                
                // Texto del item seleccionado o placeholder
                Column(modifier = Modifier.weight(1f)) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = selectedItem?.let { itemText(it) } ?: placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedItem != null) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selectedItem != null) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Icono de flecha con animación
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Cerrar" else "Abrir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
        }
        
        // Mensaje de error
        errorMessage?.let { message ->
            if (isError) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // Dropdown expandido con animación
        AnimatedVisibility(
            visible = expanded,
            enter = slideInVertically(
                animationSpec = tween(200),
                initialOffsetY = { -it }
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                animationSpec = tween(200),
                targetOffsetY = { -it }
            ) + fadeOut(animationSpec = tween(200))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .zIndex(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    // Barra de búsqueda (si está habilitada y hay muchos items)
                    if (searchEnabled && items.size > 5) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Buscar...") },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                        
                        if (items.isNotEmpty()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    // Lista de items
                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No hay opciones disponibles" else "No se encontraron resultados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = maxHeight.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(filteredItems) { item ->
                                val isSelected = item == selectedItem
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .clickable { 
                                            onItemSelected(item)
                                            expanded = false
                                            searchQuery = ""
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) 
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else 
                                            Color.Transparent
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Icono del item
                                        itemIcon?.invoke(item)?.invoke() ?: run {
                                            Spacer(modifier = Modifier.size(24.dp))
                                        }
                                        
                                        // Texto del item
                                        Text(
                                            text = itemText(item),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) 
                                                MaterialTheme.colorScheme.primary
                                            else 
                                                MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        // Indicador de selección
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seleccionado",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Cerrar dropdown cuando se hace clic fuera
    LaunchedEffect(expanded) {
        if (expanded) {
            // Aquí podrías agregar lógica para cerrar cuando se hace clic fuera
            // Por ahora se cierra solo cuando se selecciona un item
        }
    }
}
