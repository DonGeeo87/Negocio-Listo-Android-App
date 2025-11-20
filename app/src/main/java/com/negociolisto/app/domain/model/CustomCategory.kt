package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime
import java.util.UUID

/**
 * 📂 CATEGORÍA PERSONALIZADA DE PRODUCTOS
 * 
 * Modelo que representa una categoría personalizada creada por el usuario.
 * Permite que cada usuario adapte las categorías a su tipo de negocio.
 */
data class CustomCategory(
    /**
     * 🆔 ID ÚNICO DE LA CATEGORÍA
     * Identificador único que nunca cambia
     */
    val id: String = UUID.randomUUID().toString(),
    
    /**
     * 🏷️ NOMBRE DE LA CATEGORÍA
     * Nombre que ve el usuario
     * Ejemplo: "Electrodomésticos", "Ropa de Verano"
     */
    val name: String,
    
    /**
     * 🎨 ICONO DE LA CATEGORÍA
     * Emoji o símbolo que representa la categoría
     * Ejemplo: "🏠", "👕", "💻"
     */
    val icon: String,
    
    /**
     * 🌈 COLOR DE LA CATEGORÍA
     * Color en formato hex para la UI
     * Ejemplo: "#FF5722", "#2196F3"
     */
    val color: String,
    
    /**
     * 📝 DESCRIPCIÓN (OPCIONAL)
     * Descripción adicional de la categoría
     */
    val description: String? = null,
    
    /**
     * 📅 FECHA DE CREACIÓN
     * Cuándo se creó esta categoría
     */
    val createdAt: LocalDateTime,
    
    /**
     * 🔄 FECHA DE ÚLTIMA ACTUALIZACIÓN
     * Cuándo se modificó por última vez
     */
    val updatedAt: LocalDateTime,
    
    /**
     * ✅ SI ESTÁ ACTIVA
     * Para "eliminar" categorías sin borrar el historial
     */
    val isActive: Boolean = true,
    
    /**
     * 👤 ID DEL USUARIO
     * A qué usuario pertenece esta categoría
     */
    val userId: String,
    
    /**
     * 📊 ORDEN DE APARICIÓN
     * Para ordenar las categorías en la UI
     */
    val sortOrder: Int = 0
) {
    
    /**
     * 🎨 OBTENER COLOR COMO INT
     * 
     * Convierte el color hex a un entero para usar en Compose.
     */
    fun getColorInt(): Long {
        return color.removePrefix("#").toLong(16) or 0xFF000000
    }
    
    /**
     * 📊 VERIFICAR SI ES CATEGORÍA POPULAR
     * 
     * Algunas categorías son más comunes y se muestran primero.
     */
    fun isPopularCategory(): Boolean {
        return when (name.lowercase()) {
            "bebidas", "panadería", "lácteos", "abarrotes", "limpieza", "otros" -> true
            else -> false
        }
    }
    
    /**
     * 🔍 VALIDAR CATEGORÍA
     * 
     * Verifica que la categoría tenga datos válidos.
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               icon.isNotBlank() && 
               color.matches(Regex("#[0-9A-Fa-f]{6}")) &&
               userId.isNotBlank()
    }
}

/**
 * 🏭 FACTORY PARA CATEGORÍAS PREDEFINIDAS
 * 
 * Crea categorías por defecto basadas en el enum original.
 */
object CustomCategoryFactory {
    
    // Método eliminado: No se crean categorías predeterminadas
    // El usuario debe crear sus propias categorías
    
    /**
     * 🎨 CREAR CATEGORÍA PERSONALIZADA
     * 
     * Crea una nueva categoría con validación.
     */
    fun createCustomCategory(
        name: String,
        icon: String,
        color: String,
        description: String? = null,
        userId: String,
        createdAt: LocalDateTime
    ): CustomCategory? {
        val category = CustomCategory(
            name = name.trim(),
            icon = icon.trim(),
            color = color.trim(),
            description = description?.trim(),
            createdAt = createdAt,
            updatedAt = createdAt,
            userId = userId
        )
        
        return if (category.isValid()) category else null
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **CustomCategory**: Modelo de dominio para categorías personalizadas
 * 2. **Validación**: Verificación de datos antes de guardar
 * 3. **Factory Pattern**: Creación controlada de objetos
 * 4. **User-specific**: Cada usuario tiene sus propias categorías
 * 5. **Soft Delete**: Eliminación lógica con isActive
 * 
 * ANALOGÍA:
 * 
 * Una CustomCategory es como una "etiqueta personalizada" en una tienda:
 * 
 * 1. **Nombre**: Como el texto de la etiqueta
 * 2. **Icono**: Como el símbolo visual de la etiqueta
 * 3. **Color**: Como el color de fondo de la etiqueta
 * 4. **Usuario**: Como el dueño de la tienda que creó la etiqueta
 * 5. **Orden**: Como la posición en el estante
 * 
 * CASOS DE USO REALES:
 * - "Electrodomésticos" - 🏠 - #FF5722 - Para tienda de electrodomésticos
 * - "Ropa de Verano" - 👕 - #2196F3 - Para tienda de ropa
 * - "Herramientas" - 🔧 - #795548 - Para ferretería
 */
