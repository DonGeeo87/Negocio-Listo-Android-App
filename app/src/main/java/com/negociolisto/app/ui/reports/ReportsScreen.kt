package com.negociolisto.app.ui.reports

import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.components.UnifiedGradientHeaderCard
import com.negociolisto.app.ui.export.DataExportViewModel
import com.negociolisto.app.ui.components.UnifiedStatsCard
import com.negociolisto.app.ui.components.StatData
import com.negociolisto.app.ui.components.StatIcon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.ui.components.ModernListTopAppBar
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.ui.invoices.InvoiceViewModel
import kotlinx.datetime.*
import com.negociolisto.app.ui.design.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    exportViewModel: DataExportViewModel = hiltViewModel()
) {
    val invoices by invoiceViewModel.invoices.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Estados de exportación
    val isExporting by exportViewModel.isExporting.collectAsStateWithLifecycle()
    val exportProgress by exportViewModel.exportProgress.collectAsStateWithLifecycle()
    val exportStatus by exportViewModel.exportStatus.collectAsStateWithLifecycle()
    val exportMessage by exportViewModel.exportMessage.collectAsStateWithLifecycle()
    val exportUri by exportViewModel.exportUri.collectAsStateWithLifecycle()
    
    // Estados locales para exportación
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("PDF") }
    var selectedDataTypes by remember { mutableStateOf(setOf("Facturas")) }

    // Filtro de rango rápido
    var dateFilter by remember { mutableStateOf("MONTH") }
    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val startDate: LocalDateTime? = remember(dateFilter, now) {
        when (dateFilter) {
            "TODAY" -> LocalDateTime(now.year, now.monthNumber, now.dayOfMonth, 0, 0)
            "WEEK" -> (now.date - DatePeriod(days = 7)).atTime(now.time)
            "MONTH" -> LocalDateTime(now.year, now.monthNumber, 1, 0, 0)
            else -> null
        }
    }

    val filtered = remember(invoices, startDate) {
        if (startDate == null) invoices
        else invoices.filter { it.date >= startDate }
    }

    // KPIs
    val totalInvoices = filtered.size
    val totalAmount = filtered.sumOf { it.total }
    val averageTicket = if (totalInvoices > 0) totalAmount / totalInvoices else 0.0

    // Serie por mes (últimos 6)
    val series = remember(filtered) {
        val map = mutableMapOf<String, Double>()
        filtered.forEach { inv ->
            val key = "${inv.date.year}-${inv.date.monthNumber.toString().padStart(2, '0')}"
            map[key] = (map[key] ?: 0.0) + inv.total
        }
        map.toSortedMap()
    }

    Scaffold() { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filtros compactos
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UnifiedChip(
                        text = "Hoy",
                        onClick = { dateFilter = "TODAY" },
                        isSelected = dateFilter == "TODAY",
                        modifier = Modifier.weight(1f)
                    )
                    UnifiedChip(
                        text = "7 días",
                        onClick = { dateFilter = "WEEK" },
                        isSelected = dateFilter == "WEEK",
                        modifier = Modifier.weight(1f)
                    )
                    UnifiedChip(
                        text = "Mes",
                        onClick = { dateFilter = "MONTH" },
                        isSelected = dateFilter == "MONTH",
                        modifier = Modifier.weight(1f)
                    )
                    UnifiedChip(
                        text = "Todo",
                        onClick = { dateFilter = "ALL" },
                        isSelected = dateFilter == "ALL",
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            // KPIs compactos
            item {
                UnifiedStatsCard(
                    title = "📊 Resumen de Reportes",
                    stats = listOf(
                        StatData(
                            label = "Facturas",
                            value = totalInvoices.toString(),
                            icon = StatIcon.Vector(Icons.Filled.Description),
                            color = BrandColors.blueLilac
                        ),
                        StatData(
                            label = "Total Ventas",
                            value = Formatters.formatClp(totalAmount),
                            icon = StatIcon.Vector(Icons.Filled.AttachMoney),
                            color = BrandColors.turquoise
                        ),
                        StatData(
                            label = "Ticket Promedio",
                            value = Formatters.formatClp(averageTicket),
                            icon = StatIcon.Vector(Icons.Filled.Receipt),
                            color = BrandColors.turquoiseLight
                        )
                    )
                )
            }

            // Serie temporal simple (barras)
            if (series.isNotEmpty()) {
                item {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignTokens.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "📈 Ingresos por Mes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val maxVal = (series.values.maxOrNull() ?: 1.0)
                            series.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.width(72.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val ratio = (value / maxVal).toFloat()
                                    Box(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .weight(1f)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = GradientTokens.brandGradient()
                                                ),
                                                androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                            )
                                    ) {
                                        // Barra de progreso visual
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = Formatters.formatClp(value),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandColors.turquoise
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Insights y Recomendaciones
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "💡 Insights y Recomendaciones",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Análisis de rendimiento
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Tendencias de ventas
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        BrandColors.turquoise.copy(alpha = 0.1f),
                                        androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "📈 Tendencia",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (totalInvoices > 0) "Ventas activas" else "Sin ventas",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandColors.turquoise
                                    )
                                }
                            }
                            
                            // Recomendación de acción
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        BrandColors.blueLilac.copy(alpha = 0.1f),
                                        androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "🎯 Acción",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (averageTicket > 50000) "Mantener precio" else "Revisar precios",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandColors.blueLilac
                                    )
                                }
                            }
                        }
                        
                        // Recomendaciones específicas
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Recomendaciones:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            val recommendations = remember(totalInvoices, averageTicket) {
                                buildList {
                                    if (totalInvoices == 0) {
                                        add("🚀 ¡Comienza a vender! Crea tu primera factura")
                                        add("📦 Revisa tu inventario para productos disponibles")
                                    } else if (totalInvoices < 5) {
                                        add("📢 Promociona tus productos para aumentar ventas")
                                        add("👥 Contacta a tus clientes para nuevas oportunidades")
                                    } else if (averageTicket < 20000) {
                                        add("💰 Considera aumentar precios o vender paquetes")
                                        add("🎁 Ofrece descuentos por compras mayores")
                                    } else {
                                        add("✅ ¡Excelente rendimiento! Mantén el ritmo")
                                        add("📊 Analiza qué productos venden mejor")
                                    }
                                }
                            }
                            
                            recommendations.forEach { recommendation ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = recommendation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Exportación de Datos
            item {
                UnifiedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showExportDialog = true }
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
                                text = "📊 Exportar Reporte",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Exporta tus datos en CSV, PDF o Excel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Exportar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Feedback de exportación
            if (isExporting || exportMessage != null) {
                item {
                    ExportFeedbackCard(
                        isExporting = isExporting,
                        progress = exportProgress,
                        status = exportStatus,
                        message = exportMessage,
                        exportUri = exportUri,
                        onClearMessage = { exportViewModel.clearExportMessage() },
                        context = context,
                        exportFormat = exportFormat
                    )
                }
            }
        }
    }
    
    // Diálogo de exportación
    if (showExportDialog) {
        ExportDialog(
            selectedTypes = selectedDataTypes,
            exportFormat = exportFormat,
            onTypesChange = { selectedDataTypes = it },
            onFormatChange = { exportFormat = it },
            onExport = { types, format ->
                // Convertir nombres a minúsculas para coincidir con ExportService
                val normalizedTypes = types.map { type ->
                    when (type.lowercase()) {
                        "facturas" -> "facturas"
                        "inventario" -> "inventario"
                        "clientes" -> "clientes"
                        "ventas" -> "ventas"
                        "gastos" -> "gastos"
                        "colecciones" -> "colecciones"
                        else -> type.lowercase()
                    }
                }
                
                // Convertir filtro de fecha a Date range
                val dateRange = when (dateFilter) {
                    "TODAY" -> {
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }
                        today.time to Calendar.getInstance().time
                    }
                    "WEEK" -> {
                        val weekAgo = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_MONTH, -7)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }
                        weekAgo.time to Calendar.getInstance().time
                    }
                    "MONTH" -> {
                        val monthStart = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }
                        monthStart.time to Calendar.getInstance().time
                    }
                    else -> null to null
                }
                exportViewModel.exportData(normalizedTypes, format, true, dateRange)
                showExportDialog = false
            },
            onDismiss = { showExportDialog = false }
        )
    }
}

