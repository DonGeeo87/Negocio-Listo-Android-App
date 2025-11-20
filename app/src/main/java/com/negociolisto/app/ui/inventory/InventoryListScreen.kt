package com.negociolisto.app.ui.inventory

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.negociolisto.app.domain.model.Product
// import com.negociolisto.app.domain.model.ProductCategory // Eliminado - usar solo CustomCategory
import com.negociolisto.app.domain.model.CustomCategory
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.ModernListTopAppBar
import com.negociolisto.app.ui.components.StandardFloatingActionButton
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import com.negociolisto.app.ui.components.UnifiedListTopAppBar
import com.negociolisto.app.ui.components.UnifiedFloatingActionButton
import com.negociolisto.app.ui.components.GoogleAuthCard
import com.negociolisto.app.ui.auth.AuthViewModel
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.AnimationTokens
import com.negociolisto.app.ui.design.ShadowTokens
import com.negociolisto.app.ui.design.StandardCard
import com.negociolisto.app.ui.design.UnifiedGradientHeader
import com.negociolisto.app.ui.components.FixedBottomBar
import com.negociolisto.app.ui.components.UnifiedStatsCard
import com.negociolisto.app.ui.components.StatData
import com.negociolisto.app.ui.components.StatIcon

/**
 * 📦 PANTALLA DE LISTA DE INVENTARIO
 * 
 * Muestra todos los productos del inventario con funciones de búsqueda y filtrado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    authVm: AuthViewModel = hiltViewModel(),
    onAddProductClick: () -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onEditProductClick: (Product) -> Unit = {},
    onBackClick: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onGoogleSignUp: () -> Unit = {}
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isAuthenticated by authVm.isAuthenticated.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Overlay de carga inicial persistente hasta primera emisión utilizable
    var isInitialLoading by remember { mutableStateOf(true) }
    LaunchedEffect(products.isEmpty(), isLoading) {
        if (!isLoading || products.isNotEmpty()) {
            isInitialLoading = false
        }
    }
    
    // Crear mapa de categorías para acceso rápido (con manejo seguro)
    val categoryMap = remember(customCategories) {
        try {
            customCategories.associateBy { it.id }
        } catch (e: Exception) {
            println("⚠️ Error creando mapa de categorías: ${e.message}")
            emptyMap()
        }
    }
    
    // Mostrar errores en snackbar
    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }
    
    // Paginación automática: se activa cuando hay más de 10 productos
    val paginationThreshold = 10
    val pageSize = 10
    val shouldPaginate = products.size > paginationThreshold
    
    var currentPage by remember(products) { mutableStateOf(0) }
    val pages = remember(products) { 
        if (shouldPaginate) {
            products.chunked(pageSize)
        } else {
            listOf(products) // Una sola página con todos los productos
        }
    }
    
    // Resetear a página 0 cuando cambia la lista de productos, categoría o búsqueda
    LaunchedEffect(products.size, selectedCategory, searchQuery) {
        if (currentPage >= pages.size || currentPage < 0) {
            currentPage = 0
        }
    }
    
    val currentPageProducts = remember(pages, currentPage) {
        pages.getOrNull(currentPage) ?: emptyList()
    }
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(AnimationTokens.longDuration, easing = AnimationTokens.decelerateEasing),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(AnimationTokens.extraLongDuration, easing = AnimationTokens.decelerateEasing),
        label = "fadeIn"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (isInitialLoading && products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .offset(y = slideInOffset)
                .alpha(fadeInAlpha),
            contentPadding = PaddingValues(
                start = DesignTokens.cardPadding,
                end = DesignTokens.cardPadding,
                top = 0.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.sectionSpacing)
        ) {
            // Header con estadísticas
            item {
                UnifiedStatsCard(
                    title = "Resumen del Inventario",
                    stats = listOf(
                        StatData(
                            label = "Productos",
                            value = products.size.toString(),
                            icon = StatIcon.Emoji("📦"),
                            color = MaterialTheme.colorScheme.primary
                        ),
                        StatData(
                            label = "Valor Total",
                            value = Formatters.formatClp(products.sumOf { it.salePrice * it.stockQuantity }),
                            icon = StatIcon.Emoji("💰"),
                            color = MaterialTheme.colorScheme.primary
                        ),
                        StatData(
                            label = "Stock Bajo",
                            value = lowStockProducts.size.toString(),
                            icon = StatIcon.Emoji("⚠️"),
                            color = if (lowStockProducts.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    )
                )
            }
            
            // Botones de autenticación rápida con Google (solo si no está autenticado)
            if (!isAuthenticated) {
                item {
                    GoogleAuthCard(
                        onSignInClick = onGoogleSignIn,
                        onSignUpClick = onGoogleSignUp,
                        isLoading = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Barra de búsqueda moderna
            item {
                ModernSearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::updateSearchQuery
                )
            }
            
            // Filtros de categoría modernos
            item {
                ModernCategoryFilters(
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::selectCategory,
                    customCategories = viewModel.customCategories.value
                )
            }
            
            // Alerta de stock bajo moderna
            if (lowStockProducts.isNotEmpty()) {
                item {
                    ModernLowStockAlert(
                        lowStockCount = lowStockProducts.size,
                        lowStockProducts = lowStockProducts.take(3)
                    )
                }
            }
            
            // Lista de productos
            when {
                isLoading -> {
                    item {
                        LoadingSkeleton()
                    }
                }
                products.isEmpty() -> {
                    item {
                        ModernEmptyState(
                            onAddClick = onAddProductClick
                        )
                    }
                }
                else -> {
                    // Mostrar productos de la página actual
                    items(currentPageProducts, key = { it.id }, contentType = { "product" }) { product ->
                        val categoryName = categoryMap[product.customCategoryId]?.name ?: "Sin categoría"
                        ModernProductCard(
                            product = product,
                            categoryName = categoryName,
                            onClick = { onProductClick(product) },
                            onEditClick = { onEditProductClick(product) }
                        )
                    }
                    
                    // Controles de paginación (solo si hay más de 10 productos)
                    if (shouldPaginate && pages.size > 1) {
                        item(key = "pagination_controls") {
                            ModernPaginationControls(
                                currentPage = currentPage,
                                totalPages = pages.size,
                                onPreviousPage = { 
                                    if (currentPage > 0) {
                                        currentPage = currentPage - 1
                                    }
                                },
                                onNextPage = { 
                                    if (currentPage < pages.lastIndex) {
                                        currentPage = currentPage + 1
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Espacio final
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = "Agregar Producto",
            primaryButtonOnClick = onAddProductClick,
            primaryButtonIcon = Icons.Filled.Add,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Snackbar host (se muestra automáticamente arriba)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
    
    // Mostrar error si existe
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
}


// 🔍 BARRA DE BÚSQUEDA MODERNA - UNIFICADA
@Composable
private fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar producto o colección…") },
        leadingIcon = { 
            Icon(
                Icons.Filled.Search, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            ) 
        },
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.buttonShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

// 🏷️ FILTROS DE CATEGORÍA MODERNOS - UNIFICADOS
@Composable
private fun ModernCategoryFilters(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    customCategories: List<CustomCategory>
) {
    Column {
        Text(
            text = "🏷️ Categorías",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = DesignTokens.smallSpacing)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
            contentPadding = PaddingValues(horizontal = DesignTokens.smallSpacing)
        ) {
            item {
                ModernFilterChip(
                    text = "Todos",
                    isSelected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    icon = "📋"
                )
            }
            
            // Mostrar solo categorías personalizadas
            items(customCategories) { customCategory ->
                ModernFilterChip(
                    text = customCategory.name,
                    isSelected = selectedCategory == customCategory.id, // Usar ID de categoría personalizada
                    onClick = { onCategorySelected(customCategory.id) }, // Usar ID de categoría personalizada
                    icon = customCategory.icon
                )
            }
        }
    }
}

@Composable
private fun ModernFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: String
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = DesignTokens.chipShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = DesignTokens.cardPadding,
                vertical = DesignTokens.smallSpacing
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// Función eliminada - usar CustomCategory.icon directamente

// ⚠️ ALERTA DE STOCK BAJO MODERNA
@Composable
private fun ModernLowStockAlert(
    lowStockCount: Int,
    lowStockProducts: List<Product>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Stock Bajo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$lowStockCount productos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (lowStockProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Productos críticos:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                lowStockProducts.forEach { product ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• ${product.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stock: ${product.stockQuantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// 📦 ESTADO VACÍO MODERNO - UNIFICADO
@Composable
private fun ModernEmptyState(
    onAddClick: () -> Unit
) {
    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.largeSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📦",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
            
            Text(
                text = "¡Tu inventario está vacío!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
            
            Text(
                text = "Agrega tu primer producto para comenzar a gestionar tu negocio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
            
            // Botón con gradiente unificado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignTokens.buttonHeight)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = DesignTokens.buttonShape
                    )
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Add, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                    Text(
                        text = "📦 Agregar Primer Producto",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// 🛍️ TARJETA DE PRODUCTO MODERNA - UNIFICADA
@Composable
private fun ModernProductCard(
    product: Product,
    categoryName: String,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    StandardCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .size(DesignTokens.largeIconSize)
                    .clip(DesignTokens.buttonShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (product.photoUrl != null) {
                    AsyncImage(
                        model = product.photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "📦", // Icono por defecto
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(DesignTokens.itemSpacing))
            
            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (product.description != null) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                ) {
                    Text(
                        text = "📦", // Icono por defecto
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = categoryName, // Mostrar nombre de categoría en lugar del UUID
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Precio y stock
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = Formatters.formatClp(product.salePrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (product.stockQuantity <= product.minimumStock) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "${product.stockQuantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.stockQuantity <= product.minimumStock) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (product.stockQuantity <= product.minimumStock) {
                    Text(
                        text = "Stock bajo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Menú de opciones
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        modifier = Modifier.size(DesignTokens.mediumIconSize)
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

// 🔄 SKELETON DE CARGA MODERNO
@Composable
private fun LoadingSkeleton() {
    com.negociolisto.app.ui.components.SkeletonProductList(
        itemCount = 6,
        modifier = Modifier.fillMaxWidth()
    )
}

// ➕ BOTÓN FLOTANTE MODERNO
@Composable
private fun ModernFloatingActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .shadow(ShadowTokens.extraLarge, RoundedCornerShape(16.dp)),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 📄 BOTÓN CARGAR MÁS MODERNO
@Composable
private fun ModernLoadMoreButton(
    remainingCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(ShadowTokens.small, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cargar más productos ($remainingCount restantes)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 🔢 CONTROLES DE PAGINACIÓN MODERNOS
@Composable
private fun ModernPaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    StandardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Anterior
            OutlinedButton(
                onClick = onPreviousPage,
                enabled = currentPage > 0,
                modifier = Modifier.weight(1f),
                shape = DesignTokens.buttonShape
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Página anterior",
                    modifier = Modifier.size(DesignTokens.mediumIconSize)
                )
                Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                Text(
                    text = "Anterior",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(DesignTokens.itemSpacing))
            
            // Indicador de página
            Text(
                text = "Página ${currentPage + 1} / $totalPages",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(DesignTokens.itemSpacing))
            
            // Botón Siguiente
            Button(
                onClick = onNextPage,
                enabled = currentPage < totalPages - 1,
                modifier = Modifier.weight(1f),
                shape = DesignTokens.buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Siguiente",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Página siguiente",
                    modifier = Modifier.size(DesignTokens.mediumIconSize)
                )
            }
        }
    }
}