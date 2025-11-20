package com.negociolisto.app.ui.settings

import com.negociolisto.app.ui.components.UnifiedCard

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.data.service.DataExportService
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.ui.design.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 📊 PANTALLA DE EXPORTACIÓN DE DATOS
 * 
 * Permite al usuario configurar y ejecutar la exportación de datos
 * con filtros avanzados y seguimiento de progreso en tiempo real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val exportStatus by viewModel.exportStatus.collectAsStateWithLifecycle()
    val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()
    val exportUri by viewModel.exportUri.collectAsStateWithLifecycle()
    
    // Estados locales
    var selectedType by remember { mutableStateOf(DataExportService.ExportType.PDF) }
    var selectedCategories by remember { mutableStateOf(setOf(DataExportService.DataCategory.ALL)) }
    var selectedPeriod by remember { mutableStateOf("ALL") }
    var includeImages by remember { mutableStateOf(false) }
    var includeMetadata by remember { mutableStateOf(true) }
    
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
    
    // NO usar Scaffold con topBar para evitar doble topbar
    // El MainScreen ya maneja el topbar dinámico
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .offset(y = slideInOffset)
            .alpha(fadeInAlpha)
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header informativo
        item {
            ExportHeaderCard()
        }
        
        // Configuración de tipo de archivo
        item {
            ExportTypeCard(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
        }
        
        // Configuración de categorías
        item {
            ExportCategoriesCard(
                selectedCategories = selectedCategories,
                onCategoriesChanged = { selectedCategories = it }
            )
        }
        
        // Configuración de filtros de fecha
        item {
            ExportDateFilterCard(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
            )
        }
        
        // Configuración de opciones avanzadas
        item {
            ExportOptionsCard(
                includeImages = includeImages,
                includeMetadata = includeMetadata,
                onIncludeImagesChanged = { includeImages = it },
                onIncludeMetadataChanged = { includeMetadata = it }
            )
        }
        
        // Botón de exportación
        item {
            ExportActionCard(
                isExporting = uiState.isExporting,
                onExportClick = {
                    val config = DataExportService.ExportConfig(
                        type = selectedType,
                        categories = selectedCategories.toList(),
                        dateFilter = DataExportService.DateFilter(period = selectedPeriod),
                        includeImages = includeImages,
                        includeMetadata = includeMetadata
                    )
                    scope.launch {
                        viewModel.exportData(config)
                    }
                }
            )
        }
        
        // Progreso de exportación
        if (uiState.isExporting) {
            item {
                ExportProgressCard(
                    progress = exportProgress,
                    status = exportStatus
                )
            }
        }
        
        // Resultado de exportación
        exportMessage?.let { message ->
            item {
                ExportResultCard(
                    message = message,
                    exportUri = exportUri,
                    onShareClick = { uri ->
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "*/*"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir archivo exportado"))
                    },
                    onDismissClick = { viewModel.clearExportMessage() }
                )
            }
        }
        
        // Espacio final
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// 🎨 TARJETA DE ENCABEZADO
@Composable
private fun ExportHeaderCard() {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Exportación de Datos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Exporta todos tus datos de NegocioListo en formato PDF o Excel. Configura qué datos incluir y aplica filtros según tus necesidades.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(
                    icon = "📄",
                    label = "Formatos",
                    value = "PDF, Excel"
                )
                InfoItem(
                    icon = "🔒",
                    label = "Seguro",
                    value = "Datos locales"
                )
                InfoItem(
                    icon = "⚡",
                    label = "Rápido",
                    value = "Sin límites"
                )
            }
        }
    }
}

