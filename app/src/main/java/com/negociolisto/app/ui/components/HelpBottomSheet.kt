package com.negociolisto.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onShowOnboarding: () -> Unit = {},
    onResetTutorials: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = { } // Ocultar el handle
        ) {
            HelpBottomSheetContent(
                onDismiss = onDismiss,
                onShowOnboarding = onShowOnboarding,
                onResetTutorials = onResetTutorials,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HelpBottomSheetContent(
    onDismiss: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onShowOnboarding: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onResetTutorials: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header compacto
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ayuda y Soporte",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tabs simplificados: Solo Atajos y Preguntas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Atajos", "Preguntas").forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = tween(200),
                    label = "tab_scale"
                )
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = { selectedTab = index }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (index == 0) Icons.Filled.Speed else Icons.AutoMirrored.Filled.Help,
                            contentDescription = title,
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contenido basado en tab seleccionado
        when (selectedTab) {
            0 -> ShortcutsContent()
            1 -> FAQContent()
        }
    }
}

@Composable
private fun ShortcutsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(HelpShortcuts) { shortcut ->
            ShortcutCard(shortcut = shortcut)
        }
    }
}

@Composable
private fun TutorialsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Aviso de videos próximos
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.VideoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Videos en camino",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Muy pronto podrás ver tutoriales en video paso a paso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HelpTutorials) { tutorial ->
                TutorialCard(tutorial = tutorial)
            }
        }
    }
}

@Composable
private fun FAQContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(HelpFAQs) { faq ->
            FAQCard(faq = faq)
        }
    }
}

@Composable
private fun ShortcutCard(shortcut: HelpShortcut) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = shortcut.icon,
                contentDescription = shortcut.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shortcut.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = shortcut.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TutorialCard(tutorial: HelpTutorial) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = tutorial.icon,
                    contentDescription = tutorial.title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = tutorial.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tutorial.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FAQCard(faq: HelpFAQ) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir"
                    )
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Data classes
enum class HelpTab(val title: String, val icon: ImageVector) {
    SHORTCUTS("Atajos", Icons.Filled.Speed),
    TUTORIALS("Tutoriales", Icons.Filled.PlayArrow),
    FAQ("Preguntas", Icons.AutoMirrored.Filled.Help)
}

data class HelpShortcut(
    val title: String,
    val description: String,
    val icon: ImageVector
)

data class HelpTutorial(
    val title: String,
    val description: String,
    val icon: ImageVector
)

data class HelpFAQ(
    val question: String,
    val answer: String
)

// Sample data - ACTUALIZADO 2024
private val HelpShortcuts = listOf(
    HelpShortcut(
        title = "📦 Inventario - Agregar Producto",
        description = "Toca el botón + en la pantalla de Inventario. Incluye código de barras, SKU automático, múltiples categorías, imágenes comprimidas automáticamente y sincronización con Firebase.",
        icon = Icons.Filled.Add
    ),
    HelpShortcut(
        title = "💰 Registrar Venta Rápida",
        description = "Dirígete a Ventas y toca 'Nueva Venta'. Agrega productos desde el carrito, selecciona cliente y método de pago. La app calcula totales automáticamente.",
        icon = Icons.Filled.ShoppingCart
    ),
    HelpShortcut(
        title = "📊 Ver Dashboard y Métricas",
        description = "Desde el Dashboard principal accede a reportes en tiempo real: ventas del día, inventario bajo stock, gastos recientes, y gráficos de rendimiento.",
        icon = Icons.Filled.Analytics
    ),
    HelpShortcut(
        title = "👥 Gestión de Clientes",
        description = "En Clientes puedes agregar, editar, buscar y filtrar. Incluye historial de ventas, asociación con colecciones y herramientas de gestión avanzadas.",
        icon = Icons.Filled.People
    ),
    HelpShortcut(
        title = "💸 Control de Gastos",
        description = "Registra gastos categorizados desde la sección Gastos. Filtra por categoría, fecha y muestra reportes detallados para análisis financiero.",
        icon = Icons.Filled.Receipt
    ),
    HelpShortcut(
        title = "📚 Crear Colecciones",
        description = "Organiza productos en colecciones personalizadas con precios totales, asociación con clientes y seguimiento de estados (activa/completada/cancelada).",
        icon = Icons.Filled.Category
    ),
    HelpShortcut(
        title = "📄 Generar Facturas",
        description = "Crea facturas profesionales desde la sección Facturas. Incluye selección de clientes, productos, cálculos de IVA (19%), impresión PDF y configuración de templates.",
        icon = Icons.Filled.Description
    ),
    HelpShortcut(
        title = "⚙️ Ajustar Tamaño UI",
        description = "Ve a Ajustes > Escala de la Interfaz para personalizar el tamaño visual. Ajusta entre 85% y 115% según tus preferencias de legibilidad.",
        icon = Icons.Filled.ZoomIn
    )
)

