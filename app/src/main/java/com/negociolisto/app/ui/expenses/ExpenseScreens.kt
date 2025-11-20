package com.negociolisto.app.ui.expenses

import com.negociolisto.app.ui.components.UnifiedCard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.negociolisto.app.domain.model.Expense
import com.negociolisto.app.ui.components.ModernListTopAppBar
import com.negociolisto.app.ui.components.StandardFloatingActionButton
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.domain.model.ExpenseCategory
import com.negociolisto.app.domain.model.ExpenseStatus
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.expenses.components.EmptyExpenseState
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.delay
import com.negociolisto.app.ui.design.*
import com.negociolisto.app.ui.design.AnimationTokens
import com.negociolisto.app.ui.components.UnifiedListTopAppBar
import com.negociolisto.app.ui.components.UnifiedFloatingActionButton
import com.negociolisto.app.ui.components.FixedBottomBar
import com.negociolisto.app.ui.components.UnifiedStatsCard
import com.negociolisto.app.ui.components.StatData
import com.negociolisto.app.ui.components.StatIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onAddExpense: () -> Unit,
    onEditExpense: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    var categoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<String?>(null) }

    // Filtro por rango rápido
    var dateFilter by remember { mutableStateOf("MONTH") }
    val now = remember { kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()) }
    val nowEpoch = remember(now) { now.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds() }
    val startEpoch = remember(dateFilter, nowEpoch) {
        when (dateFilter) {
            "TODAY" -> kotlinx.datetime.LocalDateTime(now.year, now.monthNumber, now.dayOfMonth, 0, 0).toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
            "WEEK" -> nowEpoch - 7L * 24L * 60L * 60L * 1000L
            "MONTH" -> kotlinx.datetime.LocalDateTime(now.year, now.monthNumber, 1, 0, 0).toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
            else -> null
        }
    }
    val filtered = remember(expenses, categoryFilter, startEpoch) {
        val base = categoryFilter?.let { cat -> expenses.filter { it.category == cat } } ?: expenses
        base.filter { e ->
            val epoch = e.date.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
            startEpoch == null || epoch >= startEpoch
        }
    }
    // Paginación en memoria para gastos
    val pageSize = 20
    var visibleCount by remember(filtered) { mutableStateOf(pageSize) }
    val page = remember(filtered, visibleCount) { filtered.take(visibleCount.coerceAtMost(filtered.size)) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filtros compactos en una sola línea
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // Filtros por categoría (solo las más comunes)
                UnifiedChip(
                    text = "Todas",
                    onClick = { categoryFilter = null },
                    isSelected = categoryFilter == null
                )
                // Mostrar solo las categorías más comunes para ahorrar espacio
                ExpenseCategory.values().take(4).forEach { cat ->
                    UnifiedChip(
                        text = cat.displayName.take(12), // Limitar texto
                        onClick = { categoryFilter = cat },
                        isSelected = categoryFilter == cat
                    )
                }
                
                // Separador visual
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                
                // Filtros por fecha compactos
                UnifiedChip(
                    text = "Hoy",
                    onClick = { dateFilter = "TODAY" },
                    isSelected = dateFilter == "TODAY"
                )
                UnifiedChip(
                    text = "7d",
                    onClick = { dateFilter = "WEEK" },
                    isSelected = dateFilter == "WEEK"
                )
                UnifiedChip(
                    text = "Mes",
                    onClick = { dateFilter = "MONTH" },
                    isSelected = dateFilter == "MONTH"
                )
                UnifiedChip(
                    text = "Todo",
                    onClick = { dateFilter = "ALL" },
                    isSelected = dateFilter == "ALL"
                )
            }

            // Resumen de estadísticas
            UnifiedStatsCard(
                title = "💸 Resumen de Gastos",
                stats = listOf(
                    StatData(
                        label = "Total",
                        value = Formatters.formatClp(filtered.sumOf { it.amount }),
                        icon = StatIcon.Vector(Icons.Filled.AttachMoney),
                        color = BrandColors.turquoise
                    ),
                    StatData(
                        label = "Cantidad",
                        value = filtered.size.toString(),
                        icon = StatIcon.Vector(Icons.Filled.Receipt),
                        color = BrandColors.blueLilac
                    ),
                    StatData(
                        label = "Promedio",
                        value = if (filtered.isNotEmpty()) Formatters.formatClp(filtered.sumOf { it.amount } / filtered.size) else "$0",
                        icon = StatIcon.Vector(Icons.Filled.Calculate),
                        color = BrandColors.turquoiseLight
                    )
                )
            )

            // Encabezado del listado de gastos (compacto)
            Text(
                text = "📋 Lista de Gastos (${filtered.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Mostrar empty state si no hay gastos
            if (filtered.isEmpty()) {
                EmptyExpenseState(
                    hasFilters = categoryFilter != null || (dateFilter != "ALL" && dateFilter != "MONTH"),
                    onAddExpenseClick = onAddExpense,
                    onClearFilters = { 
                        categoryFilter = null
                        dateFilter = "ALL"
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 0.dp,
                        end = 0.dp,
                        top = 0.dp,
                        bottom = 16.dp
                    )
                ) {
                    items(
                        items = page,
                        key = { it.id },
                        contentType = { "expense" }
                    ) { e -> 
                        ExpenseRow(
                            expense = e,
                            onEdit = onEditExpense
                        )
                    }
                    if (visibleCount < filtered.size) {
                        item(key = "load_more_expense") {
                            UnifiedButton(
                                text = "📊 Cargar más (${filtered.size - visibleCount} restantes)",
                                onClick = { visibleCount = (visibleCount + pageSize).coerceAtMost(filtered.size) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = "Nuevo Gasto",
            primaryButtonOnClick = onAddExpense,
            primaryButtonIcon = Icons.Filled.Add,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                expenseToDelete = null
            },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar este gasto? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.let { expenseId ->
                            viewModel.deleteExpense(expenseId)
                        }
                        showDeleteDialog = false
                        expenseToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        expenseToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    onEdit: (String) -> Unit = {}
) {
    UnifiedCard(
        onClick = { onEdit(expense.id) },
        modifier = Modifier.fillMaxWidth()
    ) { 
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono de categoría compacto
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        when (expense.category) {
                            ExpenseCategory.OFFICE_SUPPLIES -> BrandColors.blueLilac.copy(alpha = 0.2f)
                            ExpenseCategory.MARKETING -> BrandColors.turquoise.copy(alpha = 0.2f)
                            ExpenseCategory.TRANSPORTATION -> BrandColors.turquoiseLight.copy(alpha = 0.2f)
                            ExpenseCategory.UTILITIES -> Color(0xFFFF9800).copy(alpha = 0.2f)
                            ExpenseCategory.MAINTENANCE -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            else -> Color(0xFF9E9E9E).copy(alpha = 0.2f)
                        },
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (expense.category) {
                        ExpenseCategory.OFFICE_SUPPLIES -> "📋"
                        ExpenseCategory.MARKETING -> "📢"
                        ExpenseCategory.TRANSPORTATION -> "✈️"
                        ExpenseCategory.UTILITIES -> "⚡"
                        ExpenseCategory.MAINTENANCE -> "🔧"
                        else -> "💸"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // Información principal compacta
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                // Información secundaria en una línea
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = expense.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = Formatters.formatDate(expense.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    
                    // Estado compacto
                    Text(
                        text = when (expense.status) {
                            ExpenseStatus.PENDING -> "⏳"
                            ExpenseStatus.PAID -> "✅"
                            ExpenseStatus.CANCELLED -> "❌"
                            ExpenseStatus.OVERDUE -> "⚠️"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Monto destacado
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = Formatters.formatClp(expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.turquoise
                )
            }
        } 
    }
    
}

/**
 * 🎠 SELECTOR DE CATEGORÍAS EN CARRUSEL
 * 
 * Componente visual que muestra las categorías de gastos en formato de mini tarjetas
 * organizadas en un carrusel horizontal scrollable.
 */
@Composable
fun ExpenseCategoryCarousel(
    selectedCategory: ExpenseCategory,
    onCategorySelected: (ExpenseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val categories = ExpenseCategory.values()
    
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { category ->
                ExpenseCategoryCard(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier.width(85.dp)
                )
            }
        }
    }
}

/**
 * 🎴 MINI TARJETA DE CATEGORÍA
 * 
 * Componente individual que representa una categoría en el carrusel.
 */
@Composable
private fun ExpenseCategoryCard(
    category: ExpenseCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(category.getColor()))
    
    Card(
        onClick = onClick,
        modifier = modifier
            .height(110.dp)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = color.copy(alpha = 0.3f)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) {
                color.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSelected) {
                        Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.08f),
                                    color.copy(alpha = 0.03f),
                                    MaterialTheme.colorScheme.surface
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Icono de categoría con efecto moderno
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = if (isSelected) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        color.copy(alpha = 0.25f),
                                        color.copy(alpha = 0.1f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                        .then(
                            if (isSelected) {
                                Modifier.shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    spotColor = color.copy(alpha = 0.4f)
                                )
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.getIcon(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                // Nombre de categoría (truncado si es muy largo)
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        color.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    onDone: () -> Unit,
    expenseId: String? = null,
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val existingExpense = remember(expenses, expenseId) { 
        expenses.firstOrNull { it.id == expenseId } 
    }
    
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var supplier by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Estado para diálogo de eliminación
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Cargar datos del gasto existente
    LaunchedEffect(existingExpense?.id) {
        if (existingExpense != null) {
            description = existingExpense.description
            amountText = existingExpense.amount.toString()
            category = existingExpense.category
            supplier = existingExpense.supplier ?: ""
            notes = existingExpense.notes ?: ""
        }
    }
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(AnimationTokens.longDuration, easing = AnimationTokens.decelerateEasing),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(AnimationTokens.extraLongDuration, easing = AnimationTokens.decelerateEasing),
        label = "fadeIn"
    )

    Box(modifier = modifier.fillMaxSize()) {
    Column(
            modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .offset(y = slideInOffset)
            .alpha(fadeInAlpha),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Información del Gasto
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
            ) {
                Text(
                    text = "💸 Información del Gasto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                UnifiedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    modifier = Modifier.fillMaxWidth()
                )
                
                UnifiedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Monto (CLP)",
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de categoría en formato carrusel
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "📂 Categoría",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                ExpenseCategoryCarousel(
                    selectedCategory = category,
                    onCategorySelected = { category = it },
                    modifier = Modifier.fillMaxWidth()
                )

                UnifiedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = "Proveedor (opcional)",
                    modifier = Modifier.fillMaxWidth()
                )
                
                UnifiedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notas",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Espaciado para el bottom bar
        Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Barra inferior fija con acciones
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // Barra de acciones principal
            FixedBottomBar(
                primaryButtonText = if (existingExpense != null) "Actualizar Gasto" else "Agregar Gasto",
                primaryButtonOnClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    
                    if (existingExpense != null) {
                        // Actualizar gasto existente
                        val updatedExpense = existingExpense.copy(
                            description = description.ifBlank { existingExpense.description },
                            amount = amount,
                            category = category,
                            supplier = supplier.ifBlank { null },
                            notes = notes.ifBlank { null }
                        )
                        viewModel.updateExpense(updatedExpense)
                    } else {
                        // Crear nuevo gasto
                        val expense = Expense(
                            id = UUID.randomUUID().toString(),
                            description = description.ifBlank { "Gasto" },
                            amount = amount,
                            category = category,
                            supplier = supplier.ifBlank { null },
                            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                            receiptNumber = null,
                            notes = notes.ifBlank { null },
                            status = ExpenseStatus.PENDING
                        )
                        viewModel.addExpense(expense)
                    }
                    onDone()
                },
                primaryButtonIcon = Icons.Filled.Save,
                secondaryButtonText = if (existingExpense != null) "Eliminar" else "Cancelar",
                secondaryButtonOnClick = {
                    if (existingExpense != null) {
                        showDeleteDialog = true
                    } else {
                        onDone()
                    }
                },
                secondaryButtonIcon = if (existingExpense != null) Icons.Filled.Delete else null,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Diálogo de confirmación de eliminación
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { 
                    Text(
                        text = "Eliminar Gasto",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = { 
                    Text("¿Estás seguro de que quieres eliminar este gasto? Esta acción no se puede deshacer y se recalcularán todos los totales relacionados.") 
                },
                confirmButton = {
                    Button(
                        onClick = {
                            existingExpense?.id?.let { id ->
                                viewModel.deleteExpense(id)
                            }
                            showDeleteDialog = false
                            onDone()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}



