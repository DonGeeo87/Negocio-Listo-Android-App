package com.negociolisto.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 📊 DATOS DE ESTADÍSTICA
 * 
 * Representa una estadística individual con su etiqueta, valor, icono y color.
 * El icono puede ser un ImageVector o un emoji (String).
 */
sealed class StatIcon {
    data class Vector(val imageVector: ImageVector) : StatIcon()
    data class Emoji(val emoji: String) : StatIcon()
}

data class StatData(
    val label: String,
    val value: String,
    val icon: StatIcon,
    val color: Color
)

/**
 * 📊 TARJETA DE ESTADÍSTICAS UNIFICADA
 * 
 * Componente reutilizable para mostrar estadísticas en todas las pantallas de la app.
 * Usa EXACTAMENTE la misma estructura visual que InventoryStatsCard.
 * 
 * @param title Título de la tarjeta (puede incluir emoji)
 * @param stats Lista de hasta 3 estadísticas a mostrar
 * @param modifier Modificador de diseño
 */
@Composable
fun UnifiedStatsCard(
    title: String,
    stats: List<StatData>,
    modifier: Modifier = Modifier
) {
    // Limitar a 3 estadísticas para mantener diseño consistente
    val displayStats = stats.take(3)
    
    val elevation by animateDpAsState(
        targetValue = 4.dp,
        animationSpec = tween(300),
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = elevation, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                displayStats.forEach { stat ->
                    StatItem(
                        label = stat.label,
                        value = stat.value,
                        icon = stat.icon,
                        color = stat.color
                    )
                }
            }
        }
    }
}

/**
 * 📊 ÍTEM DE ESTADÍSTICA
 * 
 * Componente interno para mostrar una estadística individual.
 * Copia EXACTAMENTE el diseño de InventoryHeaderCard.StatItem (sin círculos ni gradientes).
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: StatIcon,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono plano (emoji o ImageVector) SIN círculo ni gradiente
        when (icon) {
            is StatIcon.Emoji -> {
                Text(
                    text = icon.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            is StatIcon.Vector -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Valor en negrita
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        
        // Label debajo
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

