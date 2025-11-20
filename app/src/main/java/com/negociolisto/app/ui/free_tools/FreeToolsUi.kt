package com.negociolisto.app.ui.free_tools

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

/**
 * 🎴 TARJETA DE HERRAMIENTA
 * 
 * Muestra una tarjeta con el logo, nombre y categoría de la herramienta.
 */
@Composable
fun FreeToolCard(
    tool: FreeTool?,
    imageLoader: ImageLoader?,
    onClick: (String) -> Unit
) {
    if (tool == null || imageLoader == null) return
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .aspectRatio(0.85f)
            .clickable { onClick(tool.url) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current
            val domain = FreeToolsRepository.domainFromUrl(tool.url)
            val iconUrl = FreeToolsRepository.iconUrlFor(tool.icon, domain)

            // Ícono centrado con fondo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(iconUrl)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = tool.name,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(8.dp))
            
            // Nombre centrado
            Text(
                text = tool.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            
            Spacer(Modifier.height(4.dp))

            // Descripción centrada (10-15 palabras)
            Text(
                text = tool.description.ifEmpty { "Herramienta para gestión de negocio" },
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(Modifier.height(8.dp))

            // Chip de categoría centrado
            SuggestionChip(
                onClick = { onClick(tool.url) },
                label = { 
                    Text(
                        text = tool.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

/**
 * 🔲 GRILLA DE HERRAMIENTAS
 * 
 * Muestra una grilla adaptativa con todas las herramientas filtradas.
 * Incluye paginación y filtros combinados.
 */
@Composable
fun FreeToolsGrid(
    tools: List<FreeTool>,
    selectedCategory: String?,
    searchQuery: String = "",
    onOpenUrl: (String) -> Unit
) {
    // Filtros combinados: categoría + búsqueda
    val filtered = remember(tools, selectedCategory, searchQuery) {
        var result = tools
        
        // Filtro por categoría
        if (!selectedCategory.isNullOrBlank() && selectedCategory != "Todas") {
            result = result.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        
        // Filtro por búsqueda
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.trim().lowercase()
            result = result.filter { 
                it.name.lowercase().contains(query) ||
                it.description.lowercase().contains(query) ||
                it.category.lowercase().contains(query)
            }
        }
        
        result
    }
    
    // Paginación
    val pageSize = 20
    var visibleCount by remember(filtered) { mutableStateOf(pageSize) }
    val page = remember(filtered, visibleCount) { 
        filtered.take(visibleCount.coerceAtMost(filtered.size)) 
    }

    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) } // Soporte SVG
            .build()
    }

    if (filtered.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔍 No se encontraron herramientas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Intenta con otros filtros o términos de búsqueda",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(page.size) { index ->
                val tool = page[index]
                FreeToolCard(tool = tool, imageLoader = imageLoader, onClick = onOpenUrl)
            }
            
            // Botón "Cargar más" si hay más elementos
            if (visibleCount < filtered.size) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .clickable { visibleCount = (visibleCount + pageSize).coerceAtMost(filtered.size) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cargar más (${filtered.size - visibleCount} restantes)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🏷️ CHIPS DE CATEGORÍAS
 * 
 * Filtros horizontales con scroll para seleccionar categoría.
 */
@Composable
fun CategoryChips(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { cat ->
            FilterChip(
                selected = selected == cat,
                onClick = { onSelect(cat) },
                label = { 
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                modifier = Modifier.height(36.dp)
            )
        }
    }
}

