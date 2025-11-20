package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * 📚 MODELO DE COLECCIÓN
 * 
 * Esta clase representa una colección de productos que creamos para mostrar
 * a clientes específicos. Es como un "catálogo personalizado" o "lista de productos"
 * que armamos pensando en las necesidades de ciertos clientes.
 * 
 * Las colecciones nos ayudan a:
 * - Crear catálogos personalizados por cliente
 * - Organizar productos por temporada o evento
 * - Facilitar las ventas mostrando productos relacionados
 * - Generar PDFs para compartir con clientes
 */
data class Collection(
    /**
     * 🆔 ID ÚNICO DE LA COLECCIÓN
     * Cada colección tiene un identificador único.
     * Nunca hay dos colecciones con el mismo ID.
     */
    val id: String,
    
    /**
     * 🏷️ NOMBRE DE LA COLECCIÓN
     * Un nombre descriptivo que identifica esta colección.
     * Ejemplos: "Ropa de Verano 2024", "Productos para Oficina", "Catálogo VIP"
     */
    val name: String,
    
    /**
     * 📝 DESCRIPCIÓN (OPCIONAL)
     * Una explicación más detallada de qué contiene esta colección
     * y para qué está pensada.
     */
    val description: String?,
    
    /**
     * 📦 LISTA DE PRODUCTOS EN LA COLECCIÓN
     * Todos los productos que incluimos en esta colección.
     * Cada item contiene el producto y información adicional.
     */
    val items: List<CollectionItem>,
    
    /**
     * 👥 CLIENTES ASOCIADOS
     * Lista de IDs de clientes para quienes creamos esta colección.
     * Una colección puede ser para varios clientes.
     */
    val associatedCustomerIds: List<String>,
    
    /**
     * 🔑 TOKENS DE ACCESO POR CLIENTE
     * Mapa de customerId -> token único para acceder al portal del cliente.
     * Cada cliente tiene su propio token para ver sus colecciones y pedidos.
     */
    val customerAccessTokens: Map<String, String> = emptyMap(),
    
    /**
     * 📅 FECHA DE CREACIÓN
     * Cuándo creamos esta colección por primera vez.
     */
    val createdAt: LocalDateTime,
    
    /**
     * 🔄 FECHA DE ÚLTIMA ACTUALIZACIÓN
     * Cuándo modificamos esta colección por última vez.
     */
    val updatedAt: LocalDateTime,
    
    /**
     * ✅ ESTADO DE LA COLECCIÓN
     * Si está activa, archivada, o en borrador.
     */
    val status: CollectionStatus,
    
    /**
     * 🎨 COLOR DE LA COLECCIÓN (OPCIONAL)
     * Un color para identificar visualmente esta colección.
     * Útil para organización y presentación.
     */
    val color: String?,
    
    /**
     * 💬 HABILITAR CHAT
     * Si está activado, los clientes pueden usar el chat en tiempo real
     * desde la mini-web para comunicarse con el negocio.
     */
    val enableChat: Boolean = true,
    
    /**
     * 🎨 TEMPLATE DE MINI-WEB
     * Estilo visual que se usará en la mini-web pública de esta colección.
     * Por defecto es MODERN.
     */
    val webTemplate: CollectionWebTemplate = CollectionWebTemplate.MODERN
) {
    
    /**
     * 📊 CALCULAR VALOR TOTAL DE LA COLECCIÓN
     * 
     * Suma los precios de venta de todos los productos en la colección.
     * Útil para mostrar el valor total del catálogo.
     * 
     * @param products Lista de productos para obtener precios actuales
     * @return El valor total de todos los productos en la colección
     */
    fun calculateTotalValue(products: List<Product>): Double {
        return items.sumOf { collectionItem ->
            val product = products.find { it.id == collectionItem.productId }
            product?.salePrice ?: 0.0
        }
    }
    
    /**
     * 📦 CONTAR PRODUCTOS ÚNICOS
     * 
     * Cuenta cuántos productos diferentes hay en la colección.
     * 
     * @return El número de productos únicos
     */
    fun getUniqueProductCount(): Int {
        return items.distinctBy { it.productId }.size
    }
    
    /**
     * 📂 OBTENER CATEGORÍAS REPRESENTADAS
     * 
     * Identifica todas las categorías de productos que están
     * representadas en esta colección.
     * 
     * @param products Lista de productos para obtener categorías
     * @return Lista de categorías únicas en la colección
     */
    fun getRepresentedCategories(products: List<Product>): List<String> {
        return items.mapNotNull { collectionItem ->
            products.find { it.id == collectionItem.productId }?.customCategoryId
        }.distinct()
    }
    
    /**
     * ⚠️ VERIFICAR PRODUCTOS CON STOCK BAJO
     * 
     * Identifica productos en la colección que tienen stock bajo.
     * Útil para alertar antes de compartir el catálogo.
     * 
     * @param products Lista de productos para verificar stock
     * @return Lista de productos con stock bajo
     */
    fun getLowStockProducts(products: List<Product>): List<Product> {
        return items.mapNotNull { collectionItem ->
            products.find { it.id == collectionItem.productId && it.hasLowStock() }
        }
    }
    
    /**
     * 👤 VERIFICAR SI ESTÁ ASOCIADA A UN CLIENTE
     * 
     * Verifica si esta colección fue creada para un cliente específico.
     * 
     * @param customerId El ID del cliente a verificar
     * @return true si la colección está asociada a este cliente
     */
    fun isAssociatedWithCustomer(customerId: String): Boolean {
        return associatedCustomerIds.contains(customerId)
    }
    
    /**
     * ✅ VERIFICAR SI LA COLECCIÓN ES VÁLIDA
     * 
     * Valida que la colección tenga datos consistentes:
     * - Nombre no vacío
     * - Al menos un producto
     * - No productos duplicados
     * 
     * @return true si la colección es válida
     */
    fun isValid(): Boolean {
        if (name.isBlank()) return false
        if (items.isEmpty()) return false
        
        // Verificar que no haya productos duplicados
        val uniqueProductIds = items.map { it.productId }.distinct()
        if (uniqueProductIds.size != items.size) return false
        
        return true
    }
}

