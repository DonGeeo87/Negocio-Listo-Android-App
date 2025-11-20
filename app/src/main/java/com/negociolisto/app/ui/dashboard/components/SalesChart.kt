package com.negociolisto.app.ui.dashboard.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.domain.model.DailySales
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import kotlinx.datetime.*

/**
 * 📈 GRÁFICO DE VENTAS
 * 
 * Muestra las ventas de los últimos 7 días en un gráfico de líneas.
 */
@Composable
fun SalesChart(
    dailySales: List<DailySales>,
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
                text = "📈 Ventas (7 días)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = DesignTokens.itemSpacing)
            )
            
            if (dailySales.isEmpty()) {
                // Estado vacío - no hay datos
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text = "No hay datos de ventas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Las ventas aparecerán aquí",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else if (dailySales.all { it.amount == 0.0 }) {
                // Estado vacío - todos los valores son cero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💰",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text = "Sin ventas esta semana",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Registra tus primeras ventas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Gráfico con datos
                SalesLineChart(
                    dailySales = dailySales,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

/**
 * Gráfico de líneas para ventas diarias
 */
@Composable
private fun SalesLineChart(
    dailySales: List<DailySales>,
    modifier: Modifier = Modifier
) {
    // Animación de entrada
    var animationProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = EaseOutCubic
            )
        ) { value, _ ->
            animationProgress = value
        }
    }
    
    val maxValue = dailySales.maxOfOrNull { it.amount } ?: 1.0
    val minValue = 0.0
    val valueRange = (maxValue - minValue).coerceAtLeast(1.0)
    
    // Leer colores del tema antes de entrar al Canvas
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    val lineColor = BrandColors.secondary
    val gradientStartColor = BrandColors.secondary.copy(alpha = 0.3f)
    val gradientEndColor = BrandColors.secondary.copy(alpha = 0.0f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)
        
        // Calcular puntos del gráfico
        val points = if (dailySales.isNotEmpty()) {
            dailySales.mapIndexed { index, dailySale ->
                val x = if (dailySales.size > 1) {
                    padding + (chartWidth / (dailySales.size - 1)) * index
                } else {
                    padding + chartWidth / 2 // Centrar si solo hay un punto
                }
                val normalizedValue = if (valueRange > 0) {
                    ((dailySale.amount - minValue) / valueRange).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }
                val y = padding + chartHeight - (normalizedValue * chartHeight * animationProgress)
                Offset(x, y)
            }
        } else {
            emptyList()
        }
        
        // Dibujar línea de fondo (grid)
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = padding + (chartHeight / gridLines) * i
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Dibujar área bajo la línea (gradiente) solo si hay puntos
        if (points.isNotEmpty() && points.size > 1) {
            val areaPath = Path().apply {
                moveTo(points.first().x, padding + chartHeight)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, padding + chartHeight)
                close()
            }
            
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        gradientStartColor,
                        gradientEndColor
                    ),
                    startY = padding,
                    endY = padding + chartHeight
                )
            )
        }
        
        // Dibujar línea del gráfico solo si hay más de un punto
        if (points.isNotEmpty() && points.size > 1) {
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
            
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
        
        // Dibujar puntos solo si hay datos
        if (points.isNotEmpty()) {
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = surfaceColor,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
    }
    
    // Etiquetas de días (debajo del gráfico)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dailySales.forEach { dailySale ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatDayLabel(dailySale.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Formatear etiqueta de día
 */
private fun formatDayLabel(date: LocalDate): String {
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    val yesterday = today - DatePeriod(days = 1)
    
    return when {
        date == today -> "Hoy"
        date == yesterday -> "Ayer"
        else -> {
            val day = date.dayOfMonth
            val month = date.monthNumber
            "$day/$month"
        }
    }
}

