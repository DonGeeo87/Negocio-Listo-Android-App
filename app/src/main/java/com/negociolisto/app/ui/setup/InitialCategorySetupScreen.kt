package com.negociolisto.app.ui.setup

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.data.local.UiPreferencesStore
import com.negociolisto.app.ui.components.UnifiedTopAppBar
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.components.UnifiedPrimaryButton
import com.negociolisto.app.ui.components.UnifiedTextButton
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.BrandColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 🏗️ PANTALLA DE CONFIGURACIÓN INICIAL DE CATEGORÍAS
 * 
 * Pantalla obligatoria que aparece después del onboarding para configurar
 * las categorías personalizadas del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialCategorySetupScreen(
    onComplete: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: InitialCategorySetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uiPrefs = remember { UiPreferencesStore(context) }
    val appScale by uiPrefs.appScale.collectAsStateWithLifecycle(initialValue = 1.0)
    var sliderScale by remember(appScale) { mutableStateOf(appScale) }
    var isApplyingScale by remember { mutableStateOf(false) }
    var showScaleSaved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandColors.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.onPrimary
                    )
                )
            )
    ) {
        // Header
        UnifiedTopAppBar(
            title = "Configuración Inicial",
            onNavigationClick = onBack,
            navigationIcon = if (onBack != null) Icons.AutoMirrored.Filled.ArrowBack else null
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Información
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = BrandColors.primary
                        )
                        Text(
                            text = "Configuración Obligatoria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.primary
                        )
                    }
                    Text(
                        text = if (categories.size >= 5) {
                            "¡Perfecto! Puedes agregar más categorías o continuar a la app."
                        } else {
                            "Para comenzar a usar la app, necesitas configurar al menos 5 categorías de productos personalizadas."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (categories.size >= 5) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            UIScaleQuickAdjustCard(
                sliderScale = sliderScale,
                onScaleChange = { sliderScale = it },
                onApply = {
                    scope.launch {
                        isApplyingScale = true
                        uiPrefs.setAppScale(sliderScale)
                        val sp = context.getSharedPreferences("ui_prefs_mirror", Context.MODE_PRIVATE)
                        sp.edit().putFloat("app_scale", sliderScale.toFloat()).apply()
                        isApplyingScale = false
                        showScaleSaved = true
                        delay(2000)
                        showScaleSaved = false
                    }
                },
                isApplying = isApplyingScale,
                showSuccess = showScaleSaved
            )
            
            // Indicador de progreso
            LinearProgressIndicator(
                progress = { (categories.size.coerceAtMost(5) / 5f) },
                modifier = Modifier.fillMaxWidth(),
                color = BrandColors.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Input de categorías
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Ingresa tus categorías",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Separa cada categoría con una coma. Ejemplo: Bebidas, Panadería, Carnes, Frutas, Verduras",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = uiState.categoriesInput,
                        onValueChange = viewModel::updateCategoriesInput,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Categorías separadas por coma") },
                        placeholder = { Text("Bebidas, Panadería, Carnes, Frutas, Verduras") },
                        isError = uiState.hasError,
                        supportingText = if (uiState.hasError) {
                            { Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error) }
                        } else {
                            { Text("Ingresa al menos 5 categorías") }
                        },
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
            
            // Preview de categorías
            if (categories.isNotEmpty()) {
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Vista previa de tus categorías",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                CategoryPreviewChip(
                                    name = category.name,
                                    icon = category.icon,
                                    colorHex = category.colorHex
                                )
                            }
                        }
                    }
                }
            }
            
            // Botón continuar
            UnifiedPrimaryButton(
                text = if (uiState.isLoading) "Guardando..." else "Continuar a la App",
                onClick = { viewModel.saveCategoriesAndContinue(onComplete) },
                modifier = Modifier.fillMaxWidth(),
                enabled = categories.size >= 5 && !uiState.isLoading,
                loading = uiState.isLoading,
                icon = if (!uiState.isLoading) Icons.Filled.Check else null
            )
            
            // Botón para saltar (solo si ya tiene algunas categorías)
            if (categories.size >= 3) {
                UnifiedTextButton(
                    text = "Saltar y configurar después",
                    onClick = { viewModel.skipSetup(onComplete) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Información adicional
            Text(
                text = "💡 Tip: Puedes modificar estas categorías más tarde desde la configuración de la app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 🏷️ CHIP DE PREVIEW DE CATEGORÍA
 */
@Composable
private fun CategoryPreviewChip(
    name: String,
    icon: String,
    colorHex: String
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        BrandColors.primary
    }
    
    Card(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun UIScaleQuickAdjustCard(
    sliderScale: Double,
    onScaleChange: (Double) -> Unit,
    onApply: () -> Unit,
    isApplying: Boolean,
    showSuccess: Boolean
) {
    val percentage = (sliderScale * 100).roundToInt()
    
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    tint = BrandColors.primary
                )
                Column {
                    Text(
                        text = "Ajusta el zoom de la app",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configura el tamaño inicial de la interfaz para que todo se vea perfecto en tu pantalla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "$percentage %",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Slider(
                value = sliderScale.toFloat(),
                onValueChange = { onScaleChange(it.toDouble()) },
                valueRange = 0.85f..1.2f,
                steps = 7
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Más compacto", style = MaterialTheme.typography.labelMedium)
                Text("Más amplio", style = MaterialTheme.typography.labelMedium)
            }
            
            UnifiedPrimaryButton(
                text = if (isApplying) "Aplicando..." else "Guardar zoom inicial",
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isApplying,
                loading = isApplying,
                icon = if (!isApplying) Icons.Filled.Check else null
            )
            
            if (showSuccess) {
                Text(
                    text = "✅ Zoom actualizado. Los cambios son inmediatos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

