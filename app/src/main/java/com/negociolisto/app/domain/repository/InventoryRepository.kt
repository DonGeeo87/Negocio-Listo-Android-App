package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.StockMovement
import com.negociolisto.app.domain.model.StockMovementSummary
import kotlinx.coroutines.flow.Flow

/**
 * 📦 REPOSITORIO DE INVENTARIO
 * 
 * Esta interfaz define todas las operaciones que podemos hacer
 * con el inventario de productos. Es como un "contrato" que
 * especifica qué funcionalidades debe tener nuestro sistema
 * de gestión de inventario.
 * 
 * El repositorio actúa como una "capa de abstracción" entre
 * la lógica de negocio y el almacenamiento de datos.
 */
interface InventoryRepository {
    
    // ==========================================
    // OPERACIONES BÁSICAS DE PRODUCTOS
    // ==========================================
    
    /**
     * 📋 OBTENER TODOS LOS PRODUCTOS
     * 
     * Devuelve una lista de todos los productos activos en el inventario.
     * 
     * @return Flow que emite la lista actualizada de productos
     */
    fun getAllProducts(): Flow<List<Product>>
    
    /**
     * 🔍 BUSCAR PRODUCTO POR ID
     * 
     * Encuentra un producto específico usando su ID único.
     * 
     * @param productId El ID del producto a buscar
     * @return El producto encontrado, o null si no existe
     */
    suspend fun getProductById(productId: String): Product?
    
    /**
     * 🔢 BUSCAR PRODUCTO POR SKU
     * 
     * Encuentra un producto usando su código SKU único.
     * 
     * @param sku El SKU del producto a buscar
     * @return El producto encontrado, o null si no existe
     */
    suspend fun getProductBySku(sku: String): Product?
    
    /**
     * ➕ AGREGAR NUEVO PRODUCTO
     * 
     * Agrega un producto nuevo al inventario.
     * 
     * @param product El producto a agregar
     * @return El ID del producto creado
     */
    suspend fun addProduct(product: Product): String
    
    /**
     * 🔄 ACTUALIZAR PRODUCTO EXISTENTE
     * 
     * Modifica la información de un producto existente.
     * 
     * @param product El producto con los datos actualizados
     */
    suspend fun updateProduct(product: Product)
    
    /**
     * 🗑️ ELIMINAR PRODUCTO (SOFT DELETE)
     * 
     * Marca un producto como inactivo sin borrarlo físicamente.
     * Esto preserva el historial de ventas y movimientos.
     * 
     * @param productId El ID del producto a eliminar
     */
    suspend fun deleteProduct(productId: String)
    
    // ==========================================
    // BÚSQUEDA Y FILTRADO
    // ==========================================
    
    /**
     * 🔍 BUSCAR PRODUCTOS POR TEXTO
     * 
     * Busca productos que coincidan con un término de búsqueda
     * en nombre, SKU, categoría o proveedor.
     * 
     * @param query Término de búsqueda
     * @return Flow con los productos que coinciden
     */
    fun searchProducts(query: String): Flow<List<Product>>
    
    /**
     * 📂 FILTRAR PRODUCTOS POR CATEGORÍA
     * 
     * Obtiene todos los productos de una categoría específica.
     * 
     * @param category La categoría a filtrar
     * @return Flow con los productos de esa categoría
     */
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    
    /**
     * 🏪 FILTRAR PRODUCTOS POR PROVEEDOR
     * 
     * Obtiene todos los productos de un proveedor específico.
     * 
     * @param supplier El nombre del proveedor
     * @return Flow con los productos de ese proveedor
     */
    fun getProductsBySupplier(supplier: String): Flow<List<Product>>
    
    /**
     * ⚠️ OBTENER PRODUCTOS CON STOCK BAJO
     * 
     * Devuelve productos que necesitan reposición (stock <= mínimo).
     * 
     * @return Flow con los productos que tienen stock bajo
     */
    fun getLowStockProducts(): Flow<List<Product>>
    
    /**
     * 📦 OBTENER PRODUCTOS SIN STOCK
     * 
     * Devuelve productos que están agotados (stock = 0).
     * 
     * @return Flow con los productos agotados
     */
    fun getOutOfStockProducts(): Flow<List<Product>>
    
    // ==========================================
    // GESTIÓN DE STOCK Y MOVIMIENTOS
    // ==========================================
    
