package com.negociolisto.app.ui.invoices

import com.negociolisto.app.ui.components.UnifiedCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.ui.components.ModernListTopAppBar
import com.negociolisto.app.ui.components.StandardFloatingActionButton
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.domain.model.InvoiceTemplateType
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.invoices.InvoiceSettingsStore
import com.negociolisto.app.ui.invoices.components.EmptyInvoiceState
import com.negociolisto.app.ui.design.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.platform.LocalContext
import com.negociolisto.app.ui.components.UnifiedListTopAppBar
import com.negociolisto.app.ui.components.UnifiedFloatingActionButton
import com.negociolisto.app.ui.components.FixedBottomBar
import com.negociolisto.app.ui.components.UnifiedStatsCard
import com.negociolisto.app.ui.components.StatData
import com.negociolisto.app.ui.components.StatIcon
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.content.ActivityNotFoundException
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    onInvoiceClick: (String) -> Unit,
    onCreateInvoice: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    // Filtro por rango de fechas
    var dateFilter by remember { mutableStateOf("MONTH") }
    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val nowEpoch = remember(now) { now.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds() }
    val startEpoch: Long? = remember(dateFilter, now) {
        when (dateFilter) {
            "TODAY" -> LocalDateTime(now.year, now.monthNumber, now.dayOfMonth, 0, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            "WEEK" -> nowEpoch - 7L * 24L * 60L * 60L * 1000L
            "MONTH" -> LocalDateTime(now.year, now.monthNumber, 1, 0, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            else -> null
        }
    }
    val filtered = remember(invoices, query, startEpoch) {
        val q = query.trim().lowercase()
        invoices.filter { inv ->
            val epoch = inv.date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            (q.isEmpty() || inv.number.lowercase().contains(q)) && (startEpoch == null || epoch >= startEpoch)
        }
    }
    // Paginación sencilla en memoria
    val pageSize = 20
    var visibleCount by remember(filtered) { mutableStateOf(pageSize) }
    val page = remember(filtered, visibleCount) { filtered.take(visibleCount.coerceAtMost(filtered.size)) }
    Box(modifier = Modifier.fillMaxSize()) {
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
            // Estadísticas de facturas - MOVIDO AL TOP
            item {
                val totalInvoices = filtered.size
                val totalAmount = filtered.sumOf { it.total }
                val withSale = filtered.count { it.saleId != null }
                
                UnifiedStatsCard(
                    title = "📄 Resumen de Facturas",
                    stats = listOf(
                        StatData(
                            label = "Total Facturas",
                            value = totalInvoices.toString(),
                            icon = StatIcon.Vector(Icons.Filled.Description),
                            color = BrandColors.blueLilac
                        ),
                        StatData(
                            label = "Monto Total",
                            value = Formatters.formatClp(totalAmount),
                            icon = StatIcon.Vector(Icons.Filled.AttachMoney),
                            color = BrandColors.turquoise
                        ),
                        StatData(
                            label = "Con Venta",
                            value = withSale.toString(),
                            icon = StatIcon.Vector(Icons.Filled.CheckCircle),
                            color = BrandColors.turquoiseLight
                        )
                    )
                )
            }
            
            // Barra de búsqueda
            item {
                UnifiedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = "Buscar facturas",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Filtros de fecha
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding)
                    ) {
                        Text(
                            text = "📅 Filtros de Fecha",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UnifiedChip(
                                text = "Hoy",
                                onClick = { dateFilter = "TODAY" },
                                modifier = Modifier.weight(1f)
                            )
                            UnifiedChip(
                                text = "7 días",
                                onClick = { dateFilter = "WEEK" },
                                modifier = Modifier.weight(1f)
                            )
                            UnifiedChip(
                                text = "Mes",
                                onClick = { dateFilter = "MONTH" },
                                modifier = Modifier.weight(1f)
                            )
                            UnifiedChip(
                                text = "Todo",
                                onClick = { dateFilter = "ALL" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Botón de configuración
            item {
                UnifiedOutlinedButton(
                    text = "⚙️ Configurar Facturas",
                    onClick = onSettingsClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Mostrar empty state si no hay facturas
            if (page.isEmpty()) {
                item {
                    EmptyInvoiceState(
                        hasFilters = query.isNotEmpty() || (dateFilter != "ALL" && dateFilter != "MONTH"),
                        onConfigureInvoiceClick = onSettingsClick,
                        onClearFilters = { 
                            query = ""
                            dateFilter = "ALL"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(
                    items = page,
                    key = { it.id },
                    contentType = { "invoice" }
                ) { invoice ->
                    val customerVm: com.negociolisto.app.ui.customers.CustomerViewModel = hiltViewModel()
                    val customers by customerVm.customers.collectAsStateWithLifecycle()
                    val customerName = remember(invoice, customers) { customers.firstOrNull { it.id == invoice.customerId }?.name }
                    InvoiceCard(
                        invoice = invoice,
                        customerName = customerName,
                        onInvoiceClick = onInvoiceClick,
                        onDeleteInvoice = { invoiceId -> 
                            viewModel.deleteInvoice(invoiceId)
                        }
                    )
                }
                
                // Botón cargar más
                if (visibleCount < filtered.size) {
                    item {
                        UnifiedButton(
                            text = "📄 Cargar más (${filtered.size - visibleCount} restantes)",
                            onClick = { visibleCount = (visibleCount + pageSize).coerceAtMost(filtered.size) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = "Nueva Factura",
            primaryButtonOnClick = onCreateInvoice,
            primaryButtonIcon = Icons.Filled.Add,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun InvoiceStatsCard(
    totalInvoices: Int,
    totalAmount: Double,
    paidInvoices: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 Estadísticas de Facturas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = "📄",
                    value = totalInvoices.toString(),
                    label = "Total Facturas",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = "💰",
                    value = Formatters.formatClp(totalAmount),
                    label = "Monto Total",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    icon = "✅",
                    value = paidInvoices.toString(),
                    label = "Pagadas",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModernInvoiceCard(
    invoice: Invoice,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = invoice.number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = InvoiceTemplateType.values().first { it == invoice.template }.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatDate(invoice.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = Formatters.formatClp(invoice.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar factura",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar factura") },
            text = { Text("¿Estás seguro de que quieres eliminar esta factura? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
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

@Composable
private fun ModernDateFilters(
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(
            "TODAY" to "Hoy",
            "WEEK" to "7 días", 
            "MONTH" to "Mes",
            "ALL" to "Todo"
        )
        
        filters.forEach { (value, label) ->
            FilterChip(
                selected = selectedFilter == value,
                onClick = { onFilterChange(value) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val inv = invoices.firstOrNull { it.id == invoiceId }
    // Obtener nombre de cliente si aplica
    val customerVm: com.negociolisto.app.ui.customers.CustomerViewModel = hiltViewModel()
    val customers by customerVm.customers.collectAsStateWithLifecycle()
    val customerName = remember(inv, customers) {
        inv?.customerId?.let { id -> customers.firstOrNull { it.id == id }?.name }
    }
    val settings by InvoiceSettingsStore.settings.collectAsStateWithLifecycle()
    // Tomar siempre datos actuales del usuario/empresa
    val settingsViewModel: com.negociolisto.app.ui.settings.SettingsViewModel = hiltViewModel()
    val user by settingsViewModel.user.collectAsStateWithLifecycle()
    val mergedSettings = remember(settings, user) {
        settings.copy(
            companyName = user?.businessName ?: settings.companyName,
            companyAddress = user?.businessAddress ?: settings.companyAddress,
            companyRut = user?.businessRut ?: settings.companyRut,
            companyPhone = user?.businessPhone ?: settings.companyPhone,
            companyEmail = user?.businessEmail ?: settings.companyEmail,
            logoUrl = user?.businessLogoUrl ?: settings.logoUrl
        )
    }
    // Usar SIEMPRE el template de la factura creada
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var lastPdfUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    // NO usar Scaffold con topBar para evitar doble topbar
    // El MainScreen ya maneja el topbar dinámico
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // Compartir rápido
                        inv?.let { invoice ->
                            scope.launch {
                                val uri = lastPdfUri ?: InvoicePdfExporter.generate(context, invoice, mergedSettings, invoice.template, customerName)
                                lastPdfUri = uri
                                uri?.let {
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, it)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(share, "Compartir factura"))
                                } ?: run {
                                    snackbarHostState.showSnackbar("No se pudo generar el PDF para compartir")
                                }
                            }
                        }
                    },
                    containerColor = BrandColors.turquoise,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { 
                    Text("↗") 
                }
            }
        ) { padding ->
            if (inv == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { 
                    Text("Factura no encontrada") 
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp), 
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Plantilla: se muestra la elegida en la creación
                    Text("Plantilla: ${inv.template.displayName}")
                    Text("Fecha: ${Formatters.formatDate(inv.date)}")
                    HorizontalDivider()
                    customerName?.let { Text("Cliente: $it") }
                    inv.items.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.description} x${item.quantity}")
                            Text(Formatters.formatClp(item.total))
                        }
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal")
                        Text(Formatters.formatClp(inv.subtotal))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("IVA (19%)")
                        Text(Formatters.formatClp(inv.tax))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text(Formatters.formatClp(inv.total), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }

                    HorizontalDivider()
                    Text("Previsualización", style = MaterialTheme.typography.titleMedium)
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Cabecera
                            Text(mergedSettings.companyName, style = MaterialTheme.typography.titleMedium)
                            Text(mergedSettings.companyAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Factura ${inv.number}")
                            Text("Cliente: ${inv.customerId ?: "N/A"}")
                            inv.items.forEach { item ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.description} x${item.quantity}")
                                    Text(Formatters.formatClp(item.total))
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total")
                                Text(Formatters.formatClp(inv.total), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        UnifiedButton(
                            text = "📄 Exportar PDF",
                            onClick = {
                                inv?.let { invoice ->
                                    scope.launch {
                                        val uri = InvoicePdfExporter.generate(context, invoice, mergedSettings, invoice.template, customerName)
                                        lastPdfUri = uri
                                        if (uri == null) {
                                            snackbarHostState.showSnackbar("Error al generar el PDF")
                                        } else {
                                            try {
                                                val view = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/pdf")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(view)
                                            } catch (e: ActivityNotFoundException) {
                                                snackbarHostState.showSnackbar("No hay app para abrir PDF. Archivo guardado.")
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        UnifiedOutlinedButton(
                            text = "📤 Compartir",
                            onClick = {
                                inv?.let { invoice ->
                                    scope.launch {
                                        val uri = lastPdfUri ?: InvoicePdfExporter.generate(context, invoice, mergedSettings, invoice.template, customerName)
                                        lastPdfUri = uri
                                        uri?.let {
                                            val share = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(Intent.EXTRA_STREAM, it)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(share, "Compartir factura"))
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 📄 TARJETA DE FACTURA CON MENÚ CONTEXTUAL
 * 
 * Componente que muestra una factura individual con opciones de eliminar.
 */
@Composable
private fun InvoiceCard(
    invoice: Invoice,
    customerName: String?,
    onInvoiceClick: (String) -> Unit,
    onDeleteInvoice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Emoji según el template
    val templateEmoji = when (invoice.template) {
        com.negociolisto.app.domain.model.InvoiceTemplateType.CLASSIC -> "📄"
        com.negociolisto.app.domain.model.InvoiceTemplateType.MODERN -> "✨"
        com.negociolisto.app.domain.model.InvoiceTemplateType.MINIMAL -> "📋"
    }
    
    UnifiedCard(
        onClick = { onInvoiceClick(invoice.id) },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lado izquierdo: Info compacta
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = templateEmoji,
                    style = MaterialTheme.typography.titleMedium
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = invoice.number,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (customerName != null) {
                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = invoice.template.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = Formatters.formatDate(invoice.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Lado derecho: Monto y menú
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = Formatters.formatClp(invoice.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (invoice.items.isNotEmpty()) {
                        Text(
                            text = "${invoice.items.size} producto${if (invoice.items.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Botón de menú contextual compacto
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones de factura",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
    
    // Menú contextual
    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text("Opciones de factura") },
            text = { Text("¿Qué deseas hacer con esta factura?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onInvoiceClick(invoice.id)
                        showMenu = false
                    }
                ) {
                    Text("Ver detalles")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDeleteInvoice(invoice.id)
                        showMenu = false
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}


