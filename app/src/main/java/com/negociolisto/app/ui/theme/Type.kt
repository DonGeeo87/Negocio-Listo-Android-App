package com.negociolisto.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 📝 TIPOGRAFÍA DE NEGOCIO LISTO
 * 
 * Define todos los estilos de texto que usamos en la app.
 * Es como tener un "manual de estilo" para que todo el texto
 * se vea consistente y profesional.
 * 
 * Material Design 3 define diferentes categorías de texto:
 * - Display: Títulos muy grandes
 * - Headline: Títulos de sección
 * - Title: Títulos de contenido
 * - Body: Texto normal
 * - Label: Etiquetas y botones
 */
val Typography = Typography(
    // 🏆 DISPLAY - Para títulos principales muy grandes
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    
    // 📰 HEADLINE - Para títulos de sección
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    
    // 📋 TITLE - Para títulos de contenido
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    
    // 📄 BODY - Para texto normal de contenido
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    
    // 🏷️ LABEL - Para etiquetas y botones
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Typography Scale**: Escala de tamaños consistente
 * 2. **Line Height**: Espaciado entre líneas para legibilidad
 * 3. **Letter Spacing**: Espaciado entre caracteres
 * 4. **Font Weight**: Grosor de la fuente (Normal, Medium, Bold)
 * 5. **Semantic Naming**: Nombres que describen el uso, no la apariencia
 * 
 * CUÁNDO USAR CADA ESTILO:
 * 
 * 📱 **Display**: 
 * - Pantalla de bienvenida
 * - Números grandes (ventas totales)
 * - Títulos de onboarding
 * 
 * 📰 **Headline**:
 * - Títulos de pantalla ("Mis Ventas")
 * - Nombres de secciones
 * - Títulos de cards importantes
 * 
 * 📋 **Title**:
 * - Títulos de listas
 * - Nombres de productos
 * - Títulos de formularios
 * 
 * 📄 **Body**:
 * - Descripciones
 * - Contenido de cards
 * - Texto de ayuda
 * 
 * 🏷️ **Label**:
 * - Texto de botones
 * - Etiquetas de campos
 * - Texto de navegación
 * 
 * EJEMPLOS EN NEGOCIO LISTO:
 * - displayLarge: "$1,250,000" (ventas del mes)
 * - headlineMedium: "Ventas de Hoy"
 * - titleLarge: "Producto Estrella"
 * - bodyMedium: "Descripción del producto..."
 * - labelMedium: "AGREGAR AL CARRITO"
 * 
 * BUENAS PRÁCTICAS:
 * - Usar máximo 3-4 tamaños diferentes por pantalla
 * - Mantener jerarquía visual clara
 * - Asegurar legibilidad en diferentes tamaños de pantalla
 * - Probar con texto largo y corto
 * - Considerar accesibilidad (tamaños mínimos)
 */