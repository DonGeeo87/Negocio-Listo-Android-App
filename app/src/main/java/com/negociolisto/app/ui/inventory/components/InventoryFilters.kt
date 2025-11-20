package com.negociolisto.app.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.components.Formatters
// import com.negociolisto.app.domain.model.ProductCategory // Eliminado - usar solo CustomCategory

/**
 * 🔧 FILTROS DE INVENTARIO
 * 
 * Componente que proporciona múltiples opciones de filtrado:
 * - Filtro por categoría
 * - Filtro por proveedor
 * - Filtro de stock bajo
 * - Ordenamiento
 * - Limpieza de filtros
 * 
 * Es como el "panel de control" para personalizar la vista del inventario.
 */
@Composable
fun InventoryFilters(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    showOnlyLowStock: Boolean,
    onLowStockToggle: () -> Unit,
    availableSuppliers: List<String>,
    onSupplierSelected: (String?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSupplier by remember { mutableStateOf<String?>(null) }
    var sortOrder by remember { mutableStateOf(SortOrder.NAME_ASC) }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado de filtros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(
                    onClick = {
                        onClearFilters()
                        selectedSupplier = null
                        sortOrder = SortOrder.NAME_ASC
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar todo")
                }
            }
            
            // Filtros rápidos
            QuickFilters(
                showOnlyLowStock = showOnlyLowStock,
                onLowStockToggle = onLowStockToggle
            )
            
            // Filtro por categoría
            CategoryFilter(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
            
            // Filtro por proveedor
            if (availableSuppliers.isNotEmpty()) {
                SupplierFilter(
                    availableSuppliers = availableSuppliers,
                    selectedSupplier = selectedSupplier,
                    onSupplierSelected = { supplier ->
                        selectedSupplier = supplier
                        onSupplierSelected(supplier)
                    }
                )
            }
            
            // Ordenamiento
            SortOrderFilter(
                currentOrder = sortOrder,
                onOrderChanged = { sortOrder = it }
            )
        }
    }
}

/**
 * ⚡ FILTROS RÁPIDOS
 * 
 * Switches y toggles para filtros comunes.
 */
@Composable
private fun QuickFilters(
    showOnlyLowStock: Boolean,
    onLowStockToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Filtros rápidos",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Stock bajo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Solo stock bajo",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Switch(
                checked = showOnlyLowStock,
                onCheckedChange = { onLowStockToggle() }
            )
        }
    }
}

/**
 * 📂 FILTRO POR CATEGORÍA
 * 
 * Selector horizontal de categorías con chips.
 */
@Composable
private fun CategoryFilter(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // Opción "Todas"
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("Todas") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
            
            // Categorías populares primero - comentado temporalmente
            // items(ProductCategory.values().filter { it.isPopularCategory() }) { category ->
            //     FilterChip(
            //         selected = selectedCategory == category,
            //         onClick = { onCategorySelected(category) },
            //         label = { Text(category.displayName) },
            //         leadingIcon = {
            //             Text(
            //                 text = category.icon,
            //                 style = MaterialTheme.typography.bodySmall
            //             )
            //         },
            //         colors = FilterChipDefaults.filterChipColors(
            //             selectedContainerColor = Color(category.getColorInt()).copy(alpha = 0.2f),
            //             selectedLabelColor = Color(category.getColorInt())
            //         )
            //     )
            // }
            
            // Resto de categorías - comentado temporalmente
            // items(ProductCategory.values().filter { !it.isPopularCategory() }) { category ->
            //     FilterChip(
            //         selected = selectedCategory == category,
            //         onClick = { onCategorySelected(category) },
            //         label = { Text(category.displayName) },
            //         leadingIcon = {
            //             Text(
            //                 text = category.icon,
            //                 style = MaterialTheme.typography.bodySmall
            //             )
            //         },
            //         colors = FilterChipDefaults.filterChipColors(
            //             selectedContainerColor = Color(category.getColorInt()).copy(alpha = 0.2f),
            //             selectedLabelColor = Color(category.getColorInt())
            //         )
            //     )
            // }
        }
    }
}

/**
 * 🏪 FILTRO POR PROVEEDOR
 * 
 * Dropdown para seleccionar proveedor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierFilter(
    availableSuppliers: List<String>,
    selectedSupplier: String?,
    onSupplierSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Proveedor",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedSupplier ?: "Todos los proveedores",
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Opción "Todos"
                DropdownMenuItem(
                    text = { Text("Todos los proveedores") },
                    onClick = {
                        onSupplierSelected(null)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null
                        )
                    }
                )
                
                // Proveedores disponibles
                availableSuppliers.forEach { supplier ->
                    DropdownMenuItem(
                        text = { Text(supplier) },
                        onClick = {
                            onSupplierSelected(supplier)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 📊 FILTRO DE ORDENAMIENTO
 * 
 * Opciones para ordenar los resultados.
 */
