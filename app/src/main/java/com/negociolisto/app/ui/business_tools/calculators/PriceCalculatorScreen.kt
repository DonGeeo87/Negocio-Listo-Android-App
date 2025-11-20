package com.negociolisto.app.ui.business_tools.calculators

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.UnifiedTextField

enum class PriceCalculatorMode {
    RESELL,    // Reventa
    PRODUCTION // Producción/Fabricación
}

/**
 * 💰 CALCULADORA DE PRECIOS - MINI CLASE EDUCATIVA
 * 
 * Guía educativa para que el emprendedor comprenda y calcule precios, márgenes, IVA y descuentos.
 * Soporta dos modos: Reventa y Producción/Fabricación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceCalculatorScreen(
    onBackClick: () -> Unit = {}
) {
    var showExplanation by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(PriceCalculatorMode.RESELL) }
    
    var purchasePrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var ivaPercentage by remember { mutableStateOf("19") }
    var discountPercentage by remember { mutableStateOf("") }
    
    // Campos específicos para producción
    var materialCost by remember { mutableStateOf("") }
    var laborCost by remember { mutableStateOf("") }
    var overheadCost by remember { mutableStateOf("") } // Costos indirectos
    
    // Calcular costo de producción
    val materialCostNum = materialCost.toDoubleOrNull() ?: 0.0
    val laborCostNum = laborCost.toDoubleOrNull() ?: 0.0
    val overheadCostNum = overheadCost.toDoubleOrNull() ?: 0.0
    val productionCost = materialCostNum + laborCostNum + overheadCostNum
    
    // Si está en modo producción y hay costos, actualizar purchasePrice
    LaunchedEffect(mode, productionCost) {
        if (mode == PriceCalculatorMode.PRODUCTION && productionCost > 0 && purchasePrice.isEmpty()) {
            purchasePrice = productionCost.toString()
        }
    }
    
    // Cálculos
    val purchasePriceNum = if (mode == PriceCalculatorMode.PRODUCTION && productionCost > 0) {
        productionCost
    } else {
        purchasePrice.toDoubleOrNull() ?: 0.0
    }
    
    val salePriceNum = salePrice.toDoubleOrNull() ?: 0.0
    val ivaPercentageNum = ivaPercentage.toDoubleOrNull() ?: 0.0
    val discountPercentageNum = discountPercentage.toDoubleOrNull() ?: 0.0
    
    // Precio con IVA
    val priceWithIva = if (salePriceNum > 0) {
        salePriceNum * (1 + (ivaPercentageNum / 100))
    } else 0.0
    
    // Precio con descuento
    val priceWithDiscount = if (salePriceNum > 0 && discountPercentageNum > 0) {
        salePriceNum * (1 - (discountPercentageNum / 100))
    } else salePriceNum
    
    // Margen de ganancia
    val profitMargin = if (purchasePriceNum > 0 && salePriceNum > 0) {
        ((salePriceNum - purchasePriceNum) / purchasePriceNum) * 100
    } else 0.0
    
    // Ganancia por unidad
    val profitPerUnit = salePriceNum - purchasePriceNum
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.cardPadding)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "💰 Calculadora de Precios",
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
                        text = "📚 ¿Qué es la Calculadora de Precios?",
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
                            text = "Esta herramienta te ayuda a calcular precios de venta, márgenes de ganancia, precios con IVA y descuentos. Es fundamental para asegurar que tus productos sean rentables y competitivos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        Divider()
                        
                        Text(
                            text = "💡 Conceptos clave:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Margen de ganancia: Porcentaje de ganancia sobre el costo\n• Precio con IVA: Precio final que paga el cliente (incluye impuesto)\n• Descuento: Reducción del precio para atraer clientes\n• Ganancia por unidad: Diferencia entre precio de venta y costo",
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
                            text = "Si compras un producto en $10.000 y lo vendes en $15.000:\n\n• Margen de ganancia: ((15.000 - 10.000) ÷ 10.000) × 100 = 50%\n• Ganancia por unidad: $15.000 - $10.000 = $5.000\n• Con IVA 19%: $15.000 × 1.19 = $17.850\n• Con descuento 10%: $15.000 × 0.90 = $13.500",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        // Selector de modo
        UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Text(
                    text = "Tipo de negocio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(DesignTokens.cardPadding).padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.cardPadding)
                        .padding(bottom = DesignTokens.cardPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Reventa
                    ModeButton(
                        title = "🛒 Reventa",
                        description = "Compra y vende",
                        isSelected = mode == PriceCalculatorMode.RESELL,
                        onClick = { 
                            mode = PriceCalculatorMode.RESELL
                            materialCost = ""
                            laborCost = ""
                            overheadCost = ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Botón Producción
                    ModeButton(
                        title = "🏭 Producción",
                        description = "Fabricas/creas",
                        isSelected = mode == PriceCalculatorMode.PRODUCTION,
                        onClick = { 
                            mode = PriceCalculatorMode.PRODUCTION
                            purchasePrice = ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        if (mode == PriceCalculatorMode.RESELL) {
            // Modo Reventa
            UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column {
                    Text(
                        text = "Precio de Compra",
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
                            text = "El precio al que compras el producto a tu proveedor:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "• Precio unitario del producto\n• Incluye el costo de compra directo\n• No incluyas gastos de envío o almacenamiento aquí (son costos indirectos)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        UnifiedTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = "Precio de Compra",
                            leadingIcon = Icons.Filled.ShoppingCart,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }
        } else {
            // Modo Producción
            UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column {
                    Text(
                        text = "Costos de Producción",
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
                            text = "Suma todos los costos para producir una unidad:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        UnifiedTextField(
                            value = materialCost,
                            onValueChange = { materialCost = it },
                            label = "Materiales",
                            leadingIcon = Icons.Filled.Inventory2,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = "Ej: $10.000"
                        )
                        Text(
                            text = "💡 Materias primas, insumos y componentes necesarios",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        UnifiedTextField(
                            value = laborCost,
                            onValueChange = { laborCost = it },
                            label = "Mano de Obra / Tiempo",
                            leadingIcon = Icons.Filled.Schedule,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = "Ej: $5.000"
                        )
                        Text(
                            text = "💡 Tu tiempo o el costo de mano de obra por unidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        UnifiedTextField(
                            value = overheadCost,
                            onValueChange = { overheadCost = it },
                            label = "Costos Indirectos (opcional)",
                            leadingIcon = Icons.Filled.Build,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = "Ej: herramientas, energía, etc."
                        )
                        Text(
                            text = "💡 Herramientas, energía, espacio de trabajo (opcional)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        
                        if (productionCost > 0) {
                            Text(
                                text = "💰 Costo Total de Producción: ${Formatters.formatClpWithSymbol(productionCost)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Precio de venta
        UnifiedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column {
                Text(
                    text = "Precio de Venta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(DesignTokens.cardPadding).padding(bottom = 4.dp)
                )
                
                Column(
                    modifier = Modifier.padding(horizontal = DesignTokens.cardPadding).padding(bottom = DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "El precio al que vendes el producto a tus clientes (sin IVA ni descuentos).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    UnifiedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = "Precio de Venta",
                        leadingIcon = Icons.Filled.AttachMoney,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    Text(
                        text = "💡 Este precio debe cubrir tu costo y darte una ganancia razonable. Considera la competencia y el valor percibido por el cliente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        
        // IVA y Descuento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnifiedCard(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                Column {
                    Text(
                        text = "IVA (%)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(DesignTokens.cardPadding).padding(bottom = 4.dp)
                    )
                    
                    Column(
                        modifier = Modifier.padding(horizontal = DesignTokens.cardPadding).padding(bottom = DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UnifiedTextField(
                            value = ivaPercentage,
                            onValueChange = { ivaPercentage = it },
                            label = "IVA (%)",
                            leadingIcon = Icons.Filled.Percent,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Text(
                            text = "💡 En Chile es 19%. El IVA se suma al precio de venta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            
            UnifiedCard(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                Column {
                    Text(
                        text = "Descuento (%)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(DesignTokens.cardPadding).padding(bottom = 4.dp)
                    )
                    
                    Column(
                        modifier = Modifier.padding(horizontal = DesignTokens.cardPadding).padding(bottom = DesignTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UnifiedTextField(
                            value = discountPercentage,
                            onValueChange = { discountPercentage = it },
                            label = "Descuento (%)",
                            leadingIcon = Icons.Filled.Discount,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Text(
                            text = "💡 Opcional. Reducción del precio para promociones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
        
        // Resultados
        if (salePriceNum > 0 || purchasePriceNum > 0) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    
                    if (purchasePriceNum > 0 && salePriceNum > 0) {
                        ResultRow("Costo Total", Formatters.formatClpWithSymbol(purchasePriceNum))
                        ResultRow("Margen de Ganancia", "${Formatters.formatClpNumber(profitMargin)}%")
                        ResultRow("Ganancia por Unidad", Formatters.formatClpWithSymbol(profitPerUnit))
                    }
                    
                    if (priceWithIva > 0 && ivaPercentageNum > 0) {
                        ResultRow("Precio con IVA", Formatters.formatClpWithSymbol(priceWithIva))
                    }
                    
                    if (priceWithDiscount < salePriceNum && priceWithDiscount > 0 && discountPercentageNum > 0) {
                        ResultRow("Precio con Descuento", Formatters.formatClpWithSymbol(priceWithDiscount))
                        ResultRow("Ahorro del Cliente", Formatters.formatClpWithSymbol(salePriceNum - priceWithDiscount))
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Explicación del resultado
                    if (purchasePriceNum > 0 && salePriceNum > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "💡 ¿Qué significa esto?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• Margen de ${Formatters.formatClpNumber(profitMargin)}%: Por cada $100 de costo, ganas $${Formatters.formatClpNumber(profitMargin)}\n• Ganancia por unidad: ${Formatters.formatClpWithSymbol(profitPerUnit)} por cada producto vendido\n• Con IVA: El cliente pagará ${Formatters.formatClpWithSymbol(if (priceWithIva > 0) priceWithIva else salePriceNum)}${if (ivaPercentageNum > 0) " (incluye IVA)" else ""}\n• ${if (profitMargin > 0) "Tu precio es rentable" else "⚠️ Tu precio no cubre el costo"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Consejos
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🎯 Consejos para fijar precios:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• Un margen del 30-50% es común en reventa\n• En producción, considera 50-100% de margen para cubrir riesgos\n• Investiga precios de la competencia antes de fijar el tuyo\n• Considera el valor percibido: calidad, servicio, ubicación\n• Revisa tus precios periódicamente según cambios en costos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ModeButton(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )
        }
    }
}

