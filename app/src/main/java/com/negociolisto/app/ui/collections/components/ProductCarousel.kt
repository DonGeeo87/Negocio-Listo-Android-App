package com.negociolisto.app.ui.collections.components

import com.negociolisto.app.ui.components.UnifiedCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.OptimizedProductImage
import com.negociolisto.app.ui.design.*

/**
 * 🎠 CARRUSEL DE PRODUCTOS PARA COLECCIONES
 * 
 * Muestra productos en un carrusel horizontal con navegación.
 * Ideal para mostrar productos de forma visual y atractiva.
 */
@Composable
fun ProductCarousel(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onProductRemove: (Product) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Productos en la Colección"
) {
    var currentIndex by remember { mutableStateOf(0) }
    val itemsPerPage = 3
    val maxIndex = maxOf(0, products.size - itemsPerPage)
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header con título y controles de navegación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (products.size > itemsPerPage) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Indicador de posición
                    Text(
                        text = "${currentIndex + 1}-${minOf(currentIndex + itemsPerPage, products.size)} de ${products.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Botón anterior
                    IconButton(
                        onClick = { 
                            if (currentIndex > 0) {
                                currentIndex--
                            }
                        },
                        enabled = currentIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Anterior"
                        )
                    }
                    
                    // Botón siguiente
                    IconButton(
                        onClick = { 
                            if (currentIndex < maxIndex) {
                                currentIndex++
                            }
                        },
                        enabled = currentIndex < maxIndex
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Siguiente"
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (products.isEmpty()) {
            // Estado vacío
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.largeSpacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📦",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay productos seleccionados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Agrega productos desde la lista de disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Carrusel de productos
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                val visibleProducts = products.subList(
                    currentIndex,
                    minOf(currentIndex + itemsPerPage, products.size)
                )
                
                items(visibleProducts) { product ->
                    ProductCarouselCard(
                        product = product,
                        onClick = { onProductClick(product) },
                        onRemove = { onProductRemove(product) }
                    )
                }
            }
        }
        
        // Indicadores de puntos (si hay más de una página)
        if (products.size > itemsPerPage) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat((products.size + itemsPerPage - 1) / itemsPerPage) { index ->
                    val isActive = currentIndex / itemsPerPage == index
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isActive) BrandColors.turquoise else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    if (index < (products.size + itemsPerPage - 1) / itemsPerPage - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * 🎴 TARJETA DE PRODUCTO PARA CARRUSEL
 */
@Composable
private fun ProductCarouselCard(
    product: Product,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedCard(
        onClick = onClick,
        modifier = modifier.width(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding)
        ) {
            // Imagen del producto
            OptimizedProductImage(
                imageUrl = product.photoUrl,
                productName = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                categoryIcon = "📦" // Icono por defecto
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información del producto
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = Formatters.formatClp(product.salePrice),
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandColors.turquoise,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock: ${product.stockQuantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            product.stockQuantity == 0 -> MaterialTheme.colorScheme.error
                            product.stockQuantity <= product.minimumStock -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    // Botón de eliminar
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quitar de colección",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * 📋 LISTA SIMPLE DE PRODUCTOS (alternativa al carrusel)
 */
@Composable
fun ProductSimpleList(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onProductRemove: (Product) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Productos Seleccionados"
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (products.isEmpty()) {
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.largeSpacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📦",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay productos seleccionados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                products.forEach { product ->
                    ProductSimpleCard(
                        product = product,
                        onClick = { onProductClick(product) },
                        onRemove = { onProductRemove(product) }
                    )
                }
            }
        }
    }
}

/**
 * 🎴 TARJETA SIMPLE DE PRODUCTO
 */
@Composable
private fun ProductSimpleCard(
    product: Product,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            OptimizedProductImage(
                imageUrl = product.photoUrl,
                productName = product.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                categoryIcon = "📦" // Icono por defecto
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = Formatters.formatClp(product.salePrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.turquoise
                )
                
                Text(
                    text = "Stock: ${product.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botón de eliminar
            IconButton(
                onClick = onRemove
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitar de colección",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
