package com.negociolisto.app.ui.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * 🎨 SISTEMA DE DISEÑO UNIFICADO
 * 
 * Este archivo contiene todas las constantes de diseño que deben usarse
 * consistentemente en toda la aplicación para mantener la identidad visual.
 */

// 🎨 PALETA DE COLORES UNIFICADA DEL BRANDING
object BrandColors {
    // Colores principales del branding
    val primary = Color(0xFF009FE3) // Azul marca principal
    val primaryVariant = Color(0xFF007FB6) // Azul más oscuro
    val secondary = Color(0xFF312783) // Índigo marca secundario
    val secondaryVariant = Color(0xFF241D62) // Índigo más oscuro
    
    // Colores neutros
    val white = Color(0xFFFEFEFE) // Blanco
    val lightBlue = Color(0xFFE3F2FD) // Azul muy claro
    val lightIndigo = Color(0xFFE8E5FF) // Índigo muy claro
    
    // Variaciones para gradientes y estados (mantener compatibilidad)
    val lightLilac = Color(0xFFE3F2FD) // Fondo azul claro (actualizado)
    val blueLilac = Color(0xFF009FE3) // Azul principal (actualizado)
    val turquoise = Color(0xFF312783) // Índigo secundario (actualizado)
    val lightLilacVariant = Color(0xFFE8E5FF) // Índigo claro (actualizado)
    val blueLilacLight = Color(0xFF42A5F5) // Azul claro para estados
    val turquoiseLight = Color(0xFF5E35B1) // Índigo claro para estados
}

// 📏 CONSTANTES DE DISEÑO UNIFICADAS
object DesignTokens {
    // Elevaciones UNIFICADAS
    val cardElevation = 4.dp // Estandarizado
    val buttonElevation = 2.dp
    val fabElevation = 6.dp
    val headerElevation = 2.dp
    
    // Formas UNIFICADAS
    val cardShape = RoundedCornerShape(16.dp) // ÚNICO para todas las tarjetas
    val buttonShape = RoundedCornerShape(12.dp) // ÚNICO para todos los botones
    val chipShape = RoundedCornerShape(20.dp)
    val fabShape = RoundedCornerShape(16.dp)
    val headerShape = RoundedCornerShape(12.dp) // Para headers con gradiente
    
    // Espaciado UNIFICADO
    val cardPadding = 16.dp // ÚNICO para todas las tarjetas
    val sectionSpacing = 16.dp // ÚNICO entre secciones
    val itemSpacing = 12.dp // ÚNICO entre elementos
    val smallSpacing = 8.dp
    val largeSpacing = 24.dp
    val extraLargeSpacing = 32.dp
    
    // Espaciado entre elementos en Column/Row
    val columnSpacing = 16.dp // Para verticalArrangement
    val rowSpacing = 12.dp // Para horizontalArrangement
    val compactSpacing = 4.dp // Para elementos muy cercanos
    val tightSpacing = 6.dp // Para elementos en chips/filtros
    val looseSpacing = 20.dp // Para elementos destacados
    
    // Tamaños de iconos UNIFICADOS
    val iconSize = 24.dp
    val smallIconSize = 18.dp
    val mediumIconSize = 20.dp // Para iconos en campos/botones
    val largeIconSize = 48.dp
    val extraLargeIconSize = 56.dp // Para FABs
    val fabIconSize = 24.dp
    
    // Tamaños de componentes UNIFICADOS
    val buttonHeight = 56.dp
    val fabSize = 56.dp
    val avatarSize = 100.dp // Estandarizado
    val smallAvatarSize = 40.dp
    
    // Bordes UNIFICADOS
    val borderWidth = 1.dp
    val thickBorderWidth = 2.dp
    
