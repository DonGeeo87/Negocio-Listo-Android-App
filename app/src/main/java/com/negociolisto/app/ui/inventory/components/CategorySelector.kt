package com.negociolisto.app.ui.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
// import com.negociolisto.app.domain.model.ProductCategory // Eliminado - usar solo CustomCategory
import com.negociolisto.app.domain.model.CustomCategory

/**
 * 📂 SELECTOR DE CATEGORÍAS SIMPLIFICADO
 * 
 * Solo usa las categorías del administrador (CustomCategories).
 * Eliminamos las categorías predefinidas para simplificar la experiencia.
 */
@Composable
fun CategorySelector(
    selectedCategory: String?,
    selectedCustomCategory: CustomCategory?,
    customCategories: List<CustomCategory>,
    onCategorySelected: (String?, CustomCategory?) -> Unit,
    onManageCategories: () -> Unit,
    onCreateCustomCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showModal by remember { mutableStateOf(false) }
    
    // Determinar el item seleccionado actual
    val currentSelection = when {
        selectedCustomCategory != null -> selectedCustomCategory.name
        selectedCategory != null -> selectedCategory // Usar String directamente
        else -> "Sin categoría"
    }
    
    val currentIcon = when {
        selectedCustomCategory != null -> selectedCustomCategory.icon
        selectedCategory != null -> "📦" // Icono por defecto
        else -> "📦"
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Campo de selección de categoría (clickeable)
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { showModal = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icono de la categoría
                Text(
                    text = currentIcon,
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Texto de la categoría
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentSelection,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Icono de flecha
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Abrir selector",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Botón para gestionar categorías
        IconButton(
            onClick = onManageCategories,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Gestionar categorías",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    
    // Modal de selección de categorías - comentado temporalmente
    // CategorySelectorModal(
    //     isVisible = showModal,
    //     onDismiss = { showModal = false },
    //     selectedCategory = selectedCategory,
    //     selectedCustomCategory = selectedCustomCategory,
    //     customCategories = customCategories,
    //     onCategorySelected = onCategorySelected,
    //     onCreateCustomCategory = onCreateCustomCategory
    // )
}

/**
 * 🎨 VISTA PREVIA DE CATEGORÍA
 */
@Composable
fun CategoryPreview(
    category: String?,
    customCategory: CustomCategory?,
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
                        if (customCategory != null) {
                            Color(customCategory.getColorInt()).copy(alpha = 0.2f)
                        } else {
                            // Usar color por defecto ya que category es ahora String
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        },
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customCategory?.icon ?: "📦", // Usar icono por defecto
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Información de la categoría
            Column {
                Text(
                    text = "Categoría seleccionada:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = customCategory?.name ?: category ?: "Sin categoría", // Usar String directamente
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (customCategory?.description?.isNotBlank() == true) {
                    Text(
                        text = customCategory.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
