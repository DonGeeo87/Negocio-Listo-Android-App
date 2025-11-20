package com.negociolisto.app.ui.collections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.domain.model.OrderStatus
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.inventory.InventoryViewModel

/**
 * 📋 PANTALLA DE DETALLE DE PEDIDO
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderDetailViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel()
) {
    val response by viewModel.response.collectAsStateWithLifecycle()
    val products by inventoryViewModel.products.collectAsStateWithLifecycle()
    
    LaunchedEffect(orderId) {
        viewModel.loadResponse(orderId)
    }
    
    // Calcular el total correctamente sumando todos los items
    val currentResponse = response
    val calculatedTotal = remember(currentResponse, products) {
        if (currentResponse == null) 0.0
        else {
            currentResponse.items.entries.sumOf { (productId, orderItem) ->
                val product = products.find { it.id == productId }
                val unitPrice = product?.salePrice ?: 0.0
                unitPrice * orderItem.quantity
            }
        }
    }
    
    // Usar el total calculado si el subtotal del pedido es 0 o menor
    val displayTotal = if (currentResponse?.subtotal ?: 0.0 <= 0.0) calculatedTotal else currentResponse?.subtotal ?: 0.0
    
    // NO usar ModernFormTopAppBar aquí para evitar doble topbar
    // El MainScreen ya maneja el topbar dinámico
    Column(modifier = modifier.fillMaxSize()) {
        if (response == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val order = response!!
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Estado del pedido (banner superior)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = when (order.status) {
                        OrderStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer
                        OrderStatus.IN_PRODUCTION -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Estado: ${order.status.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total: ${Formatters.formatClp(displayTotal)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${order.itemCount} ${if (order.itemCount == 1) "producto" else "productos"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Información del cliente
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "👤 Cliente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider()
                        Text("Nombre: ${order.clientName}")
                        Text("Email: ${order.clientEmail}")
                        Text("Teléfono: ${order.clientPhone}")
                    }
                }
                
                // Productos del pedido
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📦 Productos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider()
                        
                        order.items.forEach { (productId, orderItem) ->
                            val product = products.find { it.id == productId }
                            val productName = product?.name ?: "Producto desconocido"
                            val unitPrice = product?.salePrice ?: 0.0
                            val lineTotal = unitPrice * orderItem.quantity
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = productName,
                                            fontWeight = FontWeight.Medium,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Cantidad: ${orderItem.quantity} × ${Formatters.formatClp(unitPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = Formatters.formatClp(lineTotal),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                
                                // Valoración de estrellas
                                orderItem.rating?.let { rating ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Valoración:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            repeat(5) { index ->
                                                Icon(
                                                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (index < rating) 
                                                        MaterialTheme.colorScheme.primary 
                                                    else 
                                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "$rating/5",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                // Notas del cliente
                                if (!orderItem.notes.isNullOrBlank()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "📝 Notas del cliente:",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = orderItem.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                // Personalización
                                if (!orderItem.customization.isNullOrBlank()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "🎨 Personalización:",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = orderItem.customization,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                if (order.items.keys.last() != productId) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                }
                            }
                        }
                    }
                }
                
                // Información de entrega y pago
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🚚 Entrega y Pago",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider()
                        Text("Método de entrega: ${order.deliveryMethod}")
                        if (!order.address.isNullOrBlank()) {
                            Text("Dirección: ${order.address}")
                        }
                        Text("Método de pago: ${order.paymentMethod}")
                        if (order.desiredDate != null) {
                            Text("Fecha deseada: ${Formatters.formatDate(order.desiredDate)}")
                        }
                        if (order.urgent) {
                            Text(
                                text = "⚠️ Pedido Urgente",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Observaciones generales
                if (!order.observations.isNullOrBlank()) {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📝 Observaciones Generales",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider()
                            Text(order.observations)
                        }
                    }
                }
                
                // Comentarios del cliente
                if (!order.feedbackComments.isNullOrBlank()) {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💬 Comentarios del Cliente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider()
                            Text(
                                text = order.feedbackComments,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Notas internas del negocio
                if (!order.businessNotes.isNullOrBlank()) {
                    UnifiedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📋 Notas Internas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider()
                            Text(
                                text = order.businessNotes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Cambiar estado del pedido
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Cambiar Estado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (order.status != OrderStatus.APPROVED) {
                                OutlinedButton(
                                    onClick = { viewModel.updateStatus(OrderStatus.APPROVED) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Aprobado")
                                }
                            } else {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Aprobado")
                                }
                            }
                            if (order.status != OrderStatus.IN_PRODUCTION) {
                                OutlinedButton(
                                    onClick = { viewModel.updateStatus(OrderStatus.IN_PRODUCTION) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("En Producción")
                                }
                            } else {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("En Producción")
                                }
                            }
                            if (order.status != OrderStatus.READY_FOR_DELIVERY) {
                                OutlinedButton(
                                    onClick = { viewModel.updateStatus(OrderStatus.READY_FOR_DELIVERY) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Listo para Entrega")
                                }
                            } else {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Listo para Entrega")
                                }
                            }
                            if (order.status != OrderStatus.DELIVERED) {
                                OutlinedButton(
                                    onClick = { viewModel.updateStatus(OrderStatus.DELIVERED) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Entregado")
                                }
                            } else {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Entregado")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
