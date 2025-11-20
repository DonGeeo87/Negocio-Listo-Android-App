package com.negociolisto.app.ui.invoices.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 📄 ESTADO VACÍO DE FACTURAS
 * 
 * Componente que se muestra cuando no hay facturas registradas
 * o cuando los filtros no devuelven resultados. Proporciona orientación
 * clara al usuario sobre qué hacer a continuación.
 * 
 * Es como una "guía amigable" que ayuda al usuario cuando no encuentra
 * facturas o cuando está empezando a generar facturas.
 */
@Composable
fun EmptyInvoiceState(
    hasFilters: Boolean,
    onConfigureInvoiceClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (hasFilters) {
        // Estado: Sin resultados por filtros
        NoResultsInvoiceState(
            onClearFilters = onClearFilters,
            onConfigureClick = onConfigureInvoiceClick,
            modifier = modifier
        )
    } else {
        // Estado: Sin facturas registradas
        EmptyInvoicesState(
            onConfigureClick = onConfigureInvoiceClick,
            modifier = modifier
        )
    }
}

/**
 * 🔍 ESTADO SIN RESULTADOS DE FACTURAS
 * 
 * Se muestra cuando hay filtros activos pero no hay resultados.
 */
@Composable
private fun NoResultsInvoiceState(
    onClearFilters: () -> Unit,
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Ícono de búsqueda sin resultados
        Card(
            modifier = Modifier.size(120.dp),
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
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        
        // Título
        Text(
            text = "No se encontraron facturas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        // Descripción
        Text(
            text = "No hay facturas que coincidan con los filtros seleccionados. Intenta ajustar los criterios de búsqueda o crear nuevas facturas.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
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
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Limpiar Filtros")
            }
            
            // Botón secundario: Configurar factura
            OutlinedButton(
                onClick = onConfigureClick,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Configurar Factura")
            }
        }
    }
}

/**
 * 📄 ESTADO VACÍO DE FACTURAS
 * 
 * Se muestra cuando no hay facturas registradas en el sistema.
 */
@Composable
private fun EmptyInvoicesState(
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Área visual principal con círculo de fondo
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Círculo de fondo suave
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            // Ícono principal de facturas
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Título principal
        Text(
            text = "¡Tu lista de facturas está vacía!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Descripción motivacional
        Text(
            text = "Comienza configurando tu plantilla de factura para generar documentos profesionales y llevar un control completo de tus ventas.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3
        )
        
        // Card de beneficios con diseño mejorado
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Beneficios de las facturas:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                BenefitItem(
                    icon = "📄",
                    text = "Documentos profesionales y legales"
                )
                
                BenefitItem(
                    icon = "💰",
                    text = "Control total de ingresos y ventas"
                )
                
                BenefitItem(
                    icon = "📊",
                    text = "Reportes automáticos de facturación"
                )
                
                BenefitItem(
                    icon = "📱",
                    text = "Acceso desde cualquier dispositivo"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Botón principal con diseño mejorado
        Button(
            onClick = onConfigureClick,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Configurar primera factura",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 🎯 ITEM DE BENEFICIO
 * 
 * Componente para mostrar cada beneficio de manera visual.
 */
@Composable
private fun BenefitItem(
    icon: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 🔄 ESTADO DE CARGA DE FACTURAS
 * 
 * Se muestra mientras se cargan las facturas por primera vez.
 */
@Composable
fun LoadingInvoiceState(
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
            text = "Cargando facturas...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ❌ ESTADO DE ERROR DE FACTURAS
 * 
 * Se muestra cuando hay un error cargando las facturas.
 */
@Composable
fun ErrorInvoiceState(
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
            text = "Error al cargar facturas",
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
 * Se muestra temporalmente después de crear la primera factura.
 */
@Composable
fun FirstInvoiceCreatedState(
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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Has configurado tu primera factura. Ahora puedes generar documentos profesionales para tus ventas.",
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
 * Este componente maneja todos los estados vacíos relacionados con facturas:
 * 
 * FUNCIONALIDADES:
 * 1. **Estado vacío inicial**: Bienvenida motivacional con beneficios
 * 2. **Sin resultados**: Guía para ajustar filtros o configurar facturas
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

