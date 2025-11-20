package com.negociolisto.app.ui.invoices

import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.components.FixedBottomBar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.negociolisto.app.domain.model.Product
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.domain.model.InvoiceItem
import com.negociolisto.app.domain.model.InvoiceTemplateType
import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.domain.model.SaleItem
import com.negociolisto.app.domain.model.PaymentMethod
import com.negociolisto.app.domain.util.TaxCalculator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
// Usamos las clases totalmente calificadas para opciones de teclado
import com.negociolisto.app.ui.components.Formatters
import java.util.UUID
import com.negociolisto.app.ui.design.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.negociolisto.app.ui.components.OptimizedProductImage
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    modifier: Modifier = Modifier,
    saleId: String? = null,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    customersViewModel: com.negociolisto.app.ui.customers.CustomerViewModel = hiltViewModel(),
    inventoryViewModel: com.negociolisto.app.ui.inventory.InventoryViewModel = hiltViewModel(),
    salesViewModel: com.negociolisto.app.ui.sales.SalesViewModel = hiltViewModel()
) {
    val customers by customersViewModel.customers.collectAsStateWithLifecycle()
    val products by inventoryViewModel.products.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val sales by salesViewModel.sales.collectAsStateWithLifecycle()

    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var quantityByProductId by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var productQuery by remember { mutableStateOf("") }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var showQuickCreateDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    // Usar el template seleccionado globalmente desde la configuración
    val invoiceSettings by InvoiceSettingsStore.settings.collectAsStateWithLifecycle()
    val selectedTemplate = invoiceSettings.defaultTemplate
    
    // Pre-llenar datos si saleId está presente
    LaunchedEffect(saleId, sales, products) {
        if (saleId != null && sales.isNotEmpty() && products.isNotEmpty()) {
            val sale = sales.firstOrNull { it.id == saleId }
            if (sale != null) {
                // Pre-llenar cliente
                selectedCustomerId = sale.customerId
                
                // Pre-llenar notas
                note = sale.note ?: ""
                
                // Pre-llenar productos y cantidades
                val quantitiesMap = mutableMapOf<String, Int>()
                sale.items.forEach { saleItem ->
                    // Buscar el producto por ID o por nombre
                    val product = products.firstOrNull { 
                        it.id == saleItem.productId || it.name == saleItem.productName 
                    }
                    product?.let { p ->
                        quantitiesMap[p.id] = saleItem.quantity
                    }
                }
                quantityByProductId = quantitiesMap
            }
        }
    }

    val items = remember(products, quantityByProductId) {
        products.mapNotNull { p ->
            val q = quantityByProductId[p.id] ?: 0
            if (q > 0) InvoiceItem(description = p.name, quantity = q, unitPrice = p.salePrice) else null
        }
    }
    val filteredProducts = remember(products, productQuery) {
        val q = productQuery.trim().lowercase()
        when {
            q == "*" -> products
            q.isEmpty() -> emptyList() // no mostrar hasta que se busque o se elija "mostrar todos"
            else -> products.filter { pr ->
                pr.name.lowercase().contains(q) || pr.sku.lowercase().contains(q)
            }
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
    
    // Calcular subtotal, IVA y total según la configuración
    val priceIsNet = invoiceSettings.priceIsNet
    val totals = remember(items, priceIsNet) {
        TaxCalculator.fromInvoiceItems(items, priceIsNet)
    }
    val subtotal = totals.subtotal
    val tax = totals.tax
    val total = totals.total

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
        ) {
            item {
                UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding)
                ) {
                        Text(
                            text = "👤 Cliente",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                        
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
                                        text = customers.firstOrNull { it.id == selectedCustomerId }?.name ?: "Seleccionar cliente",
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
                    }
                }
            }


            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding)
                    ) {
                        Text(
                            text = "📦 Productos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                        
                        UnifiedTextField(
                            value = productQuery,
                            onValueChange = { productQuery = it },
                            label = "Buscar por nombre o SKU",
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        if (productQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
                            UnifiedChip(
                                text = "Mostrar todos (${products.size})",
                                onClick = { productQuery = "*" },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                if (productPages.isNotEmpty()) {
                    val pageProducts = productPages[currentProductPage]
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        // Mostrar productos de la página actual
                        pageProducts.forEach { product ->
                            ProductListItem(
                                product = product,
                                quantity = quantityByProductId[product.id] ?: 0,
                                onDec = {
                                    val newQ = ((quantityByProductId[product.id] ?: 0) - 1).coerceAtLeast(0)
                                    quantityByProductId = quantityByProductId.toMutableMap().apply { put(product.id, newQ) }
                                },
                                onInc = {
                                    val newQ = (quantityByProductId[product.id] ?: 0) + 1
                                    quantityByProductId = quantityByProductId.toMutableMap().apply { put(product.id, newQ) }
                                },
                                onQuantityChange = { newQ ->
                                    quantityByProductId = quantityByProductId.toMutableMap().apply { put(product.id, newQ.coerceAtLeast(0)) }
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
                }
            }

            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding)
                    ) {
                        Text(
                            text = "📝 Notas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                        
                        UnifiedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = "Notas (opcional)",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "💰 Resumen",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal")
                            Text(Formatters.formatClp(subtotal))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IVA (19%)")
                            Text(Formatters.formatClp(tax))
                        }
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", style = MaterialTheme.typography.titleMedium)
                            Text(Formatters.formatClp(total), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            // Espaciado para el bottom bar
            item {
                Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
            }
        }
        
        // BottomSheet para selección de clientes
        if (showCustomerDialog) {
            com.negociolisto.app.ui.components.CustomerSelectionBottomSheet(
                customers = customers,
                selectedCustomer = customers.firstOrNull { it.id == selectedCustomerId },
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
                    showQuickCreateDialog = true
                }
            )
        }
        
        // Diálogo para creación rápida de cliente
        if (showQuickCreateDialog) {
            QuickCreateCustomerDialog(
                onDismiss = { showQuickCreateDialog = false },
                onCustomerCreated = { customer ->
                    selectedCustomerId = customer.id
                    showQuickCreateDialog = false
                },
                customersViewModel = customersViewModel
            )
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = if (items.isNotEmpty()) "📄 Emitir Factura" else "📄 Selecciona productos",
            primaryButtonOnClick = {
                if (items.isNotEmpty()) {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val number = "INV-%04d%02d%02d-%02d%02d".format(now.year, now.monthNumber, now.dayOfMonth, now.hour, now.minute)
                    val invoice = Invoice(
                        id = UUID.randomUUID().toString(),
                        number = number,
                        customerId = selectedCustomerId,
                        items = items,
                        subtotal = subtotal,
                        tax = tax,
                        total = total,
                        date = now,
                        template = selectedTemplate,
                        notes = note.ifBlank { null }
                    )
                    
                    // Crear una venta correspondiente ANTES de la factura para que se actualice el stock
                    // Solo incluir productos que existen en el inventario
                    val saleItems = items.mapNotNull { item ->
                        val product = products.find { p -> p.name == item.description }
                        if (product != null) {
                            SaleItem(
                                productId = product.id,
                                productName = product.name,
                                quantity = item.quantity,
                                unitPrice = item.unitPrice
                            )
                        } else {
                            // Si no se encuentra el producto, no incluirlo en la venta
                            // pero sí incluirlo en la factura (puede ser un servicio o producto eliminado)
                            null
                        }
                    }
                    
                    // Solo crear la venta si hay productos válidos
                    if (saleItems.isNotEmpty()) {
                        val saleTotals = TaxCalculator.fromSaleItems(saleItems, priceIsNet)
                        val sale = Sale(
                            id = UUID.randomUUID().toString(),
                            customerId = selectedCustomerId,
                            items = saleItems,
                            total = saleTotals.total,
                            date = now,
                            paymentMethod = PaymentMethod.CASH, // Por defecto efectivo
                            note = note.ifBlank { null }
                        )
                        
                        // Registrar la venta (esto actualizará el stock automáticamente)
                        scope.launch {
                            salesViewModel.recordSale(sale)
                            
                            // Asociar la factura con la venta
                            val invoiceWithSale = invoice.copy(saleId = sale.id)
                            invoiceViewModel.addInvoice(invoiceWithSale)
                            // El repositorio de ventas ya actualiza el total de compras del cliente automáticamente
                            
                            onCreated(invoice.id)
                        }
                    } else {
                        // Si no hay productos válidos, crear la factura sin venta
                        scope.launch {
                            invoiceViewModel.addInvoice(invoice)
                            onCreated(invoice.id)
                        }
                    }
                }
            },
            primaryButtonIcon = Icons.Filled.Description,
            enabled = items.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ProductListItem(
    product: Product,
    quantity: Int,
    onDec: () -> Unit,
    onInc: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    var textValue by remember(quantity) { mutableStateOf(quantity.toString()) }
    var isEditing by remember { mutableStateOf(false) }
    
    // Sincronizar texto cuando cambia la cantidad externamente (pero no cuando estamos editando)
    LaunchedEffect(quantity) {
        if (!isEditing) {
            textValue = quantity.toString()
        }
    }
    
    // Interacción para mantener presionado
    val decInteractionSource = remember { MutableInteractionSource() }
    val incInteractionSource = remember { MutableInteractionSource() }
    val isDecPressed by decInteractionSource.collectIsPressedAsState()
    val isIncPressed by incInteractionSource.collectIsPressedAsState()
    
    // Estado local para rastrear cantidad durante auto-incremento
    var localQuantity by remember { mutableStateOf(quantity) }
    
    // Sincronizar cantidad local con prop
    LaunchedEffect(quantity) {
        if (!isEditing) {
            localQuantity = quantity
        }
    }
    
    // Auto-incremento/decremento mientras se mantiene presionado
    LaunchedEffect(isDecPressed) {
        if (isDecPressed) {
            delay(300) // Esperar 300ms antes de empezar
            while (isDecPressed) {
                if (localQuantity > 0) {
                    localQuantity--
                    onDec()
                    delay(100) // Decrementar cada 100ms
                } else {
                    break
                }
            }
        }
    }
    
    LaunchedEffect(isIncPressed) {
        if (isIncPressed) {
            delay(300) // Esperar 300ms antes de empezar
            while (isIncPressed) {
                localQuantity++
                onInc()
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
                            color = if (quantity > 0) 
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .clickable(
                            enabled = quantity > 0,
                            interactionSource = decInteractionSource,
                            indication = null,
                            onClick = onDec
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Reducir",
                        modifier = Modifier.size(10.dp),
                        tint = if (quantity > 0) 
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
                                filtered.toIntOrNull()?.let { num -> onQuantityChange(num) }
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
                                onQuantityChange(numValue)
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
                        focusedTextColor = if (quantity > 0) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = if (quantity > 0) 
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
                            onClick = onInc
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


