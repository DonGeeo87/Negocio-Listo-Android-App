package com.negociolisto.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.Collection
import com.negociolisto.app.domain.model.CollectionStatus
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.GradientTokens
import com.negociolisto.app.ui.design.DesignTokens

/**
 * 📊 TARJETA DE ESTADÍSTICAS DE INVENTARIO
 */
@Composable
fun InventoryStatsCard(
    totalProducts: Int,
    lowStockProducts: Int,
    totalValue: Double,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = 4.dp,
        animationSpec = tween(300),
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = elevation, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Resumen del Inventario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = totalProducts.toString(),
                    icon = Icons.Default.Inventory,
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatItem(
                    label = "Stock Bajo",
                    value = lowStockProducts.toString(),
                    icon = Icons.Filled.Warning,
                    color = if (lowStockProducts > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
                
                StatItem(
                    label = "Valor Total",
                    value = Formatters.formatClp(totalValue),
                    icon = Icons.Default.AttachMoney,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.4f),
                            color.copy(alpha = 0.15f)
                        )
                    )
                )
                .then(
                    Modifier.border(
                        width = 2.dp,
                        color = color.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                )
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    spotColor = color.copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.9f),
                modifier = Modifier.size(32.dp)
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 🔍 BARRA DE BÚSQUEDA MODERNA
 */
@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Buscar...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar"
                    )
                }
            }
        } else null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

/**
 * 🏷️ FILTROS DE CATEGORÍA MODERNOS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCategoryFilters(
    selectedCategories: Set<String>,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Todos", "Electrónicos", "Ropa", "Hogar", "Deportes", "Libros")
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategories.isEmpty() && category == "Todos" || 
                          selectedCategories.contains(category),
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * 📦 TARJETA DE PRODUCTO MODERNA
 */
