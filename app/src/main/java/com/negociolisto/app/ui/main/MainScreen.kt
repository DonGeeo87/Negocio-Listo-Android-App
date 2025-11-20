package com.negociolisto.app.ui.main

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.negociolisto.app.ui.components.GlobalToast
import com.negociolisto.app.ui.components.ModernSidebar
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.ui.inventory.InventoryListScreen
import com.negociolisto.app.ui.inventory.AddEditProductScreen
import com.negociolisto.app.ui.inventory.ProductDetailScreen
import com.negociolisto.app.ui.theme.NegocioListoTheme
import com.negociolisto.app.ui.customers.CustomerListScreen
import com.negociolisto.app.ui.customers.AddEditCustomerScreen
import com.negociolisto.app.ui.customers.ContactImportScreen
import com.negociolisto.app.ui.sales.SalesListScreen
import com.negociolisto.app.ui.sales.RecordSaleScreen
import com.negociolisto.app.ui.sales.SaleDetailScreen
import com.negociolisto.app.ui.expenses.ExpenseListScreen
import com.negociolisto.app.ui.expenses.AddEditExpenseScreen
import com.negociolisto.app.ui.collections.CollectionListScreen
import com.negociolisto.app.ui.collections.AddEditCollectionScreen
import com.negociolisto.app.ui.collections.ChatScreen
import com.negociolisto.app.ui.collections.OrderDetailScreen
import com.negociolisto.app.ui.collections.CollectionResponsesScreen
import com.negociolisto.app.ui.invoices.InvoiceListScreen
import com.negociolisto.app.ui.invoices.InvoiceDetailScreen
import com.negociolisto.app.ui.invoices.InvoiceSettingsScreen
import com.negociolisto.app.ui.reports.ReportsScreen
import com.negociolisto.app.ui.dashboard.DashboardScreen
import com.negociolisto.app.ui.components.HelpBottomSheet
import com.negociolisto.app.ui.onboarding.OnboardingScreen
import com.negociolisto.app.ui.setup.InitialCategorySetupScreen
import com.negociolisto.app.data.local.UiPreferencesStore
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.negociolisto.app.ui.main.NavigationTrackingViewModel
import com.negociolisto.app.ui.auth.AuthViewModel
import com.negociolisto.app.notification.NotificationTriggerService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.negociolisto.app.ui.components.PermissionUtils
import android.os.Build

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationServiceEntryPoint {
    fun notificationTriggerService(): NotificationTriggerService
}

