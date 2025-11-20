package com.negociolisto.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 🎨 COLORES PARA TEMA CLARO - UNIFICADO CON DESIGN SYSTEM
 * 
 * Colores basados en la identidad visual del logo:
 * - Azul/Lila: Color principal de marca
 * - Turquesa: Color secundario complementario
 * - Lila claro: Fondos y contenedores
 */
private val LightColorScheme = lightColorScheme(
    // Colores principales basados en el branding unificado
    primary = androidx.compose.ui.graphics.Color(0xFF009FE3), // NLPrimary - Azul marca
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFEFEFE), // Blanco
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFE3F2FD), // Azul muy claro
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF009FE3),
    
    // Colores secundarios
    secondary = androidx.compose.ui.graphics.Color(0xFF312783), // NLSecondary - Índigo marca
    onSecondary = androidx.compose.ui.graphics.Color(0xFFFEFEFE),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFE8E5FF), // Índigo muy claro
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF312783),
    
    // Acento para llamadas a la acción
    tertiary = androidx.compose.ui.graphics.Color(0xFF007FB6), // NLPrimaryVariant
    onTertiary = androidx.compose.ui.graphics.Color(0xFFFEFEFE),
    
    // Estados de error
    error = androidx.compose.ui.graphics.Color(0xFFE53E3E),
    onError = androidx.compose.ui.graphics.Color(0xFFFEFEFE),
    
    // Fondos y superficies
    background = androidx.compose.ui.graphics.Color(0xFFFEFEFE), // BrandColors.white
    onBackground = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    
    surface = androidx.compose.ui.graphics.Color(0xFFF0F0FF), // BrandColors.lightLilacVariant
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
)

/**
 * 🌙 COLORES PARA TEMA OSCURO - UNIFICADO CON DESIGN SYSTEM
 * 
 * Versión nocturna adaptada de los colores de marca
 */
private val DarkColorScheme = darkColorScheme(
    // Colores principales adaptados para tema oscuro
    primary = androidx.compose.ui.graphics.Color(0xFF42A5F5), // Azul más claro para tema oscuro
    onPrimary = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF009FE3).copy(alpha = 0.2f),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF42A5F5),
    
    // Colores secundarios para tema oscuro
    secondary = androidx.compose.ui.graphics.Color(0xFF5E35B1), // Índigo más claro para tema oscuro
    onSecondary = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF312783).copy(alpha = 0.2f),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF5E35B1),
    
    // Acento para tema oscuro
    tertiary = androidx.compose.ui.graphics.Color(0xFF007FB6), // NLPrimaryVariant
    onTertiary = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    
    // Estados de error para tema oscuro
    error = androidx.compose.ui.graphics.Color(0xFFEF5350),
    onError = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    
    // Fondos y superficies para tema oscuro
    background = androidx.compose.ui.graphics.Color(0xFF121212),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    
    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
)

/**
 * 🎨 TEMA PRINCIPAL DE NEGOCIO LISTO
 * 
 * Esta función aplica nuestro tema personalizado a toda la app.
 * 
 * @param darkTheme Si usar tema oscuro o claro
 * @param dynamicColor Si usar colores dinámicos del sistema (Android 12+)
 * @param content El contenido de la app que usará este tema
 */
@Composable
fun NegocioListoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Los colores dinámicos están disponibles en Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Usar colores dinámicos si están disponibles y habilitados
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        
        // Usar nuestros colores personalizados
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Configurar la barra de estado para que coincida con nuestro tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Preferimos surface para mejor contraste de iconos
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Aplicar el tema a todo el contenido
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Material Design 3**: Sistema de diseño moderno de Google
 * 2. **Color Scheme**: Paleta de colores consistente en toda la app
 * 3. **Dynamic Colors**: Colores que se adaptan al wallpaper del usuario
 * 4. **Dark Theme**: Tema oscuro para mejor experiencia nocturna
 * 5. **Status Bar**: Configuración de la barra superior del teléfono
 * 
 * COLORES Y SU SIGNIFICADO EN NEGOCIOS:
 * 
 * 🟢 **Verde**: Dinero, crecimiento, éxito, naturaleza
 * 🔵 **Azul**: Confianza, profesionalismo, estabilidad
 * 🟠 **Naranja**: Energía, creatividad, llamadas a la acción
 * 🔴 **Rojo**: Urgencia, errores, alertas importantes
 * ⚫ **Gris**: Elegancia, neutralidad, información secundaria
 * 
 * BUENAS PRÁCTICAS:
 * - Usar colores consistentes en toda la app
 * - Asegurar buen contraste para accesibilidad
 * - Probar tanto en tema claro como oscuro
 * - Considerar daltonismo en la elección de colores
 * - Usar colores que reflejen la identidad de la marca
 */