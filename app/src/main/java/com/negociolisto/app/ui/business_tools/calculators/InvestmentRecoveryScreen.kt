package com.negociolisto.app.ui.business_tools.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.ui.business_tools.BusinessToolsViewModel
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.UnifiedTextField
import kotlinx.datetime.*

/**
 * 📈 RECUPERACIÓN DE INVERSIÓN - MINI CLASE EDUCATIVA
 * 
 * Guía educativa para que el emprendedor comprenda y calcule su tiempo de recuperación de inversión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentRecoveryScreen(
    vm: BusinessToolsViewModel = hiltViewModel(),
    @Suppress("UNUSED_PARAMETER") onBackClick: () -> Unit = {}
) {
    var showExplanation by remember { mutableStateOf(true) }
    var initialInvestment by remember { mutableStateOf("") }
    var monthlyProfit by remember { mutableStateOf("") }
    
    val sales by vm.sales.collectAsStateWithLifecycle()
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    
    // Calcular ganancia mensual promedio
    val suggestedProfit = remember(sales, expenses) {
        if (sales.isNotEmpty() && expenses.isNotEmpty()) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val thisMonth = LocalDate(now.year, now.monthNumber, 1)
            
            val monthlySales = sales
                .filter { it.date.date >= thisMonth }
                .sumOf { it.total }
            
            val monthlyExpenses = expenses
                .filter { it.date.date >= thisMonth }
                .sumOf { it.amount }
            
            monthlySales - monthlyExpenses
        } else 0.0
    }
    
    LaunchedEffect(suggestedProfit) {
        if (monthlyProfit.isEmpty() && suggestedProfit > 0) {
            monthlyProfit = suggestedProfit.toString()
        }
    }
    
    val investmentNum = initialInvestment.toDoubleOrNull() ?: 0.0
    val profitNum = monthlyProfit.toDoubleOrNull() ?: 0.0
    
    // Meses para recuperar
    val monthsToRecover = if (profitNum > 0 && investmentNum > 0) {
        investmentNum / profitNum
    } else 0.0
    
    // Años y meses
    val years = monthsToRecover.toInt() / 12
    val months = monthsToRecover.toInt() % 12
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.cardPadding)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "📈 Recuperación de Inversión",
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
                        text = "📚 ¿Qué es el Tiempo de Recuperación de Inversión?",
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
                            text = "El tiempo de recuperación de inversión es el período que necesitas para recuperar todo el dinero que invertiste inicialmente en tu negocio. Es una métrica clave para evaluar la viabilidad de tu emprendimiento.",
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
                            text = "Si invertiste $500.000 para iniciar tu negocio y cada mes obtienes una ganancia de $50.000, tu tiempo de recuperación sería:\n\n500.000 ÷ 50.000 = 10 meses\n\nEsto significa que después de 10 meses habrás recuperado tu inversión inicial. A partir del mes 11, todas las ganancias son beneficios netos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        // Inversión inicial
        UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Inversión Inicial",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(DesignTokens.cardPadding).padding(bottom = 4.dp)
                        )
                        Text(
                            text = "💡 ¿Qué incluir?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = DesignTokens.cardPadding).padding(bottom = 8.dp)
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.padding(horizontal = DesignTokens.cardPadding).padding(bottom = DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Todo el dinero que gastaste para iniciar tu negocio:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "• Equipos y maquinaria\n• Mobiliario y decoración\n• Inventario inicial\n• Licencias y permisos\n• Publicidad de lanzamiento\n• Capital de trabajo inicial\n• Mejoras al local (si aplica)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    UnifiedTextField(
                        value = initialInvestment,
                        onValueChange = { initialInvestment = it },
                        label = "Total de Inversión Inicial",
                        leadingIcon = Icons.Filled.AccountBalance,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    
                    Text(
                        text = "💡 Incluye todos los gastos únicos que hiciste al iniciar, no los gastos mensuales recurrentes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        // Ganancia mensual (auto-completado)
        UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Text(
                    text = "Ganancia Mensual Promedio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(DesignTokens.cardPadding).padding(bottom = 4.dp)
                )
                Text(
                    text = "💡 ¿Qué incluir?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = DesignTokens.cardPadding).padding(bottom = 8.dp)
                )
                
                Column(
                    modifier = Modifier.padding(horizontal = DesignTokens.cardPadding).padding(bottom = DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "La ganancia mensual es la diferencia entre tus ingresos y tus gastos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "• Ingresos: Total de ventas del mes\n• Menos: Todos los gastos del mes (fijos y variables)\n• Resultado: Ganancia neta mensual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    UnifiedTextField(
                        value = monthlyProfit,
                        onValueChange = { monthlyProfit = it },
                        label = "Ganancia Mensual",
                        leadingIcon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    
                    if (suggestedProfit > 0) {
                        Text(
                            text = "💡 Sugerido basado en tus datos: ${Formatters.formatClpWithSymbol(suggestedProfit)} (ventas - gastos del mes)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Text(
                        text = "💡 Si tu negocio es nuevo, usa una proyección conservadora basada en tus primeros meses de operación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        // Resultados
        if (monthsToRecover > 0) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📊 Resultados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    ResultRow("Tiempo de recuperación", 
                        when {
                            years > 0 && months > 0 -> "$years año(s) y $months mes(es)"
                            years > 0 -> "$years año(s)"
                            else -> "$months mes(es)"
                        }
                    )
                    
                    ResultRow("Meses totales", "${monthsToRecover.toInt()} meses")
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Explicación del resultado
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "💡 ¿Qué significa esto?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Con una ganancia mensual de ${Formatters.formatClpWithSymbol(profitNum)}, recuperarás tu inversión de ${Formatters.formatClpWithSymbol(investmentNum)} en aproximadamente ${monthsToRecover.toInt()} meses (${if (years > 0) "$years año(s) y " else ""}${months} mes(es)).\n\n• Menos de ${monthsToRecover.toInt()} meses = Aún no has recuperado tu inversión\n• Después de ${monthsToRecover.toInt()} meses = Has recuperado tu inversión inicial\n• A partir del mes ${monthsToRecover.toInt() + 1} = Todas las ganancias son beneficios netos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Consejos
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🎯 Consejos para mejorar tu tiempo de recuperación:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Aumenta tus ventas mensuales con marketing efectivo\n• Reduce gastos innecesarios para aumentar la ganancia neta\n• Considera inversiones escalonadas en lugar de todo de una vez\n• Un tiempo de recuperación menor a 24 meses es generalmente considerado bueno",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

