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
 * ⚖️ PUNTO DE EQUILIBRIO - MINI CLASE EDUCATIVA
 * 
 * Guía educativa para que el emprendedor comprenda y calcule su punto de equilibrio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakEvenScreen(
    vm: BusinessToolsViewModel = hiltViewModel(),
    @Suppress("UNUSED_PARAMETER") onBackClick: () -> Unit = {}
) {
    var showExplanation by remember { mutableStateOf(true) }
    var fixedCosts by remember { mutableStateOf("") }
    var variableCostsPerUnit by remember { mutableStateOf("") }
    var salePricePerUnit by remember { mutableStateOf("") }
    
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    
    // Calcular sugerencia de gastos del mes
    val suggestedExpenses = remember(expenses) {
        if (expenses.isNotEmpty()) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val thisMonth = LocalDate(now.year, now.monthNumber, 1)
            expenses
                .filter { it.date.date >= thisMonth }
                .sumOf { it.amount }
        } else 0.0
    }
    
    // Calcular costos fijos promedio del último mes
    LaunchedEffect(expenses) {
        if (fixedCosts.isEmpty() && expenses.isNotEmpty() && suggestedExpenses > 0) {
            fixedCosts = suggestedExpenses.toString()
        }
    }
    
    val fixedCostsNum = fixedCosts.toDoubleOrNull() ?: 0.0
    val variableCostsNum = variableCostsPerUnit.toDoubleOrNull() ?: 0.0
    val salePriceNum = salePricePerUnit.toDoubleOrNull() ?: 0.0
    
    // Punto de equilibrio en unidades
    val breakEvenUnits = if (salePriceNum > variableCostsNum && fixedCostsNum > 0) {
        fixedCostsNum / (salePriceNum - variableCostsNum)
    } else 0.0
    
    // Punto de equilibrio en dinero
    val breakEvenAmount = breakEvenUnits * salePriceNum
    
    // Margen de contribución
    val contributionMargin = if (salePriceNum > 0) {
        ((salePriceNum - variableCostsNum) / salePriceNum) * 100
    } else 0.0
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.cardPadding)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "⚖️ Punto de Equilibrio",
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
                        text = "📚 ¿Qué es el Punto de Equilibrio?",
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
                            text = "El punto de equilibrio es la cantidad de productos o servicios que debes vender para cubrir todos tus costos. Después de este punto, cada venta genera ganancia.",
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
                            text = "Si tus costos fijos son $100.000 al mes, vendes cada producto a $10.000 y te cuesta $6.000 producirlo, tu punto de equilibrio sería:\n\n100.000 ÷ (10.000 - 6.000) = 25 productos\n\nNecesitas vender 25 productos al mes solo para cubrir costos. A partir del producto 26, empiezas a ganar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        // Sección: Costos Fijos
        UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Costos Fijos Mensuales",
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
                        text = "Costos que pagas cada mes sin importar cuánto vendas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "• Arriendo o alquiler\n• Servicios (luz, agua, internet)\n• Salarios fijos\n• Seguros\n• Publicidad básica\n• Préstamos o créditos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    UnifiedTextField(
                        value = fixedCosts,
                        onValueChange = { fixedCosts = it },
                        label = "Total de Costos Fijos",
                        leadingIcon = Icons.Filled.Home,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    if (suggestedExpenses > 0) {
                        Text(
                            text = "💡 Sugerido basado en tus gastos del mes: ${Formatters.formatClpWithSymbol(suggestedExpenses)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Text(
                        text = "⚠️ No incluyas aquí el costo de los materiales o productos que compras para vender. Esos son costos variables.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        // Sección: Costos Variables
        UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Text(
                    text = "Costo Variable por Unidad",
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
                        text = "Costos que cambian según cuánto vendas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "• Costo de materiales por producto\n• Costo de producción por unidad\n• Comisiones de venta\n• Empaque por producto\n• Envío (si aplica)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    UnifiedTextField(
                        value = variableCostsPerUnit,
                        onValueChange = { variableCostsPerUnit = it },
                        label = "Costo por Unidad",
                        leadingIcon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    Text(
                        text = "💡 Si fabricas productos, suma: materiales + mano de obra por unidad. Si revendes, usa el precio al que compras cada producto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        // Sección: Precio de Venta
        UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Text(
                    text = "Precio de Venta por Unidad",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(DesignTokens.cardPadding).padding(bottom = 4.dp)
                )
                
                Column(
                    modifier = Modifier.padding(horizontal = DesignTokens.cardPadding).padding(bottom = DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "El precio al que vendes cada producto o servicio a tus clientes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    UnifiedTextField(
                        value = salePricePerUnit,
                        onValueChange = { salePricePerUnit = it },
                        label = "Precio de Venta",
                        leadingIcon = Icons.Filled.AttachMoney,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }
        
        // Resultados con explicación
        if (breakEvenUnits > 0) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📊 Tu Punto de Equilibrio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Resultado destacado
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${breakEvenUnits.toInt()}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "unidades al mes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "o ${Formatters.formatClpWithSymbol(breakEvenAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    
                    Divider()
                    
                    ResultRow(
                        "Margen de Contribución", 
                        "${Formatters.formatClpNumber(contributionMargin)}%",
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        valueColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Divider()
                    
                    // Explicación del resultado
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "💡 ¿Qué significa esto?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Necesitas vender ${breakEvenUnits.toInt()} unidades (${Formatters.formatClpWithSymbol(breakEvenAmount)}) al mes para cubrir todos tus costos.\n\n• Menos de ${breakEvenUnits.toInt()} ventas = Pierdes dinero\n• Exactamente ${breakEvenUnits.toInt()} ventas = Sin ganancia ni pérdida\n• Más de ${breakEvenUnits.toInt()} ventas = Empiezas a ganar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    
                    Divider()
                    
                    // Consejos
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🎯 Consejos para mejorar tu punto de equilibrio:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Reduce costos fijos negociando mejor arriendo o servicios\n• Aumenta el precio de venta (si el mercado lo permite)\n• Reduce costos variables buscando mejores proveedores\n• Vende más unidades con marketing efectivo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else if (fixedCostsNum > 0 || variableCostsNum > 0 || salePriceNum > 0) {
            // Mensaje de error o información incompleta
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ Información incompleta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Por favor completa todos los campos para calcular tu punto de equilibrio. Recuerda: el precio de venta debe ser mayor que el costo variable.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