/**
 * 📦 ITEM DE COLECCIÓN
 * 
 * Representa un producto específico dentro de una colección.
 * Incluye información adicional sobre cómo se presenta el producto.
 */
data class CollectionItem(
    /**
     * 🆔 ID DEL PRODUCTO
     * Referencia al producto en nuestro inventario.
     */
    val productId: String,
    
    /**
     * 📝 NOTAS ESPECÍFICAS (OPCIONAL)
     * Información adicional sobre este producto en esta colección.
     * Ejemplos: "Recomendado para oficinas", "Producto estrella", "Oferta especial"
     */
    val notes: String?,
    
    /**
     * 🔢 ORDEN DE PRESENTACIÓN
     * En qué posición mostrar este producto en la colección.
     * Permite organizar los productos en un orden específico.
     */
    val displayOrder: Int,
    
    /**
     * ⭐ PRODUCTO DESTACADO
     * Si este producto debe resaltarse en la presentación.
     * Los productos destacados aparecen más prominentemente.
     */
    val isFeatured: Boolean,
    
    /**
     * 💰 PRECIO ESPECIAL (OPCIONAL)
     * Si queremos ofrecer un precio diferente al normal para esta colección.
     * Puede ser null si usamos el precio normal.
     */
    val specialPrice: Double?
) {
    
    /**
     * 💸 CALCULAR DESCUENTO APLICADO
     * 
     * Si hay un precio especial, calcula cuánto descuento representa.
     * 
     * @param normalPrice El precio normal del producto
     * @return El monto de descuento, o 0 si no hay precio especial
     */
    fun calculateDiscount(normalPrice: Double): Double {
        return if (specialPrice != null && specialPrice < normalPrice) {
            normalPrice - specialPrice
        } else {
            0.0
        }
    }
    
    /**
     * 📈 CALCULAR PORCENTAJE DE DESCUENTO
     * 
     * Si hay un precio especial, calcula el porcentaje de descuento.
     * 
     * @param normalPrice El precio normal del producto
     * @return El porcentaje de descuento (0.0 a 100.0)
     */
    fun calculateDiscountPercentage(normalPrice: Double): Double {
        val discount = calculateDiscount(normalPrice)
        return if (normalPrice > 0) {
            (discount / normalPrice) * 100
        } else {
            0.0
        }
    }
}

/**
 * 🎨 TEMPLATES DE MINI-WEB
 * 
 * Enum que define los diferentes estilos/templates disponibles para la mini-web pública.
 */
