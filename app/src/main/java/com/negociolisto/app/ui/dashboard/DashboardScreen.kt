package com.negociolisto.app.ui.dashboard

import com.negociolisto.app.ui.components.UnifiedCard

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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import com.negociolisto.app.domain.model.LowStockProduct
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.CoachMarkOverlay
import com.negociolisto.app.ui.components.CoachMarkTarget
import com.negociolisto.app.ui.components.CoachMarks
import com.negociolisto.app.data.local.UiPreferencesStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Rect
import com.negociolisto.app.ui.design.*
import com.negociolisto.app.ui.design.AnimationTokens
import com.negociolisto.app.ui.components.UnifiedDashboardTopAppBar
import com.negociolisto.app.ui.components.GoogleAuthCard
import com.negociolisto.app.ui.auth.AuthViewModel
import com.negociolisto.app.ui.dashboard.components.SalesChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    vm: DashboardViewModel = hiltViewModel(),
    inspirationVm: InspirationBoxViewModel = hiltViewModel(),
    authVm: AuthViewModel = hiltViewModel(),
    onNavigateToSales: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToCollections: () -> Unit = {},
    onNavigateToInvoices: () -> Unit = {},
    onNavigateToTools: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAddSale: () -> Unit = {},
    onNavigateToAddProduct: () -> Unit = {},
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToAddCustomer: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onGoogleSignUp: () -> Unit = {}
) {
    val m by vm.businessMetrics.collectAsStateWithLifecycle()
    val user by vm.currentUser.collectAsStateWithLifecycle()
    val isAuthenticated by authVm.isAuthenticated.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Estado de la caja sorpresa
    val currentTip by inspirationVm.currentTip.collectAsStateWithLifecycle()
    val currentTimeOfDay by inspirationVm.currentTimeOfDay.collectAsStateWithLifecycle()
    val isLoading by inspirationVm.isLoading.collectAsStateWithLifecycle()
    
    // Coach mark state
    val uiPrefs = remember { UiPreferencesStore(context) }
    val hasSeenCoachMark by uiPrefs.isCoachMarkSeen("dashboard").collectAsStateWithLifecycle(initialValue = false)
    var showCoachMark by remember { mutableStateOf(false) }
    var coachMarkBounds by remember { mutableStateOf<Rect?>(null) }
    var currentCoachMark by remember { mutableStateOf(CoachMarks.DASHBOARD_METRICS) }
    
    // Check if coach mark should be shown
    LaunchedEffect(hasSeenCoachMark) {
        if (!hasSeenCoachMark) {
            delay(1000) // Wait for UI to settle
            showCoachMark = true
        }
    }
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = tween(AnimationTokens.longDuration, easing = AnimationTokens.decelerateEasing),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "fadeIn"
    )
    
    // Overlay de carga inicial hasta tener primera emisión de métricas/usuario
    var isInitialLoading by remember { mutableStateOf(true) }
    LaunchedEffect(m.totalSales, m.totalExpenses, user) {
        // Consideramos primera emisión como "cargado" (aunque sean ceros) una vez que llega cualquier valor
        isInitialLoading = false
    }

    // NO usar Scaffold con topBar para evitar doble topbar
    // El MainScreen ya maneja el topbar dinámico
    Box(modifier = Modifier.fillMaxSize()) {
        if (isInitialLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .offset(y = slideInOffset)
                .alpha(fadeInAlpha),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.sectionSpacing)
        ) {
            // Header unificado de perfil/bienvenida
            item {
                com.negociolisto.app.ui.components.HeaderProfile(
                    title = "Hola, ${user?.name ?: "Usuario"}",
                    subtitle = when (currentTimeOfDay) {
                        com.negociolisto.app.domain.model.TimeOfDay.DAWN -> "Buenos días"
                        com.negociolisto.app.domain.model.TimeOfDay.MORNING -> "Buenos días"
                        com.negociolisto.app.domain.model.TimeOfDay.AFTERNOON -> "Buenas tardes"
                        com.negociolisto.app.domain.model.TimeOfDay.NIGHT -> "Buenas noches"
                    },
                    avatarEmoji = "🏪",
                    modifier = Modifier.fillMaxWidth()
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
            
            // Caja sorpresa de tips e inspiración
            item {
                InspirationBox(
                    currentTip = currentTip,
                    currentTimeOfDay = currentTimeOfDay,
                    isLoading = isLoading,
                    onTipRequested = {
                        inspirationVm.getNewRandomTip()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Separador visual entre sección de bienvenida y métricas
            item {
                Spacer(modifier = Modifier.height(DesignTokens.largeSpacing))
            }
            
            // Widget de resumen compacto con métricas principales
            item {
                CompactMetricsWidget(
                    totalSales = m.totalSales,
                    totalExpenses = m.totalExpenses,
                    grossMargin = m.grossMargin,
                    grossMarginPercent = m.grossMarginPercent,
                    salesThisMonth = m.salesThisMonth,
                    salesLastMonth = m.salesLastMonth,
                    salesGrowth = m.salesGrowth,
                    expensesThisMonth = m.expensesThisMonth,
                    expensesLastMonth = m.expensesLastMonth,
                    expenseGrowth = m.expenseGrowth,
                    onNavigateToSales = onNavigateToSales,
                    onNavigateToExpenses = onNavigateToExpenses
                )
            }
            
            // Gráfico de ventas (7 días) - Mostrar después de métricas principales
            if (m.dailySales.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
                    SalesChart(
                        dailySales = m.dailySales,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Métricas secundarias (scroll horizontal)
            item {
                Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
                SecondaryMetricsRow(
                    averageTicketSize = m.averageTicketSize,
                    totalProducts = m.totalProducts,
                    totalCustomers = m.totalCustomers,
                    activeCollections = m.activeCollections,
                    invoicesThisMonth = m.invoicesThisMonth,
                    onNavigateToInventory = onNavigateToInventory,
                    onNavigateToCustomers = onNavigateToCustomers,
                    onNavigateToCollections = onNavigateToCollections,
                    onNavigateToInvoices = onNavigateToInvoices
                )
            }
            
            // Quick Actions (Acciones rápidas)
            item {
                Spacer(modifier = Modifier.height(DesignTokens.largeSpacing))
                QuickActionsBar(
                    onAddSale = onNavigateToAddSale,
                    onAddProduct = onNavigateToAddProduct,
                    onAddExpense = onNavigateToAddExpense,
                    onAddCustomer = onNavigateToAddCustomer
                )
            }
            
            // Separador visual antes de accesos rápidos
            item {
                Spacer(modifier = Modifier.height(DesignTokens.largeSpacing))
            }
            
            // Accesos directos elegantes a las pantallas principales
            item {
                Text(
                    text = "🚀 Accesos Rápidos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = DesignTokens.itemSpacing)
                )
            }
            
            // Grid de accesos directos expandido (2x4)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
                ) {
                    // Primera fila: Inventario y Ventas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
                    ) {
                        QuickAccessCard(
                            title = "Inventario",
                            subtitle = "Gestionar productos",
                            icon = "📦",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = onNavigateToInventory,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Ventas",
                            subtitle = "Ver facturas",
                            icon = "💰",
                            color = MaterialTheme.colorScheme.secondary,
                            onClick = onNavigateToSales,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Segunda fila: Gastos y Clientes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
                    ) {
                        QuickAccessCard(
                            title = "Gastos",
                            subtitle = "Control de costos",
                            icon = "📊",
                            color = MaterialTheme.colorScheme.tertiary,
                            onClick = onNavigateToExpenses,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Clientes",
                            subtitle = "Base de datos",
                            icon = "👥",
                            color = MaterialTheme.colorScheme.error,
                            onClick = onNavigateToCustomers,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Tercera fila: Colecciones y Facturas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
                    ) {
                        QuickAccessCard(
                            title = "Colecciones",
                            subtitle = "Catálogos",
                            icon = "📚",
                            color = MaterialTheme.colorScheme.primaryContainer,
                            onClick = onNavigateToCollections,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Facturas",
                            subtitle = "Documentos",
                            icon = "📄",
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = onNavigateToInvoices,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Cuarta fila: Herramientas y Ajustes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
                    ) {
                        QuickAccessCard(
                            title = "Herramientas",
                            subtitle = "Calculadoras",
                            icon = "🛠️",
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick = onNavigateToTools,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Ajustes",
                            subtitle = "Configuración",
                            icon = "⚙️",
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = onNavigateToSettings,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Separador visual antes de alertas
            if (m.lowStockProducts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(DesignTokens.largeSpacing))
                }
            }
            
            // Alertas de stock bajo mejoradas
            if (m.lowStockProducts.isNotEmpty()) {
                item {
                    LowStockAlertCard(
                        lowStockProducts = m.lowStockProducts,
                        onNavigateToInventory = onNavigateToInventory
                    )
                }
            }
            
            // Separador visual antes de top productos
            item {
                Spacer(modifier = Modifier.height(DesignTokens.largeSpacing))
            }
            
            // Top productos
            item {
                CoachMarkTarget(
                    targetId = "dashboard_top_products",
                    onBoundsChanged = { bounds ->
                        if (currentCoachMark.id == "dashboard_top_products") {
                            coachMarkBounds = bounds
                        }
                    }
                ) {
                    Text(
                        text = "🏆 Productos Top",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onNavigateToInventory() }
                            .padding(bottom = DesignTokens.itemSpacing)
                    )
                }
            }
            
            items(m.topProducts.take(5)) { product ->
                UnifiedCard(
                    onClick = onNavigateToInventory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.rowSpacing)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        BrandColors.secondary.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📦",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Column {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Vendidos: ${product.totalQuantitySold}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = Formatters.formatClp(product.totalSalesAmount),
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandColors.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Separador visual antes de top clientes
            item {
                Spacer(modifier = Modifier.height(DesignTokens.largeSpacing))
            }
            
            // Top clientes
            item {
                CoachMarkTarget(
                    targetId = "dashboard_top_customers",
                    onBoundsChanged = { bounds ->
                        if (currentCoachMark.id == "dashboard_top_customers") {
                            coachMarkBounds = bounds
                        }
                    }
                ) {
                    Text(
                        text = "👥 Clientes Top",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onNavigateToCustomers() }
                            .padding(bottom = DesignTokens.itemSpacing)
                    )
                }
            }
            
            items(m.topCustomers.take(5)) { customer ->
                UnifiedCard(
                    onClick = onNavigateToCustomers,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.rowSpacing)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        BrandColors.primary.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "👤",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = customer.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = Formatters.formatClp(customer.totalSalesAmount),
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandColors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            
            // Espacio final
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        
        // Coach Mark Overlay
        val scope = rememberCoroutineScope()
        CoachMarkOverlay(
            isVisible = showCoachMark,
            targetBounds = coachMarkBounds,
            title = currentCoachMark.title,
            description = currentCoachMark.description,
            onDismiss = {
                showCoachMark = false
                scope.launch {
                    uiPrefs.setCoachMarkSeen("dashboard", true)
                }
            }
        )
    }
}

/**
 * 📊 WIDGET DE RESUMEN RÁPIDO MEJORADO
 * 
 * Muestra un resumen visual de las métricas principales con indicadores de tendencia.
 * Versión mejorada con mejor jerarquía tipográfica y estados vacíos informativos.
 */
@Composable
private fun QuickSummaryWidget(
    totalSales: Double,
    totalExpenses: Double,
    grossMargin: Double,
    grossMarginPercent: Double,
    modifier: Modifier = Modifier
) {
    UnifiedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.looseSpacing)
        ) {
            // Título con jerarquía consistente
            Text(
                text = "📈 Resumen Ejecutivo",
                style = MaterialTheme.typography.titleLarge, // Consistente con otros títulos
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Verificar si hay datos para mostrar
            val hasData = totalSales > 0 || totalExpenses > 0
            
            if (hasData) {
                // Gráfico de barras mejorado para visualizar proporciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.rowSpacing),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Barra de ventas
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        val salesHeight = if (totalSales > 0) {
                            (totalSales / (totalSales + totalExpenses)).coerceIn(0.1, 1.0)
                        } else 0.1
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((salesHeight * 80).dp.coerceAtLeast(20.dp)) // Altura mínima de 20dp
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            BrandColors.secondary.copy(alpha = 0.8f),
                                            BrandColors.secondary
                                        )
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ventas",
                            style = MaterialTheme.typography.bodyMedium, // Consistente
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatClp(totalSales),
                            style = MaterialTheme.typography.titleMedium, // Consistente
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.secondary
                        )
                    }
                    
                    // Barra de gastos
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        val expensesHeight = if (totalExpenses > 0) {
                            (totalExpenses / (totalSales + totalExpenses)).coerceIn(0.1, 1.0)
                        } else 0.1
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((expensesHeight * 80).dp.coerceAtLeast(20.dp)) // Altura mínima de 20dp
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            BrandColors.primary.copy(alpha = 0.8f),
                                            BrandColors.primary
                                        )
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gastos",
                            style = MaterialTheme.typography.bodyMedium, // Consistente
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatClp(totalExpenses),
                            style = MaterialTheme.typography.titleMedium, // Consistente
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.primary
                        )
                    }
                }
                
                // Indicadores de rendimiento mejorados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PerformanceIndicator(
                        label = "Margen",
                        value = Formatters.formatClp(grossMargin),
                        percentage = grossMarginPercent,
                        isPositive = grossMargin >= 0,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    PerformanceIndicator(
                        label = "Rentabilidad",
                        value = String.format("%.1f%%", grossMarginPercent),
                        percentage = grossMarginPercent,
                        isPositive = grossMarginPercent >= 10.0,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Estado vacío informativo cuando no hay datos
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.rowSpacing)
                ) {
                    Text(
                        text = "📊",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        text = "¡Comienza registrando tus primeras ventas!",
                        style = MaterialTheme.typography.titleMedium, // Consistente
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Los gráficos aparecerán aquí cuando tengas datos",
                        style = MaterialTheme.typography.bodyMedium, // Consistente
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 📊 INDICADOR DE RENDIMIENTO MEJORADO
 */
@Composable
private fun PerformanceIndicator(
    label: String,
    value: String,
    percentage: Double,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium, // Consistente
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge, // Más prominente
            fontWeight = FontWeight.Bold,
            color = if (isPositive) BrandColors.secondary else MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.compactSpacing)
        ) {
            Icon(
                imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = if (isPositive) "Tendencia positiva" else "Tendencia negativa",
                tint = if (isPositive) BrandColors.secondary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(DesignTokens.smallIconSize) // Un poco más grande
            )
            
            Text(
                text = if (isPositive) "Bueno" else "Revisar",
                style = MaterialTheme.typography.bodyMedium, // Consistente
                color = if (isPositive) BrandColors.secondary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 🚀 TARJETA DE ACCESO RÁPIDO
 * 
 * Componente compacto y elegante para accesos directos a las pantallas principales.
 * Diseño minimalista que no invade espacio pero mantiene funcionalidad.
 */
@Composable
fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono principal
            Text(
                text = icon,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Título
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Subtítulo
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 📊 WIDGET DE MÉTRICAS COMPACTO MEJORADO
 * 
 * Muestra las métricas principales con comparaciones temporales y indicadores de tendencia.
 */
@Composable
fun CompactMetricsWidget(
    totalSales: Double,
    totalExpenses: Double,
    grossMargin: Double,
    grossMarginPercent: Double,
    salesThisMonth: Double,
    salesLastMonth: Double,
    salesGrowth: Double,
    expensesThisMonth: Double,
    expensesLastMonth: Double,
    expenseGrowth: Double,
    onNavigateToSales: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding)
        ) {
            Text(
                text = "📊 Resumen Financiero",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = DesignTokens.itemSpacing)
            )
            
            // Métricas principales con comparaciones temporales
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.rowSpacing)
            ) {
                // Primera fila: Ventas (con comparación mensual)
                MetricWithTrend(
                    label = "Ventas",
                    value = Formatters.formatClp(salesThisMonth),
                    icon = "📈",
                    color = MaterialTheme.colorScheme.secondary,
                    comparisonValue = salesLastMonth,
                    growthPercent = salesGrowth,
                    subtitle = "Este mes",
                    onClick = onNavigateToSales,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Segunda fila: Gastos (con comparación mensual)
                MetricWithTrend(
                    label = "Gastos",
                    value = Formatters.formatClp(expensesThisMonth),
                    icon = "📉",
                    color = MaterialTheme.colorScheme.tertiary,
                    comparisonValue = expensesLastMonth,
                    growthPercent = expenseGrowth,
                    subtitle = "Este mes",
                    onClick = onNavigateToExpenses,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Tercera fila: Margen y % Margen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CleanMetricItem(
                        label = "Margen",
                        value = Formatters.formatClp(grossMargin),
                        icon = "💰",
                        color = if (grossMargin >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    CleanMetricItem(
                        label = "% Margen",
                        value = "${String.format("%.1f", grossMarginPercent)}%",
                        icon = "📊",
                        color = when {
                            grossMarginPercent >= 50 -> MaterialTheme.colorScheme.primary
                            grossMarginPercent >= 25 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        },
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Métrica con indicador de tendencia
 */
@Composable
private fun MetricWithTrend(
    label: String,
    value: String,
    icon: String,
    color: Color,
    comparisonValue: Double,
    growthPercent: Double,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.rowSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineSmall
                )
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Indicador de tendencia (mostrar si hay crecimiento/decrecimiento calculado)
            if (growthPercent != 0.0) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (growthPercent >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = if (growthPercent >= 0) "Crecimiento" else "Decrecimiento",
                            tint = if (growthPercent >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${if (growthPercent >= 0) "+" else ""}${String.format("%.1f", growthPercent)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (growthPercent >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 📈 ITEM DE MÉTRICA LIMPIO
 * 
 * Componente individual para cada métrica sin fondos grises,
 * manteniendo la estética consistente de la app.
 */
@Composable
fun CleanMetricItem(
    label: String,
    value: String,
    icon: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = DesignTokens.compactSpacing)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 📊 FILA DE MÉTRICAS SECUNDARIAS
 * 
 * Muestra métricas secundarias en scroll horizontal.
 */
@Composable
fun SecondaryMetricsRow(
    averageTicketSize: Double,
    totalProducts: Int,
    totalCustomers: Int,
    activeCollections: Int,
    invoicesThisMonth: Int,
    onNavigateToInventory: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "📈 Métricas del Negocio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = DesignTokens.itemSpacing)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                SecondaryMetricCard(
                    label = "Ticket Promedio",
                    value = Formatters.formatClp(averageTicketSize),
                    icon = "🎫",
                    onClick = { }
                )
            }
            
            item {
                SecondaryMetricCard(
                    label = "Productos",
                    value = totalProducts.toString(),
                    icon = "📦",
                    onClick = onNavigateToInventory
                )
            }
            
            item {
                SecondaryMetricCard(
                    label = "Clientes",
                    value = totalCustomers.toString(),
                    icon = "👥",
                    onClick = onNavigateToCustomers
                )
            }
            
            item {
                SecondaryMetricCard(
                    label = "Colecciones",
                    value = activeCollections.toString(),
                    icon = "📚",
                    onClick = onNavigateToCollections
                )
            }
            
            item {
                SecondaryMetricCard(
                    label = "Facturas",
                    value = invoicesThisMonth.toString(),
                    icon = "📄",
                    subtitle = "Este mes",
                    onClick = onNavigateToInvoices
                )
            }
        }
    }
}

/**
 * 📊 TARJETA DE MÉTRICA SECUNDARIA
 */
@Composable
private fun SecondaryMetricCard(
    label: String,
    value: String,
    icon: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * ⚠️ ALERTA DE STOCK BAJO MEJORADA
 * 
 * Muestra lista de productos con stock bajo.
 */
@Composable
fun LowStockAlertCard(
    lowStockProducts: List<LowStockProduct>,
    onNavigateToInventory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    UnifiedCard(
        onClick = { isExpanded = !isExpanded },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.rowSpacing)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Stock Bajo",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(DesignTokens.iconSize)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚠️ Stock Bajo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tienes ${lowStockProducts.size} producto${if (lowStockProducts.size != 1) "s" else ""} con stock bajo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Lista de productos (expandible)
            if (isExpanded && lowStockProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(DesignTokens.rowSpacing))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    lowStockProducts.take(5).forEach { product ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Stock: ${product.currentStock} / ${product.minStock}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (lowStockProducts.size > 5) {
                        Text(
                            text = "... y ${lowStockProducts.size - 5} más",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🚀 BARRA DE ACCIONES RÁPIDAS
 * 
 * Botones de acción rápida para tareas comunes.
 */
@Composable
fun QuickActionsBar(
    onAddSale: () -> Unit,
    onAddProduct: () -> Unit,
    onAddExpense: () -> Unit,
    onAddCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "⚡ Acciones Rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = DesignTokens.itemSpacing)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                QuickActionButton(
                    label = "Nueva Venta",
                    icon = Icons.Filled.PointOfSale,
                    onClick = onAddSale
                )
            }
            
            item {
                QuickActionButton(
                    label = "Agregar Producto",
                    icon = Icons.Filled.Inventory2,
                    onClick = onAddProduct
                )
            }
            
            item {
                QuickActionButton(
                    label = "Agregar Gasto",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    onClick = onAddExpense
                )
            }
            
            item {
                QuickActionButton(
                    label = "Agregar Cliente",
                    icon = Icons.Filled.PersonAdd,
                    onClick = onAddCustomer
                )
            }
        }
    }
}

/**
 * 🔘 BOTÓN DE ACCIÓN RÁPIDA
 */
@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

