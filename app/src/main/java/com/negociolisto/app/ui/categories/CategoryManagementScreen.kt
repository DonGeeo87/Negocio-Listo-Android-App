package com.negociolisto.app.ui.categories

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import com.negociolisto.app.domain.model.CustomCategory
import com.negociolisto.app.ui.categories.components.SimpleCategoryList
import com.negociolisto.app.ui.categories.components.CategoryCounter
import com.negociolisto.app.ui.components.UnifiedGradientHeaderCard
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.UnifiedButton
import com.negociolisto.app.ui.components.FixedBottomBar
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.AnimationTokens
import com.negociolisto.app.ui.design.ShadowTokens
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 📂 PANTALLA DE GESTIÓN DE CATEGORÍAS
 * 
 * Permite al usuario crear, editar y eliminar sus categorías personalizadas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    comeFromProductScreen: Boolean = false,
    onCategorySelected: (CustomCategory) -> Unit = {},
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
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
                .padding(bottom = 80.dp)
                .offset(y = slideInOffset)
                .alpha(fadeInAlpha)
        ) {
            // Contenido principal
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (categories.isEmpty()) {
                EmptyCategoriesState(
                    onAddCategory = { viewModel.showAddCategoryDialog() }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = DesignTokens.cardPadding, vertical = DesignTokens.smallSpacing),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
                ) {
                    // Header
                    UnifiedGradientHeaderCard(
                        title = "📂 Gestión de Categorías",
                        subtitle = "Administra tus categorías personalizadas",
                        modifier = Modifier.fillMaxWidth()
                    ) {}
                    
                    // Contador de categorías
                    CategoryCounter(
                        totalCategories = categories.size,
                        activeCategories = categories.count { it.isActive },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Lista simple de categorías (ordenadas alfabéticamente)
                    SimpleCategoryList(
                        categories = categories,
                        onEditCategory = { category ->
                            viewModel.showEditCategoryDialog(category)
                        },
                        onDeleteCategory = { category ->
                            viewModel.deleteCategory(category.id)
                        },
                        onCategorySelected = onCategorySelected,
                        allowSelection = comeFromProductScreen,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Mostrar error si existe
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.cardPadding),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Barra inferior fija
        FixedBottomBar(
            primaryButtonText = "➕ Nueva Categoría",
            primaryButtonOnClick = { viewModel.showAddCategoryDialog() },
            primaryButtonIcon = Icons.Filled.Add,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    // Diálogos
    AddEditCategoryDialog(
        isVisible = viewModel.showAddDialog.collectAsStateWithLifecycle().value,
        category = null,
        onDismiss = { viewModel.hideAddCategoryDialog() },
        onSave = { name, icon, color, description ->
            viewModel.addCategory(name, icon, color, description)
        }
    )
    
    AddEditCategoryDialog(
        isVisible = viewModel.showEditDialog.collectAsStateWithLifecycle().value,
        category = viewModel.editingCategory.collectAsStateWithLifecycle().value,
        onDismiss = { viewModel.hideEditCategoryDialog() },
        onSave = { name, icon, color, description ->
            viewModel.updateCategory(name, icon, color, description)
        }
    )
}

/**
 * 📋 TARJETA DE CATEGORÍA
 */
@Composable
private fun CategoryCard(
    category: CustomCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(DesignTokens.cardElevation, DesignTokens.cardShape),
        shape = DesignTokens.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icono de la categoría
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(category.getColorInt()).copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.icon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                
                // Información de la categoría
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    category.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(DesignTokens.mediumIconSize)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            DesignTokens.buttonShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(DesignTokens.mediumIconSize)
                    )
                }
            }
        }
    }
}

/**
 * 📭 ESTADO VACÍO DE CATEGORÍAS
 */
@Composable
private fun EmptyCategoriesState(
    onAddCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignTokens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📂",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No tienes categorías personalizadas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Crea categorías personalizadas para organizar mejor tus productos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
        
        UnifiedButton(
            text = "➕ Crear Primera Categoría",
            onClick = onAddCategory,
            icon = Icons.Filled.Add,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
