package com.negociolisto.app.ui.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.negociolisto.app.ui.components.UnifiedListTopAppBar
import com.negociolisto.app.ui.components.UnifiedGradientHeaderCard
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.GradientTokens

// 🎨 CONSTANTES DE DISEÑO
private object UIScaleDesign {
    val cardElevation = 6.dp
    val cardShape = RoundedCornerShape(16.dp)
    val buttonShape = RoundedCornerShape(12.dp)
    val cardPadding = 20.dp
    val sectionSpacing = 16.dp
    val iconSize = 24.dp
}

/**
 * 📏 PANTALLA DE CONFIGURACIÓN DE ESCALA DE INTERFAZ
 * 
 * Permite al usuario ajustar el tamaño de la interfaz de usuario
 * para mejorar la experiencia según sus preferencias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UIScaleSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    // UI Scale preferences
    val uiPrefs = remember { com.negociolisto.app.data.local.UiPreferencesStore(context) }
    val appScale by uiPrefs.appScale.collectAsStateWithLifecycle(initialValue = 1.0)
    var sliderScale by remember(appScale) { mutableStateOf(appScale) }
    
    // Estados para feedback
    var showSuccessMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "fadeIn"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .offset(y = slideInOffset)
                .alpha(fadeInAlpha),
            contentPadding = PaddingValues(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.columnSpacing)
        ) {
            // Header
            item {
                UnifiedGradientHeaderCard(
                    title = "📏 Escala de la Interfaz",
                    subtitle = "Ajusta el tamaño de los elementos de la UI",
                    modifier = Modifier.fillMaxWidth()
                ) {}
            }
            
            // Control de escala
            item {
                ScaleControlCard(
                    sliderScale = sliderScale,
                    onScaleChange = { sliderScale = it },
                    onApplyScale = {
                        scope.launch {
                            uiPrefs.setAppScale(sliderScale)
                            // También guardar en SharedPreferences para compatibilidad
                            val sp = context.getSharedPreferences("ui_prefs_mirror", android.content.Context.MODE_PRIVATE)
                            sp.edit().putFloat("app_scale", sliderScale.toFloat()).apply()
                            
                            showSuccessMessage = true
                            delay(2000)
                            showSuccessMessage = false
                        }
                    }
                )
            }
            
            // Vista previa
            item {
                ScalePreviewCard(currentScale = sliderScale)
            }
            
            // Información adicional
            item {
                ScaleTipsCard()
            }
            
            // Espacio final
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // Snackbar para mostrar mensaje de éxito
    if (showSuccessMessage) {
        LaunchedEffect(showSuccessMessage) {
            // Mostrar toast
            android.widget.Toast.makeText(
                context, 
                "✅ Escala aplicada correctamente", 
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}

/**
 * 🎛️ TARJETA DE CONTROL DE ESCALA
 */
@Composable
private fun ScaleControlCard(
    sliderScale: Double,
    onScaleChange: (Double) -> Unit,
    onApplyScale: () -> Unit
) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(DesignTokens.iconSize)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    BrandColors.blueLilac.copy(alpha = 0.3f),
                                    BrandColors.blueLilac.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = BrandColors.blueLilac,
                        modifier = Modifier.size(DesignTokens.smallIconSize)
                    )
                }
                Spacer(modifier = Modifier.width(DesignTokens.itemSpacing))
                Text(
                    text = "Control de Escala",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
            
            // Slider de escala con gradiente vibrante
            Slider(
                value = sliderScale.toFloat(),
                onValueChange = { onScaleChange(it.toDouble()) },
                valueRange = 0.85f..1.15f,
                steps = 6, // 7 valores: 0.85, 0.90, 0.95, 1.00, 1.05, 1.10, 1.15
                colors = SliderDefaults.colors(
                    thumbColor = BrandColors.blueLilac,
                    activeTrackColor = BrandColors.blueLilac,
                    inactiveTrackColor = BrandColors.blueLilac.copy(alpha = 0.3f)
                )
            )
            
            // Valores de escala
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0.85x",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                GradientTokens.brandGradient()
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = String.format("%.2fx", sliderScale),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "1.15x",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
            
            // Botón aplicar con gradiente
            Button(
                onClick = onApplyScale,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            GradientTokens.brandGradient()
                        ),
                        shape = DesignTokens.buttonShape
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = DesignTokens.buttonShape
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(DesignTokens.smallIconSize)
                )
                Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
                Text(
                    text = "Aplicar Escala",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * 👁️ TARJETA DE VISTA PREVIA
 */
@Composable
private fun ScalePreviewCard(currentScale: Double) {
    UnifiedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.cardPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(DesignTokens.iconSize)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    BrandColors.turquoise.copy(alpha = 0.3f),
                                    BrandColors.turquoise.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = BrandColors.turquoise,
                        modifier = Modifier.size(DesignTokens.smallIconSize)
                    )
                }
                Spacer(modifier = Modifier.width(DesignTokens.itemSpacing))
                Text(
                    text = "Vista Previa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
            
            // Ejemplo visual mejorado con gradiente
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    BrandColors.blueLilac.copy(alpha = 0.1f),
                                    BrandColors.turquoise.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(DesignTokens.cardPadding)
                ) {
                    Text(
                        text = "Ejemplo de interfaz",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
                    
                    Text(
                        text = "Este es un ejemplo de cómo se verá la interfaz con la escala ${String.format("%.2fx", currentScale)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.smallSpacing)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.blueLilac
                            ),
                            shape = DesignTokens.buttonShape
                        ) {
                            Text(
                                text = "Botón",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = DesignTokens.buttonShape
                        ) {
                            Text(
                                text = "Botón",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "La escala afecta el tamaño de todos los elementos de la interfaz, incluyendo texto, botones, iconos y espaciado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 💡 TARJETA DE CONSEJOS
 */
@Composable
private fun ScaleTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = UIScaleDesign.cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = UIScaleDesign.cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(UIScaleDesign.cardPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = BrandColors.secondary,
                    modifier = Modifier.size(UIScaleDesign.iconSize)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Consejos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 🔍 DETECTAR ZOOM DEL SISTEMA
            val hasSystemZoom = LocalDensity.current.fontScale > 1.0f
            val systemZoomFactor = LocalDensity.current.fontScale
            
            // Mostrar información sobre zoom del sistema si está activado
            if (hasSystemZoom) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zoom del sistema detectado (${String.format("%.1f", systemZoomFactor)}x). " +
                                    "La aplicación se ajusta automáticamente para mantener las proporciones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            val tips = listOf(
                "• Usa 0.85x si tienes una pantalla grande y quieres ver más contenido",
                "• Usa 1.00x para el tamaño estándar recomendado",
                "• Usa 1.15x si tienes dificultades para leer texto pequeño",
                "• Los cambios se aplican inmediatamente al guardar"
            )
            
            tips.forEach { tip ->
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

