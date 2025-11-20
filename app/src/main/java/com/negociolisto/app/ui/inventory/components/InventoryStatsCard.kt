package com.negociolisto.app.ui.inventory.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.inventory.InventoryStats

/**
 * 📊 CARD DE ESTADÍSTICAS DE INVENTARIO
 * 
 * Componente que muestra un resumen visual de las estadísticas principales
 * del inventario. Es como el "dashboard ejecutivo" que da una vista rápida
 * del estado general del negocio.
 * 
 * Incluye:
 * - Total de productos
 * - Valor total del inventario
 * - Alertas de stock bajo
 * - Indicadores visuales de estado
 */
@Composable
fun InventoryStatsCard(
    stats: InventoryStats,
    lowStockCount: Int,
    modifier: Modifier = Modifier,
    onStatsClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.let { mod ->
            if (onStatsClick != null) {
                mod.clickable { onStatsClick() }
            } else {
                mod
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resumen del inventario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (onStatsClick != null) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ver más detalles",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Estadísticas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total de productos
                StatItem(
                    icon = Icons.Default.Inventory,
                    label = "Productos",
                    value = stats.totalProducts.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                // Valor total
                StatItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Valor total",
                    value = stats.getFormattedValue(),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                
                // Stock bajo
                StatItem(
                    icon = Icons.Filled.Warning,
                    label = "Stock bajo",
                    value = lowStockCount.toString(),
                    color = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Barra de progreso de salud del inventario
            InventoryHealthBar(
                totalProducts = stats.totalProducts,
                lowStockCount = lowStockCount
            )
        }
    }
}

/**
 * 📊 ITEM DE ESTADÍSTICA
 * 
 * Componente individual para mostrar una métrica específica.
 */
@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 📊 BARRA DE SALUD DEL INVENTARIO
 * 
 * Indicador visual del estado general del inventario.
 */
@Composable
private fun InventoryHealthBar(
    totalProducts: Int,
    lowStockCount: Int,
    modifier: Modifier = Modifier
) {
    val healthPercentage = if (totalProducts > 0) {
        ((totalProducts - lowStockCount).toFloat() / totalProducts) * 100
    } else {
        100f
    }
    
    val healthColor = when {
        healthPercentage >= 80 -> MaterialTheme.colorScheme.primary
        healthPercentage >= 60 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    
    val healthText = when {
        healthPercentage >= 80 -> "Excelente"
        healthPercentage >= 60 -> "Bueno"
        else -> "Necesita atención"
    }
    
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Salud del inventario",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "$healthText (${String.format("%.0f", healthPercentage)}%)",
                style = MaterialTheme.typography.labelMedium,
                color = healthColor,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = healthPercentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = healthColor,
            trackColor = healthColor.copy(alpha = 0.2f)
        )
    }
}

/**
 * 📊 CARD DE ESTADÍSTICAS EXPANDIDA
 * 
 * Versión más detallada con métricas adicionales.
 */
@Composable
fun ExpandedInventoryStatsCard(
    stats: InventoryStats,
    lowStockCount: Int,
    outOfStockCount: Int,
    averageProductValue: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado con ícono
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Análisis detallado",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Grid de estadísticas
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primera fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailedStatItem(
                        icon = Icons.Default.Inventory,
                        label = "Total productos",
                        value = stats.totalProducts.toString(),
                        subtitle = "activos",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    DetailedStatItem(
                        icon = Icons.Default.AttachMoney,
                        label = "Valor total",
                        value = stats.getFormattedValue(),
                        subtitle = "inventario",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Segunda fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailedStatItem(
                        icon = Icons.Filled.Warning,
                        label = "Stock bajo",
                        value = lowStockCount.toString(),
                        subtitle = "productos",
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    DetailedStatItem(
                        icon = Icons.Filled.ErrorOutline,
                        label = "Sin stock",
                        value = outOfStockCount.toString(),
                        subtitle = "agotados",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Tercera fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailedStatItem(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = "Valor promedio",
                        value = Formatters.formatClpWithSymbol(averageProductValue),
                        subtitle = "por producto",
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Espacio vacío para mantener simetría
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            // Barra de salud expandida
            InventoryHealthBar(
                totalProducts = stats.totalProducts,
                lowStockCount = lowStockCount
            )
        }
    }
}

/**
 * 📊 ITEM DE ESTADÍSTICA DETALLADA
 * 
 * Versión más rica del item de estadística.
 */
@Composable
private fun DetailedStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Dashboard Design**: Diseño de dashboard informativo
 * 2. **Data Visualization**: Visualización clara de métricas
 * 3. **Status Indicators**: Indicadores de estado visual
 * 4. **Progressive Disclosure**: Información básica y expandida
 * 5. **Color Psychology**: Uso de colores para comunicar estado
 * 
 * ANALOGÍA:
 * 
 * InventoryStatsCard es como el "tablero de instrumentos" de un auto:
 * 
 * 1. **Velocímetro**: Total de productos (qué tan grande es el inventario)
 * 2. **Combustible**: Valor total (cuánto dinero tienes invertido)
 * 3. **Luces de advertencia**: Stock bajo (qué necesita atención)
 * 4. **Indicador de salud**: Estado general del motor (inventario)
 * 5. **Display digital**: Métricas adicionales cuando las necesitas
 * 
 * MÉTRICAS MOSTRADAS:
 * ✅ **Total de productos**: Tamaño del inventario
 * ✅ **Valor total**: Inversión en inventario
 * ✅ **Stock bajo**: Productos que necesitan reposición
 * ✅ **Salud del inventario**: Indicador general de estado
 * ✅ **Productos agotados**: Críticos sin stock
 * ✅ **Valor promedio**: Precio promedio por producto
 * 
 * INDICADORES VISUALES:
 * - **Verde**: Estado saludable (80%+)
 * - **Amarillo**: Necesita atención (60-80%)
 * - **Rojo**: Crítico (<60%)
 * - **Iconos**: Identificación rápida de métricas
 * - **Progreso**: Barra de salud visual
 * 
 * BENEFICIOS:
 * - Vista rápida del estado general
 * - Identificación inmediata de problemas
 * - Métricas clave siempre visibles
 * - Navegación a detalles expandidos
 * - Diseño responsive y accesible
 */