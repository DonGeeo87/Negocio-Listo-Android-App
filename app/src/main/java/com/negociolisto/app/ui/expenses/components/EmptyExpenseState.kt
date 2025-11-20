package com.negociolisto.app.ui.expenses.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.design.DesignTokens

/**
 * 💸 ESTADO VACÍO DE GASTOS
 * 
 * Componente que se muestra cuando no hay gastos registrados
 * o cuando los filtros no devuelven resultados. Proporciona orientación
 * clara al usuario sobre qué hacer a continuación.
 * 
 * Es como una "guía amigable" que ayuda al usuario cuando no encuentra
 * gastos o cuando está empezando a registrar sus gastos.
 */
@Composable
fun EmptyExpenseState(
    hasFilters: Boolean,
    onAddExpenseClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (hasFilters) {
        // Estado: Sin resultados por filtros
        NoResultsExpenseState(
            onClearFilters = onClearFilters,
            onAddClick = onAddExpenseClick,
            modifier = modifier
        )
    } else {
        // Estado: Sin gastos registrados
        EmptyExpensesState(
            onAddClick = onAddExpenseClick,
            modifier = modifier
        )
    }
}

/**
 * 🔍 ESTADO SIN RESULTADOS DE GASTOS
 * 
 * Se muestra cuando hay filtros activos pero no hay resultados.
 */
@Composable
private fun NoResultsExpenseState(
    onClearFilters: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(DesignTokens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
    ) {
        // Ícono de búsqueda sin resultados (reducido de 120dp a 60dp)
        Card(
            modifier = Modifier.size(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = "Sin resultados",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        
        // Título
        Text(
            text = "No se encontraron gastos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        // Descripción
        Text(
            text = "No hay gastos que coincidan con los filtros seleccionados. Intenta ajustar los criterios de búsqueda o agregar nuevos gastos.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
        
        // Acciones
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
        ) {
            // Botón principal: Limpiar filtros
            Button(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Limpiar filtros",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                Text(
                    "Limpiar Filtros",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Botón secundario: Agregar gasto
            OutlinedButton(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar gasto",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                Text(
                    "Agregar Gasto",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 💸 ESTADO VACÍO DE GASTOS
 * 
 * Se muestra cuando no hay gastos registrados en el sistema.
 */
@Composable
private fun EmptyExpensesState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(DesignTokens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
    ) {
        // Área visual principal con círculo de fondo (reducido de 160dp a 80dp)
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Círculo de fondo suave
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            // Ícono principal de gastos (reducido de 80dp a 40dp)
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "Agregar gasto",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Título principal
        Text(
            text = "¡Tu lista de gastos está vacía!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Descripción motivacional
        Text(
            text = "Comienza registrando tus gastos empresariales para tener un control financiero completo y optimizar los costos de tu negocio.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
        
        // Botón principal con diseño mejorado
        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(DesignTokens.buttonHeight),
            shape = DesignTokens.buttonShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar gasto",
                modifier = Modifier.size(DesignTokens.mediumIconSize)
            )
            Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
            Text(
                text = "Agregar primer gasto",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


/**
 * 🔄 ESTADO DE CARGA DE GASTOS
 * 
 * Se muestra mientras se cargan los gastos por primera vez.
 */
@Composable
fun LoadingExpenseState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Cargando gastos...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ❌ ESTADO DE ERROR DE GASTOS
 * 
 * Se muestra cuando hay un error cargando los gastos.
 */
@Composable
fun ErrorExpenseState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error al cargar gastos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

/**
 * 🎉 ESTADO DE ÉXITO
 * 
 * Se muestra temporalmente después de agregar el primer gasto.
 */
@Composable
fun FirstExpenseAddedState(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "¡Excelente!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Has registrado tu primer gasto. Ahora puedes llevar un control detallado de tus finanzas empresariales.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Continuar")
        }
    }
}

/**
 * 📋 DOCUMENTACIÓN DEL COMPONENTE
 * 
 * Este componente maneja todos los estados vacíos relacionados con gastos:
 * 
 * FUNCIONALIDADES:
 * 1. **Estado vacío inicial**: Bienvenida motivacional con beneficios
 * 2. **Sin resultados**: Guía para ajustar filtros o agregar gastos
 * 3. **Cargando**: Indicador de progreso informativo
 * 4. **Error**: Recuperación con botón de reintento
 * 5. **Éxito**: Celebración de logros iniciales
 * 
 * TIPOS DE ESTADOS VACÍOS:
 * ✅ **Primera vez**: Bienvenida motivacional con beneficios
 * ✅ **Sin resultados**: Guía para ajustar filtros
 * ✅ **Cargando**: Indicador de progreso informativo
 * ✅ **Error**: Recuperación con botón de reintento
 * ✅ **Éxito**: Celebración de logros iniciales
 * 
 * ELEMENTOS DE DISEÑO:
 * - **Iconos grandes**: Comunicación visual clara
 * - **Títulos motivacionales**: Lenguaje positivo y alentador
 * - **Descripciones útiles**: Explicaciones claras de qué hacer
 * - **Acciones prominentes**: Botones que guían al siguiente paso
 * - **Beneficios listados**: Valor claro de usar la aplicación
 * 
 * PRINCIPIOS UX:
 * - **Nunca dejar al usuario perdido**: Siempre hay una acción clara
 * - **Motivar en lugar de frustrar**: Lenguaje positivo y alentador
 * - **Educar sobre beneficios**: Mostrar el valor de la aplicación
 * - **Facilitar la recuperación**: Botones claros para solucionar problemas
 * - **Celebrar los logros**: Reconocer el progreso del usuario
 */