@Composable
fun ModernProductCard(
    product: Product,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = 2.dp,
        animationSpec = tween(200),
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(elevation = elevation, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            AsyncImage(
                model = product.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (product.description != null) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Stock: ${product.stockQuantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (product.hasLowStock()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        
                        Text(
                            text = Formatters.formatClp(product.salePrice),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Indicador de stock bajo
                    if (product.hasLowStock()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Stock Bajo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Botón de editar
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar producto",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 📊 TARJETA DE ESTADÍSTICAS DE COLECCIONES
 */
@Composable
fun CollectionStatsCard(
    totalCollections: Int,
    activeCollections: Int,
    totalValue: Double,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = 4.dp,
        animationSpec = tween(300),
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = elevation, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Resumen de Colecciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = totalCollections.toString(),
                    icon = Icons.Default.Collections,
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatItem(
                    label = "Activas",
                    value = activeCollections.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                StatItem(
                    label = "Valor Total",
                    value = Formatters.formatClp(totalValue),
                    icon = Icons.Default.AttachMoney,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * 🎨 TARJETA DE COLECCIÓN MODERNA
 */
@Composable
fun ModernCollectionCard(
    collection: Collection,
    productCount: Int,
    totalClp: Double,
    customerName: String?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = 2.dp,
        animationSpec = tween(200),
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = elevation, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (collection.description != null) {
                        Text(
                            text = collection.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
                
                // Botón de opciones
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Productos: $productCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = Formatters.formatClp(totalClp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (customerName != null) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Cliente:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Indicador de estado
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (collection.status) {
                            CollectionStatus.ACTIVE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            CollectionStatus.DRAFT -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            CollectionStatus.ARCHIVED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            CollectionStatus.SHARED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (collection.status) {
                        CollectionStatus.ACTIVE -> "Activa"
                        CollectionStatus.DRAFT -> "Borrador"
                        CollectionStatus.ARCHIVED -> "Archivada"
                        CollectionStatus.SHARED -> "Compartida"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (collection.status) {
                        CollectionStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                        CollectionStatus.DRAFT -> MaterialTheme.colorScheme.secondary
                        CollectionStatus.ARCHIVED -> MaterialTheme.colorScheme.outline
                        CollectionStatus.SHARED -> MaterialTheme.colorScheme.tertiary
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 📊 TARJETA DE ESTADÍSTICAS DE CLIENTES
 */
@Composable
fun CustomerStatsCard(
    totalCustomers: Int,
    activeCustomers: Int,
    totalOrders: Int,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = 4.dp,
        animationSpec = tween(300),
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = elevation, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Resumen de Clientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total",
                    value = totalCustomers.toString(),
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatItem(
                    label = "Activos",
                    value = activeCustomers.toString(),
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                StatItem(
                    label = "Pedidos",
                    value = totalOrders.toString(),
                    icon = Icons.Default.ShoppingCart,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * 👤 TARJETA DE CLIENTE MODERNA MEJORADA
 * 
 * Tarjeta mejorada que incluye:
 * - Información del cliente sin mostrar compras
 * - Botón de WhatsApp para comunicación directa
 * - Acciones rápidas (llamar, email, ver historial)
 * - Diseño más limpio y funcional
 */
@Composable
fun ModernCustomerCard(
    customer: Customer,
    onClick: () -> Unit,
    onWhatsAppClick: (() -> Unit)? = null,
    onCallClick: (() -> Unit)? = null,
    onEmailClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = 2.dp,
        animationSpec = tween(200),
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(elevation = elevation, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con avatar y información principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del cliente
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Información del cliente
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    customer.email?.let { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    customer.phone?.let { phone ->
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Indicador de estado
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (customer.totalPurchases > 0) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (customer.totalPurchases > 0) "Activo" else "Nuevo",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (customer.totalPurchases > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botones de acción rápida
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de WhatsApp
                if (customer.phone != null && onWhatsAppClick != null) {
                    Button(
                        onClick = onWhatsAppClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366) // Color oficial de WhatsApp
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                // Botón de llamada
                if (customer.phone != null && onCallClick != null) {
                    OutlinedButton(
                        onClick = onCallClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Llamar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Llamar", style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                // Botón de email
                if (customer.email != null && onEmailClick != null) {
                    OutlinedButton(
                        onClick = onEmailClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Email", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

/**
 * ⬇️ BOTÓN MODERNO PARA CARGAR MÁS
 * 
 * Botón mejorado con diseño moderno y atractivo
 */
@Composable
fun ModernLoadMoreButton(
    remainingCount: Int,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onLoadMore,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Cargar más ($remainingCount restantes)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 📊 TARJETA DE ESTADÍSTICAS DE VENTAS MODERNIZADA
 * 
 * Diseño mejorado con:
 * - Gradientes sutiles
 * - Mejor espaciado y jerarquía visual
 * - Iconos más expresivos
 */
@Composable
fun SalesStatsCard(
    totalSales: Int,
    totalRevenue: Double,
    averageSale: Double,
    modifier: Modifier = Modifier
) {
    val primaryColor = BrandColors.blueLilac
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = primaryColor.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.12f),
                            primaryColor.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.surface
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            // Borde superior con gradiente de marca
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            GradientTokens.brandGradient()
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resumen de Ventas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.25f),
                                        primaryColor.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = primaryColor.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Analytics,
                            contentDescription = null,
                            tint = primaryColor.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Total",
                        value = totalSales.toString(),
                        icon = Icons.Filled.ShoppingCart,
                        color = BrandColors.blueLilac
                    )
                    
                    StatItem(
                        label = "Ingresos",
                        value = Formatters.formatClp(totalRevenue),
                        icon = Icons.Filled.AttachMoney,
                        color = BrandColors.turquoise
                    )
                    
                    StatItem(
                        label = "Promedio",
                        value = Formatters.formatClp(averageSale),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = BrandColors.secondary
                    )
                }
            }
        }
    }
}

/**
 * 📅 FILTROS DE FECHA MODERNOS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDateFilters(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "TODAY" to ("Hoy" to Icons.Filled.Today),
        "WEEK" to ("7 días" to Icons.Filled.DateRange),
        "MONTH" to ("Mes" to Icons.Filled.CalendarMonth),
        "ALL" to ("Todo" to Icons.Filled.AllInclusive)
    )
    val primaryColor = BrandColors.blueLilac
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(filters) { (key, labelIcon) ->
            val (label, icon) = labelIcon
            val isSelected = selectedFilter == key
            
            // Animación de escala al seleccionar
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = tween(200),
                label = "scale"
            )
            
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(key) },
                label = { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Transparent, // Usaremos gradiente personalizado
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        GradientTokens.brandGradient()
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    spotColor = primaryColor.copy(alpha = 0.4f)
                                )
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

/**
 * 🔢 Helper para generar números correlativos de ventas
 * 
 * Genera números correlativos basados en el orden de creación (fecha más antigua = menor número)
 */
fun getSaleSequentialNumber(saleIndex: Int): String {
    return String.format("%04d", saleIndex + 1)
}

/**
 * 💰 TARJETA DE VENTA COMPACTA Y MODERNA
 * 
 * Diseño compacto optimizado para mostrar más ventas en pantalla:
 * - Números correlativos (0001, 0002, etc.)
 * - Diseño compacto con información esencial
 * - Click para ver detalles tipo boleta
 * - Opciones de editar y anular
 */
@Composable
fun ModernSaleCard(
    sale: Sale,
    customerName: String?,
    onCancel: () -> Unit,
    onEdit: () -> Unit = {},
    onClick: () -> Unit = {},
    sequentialNumber: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = BrandColors.blueLilac
    
    val totalItems = sale.getTotalItemCount()
    val paymentMethodName = sale.paymentMethod.displayName
    
    // Color del indicador lateral según método de pago
    val indicatorColor = when (sale.paymentMethod) {
        com.negociolisto.app.domain.model.PaymentMethod.CASH -> BrandColors.turquoise
        com.negociolisto.app.domain.model.PaymentMethod.DEBIT_CARD -> BrandColors.blueLilac
        com.negociolisto.app.domain.model.PaymentMethod.CREDIT_CARD -> BrandColors.secondary
        com.negociolisto.app.domain.model.PaymentMethod.BANK_TRANSFER -> BrandColors.turquoiseLight
        com.negociolisto.app.domain.model.PaymentMethod.DIGITAL_WALLET -> BrandColors.blueLilacLight
        else -> BrandColors.secondary
    }
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "fadeIn"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(fadeInAlpha),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = DesignTokens.cardShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = DesignTokens.cardElevation
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            indicatorColor.copy(alpha = 0.02f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // Indicador de color lateral
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                indicatorColor.copy(alpha = 0.8f),
                                indicatorColor.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
            )
            
            // Contenido compacto en una sola fila principal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lado izquierdo: Número y info básica (clickeable para ver detalles)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClick() },
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge número con gradiente de marca
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    GradientTokens.brandGradient()
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#$sequentialNumber",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                
                    // Info compacta en columna
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        customerName?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$totalItems",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = paymentMethodName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = Formatters.formatDate(sale.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Lado derecho: Monto y acciones
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.widthIn(min = 120.dp)
                ) {
                    Text(
                        text = Formatters.formatClp(sale.total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor.copy(alpha = 0.9f)
                    )
                    
                    // Botones de acción compactos con mejor espaciado
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.widthIn(min = 80.dp)
                    ) {
                        // Botón editar
                        IconButton(
                            onClick = { onEdit() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Botón anular
                        IconButton(
                            onClick = { onCancel() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Anular",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