// 🎨 TARJETA DE TIPO DE ARCHIVO
@Composable
private fun ExportTypeCard(
    selectedType: DataExportService.ExportType,
    onTypeSelected: (DataExportService.ExportType) -> Unit
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📄 Tipo de Archivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Solo mostrar PDF y Excel
                val supportedTypes = listOf(
                    DataExportService.ExportType.PDF to Triple("📋", "PDF", "Para imprimir"),
                    DataExportService.ExportType.EXCEL to Triple("📊", "Excel", "Formato nativo")
                )
                
                supportedTypes.forEach { (type, info) ->
                    val (icon, label, description) = info
                    val isSelected = selectedType == type
                    
                    UnifiedChip(
                        text = "$icon $label",
                        onClick = { onTypeSelected(type) },
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Text(
                text = when (selectedType) {
                    DataExportService.ExportType.EXCEL -> "Formato Excel: Archivo nativo de Microsoft Excel"
                    DataExportService.ExportType.PDF -> "Formato PDF: Ideal para imprimir y compartir"
                    else -> "Formato seleccionado"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 🎨 TARJETA DE CATEGORÍAS
@Composable
private fun ExportCategoriesCard(
    selectedCategories: Set<DataExportService.DataCategory>,
    onCategoriesChanged: (Set<DataExportService.DataCategory>) -> Unit
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📂 Categorías de Datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            val categories = listOf(
                DataExportService.DataCategory.ALL to Triple("📊", "Todos los Datos", "Incluye todo"),
                DataExportService.DataCategory.SALES to Triple("💰", "Ventas", "Transacciones de venta"),
                DataExportService.DataCategory.INVENTORY to Triple("📦", "Inventario", "Productos y stock"),
                DataExportService.DataCategory.CUSTOMERS to Triple("👥", "Clientes", "Base de datos de clientes"),
                DataExportService.DataCategory.EXPENSES to Triple("💸", "Gastos", "Gastos operacionales"),
                DataExportService.DataCategory.INVOICES to Triple("📄", "Facturas", "Facturas emitidas")
            )
            
            categories.forEach { (category, info) ->
                val (icon, label, description) = info
                val isSelected = selectedCategories.contains(category)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (category == DataExportService.DataCategory.ALL) {
                                onCategoriesChanged(setOf(category))
                            } else {
                                val newCategories = if (isSelected) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories - DataExportService.DataCategory.ALL + category
                                }
                                onCategoriesChanged(newCategories)
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            if (category == DataExportService.DataCategory.ALL) {
                                onCategoriesChanged(setOf(category))
                            } else {
                                val newCategories = if (isSelected) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories - DataExportService.DataCategory.ALL + category
                                }
                                onCategoriesChanged(newCategories)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$icon $label",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// 🎨 TARJETA DE FILTROS DE FECHA
@Composable
private fun ExportDateFilterCard(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📅 Filtro de Fecha",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            val periods = listOf(
                "ALL" to "📊 Todos los datos",
                "TODAY" to "📅 Solo hoy",
                "WEEK" to "📆 Última semana",
                "MONTH" to "📆 Último mes",
                "YEAR" to "📆 Último año"
            )
            
            periods.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPeriodSelected(value) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = selectedPeriod == value,
                        onClick = { onPeriodSelected(value) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// 🎨 TARJETA DE OPCIONES AVANZADAS
@Composable
private fun ExportOptionsCard(
    includeImages: Boolean,
    includeMetadata: Boolean,
    onIncludeImagesChanged: (Boolean) -> Unit,
    onIncludeMetadataChanged: (Boolean) -> Unit
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "⚙️ Opciones Avanzadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🖼️ Incluir Imágenes",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Incluye fotos de productos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = includeImages,
                    onCheckedChange = onIncludeImagesChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📋 Incluir Metadatos",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Información de exportación",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = includeMetadata,
                    onCheckedChange = onIncludeMetadataChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

// 🎨 TARJETA DE ACCIÓN DE EXPORTACIÓN
@Composable
private fun ExportActionCard(
    isExporting: Boolean,
    onExportClick: () -> Unit
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚀 Iniciar Exportación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Button(
                onClick = onExportClick,
                enabled = !isExporting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportando...")
                } else {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar Datos")
                }
            }
            
            Text(
                text = "El archivo se guardará en tu dispositivo y podrás compartirlo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 🎨 TARJETA DE PROGRESO DE EXPORTACIÓN
@Composable
private fun ExportProgressCard(
    progress: Float,
    status: String
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 Progreso de Exportación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 🎨 TARJETA DE RESULTADO DE EXPORTACIÓN
@Composable
private fun ExportResultCard(
    message: String,
    exportUri: Uri?,
    onShareClick: (Uri) -> Unit,
    onDismissClick: () -> Unit
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "✅ Exportación Completada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            exportUri?.let { uri ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onShareClick(uri) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartir")
                    }
                    
                    OutlinedButton(
                        onClick = onDismissClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Cerrar")
                    }
                }
            } ?: run {
                OutlinedButton(
                    onClick = onDismissClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

// 🎨 COMPONENTE DE INFORMACIÓN
@Composable
private fun InfoItem(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