    /**
     * 📱 PADDING ADAPTATIVO BASADO EN TAMAÑO DE PANTALLA
     * 
     * Retorna padding que se adapta automáticamente según el ancho de pantalla disponible.
     * Los valores en DesignTokens se escalan automáticamente con LocalDensity,
     * por lo que funcionan correctamente con zoom del sistema y escalado manual.
     * 
     * @return PaddingValues adaptado al tamaño de pantalla:
     * - Pantallas pequeñas (< 360dp): 12.dp
     * - Pantallas medianas (360-600dp): 16.dp
     * - Pantallas grandes/tablets (> 600dp): 24.dp
     */
    @Composable
    fun adaptivePadding(): PaddingValues {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> PaddingValues(12.dp)
            screenWidth < 600 -> PaddingValues(16.dp)
            else -> PaddingValues(24.dp)
        }
    }
}

// 🎭 TIPOGRAFÍA UNIFICADA
// Nota: La tipografía se usa directamente en los componentes
// usando MaterialTheme.typography.headlineLarge, etc.

// 🎨 COLORES DEL TEMA (RESPETA MODO OSCURO)
// Nota: Los colores del tema se usan directamente en los componentes
// usando MaterialTheme.colorScheme.surface, etc.

// 🎯 GRADIENTES UNIFICADOS
object GradientTokens {
    // Gradiente secundario (índigo)
    fun secondaryGradient() = listOf(
        BrandColors.secondary,
        BrandColors.secondaryVariant
    )
    
    // Gradiente de marca (azul principal)
    fun brandGradient() = listOf(
        BrandColors.primary,
        BrandColors.primaryVariant
    )

    // Gradiente de marca más oscuro para encabezados destacados
    fun brandGradientDark() = listOf(
        BrandColors.primary.copy(alpha = 1f),
        BrandColors.primary.copy(alpha = 0.9f)
    )

    // 🌅 GRADIENTES DINÁMICOS PARA CAJA SORPRESA SEGÚN HORA DEL DÍA
    
    // Madrugada (0-6h): Azul oscuro a púrpura suave (simulando amanecer)
    fun dawnGradient() = listOf(
        Color(0xFF1A237E), // Índigo oscuro
        Color(0xFF3F51B5), // Índigo medio
        Color(0xFF5C6BC0)  // Índigo claro
    )
    
    // Mañana (6-12h): Amarillo dorado a naranja (energía matutina)
    fun morningGradient() = listOf(
        Color(0xFFFFB300), // Ámbar dorado
        Color(0xFFFF9800), // Naranja
        Color(0xFFFF7043)  // Naranja claro
    )
    
    // Tarde (12-18h): Azul cielo a turquesa (tarde activa)
    fun afternoonGradient() = listOf(
        Color(0xFF2196F3), // Azul cielo
        Color(0xFF00BCD4), // Cian
        Color(0xFF4DD0E1)  // Cian claro
    )
    
    // Noche (18-24h): Índigo oscuro a púrpura profundo (noche tranquila)
    fun nightGradient() = listOf(
        Color(0xFF0D47A1), // Azul muy oscuro
        Color(0xFF1A237E), // Índigo oscuro
        Color(0xFF512DA8)  // Púrpura profundo
    )
}

// 🎨 ESTADOS DE COMPONENTES
object ComponentStates {
    // Estados de botones
    val enabled = true
    val disabled = false
    val loading = true
    
    // Estados de tarjetas
    val selected = true
    val unselected = false
    val pressed = true
    val unpressed = false
}

// 📱 BREAKPOINTS RESPONSIVOS
object Breakpoints {
    val mobile = 0.dp
    val tablet = 600.dp
    val desktop = 840.dp
}

// 🎭 ANIMACIONES UNIFICADAS
object AnimationTokens {
    // Duración de animaciones
    val shortDuration = 200
    val mediumDuration = 300
    val longDuration = 500
    val extraLongDuration = 800
    
    // Easing curves
    val standardEasing = androidx.compose.animation.core.FastOutSlowInEasing
    val decelerateEasing = androidx.compose.animation.core.EaseOutCubic
    val accelerateEasing = androidx.compose.animation.core.EaseInCubic
}

// 🎨 SOMBRAS UNIFICADAS
object ShadowTokens {
    val none = 0.dp
    val small = 2.dp
    val medium = 4.dp
    val large = 6.dp
    val extraLarge = 8.dp
    val huge = 12.dp
}

