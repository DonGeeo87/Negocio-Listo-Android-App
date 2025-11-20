package com.negociolisto.app.ui.customers

import com.negociolisto.app.ui.components.UnifiedCard

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Note
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.domain.model.Customer
import androidx.compose.foundation.lazy.itemsIndexed
// removed unused ModernTopAppBar import

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
// removed incorrect lazy.item import
import com.negociolisto.app.ui.components.Formatters
import kotlinx.coroutines.delay

/**
 * 👤 PANTALLA DE DETALLES DEL CLIENTE
 * 
 * Muestra información completa del cliente antes de permitir la edición
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: String,
    onNavigateBack: () -> Unit,
    onEditCustomer: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Cargar datos del cliente
    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
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
    Column(modifier = modifier.fillMaxSize()) {
        // Botón de editar flotante en la parte superior
        if (uiState.customer != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onEditCustomer(customerId) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Cliente",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = slideInOffset)
                .alpha(fadeInAlpha)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Cargando información del cliente...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                uiState.error != null -> {
                    item {
                        UnifiedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Error",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                Text(
                    text = uiState.error ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadCustomer(customerId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
                
                uiState.customer != null -> {
                    val customer = uiState.customer!!
                    
                    // Header con información básica
                    item {
                        CustomerHeaderCard(customer = customer)
                    }
                    
                    // Información de contacto
                    item {
                        ContactInfoCard(customer = customer, context = context, viewModel = viewModel)
                    }
                    
                    // Información de compras
                    item {
                        PurchaseInfoCard(customer = customer)
                    }
                    
                    // Información adicional
                    if (customer.companyName != null || customer.address != null || customer.notes != null) {
                        item {
                            AdditionalInfoCard(customer = customer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerHeaderCard(customer: Customer) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar del cliente
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Nombre del cliente
            Text(
                text = customer.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // Empresa si existe
            customer.companyName?.let { companyName ->
                Text(
                    text = companyName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ContactInfoCard(
    customer: Customer,
    context: Context,
    viewModel: CustomerDetailViewModel
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📞 Información de Contacto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Teléfono
            customer.phone?.let { phone ->
                ContactItem(
                    icon = Icons.Default.Phone,
                    label = "Teléfono",
                    value = phone,
                    actionText = "Llamar",
                    onClick = { viewModel.callCustomer(context, customer) }
                )
            }
            
            // Email
            customer.email?.let { email ->
                ContactItem(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = email,
                    actionText = "Enviar Email",
                    onClick = { viewModel.sendEmail(context, customer) }
                )
            }
            
            // WhatsApp
            customer.phone?.let { phone ->
                ContactItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "WhatsApp",
                    value = phone,
                    actionText = "WhatsApp",
                    onClick = { viewModel.openWhatsApp(context, customer, "¡Hola ${customer.name}! 👋") },
                    isWhatsApp = true
                )
            }
        }
    }
}

@Composable
private fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    actionText: String,
    onClick: () -> Unit,
    isWhatsApp: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isWhatsApp) Color(0xFF25D366) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Card(
            modifier = Modifier.clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isWhatsApp) Color(0xFF25D366) else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = actionText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PurchaseInfoCard(customer: Customer) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "💰 Información de Compras",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    value = Formatters.formatClp(customer.totalPurchases),
                    label = "Total Compras",
                    icon = "💰"
                )
                StatItem(
                    value = customer.lastPurchaseDate?.let { Formatters.formatDate(it) } ?: "Nunca",
                    label = "Última Compra",
                    icon = "📅"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdditionalInfoCard(customer: Customer) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📝 Información Adicional",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            customer.address?.let { address ->
                InfoItem(
                    icon = Icons.Default.LocationOn,
                    label = "Dirección",
                    value = address
                )
            }
            
            customer.notes?.let { notes ->
                InfoItem(
                    icon = Icons.AutoMirrored.Filled.Note,
                    label = "Notas",
                    value = notes
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
