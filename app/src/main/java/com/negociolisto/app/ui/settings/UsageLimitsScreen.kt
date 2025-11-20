package com.negociolisto.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import com.negociolisto.app.data.service.UsageLimitsService
import com.negociolisto.app.data.service.LimitStatus
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.components.UnifiedCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🔒 PANTALLA DE LÍMITES DE USO
 * 
 * Muestra información sobre los límites de uso y el estado actual del usuario.
 */
@Composable
fun UsageLimitsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UsageLimitsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.loadUsageStatistics()
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(DesignTokens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.sectionSpacing)
    ) {
        // Información general
        item {
            InfoCard(
                title = "Información General",
                subtitle = "Estos límites están diseñados para garantizar un rendimiento óptimo y una experiencia fluida para todos los usuarios.",
                icon = Icons.Filled.Info
            )
        }
        
        // Estadísticas de uso
        item {
            Text(
                text = "Uso Actual",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = DesignTokens.smallSpacing)
            )
        }
        
        // Productos
        item {
            UsageLimitCard(
                title = "Productos",
                icon = Icons.Filled.Inventory,
                current = uiState.products.current,
                limit = uiState.products.limit,
                percentage = uiState.products.percentage,
                status = uiState.products.status
            )
        }
        
        // Clientes
        item {
            UsageLimitCard(
                title = "Clientes",
                icon = Icons.Filled.People,
                current = uiState.customers.current,
                limit = uiState.customers.limit,
                percentage = uiState.customers.percentage,
                status = uiState.customers.status
            )
        }
        
        // Colecciones
        item {
            UsageLimitCard(
                title = "Colecciones",
                icon = Icons.Filled.Collections,
                current = uiState.collections.current,
                limit = uiState.collections.limit,
                percentage = uiState.collections.percentage,
                status = uiState.collections.status
            )
        }
        
        // Información adicional
        item {
            InfoCard(
                title = "Mejoras Futuras",
                subtitle = "Estamos trabajando continuamente en mejorar la aplicación. En futuras versiones esperamos aumentar los límites disponibles y optimizar aún más el uso de almacenamiento.",
                icon = Icons.Filled.Info,
                isWarning = false
            )
        }
        
        // Mostrar error si existe
        uiState.errorMessage?.let { error ->
            item {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(DesignTokens.smallSpacing))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Error al cargar estadísticas",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
        
        // Botón de actualizar
        item {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.loadUsageStatistics()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(DesignTokens.smallSpacing))
                Text("Actualizar Estadísticas")
            }
        }
    }
}

/**
 * 📊 TARJETA DE LÍMITE DE USO
 */
@Composable
private fun UsageLimitCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    current: Int,
    limit: Int,
    percentage: Int,
    status: LimitStatus
) {
    val statusColor = when (status) {
        LimitStatus.OK -> MaterialTheme.colorScheme.primary
        LimitStatus.WARNING -> MaterialTheme.colorScheme.tertiary
        LimitStatus.CRITICAL -> MaterialTheme.colorScheme.error
    }
    
    val statusText = when (status) {
        LimitStatus.OK -> "Normal"
        LimitStatus.WARNING -> "Advertencia"
        LimitStatus.CRITICAL -> "Crítico"
    }
    
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = statusColor
                    )
                    Spacer(Modifier.width(DesignTokens.itemSpacing))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = DesignTokens.smallSpacing, vertical = DesignTokens.compactSpacing),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(Modifier.height(DesignTokens.itemSpacing))
            
            // Barra de progreso
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surface
            )
            
            Spacer(Modifier.height(DesignTokens.smallSpacing))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$current / $limit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

/**
 * ℹ️ TARJETA DE INFORMACIÓN
 */
@Composable
private fun InfoCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isWarning: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignTokens.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isWarning) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
            Spacer(Modifier.width(DesignTokens.itemSpacing))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isWarning) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
                Spacer(Modifier.height(DesignTokens.compactSpacing))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isWarning) {
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    }
                )
            }
        }
    }
}

/**
 * 📊 VIEWMODEL PARA LÍMITES DE USO
 */
@HiltViewModel
class UsageLimitsViewModel @Inject constructor(
    private val usageLimitsService: UsageLimitsService
) : androidx.lifecycle.ViewModel() {
    
    private val _uiState = mutableStateOf(
        UsageLimitsUiState(
            products = ItemUsageUiState(0, 100, 0, LimitStatus.OK),
            customers = ItemUsageUiState(0, 50, 0, LimitStatus.OK),
            collections = ItemUsageUiState(0, 50, 0, LimitStatus.OK),
            isLoading = false,
            errorMessage = null
        )
    )
    
    val uiState: State<UsageLimitsUiState> = _uiState
    
    fun loadUsageStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val stats = usageLimitsService.getUsageStatistics()
                _uiState.value = UsageLimitsUiState(
                    products = ItemUsageUiState(
                        current = stats.products.current,
                        limit = stats.products.limit,
                        percentage = stats.products.percentage,
                        status = stats.products.status
                    ),
                    customers = ItemUsageUiState(
                        current = stats.customers.current,
                        limit = stats.customers.limit,
                        percentage = stats.customers.percentage,
                        status = stats.customers.status
                    ),
                    collections = ItemUsageUiState(
                        current = stats.collections.current,
                        limit = stats.collections.limit,
                        percentage = stats.collections.percentage,
                        status = stats.collections.status
                    ),
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar estadísticas: ${e.message ?: "Error desconocido"}"
                )
            }
        }
    }
}

/**
 * 📊 ESTADO DE UI PARA LÍMITES
 */
data class UsageLimitsUiState(
    val products: ItemUsageUiState,
    val customers: ItemUsageUiState,
    val collections: ItemUsageUiState,
    val isLoading: Boolean,
    val errorMessage: String? = null
)

/**
 * 📦 ESTADO DE USO DE ITEM
 */
data class ItemUsageUiState(
    val current: Int,
    val limit: Int,
    val percentage: Int,
    val status: LimitStatus
)

