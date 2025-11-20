package com.negociolisto.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.delay

@Composable
fun CoachMarkOverlay(
    isVisible: Boolean,
    targetBounds: androidx.compose.ui.geometry.Rect?,
    title: String,
    description: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible && targetBounds != null) {
        var animationVisible by remember { mutableStateOf(false) }
        
        LaunchedEffect(isVisible) {
            delay(100)
            animationVisible = true
        }
        
        val scale by animateFloatAsState(
            targetValue = if (animationVisible) 1f else 0.8f,
            animationSpec = tween(300, easing = EaseOutBack),
            label = "coach_mark_scale"
        )
        
        val alpha by animateFloatAsState(
            targetValue = if (animationVisible) 1f else 0f,
            animationSpec = tween(300),
            label = "coach_mark_alpha"
        )
        
        Box(
            modifier = modifier
                .fillMaxSize()
                .zIndex(1000f)
                .alpha(alpha)
                .clickable { onDismiss() }
        ) {
            // Overlay oscuro
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
            )
            
            // Highlight del elemento objetivo con borde más visible
            if (targetBounds != null) {
                val density = LocalDensity.current
                val x = with(density) { targetBounds.left.toDp() }
                val y = with(density) { targetBounds.top.toDp() }
                val w = with(density) { targetBounds.width.toDp() }
                val h = with(density) { targetBounds.height.toDp() }
                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(width = w, height = h)
                        .border(
                            width = 4.dp,
                            color = Color(0xFF1565C0),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .background(
                            Color(0xFF1565C0).copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        )
                )
            }
            
            // Tooltip con información
            CoachMarkTooltip(
                targetBounds = targetBounds,
                title = title,
                description = description,
                onDismiss = onDismiss,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
            )
        }
    }
}

@Composable
private fun CoachMarkTooltip(
    targetBounds: androidx.compose.ui.geometry.Rect?,
    title: String,
    description: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (targetBounds == null) return
    
    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    
    // Calcular posición del tooltip
    val tooltipWidth = 280.dp
    val tooltipHeight = 140.dp
    val padding = 16.dp
    
    // Centrar la tarjeta en pantalla (independiente del objetivo resaltado)
    val tooltipX = (screenWidth - tooltipWidth) / 2
    val tooltipY = (screenHeight - tooltipHeight) / 2
    
    Card(
        modifier = modifier
            .offset(x = tooltipX, y = tooltipY)
            .size(tooltipWidth, tooltipHeight)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Entendido")
                    }
                }
            }
        }
    }
}

@Composable
fun CoachMarkTarget(
    targetId: String,
    onBoundsChanged: (androidx.compose.ui.geometry.Rect?) -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            onBoundsChanged(bounds)
        }
    ) {
        content()
    }
}

// Data class para definir coach marks
data class CoachMarkData(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector? = null
)

// Coach marks predefinidos para diferentes pantallas
object CoachMarks {
    val DASHBOARD_METRICS = CoachMarkData(
        id = "dashboard_metrics",
        title = "📊 Métricas Principales",
        description = "Aquí puedes ver las métricas más importantes de tu negocio: ventas, gastos, margen y porcentaje de rentabilidad."
    )
    
    val DASHBOARD_TOP_PRODUCTS = CoachMarkData(
        id = "dashboard_top_products",
        title = "🏆 Productos Top",
        description = "Los productos más vendidos aparecen aquí. Útil para identificar qué productos generan más ingresos."
    )
    
    val DASHBOARD_TOP_CUSTOMERS = CoachMarkData(
        id = "dashboard_top_customers",
        title = "👥 Clientes Top",
        description = "Tus mejores clientes aparecen aquí. Útil para identificar a tus clientes más valiosos."
    )
    
    val INVENTORY_ADD_BUTTON = CoachMarkData(
        id = "inventory_add_button",
        title = "➕ Agregar Producto",
        description = "Toca aquí para agregar un nuevo producto a tu inventario."
    )
    
    val SALES_NEW_SALE = CoachMarkData(
        id = "sales_new_sale",
        title = "🛒 Nueva Venta",
        description = "Toca aquí para registrar una nueva venta."
    )
}
