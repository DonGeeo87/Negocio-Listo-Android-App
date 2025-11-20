package com.negociolisto.app.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
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

/**
 * 📦 CARD DE PRODUCTO
 * 
 * Componente reutilizable que muestra la información de un producto
 * en formato de tarjeta. Es como una "ficha de producto" que incluye:
 * - Imagen del producto
 * - Información básica (nombre, SKU, precio)
 * - Indicadores de stock
 * - Acciones rápidas (ver, editar)
 * - Alertas visuales para stock bajo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    isLowStock: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isLowStock) {
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            ProductImage(
                imageUrl = product.photoUrl,
                thumbnailUrl = product.thumbnailUrl,
                productName = product.name,
                category = product.customCategoryId, // Usar ID de categoría personalizada
                modifier = Modifier.size(60.dp)
            )
            
            // Debug info (temporal)
            if (product.photoUrl != null && product.photoUrl.isNotBlank()) {
                Text(
                    text = "📸",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.size(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información principal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre del producto
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // SKU y categoría
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.sku,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "📦", // Icono por defecto
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = product.customCategoryId, // Usar ID de categoría personalizada
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Precio y stock
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Formatters.formatClpWithSymbol(product.salePrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    StockIndicator(
                        currentStock = product.stockQuantity,
                        minimumStock = product.minimumStock,
                        isLowStock = isLowStock
                    )
                }
            }
            
            // Menú de acciones
            Box {
                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ver detalles") },
                        onClick = {
                            showMenu = false
                            onClick()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
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
 * 🖼️ IMAGEN DE PRODUCTO
 * 
 * Componente que muestra la imagen del producto con fallback.
 */
@Composable
private fun ProductImage(
    imageUrl: String?,
    thumbnailUrl: String?,
    productName: String,
    @Suppress("UNUSED_PARAMETER") category: String, // Usar String en lugar de ProductCategory
    modifier: Modifier = Modifier
) {
    // Usar thumbnail si está disponible, sino usar imagen completa
    val displayImageUrl = thumbnailUrl ?: imageUrl
    
    OptimizedProductImage(
        imageUrl = displayImageUrl,
        productName = productName,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        categoryIcon = "📦" // Icono por defecto
    )
}

/**
 * 📊 INDICADOR DE STOCK
 * 
 * Componente que muestra el nivel de stock con indicadores visuales.
 */
@Composable
private fun StockIndicator(
    currentStock: Int,
    @Suppress("UNUSED_PARAMETER") minimumStock: Int,
    isLowStock: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícono de estado
        Icon(
            imageVector = when {
                currentStock == 0 -> Icons.Filled.ErrorOutline
                isLowStock -> Icons.Filled.Warning
                else -> Icons.Filled.CheckCircleOutline
            },
            contentDescription = null,
            tint = when {
                currentStock == 0 -> MaterialTheme.colorScheme.error
                isLowStock -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Texto de stock
        Text(
            text = when {
                currentStock == 0 -> "Agotado"
                isLowStock -> "Stock: $currentStock"
                else -> "Stock: $currentStock"
            },
            style = MaterialTheme.typography.bodySmall,
            color = when {
                currentStock == 0 -> MaterialTheme.colorScheme.error
                isLowStock -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isLowStock || currentStock == 0) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * 📊 INDICADOR DE MARGEN
 * 
 * Componente que muestra el margen de ganancia del producto.
 */
@Composable
fun ProductMarginIndicator(
    product: Product,
    modifier: Modifier = Modifier
) {
    val margin = product.getProfitMarginPercentage()
    val marginColor = when {
        margin >= 50 -> MaterialTheme.colorScheme.primary
        margin >= 25 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = marginColor.copy(alpha = 0.15f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = when {
                    margin >= 50 -> Icons.AutoMirrored.Filled.TrendingUp
                    margin >= 25 -> Icons.AutoMirrored.Filled.TrendingFlat
                    else -> Icons.AutoMirrored.Filled.TrendingDown
                },
                contentDescription = null,
                tint = marginColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${String.format("%.1f", margin)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = marginColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 🏷️ ETIQUETA DE CATEGORÍA
 * 
 * Componente que muestra la categoría del producto como chip.
 */
@Composable
fun ProductCategoryChip(
    category: String,
    modifier: Modifier = Modifier,
    icon: String = "📦",
    color: String = "#2196F3"
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) {
        Color(0xFF2196F3) // Color por defecto si hay error
    }
    
    AssistChip(
        onClick = { },
        label = { Text(category) },
        leadingIcon = {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = categoryColor.copy(alpha = 0.1f),
            labelColor = categoryColor
        )
    )
}

/**
 * 💰 INFORMACIÓN DE PRECIOS
 * 
 * Componente que muestra información detallada de precios.
 */
@Composable
fun ProductPriceInfo(
    product: Product,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Precio de venta
        Text(
            text = "Precio de venta",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = Formatters.formatClpWithSymbol(product.salePrice),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Precio de compra y ganancia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Costo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatClpWithSymbol(product.purchasePrice),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Ganancia",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatClpWithSymbol(product.getProfit()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Reusable Components**: Componentes reutilizables y modulares
 * 2. **Visual Hierarchy**: Jerarquía visual clara con tipografía
 * 3. **State Indicators**: Indicadores visuales de estado
 * 4. **Accessibility**: Descripciones para lectores de pantalla
 * 5. **Responsive Design**: Adaptable a diferentes tamaños
 * 
 * ANALOGÍA:
 * 
 * ProductCard es como una "etiqueta de producto" en una tienda:
 * 
 * 1. **Foto del producto**: Identificación visual rápida
 * 2. **Información básica**: Nombre, código, categoría
 * 3. **Precio destacado**: Información comercial importante
 * 4. **Estado de stock**: Alertas visuales de disponibilidad
 * 5. **Acciones rápidas**: Botones para ver más o editar
 * 
 * CARACTERÍSTICAS VISUALES:
 * ✅ Imagen con fallback a ícono de categoría
 * ✅ Información jerárquica y clara
 * ✅ Indicadores de stock con colores
 * ✅ Alertas visuales para stock bajo
 * ✅ Menú contextual con acciones
 * ✅ Diseño responsive y accesible
 * 
 * ESTADOS MANEJADOS:
 * - **Stock normal**: Indicador verde
 * - **Stock bajo**: Indicador amarillo con borde
 * - **Sin stock**: Indicador rojo con "Agotado"
 * - **Con imagen**: Muestra foto del producto
 * - **Sin imagen**: Placeholder con ícono de categoría
 * 
 * INTERACCIONES:
 * - **Tap en card**: Ver detalles del producto
 * - **Menú contextual**: Acciones adicionales
 * - **Indicadores visuales**: Información de estado
 * - **Navegación intuitiva**: Flujo natural de acciones
 */