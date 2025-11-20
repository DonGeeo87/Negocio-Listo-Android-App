package com.negociolisto.app.ui.customers

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.*
import com.negociolisto.app.ui.components.StandardFloatingActionButton
import com.negociolisto.app.ui.components.UnifiedStatsCard
import com.negociolisto.app.ui.components.StatData
import com.negociolisto.app.ui.components.StatIcon
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import kotlinx.coroutines.delay
import com.negociolisto.app.ui.design.*
import com.negociolisto.app.ui.design.AnimationTokens
import com.negociolisto.app.ui.components.FixedBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onAddCustomer: () -> Unit,
    onEditCustomer: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onImportContacts: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    // Feedback de carga inicial breve al entrar (especialmente en listas grandes)
    var isInitialLoading by remember { mutableStateOf(true) }
    // Estado para el diálogo de confirmación de eliminación
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    LaunchedEffect(customers.isEmpty()) {
        if (customers.isEmpty()) {
            kotlinx.coroutines.delay(400)
        }
        isInitialLoading = false
    }
    var query by remember { mutableStateOf("") }
    val filtered = remember(customers, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) customers else customers.filter {
            it.name.lowercase().contains(q) ||
            (it.phone?.lowercase()?.contains(q) == true) ||
            (it.email?.lowercase()?.contains(q) == true)
        }
    }
    val pageSize = 20
    var visibleCount by remember(filtered) { mutableStateOf(pageSize) }
    val page = remember(filtered, visibleCount) { filtered.take(visibleCount.coerceAtMost(filtered.size)) }
    Box(modifier = modifier.fillMaxSize()) {
        if (isInitialLoading && customers.isEmpty()) {
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
                    title = "👥 Estadísticas de Clientes",
                    stats = listOf(
                        StatData(
                            label = "Total Clientes",
                            value = customers.size.toString(),
                            icon = StatIcon.Vector(Icons.Filled.People),
                            color = BrandColors.blueLilac
                        ),
                        StatData(
                            label = "Clientes Activos",
                            value = customers.count { it.totalPurchases > 0 }.toString(),
                            icon = StatIcon.Vector(Icons.Filled.CheckCircle),
                            color = BrandColors.turquoise
                        ),
                        StatData(
                            label = "Ventas Totales",
                            value = Formatters.formatClp(customers.sumOf { it.totalPurchases }),
                            icon = StatIcon.Vector(Icons.Filled.ShoppingCart),
                            color = BrandColors.turquoiseLight
                        )
                    )
                )
            }
            
            // Tarjeta de importación de contactos
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icono de contactos (reducido)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    BrandColors.turquoise.copy(alpha = 0.2f),
                                    RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Contacts,
                                contentDescription = null,
                                tint = BrandColors.turquoise,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Texto descriptivo
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Importar Contactos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Importa contactos de tu agenda como clientes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Botón de acción (mejor ajustado)
                        UnifiedSecondaryButton(
                            text = "Importar",
                            onClick = onImportContacts,
                            icon = Icons.Filled.Add,
                            modifier = Modifier
                                .height(40.dp)
                                .widthIn(min = 120.dp)
                        )
                    }
                }
            }
            
            // Barra de búsqueda moderna
            item {
                UnifiedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = "Buscar clientes...",
                    leadingIcon = Icons.Filled.Search,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Lista de clientes
            if (page.isEmpty()) {
                item {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.cardPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👥",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay clientes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Comienza agregando tu primer cliente",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            UnifiedButton(
                                text = "Agregar Cliente",
                                onClick = onAddCustomer,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                items(page, key = { it.id }) { customer ->
                    UnifiedCard(
                        onClick = { onEditCustomer(customer.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Información principal (compacta)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Nombre del cliente
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                
                                // Información secundaria en una sola línea
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Teléfono (si existe)
                                    customer.phone?.let { phone ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Phone,
                                                contentDescription = null,
                                                tint = BrandColors.turquoise,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = phone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    
                                    // Saldo (siempre visible)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AttachMoney,
                                            contentDescription = null,
                                            tint = BrandColors.turquoise,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = Formatters.formatClp(customer.totalPurchases),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = BrandColors.turquoise,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            
                            // Botones de acción compactos
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Botón de WhatsApp (solo ícono)
                                IconButton(
                                    onClick = {
                                        viewModel.openWhatsApp(context, customer, "¡Hola ${customer.name}! 👋")
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                // Botón de llamar (solo ícono)
                                if (customer.phone != null) {
                                    IconButton(
                                        onClick = {
                                            viewModel.callCustomer(context, customer)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Phone,
                                            contentDescription = "Llamar",
                                            tint = BrandColors.blueLilac,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                // Botón de eliminar (solo ícono)
                                IconButton(
                                    onClick = {
                                        customerToDelete = customer
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                    
                // Botón cargar más
                if (visibleCount < filtered.size) {
                    item {
                        UnifiedButton(
                            text = "👥 Cargar más (${filtered.size - visibleCount} restantes)",
                            onClick = { visibleCount = (visibleCount + pageSize).coerceAtMost(filtered.size) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = "Nuevo Cliente",
            primaryButtonOnClick = onAddCustomer,
            primaryButtonIcon = Icons.Filled.Add,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Diálogo de confirmación para eliminar cliente
        customerToDelete?.let { customer ->
            AlertDialog(
                onDismissRequest = { customerToDelete = null },
                title = { Text("Eliminar Cliente") },
                text = {
                    Text("¿Estás seguro de que deseas eliminar a ${customer.name}? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCustomer(customer.id)
                            customerToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { customerToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun CustomerRow(customer: Customer, onClick: () -> Unit = {}) {
    Card(onClick = onClick) { Column(Modifier.padding(DesignTokens.cardPadding), verticalArrangement = Arrangement.spacedBy(DesignTokens.compactSpacing)) {
        Text(customer.name, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                customer.phone?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                customer.email?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(Formatters.formatClp(customer.totalPurchases), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                customer.lastPurchaseDate?.let { Text(Formatters.formatDate(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    onDone: () -> Unit,
    customerId: String? = null,
    modifier: Modifier = Modifier,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val existing = remember(customers, customerId) { customers.firstOrNull { it.id == customerId } }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf(existing?.companyName ?: "") }
    LaunchedEffect(existing?.id) {
        if (existing != null) {
            name = existing.name
            phone = existing.phone ?: ""
            email = existing.email ?: ""
            address = existing.address ?: ""
            notes = existing.notes ?: ""
        }
    }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(AnimationTokens.longDuration, easing = AnimationTokens.decelerateEasing),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(AnimationTokens.extraLongDuration, easing = AnimationTokens.decelerateEasing),
        label = "fadeIn"
    )

    Box(modifier = modifier.fillMaxSize()) {
    Column(
            modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .offset(y = slideInOffset)
            .alpha(fadeInAlpha),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Información Personal
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
            ) {
                // Header con gradiente
                GradientCard(
                    gradientColors = GradientTokens.brandGradient(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "👤 Información Personal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                UnifiedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = "Nombre *",
                    placeholder = "Ingresa el nombre del cliente",
                    isError = nameError != null,
                    errorMessage = nameError,
                    modifier = Modifier.fillMaxWidth()
                )
                
                UnifiedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = "Nombre de empresa",
                    placeholder = "Opcional - Nombre de la empresa",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Información de Contacto
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
            ) {
                Text(
                    text = "📞 Información de Contacto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                UnifiedTextField(
                    value = phone,
                    onValueChange = { phone = it; phoneError = null },
                    label = "Teléfono",
                    placeholder = "+56 9 1234 5678",
                    isError = phoneError != null,
                    errorMessage = phoneError,
                    modifier = Modifier.fillMaxWidth()
                )
                
                UnifiedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = "Email",
                    placeholder = "cliente@ejemplo.com",
                    isError = emailError != null,
                    errorMessage = emailError,
                    modifier = Modifier.fillMaxWidth()
                )
                
                UnifiedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Dirección",
                    placeholder = "Av. Principal 123, Santiago",
                    modifier = Modifier.fillMaxWidth()
                )
                
                UnifiedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notas adicionales",
                    placeholder = "Información adicional sobre el cliente...",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Espaciado para el bottom bar
        Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = if (existing == null) "💾 Agregar Cliente" else "💾 Actualizar Cliente",
            primaryButtonOnClick = {
                        // Validaciones básicas
                        if (name.isBlank()) { 
                            nameError = "El nombre es requerido"
                            return@FixedBottomBar 
                        }
                        if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { 
                            emailError = "Email inválido"
                            return@FixedBottomBar 
                        }
                        
                        // Validar duplicados antes de guardar
                        scope.launch {
                            var hasError = false
                            
                            // Validar teléfono duplicado
                            val trimmedPhone = phone.trim()
                            if (trimmedPhone.isNotBlank()) {
                                val duplicateByPhone = viewModel.checkCustomerByPhone(trimmedPhone, existing?.id)
                                if (duplicateByPhone != null) {
                                    phoneError = "Ya existe un cliente con este teléfono: ${duplicateByPhone.name}"
                                    hasError = true
                                }
                            }
                            
                            // Validar email duplicado
                            val trimmedEmail = email.trim()
                            if (trimmedEmail.isNotBlank()) {
                                val duplicateByEmail = viewModel.checkCustomerByEmail(trimmedEmail, existing?.id)
                                if (duplicateByEmail != null) {
                                    emailError = "Ya existe un cliente con este email: ${duplicateByEmail.name}"
                                    hasError = true
                                }
                            }
                            
                            if (hasError) return@launch
                            
                            // Crear o actualizar cliente
                            if (existing == null) {
                                val customer = Customer(
                                    id = UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    companyName = companyName.trim().ifBlank { null },
                                    phone = trimmedPhone.ifBlank { null },
                                    email = trimmedEmail.ifBlank { null },
                                    address = address.trim().ifBlank { null },
                                    notes = notes.trim().ifBlank { null },
                                    createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                                    totalPurchases = 0.0,
                                    lastPurchaseDate = null
                                )
                                viewModel.addCustomer(customer)
                            } else {
                                val updated = existing.copy(
                                    name = name.trim(),
                                    companyName = companyName.trim().ifBlank { null },
                                    phone = trimmedPhone.ifBlank { null },
                                    email = trimmedEmail.ifBlank { null },
                                    address = address.trim().ifBlank { null },
                                    notes = notes.trim().ifBlank { null }
                                )
                                viewModel.updateCustomer(updated)
                            }
                            onDone()
                        }
                    },
            primaryButtonIcon = Icons.Filled.Save,
            secondaryButtonText = "Cancelar",
            secondaryButtonOnClick = onDone,
            modifier = Modifier.align(Alignment.BottomCenter)
                )
    }
}



