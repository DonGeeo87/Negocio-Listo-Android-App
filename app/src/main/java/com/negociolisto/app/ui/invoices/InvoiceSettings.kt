package com.negociolisto.app.ui.invoices

import com.negociolisto.app.domain.model.InvoiceTemplateType
import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.domain.model.InvoiceItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.shadow
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.components.FixedBottomBar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import android.content.Context
import android.content.SharedPreferences

data class InvoiceSettings(
    val companyName: String = "Mi Empresa",
    val companyAddress: String = "Dirección de la empresa",
    val companyRut: String? = null,
    val companyPhone: String? = null,
    val companyEmail: String? = null,
    val logoUrl: String? = null,
    val defaultTemplate: InvoiceTemplateType = InvoiceTemplateType.CLASSIC,
    val priceIsNet: Boolean = true // true = precio neto (sin IVA), false = precio incluye IVA
)

object InvoiceSettingsStore {
    private lateinit var prefs: SharedPreferences
    private const val PREFS_NAME = "invoice_settings"
    private const val KEY_DEFAULT_TEMPLATE = "default_template"
    private const val KEY_PRICE_IS_NET = "price_is_net"

    private val _settings = MutableStateFlow(InvoiceSettings())
    val settings: StateFlow<InvoiceSettings> = _settings.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedTemplate = prefs.getString(KEY_DEFAULT_TEMPLATE, null)
        val defaultTemplate = when (storedTemplate) {
            InvoiceTemplateType.CLASSIC.name -> InvoiceTemplateType.CLASSIC
            InvoiceTemplateType.MODERN.name -> InvoiceTemplateType.MODERN
            InvoiceTemplateType.MINIMAL.name -> InvoiceTemplateType.MINIMAL
            else -> InvoiceTemplateType.CLASSIC
        }
        val priceIsNet = prefs.getBoolean(KEY_PRICE_IS_NET, true) // Por defecto precio neto
        _settings.value = _settings.value.copy(
            defaultTemplate = defaultTemplate,
            priceIsNet = priceIsNet
        )
    }

    fun update(newSettings: InvoiceSettings) {
        _settings.value = newSettings
        if (::prefs.isInitialized) {
            prefs.edit()
                .putString(KEY_DEFAULT_TEMPLATE, newSettings.defaultTemplate.name)
                .putBoolean(KEY_PRICE_IS_NET, newSettings.priceIsNet)
                .apply()
        }
    }
}

// Datos de prueba para las vistas previas
private val sampleInvoice = Invoice(
    number = "FAC-001",
    items = listOf(
        InvoiceItem("Producto A", 2, 15000.0),
        InvoiceItem("Producto B", 1, 25000.0),
        InvoiceItem("Servicio C", 3, 8000.0)
    ),
    subtotal = 71000.0,
    tax = 13490.0,
    total = 84490.0,
    date = kotlinx.datetime.LocalDateTime(2024, 1, 15, 10, 30),
    notes = "Gracias por su compra"
)

private val sampleCompanyInfo = mapOf(
    "name" to "NegocioListo S.A.",
    "address" to "Av. Principal 123, Santiago, Chile",
    "rut" to "12.345.678-9",
    "phone" to "+56 9 1234 5678",
    "email" to "contacto@negociolisto.cl"
)

