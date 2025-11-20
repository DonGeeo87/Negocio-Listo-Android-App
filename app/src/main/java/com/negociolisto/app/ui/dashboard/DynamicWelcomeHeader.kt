package com.negociolisto.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.domain.model.TimeOfDay
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.GradientTokens

/**
 * 🌅 HEADER DE BIENVENIDA DINÁMICO
 * 
 * Header que cambia según la hora del día con saludos, iconos y gradientes únicos.
 */
@Composable
fun DynamicWelcomeHeader(
    userName: String,
    currentTimeOfDay: TimeOfDay,
    modifier: Modifier = Modifier
) {
    // Obtener datos según la hora del día
    val welcomeData = getWelcomeData(currentTimeOfDay)
    
    // Obtener gradiente según la hora del día
    val gradient = when (currentTimeOfDay) {
        TimeOfDay.DAWN -> GradientTokens.dawnGradient()
        TimeOfDay.MORNING -> GradientTokens.morningGradient()
        TimeOfDay.AFTERNOON -> GradientTokens.afternoonGradient()
        TimeOfDay.NIGHT -> GradientTokens.nightGradient()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DesignTokens.cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.cardElevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradient)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.cardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con icono y saludo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Saludo dinámico
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${welcomeData.saludo}, $userName! ${welcomeData.emoji}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = welcomeData.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Icono dinámico
                    Icon(
                        imageVector = welcomeData.icon,
                        contentDescription = welcomeData.description,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Mensaje motivacional
                Text(
                    text = welcomeData.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 📊 DATOS DE BIENVENIDA SEGÚN HORA DEL DÍA
 */
private data class WelcomeData(
    val saludo: String,
    val emoji: String,
    val subtitle: String,
    val message: String,
    val icon: ImageVector,
    val description: String
)

/**
 * 🕐 OBTENER DATOS DE BIENVENIDA
 */
private fun getWelcomeData(timeOfDay: TimeOfDay): WelcomeData {
    return when (timeOfDay) {
        TimeOfDay.DAWN -> WelcomeData(
            saludo = "Buenos días",
            emoji = "🌅",
            subtitle = "Resumen de tu negocio",
            message = "La madrugada es de los emprendedores. ¡Prepárate para conquistar el día!",
            icon = Icons.Default.WbSunny,
            description = "Amanecer"
        )
        TimeOfDay.MORNING -> WelcomeData(
            saludo = "¡Hola",
            emoji = "🌞",
            subtitle = "Resumen de tu negocio",
            message = "El éxito comienza con el primer cliente del día. ¡Dale la bienvenida con energía!",
            icon = Icons.Default.LightMode,
            description = "Mañana"
        )
        TimeOfDay.AFTERNOON -> WelcomeData(
            saludo = "Buenas tardes",
            emoji = "☀️",
            subtitle = "Resumen de tu negocio",
            message = "Revisa tus números del mediodía. ¿Cómo van las ventas? ¡Sigue así!",
            icon = Icons.Default.WbSunny,
            description = "Tarde"
        )
        TimeOfDay.NIGHT -> WelcomeData(
            saludo = "Buenas noches",
            emoji = "🌙",
            subtitle = "Resumen de tu negocio",
            message = "Celebra tus logros del día, por pequeños que sean. ¡Cada paso cuenta!",
            icon = Icons.Default.DarkMode,
            description = "Noche"
        )
    }
}
