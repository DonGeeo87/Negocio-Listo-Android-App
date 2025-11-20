package com.negociolisto.app.ui.sales

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.domain.model.SaleItem
import com.negociolisto.app.domain.model.PaymentMethod
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.ui.customers.CustomerViewModel
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.*
import com.negociolisto.app.ui.design.UnifiedOutlinedButton
import com.negociolisto.app.ui.components.UnifiedStatsCard
import com.negociolisto.app.ui.components.StatData
import com.negociolisto.app.ui.components.StatIcon
import com.negociolisto.app.ui.components.FixedBottomBar
import com.negociolisto.app.ui.auth.AuthViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import com.negociolisto.app.ui.design.BrandColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Checkbox
import com.negociolisto.app.ui.components.ModernDropdown
import com.negociolisto.app.ui.invoices.QuickCreateCustomerDialog
import com.negociolisto.app.ui.collections.CollectionViewModel
import com.negociolisto.app.domain.model.Collection
import com.negociolisto.app.domain.model.CollectionItem
import com.negociolisto.app.domain.model.CollectionStatus
import com.negociolisto.app.domain.model.CollectionWebTemplate
import com.negociolisto.app.domain.util.TaxCalculator
import com.negociolisto.app.ui.invoices.InvoiceSettingsStore
import java.util.UUID
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.input.ImeAction
import com.negociolisto.app.ui.components.OptimizedProductImage
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.layout.ContentScale
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.datetime.toInstant
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.UnifiedTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesListScreen(
    modifier: Modifier = Modifier,
    viewModel: SalesViewModel = hiltViewModel(),
    authVm: AuthViewModel = hiltViewModel(),
    onAddSale: () -> Unit,
    onViewSale: (String) -> Unit = {},
    onEditSale: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onGoogleSignUp: () -> Unit = {}
) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    // Overlay de carga inicial persistente hasta primera emisión utilizable
    var isInitialLoading by remember { mutableStateOf(true) }
    LaunchedEffect(sales.isEmpty()) {
        if (sales.isNotEmpty()) {
            isInitialLoading = false
        }
    }
    val isAuthenticated by authVm.isAuthenticated.collectAsStateWithLifecycle()
    val customerVm: CustomerViewModel = hiltViewModel()
    val customers by customerVm.customers.collectAsStateWithLifecycle()
    val idToName = remember(customers) { customers.associate { it.id to it.name } }
    // Paginación en memoria para ventas
    val pageSize = 20
    var visibleCount by remember { mutableStateOf(pageSize) }
    // Filtro por rango rápido
    var dateFilter by remember { mutableStateOf("MONTH") }
    
    // Resetear paginación cuando cambia el filtro de fecha
    LaunchedEffect(dateFilter) {
        visibleCount = pageSize
    }
    val now = remember { kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()) }
    val nowEpoch = remember(now) { now.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds() }
    val startEpoch = remember(dateFilter, nowEpoch) {
        when (dateFilter) {
            "TODAY" -> kotlinx.datetime.LocalDateTime(now.year, now.monthNumber, now.dayOfMonth, 0, 0).toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
            "WEEK" -> nowEpoch - 7L * 24L * 60L * 60L * 1000L
            "MONTH" -> kotlinx.datetime.LocalDateTime(now.year, now.monthNumber, 1, 0, 0).toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
            else -> null
        }
    }
    // Filtrar ventas activas
    val filteredSales = remember(sales, startEpoch) {
        sales.filter {
            val ok = it.status == com.negociolisto.app.domain.model.SaleStatus.ACTIVE
            val epoch = it.date.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
            ok && (startEpoch == null || epoch >= startEpoch)
        }
    }
    
    // Ordenar por fecha cronológica (más antigua primero) para generar números correlativos
    val sortedByDate = remember(filteredSales) {
        filteredSales.sortedBy { it.date }
    }
    
    // Mapa de IDs de venta a números correlativos (basado en orden cronológico)
    val saleSequentialNumbers = remember(sortedByDate) {
        sortedByDate.mapIndexed { index, sale ->
            sale.id to com.negociolisto.app.ui.components.getSaleSequentialNumber(index)
        }.toMap()
    }
    
    // Ordenar descendente (más nueva primero) para mostrar en la lista
    val activeSales = remember(filteredSales) {
        filteredSales.sortedByDescending { it.date }
    }
    
    val page = remember(activeSales, visibleCount) { activeSales.take(visibleCount.coerceAtMost(activeSales.size)) }
    
    // Estado para confirmación de eliminación
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var saleToDelete by remember { mutableStateOf<String?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (isInitialLoading && sales.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
        ) {
            // Header con estadísticas
            item {
                UnifiedStatsCard(
                    title = "Resumen de Ventas",
                    stats = listOf(
                        StatData(
                            label = "Total",
                            value = activeSales.size.toString(),
                            icon = StatIcon.Vector(Icons.Filled.ShoppingCart),
                            color = BrandColors.blueLilac
                        ),
                        StatData(
                            label = "Ingresos",
                            value = Formatters.formatClp(activeSales.sumOf { it.total }),
                            icon = StatIcon.Vector(Icons.Filled.AttachMoney),
                            color = BrandColors.turquoise
                        ),
                        StatData(
                            label = "Promedio",
                            value = Formatters.formatClp(if (activeSales.isNotEmpty()) activeSales.sumOf { it.total } / activeSales.size else 0.0),
                            icon = StatIcon.Vector(Icons.AutoMirrored.Filled.TrendingUp),
                            color = BrandColors.secondary
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
            
            // Filtros de fecha modernos
            item {
                ModernDateFilters(
                    selectedFilter = dateFilter,
                    onFilterSelected = { dateFilter = it }
                )
            }
            
            // Lista de ventas
            if (page.isEmpty()) {
                item {
                    UnifiedEmptyState(
                        title = "¡No hay ventas registradas!",
                        message = "Comienza registrando tus primeras ventas para llevar un control completo de tus ingresos y clientes.",
                        icon = "💰",
                        actionText = "Registrar primera venta",
                        onActionClick = onAddSale,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(page, key = { it.id }) { sale ->
                    ModernSaleCard(
                        sale = sale,
                        customerName = sale.customerId?.let { idToName[it] },
                        onCancel = { 
                            saleToDelete = sale.id
                            showDeleteConfirmDialog = true
                        },
                        onEdit = {
                            onEditSale(sale.id)
                        },
                        onClick = {
                            onViewSale(sale.id)
                        },
                        sequentialNumber = saleSequentialNumbers[sale.id] ?: "0000"
                    )
                }
                    
                // Botón cargar más
                    if (visibleCount < activeSales.size) {
                    item {
                            ModernLoadMoreButton(
                                remainingCount = activeSales.size - visibleCount,
                            onLoadMore = { visibleCount = (visibleCount + pageSize).coerceAtMost(activeSales.size) }
                        )
                    }
                }
            }
        }
        
        // Diálogo de confirmación de eliminación
        if (showDeleteConfirmDialog && saleToDelete != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteConfirmDialog = false
                    saleToDelete = null
                },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Estás seguro de que quieres eliminar esta venta? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            saleToDelete?.let { saleId ->
                                viewModel.deleteSale(saleId)
                            }
                            showDeleteConfirmDialog = false
                            saleToDelete = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmDialog = false
                            saleToDelete = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = "Nueva Venta",
            primaryButtonOnClick = onAddSale,
            primaryButtonIcon = Icons.Filled.Add,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SaleRow(sale: Sale, customerName: String? = null) {
    Card {
        Column(Modifier.padding(DesignTokens.cardPadding)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Venta", style = MaterialTheme.typography.titleMedium)
                    customerName?.let {
            Text(
                            text = "Cliente: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = sale.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val net = sale.total / 1.19
                    val tax = sale.total - net
                    Text(Formatters.formatClp(sale.total), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Neto: ${Formatters.formatClp(net)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("IVA: ${Formatters.formatClp(tax)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            sale.items.forEach { item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.productName + " x" + item.quantity)
                    Text(Formatters.formatClp(item.lineTotal))
                }
            }
        }
    }
}

@Composable
fun SalesSummaryCard(sales: List<Sale>, modifier: Modifier = Modifier) {
    val totalSales = sales.size
    val totalAmount = sales.sumOf { it.total }
    val average = if (totalSales > 0) totalAmount / totalSales else 0.0
    Card(modifier = modifier) {
        Column(Modifier.padding(DesignTokens.cardPadding), verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)) {
            Text("Resumen de ventas", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Ventas"); Text(totalSales.toString(), style = MaterialTheme.typography.titleLarge) }
                Column { Text("Total"); Text(Formatters.formatClp(totalAmount), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }
                Column { Text("Promedio"); Text(Formatters.formatClp(average), style = MaterialTheme.typography.titleLarge) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSaleScreen(
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saleId: String? = null,
    viewModel: SalesViewModel = hiltViewModel(),
    invoiceViewModel: com.negociolisto.app.ui.invoices.InvoiceViewModel = hiltViewModel(),
    collectionViewModel: CollectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toastViewModel: com.negociolisto.app.ui.components.ToastViewModel = hiltViewModel()
    val draftManager = remember { SaleDraftManager(context) }
    val products by viewModel.products.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val customerVm: CustomerViewModel = hiltViewModel()
    val customers by customerVm.customers.collectAsStateWithLifecycle()
    var showQuickCreateDialog by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var productQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showGenerateInvoiceDialog by remember { mutableStateOf(false) }
    var isGeneratingInvoice by remember { mutableStateOf(false) }
    var invoiceGenerationProgress by remember { mutableStateOf(0f) }
    var invoiceGenerationError by remember { mutableStateOf<String?>(null) }
    var invoiceGeneratedSuccessfully by remember { mutableStateOf(false) }
    var savedSaleId by remember { mutableStateOf<String?>(null) }
    
    // Estado para crear colección
    var createCollection by remember { mutableStateOf(false) }
    var collectionName by remember { mutableStateOf("") }
    
    // Estado para método de pago
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    
    // Cargar borrador al iniciar (solo si no estamos editando)
    var cart by remember { mutableStateOf(listOf<SaleItem>()) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    
    // Variable para rastrear si ya se cargaron los datos
    var dataLoaded by remember { mutableStateOf(false) }
    
    // Precargar datos de la venta existente
    LaunchedEffect(saleId, sales, customers) {
        android.util.Log.d("RecordSaleScreen", "LaunchedEffect ejecutado - saleId: $saleId, sales count: ${sales.size}, dataLoaded: $dataLoaded")
        if (saleId != null && saleId.isNotBlank() && !dataLoaded) {
            // Buscar la venta en la lista
            android.util.Log.d("RecordSaleScreen", "Buscando venta con ID: $saleId")
            android.util.Log.d("RecordSaleScreen", "IDs disponibles: ${sales.map { it.id }.take(5)}")
            val existingSale = sales.firstOrNull { it.id == saleId }
            
            if (existingSale != null) {
                android.util.Log.d("RecordSaleScreen", "✅ Venta encontrada! Cargando datos de venta: $saleId")
                android.util.Log.d("RecordSaleScreen", "Items encontrados: ${existingSale.items.size}")
                
                // Precargar carrito con los items de la venta
                cart = existingSale.items
                
                // Precargar cliente
                selectedCustomer = existingSale.customerId?.let { id ->
                    customers.firstOrNull { it.id == id }
                }
                
                // Precargar método de pago
                selectedPaymentMethod = existingSale.paymentMethod
                
                android.util.Log.d("RecordSaleScreen", "Datos precargados: cart=${cart.size}, customer=${selectedCustomer?.name}, payment=${selectedPaymentMethod}")
                
                dataLoaded = true
            } else {
                android.util.Log.w("RecordSaleScreen", "Venta no encontrada: $saleId (total ventas: ${sales.size})")
            }
        } else if (saleId == null && !dataLoaded) {
            // Solo cargar borrador si no estamos editando
            val draft = draftManager.loadDraft()
            if (draft != null) {
                showRestoreDialog = true
            }
            dataLoaded = true
        }
    }
    
    // Obtener la venta existente para usar en otros lugares
    val existingSale = remember(sales, saleId) {
        saleId?.let { id -> sales.firstOrNull { it.id == id } }
    }
    
    // Función para restaurar borrador
    val restoreDraft = {
        val draft = draftManager.loadDraft()
        if (draft != null) {
            cart = draft.items.map { it.toSaleItem() }
            selectedCustomer = draft.customerId?.let { customerId ->
                customers.firstOrNull { it.id == customerId }
            }
            draftManager.clearDraft()
            showRestoreDialog = false
        }
    }
    
    // Guardar borrador automáticamente cuando cambia el carrito o cliente (con debounce)
    // Solo si no estamos editando una venta existente
    LaunchedEffect(cart.size, selectedCustomer?.id, saleId) {
        if (saleId == null) {
            delay(500) // Esperar 500ms después del último cambio
            if (cart.isNotEmpty() || selectedCustomer != null) {
                val draft = SaleDraft(
                    customerId = selectedCustomer?.id,
                    items = cart.map { it.toSaleItemData() },
                    total = cart.sumOf { it.lineTotal }
                )
                draftManager.saveDraft(draft)
            }
        }
    }
    
    // Actualizar nombre de colección cuando cambia el cliente
    LaunchedEffect(selectedCustomer?.name, createCollection) {
        if (createCollection && selectedCustomer != null && collectionName.isBlank()) {
            val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            collectionName = "Colección ${selectedCustomer!!.name} - ${date.dayOfMonth}/${date.monthNumber}/${date.year}"
        }
    }
    
    // Obtener configuración de facturas para calcular IVA
    val invoiceSettings by InvoiceSettingsStore.settings.collectAsStateWithLifecycle()
    val priceIsNet = invoiceSettings.priceIsNet
    
    // Calcular subtotal, IVA y total según la configuración (igual que en CreateInvoiceScreen)
    val totals = remember(cart, priceIsNet) {
        TaxCalculator.fromSaleItems(cart, priceIsNet)
    }
    val subtotal = totals.subtotal
    val tax = totals.tax
    val total = totals.total
    
    val filteredProducts = remember(products, productQuery) {
        val q = productQuery.trim().lowercase()
        if (q.isEmpty()) products else products.filter { 
            it.name.lowercase().contains(q) || it.sku.lowercase().contains(q) 
        }
    }
    
    // Paginación: 4 productos por página
    val productsPerPage = 4
    var currentProductPage by remember(filteredProducts) { mutableStateOf(0) }
    val productPages = remember(filteredProducts) { filteredProducts.chunked(productsPerPage) }
    
    // Resetear página cuando cambia la búsqueda
    LaunchedEffect(productQuery) {
        currentProductPage = 0
    }
    
    // Estado para tarjeta desplegable de búsqueda
    var isProductSearchExpanded by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize()) {
        // Usar LazyColumn principal en lugar de Column con scroll anidado
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Sección 1: Búsqueda de productos (tarjeta desplegable)
            item {
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
                                .clickable { isProductSearchExpanded = !isProductSearchExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛒 Seleccionar Productos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (isProductSearchExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isProductSearchExpanded) "Colapsar" else "Expandir",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = isProductSearchExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)) {
                                UnifiedTextField(
                                    value = productQuery,
                                    onValueChange = { productQuery = it },
                                    label = "Buscar productos...",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                // Lista de productos con paginación (4 por página)
                                if (productPages.isNotEmpty()) {
                                    val pageProducts = productPages[currentProductPage]
                                    
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.heightIn(max = 400.dp)
                                    ) {
                                        // Mostrar productos de la página actual
                                        pageProducts.forEach { product ->
                                            SaleProductListItem(
                                                product = product,
                                                cart = cart,
                                                onQuantityChange = { newQty ->
                                                    // Validar stock disponible antes de cambiar la cantidad
                                                    if (newQty > 0) {
                                                        // Calcular cantidad ya en el carrito para este producto (cantidad actual antes del cambio)
                                                        val currentQtyInCart = cart.find { it.productId == product.id }?.quantity ?: 0
                                                        // Stock disponible = stock total - cantidad ya reservada en carrito + cantidad actual (que se está reemplazando)
                                                        val reservedInCart = cart
                                                            .filter { it.productId != product.id }
                                                            .sumOf { it.quantity }
                                                        val availableStock = product.stockQuantity - reservedInCart
                                                        
                                                        // Validar que la nueva cantidad no supere el stock disponible
                                                        if (newQty > availableStock) {
                                                            toastViewModel.showError(
                                                                "⚠️ Stock insuficiente para ${product.name}\n" +
                                                                "Disponible: $availableStock, Solicitado: $newQty"
                                                            )
                                                            return@SaleProductListItem
                                                        }
                                                    }
                                                    
                                                    val existing = cart.find { it.productId == product.id }
                                                    cart = when {
                                                        newQty <= 0 -> cart.filterNot { it.productId == product.id }
                                                        existing == null -> cart + SaleItem(product.id, product.name, newQty, product.salePrice)
                                                        else -> cart.map { if (it.productId == product.id) it.copy(quantity = newQty) else it }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    
                                    // Controles de paginación (solo si hay más de una página)
                                    if (productPages.size > 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { 
                                                    currentProductPage = (currentProductPage - 1).coerceAtLeast(0) 
                                                },
                                                enabled = currentProductPage > 0,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("◀ Anterior", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            
                                            Text(
                                                text = "${currentProductPage + 1} / ${productPages.size}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                            
                                            Button(
                                                onClick = { 
                                                    currentProductPage = (currentProductPage + 1).coerceAtMost(productPages.lastIndex) 
                                                },
                                                enabled = currentProductPage < productPages.lastIndex,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Siguiente ▶", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                } else if (productQuery.isNotBlank()) {
                                    Text(
                                        text = "No se encontraron productos",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "No hay productos disponibles",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Sección 5: Cliente seleccionado
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Título de la sección
                        Text(
                            text = "👤 Cliente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Selector de cliente con botón que abre BottomSheet
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón para abrir selector de clientes
                            OutlinedButton(
                                onClick = { showCustomerDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = selectedCustomer?.name ?: "Seleccionar cliente",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            // Botón para crear cliente rápido
                            FilledTonalButton(
                                onClick = { showQuickCreateDialog = true }
                            ) {
                                Text("+ Nuevo")
                            }
                        }
                        
                        // Información del cliente seleccionado
                        selectedCustomer?.let { customer ->
                            Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
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
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                                            text = customer.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        customer.companyName?.takeIf { it.isNotBlank() }?.let { company ->
                                            Text(
                                                text = company,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                
                                customer.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                
                                customer.email?.takeIf { it.isNotBlank() }?.let { email ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                
                                // Botón para quitar cliente
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { selectedCustomer = null }
                                    ) {
                                        Text(
                                            text = "Quitar cliente",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        }
                    }
                }
            }
            
            // Sección 6: Método de pago (solo si hay productos)
            if (cart.isNotEmpty()) {
                item {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "💳 Método de Pago",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Selector de método de pago con chips
                            val paymentMethods = remember { PaymentMethod.values().toList() }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(paymentMethods, key = { it.name }) { method ->
                                    FilterChip(
                                        selected = selectedPaymentMethod == method,
                                        onClick = { selectedPaymentMethod = method },
                                        label = { 
                                            Text(
                                                text = method.displayName,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Sección 7: Opción para crear colección (solo si hay cliente y productos)
            if (selectedCustomer != null && cart.isNotEmpty()) {
                item {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = createCollection,
                                    onCheckedChange = { createCollection = it }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📚 Crear colección para este cliente",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Crea una colección con los productos de esta venta para compartir con el cliente",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            if (createCollection) {
                                val placeholderText = remember(selectedCustomer?.name) {
                                    val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    "Ej: Colección ${selectedCustomer?.name ?: "Cliente"} - ${date.dayOfMonth}/${date.monthNumber}/${date.year}"
                                }
                                
                                UnifiedTextField(
                                    value = collectionName,
                                    onValueChange = { collectionName = it },
                                    label = "Nombre de la colección",
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = placeholderText
                                )
                            }
                        }
                    }
                }
            }
            
            // Sección 8: Carrito y resumen
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Carrito (${cart.size} productos)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (cart.isEmpty()) {
                            Text(
                                text = "No hay productos en el carrito",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(cart) { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                item.productName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "Cantidad: ${item.quantity}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            Formatters.formatClp(item.lineTotal),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
                            HorizontalDivider()
                            
                            // Mostrar subtotal, IVA y total (igual que en CreateInvoiceScreen)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Subtotal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        Formatters.formatClp(subtotal),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "IVA (19%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        Formatters.formatClp(tax),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                HorizontalDivider()
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Total",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        Formatters.formatClp(total),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = if (saleId != null) "✏️ Actualizar Venta" else "💰 Guardar Venta",
            primaryButtonOnClick = {
                android.util.Log.d("RecordSaleScreen", "Botón clickeado - saleId: $saleId, cart size: ${cart.size}")
                if (cart.isNotEmpty()) {
                    val sale = Sale(
                        id = saleId ?: UUID.randomUUID().toString(),
                        customerId = selectedCustomer?.id,
                        items = cart,
                        total = total,
                        date = existingSale?.date ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        paymentMethod = selectedPaymentMethod,
                        note = existingSale?.note
                    )
                    // Guardar o actualizar la venta y crear colección si está marcado
                    scope.launch {
                        try {
                            if (saleId != null) {
                                // Actualizar venta existente
                                android.util.Log.d("RecordSaleScreen", "Iniciando actualización de venta: $saleId")
                                android.util.Log.d("RecordSaleScreen", "Venta a actualizar: items=${sale.items.size}, total=${sale.total}")
                                viewModel.updateSale(sale)
                                android.util.Log.d("RecordSaleScreen", "Venta actualizada exitosamente")
                                toastViewModel.showSuccess("Venta actualizada exitosamente")
                                // Esperar un poco para que el toast se muestre
                                kotlinx.coroutines.delay(500)
                                // Volver atrás después de actualizar
                                android.util.Log.d("RecordSaleScreen", "Navegando de vuelta...")
                                onSave()
                                return@launch
                            } else {
                                // Crear nueva venta
                                viewModel.recordSale(sale)
                                // Esperar más tiempo para que el StateFlow se actualice y la venta esté disponible
                                kotlinx.coroutines.delay(800)
                                
                                // Crear colección si está marcado y hay cliente (solo para nuevas ventas)
                                if (createCollection && selectedCustomer != null && cart.isNotEmpty()) {
                                    try {
                                        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                        val collectionId = UUID.randomUUID().toString()
                                        
                                        // Mapear productos de la venta a CollectionItems
                                        val collectionItems = cart.mapIndexed { index, saleItem ->
                                            CollectionItem(
                                                productId = saleItem.productId,
                                                notes = null,
                                                displayOrder = index,
                                                isFeatured = false,
                                                specialPrice = null
                                            )
                                        }
                                        
                                        // Crear la colección
                                        val collection = Collection(
                                            id = collectionId,
                                            name = collectionName.ifBlank { "Colección ${selectedCustomer!!.name}" },
                                            description = "Colección creada desde la venta del ${now.dayOfMonth}/${now.monthNumber}/${now.year}",
                                            items = collectionItems,
                                            associatedCustomerIds = listOf(selectedCustomer!!.id),
                                            createdAt = now,
                                            updatedAt = now,
                                            status = CollectionStatus.ACTIVE,
                                            color = null,
                                            enableChat = true,
                                            webTemplate = CollectionWebTemplate.MODERN
                                        )
                                        
                                        collectionViewModel.addCollection(collection)
                                        toastViewModel.showSuccess("Colección creada exitosamente")
                                    } catch (e: Exception) {
                                        toastViewModel.showError("Error al crear colección: ${e.message}")
                                    }
                                }
                                
                                // Guardar el ID de la venta para generar factura si el usuario lo desea
                                savedSaleId = sale.id
                                // Limpiar borrador después de guardar
                                draftManager.clearDraft()
                                // Mostrar diálogo para preguntar si desea generar factura
                                showGenerateInvoiceDialog = true
                            }
                        } catch (e: Exception) {
                            // Manejar errores específicos
                            val errorMessage = when (e) {
                                is com.negociolisto.app.domain.util.NegocioListoError.InsufficientStockError -> {
                                    "⚠️ Stock insuficiente: ${e.productName}\nSolicitado: ${e.requestedQuantity}, Disponible: ${e.availableStock}"
                                }
                                is com.negociolisto.app.domain.util.NegocioListoError.BusinessRuleError -> {
                                    "Error: ${e.message}"
                                }
                                else -> {
                                    "Error al ${if (saleId != null) "actualizar" else "guardar"} venta: ${e.message}"
                                }
                            }
                            toastViewModel.showError(errorMessage)
                            android.util.Log.e("RecordSaleScreen", "Error al ${if (saleId != null) "actualizar" else "guardar"} venta", e)
                        }
                    }
                }
            },
            primaryButtonIcon = if (saleId != null) Icons.Filled.Edit else Icons.Filled.Add,
            enabled = cart.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Diálogo para restaurar borrador
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { 
                    draftManager.clearDraft()
                    showRestoreDialog = false 
                },
                title = { Text("Recuperar borrador") },
                text = { Text("¿Deseas recuperar la venta que estabas creando?") },
                confirmButton = {
                    TextButton(onClick = restoreDraft) {
                        Text("Recuperar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        draftManager.clearDraft()
                        showRestoreDialog = false 
                    }) {
                        Text("Descartar")
                    }
                }
            )
        }
        
        // Diálogo para preguntar si desea generar factura
        if (showGenerateInvoiceDialog && savedSaleId != null) {
            AlertDialog(
                onDismissRequest = { 
                    if (!isGeneratingInvoice && invoiceGeneratedSuccessfully) {
                        showGenerateInvoiceDialog = false
                        invoiceGeneratedSuccessfully = false
                        invoiceGenerationProgress = 0f
                        onSave()
                    }
                },
                title = { 
                    Text(
                        when {
                            invoiceGeneratedSuccessfully -> "✅ Factura Generada"
                            isGeneratingInvoice -> "Generando Factura"
                            invoiceGenerationError != null -> "Error"
                            else -> "¿Generar factura?"
                        }
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when {
                            invoiceGeneratedSuccessfully -> {
                                // Estado de éxito - Factura lista al 100%
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "¡Factura generada exitosamente!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "La factura ha sido agregada al registro de facturas y está lista para usar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                LinearProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                                Text(
                                    text = "100% Completado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            isGeneratingInvoice -> {
                                // Estado de carga - Mostrando progreso
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Generando factura...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                LinearProgressIndicator(
                                    progress = { invoiceGenerationProgress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                                Text(
                                    text = "${(invoiceGenerationProgress * 100).toInt()}% Completado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Por favor espera mientras se procesa...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            invoiceGenerationError != null -> {
                                // Estado de error
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Error al generar factura",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = invoiceGenerationError ?: "Error desconocido",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            else -> {
                                // Estado inicial - Pregunta al usuario
                                Text(
                                    text = "¿Deseas generar una factura para esta venta y agregarla al registro de facturas?",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    when {
                        invoiceGeneratedSuccessfully -> {
                            // Botón para continuar cuando la factura está lista
                            Button(
                                onClick = {
                                    showGenerateInvoiceDialog = false
                                    invoiceGeneratedSuccessfully = false
                                    invoiceGenerationProgress = 0f
                                    onSave()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Continuar")
                            }
                        }
                        invoiceGenerationError != null -> {
                            // Botón para reintentar en caso de error
                            TextButton(
                                onClick = {
                                    invoiceGenerationError = null
                                    isGeneratingInvoice = true
                                    invoiceGenerationProgress = 0f
                                    scope.launch {
                                        generateInvoiceWithProgress(
                                            savedSaleId!!,
                                            viewModel,
                                            toastViewModel,
                                            onProgressUpdate = { progress ->
                                                invoiceGenerationProgress = progress
                                            },
                                            onSuccess = {
                                                isGeneratingInvoice = false
                                                invoiceGeneratedSuccessfully = true
                                                invoiceGenerationProgress = 1f
                                                toastViewModel.showSuccess("Factura generada exitosamente")
                                            },
                                            onError = { error ->
                                                isGeneratingInvoice = false
                                                invoiceGenerationError = error
                                                toastViewModel.showError("Error al generar factura: $error")
                                            }
                                        )
                                    }
                                }
                            ) {
                                Text("Reintentar")
                            }
                        }
                        isGeneratingInvoice -> {
                            // Botón deshabilitado durante la generación
                            TextButton(
                                onClick = { },
                                enabled = false
                            ) {
                                Text("Generando...")
                            }
                        }
                        else -> {
                            // Botón inicial para confirmar
                            TextButton(
                                onClick = {
                                    isGeneratingInvoice = true
                                    invoiceGenerationError = null
                                    invoiceGenerationProgress = 0f
                                    scope.launch {
                                        generateInvoiceWithProgress(
                                            savedSaleId!!,
                                            viewModel,
                                            toastViewModel,
                                            onProgressUpdate = { progress ->
                                                invoiceGenerationProgress = progress
                                            },
                                            onSuccess = {
                                                isGeneratingInvoice = false
                                                invoiceGeneratedSuccessfully = true
                                                invoiceGenerationProgress = 1f
                                                toastViewModel.showSuccess("Factura generada exitosamente")
                                            },
                                            onError = { error ->
                                                isGeneratingInvoice = false
                                                invoiceGenerationError = error
                                                toastViewModel.showError("Error al generar factura: $error")
                                            }
                                        )
                                    }
                                }
                            ) {
                                Text("Sí, generar factura")
                            }
                        }
                    }
                },
                dismissButton = {
                    if (!invoiceGeneratedSuccessfully) {
                        TextButton(
                            onClick = { 
                                if (!isGeneratingInvoice) {
                                    showGenerateInvoiceDialog = false
                                    invoiceGenerationError = null
                                    invoiceGenerationProgress = 0f
                                    onSave()
                                }
                            },
                            enabled = !isGeneratingInvoice
                        ) {
                            Text("No, solo guardar venta")
                        }
                    }
                }
            )
        }
        
        // BottomSheet para selección de clientes
        if (showCustomerDialog) {
            com.negociolisto.app.ui.components.CustomerSelectionBottomSheet(
                customers = customers,
                selectedCustomer = selectedCustomer,
                searchQuery = customerSearchQuery,
                onSearchQueryChange = { customerSearchQuery = it },
                onCustomerSelected = { customer ->
                    selectedCustomer = customer
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
                    showQuickCreateDialog = true
                }
            )
        }
        
        // Diálogo para crear cliente rápido
        if (showQuickCreateDialog) {
            QuickCreateCustomerDialog(
                onDismiss = { showQuickCreateDialog = false },
                onCustomerCreated = { customer ->
                    selectedCustomer = customer
                    showQuickCreateDialog = false
                },
                customersViewModel = customerVm
            )
        }
    }
}

/**
 * Función auxiliar para generar factura con indicador de progreso
 */
private suspend fun generateInvoiceWithProgress(
    saleId: String,
    viewModel: SalesViewModel,
    toastViewModel: com.negociolisto.app.ui.components.ToastViewModel,
    onProgressUpdate: (Float) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        // Simular progreso de generación (0-100%)
        onProgressUpdate(0.1f) // 10% - Iniciando
        kotlinx.coroutines.delay(200)
        
        onProgressUpdate(0.3f) // 30% - Obteniendo datos de la venta
        kotlinx.coroutines.delay(300)
        
        onProgressUpdate(0.5f) // 50% - Creando items de factura
        kotlinx.coroutines.delay(200)
        
        onProgressUpdate(0.7f) // 70% - Calculando impuestos
        kotlinx.coroutines.delay(200)
        
        onProgressUpdate(0.9f) // 90% - Guardando factura
        kotlinx.coroutines.delay(200)
        
        // Generar la factura
        val result = viewModel.generateInvoiceFromSale(saleId)
        
        result.onSuccess { invoiceId ->
            onProgressUpdate(1.0f) // 100% - Completado
            kotlinx.coroutines.delay(300) // Pequeña pausa para mostrar el 100%
            onSuccess()
        }.onFailure { error ->
            onError(error.message ?: "Error desconocido al generar la factura")
        }
    } catch (e: Exception) {
        onError(e.message ?: "Error desconocido al generar la factura")
    }
}

/**
 * 🎴 ITEM DE PRODUCTO EN LISTA PARA VENTAS
 * 
 * Diseño horizontal optimizado para lista con mejor distribución de elementos
 */
@Composable
private fun SaleProductListItem(
    product: Product,
    cart: List<SaleItem>,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQty = remember(cart, product.id) { 
        cart.find { it.productId == product.id }?.quantity ?: 0 
    }
    val hasItem = currentQty > 0
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    var textValue by remember(currentQty) { mutableStateOf(currentQty.toString()) }
    var isEditing by remember { mutableStateOf(false) }
    
    // Sincronizar texto cuando cambia la cantidad externamente (pero no cuando estamos editando)
    LaunchedEffect(currentQty) {
        if (!isEditing) {
            textValue = currentQty.toString()
        }
    }
    
    // Interacción para mantener presionado
    val decInteractionSource = remember { MutableInteractionSource() }
    val incInteractionSource = remember { MutableInteractionSource() }
    val isDecPressed by decInteractionSource.collectIsPressedAsState()
    val isIncPressed by incInteractionSource.collectIsPressedAsState()
    
    // Auto-incremento/decremento mientras se mantiene presionado
    LaunchedEffect(isDecPressed) {
        if (isDecPressed) {
            delay(300) // Esperar 300ms antes de empezar
            while (isDecPressed) {
                val qty = cart.find { it.productId == product.id }?.quantity ?: 0
                if (qty > 0) {
                    onQuantityChange(qty - 1)
                } else {
                    break
                }
                delay(100) // Decrementar cada 100ms
            }
        }
    }
    
    LaunchedEffect(isIncPressed) {
        if (isIncPressed) {
            delay(300) // Esperar 300ms antes de empezar
            while (isIncPressed) {
                val qty = cart.find { it.productId == product.id }?.quantity ?: 0
                onQuantityChange(qty + 1)
                delay(100) // Incrementar cada 100ms
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto (más pequeña para lista)
            OptimizedProductImage(
                imageUrl = product.photoUrl,
                productName = product.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            // Información del producto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Formatters.formatClp(product.salePrice),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Controles de cantidad (verticalmente centrados)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón disminuir (muy pequeño - 14dp)
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            color = if (hasItem) 
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .clickable(
                            enabled = hasItem,
                            interactionSource = decInteractionSource,
                            indication = null,
                            onClick = { onQuantityChange((currentQty - 1).coerceAtLeast(0)) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Reducir",
                        modifier = Modifier.size(10.dp),
                        tint = if (hasItem) 
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                
                // Campo de texto para entrada manual (más grande - 80dp ancho)
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { newValue ->
                        // Solo permitir números
                        val filtered = newValue.filter { char -> char.isDigit() }
                        if (filtered.isEmpty() || filtered.toIntOrNull() != null) {
                            textValue = filtered
                            isEditing = true
                            if (filtered.isNotEmpty()) {
                                filtered.toIntOrNull()?.let { num -> onQuantityChange(num.coerceAtLeast(0)) }
                            } else {
                                onQuantityChange(0)
                            }
                        }
                    },
                    modifier = Modifier
                        .width(80.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                isEditing = false
                                // Validar y actualizar cuando pierde el foco
                                val numValue = textValue.toIntOrNull() ?: 0
                                textValue = numValue.toString()
                                onQuantityChange(numValue.coerceAtLeast(0))
                                keyboardController?.hide()
                            }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (hasItem) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = if (hasItem) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    placeholder = {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                )
                
                // Botón aumentar (muy pequeño - 14dp)
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .clickable(
                            interactionSource = incInteractionSource,
                            indication = null,
                            onClick = { onQuantityChange(currentQty + 1) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Agregar",
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

