package com.negociolisto.app.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.negociolisto.app.domain.model.CustomCategory

/**
 * 📝 DIÁLOGO PARA AGREGAR/EDITAR CATEGORÍAS
 * 
 * Permite al usuario crear o modificar categorías personalizadas.
 */
@Composable
fun AddEditCategoryDialog(
    isVisible: Boolean,
    category: CustomCategory?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String, description: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "📦") }
    var color by remember { mutableStateOf(category?.color ?: "#9E9E9E") }
    var description by remember { mutableStateOf(category?.description ?: "") }
    
    // Resetear valores cuando cambie la categoría
    LaunchedEffect(category) {
        name = category?.name ?: ""
        icon = category?.icon ?: "📦"
        color = category?.color ?: "#9E9E9E"
        description = category?.description ?: ""
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .graphicsLayer {
                    scaleX = 0.75f
                    scaleY = 0.75f
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (category == null) "➕ Nueva Categoría" else "✏️ Editar Categoría",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Campos del formulario
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre de la categoría
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de la categoría") },
                        placeholder = { Text("Ej: Electrodomésticos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = name.isBlank()
                    )
                    
                    // Icono
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { icon = it },
                        label = { Text("Icono") },
                        placeholder = { Text("📦") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = icon.isBlank()
                    )
                    
                    // Selector de color visual
                    ColorSelector(
                        selectedColor = color,
                        onColorSelected = { color = it }
                    )
                    
                    // Vista previa de la categoría
                    CategoryPreview(
                        name = name,
                        icon = icon,
                        color = color
                    )
                    
                    // Descripción (opcional)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("Descripción de la categoría...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Cancelar",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (name.isNotBlank() && icon.isNotBlank() && color.isNotBlank()) {
                                onSave(
                                    name.trim(),
                                    icon.trim(),
                                    color.trim(),
                                    description.trim().takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && icon.isNotBlank() && color.isNotBlank()
                    ) {
                        Text(
                            if (category == null) "Crear" else "Actualizar",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * 👁️ VISTA PREVIA DE LA CATEGORÍA
 */
@Composable
private fun CategoryPreview(
    name: String,
    icon: String,
    color: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
            // Icono de la categoría
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        try {
                            Color(color.removePrefix("#").toLong(16) or 0xFF000000).copy(alpha = 0.2f)
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        },
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon.ifBlank { "📦" },
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Información de la categoría
            Column {
                Text(
                    text = "Vista previa:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = name.ifBlank { "Nombre de la categoría" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 🎨 SELECTOR DE COLORES VISUAL
 * 
 * Permite seleccionar un color de una paleta predefinida de forma visual.
 */
@Composable
private fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Color",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(CategoryColorPalette.colors) { colorOption ->
                ColorChip(
                    color = colorOption,
                    isSelected = selectedColor == colorOption.hex,
                    onClick = { onColorSelected(colorOption.hex) }
                )
            }
        }
    }
}

/**
 * 🎯 CHIP DE COLOR INDIVIDUAL
 */
@Composable
private fun ColorChip(
    color: CategoryColor,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = Color(android.graphics.Color.parseColor(color.hex)),
        shape = CircleShape,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 🎨 PALETA DE COLORES PARA CATEGORÍAS
 */
private object CategoryColorPalette {
    val colors = listOf(
        CategoryColor("Azul", "#2196F3"),
        CategoryColor("Verde", "#4CAF50"),
        CategoryColor("Rojo", "#F44336"),
        CategoryColor("Naranja", "#FF9800"),
        CategoryColor("Morado", "#9C27B0"),
        CategoryColor("Rosa", "#E91E63"),
        CategoryColor("Cian", "#00BCD4"),
        CategoryColor("Lima", "#CDDC39"),
        CategoryColor("Índigo", "#3F51B5"),
        CategoryColor("Marrón", "#795548"),
        CategoryColor("Gris", "#9E9E9E"),
        CategoryColor("Azul Oscuro", "#1976D2"),
        CategoryColor("Verde Oscuro", "#388E3C"),
        CategoryColor("Rojo Oscuro", "#D32F2F"),
        CategoryColor("Naranja Oscuro", "#F57C00"),
        CategoryColor("Morado Oscuro", "#7B1FA2"),
        CategoryColor("Rosa Oscuro", "#C2185B"),
        CategoryColor("Cian Oscuro", "#0097A7"),
        CategoryColor("Índigo Oscuro", "#303F9F"),
        CategoryColor("Marrón Oscuro", "#5D4037")
    )
}

/**
 * 🎨 MODELO DE COLOR DE CATEGORÍA
 */
private data class CategoryColor(
    val name: String,
    val hex: String
)
