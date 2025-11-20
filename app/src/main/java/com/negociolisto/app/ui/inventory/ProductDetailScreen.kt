package com.negociolisto.app.ui.inventory

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.OptimizedProductImage
import com.negociolisto.app.ui.components.*
import com.negociolisto.app.ui.inventory.components.ProductMarginIndicator
import com.negociolisto.app.ui.inventory.components.ProductCategoryChip
import com.negociolisto.app.ui.inventory.components.ProductPriceInfo
import com.negociolisto.app.ui.components.FixedBottomBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.negociolisto.app.ui.design.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier,
    productId: String? = null
) {
    val vm: InventoryViewModel = hiltViewModel()
    var product by remember(productId) { mutableStateOf<Product?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val customCategories by vm.customCategories.collectAsStateWithLifecycle()
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "fadeIn"
    )
    
    LaunchedEffect(productId) {
        productId?.let { 
            launch {
                vm.getProduct(it)?.let { foundProduct -> 
                    product = foundProduct 
                }
            }
        }
    }
    
    // Obtener la categoría personalizada del producto
    val productCategory = remember(product, customCategories) {
        product?.customCategoryId?.let { categoryId ->
            customCategories.firstOrNull { it.id == categoryId }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
                .offset(y = slideInOffset)
                .alpha(fadeInAlpha)
        ) {
        if (product == null) {
            // Estado de carga o error
            UnifiedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.largeSpacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = BrandColors.turquoise
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando producto...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            product?.let { p ->
                // Imagen principal del producto - Centrado horizontalmente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    UnifiedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.cardPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OptimizedProductImage(
                                imageUrl = p.photoUrl,
                                productName = p.name,
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                                categoryIcon = productCategory?.icon ?: "📦"
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = p.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Mostrar chip de categoría solo si existe
                            productCategory?.let { category ->
                                ProductCategoryChip(
                                    category = category.name,
                                    icon = category.icon,
                                    color = category.color,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
                
                // Información básica
                UnifiedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📋 Información Básica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // SKU
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SKU:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = p.sku,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Stock actual
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stock Actual:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when {
                                        p.stockQuantity == 0 -> Icons.Filled.ErrorOutline
                                        p.hasLowStock() -> Icons.Filled.Warning
                                        else -> Icons.Filled.CheckCircleOutline
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        p.stockQuantity == 0 -> MaterialTheme.colorScheme.error
                                        p.hasLowStock() -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${p.stockQuantity} unidades",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        p.stockQuantity == 0 -> MaterialTheme.colorScheme.error
                                        p.hasLowStock() -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                        
                        // Stock mínimo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stock Mínimo:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${p.minimumStock} unidades",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // Información de precios
                UnifiedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "💰 Información de Precios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ProductPriceInfo(
                            product = p,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Margen de ganancia integrado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Margen:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ProductMarginIndicator(
                                product = p,
                                modifier = Modifier
                            )
                        }
                    }
                }
                
                // Espaciado para el bottom bar
                Spacer(modifier = Modifier.height(16.dp))
            }
            }
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = "✏️ Editar Producto",
            primaryButtonOnClick = onEdit,
            primaryButtonIcon = Icons.Filled.Edit,
            secondaryButtonText = "🗑️ Eliminar",
            secondaryButtonOnClick = { showDeleteDialog = true },
            secondaryButtonIcon = Icons.Filled.Delete,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de que quieres eliminar este producto? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        product?.let { p ->
                            vm.deleteProduct(p.id)
                            showDeleteDialog = false
                            onBack()
                        }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


