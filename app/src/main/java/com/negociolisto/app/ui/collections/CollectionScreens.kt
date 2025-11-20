package com.negociolisto.app.ui.collections

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.negociolisto.app.domain.model.Collection
import com.negociolisto.app.domain.model.CollectionItem
import com.negociolisto.app.domain.model.CollectionStatus
import com.negociolisto.app.domain.model.CollectionWebTemplate
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.*
import com.negociolisto.app.ui.components.StandardFloatingActionButton
import com.negociolisto.app.ui.collections.components.EmptyCollectionState
import com.negociolisto.app.ui.collections.components.ProductCarousel
import com.negociolisto.app.ui.collections.components.ProductSimpleList
import com.negociolisto.app.ui.inventory.InventoryViewModel
import com.negociolisto.app.ui.customers.CustomerViewModel
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.ui.theme.NegocioListoTheme
import coil.compose.AsyncImage
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import kotlinx.coroutines.delay
import com.negociolisto.app.ui.design.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import com.negociolisto.app.ui.collections.CollectionColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionListScreen(
    onAddCollection: () -> Unit,
    onEditCollection: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onCopyPublicLink: (String) -> Unit = {},
    onViewResponses: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Productos reales desde InventoryViewModel
    val inventoryVm: InventoryViewModel = hiltViewModel()
    val products by inventoryVm.products.collectAsStateWithLifecycle()
    val activeProducts = remember(products) { 
        try {
            products.filter { it.isActive }
        } catch (e: Exception) {
            println("⚠️ Error filtrando productos activos: ${e.message}")
            emptyList()
        }
    }
    
    // Clientes para mostrar nombre asociado y agrupar
    val customerVm: CustomerViewModel = hiltViewModel()
    val customers by customerVm.customers.collectAsStateWithLifecycle()
    val idToCustomerName = remember(customers) { 
        try {
            customers.associate { it.id to it.name }
        } catch (e: Exception) {
            println("⚠️ Error creando mapa de nombres de clientes: ${e.message}")
            emptyMap()
        }
    }
    val idToCustomer = remember(customers) { 
        try {
            customers.associateBy { it.id }
        } catch (e: Exception) {
            println("⚠️ Error creando mapa de clientes: ${e.message}")
            emptyMap()
        }
    }
    
    // Estado para filtros y agrupación
    var query by remember { mutableStateOf("") }
    var filterByCustomer by remember { mutableStateOf<String?>(null) } // null = todos, "none" = sin cliente
    var showFilters by remember { mutableStateOf(false) }
    
    // Estado para secciones expandibles (cliente -> expandido/colapsado)
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }
    
    // Agrupar colecciones por cliente
    val groupedCollections = remember(collections, query, filterByCustomer, idToCustomerName) {
        val q = query.trim().lowercase()
        val filtered = collections.filter { collection ->
            // Filtrar por búsqueda
            val matchesQuery = q.isEmpty() || 
                collection.name.lowercase().contains(q) || 
                (collection.description?.lowercase()?.contains(q) == true) ||
                (collection.associatedCustomerIds.firstOrNull()?.let { idToCustomerName[it]?.lowercase()?.contains(q) } == true)
            
            // Filtrar por cliente
            val matchesFilter = when (filterByCustomer) {
                null -> true // Mostrar todos
                "none" -> collection.associatedCustomerIds.isEmpty() // Sin cliente
                else -> collection.associatedCustomerIds.contains(filterByCustomer) // Cliente específico
            }
            
            matchesQuery && matchesFilter
        }
        
        // Agrupar por cliente
        val grouped = mutableMapOf<String, MutableList<Collection>>()
        val noCustomerKey = "_sin_cliente_"
        
        filtered.forEach { collection ->
            val customerId = collection.associatedCustomerIds.firstOrNull()
            val key = if (customerId != null && idToCustomerName.containsKey(customerId)) {
                customerId
            } else {
                noCustomerKey
            }
            grouped.getOrPut(key) { mutableListOf() }.add(collection)
        }
        
        // Ordenar: primero clientes (alfabético), luego sin cliente
        grouped.toList().sortedBy { (key, _) ->
            when (key) {
                noCustomerKey -> "zzz" // Sin cliente al final
                else -> idToCustomerName[key] ?: "zzz"
            }
        }
    }

    // Overlay de carga inicial hasta que llegue primera lista (evita parpadeo)
    var isInitialLoading by remember { mutableStateOf(true) }
    LaunchedEffect(collections.isEmpty()) {
        if (collections.isEmpty()) return@LaunchedEffect
        isInitialLoading = false
    }

    // El topbar se maneja desde MainScreen
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCollection,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva Colección")
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            if (isInitialLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Estadísticas rápidas (mover al top)
                item {
                    // Calcular estadísticas
                    val totalCollections = collections.size
                    val totalGroups = groupedCollections.size
                    val activeCollections = collections.count { it.status == CollectionStatus.ACTIVE }
                    
                    UnifiedStatsCard(
                        title = "📚 Resumen de Colecciones",
                        stats = listOf(
                            StatData(
                                label = "Total Colecciones",
                                value = totalCollections.toString(),
                                icon = StatIcon.Vector(Icons.Filled.Group),
                                color = BrandColors.blueLilac
                            ),
                            StatData(
                                label = "Grupos",
                                value = totalGroups.toString(),
                                icon = StatIcon.Vector(Icons.Filled.Group),
                                color = BrandColors.turquoise
                            ),
                            StatData(
                                label = "Activas",
                                value = activeCollections.toString(),
                                icon = StatIcon.Vector(Icons.Filled.CheckCircle),
                                color = BrandColors.turquoiseLight
                            )
                        )
                    )
                }

                // Barra de búsqueda y filtros
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Buscar colecciones...") },
                                placeholder = { Text("Buscar por nombre, cliente...") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (query.isNotEmpty()) {
                                        IconButton(onClick = { query = "" }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Limpiar")
                                        }
                                    }
                                },
                                singleLine = true
                            )
                            IconButton(
                                onClick = { showFilters = !showFilters },
                                modifier = Modifier
                                    .background(
                                        if (filterByCustomer != null) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        DesignTokens.buttonShape
                                    )
                                    .padding(DesignTokens.smallSpacing)
                            ) {
                                Icon(
                                    Icons.Filled.FilterList,
                                    contentDescription = "Filtros",
                                    tint = if (filterByCustomer != null) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Panel de filtros expandible
                        AnimatedVisibility(
                            visible = showFilters,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(DesignTokens.itemSpacing),
                                    verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                                ) {
                                    Text(
                                        text = "Filtrar por cliente",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                                    ) {
                                        FilterChip(
                                            selected = filterByCustomer == null,
                                            onClick = { filterByCustomer = null },
                                            label = { Text("Todos") }
                                        )
                                        FilterChip(
                                            selected = filterByCustomer == "none",
                                            onClick = { filterByCustomer = if (filterByCustomer == "none") null else "none" },
                                            label = { Text("Sin cliente") }
                                        )
                                    }
                                    // Filtros por cliente específico
                                    if (customers.isNotEmpty()) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            items(customers) { customer ->
                                                FilterChip(
                                                    selected = filterByCustomer == customer.id,
                                                    onClick = { 
                                                        filterByCustomer = if (filterByCustomer == customer.id) null else customer.id
                                                    },
                                                    label = { Text(customer.name) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Mostrar mensaje si no hay resultados
                if (groupedCollections.isEmpty()) {
                    item {
                        EmptyCollectionState(
                            hasFilters = query.isNotEmpty() || filterByCustomer != null,
                            onAddCollectionClick = onAddCollection,
                            onClearFilters = { 
                                query = ""
                                filterByCustomer = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Agrupar por cliente con secciones expandibles
                    items(groupedCollections.size) { index ->
                        val (customerKey, collectionsList) = groupedCollections[index]
                        val isNoCustomer = customerKey == "_sin_cliente_"
                        val customer = if (!isNoCustomer) idToCustomer[customerKey] else null
                        val customerName = if (!isNoCustomer) (idToCustomerName[customerKey] ?: "Cliente Desconocido") else "Sin Cliente Asociado"
                        
                        // Estado de expansión (por defecto expandido)
                        val isExpanded = expandedSections.getOrDefault(customerKey, true)
                        
                        // Sección agrupada por cliente
                        CustomerCollectionSection(
                            customerName = customerName,
                            customer = customer,
                            collections = collectionsList,
                            isExpanded = isExpanded,
                            onExpandChange = { expandedSections[customerKey] = it },
                            activeProducts = activeProducts,
                            idToCustomerName = idToCustomerName,
                            viewModel = viewModel,
                            context = context,
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            onEditCollection = onEditCollection,
                            onOpenChat = onOpenChat,
                            onViewResponses = onViewResponses,
                            onDeleteCollection = { collectionId ->
                                viewModel.deleteCollection(collectionId)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Colección eliminada")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 📚 SECCIÓN DE COLECCIONES POR CLIENTE
 * 
 * Muestra un grupo de colecciones agrupadas por cliente con sección expandible/colapsable.
 */
@Composable
private fun CustomerCollectionSection(
    customerName: String,
    customer: Customer?,
    collections: List<Collection>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    activeProducts: List<Product>,
    idToCustomerName: Map<String, String>,
    viewModel: CollectionViewModel,
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onEditCollection: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onViewResponses: (String) -> Unit,
    onDeleteCollection: (String) -> Unit
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header de la sección (clickeable para expandir/colapsar)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange(!isExpanded) },
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = "Colecciones de clientes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(DesignTokens.iconSize)
                        )
                        Column {
                            Text(
                                text = customerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${collections.size} colección${if (collections.size != 1) "es" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Template común del grupo (si todas tienen el mismo)
                        val commonTemplate = collections.map { it.webTemplate }.distinct().singleOrNull()
                        commonTemplate?.let { template ->
                            Surface(
                                shape = DesignTokens.buttonShape,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(end = DesignTokens.smallSpacing)
                            ) {
                                Text(
                                    text = template.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = DesignTokens.smallSpacing, vertical = DesignTokens.compactSpacing),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = { onExpandChange(!isExpanded) },
                            modifier = Modifier
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) "Contraer sección de colecciones" else "Expandir sección de colecciones",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Contenido expandible
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.smallSpacing, vertical = DesignTokens.smallSpacing),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                ) {
                    collections.forEach { collection ->
                        val productCount = collection.getUniqueProductCount()
                        val collectionCustomerName = collection.associatedCustomerIds.firstOrNull()?.let { idToCustomerName[it] }
                        val responseCount by viewModel.getResponseCount(collection.id).collectAsStateWithLifecycle()
                        
                        var showMenu by remember { mutableStateOf(false) }
                        var showDeleteDialog by remember { mutableStateOf(false) }
                        
                        EnhancedCollectionCard(
                            collection = collection,
                            productCount = productCount,
                            customerName = collectionCustomerName,
                            customerId = collection.associatedCustomerIds.firstOrNull(),
                            responseCount = responseCount,
                            onClick = { onEditCollection(collection.id) },
                            onChatClick = { if (collection.enableChat) onOpenChat(collection.id) },
                            onShareClientPortalClick = {
                                val customerId = collection.associatedCustomerIds.firstOrNull()
                                if (customerId != null) {
                                    scope.launch {
                                        try {
                                            val token = viewModel.getOrGenerateCustomerToken(customerId)
                                            val portalUrl = viewModel.generateCustomerPortalLinkWithToken(token)
                                            
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Portal del Cliente", portalUrl)
                                            clipboard.setPrimaryClip(clip)
                                            
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, portalUrl)
                                                putExtra(Intent.EXTRA_SUBJECT, "Portal del Cliente - ${collection.name}")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Compartir portal del cliente"))
                                            
                                            snackbarHostState.showSnackbar("Link del portal copiado al portapapeles")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Error: ${e.message}")
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("No hay cliente asociado a esta colección")
                                    }
                                }
                            },
                            onViewClientPortalClick = {
                                val customerId = collection.associatedCustomerIds.firstOrNull()
                                if (customerId != null) {
                                    scope.launch {
                                        try {
                                            val token = viewModel.getOrGenerateCustomerToken(customerId)
                                            val portalUrl = viewModel.generateCustomerPortalLinkWithToken(token)
                                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(portalUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Error: ${e.message}")
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("No hay cliente asociado a esta colección")
                                    }
                                }
                            },
                            onViewResponsesClick = { onViewResponses(collection.id) },
                            onEditClick = { onEditCollection(collection.id) },
                            onDeleteClick = { showDeleteDialog = true },
                            onCopyLinkClick = {
                                val customerId = collection.associatedCustomerIds.firstOrNull()
                                if (customerId != null) {
                                    scope.launch {
                                        try {
                                            val token = viewModel.getOrGenerateCustomerToken(customerId)
                                            val portalUrl = viewModel.generateCustomerPortalLinkWithToken(token)
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Portal del Cliente", portalUrl)
                                            clipboard.setPrimaryClip(clip)
                                            snackbarHostState.showSnackbar("Link del portal copiado")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Error: ${e.message}")
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("No hay cliente asociado")
                                    }
                                }
                            },
                            showMenu = showMenu,
                            onShowMenuChange = { showMenu = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Eliminar colección") },
                                text = { Text("¿Estás seguro de que quieres eliminar \"${collection.name}\"? Esta acción no se puede deshacer.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onDeleteCollection(collection.id)
                                            showDeleteDialog = false
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) { Text("Eliminar") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedCollectionCard(
    collection: Collection,
    productCount: Int,
    customerName: String?,
    customerId: String?,
    responseCount: Int,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    onShareClientPortalClick: () -> Unit,
    onViewClientPortalClick: () -> Unit,
    onViewResponsesClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Obtener color de la colección (personalizado o automático)
    val collectionColor = remember(collection.id, collection.color) {
        CollectionColors.getColor(collection.id, collection.color)
    }
    
    // Obtener colores del gradiente según el estado
    val gradientColors = remember(collectionColor, collection.status) {
        CollectionColors.getColorsForStatus(collectionColor, collection.status)
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = DesignTokens.cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Brush.linearGradient(gradientColors))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.cardPadding, vertical = DesignTokens.itemSpacing)
                        .padding(end = 56.dp),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = collection.status, onDark = true)
                        Text(
                            text = collection.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!collection.description.isNullOrBlank()) {
                        Text(
                            text = collection.description.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    customerName?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cliente:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                            CustomerBadge(name = it)
                        }
                    }
                }

                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = { onShowMenuChange(true) },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Menú de acciones de la colección",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { onShowMenuChange(false) }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                onShowMenuChange(false)
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar colección")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Abrir chat") },
                            onClick = {
                                onShowMenuChange(false)
                                onChatClick()
                            },
                            enabled = collection.enableChat,
                            leadingIcon = {
                                Icon(Icons.Filled.Message, contentDescription = "Abrir chat de la colección")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copiar enlace") },
                            onClick = {
                                onShowMenuChange(false)
                                onCopyLinkClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Link, contentDescription = "Copiar enlace de la colección")
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ver pedidos")
                                    if (responseCount > 0) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = responseCount.toString(),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                onShowMenuChange(false)
                                onViewResponsesClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.ListAlt, contentDescription = "Ver pedidos de la colección")
                            },
                            enabled = responseCount > 0
                        )
                        if (customerId != null) {
                            DropdownMenuItem(
                                text = { Text("Compartir portal") },
                                onClick = {
                                    onShowMenuChange(false)
                                    onShareClientPortalClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Share, contentDescription = "Compartir portal del cliente")
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Eliminar",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                onShowMenuChange(false)
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // Acciones unificadas de la colección
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
            ) {
                // Botones de acción unificados: Chat, Compartir, Pedidos/Portal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                ) {
                    // Botón de Chat
                    CollectionQuickAction(
                        icon = Icons.Filled.ChatBubble,
                        label = "Chat",
                        onClick = onChatClick,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        enabled = collection.enableChat,
                        badge = if (collection.enableChat) null else null
                    )
                    
                    // Botón de Compartir/Portal
                    CollectionQuickAction(
                        icon = if (customerId != null) Icons.Filled.Dashboard else Icons.Filled.Share,
                        label = if (customerId != null) "Portal" else "Compartir",
                        onClick = if (customerId != null) onViewClientPortalClick else onShareClientPortalClick,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        enabled = true
                    )
                    
                    // Botón de Pedidos
                    CollectionQuickAction(
                        icon = Icons.Filled.ListAlt,
                        label = "Pedidos",
                        onClick = onViewResponsesClick,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        enabled = responseCount > 0,
                        badge = if (responseCount > 0) responseCount.toString() else null
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: CollectionStatus, onDark: Boolean = false) {
    val text = when (status) {
        CollectionStatus.DRAFT -> "Borrador"
        CollectionStatus.ACTIVE -> "Activa"
        CollectionStatus.SHARED -> "Compartida"
        CollectionStatus.ARCHIVED -> "Archivada"
    }

    val baseColor = when (status) {
        CollectionStatus.DRAFT -> MaterialTheme.colorScheme.tertiary
        CollectionStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        CollectionStatus.SHARED -> MaterialTheme.colorScheme.secondary
        CollectionStatus.ARCHIVED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val background = if (onDark) {
        Color.White.copy(alpha = 0.2f)
    } else {
        baseColor.copy(alpha = 0.18f)
    }

    val contentColor = if (onDark) {
        Color.White
    } else {
        baseColor
    }

    Surface(
        color = background,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = DesignTokens.smallSpacing, vertical = DesignTokens.compactSpacing),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CustomerBadge(name: String) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = DesignTokens.chipShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DesignTokens.smallSpacing, vertical = DesignTokens.compactSpacing),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Cliente",
                tint = Color.White,
                modifier = Modifier.size(DesignTokens.smallIconSize)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CollectionMetricChip(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CollectionQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    badge: String? = null
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = DesignTokens.buttonShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = color.copy(alpha = 0.12f),
            contentColor = color,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        badge?.let {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                color = color,
                shape = CircleShape
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * 🎨 CHIP DE TEMPLATE CON PREVIEW
 * 
 * Muestra un chip con un preview visual del template.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplatePreviewChip(
    template: CollectionWebTemplate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val templateColor = androidx.compose.ui.graphics.Color(
        android.graphics.Color.parseColor(template.getColor())
    )
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preview de color del template
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = templateColor,
                            shape = CircleShape
                        )
                )
            Text(
                    text = template.displayName,
                    fontSize = 12.sp
                )
            }
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = templateColor.copy(alpha = 0.15f),
            selectedLabelColor = templateColor,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

/**
 * 🖼️ PREVIEW VISUAL DEL TEMPLATE
 * 
 * Muestra un preview visual de cómo se vería la mini-web con el template seleccionado.
 */
@Composable
private fun TemplatePreviewCard(
    template: CollectionWebTemplate,
    modifier: Modifier = Modifier
) {
    val templateColor = androidx.compose.ui.graphics.Color(
        android.graphics.Color.parseColor(template.getColor())
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (template) {
                CollectionWebTemplate.DARK -> androidx.compose.ui.graphics.Color(0xFF1F2937)
                CollectionWebTemplate.MINIMAL -> MaterialTheme.colorScheme.surface
                CollectionWebTemplate.CLASSIC -> androidx.compose.ui.graphics.Color(0xFFF9FAFB)
                CollectionWebTemplate.MODERN -> androidx.compose.ui.graphics.Color(0xFFF3F4F6)
                CollectionWebTemplate.COLORFUL -> androidx.compose.ui.graphics.Color(0xFFF0FDF4)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header del preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(8.dp)
                        .background(
                            color = if (template == CollectionWebTemplate.DARK) {
                                // Usar color más claro para que sea visible en fondo oscuro
                                androidx.compose.ui.graphics.Color(0xFF6366F1)
                            } else {
                                templateColor
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (template == CollectionWebTemplate.DARK) {
                                // Usar color más claro para que sea visible
                                androidx.compose.ui.graphics.Color(0xFF6366F1).copy(alpha = 0.7f)
                            } else {
                                templateColor.copy(alpha = 0.5f)
                            },
                            shape = CircleShape
                        )
                )
            }
            
            // Contenido del preview (simulando productos)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Producto 1
                PreviewProductItem(
                    color = templateColor,
                    isDark = template == CollectionWebTemplate.DARK,
                    modifier = Modifier.weight(1f)
                )
                // Producto 2
                PreviewProductItem(
                    color = templateColor,
                    isDark = template == CollectionWebTemplate.DARK,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer del preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color = if (template == CollectionWebTemplate.DARK) {
                            // Usar color más claro para que sea visible en fondo oscuro
                            androidx.compose.ui.graphics.Color(0xFF6366F1).copy(alpha = 0.4f)
                        } else {
                            templateColor.copy(alpha = 0.3f)
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * 📦 ITEM DE PRODUCTO PARA PREVIEW
 */
@Composable
private fun PreviewProductItem(
    color: androidx.compose.ui.graphics.Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    // Para template DARK, usar colores más claros para visibilidad
    val accentColor = if (isDark) {
        androidx.compose.ui.graphics.Color(0xFF6366F1) // Índigo para contraste
    } else {
        color
    }
    
    Column(
        modifier = modifier
            .height(50.dp)
            .background(
                color = if (isDark) {
                    // Fondo más claro para contraste con el fondo oscuro del card
                    androidx.compose.ui.graphics.Color(0xFF3a3a3a)
                } else {
                    color.copy(alpha = 0.1f)
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    color = accentColor.copy(alpha = if (isDark) 0.5f else 0.3f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
        )
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(6.dp)
                .background(
                    color = accentColor.copy(alpha = if (isDark) 0.7f else 0.4f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp)
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCollectionScreen(
    onDone: () -> Unit,
    collectionId: String? = null,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = hiltViewModel(),
    onNavigateToProductDetail: (String) -> Unit = {}
) {
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val existing = remember(collections, collectionId) { collections.firstOrNull { it.id == collectionId } }
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
      var enableChat by remember(existing) { mutableStateOf(existing?.enableChat ?: true) }
      var status by remember(existing) { mutableStateOf(existing?.status ?: CollectionStatus.DRAFT) }
      var webTemplate by remember(existing) { mutableStateOf(existing?.webTemplate ?: CollectionWebTemplate.MODERN) }
      // Color de la colección (si no hay color personalizado, se asigna automáticamente)
      var collectionColor by remember(existing) { 
          mutableStateOf(existing?.color ?: run {
              val autoColor = CollectionColors.getColorById(existing?.id ?: UUID.randomUUID().toString())
              String.format("#%02X%02X%02X",
                  (autoColor.red * 255).toInt(),
                  (autoColor.green * 255).toInt(),
                  (autoColor.blue * 255).toInt()
              )
          })
      }
    val inventoryVm: InventoryViewModel = hiltViewModel()
    val products by inventoryVm.products.collectAsStateWithLifecycle()
    val activeProducts = remember(products) { products.filter { it.isActive } }
    var productQuery by remember { mutableStateOf("") }
    val filteredProducts = remember(activeProducts, productQuery) {
        val q = productQuery.trim().lowercase()
        if (q.isEmpty()) activeProducts else activeProducts.filter { p ->
            p.name.lowercase().contains(q) || (p.sku.lowercase().contains(q))
        }
    }
      // Mantener items con sus propiedades (precio especial, destacado, notas, orden)
      var collectionItems by remember(existing) { 
          mutableStateOf<Map<String, CollectionItem>>(
              existing?.items?.associateBy { it.productId } ?: emptyMap()
          )
      }
      var selectedIds by remember(collectionItems) { mutableStateOf(collectionItems.keys.toSet()) }
    val selectedProducts = remember(selectedIds, activeProducts) { activeProducts.filter { selectedIds.contains(it.id) } }
      val productCount = remember(collectionItems) { 
          collectionItems.size
      }

    val customerVm: CustomerViewModel = hiltViewModel()
    val customers by customerVm.customers.collectAsStateWithLifecycle()
    var showCustomerDialog by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var selectedCustomerId by remember(existing) { mutableStateOf(existing?.associatedCustomerIds?.firstOrNull()) }
    val selectedCustomer = remember(selectedCustomerId, customers) {
        customers.firstOrNull { it.id == selectedCustomerId }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
      var showItemEditDialog by remember { mutableStateOf<String?>(null) }

    val isEditing = existing != null

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.largeSpacing)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
            ) {
                UnifiedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    modifier = Modifier.fillMaxWidth()
                )
                UnifiedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    modifier = Modifier.fillMaxWidth()
                )
            }

              // Estado de la colección
              UnifiedCard(
                  modifier = Modifier.fillMaxWidth()
              ) {
                  Column(
                      modifier = Modifier.padding(DesignTokens.cardPadding),
                      verticalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                      Text(
                          text = "Estado de la Colección",
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold
                      )
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                          CollectionStatus.values().forEach { collectionStatus ->
                              FilterChip(
                                  selected = status == collectionStatus,
                                  onClick = { status = collectionStatus },
                                  label = {
                                      Text(
                                          text = when (collectionStatus) {
                                              CollectionStatus.DRAFT -> "Borrador"
                                              CollectionStatus.ACTIVE -> "Activa"
                                              CollectionStatus.SHARED -> "Compartida"
                                              CollectionStatus.ARCHIVED -> "Archivada"
                                          },
                                          fontSize = 12.sp
                                      )
                                  },
                                  modifier = Modifier.weight(1f)
                              )
                          }
                      }
                      Text(
                          text = when (status) {
                              CollectionStatus.DRAFT -> "Solo tú puedes ver esta colección"
                              CollectionStatus.ACTIVE -> "Colección activa, lista para compartir"
                              CollectionStatus.SHARED -> "Colección compartida públicamente"
                              CollectionStatus.ARCHIVED -> "Colección archivada"
                          },
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                  }
              }

              // Habilitar Chat
              UnifiedCard(
                  modifier = Modifier.fillMaxWidth()
              ) {
                  Row(
                      modifier = Modifier
                          .fillMaxWidth()
                          .padding(DesignTokens.cardPadding),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      Column(modifier = Modifier.weight(1f)) {
                          Text(
                              text = "💬 Habilitar Chat",
                              style = MaterialTheme.typography.titleSmall,
                              fontWeight = FontWeight.Medium
                          )
                          Text(
                              text = "Permite que los clientes chateen contigo desde la mini-web",
                              style = MaterialTheme.typography.bodySmall,
                              color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                      }
                      Switch(
                          checked = enableChat,
                          onCheckedChange = { enableChat = it }
                      )
                  }
              }

              // Color de la colección (desplegable)
              var isColorSectionExpanded by remember { mutableStateOf(false) }
              UnifiedCard(
                  modifier = Modifier.fillMaxWidth()
              ) {
                  Column(
                      modifier = Modifier.padding(DesignTokens.cardPadding),
                      verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                  ) {
                      Row(
                          modifier = Modifier
                              .fillMaxWidth()
                              .clickable { isColorSectionExpanded = !isColorSectionExpanded },
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                      ) {
                          Text(
                              text = "🎨 Color de la Colección",
                              style = MaterialTheme.typography.titleMedium,
                              fontWeight = FontWeight.Bold
                          )
                          Icon(
                              imageVector = if (isColorSectionExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                              contentDescription = if (isColorSectionExpanded) "Colapsar" else "Expandir",
                              tint = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                      }
                      
                      AnimatedVisibility(
                          visible = isColorSectionExpanded,
                          enter = expandVertically() + fadeIn(),
                          exit = shrinkVertically() + fadeOut()
                      ) {
                          Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)) {
                              Text(
                                  text = "El color que aparece en la cabecera de esta colección. Si no seleccionas uno, se asignará automáticamente.",
                                  style = MaterialTheme.typography.bodySmall,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant
                              )
                              
                              // Vista previa del color actual
                              Row(
                                  modifier = Modifier.fillMaxWidth(),
                                  horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
                                  verticalAlignment = Alignment.CenterVertically
                              ) {
                                  // Color seleccionado
                                  Surface(
                                      modifier = Modifier
                                          .size(48.dp)
                                          .clip(CircleShape),
                                      color = try {
                                          Color(android.graphics.Color.parseColor(collectionColor))
                                      } catch (e: Exception) {
                                          CollectionColors.getColorById(existing?.id ?: UUID.randomUUID().toString())
                                      },
                                      shape = CircleShape,
                                      border = BorderStroke(
                                          width = 2.dp,
                                          color = MaterialTheme.colorScheme.outline
                                      )
                                  ) {}
                                  
                                  Column(modifier = Modifier.weight(1f)) {
                                      Text(
                                          text = "Color actual",
                                          style = MaterialTheme.typography.labelMedium,
                                          fontWeight = FontWeight.Medium
                                      )
                                      Text(
                                          text = collectionColor,
                                          style = MaterialTheme.typography.bodySmall,
                                          color = MaterialTheme.colorScheme.onSurfaceVariant
                                      )
                                  }
                              }
                              
                              // Selector de color (paleta rápida)
                              LazyRow(
                                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                                  contentPadding = PaddingValues(horizontal = 4.dp)
                              ) {
                                  items(CollectionColors.palette.take(12)) { colorOption ->
                                      val colorHex = String.format("#%02X%02X%02X",
                                          (colorOption.red * 255).toInt(),
                                          (colorOption.green * 255).toInt(),
                                          (colorOption.blue * 255).toInt()
                                      )
                                      Surface(
                                          modifier = Modifier
                                              .size(40.dp)
                                              .clip(CircleShape)
                                              .clickable { collectionColor = colorHex },
                                          color = colorOption,
                                          shape = CircleShape,
                                          border = if (collectionColor == colorHex) {
                                              BorderStroke(
                                                  width = 3.dp,
                                                  color = MaterialTheme.colorScheme.primary
                                              )
                                          } else null
                                      ) {
                                          if (collectionColor == colorHex) {
                                              Box(
                                                  modifier = Modifier.fillMaxSize(),
                                                  contentAlignment = Alignment.Center
                                              ) {
                                                  Icon(
                                                      imageVector = Icons.Filled.CheckCircle,
                                                      contentDescription = "Seleccionado",
                                                      tint = Color.White,
                                                      modifier = Modifier.size(20.dp)
                                                  )
                                              }
                                          }
                                      }
                                  }
                              }
                      
                              TextButton(
                                  onClick = { collectionColor = "" },
                                  modifier = Modifier.fillMaxWidth()
                              ) {
                                  Text("Usar color automático")
                              }
                          }
                      }
                  }
              }

              // Template para Portal del Cliente (desplegable)
              var isTemplateSectionExpanded by remember { mutableStateOf(false) }
              UnifiedCard(
                  modifier = Modifier.fillMaxWidth()
              ) {
                  Column(
                      modifier = Modifier.padding(DesignTokens.cardPadding),
                      verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                  ) {
                      Row(
                          modifier = Modifier
                              .fillMaxWidth()
                              .clickable { isTemplateSectionExpanded = !isTemplateSectionExpanded },
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                      ) {
                          Text(
                              text = "🎨 Template para Portal del Cliente",
                              style = MaterialTheme.typography.titleMedium,
                              fontWeight = FontWeight.Bold
                          )
                          Icon(
                              imageVector = if (isTemplateSectionExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                              contentDescription = if (isTemplateSectionExpanded) "Colapsar" else "Expandir",
                              tint = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                      }
                      
                      AnimatedVisibility(
                          visible = isTemplateSectionExpanded,
                          enter = expandVertically() + fadeIn(),
                          exit = shrinkVertically() + fadeOut()
                      ) {
                          Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)) {
                              Text(
                                  text = "Elige el estilo visual del portal del cliente",
                                  style = MaterialTheme.typography.bodySmall,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant
                              )
                              
                              // Mensaje informativo sobre cambio global si hay cliente asociado
                              if (selectedCustomerId != null) {
                                  Surface(
                                      modifier = Modifier.fillMaxWidth(),
                                      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                      shape = RoundedCornerShape(8.dp),
                                      border = BorderStroke(
                                          width = 1.dp,
                                          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                      )
                                  ) {
                                      Column(
                                          modifier = Modifier
                                              .fillMaxWidth()
                                              .padding(12.dp),
                                          verticalArrangement = Arrangement.spacedBy(8.dp)
                                      ) {
                                          Row(
                                              horizontalArrangement = Arrangement.spacedBy(8.dp),
                                              verticalAlignment = Alignment.CenterVertically
                                          ) {
                                              Icon(
                                                  imageVector = Icons.Filled.Info,
                                                  contentDescription = null,
                                                  tint = MaterialTheme.colorScheme.primary,
                                                  modifier = Modifier.size(20.dp)
                                              )
                                              Text(
                                                  text = "Template Global del Cliente",
                                                  style = MaterialTheme.typography.titleSmall,
                                                  fontWeight = FontWeight.Bold,
                                                  color = MaterialTheme.colorScheme.primary
                                              )
                                          }
                                          Text(
                                              text = "Al cambiar el template de esta colección, se aplicará automáticamente a TODAS las colecciones del cliente \"${selectedCustomer?.name ?: "cliente"}\" en su portal completo. Esto garantiza una experiencia visual consistente y profesional.",
                                              style = MaterialTheme.typography.bodySmall,
                                              color = MaterialTheme.colorScheme.onPrimaryContainer,
                                              lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2
                                          )
                                          Surface(
                                              modifier = Modifier.fillMaxWidth(),
                                              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                              shape = RoundedCornerShape(4.dp)
                                          ) {
                                              Text(
                                                  text = "💡 Tip: Si el cliente tiene múltiples colecciones, todas compartirán el mismo template visual.",
                                                  style = MaterialTheme.typography.bodySmall,
                                                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                  modifier = Modifier.padding(8.dp),
                                                  fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                              )
                                          }
                                      }
                                  }
                              } else {
                                  // Mensaje cuando no hay cliente asociado
                                  Surface(
                                      modifier = Modifier.fillMaxWidth(),
                                      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                      shape = RoundedCornerShape(8.dp)
                                  ) {
                                      Row(
                                          modifier = Modifier
                                              .fillMaxWidth()
                                              .padding(12.dp),
                                          horizontalArrangement = Arrangement.spacedBy(8.dp),
                                          verticalAlignment = Alignment.CenterVertically
                                      ) {
                                          Icon(
                                              imageVector = Icons.Filled.Info,
                                              contentDescription = null,
                                              tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                              modifier = Modifier.size(20.dp)
                                          )
                                          Text(
                                              text = "Asocia un cliente para aplicar el template globalmente a todas sus colecciones.",
                                              style = MaterialTheme.typography.bodySmall,
                                              color = MaterialTheme.colorScheme.onSurfaceVariant,
                                              modifier = Modifier.weight(1f)
                                          )
                                      }
                                  }
                              }
                              
                              // Selector de templates con preview
                              LazyRow(
                                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                                  contentPadding = PaddingValues(horizontal = 4.dp)
                              ) {
                                  items(CollectionWebTemplate.values()) { template ->
                                      TemplatePreviewChip(
                                          template = template,
                                          isSelected = webTemplate == template,
                                          onClick = { webTemplate = template }
                                      )
                                  }
                              }
                              
                              // Preview visual del template seleccionado
                              TemplatePreviewCard(
                                  template = webTemplate,
                                  modifier = Modifier.fillMaxWidth()
                              )
                      
                              Text(
                                  text = webTemplate.description,
                                  style = MaterialTheme.typography.bodySmall,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                                  fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                              )
                          }
                      }
                  }
              }

            // Cliente asociado
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                val infoShape = RoundedCornerShape(12.dp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "👤 Cliente Asociado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Asocia esta colección a un cliente específico (opcional). Útil para crear catálogos personalizados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedCustomer != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = infoShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            tonalElevation = 1.dp,
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = selectedCustomer.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        selectedCustomer.companyName?.takeIf { it.isNotBlank() }?.let { company ->
                                            Text(
                                                text = company,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                selectedCustomer.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Phone,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = phone,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                selectedCustomer.email?.takeIf { it.isNotBlank() }?.let { email ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Email,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = email,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                selectedCustomer.address?.takeIf { it.isNotBlank() }?.let { address ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                selectedCustomer.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                                    Text(
                                        text = "Nota: $notes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = infoShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Ningún cliente seleccionado. Selecciona uno para personalizar esta colección.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCustomerId != null) {
                            TextButton(onClick = { selectedCustomerId = null }) {
                                Text(
                                    text = "Quitar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                        }

                        UnifiedOutlinedButton(
                            text = if (selectedCustomerId != null) "Cambiar cliente" else "Seleccionar cliente",
                            onClick = { showCustomerDialog = true },
                            modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                            fullWidth = false,
                            compact = true
                        )
                    }
                }
            }

            // BottomSheet para selección de clientes
            if (showCustomerDialog) {
                com.negociolisto.app.ui.components.CustomerSelectionBottomSheet(
                    customers = customers,
                    selectedCustomer = selectedCustomer,
                    searchQuery = customerSearchQuery,
                    onSearchQueryChange = { customerSearchQuery = it },
                    onCustomerSelected = { customer ->
                        selectedCustomerId = customer.id
                        showCustomerDialog = false
                        customerSearchQuery = ""
                        keyboardController?.hide()
                    },
                    onDismiss = {
                        showCustomerDialog = false
                        customerSearchQuery = ""
                        keyboardController?.hide()
                    },
                    onCreateNewClick = {
                        showCustomerDialog = false
                        // TODO: Abrir diálogo de creación rápida de cliente si está disponible
                    }
                )
            }

            // Productos seleccionados con carrusel
            if (selectedProducts.isNotEmpty()) {
                ProductCarousel(
                    products = selectedProducts,
                    onProductClick = { product ->
                        showItemEditDialog = product.id
                    },
                    onProductRemove = { product ->
                        collectionItems = collectionItems - product.id
                        selectedIds = selectedIds - product.id
                    },
                    title = "✅ Productos Seleccionados"
                )
            }
            
            // Dialog para editar item de colección (precio especial, destacado, notas)
            showItemEditDialog?.let { productId ->
                val product = activeProducts.firstOrNull { it.id == productId }
                val item = collectionItems[productId]
                if (product != null) {
                    CollectionItemEditDialog(
                        product = product,
                        item = item,
                        onSave = { updatedItem ->
                            collectionItems = collectionItems + (productId to updatedItem)
                            showItemEditDialog = null
                        },
                        onDismiss = { showItemEditDialog = null }
                    )
                }
            }

            // Disponibles para agregar con grid y paginación
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding)
                ) {
                    Text(
                        text = "➕ Agregar Productos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                    UnifiedTextField(
                        value = productQuery, 
                        onValueChange = { productQuery = it }, 
                        label = "Buscar producto", 
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    val available = remember(filteredProducts, selectedIds) { 
                        filteredProducts.filterNot { selectedIds.contains(it.id) } 
                    }
                    
                    if (available.isEmpty()) {
                        Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                        Text(
                            text = "No hay productos disponibles para agregar", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                        
                        // Paginación: 4 productos por página (grid 2x2)
                        var currentPage by remember(available) { mutableStateOf(0) }
                        val pages = remember(available) { available.chunked(4) }
                        
                        if (pages.isNotEmpty()) {
                            // Grid 2x2 de productos
                            val pageItems = pages[currentPage]
                            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)) {
                                for (rowStart in pageItems.indices step 2) {
                                    Row(
                                        Modifier.fillMaxWidth(), 
                                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                                    ) {
                                        val left = pageItems[rowStart]
                                        CollectionProductGridCard(
                                            product = left,
                                            onClick = {
                                                val newItem = CollectionItem(
                                                    productId = left.id,
                                                    notes = null,
                                                    displayOrder = collectionItems.size,
                                                    isFeatured = false,
                                                    specialPrice = null
                                                )
                                                collectionItems = collectionItems + (left.id to newItem)
                                                selectedIds = selectedIds + left.id
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        val right = pageItems.getOrNull(rowStart + 1)
                                        if (right != null) {
                                            CollectionProductGridCard(
                                                product = right,
                                                onClick = {
                                                    val newItem = CollectionItem(
                                                        productId = right.id,
                                                        notes = null,
                                                        displayOrder = collectionItems.size,
                                                        isFeatured = false,
                                                        specialPrice = null
                                                    )
                                                    collectionItems = collectionItems + (right.id to newItem)
                                                    selectedIds = selectedIds + right.id
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                                
                                // Controles de paginación
                                if (pages.size > 1) {
                                    Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
                                    Row(
                                        Modifier.fillMaxWidth(), 
                                        horizontalArrangement = Arrangement.SpaceBetween, 
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { currentPage = (currentPage - 1).coerceAtLeast(0) }, 
                                            enabled = currentPage > 0
                                        ) { 
                                            Text("Anterior", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text("Página ${currentPage + 1} / ${pages.size}")
                                        Button(
                                            onClick = { currentPage = (currentPage + 1).coerceAtMost(pages.lastIndex) }, 
                                            enabled = currentPage < pages.lastIndex
                                        ) { 
                                            Text("Siguiente", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Productos en la colección
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📦 Productos en la colección",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$productCount ${if (productCount == 1) "producto" else "productos"}", 
                        style = MaterialTheme.typography.titleLarge,
                        color = BrandColors.turquoise,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = if (isEditing) "💾 Actualizar Colección" else "💾 Guardar Colección",
            primaryButtonOnClick = {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                // Ordenar items por displayOrder y asegurar que todos los selectedIds estén en collectionItems
                val sortedItems = collectionItems.values.sortedBy { it.displayOrder }
                    .mapIndexed { index, item ->
                        item.copy(displayOrder = index)
                    }
                
                if (existing == null) {
                    val newId = UUID.randomUUID().toString()
                    val collection = Collection(
                        id = newId,
                        name = name.ifBlank { "Colección" },
                        description = description.ifBlank { null },
                        items = sortedItems,
                        associatedCustomerIds = selectedCustomerId?.let { listOf(it) } ?: emptyList(),
                        createdAt = now,
                        updatedAt = now,
                        status = status,
                        color = collectionColor.takeIf { it.isNotBlank() },
                        enableChat = enableChat,
                        webTemplate = webTemplate
                    )
                    viewModel.addCollection(collection)
                } else {
                    val updated = existing.copy(
                        name = name.ifBlank { existing.name },
                        description = description.ifBlank { null },
                        items = sortedItems,
                        associatedCustomerIds = selectedCustomerId?.let { listOf(it) } ?: emptyList(),
                        updatedAt = now,
                        status = status,
                        color = collectionColor.takeIf { it.isNotBlank() },
                        enableChat = enableChat,
                        webTemplate = webTemplate
                    )
                    viewModel.updateCollection(updated)
                }
                onDone()
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Preview(
    name = "Crear colección - Claro",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun AddEditCollectionScreenPreview() {
    NegocioListoTheme {
        AddEditCollectionPreviewContent()
    }
}

@Preview(
    name = "Crear colección - Oscuro",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AddEditCollectionScreenPreviewDark() {
    NegocioListoTheme {
        AddEditCollectionPreviewContent()
    }
}

@Composable
private fun AddEditCollectionPreviewContent() {
    val sampleDate = LocalDateTime(2024, 3, 15, 10, 0, 0)
    val activeProducts = remember {
        listOf(
            Product(
                id = "product-espresso",
                name = "Cafetera Espresso Pro",
                description = "Acero inoxidable, incluye vaporizador",
                sku = "CF-001",
                purchasePrice = 32000.0,
                salePrice = 54990.0,
                stockQuantity = 12,
                customCategoryId = "kitchen",
                supplier = "CoffeeCorp",
                photoUrl = null,
                createdAt = sampleDate,
                updatedAt = sampleDate
            ),
            Product(
                id = "product-grinder",
                name = "Molino Deluxe",
                description = "Fresas cónicas, 18 niveles de molienda",
                sku = "CF-002",
                purchasePrice = 18000.0,
                salePrice = 32990.0,
                stockQuantity = 8,
                customCategoryId = "kitchen",
                supplier = "CoffeeCorp",
                photoUrl = null,
                createdAt = sampleDate,
                updatedAt = sampleDate
            ),
            Product(
                id = "product-mug",
                name = "Taza Cerámica Premium",
                description = "Edición limitada, incluye caja de regalo",
                sku = "CF-003",
                purchasePrice = 4500.0,
                salePrice = 8990.0,
                stockQuantity = 40,
                customCategoryId = "merch",
                supplier = "CoffeeCorp",
                photoUrl = null,
                createdAt = sampleDate,
                updatedAt = sampleDate
            )
        )
    }
    val sampleCustomers = remember {
        listOf(
            Customer(
                id = "customer-ana",
                name = "Ana Torres",
                companyName = "Café Central",
                phone = "+56 9 1234 5678",
                email = "ana@cafecentral.cl",
                address = "Av. Providencia 1234, Santiago",
                notes = "Prefiere entregas los lunes",
                createdAt = sampleDate,
                totalPurchases = 820000.0,
                lastPurchaseDate = sampleDate
            ),
            Customer(
                id = "customer-luis",
                name = "Luis Martínez",
                companyName = null,
                phone = "+56 9 9876 5432",
                email = null,
                address = "Calle Falsa 123, Santiago",
                notes = null,
                createdAt = sampleDate,
                totalPurchases = 210000.0,
                lastPurchaseDate = sampleDate
            )
        )
    }

    var name by remember { mutableStateOf("Colección Primavera 2025") }
    var description by remember { mutableStateOf("Catálogo fresco con los favoritos de la cafetería") }
    var enableChat by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf(CollectionStatus.ACTIVE) }
    var webTemplate by remember { mutableStateOf(CollectionWebTemplate.MODERN) }
    var collectionColor by remember { mutableStateOf("#009FE3") }
    var productQuery by remember { mutableStateOf("") }
    var collectionItems by remember {
        mutableStateOf(
            activeProducts.take(2).mapIndexed { index, product ->
                product.id to CollectionItem(
                    productId = product.id,
                    notes = if (index == 0) "Incluye espumador" else null,
                    displayOrder = index,
                    isFeatured = index == 0,
                    specialPrice = if (index == 0) product.salePrice * 0.9 else null
                )
            }.toMap()
        )
    }
    var selectedIds by remember { mutableStateOf(collectionItems.keys.toSet()) }
    val selectedProducts = remember(selectedIds, activeProducts) {
        activeProducts.filter { selectedIds.contains(it.id) }
    }
    val filteredProducts = remember(activeProducts, productQuery) {
        val query = productQuery.trim().lowercase()
        if (query.isEmpty()) activeProducts else activeProducts.filter { product ->
            product.name.lowercase().contains(query) || product.sku.lowercase().contains(query)
        }
    }
    val productCount = remember(collectionItems) { 
        collectionItems.size
    }
    var selectedCustomerId by remember { mutableStateOf<String?>(sampleCustomers.firstOrNull()?.id) }
    val customers = sampleCustomers
    val selectedCustomer = remember(selectedCustomerId, customers) {
        selectedCustomerId?.let { id -> customers.firstOrNull { it.id == id } }
    }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showItemEditDialog by remember { mutableStateOf<String?>(null) }
    val isEditing = false

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.largeSpacing)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
            ) {
                UnifiedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    modifier = Modifier.fillMaxWidth()
                )
                UnifiedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Estado de la Colección",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CollectionStatus.values().forEach { collectionStatus ->
                            FilterChip(
                                selected = status == collectionStatus,
                                onClick = { status = collectionStatus },
                                label = {
                                    Text(
                                        text = when (collectionStatus) {
                                            CollectionStatus.DRAFT -> "Borrador"
                                            CollectionStatus.ACTIVE -> "Activa"
                                            CollectionStatus.SHARED -> "Compartida"
                                            CollectionStatus.ARCHIVED -> "Archivada"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Text(
                        text = when (status) {
                            CollectionStatus.DRAFT -> "Solo tú puedes ver esta colección"
                            CollectionStatus.ACTIVE -> "Colección activa, lista para compartir"
                            CollectionStatus.SHARED -> "Colección compartida públicamente"
                            CollectionStatus.ARCHIVED -> "Colección archivada"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "💬 Habilitar Chat",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Permite que los clientes chateen contigo desde la mini-web",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enableChat,
                        onCheckedChange = { enableChat = it }
                    )
                }
            }

            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🎨 Color de la Colección",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "El color que aparece en la cabecera de esta colección. Si no seleccionas uno, se asignará automáticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            color = try {
                                Color(android.graphics.Color.parseColor(collectionColor))
                            } catch (_: Exception) {
                                CollectionColors.getColorById("preview")
                            },
                            shape = CircleShape,
                            border = BorderStroke(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        ) {}

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Color actual",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = collectionColor,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(CollectionColors.palette.take(12)) { colorOption ->
                            val colorHex = String.format(
                                "#%02X%02X%02X",
                                (colorOption.red * 255).toInt(),
                                (colorOption.green * 255).toInt(),
                                (colorOption.blue * 255).toInt()
                            )
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { collectionColor = colorHex },
                                color = colorOption,
                                shape = CircleShape,
                                border = if (collectionColor == colorHex) {
                                    BorderStroke(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else null
                            ) {
                                if (collectionColor == colorHex) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Seleccionado",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { collectionColor = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usar color automático")
                    }
                }
            }

            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                      text = "🎨 Template para Portal del Cliente",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold
                      )
                      Text(
                          text = "Elige el estilo visual del portal del cliente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Template Global del Cliente",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Al cambiar el template de esta colección, se aplicará automáticamente al portal completo del cliente seleccionado. Esto garantiza una experiencia visual consistente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2f
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(CollectionWebTemplate.values()) { template ->
                            TemplatePreviewChip(
                                template = template,
                                isSelected = webTemplate == template,
                                onClick = { webTemplate = template }
                            )
                        }
                    }

                    TemplatePreviewCard(
                        template = webTemplate,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = webTemplate.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                val infoShape = RoundedCornerShape(12.dp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "👤 Cliente Asociado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Asocia esta colección a un cliente específico (opcional). Útil para crear catálogos personalizados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedCustomer != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = infoShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            tonalElevation = 1.dp,
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = selectedCustomer.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        selectedCustomer.companyName?.takeIf { it.isNotBlank() }?.let { company ->
                                            Text(
                                                text = company,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                selectedCustomer.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Phone,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = phone,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                selectedCustomer.email?.takeIf { it.isNotBlank() }?.let { email ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Email,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = email,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                selectedCustomer.address?.takeIf { it.isNotBlank() }?.let { address ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                selectedCustomer.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                                    Text(
                                        text = "Nota: $notes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = infoShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Ningún cliente seleccionado. Selecciona uno para personalizar esta colección.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCustomerId != null) {
                            TextButton(onClick = { selectedCustomerId = null }) {
                                Text(
                                    text = "Quitar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                        }

                        UnifiedOutlinedButton(
                            text = if (selectedCustomerId != null) "Cambiar cliente" else "Seleccionar cliente",
                            onClick = { showCustomerDialog = true },
                            modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                            fullWidth = false,
                            compact = true
                        )
                    }
                }
            }

            if (selectedProducts.isNotEmpty()) {
                ProductCarousel(
                    products = selectedProducts,
                    onProductClick = { product ->
                        showItemEditDialog = product.id
                    },
                    onProductRemove = { product ->
                        collectionItems = collectionItems - product.id
                        selectedIds = selectedIds - product.id
                    },
                    title = "✅ Productos Seleccionados"
                )
            }

            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding)
                ) {
                    Text(
                        text = "➕ Agregar Productos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnifiedTextField(
                        value = productQuery,
                        onValueChange = { productQuery = it },
                        label = "Buscar producto",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val available = remember(filteredProducts, selectedIds) {
                        filteredProducts.filterNot { selectedIds.contains(it.id) }
                    }

                    if (available.isEmpty()) {
                        Text(
                            text = "No hay productos disponibles para agregar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ProductSimpleList(
                            products = available,
                            onProductClick = { product ->
                                val newItem = CollectionItem(
                                    productId = product.id,
                                    notes = null,
                                    displayOrder = collectionItems.size,
                                    isFeatured = false,
                                    specialPrice = null
                                )
                                collectionItems = collectionItems + (product.id to newItem)
                                selectedIds = selectedIds + product.id
                            },
                            onProductRemove = {},
                            title = "Productos Disponibles"
                        )
                    }
                }
            }

            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📦 Productos en la colección",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$productCount ${if (productCount == 1) "producto" else "productos"}",
                        style = MaterialTheme.typography.titleLarge,
                        color = BrandColors.turquoise,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // BottomSheet para selección de clientes
        var customerSearchQuery by remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        
        if (showCustomerDialog) {
            com.negociolisto.app.ui.components.CustomerSelectionBottomSheet(
                customers = customers,
                selectedCustomer = selectedCustomer,
                searchQuery = customerSearchQuery,
                onSearchQueryChange = { customerSearchQuery = it },
                onCustomerSelected = { customer ->
                    selectedCustomerId = customer.id
                    showCustomerDialog = false
                    customerSearchQuery = ""
                    keyboardController?.hide()
                },
                onDismiss = {
                    showCustomerDialog = false
                    customerSearchQuery = ""
                    keyboardController?.hide()
                },
                onCreateNewClick = {
                    showCustomerDialog = false
                    // TODO: Abrir diálogo de creación rápida de cliente si está disponible
                }
            )
        }

        showItemEditDialog?.let { productId ->
            val product = activeProducts.firstOrNull { it.id == productId }
            val item = collectionItems[productId]
            if (product != null) {
                CollectionItemEditDialog(
                    product = product,
                    item = item,
                    onSave = { updatedItem ->
                        collectionItems = collectionItems + (productId to updatedItem)
                        showItemEditDialog = null
                    },
                    onDismiss = { showItemEditDialog = null }
                )
            }
        }

        FixedBottomBar(
            primaryButtonText = if (isEditing) "💾 Actualizar Colección" else "💾 Guardar Colección",
            primaryButtonOnClick = {},
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CollectionItemEditDialog(
    product: Product,
    item: CollectionItem?,
    onSave: (CollectionItem) -> Unit,
    onDismiss: () -> Unit
) {
    var specialPrice by remember(item) { 
        mutableStateOf(item?.specialPrice?.toString() ?: "") 
    }
    var isFeatured by remember(item) { 
        mutableStateOf(item?.isFeatured ?: false) 
    }
    var notes by remember(item) { 
        mutableStateOf(item?.notes ?: "") 
    }
    var displayOrder by remember(item) { 
        mutableStateOf((item?.displayOrder ?: 0).toString()) 
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text(
                    text = "Editar Producto",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Precio especial
                Column {
                    Text(
                        text = "Precio Especial (CLP)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Precio original: ${Formatters.formatClp(product.salePrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = specialPrice,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' || char == ',' }) {
                                specialPrice = it
                            }
                        },
                        label = { Text("Dejar vacío para usar precio original") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Text("$", style = MaterialTheme.typography.bodyLarge)
                        },
                        supportingText = {
                            if (specialPrice.isNotBlank()) {
                                val price = specialPrice.replace(",", ".").toDoubleOrNull()
                                if (price != null) {
                                    Text("Nuevo precio: ${Formatters.formatClp(price)}")
                                }
                            }
                        }
                    )
                }
                
                // Producto destacado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⭐ Producto Destacado",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Mostrar este producto como destacado en la colección",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it }
                    )
                }
                
                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    supportingText = {
                        Text("Estas notas se mostrarán junto al producto")
                    }
                )
                
                // Orden de visualización
                OutlinedTextField(
                    value = displayOrder,
                    onValueChange = { 
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            displayOrder = it
                        }
                    },
                    label = { Text("Orden de visualización") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text("Los productos se ordenan de menor a mayor (0 = primero)")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalPrice = if (specialPrice.isNotBlank()) {
                        specialPrice.replace(",", ".").toDoubleOrNull()
                    } else null
                    
                    val finalOrder = displayOrder.toIntOrNull() ?: (item?.displayOrder ?: 0)
                    
                    val updatedItem = CollectionItem(
                        productId = product.id,
                        notes = notes.ifBlank { null },
                        displayOrder = finalOrder,
                        isFeatured = isFeatured,
                        specialPrice = finalPrice
                    )
                    onSave(updatedItem)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * 🎴 TARJETA DE PRODUCTO EN GRID PARA COLECCIONES
 * 
 * Versión simplificada de ProductGridCard para selección en colecciones
 */
@Composable
private fun CollectionProductGridCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.smallSpacing),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
        ) {
            // Imagen del producto
            OptimizedProductImage(
                imageUrl = product.photoUrl,
                productName = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                categoryIcon = "📦"
            )
            
            // Nombre del producto
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
            
            // Precio
            Text(
                text = Formatters.formatClp(product.salePrice),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Botón agregar compacto
            FilledTonalButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = BrandColors.blueLilac.copy(alpha = 0.15f),
                    contentColor = BrandColors.blueLilac
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Agregar",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ProductSelectRow(product: Product, selected: Boolean, onToggle: () -> Unit) {
    UnifiedCard(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto (placeholder con emoji)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        BrandColors.turquoise.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📦",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Formatters.formatClp(product.salePrice),
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandColors.turquoise
                )
            }
            
            UnifiedChip(
                text = if (selected) "Quitar" else "Agregar",
                onClick = onToggle
            )
        }
    }
}


