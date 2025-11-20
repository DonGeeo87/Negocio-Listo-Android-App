package com.negociolisto.app.ui.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.domain.model.CollectionResponse
import com.negociolisto.app.domain.model.OrderStatus
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.ui.components.UnifiedCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionResponsesScreen(
    collectionId: String,
    onBackClick: () -> Unit,
    onOpenResponse: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionResponsesViewModel = hiltViewModel()
) {
    val responses by viewModel.responses.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(collectionId) {
        viewModel.loadResponses(collectionId)
    }

    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) }
    val filtered = remember(responses, selectedFilter) {
        if (selectedFilter == null) responses else responses.filter { it.status == selectedFilter }
    }

    // NO usar Scaffold con topBar para evitar doble topbar
    // El MainScreen ya maneja el topbar dinámico
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp)
    ) {
        // Estadísticas rápidas
        val stats = remember(responses) {
            mapOf(
                "total" to responses.size,
                "aprobados" to responses.count { it.status == OrderStatus.APPROVED },
                "produccion" to responses.count { it.status == OrderStatus.IN_PRODUCTION },
                "listos" to responses.count { it.status == OrderStatus.READY_FOR_DELIVERY },
                "entregados" to responses.count { it.status == OrderStatus.DELIVERED }
            )
        }
        
        // Card de estadísticas
        if (responses.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Total", stats["total"] ?: 0, MaterialTheme.colorScheme.primary)
                    StatItem("Aprobados", stats["aprobados"] ?: 0, Color(0xFF28A745))
                    StatItem("Producción", stats["produccion"] ?: 0, Color(0xFF17A2B8))
                    StatItem("Listos", stats["listos"] ?: 0, Color(0xFF007BFF))
                    StatItem("Entregados", stats["entregados"] ?: 0, Color(0xFF6C757D))
                }
            }
        }
        
        // Filtros rápidos con scroll horizontal
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                FilterChip(
                    label = "Todos (${stats["total"]})",
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
            }
            item {
                FilterChip(
                    label = "Aprobados (${stats["aprobados"]})",
                    selected = selectedFilter == OrderStatus.APPROVED,
                    onClick = { selectedFilter = OrderStatus.APPROVED }
                )
            }
            item {
                FilterChip(
                    label = "En Producción (${stats["produccion"]})",
                    selected = selectedFilter == OrderStatus.IN_PRODUCTION,
                    onClick = { selectedFilter = OrderStatus.IN_PRODUCTION }
                )
            }
            item {
                FilterChip(
                    label = "Listos (${stats["listos"]})",
                    selected = selectedFilter == OrderStatus.READY_FOR_DELIVERY,
                    onClick = { selectedFilter = OrderStatus.READY_FOR_DELIVERY }
                )
            }
            item {
                FilterChip(
                    label = "Entregados (${stats["entregados"]})",
                    selected = selectedFilter == OrderStatus.DELIVERED,
                    onClick = { selectedFilter = OrderStatus.DELIVERED }
                )
            }
            item {
                FilterChip(
                    label = "Cancelados",
                    selected = selectedFilter == OrderStatus.CANCELLED,
                    onClick = { selectedFilter = OrderStatus.CANCELLED }
                )
            }
        }

        if (isLoading && responses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay pedidos para esta colección")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { r ->
                    ResponseRow(r = r, onClick = { onOpenResponse(r.id) })
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            labelColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun StatItem(label: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResponseRow(r: CollectionResponse, onClick: () -> Unit) {
    val statusColor = remember(r.status) {
        when (r.status) {
            OrderStatus.APPROVED -> Color(0xFF28A745)
            OrderStatus.IN_PRODUCTION -> Color(0xFF17A2B8)
            OrderStatus.READY_FOR_DELIVERY -> Color(0xFF007BFF)
            OrderStatus.DELIVERED -> Color(0xFF6C757D)
            OrderStatus.CANCELLED -> Color(0xFFDC3545)
        }
    }
    
    val statusIcon = remember(r.status) {
        when (r.status) {
            OrderStatus.APPROVED -> Icons.Filled.CheckCircle
            OrderStatus.IN_PRODUCTION -> Icons.Filled.Schedule
            OrderStatus.READY_FOR_DELIVERY -> Icons.Filled.LocalShipping
            OrderStatus.DELIVERED -> Icons.Filled.Done
            OrderStatus.CANCELLED -> Icons.Filled.Warning
        }
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con nombre del cliente y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = r.clientName.ifBlank { r.clientEmail.ifBlank { r.clientPhone } },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = Formatters.formatDate(r.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Badge de estado
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = r.status.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            
            // Información del pedido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${r.itemCount} ${if (r.itemCount == 1) "producto" else "productos"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (r.urgent) {
                        Text(
                            text = "⚠️ Urgente",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = Formatters.formatClp(r.subtotal),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Información adicional si está disponible
            if (!r.deliveryMethod.isBlank() || !r.paymentMethod.isBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!r.deliveryMethod.isBlank()) {
                        Text(
                            text = "🚚 ${r.deliveryMethod.replaceFirstChar { it.uppercaseChar() }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!r.paymentMethod.isBlank()) {
                        Text(
                            text = "💳 ${r.paymentMethod.replaceFirstChar { it.uppercaseChar() }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

