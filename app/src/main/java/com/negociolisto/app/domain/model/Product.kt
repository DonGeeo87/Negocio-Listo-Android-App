package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime
import java.util.UUID

/**
 * 📦 MODELO DE PRODUCTO
 * 
 * Esta clase representa un producto en nuestro inventario.
 * Es como una "ficha de producto" que contiene toda la información
 * necesaria para gestionar el inventario del negocio.
 * 
 * Un producto incluye:
 * - Información básica (nombre, descripción, categoría)
 * - Información comercial (precios, SKU, proveedor)
 * - Control de inventario (stock actual, stock mínimo)
 * - Metadatos (fechas, foto)
 */
data class Product(
    /**
     * 🆔 ID ÚNICO DEL PRODUCTO
     * Identificador único que nunca cambia
     */
    val id: String = UUID.randomUUID().toString(),
    
    /**
     * 🏷️ NOMBRE DEL PRODUCTO
     * Nombre comercial que ven los clientes
     * Ejemplo: "Camiseta Polo Azul Talla M"
     */
    val name: String,
    
    /**
     * 📝 DESCRIPCIÓN DETALLADA (OPCIONAL)
     * Información adicional sobre el producto
     * Ejemplo: "Camiseta 100% algodón, manga corta, cuello polo"
     */
    val description: String?,
    
    /**
     * 🔢 SKU (Stock Keeping Unit)
     * Código único para identificar el producto internamente
     * Ejemplo: "CAM-POL-AZU-M-001"
     */
    val sku: String,
    
    /**
     * 💰 PRECIO DE COMPRA
     * Cuánto nos cuesta el producto (precio al que lo compramos)
     */
    val purchasePrice: Double,
    
    /**
     * 💵 PRECIO DE VENTA
     * Cuánto le cobramos al cliente
     */
    val salePrice: Double,
    
    /**
     * 📦 CANTIDAD EN STOCK
     * Cuántas unidades tenemos disponibles
     */
    val stockQuantity: Int,
    
    /**
     * ⚠️ STOCK MÍNIMO
     * Cantidad mínima antes de mostrar alerta de stock bajo
     * Por defecto es 5 según los requisitos
     */
    val minimumStock: Int = 5,
    
    /**
     * 🏷️ CATEGORÍA PERSONALIZADA
     * ID de la categoría personalizada creada por el usuario
     */
    val customCategoryId: String,
    
    /**
     * 🏪 PROVEEDOR (OPCIONAL)
     * De quién compramos este producto
     */
    val supplier: String?,
    
    /**
     * 🖼️ URL DE LA FOTO (OPCIONAL)
     * Ruta a la imagen del producto en Firebase Storage
     */
    val photoUrl: String?,
    
    /**
     * 🖼️ URL DEL THUMBNAIL (OPCIONAL)
     * Ruta a la miniatura optimizada para listas
     */
    val thumbnailUrl: String? = null,
    
    /**
     * ☁️ URL DE RESPALDO EN GOOGLE DRIVE (OPCIONAL)
     * Enlace público a la imagen respaldada en Google Drive
     */
    val imageBackupUrl: String? = null,
    
    /**
     * 📅 FECHA DE CREACIÓN
     * Cuándo agregamos este producto al inventario
     */
    val createdAt: LocalDateTime,
    
    /**
     * 🔄 FECHA DE ÚLTIMA ACTUALIZACIÓN
     * Cuándo se modificó por última vez
     */
    val updatedAt: LocalDateTime,
    
    /**
     * ✅ SI ESTÁ ACTIVO
     * Para "eliminar" productos sin borrar el historial
     */
    val isActive: Boolean = true
) {
    
    /**
     * 💰 CALCULAR MARGEN DE GANANCIA
     * 
     * Calcula cuánto ganamos por cada unidad vendida.
     * 
     * @return El margen de ganancia en pesos
     */
    fun getProfit(): Double {
        return salePrice - purchasePrice
    }
    
    /**
     * 📊 CALCULAR PORCENTAJE DE MARGEN
     * 
     * Calcula el porcentaje de ganancia sobre el precio de compra.
     * 
     * @return El porcentaje de margen (ejemplo: 50.0 para 50%)
     */
    fun getProfitMarginPercentage(): Double {
        return if (purchasePrice > 0) {
            ((salePrice - purchasePrice) / purchasePrice) * 100
        } else {
            0.0
        }
    }
    
    /**
     * ⚠️ VERIFICAR SI TIENE STOCK BAJO
     * 
     * Determina si el producto necesita reposición.
     * 
     * @return true si el stock actual es menor o igual al mínimo
     */
    fun hasLowStock(): Boolean {
        return stockQuantity <= minimumStock
    }
    
    /**
     * 📦 VERIFICAR SI HAY STOCK DISPONIBLE
     * 
     * Determina si podemos vender este producto.
     * 
     * @param quantity Cantidad que queremos vender
     * @return true si hay suficiente stock
     */
    fun hasAvailableStock(quantity: Int = 1): Boolean {
        return stockQuantity >= quantity && isActive
    }
    
    /**
     * 💰 CALCULAR VALOR TOTAL DEL INVENTARIO
     * 
     * Calcula cuánto vale todo el stock de este producto.
     * 
     * @return El valor total basado en el precio de compra
     */
    fun getTotalInventoryValue(): Double {
        return stockQuantity * purchasePrice
    }
    
    /**
     * 💵 CALCULAR VALOR POTENCIAL DE VENTA
     * 
     * Calcula cuánto podríamos ganar si vendemos todo el stock.
     * 
     * @return El valor total basado en el precio de venta
     */
    fun getTotalSaleValue(): Double {
        return stockQuantity * salePrice
    }
    
    /**
     * ✅ VALIDAR PRODUCTO
     * 
     * Verifica que el producto tenga datos válidos.
     * 
     * @return Lista de errores encontrados (vacía si es válido)
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("El nombre del producto es obligatorio")
        }
        
        if (sku.isBlank()) {
            errors.add("El SKU es obligatorio")
        }
        
        if (purchasePrice < 0) {
            errors.add("El precio de compra no puede ser negativo")
        }
        
        if (salePrice < 0) {
            errors.add("El precio de venta no puede ser negativo")
        }
        
        if (salePrice < purchasePrice) {
            errors.add("El precio de venta debería ser mayor al precio de compra")
        }
        
        if (stockQuantity < 0) {
            errors.add("La cantidad en stock no puede ser negativa")
        }
        
        if (minimumStock < 0) {
            errors.add("El stock mínimo no puede ser negativo")
        }
        
        return errors
    }
    
    /**
     * 🔍 COINCIDE CON BÚSQUEDA
     * 
     * Verifica si el producto coincide con un término de búsqueda.
     * 
     * @param query Término de búsqueda
     * @return true si coincide con nombre, SKU o categoría
     */
    fun matchesSearch(query: String): Boolean {
        val searchTerm = query.lowercase().trim()
        return name.lowercase().contains(searchTerm) ||
               sku.lowercase().contains(searchTerm) ||
               customCategoryId.lowercase().contains(searchTerm) ||
               (supplier?.lowercase()?.contains(searchTerm) == true)
    }
}



