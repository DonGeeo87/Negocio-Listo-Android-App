package com.negociolisto.app.ui.business_tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.DesignTokens

/**
 * 🛠️ PANTALLA PRINCIPAL DE HERRAMIENTAS PARA TU NEGOCIO
 * 
 * Ofrece herramientas integradas que usan datos reales del negocio
 * para cálculos y análisis financieros.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessToolsScreen(
    onNavigateToPriceCalculator: () -> Unit = {},
    onNavigateToBreakEven: () -> Unit = {},
    onNavigateToInvestmentRecovery: () -> Unit = {},
    onNavigateToStockEstimator: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.cardPadding)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Título de bienvenida
        Text(
            text = "🛠️ Herramientas para tu Negocio",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Text(
            text = "Calculadoras y analizadores que usan tus datos reales",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Calculadora de Precios
        ToolCard(
            title = "💰 Calculadora de Precios",
            description = "Calcula márgenes, precios con IVA, descuentos y ganancias",
            icon = Icons.Filled.Calculate,
            iconColor = MaterialTheme.colorScheme.primary,
            onClick = onNavigateToPriceCalculator
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Punto de Equilibrio
        ToolCard(
            title = "⚖️ Punto de Equilibrio",
            description = "Descubre cuánto necesitas vender para cubrir todos tus costos",
            icon = Icons.AutoMirrored.Filled.TrendingFlat,
            iconColor = MaterialTheme.colorScheme.secondary,
            onClick = onNavigateToBreakEven
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Recuperación de Inversión
        ToolCard(
            title = "📈 Recuperación de Inversión",
            description = "Calcula cuánto tiempo tardarás en recuperar tu inversión inicial",
            icon = Icons.AutoMirrored.Filled.ShowChart,
            iconColor = MaterialTheme.colorScheme.tertiary,
            onClick = onNavigateToInvestmentRecovery
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Estimador de Stock
        ToolCard(
            title = "📦 Estimador de Stock",
            description = "Predice cuándo necesitarás reabastecer según tus ventas históricas",
            icon = Icons.Filled.Inventory,
            iconColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = onNavigateToStockEstimator
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * 🔲 TARJETA DE HERRAMIENTA
 */
@Composable
fun ToolCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium,
                color = iconColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Contenido
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Flecha
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Abrir",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