@Composable
private fun SortOrderFilter(
    currentOrder: SortOrder,
    onOrderChanged: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ordenar por",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(SortOrder.values()) { order ->
                FilterChip(
                    selected = currentOrder == order,
                    onClick = { onOrderChanged(order) },
                    label = { Text(order.displayName) },
                    leadingIcon = {
                        Icon(
                            imageVector = order.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

/**
 * 📊 OPCIONES DE ORDENAMIENTO
 * 
 * Enum que define las diferentes formas de ordenar los productos.
 */
enum class SortOrder(
    val displayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    NAME_ASC("Nombre A-Z", Icons.Default.SortByAlpha),
    NAME_DESC("Nombre Z-A", Icons.Default.SortByAlpha),
    PRICE_ASC("Precio menor", Icons.AutoMirrored.Filled.TrendingUp),
    PRICE_DESC("Precio mayor", Icons.AutoMirrored.Filled.TrendingDown),
    STOCK_ASC("Stock menor", Icons.Default.Inventory),
    STOCK_DESC("Stock mayor", Icons.Default.Inventory2),
    RECENT("Más recientes", Icons.Default.Schedule),
    CATEGORY("Por categoría", Icons.Default.Category)
}

/**
 * 🔧 FILTROS AVANZADOS
 * 
 * Panel expandible con filtros adicionales.
 */
@Composable
fun AdvancedFilters(
    onPriceRangeChanged: (Float, Float) -> Unit,
    onStockRangeChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var priceRange by remember { mutableStateOf(0f..1000f) }
    var stockRange by remember { mutableStateOf(0..100) }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtros avanzados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Rango de precios
            Column {
                Text(
                    text = "Rango de precios: ${Formatters.formatClpWithSymbol(priceRange.start.toDouble())} - ${Formatters.formatClpWithSymbol(priceRange.endInclusive.toDouble())}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                RangeSlider(
                    value = priceRange,
                    onValueChange = { range ->
                        priceRange = range
                        onPriceRangeChanged(range.start, range.endInclusive)
                    },
                    valueRange = 0f..2000f,
                    steps = 19
                )
            }
            
            // Rango de stock
            Column {
                Text(
                    text = "Rango de stock: ${stockRange.first} - ${stockRange.last}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                RangeSlider(
                    value = stockRange.first.toFloat()..stockRange.last.toFloat(),
                    onValueChange = { range ->
                        stockRange = range.start.toInt()..range.endInclusive.toInt()
                        onStockRangeChanged(range.start.toInt(), range.endInclusive.toInt())
                    },
                    valueRange = 0f..200f,
                    steps = 19
                )
            }
        }
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Filter UI**: Interfaz intuitiva para filtros múltiples
 * 2. **State Management**: Manejo de estado local y global
 * 3. **Chip Design**: Uso de chips para selección múltiple
 * 4. **Dropdown Menus**: Menús desplegables para opciones
 * 5. **Range Sliders**: Controles de rango para valores numéricos
 * 
 * ANALOGÍA:
 * 
 * InventoryFilters es como el "panel de control" de una tienda:
 * 
 * 1. **Interruptores**: Filtros rápidos on/off
 * 2. **Selectores**: Opciones de categorías y proveedores
 * 3. **Controles de rango**: Ajustes finos de precios y stock
 * 4. **Ordenamiento**: Diferentes formas de organizar
 * 5. **Reset**: Botón para volver al estado inicial
 * 
 * TIPOS DE FILTROS:
 * ✅ **Filtros rápidos**: Toggles para opciones comunes
 * ✅ **Categorías**: Chips visuales con iconos
 * ✅ **Proveedores**: Dropdown con lista dinámica
 * ✅ **Ordenamiento**: Múltiples criterios de orden
 * ✅ **Rangos**: Sliders para precios y stock
 * ✅ **Limpieza**: Reset de todos los filtros
 * 
 * EXPERIENCIA DE USUARIO:
 * - **Visual**: Chips coloridos por categoría
 * - **Intuitivo**: Iconos claros para cada opción
 * - **Flexible**: Combinación de múltiples filtros
 * - **Responsive**: Adaptable a diferentes pantallas
 * - **Accesible**: Descripciones para lectores de pantalla
 * 
 * OPTIMIZACIONES:
 * - LazyRow para categorías (scroll horizontal)
 * - Estado local para filtros temporales
 * - Colores dinámicos por categoría
 * - Feedback visual de selección
 * - Limpieza granular y total
 */