/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Domain Model**: Modelo que representa la lógica de negocio
 * 2. **Value Objects**: Objetos inmutables que representan valores
 * 3. **Business Rules**: Reglas de negocio implementadas en métodos
 * 4. **Validation**: Validación de datos en el dominio
 * 5. **Enum Classes**: Para valores predefinidos y controlados
 * 
 * ANALOGÍA:
 * 
 * Un Product es como una "ficha de producto" en una tienda física:
 * 
 * 1. **Información básica**: Como la etiqueta del producto
 * 2. **Precios**: Como los stickers de precio
 * 3. **Stock**: Como contar físicamente los productos
 * 4. **Categoría**: Como los pasillos de la tienda
 * 5. **SKU**: Como el código de barras
 * 6. **Validaciones**: Como revisar que todo esté correcto
 * 
 * REGLAS DE NEGOCIO IMPLEMENTADAS:
 * - Stock mínimo de 5 unidades para alertas
 * - Validación de precios (venta > compra)
 * - Cálculo automático de márgenes
 * - Búsqueda por múltiples campos
 * - Categorización flexible pero controlada
 * 
 * CASOS DE USO REALES:
 * - "Camiseta Polo Azul M" - SKU: "CAM-001" - Stock: 15
 * - "iPhone 15 Pro" - SKU: "IPH-15P" - Stock: 3 (¡Stock bajo!)
 * - "Café Chileno 500g" - SKU: "CAF-CHL-500" - Stock: 25
 */