enum class CollectionWebTemplate(val displayName: String, val description: String) {
    MODERN("Moderno", "Gradientes azul/morado, diseño contemporáneo con colores vibrantes"),
    CLASSIC("Clásico", "Bordes oscuros, fondo gris claro, diseño tradicional y elegante"),
    MINIMAL("Minimalista", "Fondo blanco, diseño limpio y minimalista, ideal para productos premium"),
    DARK("Oscuro", "Fondo oscuro (#1a1a1a), contraste elegante con acentos índigo"),
    COLORFUL("Colorido", "Gradientes multicolor animados, diseño alegre y vibrante");
    
    /**
     * 🎨 OBTENER COLOR PARA EL TEMPLATE
     * 
     * Colores que representan visualmente cada template y coinciden con los estilos de la mini-web.
     */
    fun getColor(): String {
        return when (this) {
            MODERN -> "#009FE3"      // Azul del gradiente MODERN (header)
            CLASSIC -> "#2c3e50"     // Gris oscuro del header CLASSIC
            MINIMAL -> "#333333"     // Gris oscuro para texto (contraste con fondo blanco)
            DARK -> "#6366f1"        // Índigo para acentos (contraste con fondo oscuro)
            COLORFUL -> "#10B981"    // Verde del gradiente COLORFUL
        }
    }
}

/**
 * ✅ ESTADOS DE COLECCIÓN
 * 
 * Enum que define los diferentes estados en que puede estar una colección.
 */
enum class CollectionStatus(val displayName: String) {
    DRAFT("Borrador"),         // Colección en construcción, no lista para compartir
    ACTIVE("Activa"),          // Colección lista y disponible para usar
    ARCHIVED("Archivada"),     // Colección antigua que ya no se usa activamente
    SHARED("Compartida");      // Colección que ya fue enviada a clientes
    
    /**
     * 🎨 OBTENER COLOR PARA EL ESTADO
     * 
     * Cada estado tiene un color para mostrar visualmente el estado.
     * 
     * @return Un código de color hexadecimal
     */
    fun getColor(): String {
        return when (this) {
            DRAFT -> "#FFC107"      // Amarillo - En construcción
            ACTIVE -> "#28A745"     // Verde - Lista para usar
            ARCHIVED -> "#6C757D"   // Gris - Archivada
            SHARED -> "#17A2B8"     // Azul - Compartida
        }
    }
}

/**
 * 📊 ESTADÍSTICAS DE COLECCIÓN
 * 
 * Data class que contiene estadísticas calculadas de una colección.
 * Útil para reportes y análisis.
 */
data class CollectionStats(
    val totalProducts: Int,
    val totalValue: Double,
    val averagePrice: Double,
    val categoriesCount: Int,
    val featuredProductsCount: Int,
    val productsWithSpecialPrice: Int,
    val lowStockProductsCount: Int
)

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. Composition: Collection contiene una lista de CollectionItems
 * 2. Many-to-Many Relationship: Una colección puede tener varios clientes
 * 3. Business Logic: Cálculos de valor, descuentos, y validaciones
 * 4. Status Management: Control del ciclo de vida de las colecciones
 * 5. Flexible Pricing: Precios especiales por colección
 * 
 * ANALOGÍA:
 * 
 * Una colección es como un "catálogo de productos" físico:
 * 
 * 1. **Collection**: Es todo el catálogo
 *    - Portada con nombre y descripción
 *    - Lista de productos organizados
 *    - Información de para quién está dirigido
 * 
 * 2. **CollectionItem**: Es cada página del catálogo
 *    - Foto y descripción del producto
 *    - Precio (normal o especial)
 *    - Notas adicionales ("¡Oferta limitada!")
 *    - Orden en que aparece
 * 
 * 3. **CollectionStatus**: Es el estado del catálogo
 *    - "Borrador" = Aún lo estamos armando
 *    - "Activo" = Listo para mostrar a clientes
 *    - "Compartido" = Ya lo enviamos por WhatsApp/email
 *    - "Archivado" = Catálogo de temporada pasada
 * 
 * CASOS DE USO REALES:
 * - "Catálogo Navideño 2024" para todos los clientes VIP
 * - "Productos de Oficina" específico para empresas
 * - "Ofertas del Mes" con precios especiales
 * - "Ropa de Temporada" organizada por tallas
 * 
 * REGLAS DE NEGOCIO IMPLEMENTADAS:
 * - Validación de productos únicos (no duplicados)
 * - Cálculo automático de valores y descuentos
 * - Identificación de productos con stock bajo
 * - Organización por orden de presentación
 * - Asociación flexible con múltiples clientes
 */