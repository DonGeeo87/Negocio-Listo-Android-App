package com.negociolisto.app.ui.collections.components

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
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.components.UnifiedPrimaryButton
import com.negociolisto.app.ui.components.UnifiedSecondaryButton

/**
 * 🎁 ESTADO VACÍO DE COLECCIONES
 * 
 * Componente que se muestra cuando no hay colecciones registradas
 * o cuando los filtros no devuelven resultados. Enfocado en el propósito
 * de crear ofertas exclusivas para clientes específicos.
 * 
 * Es como una "guía estratégica" que ayuda al usuario a entender el valor
 * de crear colecciones personalizadas para sus clientes VIP.
 */
@Composable
fun EmptyCollectionState(
    hasFilters: Boolean,
    onAddCollectionClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (hasFilters) {
        // Estado: Sin resultados por filtros
        NoResultsCollectionState(
            onClearFilters = onClearFilters,
            onAddClick = onAddCollectionClick,
            modifier = modifier
        )
    } else {
        // Estado: Sin colecciones registradas
        EmptyCollectionsState(
            onAddClick = onAddCollectionClick,
            modifier = modifier
        )
    }
}

/**
 * 🔍 ESTADO SIN RESULTADOS DE COLECCIONES
 * 
 * Se muestra cuando hay filtros activos pero no hay resultados.
 */
@Composable
private fun NoResultsCollectionState(
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
            text = "No se encontraron colecciones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        // Descripción
        Text(
            text = "No hay colecciones que coincidan con los filtros seleccionados. Intenta ajustar los criterios de búsqueda o crear nuevas colecciones exclusivas.",
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
            UnifiedPrimaryButton(
                text = "Limpiar Filtros",
                onClick = onClearFilters,
                icon = Icons.Default.Clear,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            
            // Botón secundario: Crear colección
            UnifiedSecondaryButton(
                text = "Crear Colección",
                onClick = onAddClick,
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}

/**
 * 🎁 ESTADO VACÍO DE COLECCIONES
 * 
 * Se muestra cuando no hay colecciones registradas en el sistema.
 * Enfocado en el propósito de crear ofertas exclusivas para clientes.
 */
@Composable
private fun EmptyCollectionsState(
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
            // Círculo de fondo suave con gradiente
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            // Ícono principal de colecciones (gift/regalo) - reducido de 80dp a 40dp
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = "Crear colección",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Título principal enfocado en exclusividad
        Text(
            text = "¡Crea ofertas exclusivas para tus clientes!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Descripción motivacional enfocada en clientes VIP
        Text(
            text = "Diseña colecciones personalizadas para clientes específicos y crea ofertas únicas que aumenten las ventas y fidelicen a tus mejores clientes.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
        
        // Botón principal con diseño mejorado
        UnifiedPrimaryButton(
            text = "Crear primera colección",
            onClick = onAddClick,
            icon = Icons.Default.CardGiftcard,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}


/**
 * 🔄 ESTADO DE CARGA DE COLECCIONES
 * 
 * Se muestra mientras se cargan las colecciones por primera vez.
 */
@Composable
fun LoadingCollectionState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        
        Text(
            text = "Cargando colecciones...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ❌ ESTADO DE ERROR DE COLECCIONES
 * 
 * Se muestra cuando hay un error cargando las colecciones.
 */
@Composable
fun ErrorCollectionState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(DesignTokens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = "Error",
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = "Error al cargar colecciones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        UnifiedPrimaryButton(
            text = "Reintentar",
            onClick = onRetry,
            icon = Icons.Default.Refresh,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

/**
 * 🎉 ESTADO DE ÉXITO
 * 
 * Se muestra temporalmente después de crear la primera colección.
 */
@Composable
fun FirstCollectionCreatedState(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(DesignTokens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Éxito",
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "¡Excelente estrategia!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Has creado tu primera colección exclusiva. Ahora puedes personalizar ofertas para tus clientes VIP y aumentar tus ventas.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        UnifiedPrimaryButton(
            text = "Continuar",
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

/**
 * 📋 DOCUMENTACIÓN DEL COMPONENTE
 * 
 * Este componente maneja todos los estados vacíos relacionados con colecciones,
 * enfocado específicamente en el propósito de crear ofertas exclusivas para clientes:
 * 
 * FUNCIONALIDADES:
 * 1. **Estado vacío inicial**: Enfoque en ofertas exclusivas y clientes VIP
 * 2. **Sin resultados**: Guía para ajustar filtros o crear colecciones
 * 3. **Cargando**: Indicador de progreso informativo
 * 4. **Error**: Recuperación con botón de reintento
 * 5. **Éxito**: Celebración de estrategias de venta exitosas
 * 
 * TIPOS DE ESTADOS VACÍOS:
 * ✅ **Primera vez**: Enfoque en exclusividad y clientes VIP
 * ✅ **Sin resultados**: Guía para ajustar filtros
 * ✅ **Cargando**: Indicador de progreso informativo
 * ✅ **Error**: Recuperación con botón de reintento
 * ✅ **Éxito**: Celebración de estrategias de venta
 * 
 * ELEMENTOS DE DISEÑO:
 * - **Iconos estratégicos**: CardGiftcard para representar ofertas exclusivas
 * - **Títulos motivacionales**: Enfoque en exclusividad y clientes VIP
 * - **Descripciones estratégicas**: Beneficios de negocio claros
 * - **Acciones prominentes**: Botones que guían hacia la creación de valor
 * - **Beneficios listados**: Valor comercial específico para colecciones
 * 
 * PRINCIPIOS UX:
 * - **Enfoque en valor comercial**: Siempre destacar beneficios de negocio
 * - **Motivar estrategias de venta**: Lenguaje orientado a resultados
 * - **Educar sobre exclusividad**: Mostrar el valor de las ofertas VIP
 * - **Facilitar la segmentación**: Guiar hacia la personalización de clientes
 * - **Celebrar estrategias exitosas**: Reconocer el impacto comercial
 */