private val sampleCustomerInfo = mapOf(
    "name" to "Cliente Ejemplo S.A.",
    "address" to "Calle Falsa 456, Santiago, Chile",
    "rut" to "98.765.432-1",
    "phone" to "+56 9 8765 4321"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(
    onBack: () -> Unit,
    viewModel: com.negociolisto.app.ui.settings.SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    invoiceViewModel: InvoiceViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val current by InvoiceSettingsStore.settings.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    var template by remember { mutableStateOf(current.defaultTemplate) }
    var priceIsNet by remember { mutableStateOf(current.priceIsNet) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información de la empresa (solo lectura) - Modernizada
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header con gradiente
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF009FE3),
                                        Color(0xFF312783)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "🏢 Información de la Empresa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Text(
                        text = "Los datos de la empresa se toman desde la configuración de perfil. Para modificar esta información, ve a Ajustes > Editar Empresa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Mostrar datos actuales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo de empresa
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                !user?.businessLogoUrl.isNullOrBlank() -> {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(user?.businessLogoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Logo de empresa",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Text(
                                        text = user?.businessName?.take(2)?.uppercase() ?: "E",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.businessName ?: "Sin nombre de empresa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = user?.businessAddress ?: "Sin dirección",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            user?.businessRut?.let { rut ->
                                Text(
                                    text = "RUT: $rut",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Configuración de precio e IVA - Nueva sección
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header con gradiente
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF009FE3),
                                        Color(0xFF312783)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "💰 Configuración de Precios e IVA",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Text(
                        text = "Selecciona cómo manejas los precios de tus productos:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Opción: Precio Neto (sin IVA)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (priceIsNet) 2.dp else 1.dp,
                                color = if (priceIsNet) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { priceIsNet = true },
                        colors = CardDefaults.cardColors(
                            containerColor = if (priceIsNet) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Precio Neto (sin IVA)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Los precios que ingresas son netos. El IVA se calcula y se agrega al total.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ejemplo: Precio $10.000 → Subtotal $10.000 + IVA $1.900 = Total $11.900",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (priceIsNet) {
                                Text(
                                    text = "✓",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    // Opción: Precio con IVA incluido
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (!priceIsNet) 2.dp else 1.dp,
                                color = if (!priceIsNet) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { priceIsNet = false },
                        colors = CardDefaults.cardColors(
                            containerColor = if (!priceIsNet) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Precio con IVA Incluido",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Los precios que ingresas ya incluyen el IVA. El IVA se calcula y se resta del total.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ejemplo: Precio $11.900 → Subtotal $10.000 + IVA $1.900 = Total $11.900",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (!priceIsNet) {
                                Text(
                                    text = "✓",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Vista previa de templates - Modernizada
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header con gradiente
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF009FE3),
                                        Color(0xFF312783)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "📄 Seleccionar Plantilla Predeterminada",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Text(
                        text = "Toca una plantilla para seleccionarla como predeterminada:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Template Clásica
                    TemplatePreviewCard(
                        title = "Clásica",
                        description = "Diseño tradicional con bordes y separadores",
                        isSelected = template == InvoiceTemplateType.CLASSIC,
                        onClick = { template = InvoiceTemplateType.CLASSIC }
                    ) {
                        ClassicTemplatePreview()
                    }
                    
                    // Template Moderna
                    TemplatePreviewCard(
                        title = "Moderna",
                        description = "Diseño contemporáneo con colores y tipografía moderna",
                        isSelected = template == InvoiceTemplateType.MODERN,
                        onClick = { template = InvoiceTemplateType.MODERN }
                    ) {
                        ModernTemplatePreview()
                    }
                    
                    // Template Minimalista
                    TemplatePreviewCard(
                        title = "Minimalista",
                        description = "Diseño limpio y simple, ideal para empresas modernas",
                        isSelected = template == InvoiceTemplateType.MINIMAL,
                        onClick = { template = InvoiceTemplateType.MINIMAL }
                    ) {
                        MinimalTemplatePreview()
                    }
                }
            }

            // Espaciado para el bottom bar
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = "💾 Guardar Configuración",
            primaryButtonOnClick = {
                        // Solo guardar el template, los datos de empresa vienen del usuario
                        InvoiceSettingsStore.update(
                            InvoiceSettings(
                                companyName = user?.businessName ?: "Mi Empresa",
                                companyAddress = user?.businessAddress ?: "Dirección de la empresa",
                                companyRut = user?.businessRut,
                                companyPhone = user?.businessPhone,
                                companyEmail = user?.businessEmail,
                                logoUrl = user?.businessLogoUrl,
                                defaultTemplate = template,
                                priceIsNet = priceIsNet
                            )
                        )
                        // Actualizar todas las facturas existentes con el nuevo template
                        invoiceViewModel.updateAllInvoiceTemplates(template)
                        onBack()
                    },
            primaryButtonIcon = Icons.Filled.Save,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TemplatePreviewCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isSelected) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Vista previa de la factura
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ClassicTemplatePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header con fondo gris claro
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(6.dp)
        ) {
            Column {
                Text(
                    text = sampleCompanyInfo["name"] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sampleCompanyInfo["address"] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        // Info de factura con borde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(6.dp)
        ) {
            Column {
                Text(
                    text = "FACTURA ${sampleInvoice.number}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Fecha: ${sampleInvoice.date.dayOfMonth}/${sampleInvoice.date.monthNumber}/${sampleInvoice.date.year}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cliente: ${sampleCustomerInfo["name"]}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Header de tabla con bordes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray)
                .background(Color(0xFFE8E8E8))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DESCRIPCIÓN",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "TOTAL",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Items de ejemplo con bordes
        sampleInvoice.items.take(2).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray, RoundedCornerShape(0.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = Formatters.formatClpWithSymbol(item.total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Líneas punteadas para más items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "• • • • • • • • • •",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        // Totales alineados a la derecha
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Subtotal: ${Formatters.formatClpWithSymbol(sampleInvoice.subtotal)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "IVA (19%): ${Formatters.formatClpWithSymbol(sampleInvoice.tax)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Total: ${Formatters.formatClpWithSymbol(sampleInvoice.total)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ModernTemplatePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header moderno con fondo azul (como en el template real)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF009FE3))
                .padding(6.dp)
        ) {
            Column {
                Text(
                    text = sampleCompanyInfo["name"] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = sampleCompanyInfo["address"] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }
        
        // Información adicional de empresa
        Text(
            text = "RUT: ${sampleCompanyInfo["rut"]}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Tel: ${sampleCompanyInfo["phone"]}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Título de factura
        Text(
            text = "FACTURA #${sampleInvoice.number}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Información de factura
        Text(
            text = "Fecha: ${sampleInvoice.date.dayOfMonth}/${sampleInvoice.date.monthNumber}/${sampleInvoice.date.year}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Cliente: ${sampleCustomerInfo["name"]}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Header de tabla moderna
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF009FE3).copy(alpha = 0.1f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DESCRIPCIÓN",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF009FE3)
            )
            Text(
                text = "TOTAL",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF009FE3)
            )
        }
        
        // Items con fondo alternado (como en el template real)
        sampleInvoice.items.take(3).forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (index % 2 == 1) Color(0xFFF8F9FA) else Color.Transparent
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = Formatters.formatClpWithSymbol(item.total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Total destacado con fondo púrpura (como en el template real)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF312783))
                .padding(horizontal = 4.dp, vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TOTAL:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = Formatters.formatClpWithSymbol(sampleInvoice.total),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        // Desglose de totales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Subtotal: ${Formatters.formatClpWithSymbol(sampleInvoice.subtotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "IVA (19%): ${Formatters.formatClpWithSymbol(sampleInvoice.tax)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MinimalTemplatePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header minimalista elegante (como en el template real)
        Text(
            text = sampleCompanyInfo["name"] ?: "",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        
        Text(
            text = sampleCompanyInfo["address"] ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7F8C8D)
        )
        
        Text(
            text = "RUT: ${sampleCompanyInfo["rut"]}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7F8C8D)
        )
        Text(
            text = "Tel: ${sampleCompanyInfo["phone"]}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7F8C8D)
        )
        
        // Línea sutil elegante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFBDC3C7))
        )
        
        // Información de factura minimalista
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cliente: ${sampleCustomerInfo["name"]}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Factura ${sampleInvoice.number}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = "Fecha: ${sampleInvoice.date.dayOfMonth}/${sampleInvoice.date.monthNumber}/${sampleInvoice.date.year}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Items con formato minimalista mejorado (como en el template real)
        sampleInvoice.items.take(2).forEach { item ->
            // Descripción del producto alineada a la izquierda
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Cantidad y precio unitario alineados a la derecha
            Text(
                text = "${item.quantity} × ${Formatters.formatClpWithSymbol(item.unitPrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            
            // Total del item en la línea siguiente, alineado a la derecha
            Text(
                text = Formatters.formatClpWithSymbol(item.total),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
        
        // Línea final elegante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFF2C3E50))
        )
        
        // Total minimalista pero destacado, alineado a la izquierda
        Text(
            text = "Total: ${Formatters.formatClpWithSymbol(sampleInvoice.total)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        
        // Desglose sutil alineado a la derecha
        Text(
            text = "Subtotal: ${Formatters.formatClpWithSymbol(sampleInvoice.subtotal)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7F8C8D),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
        Text(
            text = "IVA (19%): $${sampleInvoice.tax.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7F8C8D),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

