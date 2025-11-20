package com.negociolisto.app.ui.business_tools.calculators

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.ui.business_tools.BusinessToolsViewModel
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.DesignTokens
import kotlinx.datetime.*
import java.text.DecimalFormat

/**
 * 📦 ESTIMADOR DE STOCK - MINI CLASE EDUCATIVA
 * 
 * Guía educativa para que el emprendedor comprenda y use el estimador de stock.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockEstimatorScreen(
    vm: BusinessToolsViewModel = hiltViewModel(),
    @Suppress("UNUSED_PARAMETER") onBackClick: () -> Unit = {}
) {
    var showExplanation by remember { mutableStateOf(true) }
    val products by vm.products.collectAsStateWithLifecycle()
    val sales by vm.sales.collectAsStateWithLifecycle()
    
    val formatter = DecimalFormat("#,##0.0")
    
    // Calcular ventas promedio por producto
    val productSalesData = remember(sales, products) {
        products.filter { it.isActive }.map { product ->
            val productSales = sales
                .flatMap { it.items }
                .filter { it.productId == product.id }
            
            val totalSold = productSales.sumOf { it.quantity }
            
            // Promedio diario de ventas (últimos 30 días)
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val thirtyDaysAgo = now.date - DatePeriod(days = 30)
            
            val recentSales = sales
                .filter { it.date.date >= thirtyDaysAgo }
                .flatMap { it.items }
                .filter { it.productId == product.id }
                .sumOf { it.quantity }
            
            val dailyAverage = if (recentSales > 0) {
                recentSales / 30.0
            } else {
                // Si no hay ventas en 30 días, usar promedio total si hay ventas
                if (totalSold > 0) {
                    val daysSinceFirstSale = sales
                        .filter { sale -> sale.items.any { it.productId == product.id } }
                        .minOfOrNull { it.date.date }?.let { firstSaleDate ->
                            val daysDiff = (now.date.toEpochDays() - firstSaleDate.toEpochDays())
                            if (daysDiff > 0) totalSold / daysDiff.toDouble() else 0.0
                        } ?: 0.0
                    daysSinceFirstSale
                } else 0.0
            }
            
            // Días hasta agotar stock
            val daysUntilOutOfStock = if (dailyAverage > 0 && product.stockQuantity > 0) {
                product.stockQuantity / dailyAverage
            } else Double.POSITIVE_INFINITY
            
            ProductStockEstimate(
                product = product,
                totalSold = totalSold,
                dailyAverage = dailyAverage,
                daysUntilOutOfStock = daysUntilOutOfStock
            )
        }
        .filter { it.totalSold > 0 || it.product.stockQuantity > 0 }
        .sortedBy { it.daysUntilOutOfStock }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.cardPadding)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "📦 Estimador de Stock",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // Explicación inicial (acordeón)
        UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📚 ¿Qué es el Estimador de Stock?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showExplanation = !showExplanation }) {
                        Icon(
                            imageVector = if (showExplanation) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (showExplanation) "Ocultar" else "Mostrar"
                        )
                    }
                }
                
                if (showExplanation) {
                    Column(
                        modifier = Modifier.padding(horizontal = DesignTokens.cardPadding).padding(bottom = DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "El estimador de stock analiza tus ventas históricas para predecir cuándo necesitarás reabastecer cada producto. Te ayuda a evitar quedarte sin inventario y a optimizar tus compras.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        Divider()
                        
                        Text(
                            text = "💡 Ejemplo práctico:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Si tienes 100 unidades de un producto en stock y en los últimos 30 días vendiste 60 unidades (promedio de 2 unidades por día), el estimador calculará:\n\n100 unidades ÷ 2 unidades/día = 50 días\n\nEsto significa que te quedarás sin stock en aproximadamente 50 días, por lo que deberías reabastecer antes de ese tiempo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        Divider()
                        
                        Text(
                            text = "📊 ¿Cómo funciona?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Analiza tus ventas de los últimos 30 días\n• Calcula el promedio diario de ventas por producto\n• Divide tu stock actual entre el promedio diario\n• Te muestra cuántos días te quedan antes de agotar el inventario\n• Clasifica productos en: Normal, Bajo Stock (<30 días) o Crítico (<7 días)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        if (productSalesData.isEmpty()) {
            UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding * 2),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay productos con ventas o stock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Agrega productos y registra ventas para ver estimaciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            productSalesData.forEach { estimate ->
                EnhancedStockEstimateCard(estimate, formatter)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Sección de consejos
            UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🎯 Consejos para gestionar tu inventario:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "• Reabastece productos críticos (<7 días) inmediatamente\n• Planifica compras para productos con bajo stock (<30 días)\n• Considera el tiempo de entrega de tus proveedores al planificar\n• Mantén un stock de seguridad para productos de alta rotación\n• Revisa estas estimaciones semanalmente para ajustar tus compras\n• Ten en cuenta estacionalidad: algunos productos venden más en ciertas épocas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    Divider()
                    
                    Text(
                        text = "💡 Nota importante:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Estas estimaciones se basan en tus ventas históricas. Si tu negocio es nuevo o si hay cambios en la demanda, ajusta las predicciones según tu experiencia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

data class ProductStockEstimate(
    val product: com.negociolisto.app.domain.model.Product,
    val totalSold: Int,
    val dailyAverage: Double,
    val daysUntilOutOfStock: Double
)

@Composable
fun EnhancedStockEstimateCard(
    estimate: ProductStockEstimate,
    formatter: DecimalFormat
) {
    val isLowStock = estimate.daysUntilOutOfStock < 30 && estimate.daysUntilOutOfStock != Double.POSITIVE_INFINITY
    val isCriticalStock = estimate.daysUntilOutOfStock < 7 && estimate.daysUntilOutOfStock != Double.POSITIVE_INFINITY
    
    // Calcular porcentaje de riesgo (inverso de días hasta agotar)
    val riskPercentage = when {
        estimate.daysUntilOutOfStock == Double.POSITIVE_INFINITY -> 0f
        estimate.daysUntilOutOfStock > 60 -> 0.2f
        estimate.daysUntilOutOfStock > 30 -> 0.4f
        estimate.daysUntilOutOfStock > 14 -> 0.6f
        estimate.daysUntilOutOfStock > 7 -> 0.8f
        else -> 1f
    }
    
    val animatedRisk by animateFloatAsState(
        targetValue = riskPercentage,
        animationSpec = tween(1000),
        label = "risk"
    )
    
    // Colores según estado
    val (cardColor, iconColor, badgeColor, badgeTextColor) = when {
        isCriticalStock -> Quadruple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError
        )
        isLowStock -> Quadruple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary
        )
        else -> Quadruple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCriticalStock) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding)
        ) {
            // Header con nombre y badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícono circular con gradiente
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        iconColor.copy(alpha = 0.3f),
                                        iconColor.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Inventory2,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = estimate.product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                        if (estimate.daysUntilOutOfStock != Double.POSITIVE_INFINITY) {
                            Text(
                                text = "Stock: ${estimate.product.stockQuantity} unidades",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Badge de estado
                if (isCriticalStock || isLowStock) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = if (isCriticalStock) "⚠️ CRÍTICO" else "⚠️ BAJO",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (estimate.daysUntilOutOfStock != Double.POSITIVE_INFINITY) {
                // Indicador visual de tiempo hasta agotar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tiempo hasta agotar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${estimate.daysUntilOutOfStock.toInt()} días",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = iconColor
                        )
                    }
                    
                    // Barra de progreso visual
                    LinearProgressIndicator(
                        progress = { animatedRisk },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = iconColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Métricas en grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Venta promedio diaria
                MetricBox(
                    title = "Venta diaria",
                    value = "${formatter.format(estimate.dailyAverage)}",
                    unit = "unid/día",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Total vendido
                MetricBox(
                    title = "Total vendido",
                    value = "${estimate.totalSold}",
                    unit = "unidades",
                    icon = Icons.Filled.ShoppingBag,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            // Mensaje de acción
            if (isLowStock && estimate.daysUntilOutOfStock != Double.POSITIVE_INFINITY) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconColor.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isCriticalStock) {
                                "¡Reabastece urgentemente! Te quedan menos de ${estimate.daysUntilOutOfStock.toInt()} días."
                            } else {
                                "Considera reabastecer pronto. Te quedan aproximadamente ${estimate.daysUntilOutOfStock.toInt()} días."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Helper data class para cuadruple
data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