    /**
     * 📊 REGISTRAR MOVIMIENTO DE STOCK
     * 
     * Registra una entrada o salida de stock y actualiza
     * automáticamente la cantidad del producto.
     * 
     * @param movement El movimiento a registrar
     */
    suspend fun recordStockMovement(movement: StockMovement)
    
    /**
     * 📈 ACTUALIZAR STOCK DE PRODUCTO
     * 
     * Actualiza directamente la cantidad en stock de un producto.
     * También registra el movimiento correspondiente.
     * 
     * @param productId ID del producto
     * @param newQuantity Nueva cantidad en stock
     * @param reason Motivo del cambio
     * @param description Descripción adicional
     */
    suspend fun updateProductStock(
        productId: String,
        newQuantity: Int,
        reason: String,
        description: String? = null
    )
    
    /**
     * 📋 OBTENER HISTORIAL DE MOVIMIENTOS
     * 
     * Devuelve todos los movimientos de stock de un producto.
     * 
     * @param productId ID del producto
     * @return Flow con el historial de movimientos
     */
    fun getStockMovements(productId: String): Flow<List<StockMovement>>
    
    /**
     * 📊 OBTENER RESUMEN DE MOVIMIENTOS
     * 
     * Calcula estadísticas de movimientos para un producto.
     * 
     * @param productId ID del producto
     * @param startDate Fecha de inicio (opcional)
     * @param endDate Fecha de fin (opcional)
     * @return Resumen de movimientos del período
     */
    suspend fun getStockMovementSummary(
        productId: String,
        startDate: String? = null,
        endDate: String? = null
    ): StockMovementSummary?
    
    // ==========================================
    // ESTADÍSTICAS Y REPORTES
    // ==========================================
    
    /**
     * 📊 OBTENER TOTAL DE PRODUCTOS
     * 
     * Cuenta cuántos productos activos hay en el inventario.
     * 
     * @return El número total de productos
     */
    suspend fun getTotalProductCount(): Int
    
    /**
     * 💰 CALCULAR VALOR TOTAL DEL INVENTARIO
     * 
     * Suma el valor de todos los productos en stock
     * basado en el precio de compra.
     * 
     * @return El valor total del inventario
     */
    suspend fun getTotalInventoryValue(): Double
    
    /**
     * 💵 CALCULAR VALOR POTENCIAL DE VENTA
     * 
     * Suma el valor de todos los productos en stock
     * basado en el precio de venta.
     * 
     * @return El valor potencial de venta
     */
    suspend fun getTotalSaleValue(): Double
    
    /**
     * 📂 OBTENER ESTADÍSTICAS POR CATEGORÍA
     * 
     * Calcula estadísticas agrupadas por categoría de producto.
     * 
     * @return Mapa con estadísticas por categoría
     */
    suspend fun getCategoryStatistics(): Map<String, CategoryStats>
    
    /**
     * 🏆 OBTENER PRODUCTOS MÁS VENDIDOS
     * 
     * Identifica los productos con más movimientos de salida.
     * 
     * @param limit Número máximo de productos a devolver
     * @return Lista de productos más vendidos
     */
    suspend fun getTopSellingProducts(limit: Int = 10): List<ProductSalesStats>
    
    /**
     * 📉 OBTENER PRODUCTOS MENOS VENDIDOS
     * 
     * Identifica los productos con menos movimientos de salida.
     * 
     * @param limit Número máximo de productos a devolver
     * @return Lista de productos menos vendidos
     */
    suspend fun getLeastSellingProducts(limit: Int = 10): List<ProductSalesStats>
    
    // ==========================================
    // VALIDACIONES Y UTILIDADES
    // ==========================================
    
    /**
     * ✅ VERIFICAR SI SKU EXISTE
     * 
     * Valida si un SKU ya está en uso por otro producto.
     * 
     * @param sku El SKU a verificar
     * @param excludeProductId ID del producto a excluir (para actualizaciones)
     * @return true si el SKU ya existe
     */
    suspend fun isSkuExists(sku: String, excludeProductId: String? = null): Boolean
    
    /**
     * 📦 VERIFICAR DISPONIBILIDAD DE STOCK
     * 
     * Verifica si hay suficiente stock para una venta.
     * 
     * @param productId ID del producto
     * @param quantity Cantidad requerida
     * @return true si hay suficiente stock
     */
    suspend fun isStockAvailable(productId: String, quantity: Int): Boolean
    
    /**
     * 🔄 SINCRONIZAR CON LA NUBE (OPCIONAL)
     * 
     * Sincroniza los datos locales con el almacenamiento en la nube.
     * 
     * @return true si la sincronización fue exitosa
     */
    suspend fun syncWithCloud(): Boolean
}