/**
 * 🏠 PANTALLA PRINCIPAL DE LA APP
 * 
 * Esta es la pantalla principal donde los usuarios pueden navegar
 * entre las diferentes secciones de la app usando el bottom navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onLoggedOut: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var isSidebarOpen by remember { mutableStateOf(false) }
    var isHelpSheetOpen by remember { mutableStateOf(false) }
    var isOnboardingOverlayOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Inicializar servicio de notificaciones cuando el usuario está autenticado
    val notificationTriggerService = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationServiceEntryPoint::class.java
        ).notificationTriggerService()
    }
    
    // Solicitar permiso de notificaciones (Android 13+)
    val requestNotificationPermission = PermissionUtils.rememberNotificationPermissionHandler(
        onGranted = {
            println("✅ Permiso de notificaciones concedido")
        },
        onDenied = {
            println("⚠️ Permiso de notificaciones denegado")
        }
    )
    
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            println("🔔 Inicializando servicio de notificaciones para usuario: ${currentUser.uid}")
            notificationTriggerService.startMonitoring(currentUser.uid)
            
            // Solicitar permiso de notificaciones si es Android 13+ y no está concedido
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission()
            }
        }
    }
    val uiPrefs = remember { UiPreferencesStore(context) }
    
    // Inyectar ViewModel para tracking de navegación
    val navigationTrackingViewModel: NavigationTrackingViewModel = hiltViewModel()
    
    // Tracking de pantallas - observar cambios de ruta
    LaunchedEffect(currentDestination?.route) {
        currentDestination?.route?.let { route ->
            // Mapear ruta a nombre de pantalla legible
            val screenName = when {
                route == "dashboard" -> "Dashboard"
                route == "inventory" -> "Inventory"
                route == "sales" -> "Sales"
                route == "expenses" -> "Expenses"
                route == "customers" -> "Customers"
                route == "collections" -> "Collections"
                route == "invoices" -> "Invoices"
                route == "reports" -> "Reports"
                route == "tools" -> "Tools"
                route.startsWith("product/") -> "ProductDetail"
                route.startsWith("sales/") -> "SalesDetail"
                route.startsWith("expenses/") -> "ExpensesDetail"
                route.startsWith("customers/") -> "CustomersDetail"
                route.startsWith("collections/") -> "CollectionsDetail"
                route.startsWith("invoices/") -> "InvoicesDetail"
                route.startsWith("tools/") -> "ToolsDetail"
                else -> route
            }
            navigationTrackingViewModel.analyticsHelper.logScreenView(screenName)
        }
    }
    
    // Estados de configuración inicial - ya no se usan aquí, se manejan en MainActivity
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "fadeIn"
    )

    // La densidad escalada ahora se aplica globalmente desde MainActivity
    // No necesitamos duplicar la lógica aquí

    Box(modifier = modifier.fillMaxSize()) {
        val snackbarHostState = remember { SnackbarHostState() }
        var showConfirmReset by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.systemBars,
            topBar = {
                ModernFormTopAppBar(
                    title = when {
                        currentDestination?.route == "dashboard" -> "📊 Dashboard"
                        currentDestination?.route == "inventory" -> "📦 Inventario"
                        currentDestination?.route == "sales" -> "💰 Ventas"
                        currentDestination?.route == "expenses" -> "💸 Gastos"
                        currentDestination?.route == "customers" -> "👥 Clientes"
                        currentDestination?.route == "collections" -> "📚 Colecciones"
                        currentDestination?.route == "collections/add" -> "📚 Nueva Colección"
                        currentDestination?.route?.startsWith("collections/edit") == true -> "📚 Editar Colección"
                        currentDestination?.route?.contains("/chat") == true -> "💬 Chat"
                        currentDestination?.route?.contains("/responses") == true -> "📋 Pedidos"
                        currentDestination?.route?.contains("/order/") == true -> "📋 Detalle del Pedido"
                        currentDestination?.route == "invoices" -> "📄 Facturas"
                        currentDestination?.route == "reports" -> "📊 Reportes"
                        currentDestination?.route == "tools" -> "🛠️ Herramientas"
                        currentDestination?.route == "tools/price-calculator" -> "💰 Calculadora de Precios"
                        currentDestination?.route == "tools/break-even" -> "⚖️ Punto de Equilibrio"
                        currentDestination?.route == "tools/investment-recovery" -> "📈 Recuperación de Inversión"
                        currentDestination?.route == "tools/stock-estimator" -> "📦 Estimador de Stock"
                        currentDestination?.route == "settings" -> "⚙️ Ajustes"
                        currentDestination?.route == "settings/edit-profile" -> "👤 Editar Perfil"
                        currentDestination?.route == "settings/edit-company" -> "🏢 Editar Empresa"
                        currentDestination?.route == "settings/backup-management" -> "💾 Backup & Respaldo"
                        currentDestination?.route == "settings/ui-scale" -> "📏 Escala de la Interfaz"
                        currentDestination?.route == "settings/category-management" -> "📂 Gestión de Categorías"
                        currentDestination?.route == "settings/usage-limits" -> "🔒 Límites de Uso"
                        currentDestination?.route == "invoices/settings" -> "⚙️ Configurar Facturas"
                        currentDestination?.route == "product/add" -> "📦 Nuevo Producto"
                        currentDestination?.route?.startsWith("product/edit") == true -> "📦 Editar Producto"
                        currentDestination?.route?.startsWith("product/detail") == true -> "📦 Detalle Producto"
                        currentDestination?.route == "product/category-management" -> "📂 Categorías de Productos"
                        currentDestination?.route == "expenses/add" -> "💸 Nuevo Gasto"
                        currentDestination?.route?.startsWith("expenses/edit") == true -> "💸 Editar Gasto"
                        currentDestination?.route == "sales/record" -> "💰 Registrar Venta"
                        currentDestination?.route?.startsWith("sales/edit") == true -> "✏️ Editar Venta"
                        currentDestination?.route?.startsWith("sales/detail") == true -> "🧾 Detalle de Venta"
                        currentDestination?.route == "customers/add" -> "👥 Nuevo Cliente"
                        currentDestination?.route?.startsWith("customers/edit") == true -> "👥 Editar Cliente"
                        currentDestination?.route?.startsWith("customers/detail") == true -> "👥 Detalle Cliente"
                        currentDestination?.route == "customers/import" -> "📥 Importar Contactos"
                        currentDestination?.route?.startsWith("invoices/create") == true -> "📄 Crear Factura"
                        currentDestination?.route?.startsWith("invoices/detail") == true -> "📄 Detalle Factura"
                        else -> "🏢 NegocioListo"
                    },
                    onBackClick = if (currentDestination?.route != "dashboard") {
                        {
                            val route = currentDestination?.route
                            when {
                                // Rutas principales - navegar al dashboard
                                route in listOf(
                                    "inventory", "sales", "expenses", "customers",
                                    "collections", "invoices", "reports", "tools", "settings"
                                ) -> {
                                    navController.navigate("dashboard") {
                                        // Limpiar el backstack hasta el dashboard
                                        popUpTo("dashboard") { inclusive = false }
                                        // Si ya estamos en dashboard, no hacer nada
                                        launchSingleTop = true
                                    }
                                }
                                // Subrutas - usar popBackStack normal
                                else -> {
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    } else {
                                        // Si no hay backstack anterior, ir al dashboard
                                        navController.navigate("dashboard") {
                                            popUpTo("dashboard") { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        }
                    } else null,
                    onHelpClick = { isHelpSheetOpen = true },
                    onMenuClick = { isSidebarOpen = true },
                    actions = {
                        // Botón de editar para pantalla de detalle de venta
                        if (currentDestination?.route?.startsWith("sales/detail/") == true) {
                            // Obtener saleId de los argumentos de navegación o de la ruta
                            val saleIdFromArgs = navBackStackEntry?.arguments?.getString("saleId")
                            val saleIdFromRoute = currentDestination?.route?.substringAfter("sales/detail/") ?: ""
                            val saleId = saleIdFromArgs ?: saleIdFromRoute
                            
                            android.util.Log.d("MainScreen", "Topbar - saleIdFromArgs: $saleIdFromArgs, saleIdFromRoute: $saleIdFromRoute, final: $saleId")
                            
                            if (saleId.isNotBlank() && saleId != "{saleId}") {
                                IconButton(
                                    onClick = {
                                        android.util.Log.d("MainScreen", "Editando venta desde topbar: $saleId")
                                        navController.navigate("sales/edit/$saleId")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Editar venta",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .offset(y = slideInOffset)
                        .alpha(fadeInAlpha)
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard", // Siempre ir directo al dashboard
            modifier = Modifier.padding(paddingValues)
        ) {
            
            composable("dashboard") { 
                DashboardScreen(
                    onNavigateToSales = { navController.navigate("sales") },
                    onNavigateToExpenses = { navController.navigate("expenses") },
                    onNavigateToInventory = { navController.navigate("inventory") },
                    onNavigateToCustomers = { navController.navigate("customers") },
                    onNavigateToCollections = { navController.navigate("collections") },
                    onNavigateToInvoices = { navController.navigate("invoices") },
                    onNavigateToTools = { navController.navigate("tools") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToAddSale = { navController.navigate("sales/record") },
                    onNavigateToAddProduct = { navController.navigate("product/add") },
                    onNavigateToAddExpense = { navController.navigate("expenses/add") },
                    onNavigateToAddCustomer = { navController.navigate("customers/add") },
                    onGoogleSignIn = { navController.navigate("login") },
                    onGoogleSignUp = { navController.navigate("register") }
                )
            }
            composable("inventory") {
                // Interceptar back gesture para navegar al dashboard
                BackHandler {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
                InventoryListScreen(
                    onProductClick = { product ->
                        navController.navigate("product/detail/${product.id}")
                    },
                    onEditProductClick = { product ->
                        navController.navigate("product/edit/${product.id}")
                    },
                    onAddProductClick = {
                        navController.navigate("product/add")
                    },
                    onBackClick = { 
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    onGoogleSignIn = { navController.navigate("login") },
                    onGoogleSignUp = { navController.navigate("register") }
                )
            }
            composable("product/add") {
                AddEditProductScreen(
                    onDone = { navController.popBackStack() },
                    onNavigateToCategoryManagement = { navController.navigate("product/category-management") }
                )
            }
            composable("product/edit/{productId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("productId")
                AddEditProductScreen(
                    onDone = { navController.popBackStack() }, 
                    productId = id,
                    onNavigateToCategoryManagement = { navController.navigate("product/category-management") }
                )
            }
            composable("product/detail/{productId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("productId")
                ProductDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("product/edit/$id") },
                    productId = id
                )
            }
            
            
            composable("sales") {
                // Interceptar back gesture para navegar al dashboard
                BackHandler {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
                SalesListScreen(
                    onAddSale = { navController.navigate("sales/record") },
                    onViewSale = { saleId -> 
                        android.util.Log.d("MainScreen", "👁️ onViewSale - saleId: '$saleId'")
                        navController.navigate("sales/detail/$saleId") 
                    },
                    onEditSale = { saleId -> 
                        android.util.Log.d("MainScreen", "✏️ onEditSale - saleId: '$saleId'")
                        navController.navigate("sales/detail/$saleId") 
                    },
                    onBackClick = { 
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    onGoogleSignIn = { navController.navigate("login") },
                    onGoogleSignUp = { navController.navigate("register") }
                )
            }
            composable("sales/record") {
                RecordSaleScreen(
                    onSave = { navController.popBackStack() }
                )
            }
            composable("sales/edit/{saleId}") { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId") ?: ""
                android.util.Log.d("MainScreen", "Navegando a edición de venta: saleId=$saleId")
                RecordSaleScreen(
                    saleId = saleId,
                    onSave = { navController.popBackStack() }
                )
            }
            composable("sales/detail/{saleId}") { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId") ?: ""
                android.util.Log.d("MainScreen", "📍 Navegando a sales/detail - saleId extraído: '$saleId'")
                SaleDetailScreen(
                    saleId = saleId,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        // Navegar a la pantalla de edición
                        android.util.Log.d("MainScreen", "✏️ onEdit llamado desde SaleDetailScreen - saleId: '$saleId'")
                        navController.navigate("sales/edit/$saleId")
                    }
                )
            }
            
            composable("expenses") {
                // Interceptar back gesture para navegar al dashboard
                BackHandler {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
                ExpenseListScreen(
                    onAddExpense = { navController.navigate("expenses/add") },
                    onEditExpense = { id -> navController.navigate("expenses/edit/$id") },
                    onBackClick = { 
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    }
                )
            }
            composable("expenses/add") {
                AddEditExpenseScreen(onDone = { navController.popBackStack() })
            }
            composable("expenses/edit/{expenseId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("expenseId")
                AddEditExpenseScreen(
                    onDone = { navController.popBackStack() },
                    expenseId = id
                )
            }
            
            composable("customers") {
                // Interceptar back gesture para navegar al dashboard
                BackHandler {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
                CustomerListScreen(
                    onAddCustomer = { navController.navigate("customers/add") },
                    onEditCustomer = { id -> navController.navigate("customers/edit/$id") },
                    onBackClick = { 
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    onImportContacts = { navController.navigate("customers/import") }
                )
            }
            composable("customers/add") {
                AddEditCustomerScreen(onDone = { navController.popBackStack() })
            }
            composable("customers/edit/{customerId}") { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId")
                AddEditCustomerScreen(onDone = { navController.popBackStack() }, customerId = customerId)
            }
            composable("customers/import") {
                ContactImportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportComplete = { navController.popBackStack() }
                )
            }

            composable("collections") {
                // Interceptar back gesture para navegar al dashboard
                BackHandler {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
                CollectionListScreen(
                    onAddCollection = { navController.navigate("collections/add") },
                    onEditCollection = { id -> navController.navigate("collections/edit/$id") },
                    onBackClick = { 
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    onOpenChat = { id -> navController.navigate("collections/$id/chat") },
                    onCopyPublicLink = { id ->
                        // Copiar al portapapeles: https://tu-proyecto.web.app/collection.html?id={id}
                        // La UI de copiar se maneja dentro de la pantalla, aquí solo navegamos o mostramos toast según capacidades actuales
                        // Se puede implementar un Snackbar/Toast global si existe
                    },
                    onViewResponses = { id -> navController.navigate("collections/$id/responses") }
                )
            }
            composable("collections/add") {
                AddEditCollectionScreen(
                    onDone = { navController.popBackStack() },
                    onNavigateToProductDetail = { productId -> navController.navigate("product/detail/$productId") }
                )
            }
            composable("collections/edit/{collectionId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("collectionId")
                AddEditCollectionScreen(
                    onDone = { navController.popBackStack() }, 
                    collectionId = id,
                    onNavigateToProductDetail = { productId -> navController.navigate("product/detail/$productId") }
                )
            }
            composable("collections/{collectionId}/chat") { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
                ChatScreen(
                    collectionId = collectionId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("collections/{collectionId}/responses") { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
                CollectionResponsesScreen(
                    collectionId = collectionId,
                    onBackClick = { navController.popBackStack() },
                    onOpenResponse = { responseId -> navController.navigate("collections/$collectionId/order/$responseId") }
                )
            }
            composable("collections/{collectionId}/order/{responseId}") { backStackEntry ->
                val responseId = backStackEntry.arguments?.getString("responseId") ?: ""
                OrderDetailScreen(
                    orderId = responseId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("invoices") {
                // Interceptar back gesture para navegar al dashboard
                BackHandler {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
                InvoiceListScreen(
                    onInvoiceClick = { id -> navController.navigate("invoices/detail/$id") },
                    onCreateInvoice = { navController.navigate("invoices/create") },
                    onSettingsClick = { navController.navigate("invoices/settings") },
                    onBackClick = { 
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    }
                )
            }
            composable("invoices/create?{saleId}") { backStackEntry ->
                // Extraer saleId de los argumentos (será null si no está presente)
                val saleId = backStackEntry.arguments?.getString("saleId")
                com.negociolisto.app.ui.invoices.CreateInvoiceScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = { id ->
                        // Navegar al detalle y remover la pantalla de creación del back stack
                        navController.navigate("invoices/detail/$id") {
                            popUpTo("invoices/create") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    saleId = saleId
                )
            }
            composable("invoices/detail/{invoiceId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("invoiceId") ?: ""
                InvoiceDetailScreen(invoiceId = id, onBack = { navController.popBackStack() })
            }
            composable("invoices/settings") {
                InvoiceSettingsScreen(onBack = { navController.popBackStack() })
            }

            // Reportes (análisis completo de ventas + facturas + gastos)
            composable("reports") {
                // Interceptar back gesture para navegar al dashboard
                BackHandler {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
                ReportsScreen(
                    onBackClick = { 
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    }
                )
            }

            // Herramientas para tu Negocio
            composable("tools") {
                // Interceptar back gesture para navegar al dashboard
                BackHandler {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
                com.negociolisto.app.ui.business_tools.BusinessToolsScreen(
                    onNavigateToPriceCalculator = { navController.navigate("tools/price-calculator") },
                    onNavigateToBreakEven = { navController.navigate("tools/break-even") },
                    onNavigateToInvestmentRecovery = { navController.navigate("tools/investment-recovery") },
                    onNavigateToStockEstimator = { navController.navigate("tools/stock-estimator") }
                )
            }
            
            // Calculadora de Precios
            composable("tools/price-calculator") {
                com.negociolisto.app.ui.business_tools.calculators.PriceCalculatorScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Punto de Equilibrio
            composable("tools/break-even") {
                com.negociolisto.app.ui.business_tools.calculators.BreakEvenScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Recuperación de Inversión
            composable("tools/investment-recovery") {
                com.negociolisto.app.ui.business_tools.calculators.InvestmentRecoveryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Estimador de Stock
            composable("tools/stock-estimator") {
                com.negociolisto.app.ui.business_tools.calculators.StockEstimatorScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Ajustes
            composable("settings") {
                // Interceptar back gesture para navegar al dashboard
                BackHandler {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
                com.negociolisto.app.ui.settings.SettingsScreen(
                    onBack = { 
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    onLoggedOut = {
                        // Delegar al NavHost raíz
                        onLoggedOut()
                    },
                    onEditProfile = {
                        navController.navigate("settings/edit-profile")
                    },
                    onEditCompany = {
                        navController.navigate("settings/edit-company")
                    },
                    onBackupManagement = {
                        navController.navigate("settings/backup-management")
                    },
                    onCategoryManagement = {
                        navController.navigate("settings/category-management")
                    },
                    onUIScaleSettings = {
                        navController.navigate("settings/ui-scale")
                    },
                    onUsageLimits = {
                        navController.navigate("settings/usage-limits")
                    },
                    onHelpClick = { isHelpSheetOpen = true },
                    onMenuClick = { isSidebarOpen = true }
                )
            }
            
            // Edición de perfil
            composable("settings/edit-profile") {
                com.negociolisto.app.ui.settings.EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() }
                )
            }
            
            // Edición de empresa
            composable("settings/edit-company") {
                com.negociolisto.app.ui.settings.EditCompanyScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() }
                )
            }
            
            // Gestión de categorías
            composable("settings/category-management") {
                com.negociolisto.app.ui.categories.CategoryManagementScreen(
                    onBackClick = { navController.popBackStack() },
                    comeFromProductScreen = false
                )
            }
            
            // Gestión de categorías desde crear producto
            composable("product/category-management") {
                com.negociolisto.app.ui.categories.CategoryManagementScreen(
                    onBackClick = { navController.popBackStack() },
                    comeFromProductScreen = true,
                    onCategorySelected = { category ->
                        // Aquí se puede manejar la selección de categoría
                        navController.popBackStack()
                    }
                )
            }
            
            // Gestión de backups con Firebase
            composable("settings/backup-management") {
                com.negociolisto.app.ui.settings.BackupRestoreScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Configuración de escala de interfaz
            composable("settings/ui-scale") {
                com.negociolisto.app.ui.settings.UIScaleSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("settings/usage-limits") {
                com.negociolisto.app.ui.settings.UsageLimitsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        }
        
        // Sidebar
        ModernSidebar(
            isOpen = isSidebarOpen,
            onClose = { isSidebarOpen = false },
            onNavigate = { route ->
                try {
                    // Cerrar sidebar siempre
                    isSidebarOpen = false
                    
                    // Obtener la ruta actual
                    val currentRoute = currentDestination?.route
                    
                    // Definir rutas principales (no subrutas)
                    val mainRoutes = setOf(
                        "dashboard", "inventory", "sales", "expenses", 
                        "customers", "collections", "invoices", "reports", "tools"
                    )
                    
                    // Verificar si la ruta destino es una ruta principal
                    val isMainRoute = mainRoutes.contains(route)
                    // Verificar si estamos en una subruta (no es una ruta principal)
                    val isCurrentlyInSubroute = currentRoute != null && !mainRoutes.contains(currentRoute)
                    
                    // Si navegamos a una ruta principal desde el sidebar
                    if (isMainRoute) {
                        // Si estamos en una subruta o no estamos en la ruta destino, limpiar backstack
                        if (isCurrentlyInSubroute || currentRoute != route) {
                            navController.navigate(route) {
                                // Limpiar todo el backstack hasta el inicio y reemplazar
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                    saveState = false
                                }
                                launchSingleTop = true
                                // No restaurar estado para asegurar limpieza completa
                            }
                        }
                        // Si ya estamos en la ruta principal, solo cerrar sidebar (ya se cerró arriba)
                    } else {
                        // Para subrutas (settings/edit-profile, etc.), navegar normalmente
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                // Si venimos de otra subruta, limpiar el backstack apropiadamente
                                if (isCurrentlyInSubroute) {
                                    // Si ambas rutas son subrutas de settings, limpiar hasta settings si existe
                                    if (currentRoute?.startsWith("settings/") == true && route.startsWith("settings/")) {
                                        // Buscar si hay una ruta "settings" en el grafo y limpiar hasta ahí
                                        try {
                                            navController.graph.findNode("settings")?.let {
                                                popUpTo("settings") {
                                                    inclusive = false
                                                    saveState = false
                                                }
                                            } ?: run {
                                                // Si no existe ruta "settings", limpiar hasta el inicio
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    inclusive = false
                                                    saveState = false
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Si falla, limpiar hasta el inicio
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                inclusive = false
                                                saveState = false
                                            }
                                        }
                                    } else {
                                        // Si venimos de cualquier subruta y vamos a otra diferente, limpiar hasta inicio
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = false
                                            saveState = false
                                        }
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Log del error para debugging
                    println("❌ Error en navegación: ${e.message}")
                    e.printStackTrace()
                    // Cerrar sidebar incluso si hay error
                    isSidebarOpen = false
                }
            },
            currentRoute = currentDestination?.route
        )
        
        // Help Bottom Sheet
        HelpBottomSheet(
            isVisible = isHelpSheetOpen,
            onDismiss = { isHelpSheetOpen = false },
            onShowOnboarding = {
                isOnboardingOverlayOpen = true
            },
            onResetTutorials = {
                showConfirmReset = true
            }
        )

        if (showConfirmReset) {
            AlertDialog(
                onDismissRequest = { showConfirmReset = false },
                title = { Text("Reiniciar tutoriales") },
                text = { Text("Esto reiniciará el onboarding y las guías en todas las pantallas. ¿Quieres continuar?") },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmReset = false
                        scope.launch {
                            uiPrefs.resetTutorials()
                            isOnboardingOverlayOpen = true
                            snackbarHostState.showSnackbar("Tutoriales reiniciados")
                        }
                    }) { Text("Sí, reiniciar") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmReset = false }) { Text("Cancelar") }
                }
            )
        }

        if (isOnboardingOverlayOpen) {
            androidx.compose.material3.Surface(
                color = androidx.compose.material3.MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
            ) {
                OnboardingScreen(onComplete = { isOnboardingOverlayOpen = false })
            }
        }
        
        // Banner de conectividad (global)
        com.negociolisto.app.ui.components.ConnectivityBanner(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopCenter)
        )

        // Toast global para notificaciones
        GlobalToast()
    }
}

/**
 * Pantalla temporal para mostrar las secciones que aún no están implementadas
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "🚧 Próximamente disponible",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Clase de datos para los elementos del bottom navigation
 */
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)


@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    NegocioListoTheme {
        MainScreen()
    }
}