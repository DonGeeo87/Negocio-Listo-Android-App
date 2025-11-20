package com.negociolisto.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 🔲 FORMAS DE NEGOCIO LISTO
 * 
 * Esta clase define todas las formas (bordes redondeados) que usamos
 * en nuestra aplicación. Las formas ayudan a crear una identidad visual
 * consistente y moderna.
 * 
 * ¿Por qué definir formas aquí?
 * - Consistencia visual en todos los componentes
 * - Identidad de marca reconocible
 * - Facilita cambios globales de estilo
 * - Mejora la experiencia de usuario con elementos amigables
 * - Sigue las mejores prácticas de Material Design
 */

/**
 * 🎨 FORMAS PRINCIPALES DE NEGOCIO LISTO
 * 
 * Basadas en Material Design 3 Shape Scale
 * Optimizadas para aplicaciones de negocio y finanzas
 */
val NegocioListoShapes = Shapes(
    
    /**
     * 🔸 EXTRA SMALL (4dp)
     * Para elementos muy pequeños: chips, badges, indicadores
     * Ejemplo: Badge de "Nuevo", indicador de stock bajo
     */
    extraSmall = RoundedCornerShape(4.dp),
    
    /**
     * 🔹 SMALL (8dp)
     * Para elementos pequeños: botones pequeños, tags
     * Ejemplo: Filtros, categorías, botones secundarios
     */
    small = RoundedCornerShape(8.dp),
    
    /**
     * 🔶 MEDIUM (12dp)
     * Para elementos medianos: botones principales, campos de texto
     * Ejemplo: Botones de "Iniciar Sesión", campos de formulario
     */
    medium = RoundedCornerShape(12.dp),
    
    /**
     * 🔷 LARGE (16dp)
     * Para elementos grandes: cards, contenedores principales
     * Ejemplo: Cards de productos, contenedores de información
     */
    large = RoundedCornerShape(16.dp),
    
    /**
     * 🔵 EXTRA LARGE (24dp)
     * Para elementos muy grandes: modales, sheets, pantallas completas
     * Ejemplo: Bottom sheets, diálogos importantes, pantallas de onboarding
     */
    extraLarge = RoundedCornerShape(24.dp)
)

// ==========================================
// FORMAS PERSONALIZADAS PARA NEGOCIO
// ==========================================

/**
 * 💳 FORMA PARA CARDS DE PRODUCTOS
 * Bordes redondeados específicos para mostrar productos
 */
val ProductCardShape = RoundedCornerShape(16.dp)

/**
 * 🎫 FORMA PARA CARDS DE MÉTRICAS
 * Bordes redondeados para mostrar estadísticas y KPIs
 */
val MetricCardShape = RoundedCornerShape(12.dp)

/**
 * 🔘 FORMA PARA BOTONES PRINCIPALES
 * Bordes redondeados para botones de acción primaria
 */
val PrimaryButtonShape = RoundedCornerShape(12.dp)

/**
 * ⚪ FORMA PARA BOTONES SECUNDARIOS
 * Bordes redondeados para botones de acción secundaria
 */
val SecondaryButtonShape = RoundedCornerShape(8.dp)

/**
 * 📝 FORMA PARA CAMPOS DE TEXTO
 * Bordes redondeados para inputs y formularios
 */
val TextFieldShape = RoundedCornerShape(8.dp)

/**
 * 🏷️ FORMA PARA CHIPS Y TAGS
 * Bordes muy redondeados para elementos tipo chip
 */
val ChipShape = RoundedCornerShape(16.dp)

/**
 * 📊 FORMA PARA CONTENEDORES DE GRÁFICOS
 * Bordes redondeados para elementos que contienen gráficos
 */
val ChartContainerShape = RoundedCornerShape(16.dp)

/**
 * 🔔 FORMA PARA NOTIFICACIONES
 * Bordes redondeados para alertas y notificaciones
 */
val NotificationShape = RoundedCornerShape(12.dp)

/**
 * 🖼️ FORMA PARA IMÁGENES DE PERFIL
 * Bordes circulares para fotos de perfil
 */
val ProfileImageShape = RoundedCornerShape(50) // Circular

/**
 * 📱 FORMA PARA MODALES Y SHEETS
 * Bordes redondeados solo en la parte superior para sheets
 */
val BottomSheetShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

/**
 * 🎯 FORMA PARA ELEMENTOS DE NAVEGACIÓN
 * Bordes redondeados para tabs y elementos de navegación
 */
val NavigationShape = RoundedCornerShape(8.dp)

