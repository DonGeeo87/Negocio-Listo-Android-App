package com.negociolisto.app.ui.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.negociolisto.app.domain.model.CustomCategory

/**
 * 🎠 SELECTOR DE CATEGORÍAS PERSONALIZADAS EN CARRUSEL
 * 
 * Componente visual que muestra las categorías personalizadas en formato de mini tarjetas
 * organizadas en un carrusel horizontal scrollable, similar al sistema usado en gastos.
 */
@Composable
fun CustomCategoryCarousel(
    categories: List<CustomCategory>,
    selectedCategory: CustomCategory?,
    onCategorySelected: (CustomCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Opción "Sin categoría"
            CustomCategoryCard(
                category = null,
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                modifier = Modifier.width(85.dp)
            )
            
            // Categorías personalizadas
            categories.sortedBy { it.sortOrder }.forEach { category ->
                CustomCategoryCard(
                    category = category,
                    isSelected = selectedCategory?.id == category.id,
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier.width(85.dp)
                )
            }
        }
    }
}

/**
 * 🎴 MINI TARJETA DE CATEGORÍA PERSONALIZADA
 * 
 * Componente individual que representa una categoría en el carrusel.
 */
@Composable
private fun CustomCategoryCard(
    category: CustomCategory?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Color de la categoría o color por defecto
    val color = if (category != null) {
        try {
            Color(android.graphics.Color.parseColor(category.color))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val displayName = category?.name ?: "Sin categoría"
    val displayIcon = category?.icon ?: "📦"
    
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
            containerColor = MaterialTheme.colorScheme.surface
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
                        text = displayIcon,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                // Nombre de categoría (truncado si es muy largo)
                Text(
                    text = displayName,
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

