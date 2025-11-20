package com.negociolisto.app.ui.inventory.components

import androidx.compose.foundation.layout.*
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
import com.negociolisto.app.ui.components.UnifiedEmptyState

/**
 * 📦 ESTADO VACÍO DEL INVENTARIO
 * 
 * Componente que se muestra cuando no hay productos en el inventario
 * o cuando los filtros no devuelven resultados. Proporciona orientación
 * clara al usuario sobre qué hacer a continuación.
 * 
 * Es como una "guía amigable" que ayuda al usuario cuando no encuentra
 * lo que busca o cuando está empezando.
 */
@Composable
fun EmptyInventoryState(
    hasFilters: Boolean,
    onAddProductClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (hasFilters) {
        // Estado: Sin resultados por filtros
        UnifiedEmptyState(
            title = "No se encontraron productos",
            message = "Intenta ajustar los filtros o agregar un nuevo producto",
            icon = "🔍",
            actionText = "Limpiar filtros",
            onActionClick = onClearFilters,
            modifier = modifier
        )
    } else {
        // Estado: Inventario completamente vacío
        UnifiedEmptyState(
            title = "¡Tu inventario está vacío!",
            message = "Comienza agregando tus productos para organizar tu negocio y hacer seguimiento de tu inventario.",
            icon = "📦",
            actionText = "Agregar producto",
            onActionClick = onAddProductClick,
            modifier = modifier
        )
    }
}

/**
 * 🔍 ESTADO SIN RESULTADOS
 * 
 * Se muestra cuando hay filtros activos pero no hay resultados.
 */
@Composable
private fun NoResultsState(
    onClearFilters: () -> Unit,
    onAddProductClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ícono de búsqueda sin resultados
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        
        // Título
        Text(
            text = "No se encontraron productos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        // Descripción
        Text(
            text = "No hay productos que coincidan con los filtros seleccionados. Intenta ajustar los criterios de búsqueda o agregar nuevos productos.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Acciones
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón principal: Limpiar filtros
            Button(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Limpiar filtros",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Botón secundario: Agregar producto
            OutlinedButton(
                onClick = onAddProductClick,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Agregar producto",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 🎯 ESTADO PRIMERA VEZ
 * 
 * Se muestra cuando el inventario está completamente vacío.
 */
@Composable
private fun EmptyInventoryFirstTime(
    onAddProductClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Ícono de inventario vacío
        Card(
            modifier = Modifier.size(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Título de bienvenida
        Text(
            text = "¡Bienvenido a tu inventario!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Descripción motivacional
        Text(
            text = "Aquí podrás gestionar todos tus productos de manera fácil y eficiente. Comienza agregando tu primer producto para empezar a organizar tu negocio.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
        )
        
        // Lista de beneficios
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Con NegocioListo podrás:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                BenefitItem(
                    icon = Icons.Default.Add,
                    text = "Agregar productos con fotos y detalles"
                )
                
                BenefitItem(
                    icon = Icons.Default.Search,
                    text = "Buscar y filtrar tu inventario fácilmente"
                )
                
                BenefitItem(
                    icon = Icons.Filled.Warning,
                    text = "Recibir alertas de stock bajo automáticamente"
                )
                
                BenefitItem(
                    icon = Icons.Default.Analytics,
                    text = "Ver estadísticas y reportes de tu negocio"
                )
            }
        }
        
        // Botón de acción principal
        Button(
            onClick = onAddProductClick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Agregar mi primer producto",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * ✅ ITEM DE BENEFICIO
 * 
 * Componente para mostrar un beneficio con ícono.
 */
@Composable
private fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 🔄 ESTADO DE CARGA VACÍO MODERNO
 * 
 * Se muestra mientras se cargan los productos por primera vez.
 */
@Composable
fun LoadingInventoryState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Usar el skeleton screen para inventario
        com.negociolisto.app.ui.components.SkeletonProductList(
            itemCount = 6,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * ❌ ESTADO DE ERROR
 * 
 * Se muestra cuando hay un error cargando el inventario.
 */
@Composable
fun ErrorInventoryState(
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
            text = "Error al cargar inventario",
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
 * Se muestra temporalmente después de agregar el primer producto.
 */
@Composable
fun FirstProductAddedState(
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
        
        Text(
            text = "Has agregado tu primer producto exitosamente. Tu inventario está listo para crecer.",
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
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Empty States**: Estados vacíos informativos y útiles
 * 2. **User Onboarding**: Guía para usuarios nuevos
 * 3. **Error Recovery**: Recuperación de errores con acciones claras
 * 4. **Progressive Disclosure**: Información gradual según el contexto
 * 5. **Motivational Design**: Diseño que motiva la acción
 * 
 * ANALOGÍA:
 * 
 * EmptyInventoryState es como un "asistente de tienda amigable":
 * 
 * 1. **Primera visita**: Te da la bienvenida y explica los beneficios
 * 2. **Búsqueda sin resultados**: Te sugiere ajustar los criterios
 * 3. **Error técnico**: Te ayuda a solucionarlo con pasos claros
 * 4. **Éxito inicial**: Te felicita y te motiva a continuar
 * 5. **Carga**: Te mantiene informado del progreso
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