/**
 * ⚠️ FORMA PARA ALERTAS Y WARNINGS
 * Bordes redondeados para mensajes de alerta
 */
val AlertShape = RoundedCornerShape(8.dp)

/**
 * 🎨 FORMA PARA ELEMENTOS DECORATIVOS
 * Bordes muy redondeados para elementos puramente visuales
 */
val DecorativeShape = RoundedCornerShape(20.dp)

// ==========================================
// FORMAS ESPECIALES
// ==========================================

/**
 * 🔲 FORMA CUADRADA
 * Sin bordes redondeados, para elementos que necesitan esquinas rectas
 */
val SquareShape = RoundedCornerShape(0.dp)

/**
 * ⭕ FORMA CIRCULAR
 * Completamente circular, para botones FAB, avatares, etc.
 */
val CircularShape = RoundedCornerShape(50) // Porcentaje para hacer círculo perfecto

/**
 * 🏷️ FORMA TIPO PILL
 * Muy redondeada, para elementos tipo píldora o badge
 */
val PillShape = RoundedCornerShape(50)

/**
 * 📐 FORMA ASIMÉTRICA PARA ELEMENTOS ESPECIALES
 * Bordes redondeados diferentes en cada esquina
 */
val AsymmetricShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 4.dp,
    bottomStart = 4.dp,
    bottomEnd = 16.dp
)

// ==========================================
// FORMAS POR TAMAÑO DE COMPONENTE
// ==========================================

/**
 * 🔸 FORMAS PARA COMPONENTES PEQUEÑOS
 */
object SmallComponentShapes {
    val button = RoundedCornerShape(6.dp)
    val card = RoundedCornerShape(8.dp)
    val chip = RoundedCornerShape(12.dp)
}

/**
 * 🔶 FORMAS PARA COMPONENTES MEDIANOS
 */
object MediumComponentShapes {
    val button = RoundedCornerShape(8.dp)
    val card = RoundedCornerShape(12.dp)
    val chip = RoundedCornerShape(16.dp)
}

/**
 * 🔷 FORMAS PARA COMPONENTES GRANDES
 */
object LargeComponentShapes {
    val button = RoundedCornerShape(12.dp)
    val card = RoundedCornerShape(16.dp)
    val chip = RoundedCornerShape(20.dp)
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. Shape System: Sistema de formas consistente y escalable
 * 2. Border Radius: Radio de los bordes redondeados
 * 3. Visual Hierarchy: Diferentes formas para diferentes niveles de importancia
 * 4. Brand Identity: Formas que reflejan la personalidad de la marca
 * 5. User Experience: Formas que mejoran la usabilidad y navegación
 * 6. Material Design: Siguiendo las mejores prácticas de diseño
 * 
 * ANALOGÍA:
 * 
 * Las formas son como los "estilos arquitectónicos" de los elementos:
 * 
 * 1. **Extra Small**: Como detalles decorativos pequeños
 * 2. **Small**: Como elementos funcionales básicos
 * 3. **Medium**: Como elementos principales de la estructura
 * 4. **Large**: Como elementos destacados y contenedores
 * 5. **Extra Large**: Como elementos monumentales o de entrada
 * 6. **Formas especiales**: Como elementos únicos con propósito específico
 * 
 * JERARQUÍA DE FORMAS:
 * 1. Más redondeado = Más amigable y accesible
 * 2. Menos redondeado = Más formal y profesional
 * 3. Circular = Elementos de acción o decorativos
 * 4. Cuadrado = Elementos técnicos o de datos
 * 5. Asimétrico = Elementos únicos o de marca
 * 
 * CASOS DE USO:
 * - ProductCardShape: Cards que muestran productos en listas
 * - PrimaryButtonShape: Botones principales como "Iniciar Sesión"
 * - TextFieldShape: Campos de entrada de texto en formularios
 * - ChipShape: Filtros, categorías, tags
 * - BottomSheetShape: Modales que aparecen desde abajo
 * - ProfileImageShape: Fotos de perfil de usuario
 * 
 * CONSISTENCIA VISUAL:
 * - Elementos similares usan formas similares
 * - La importancia se refleja en el tamaño del radio
 * - Los elementos interactivos tienen formas más amigables
 * - Los elementos de datos pueden ser más geométricos
 * 
 * ACCESIBILIDAD:
 * - Bordes redondeados mejoran la percepción de elementos tocables
 * - Formas consistentes ayudan a la navegación
 * - Diferentes formas ayudan a distinguir tipos de contenido
 */