// 📊 Diálogo de Exportación
@Composable
private fun ExportDialog(
    selectedTypes: Set<String>,
    exportFormat: String,
    onTypesChange: (Set<String>) -> Unit,
    onFormatChange: (String) -> Unit,
    onExport: (List<String>, String) -> Unit,
    onDismiss: () -> Unit
) {
    val availableDataTypes = listOf(
        "Facturas" to "📄",
        "Inventario" to "📦",
        "Clientes" to "👥",
        "Ventas" to "💰",
        "Gastos" to "💸",
        "Colecciones" to "📚"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📊 Exportar Reporte") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Selector de formato
                Text(
                    "Formato:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CSV", "PDF", "Excel").forEach { format ->
                        FilterChip(
                            selected = exportFormat == format,
                            onClick = { onFormatChange(format) },
                            label = { Text(format) }
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Selector de tipos de datos
                Text(
                    "Datos a exportar:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Seleccionar todo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val allTypes = availableDataTypes.map { it.first }.toSet()
                                onTypesChange(if (selectedTypes.size == allTypes.size) emptySet() else allTypes)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedTypes.size == availableDataTypes.size,
                            onCheckedChange = { isChecked ->
                                val allTypes = availableDataTypes.map { it.first }.toSet()
                                onTypesChange(if (isChecked) allTypes else emptySet())
                            }
                        )
                        Text("🎯 Seleccionar Todo", modifier = Modifier.padding(start = 8.dp))
                    }
                    
                    HorizontalDivider()
                    
                    // Tipos individuales
                    availableDataTypes.forEach { (dataType, icon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTypesChange(
                                        if (dataType in selectedTypes) selectedTypes - dataType
                                        else selectedTypes + dataType
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = dataType in selectedTypes,
                                onCheckedChange = { isChecked ->
                                    onTypesChange(
                                        if (isChecked) selectedTypes + dataType
                                        else selectedTypes - dataType
                                    )
                                }
                            )
                            Text("$icon $dataType", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onExport(selectedTypes.toList(), exportFormat) },
                enabled = selectedTypes.isNotEmpty()
            ) {
                Text("📤 Exportar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// 📊 Feedback de Exportación
@Composable
private fun ExportFeedbackCard(
    isExporting: Boolean,
    progress: Int,
    status: String,
    message: String?,
    exportUri: android.net.Uri?,
    onClearMessage: () -> Unit,
    context: android.content.Context,
    exportFormat: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isExporting) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            message?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.primary)
                exportUri?.let { uri ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            val mimeType = when (exportFormat) {
                                "CSV" -> "text/csv"
                                "Excel" -> "text/csv"
                                else -> "application/pdf"
                            }
                            val openIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, mimeType)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(android.content.Intent.createChooser(openIntent, "Abrir con"))
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "No hay apps para abrir $exportFormat. Puedes compartir o abrir más tarde desde Archivos",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }) {
                            Text("📄 Abrir Reporte")
                        }
                        
                        Button(onClick = {
                            val mimeType = when (exportFormat) {
                                "CSV" -> "text/csv"
                                "Excel" -> "text/csv"
                                else -> "application/pdf"
                            }
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = mimeType
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir reporte"))
                        }) {
                            Text("📤 Compartir")
                        }
                    }
                    OutlinedButton(
                        onClick = onClearMessage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                } ?: run {
                    OutlinedButton(onClick = onClearMessage) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

private fun aggregateTopProducts(invoices: List<Invoice>): List<Pair<String, Double>> {
    val map = mutableMapOf<String, Double>()
    invoices.forEach { inv ->
        inv.items.forEach { item ->
            map[item.description] = (map[item.description] ?: 0.0) + item.total
        }
    }
    return map.entries.sortedByDescending { it.value }.map { it.key to it.value }
}


