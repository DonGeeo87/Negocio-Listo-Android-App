package com.negociolisto.app.ui.export

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.ui.components.UnifiedGradientHeaderCard
import com.negociolisto.app.ui.components.UnifiedCard
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar
import android.app.DatePickerDialog
import androidx.compose.material3.Icon

/**
 * 📊 PANTALLA DEDICADA DE EXPORTACIÓN DE DATOS
 * 
 * Pantalla especializada para exportar datos con filtros de fecha
 * y opciones avanzadas de exportación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Estados de la UI
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val exportStatus by viewModel.exportStatus.collectAsStateWithLifecycle()
    val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()
    val exportUri by viewModel.exportUri.collectAsStateWithLifecycle()
    val nowText = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }
    
    // Estados locales
    var selectedDataTypes by remember { mutableStateOf(setOf<String>()) }
    var showDataSelection by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var dateRange by remember { mutableStateOf<Pair<Date?, Date?>>(null to null) }
    var exportFormat by remember { mutableStateOf("PDF") }
    var singlePdfMode by remember { mutableStateOf(true) }
    
    // Auto-scroll state
    val listState = rememberLazyListState()
    
    // Auto-scroll cuando inicie la exportación
    LaunchedEffect(isExporting) {
        if (isExporting) {
            delay(300) // Esperar un poco para que se renderice
            val lastIndex = listState.layoutInfo.totalItemsCount - 1
            if (lastIndex >= 0) {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }
    
    // Auto-scroll cuando aparezca un mensaje de exportación
    LaunchedEffect(exportMessage) {
        if (exportMessage != null) {
            delay(300) // Esperar un poco para que se renderice
            val lastIndex = listState.layoutInfo.totalItemsCount - 1
            if (lastIndex >= 0) {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }
    
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

    Scaffold(
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .offset(y = slideInOffset)
                .alpha(fadeInAlpha),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con información
            item {
                UnifiedGradientHeaderCard(
                    title = "📊 Exportación de Datos",
                    subtitle = "Exporta tus datos con opciones personalizadas",
                    modifier = Modifier.fillMaxWidth()
                ) {}
            }
            
            // Estado del sistema (último reporte y acción rápida)
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Estado del sistema",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (isExporting) exportStatus else exportStatus.ifBlank { "Listo para exportar" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalIconButton(onClick = {
                            if (selectedDataTypes.isNotEmpty()) {
                                viewModel.exportData(
                                    dataTypes = selectedDataTypes.toList(),
                                    format = exportFormat,
                                    singlePdfMode = singlePdfMode,
                                    dateRange = dateRange
                                )
                            }
                        }) {
                            Icon(Icons.Default.CloudUpload, "Exportar")
                        }
                    }
                }
            }
            
            // Filtros de fecha
            item {
                DateRangeFilterCard(
                    dateRange = dateRange,
                    onDateRangeChange = { dateRange = it },
                    onShowPicker = { showDateRangePicker = true }
                )
            }
            
            // Selector de tipos de datos
            item {
                DataTypeSelectorCard(
                    selectedTypes = selectedDataTypes,
                    onTypesChange = { selectedDataTypes = it },
                    onShowSelection = { showDataSelection = true }
                )
            }
            
            // Opciones de exportación
            item {
                ExportOptionsCard(
                    exportFormat = exportFormat,
                    onFormatChange = { exportFormat = it },
                    singlePdfMode = singlePdfMode,
                    onSinglePdfModeChange = { singlePdfMode = it }
                )
            }
            
            // Botones de exportación
            item {
                ExportButtonsCard(
                    selectedTypes = selectedDataTypes,
                    exportFormat = exportFormat,
                    singlePdfMode = singlePdfMode,
                    dateRange = dateRange,
                    onExport = { types, format, singlePdf, dates ->
                        viewModel.exportData(types, format, singlePdf, dates)
                    }
                )
            }
            
            // Feedback de exportación
            item {
        ExportFeedbackCard(
            isExporting = isExporting,
            progress = exportProgress,
            status = exportStatus,
            message = exportMessage,
            exportUri = exportUri,
            onClearMessage = { viewModel.clearExportMessage() },
            context = context,
            exportFormat = exportFormat
        )
            }
        }
    }
    
    // Diálogo de selección de datos
    if (showDataSelection) {
        DataSelectionDialog(
            selectedTypes = selectedDataTypes,
            onTypesChange = { selectedDataTypes = it },
            onDismiss = { showDataSelection = false }
        )
    }
    
    // Diálogo de rango de fechas
    if (showDateRangePicker) {
        DateRangePickerDialog(
            dateRange = dateRange,
            onDateRangeChange = { dateRange = it },
            onDismiss = { showDateRangePicker = false }
        )
    }
}

// 🎨 HEADER CARD
@Composable
private fun ExportHeaderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2196F3),
                            Color(0xFF64B5F6)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "📊 Exportación de Datos",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Exporta tus datos con opciones personalizadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }
    }
}

// 🧠 ESTADO DEL SISTEMA (estilo Backup)
@Composable
private fun ExportStatusCard(
    isExporting: Boolean,
    status: String,
    onQuickExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Estado del sistema", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isExporting) "Exportación en curso…" else status.ifBlank { "Listo para exportar" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalIconButton(onClick = { onQuickExport() }, enabled = !isExporting) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                }
            }
        }
    }
}

// 📅 FILTRO DE RANGO DE FECHAS
@Composable
private fun DateRangeFilterCard(
    dateRange: Pair<Date?, Date?>,
    onDateRangeChange: (Pair<Date?, Date?>) -> Unit,
    onShowPicker: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📅 Filtro de Fechas", style = MaterialTheme.typography.titleSmall)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onShowPicker,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        if (dateRange.first != null) "Desde: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateRange.first!!)}" 
                        else "Desde",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                OutlinedButton(
                    onClick = onShowPicker,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        if (dateRange.second != null) "Hasta: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateRange.second!!)}" 
                        else "Hasta",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                if (dateRange.first != null || dateRange.second != null) {
                    TextButton(
                        onClick = { onDateRangeChange(null to null) }
                    ) {
                        Text("Limpiar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// 📋 SELECTOR DE TIPOS DE DATOS + OPCIONES COMPACTAS
@Composable
private fun DataTypeSelectorCard(
    selectedTypes: Set<String>,
    onTypesChange: (Set<String>) -> Unit,
    onShowSelection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📋 Tipos de Datos", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                onClick = onShowSelection,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text(
                    if (selectedTypes.isEmpty()) "Seleccionar tipos de datos" 
                    else "${selectedTypes.size} tipos seleccionados",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ⚙️ OPCIONES DE EXPORTACIÓN COMPACTAS
@Composable
private fun ExportOptionsCard(
    exportFormat: String,
    onFormatChange: (String) -> Unit,
    singlePdfMode: Boolean,
    onSinglePdfModeChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("⚙️ Formato", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("CSV", "PDF", "Excel").forEach { format ->
                    FilterChip(
                        selected = exportFormat == format,
                        onClick = { onFormatChange(format) },
                        label = { Text(format, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors()
                    )
                }
            }
            
            // Modo PDF único (solo para PDF)
            if (exportFormat == "PDF") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PDF único", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = singlePdfMode,
                        onCheckedChange = onSinglePdfModeChange
                    )
                }
            }
        }
    }
}

// 🚀 BOTONES DE EXPORTACIÓN
@Composable
private fun ExportButtonsCard(
    selectedTypes: Set<String>,
    exportFormat: String,
    singlePdfMode: Boolean,
    dateRange: Pair<Date?, Date?>,
    onExport: (List<String>, String, Boolean, Pair<Date?, Date?>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedTypes.isEmpty()) {
                Text(
                    "Selecciona tipos de datos para exportar",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            } else {
                Button(
                    onClick = { onExport(selectedTypes.toList(), exportFormat, singlePdfMode, dateRange) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📤 Exportar ${selectedTypes.size} tipos en $exportFormat")
                }
            }
        }
    }
}

// 📊 FEEDBACK DE EXPORTACIÓN
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
    if (isExporting || message != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isExporting) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    // Determinar el tipo MIME basado en el formato de exportación
                                    val mimeType = when (exportFormat) {
                                        "CSV" -> "text/csv"
                                        "Excel" -> "text/csv" // Ahora Excel genera CSV también
                                        else -> "application/pdf"
                                    }
                                    
                                    val openIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, mimeType)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(android.content.Intent.createChooser(openIntent, "Abrir con"))
                                    } catch (e: Exception) {
                                        // Si no hay aplicaciones para abrir el archivo, mostrar mensaje
                                        val fileType = when (exportFormat) {
                                            "CSV" -> "CSV"
                                            "Excel" -> "Excel"
                                            else -> "PDF"
                                        }
                                        android.widget.Toast.makeText(
                                            context,
                                            "No hay apps para abrir $fileType. Puedes compartir o abrir más tarde desde Archivos",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }) { 
                                    val icon = when (exportFormat) {
                                        "CSV" -> "📊"
                                        "Excel" -> "📈"
                                        else -> "📄"
                                    }
                                    Text("$icon Abrir Reporte") 
                                }
                            
                            Button(onClick = {
                                // Determinar el tipo MIME para compartir
                                val mimeType = when (exportFormat) {
                                    "CSV" -> "text/csv"
                                    "Excel" -> "text/csv" // Ahora Excel genera CSV también
                                    else -> "application/pdf"
                                }
                                
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = mimeType
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir reporte"))
                            }) { 
                                val icon = when (exportFormat) {
                                    "CSV" -> "📊"
                                    "Excel" -> "📈"
                                    else -> "📤"
                                }
                                Text("$icon Compartir") 
                            }
                        }
                        
                        OutlinedButton(
                            onClick = onClearMessage,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Cerrar") }
                        }
                    } ?: run {
                        OutlinedButton(onClick = onClearMessage) { Text("Cerrar") }
                    }
                }
            }
        }
    }
}

// 📋 DIÁLOGO DE SELECCIÓN DE DATOS
@Composable
private fun DataSelectionDialog(
    selectedTypes: Set<String>,
    onTypesChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val availableDataTypes = listOf(
        "Inventario" to "📦",
        "Clientes" to "👥", 
        "Ventas" to "💰",
        "Gastos" to "💸",
        "Colecciones" to "📚",
        "Facturas" to "📄",
        "Ajustes" to "⚙️"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar tipos de datos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Checkbox "Seleccionar Todo"
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = { onTypesChange(emptySet()); onDismiss() }) { Text("Limpiar") }
        }
    )
}

// 📅 DIÁLOGO DE RANGO DE FECHAS - SOLUCIÓN SIMPLE Y PRAGMÁTICA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    dateRange: Pair<Date?, Date?>,
    onDateRangeChange: (Pair<Date?, Date?>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val locale = Locale("es", "MX")
    
    // Estados para los pickers individuales
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var tempStartDate by remember { mutableStateOf(dateRange.first) }
    var tempEndDate by remember { mutableStateOf(dateRange.second) }
    
    // Función para mostrar resultado formateado
    fun getDateRangeText(): String {
        if (tempStartDate == null && tempEndDate == null) return "Seleccionar fechas"
        val startText = tempStartDate?.let { SimpleDateFormat("dd/MM/yyyy", locale).format(it) } ?: "..."
        val endText = tempEndDate?.let { SimpleDateFormat("dd/MM/yyyy", locale).format(it) } ?: "..."
        return "Desde: $startText hasta: $endText"
    }

    if (showStartDatePicker) {
        val calendar = Calendar.getInstance()
        tempStartDate?.let { calendar.time = it }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(
            context,
            { _, year, month, day ->
                tempStartDate = Calendar.getInstance().apply {
                    set(year, month, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time
                showStartDatePicker = false
            },
            year,
            month,
            day
        ).apply {
            setOnDismissListener { showStartDatePicker = false }
            show()
        }
    }
    
    if (showEndDatePicker) {
        val calendar = Calendar.getInstance()
        tempEndDate?.let { calendar.time = it }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(
            context,
            { _, year, month, day ->
                tempEndDate = Calendar.getInstance().apply {
                    set(year, month, day)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time
                showEndDatePicker = false
            },
            year,
            month,
            day
        ).apply {
            setOnDismissListener { showEndDatePicker = false }
            show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar rango de fechas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Texto del rango seleccionado
                Text(
                    text = getDateRangeText(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Divider()
                
                // Botones para seleccionar fechas
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Desde", fontSize = 13.sp)
                    }
                    
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Hasta", fontSize = 13.sp)
                    }
                }
                
                // Botón para limpiar fechas
                if (tempStartDate != null || tempEndDate != null) {
                    TextButton(
                        onClick = { 
                            tempStartDate = null
                            tempEndDate = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Limpiar fechas")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeChange(tempStartDate to tempEndDate)
                    onDismiss()
                },
                enabled = tempStartDate != null || tempEndDate != null
            ) { Text("✓ Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
