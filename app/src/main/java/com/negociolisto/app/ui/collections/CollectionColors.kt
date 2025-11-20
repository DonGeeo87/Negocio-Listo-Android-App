package com.negociolisto.app.ui.collections

import androidx.compose.ui.graphics.Color

/**
 * 🎨 PALETA DE COLORES PARA COLECCIONES
 * 
 * Define una paleta vibrante de colores para hacer las cabeceras de las colecciones
 * más atractivas y distinguibles visualmente.
 */
object CollectionColors {
    
    /**
     * 🎨 PALETA DE COLORES VARIADOS
     * 
     * Lista de colores atractivos y modernos para las cabeceras de colecciones.
     * Cada color está pensado para tener buen contraste con texto blanco.
     */
    val palette = listOf(
        // Azules y cianes
        Color(0xFF009FE3), // Azul principal del branding
        Color(0xFF1976D2), // Azul Material
        Color(0xFF0288D1), // Azul claro
        Color(0xFF00ACC1), // Cian
        Color(0xFF0097A7), // Cian oscuro
        Color(0xFF00BCD4), // Cian brillante
        
        // Índigos y púrpuras
        Color(0xFF312783), // Índigo secundario del branding
        Color(0xFF3F51B5), // Índigo Material
        Color(0xFF5E35B1), // Púrpura claro
        Color(0xFF7B1FA2), // Púrpura medio
        Color(0xFF9C27B0), // Púrpura Material
        Color(0xFF673AB7), // Púrpura profundo
        
        // Verdes
        Color(0xFF10B981), // Verde moderno
        Color(0xFF00C853), // Verde Material
        Color(0xFF009688), // Verde azulado
        Color(0xFF00897B), // Verde oscuro
        Color(0xFF4CAF50), // Verde Material
        Color(0xFF2E7D32), // Verde profundo
        
        // Naranjas y corales
        Color(0xFFFF9800), // Naranja Material
        Color(0xFFFF6F00), // Naranja oscuro
        Color(0xFFFF5722), // Naranja rojizo
        Color(0xFFE64A19), // Naranja profundo
        Color(0xFFFF7043), // Coral claro
        Color(0xFFFF5252), // Rojo claro
        
        // Rosas y magentas
        Color(0xFFE91E63), // Rosa Material
        Color(0xFFC2185B), // Rosa oscuro
        Color(0xFFEC4899), // Rosa moderno
        Color(0xFFAD1457), // Rosa profundo
        Color(0xFFF06292), // Rosa claro
        Color(0xFFF50057), // Rosa brillante
        
        // Teales y turquesas
        Color(0xFF00E5FF), // Cian brillante
        Color(0xFF00B8D4), // Teal claro
        Color(0xFF00838F), // Teal oscuro
        Color(0xFF26A69A), // Teal Material
        Color(0xFF00695C), // Teal profundo
        Color(0xFF00796B), // Teal medio
    )
    
    /**
     * 🎲 OBTENER COLOR POR ID
     * 
     * Asigna un color de la paleta basándose en el ID de la colección.
     * Esto garantiza que cada colección tenga un color consistente.
     * 
     * @param collectionId ID único de la colección
     * @return Color de la paleta
     */
    fun getColorById(collectionId: String): Color {
        // Usar el hash del ID para seleccionar un color de manera determinística
        val index = collectionId.hashCode().mod(palette.size)
        return palette[if (index < 0) -index else index]
    }
    
    /**
     * 🎨 OBTENER COLOR PERSONALIZADO O AUTOMÁTICO
     * 
     * Si la colección tiene un color personalizado (hex string), lo convierte a Color.
     * Si no, asigna uno automáticamente basándose en el ID.
     * 
     * @param collectionId ID único de la colección
     * @param customColor Color personalizado en formato hexadecimal (opcional)
     * @return Color a usar
     */
    fun getColor(collectionId: String, customColor: String?): Color {
        if (customColor != null && customColor.isNotBlank()) {
            try {
                // Intentar parsear el color hexadecimal
                val colorInt = android.graphics.Color.parseColor(customColor)
                return Color(colorInt)
            } catch (e: Exception) {
                // Si falla el parsing, usar color automático
                return getColorById(collectionId)
            }
        }
        return getColorById(collectionId)
    }
    
    /**
     * 🌈 OBTENER GRADIENTE PARA CABECERA
     * 
     * Crea un gradiente atractivo usando el color base y una variación más oscura.
     * 
     * @param baseColor Color base
     * @return Lista de colores para el gradiente
     */
    fun getGradientColors(baseColor: Color): List<Color> {
        return listOf(
            baseColor,
            baseColor.copy(alpha = 0.85f)
        )
    }
    
    /**
     * 🎨 OBTENER COLORES PARA ESTADO
     * 
     * Obtiene colores específicos para diferentes estados de colección,
     * manteniendo el color de la colección como base pero ajustándolo según el estado.
     * 
     * @param baseColor Color base de la colección
     * @param status Estado de la colección
     * @return Lista de colores para el gradiente ajustado al estado
     */
    fun getColorsForStatus(baseColor: Color, status: com.negociolisto.app.domain.model.CollectionStatus): List<Color> {
        return when (status) {
            com.negociolisto.app.domain.model.CollectionStatus.DRAFT -> {
                // Para borradores, usar tono más apagado
                listOf(
                    baseColor.copy(alpha = 0.7f),
                    baseColor.copy(alpha = 0.5f)
                )
            }
            com.negociolisto.app.domain.model.CollectionStatus.ARCHIVED -> {
                // Para archivadas, usar tono grisáceo
                listOf(
                    Color(0xFF9E9E9E),
                    Color(0xFF757575)
                )
            }
            else -> {
                // Para activas y compartidas, usar el color completo
                getGradientColors(baseColor)
            }
        }
    }
}
