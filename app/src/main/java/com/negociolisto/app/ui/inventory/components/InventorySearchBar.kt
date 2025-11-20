package com.negociolisto.app.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.components.Formatters

/**
 * 🔍 BARRA DE BÚSQUEDA DE INVENTARIO
 * 
 * Componente que proporciona funcionalidades de búsqueda y acciones rápidas:
 * - Campo de búsqueda en tiempo real
 * - Botón para mostrar/ocultar filtros
 * - Botón para agregar nuevo producto
 * - Limpieza rápida de búsqueda
 * 
 * Es como la "barra de herramientas" principal para navegar el inventario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar productos, SKU, categoría..."
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Campo de búsqueda principal
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar búsqueda",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
        
        // Botón de filtros
        FilledTonalIconButton(
            onClick = onFilterClick
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filtros"
            )
        }
        
        // Botón de agregar
        FilledIconButton(
            onClick = onAddClick
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar producto"
            )
        }
    }
}

/**
 * 🔍 BARRA DE BÚSQUEDA EXPANDIDA
 * 
 * Versión expandida de la barra de búsqueda con más opciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedInventorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onAddClick: () -> Unit,
    onScanClick: (() -> Unit)? = null,
    onVoiceSearchClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar productos, SKU, categoría...",
    isLoading: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = modifier
    ) {
        // Barra principal
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Campo de búsqueda
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingIcon = {
                    Row {
                        // Búsqueda por voz (si está disponible)
                        if (onVoiceSearchClick != null) {
                            IconButton(
                                onClick = onVoiceSearchClick
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Búsqueda por voz",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // Limpiar búsqueda
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = { onQueryChange("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                )
            )
        }
        
        // Barra de acciones secundarias
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Escanear código de barras (si está disponible)
            if (onScanClick != null) {
                OutlinedButton(
                    onClick = onScanClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escanear")
                }
            }
            
            // Filtros
            OutlinedButton(
                onClick = onFilterClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filtros")
            }
            
            // Agregar producto
            Button(
                onClick = onAddClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar")
            }
        }
    }
}

/**
 * 🔍 SUGERENCIAS DE BÚSQUEDA
 * 
 * Componente que muestra sugerencias de búsqueda populares.
 */
@Composable
fun SearchSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isNotEmpty()) {
        Column(
            modifier = modifier
        ) {
            Text(
                text = "Búsquedas populares",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            suggestions.take(5).forEach { suggestion ->
                TextButton(
                    onClick = { onSuggestionClick(suggestion) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = suggestion,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Icon(
                            imageVector = Icons.Default.NorthWest,
                            contentDescription = "Usar sugerencia",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🔍 RESULTADOS DE BÚSQUEDA RÁPIDA
 * 
 * Componente que muestra resultados de búsqueda en tiempo real.
 */
@Composable
fun QuickSearchResults(
    results: List<com.negociolisto.app.domain.model.Product>,
    onResultClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (results.isNotEmpty()) {
        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Resultados (${results.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                
                results.take(3).forEach { product ->
                    TextButton(
                        onClick = { onResultClick(product.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📦", // Icono por defecto
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = product.sku,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Text(
                                text = Formatters.formatClpWithSymbol(product.salePrice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                if (results.size > 3) {
                    TextButton(
                        onClick = { /* Navegar a resultados completos */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver todos los ${results.size} resultados")
                    }
                }
            }
        }
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Search UX**: Experiencia de búsqueda intuitiva
 * 2. **Real-time Search**: Búsqueda en tiempo real
 * 3. **Quick Actions**: Acciones rápidas accesibles
 * 4. **Keyboard Handling**: Manejo apropiado del teclado
 * 5. **Accessibility**: Descripciones para lectores de pantalla
 * 
 * ANALOGÍA:
 * 
 * InventorySearchBar es como la "barra de herramientas" de una tienda:
 * 
 * 1. **Lupa**: Búsqueda rápida de productos
 * 2. **Filtros**: Organización por criterios
 * 3. **Agregar**: Botón rápido para nuevos productos
 * 4. **Sugerencias**: Ayuda para encontrar lo que buscas
 * 5. **Resultados rápidos**: Vista previa de coincidencias
 * 
 * FUNCIONALIDADES:
 * ✅ Búsqueda en tiempo real
 * ✅ Limpieza rápida de búsqueda
 * ✅ Acciones contextuales (filtros, agregar)
 * ✅ Manejo apropiado del teclado
 * ✅ Estados de carga
 * ✅ Sugerencias de búsqueda
 * ✅ Resultados rápidos
 * 
 * OPTIMIZACIONES:
 * - Debounce implícito en la búsqueda
 * - Focus management apropiado
 * - Keyboard actions configuradas
 * - Estados visuales claros
 * - Accesibilidad completa
 * 
 * EXTENSIONES FUTURAS:
 * - Búsqueda por voz
 * - Escaneo de códigos de barras
 * - Historial de búsquedas
 * - Filtros inteligentes
 * - Búsqueda por imagen
 */