/**
 * 📊 ESTADÍSTICAS POR CATEGORÍA
 * 
 * Data class para estadísticas agrupadas por categoría.
 */
data class CategoryStats(
    /**
     * 📂 CATEGORÍA
     */
    val categoryId: String,
    
    /**
     * 🔢 NÚMERO DE PRODUCTOS
     */
    val productCount: Int,
    
    /**
     * 📦 TOTAL DE UNIDADES EN STOCK
     */
    val totalStock: Int,
    
    /**
     * 💰 VALOR TOTAL DEL INVENTARIO
     */
    val totalValue: Double,
    
    /**
     * 💵 VALOR POTENCIAL DE VENTA
     */
    val totalSaleValue: Double,
    
    /**
     * ⚠️ PRODUCTOS CON STOCK BAJO
     */
    val lowStockCount: Int
) {
    
    /**
     * 📊 CALCULAR MARGEN PROMEDIO
     * 
     * Calcula el margen de ganancia promedio de la categoría.
     */
    fun getAverageMargin(): Double {
        return if (totalValue > 0) {
            ((totalSaleValue - totalValue) / totalValue) * 100
        } else {
            0.0
        }
    }
    
    /**
     * 📈 CALCULAR PORCENTAJE DE STOCK BAJO
     * 
     * Calcula qué porcentaje de productos tiene stock bajo.
     */
    fun getLowStockPercentage(): Double {
        return if (productCount > 0) {
            (lowStockCount.toDouble() / productCount) * 100
        } else {
            0.0
        }
    }
}

/**
 * 🏆 ESTADÍSTICAS DE VENTAS POR PRODUCTO
 * 
 * Data class para productos más/menos vendidos.
 */
data class ProductSalesStats(
    /**
     * 📦 INFORMACIÓN DEL PRODUCTO
     */
    val product: Product,
    
    /**
     * 🔢 TOTAL DE UNIDADES VENDIDAS
     */
    val totalSold: Int,
    
    /**
     * 💰 VALOR TOTAL DE VENTAS
     */
    val totalSalesValue: Double,
    
    /**
     * 📊 NÚMERO DE TRANSACCIONES
     */
    val transactionCount: Int,
    
    /**
     * 📅 ÚLTIMA VENTA
     */
    val lastSaleDate: String?
) {
    
    /**
     * 💵 CALCULAR VENTA PROMEDIO
     * 
     * Calcula el valor promedio por transacción.
     */
    fun getAverageTransactionValue(): Double {
        return if (transactionCount > 0) {
            totalSalesValue / transactionCount
        } else {
            0.0
        }
    }
    
    /**
     * 📦 CALCULAR UNIDADES PROMEDIO POR VENTA
     * 
     * Calcula cuántas unidades se venden en promedio por transacción.
     */
    fun getAverageUnitsPerSale(): Double {
        return if (transactionCount > 0) {
            totalSold.toDouble() / transactionCount
        } else {
            0.0
        }
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Repository Pattern**: Abstrae el acceso a datos
 * 2. **Flow**: Para datos reactivos que se actualizan automáticamente
 * 3. **Suspend Functions**: Para operaciones asíncronas
 * 4. **Business Logic**: Lógica de negocio en el dominio
 * 5. **Data Consistency**: Mantiene consistencia entre operaciones
 * 
 * ANALOGÍA:
 * 
 * El InventoryRepository es como el "gerente de inventario" de una tienda:
 * 
 * 1. **Conoce todo el inventario**: Puede buscar cualquier producto
 * 2. **Registra movimientos**: Documenta entradas y salidas
 * 3. **Genera reportes**: Calcula estadísticas y tendencias
 * 4. **Mantiene orden**: Asegura que todo esté bien organizado
 * 5. **Alerta problemas**: Avisa cuando hay stock bajo
 * 
 * BENEFICIOS DEL PATRÓN REPOSITORY:
 * - Separa la lógica de negocio del almacenamiento
 * - Facilita las pruebas unitarias (se puede mockear)
 * - Permite cambiar la implementación sin afectar el dominio
 * - Centraliza las operaciones de datos
 * - Proporciona una API limpia y consistente
 * 
 * IMPLEMENTACIONES POSIBLES:
 * - Local: Room database
 * - Remoto: Firebase Firestore
 * - Híbrido: Local + sincronización en la nube
 * - En memoria: Para pruebas unitarias
 */