private val HelpTutorials = listOf(
    HelpTutorial(
        title = "Primeros Pasos",
        description = "Aprende a configurar tu negocio y agregar tus primeros productos",
        icon = Icons.Filled.PlayArrow
    ),
    HelpTutorial(
        title = "Gestión de Inventario",
        description = "Domina el control de stock, alertas y códigos de barras",
        icon = Icons.Filled.Inventory
    ),
    HelpTutorial(
        title = "Ventas y Facturación",
        description = "Aprende a registrar ventas y generar facturas profesionales",
        icon = Icons.Filled.Receipt
    ),
    HelpTutorial(
        title = "Reportes y Análisis",
        description = "Interpreta tus datos y toma decisiones basadas en métricas",
        icon = Icons.AutoMirrored.Filled.TrendingUp
    )
)

private val HelpFAQs = listOf(
    HelpFAQ(
        question = "¿Cómo sincrono mis datos con Firebase?",
        answer = "La sincronización es automática. Ve a Ajustes > Gestión de Backups para verificar el estado y configurar respaldos automáticos. Todos tus datos se sincronizan en tiempo real con Firebase Authentication."
    ),
    HelpFAQ(
        question = "¿Puedo usar la app completamente offline?",
        answer = "Sí, NegocioListo funciona 100% offline. Room Database almacena localmente todos tus datos. La sincronización con Firebase ocurre automáticamente cuando hay conexión."
    ),
    HelpFAQ(
        question = "¿Cómo ajusto el tamaño de la interfaz?",
        answer = "Ve a Ajustes > Escala de la Interfaz. Usa el slider para ajustar entre 85% y 115%. Los cambios se aplican de inmediato en toda la app, incluyendo pantallas internas y sub-pantallas."
    ),
    HelpFAQ(
        question = "¿Cómo comprimo imágenes de productos?",
        answer = "La compresión es automática al agregar imágenes. El sistema optimiza las imágenes a una resolución eficiente (máx. 1200x1200px) para reducir el uso de datos y almacenamiento sin perder calidad visual."
    ),
    HelpFAQ(
        question = "¿Puedo crear categorías personalizadas?",
        answer = "Sí, desde Ajustes > Gestión de Categorías puedes crear, editar y organizar tus propias categorías de productos. También puedes crear categorías directamente al agregar un producto."
    ),
    HelpFAQ(
        question = "¿Cómo genero códigos de barras?",
        answer = "Al agregar un producto, escanea el código de barras con la cámara o ingresa el código manualmente. El sistema genera automáticamente el SKU basado en nombre y categoría."
    ),
    HelpFAQ(
        question = "¿Las colecciones se pueden asociar a clientes?",
        answer = "Sí, al crear o editar una colección puedes asociarla a uno o varios clientes. Las colecciones muestran el total de productos incluidos y puedes filtrar por cliente asociado."
    ),
    HelpFAQ(
        question = "¿Cómo calculo el IVA en facturas?",
        answer = "El sistema calcula automáticamente el 19% de IVA sobre el subtotal. Puedes configurar templates de factura desde la sección Facturas > Configuración."
    ),
    HelpFAQ(
        question = "¿Puedo importar contactos desde mi agenda?",
        answer = "Sí, en la sección Clientes > Importar Contactos puedes importar contactos desde tu agenda del teléfono. Los datos se sincronizan con tu base de datos."
    ),
    HelpFAQ(
        question = "¿Cómo veo el historial de ventas por cliente?",
        answer = "Desde el detalle de un cliente, accede a 'Historial de Ventas' para ver todas las transacciones asociadas. Incluye fechas, montos y detalles de productos vendidos."
    )
)
