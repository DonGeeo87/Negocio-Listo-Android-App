package com.negociolisto.app.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

/**
 * 📋 COMPONENTES DE LISTAS UNIFICADAS
 * 
 * Componentes específicos para mostrar listas de datos de manera consistente
 * en toda la aplicación.
 */

// 📋 LISTA VACÍA UNIFICADA
@Composable
fun EmptyListCard(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Filled.Inbox,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    DesignCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.largeSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(DesignTokens.largeIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            actionText?.let { text ->
                Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                Button(
                    onClick = { onActionClick?.invoke() },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(text = text)
                }
            }
        }
    }
}

// 📊 TARJETA DE ESTADÍSTICA
@Composable
fun StatisticCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    DesignCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(DesignTokens.largeIconSize),
                    tint = color.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// 🏷️ TARJETA DE ELEMENTO DE LISTA
@Composable
fun ListItemCard(
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    imageUrl: String? = null,
    placeholderIcon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    DesignCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen o icono
            when {
                !imageUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier
                            .size(DesignTokens.avatarSize)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                        error = painterResource(android.R.drawable.ic_menu_report_image)
                    )
                }
                placeholderIcon != null -> {
                    Box(
                        modifier = Modifier
                            .size(DesignTokens.avatarSize)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = placeholderIcon,
                            contentDescription = title,
                            modifier = Modifier.size(DesignTokens.iconSize),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(DesignTokens.itemSpacing))
            
            // Contenido principal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Texto final
            trailingText?.let { text ->
                Spacer(modifier = Modifier.width(DesignTokens.itemSpacing))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Indicador de selección
            if (isSelected) {
                Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Seleccionado",
                    modifier = Modifier.size(DesignTokens.iconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// 🎯 TARJETA DE ACCIÓN RÁPIDA
@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    DesignCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(DesignTokens.largeIconSize)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                color.copy(alpha = 0.2f),
                                color.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(DesignTokens.iconSize),
                    tint = color
                )
            }
            Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// 📊 GRID DE ESTADÍSTICAS
@Composable
fun StatisticsGrid(
    statistics: List<StatisticData>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
        contentPadding = PaddingValues(horizontal = DesignTokens.cardPadding)
    ) {
        items(statistics) { stat ->
            StatisticCard(
                title = stat.title,
                value = stat.value,
                subtitle = stat.subtitle,
                icon = stat.icon,
                color = stat.color,
                onClick = stat.onClick,
                modifier = Modifier.widthIn(min = 180.dp, max = 250.dp)
            )
        }
    }
}

// 📋 LISTA DE ACCIONES RÁPIDAS
@Composable
fun QuickActionsGrid(
    actions: List<QuickActionData>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing),
        contentPadding = PaddingValues(horizontal = DesignTokens.cardPadding)
    ) {
        items(actions) { action ->
            QuickActionCard(
                title = action.title,
                subtitle = action.subtitle,
                icon = action.icon,
                onClick = action.onClick,
                color = action.color,
                modifier = Modifier.widthIn(min = 130.dp, max = 200.dp)
            )
        }
    }
}

// 📊 DATOS PARA COMPONENTES
data class StatisticData(
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val color: androidx.compose.ui.graphics.Color = BrandColors.blueLilac,
    val onClick: (() -> Unit)? = null
)

data class QuickActionData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color = BrandColors.blueLilac,
    val onClick: () -> Unit
)
