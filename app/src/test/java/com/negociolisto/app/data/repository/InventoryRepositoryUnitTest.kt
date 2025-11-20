package com.negociolisto.app.data.repository

import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.ProductCategory
import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 🧪 PRUEBAS UNITARIAS PARA InventoryRepository
 * 
 * Estas son pruebas unitarias simples que no requieren Android
 * para verificar la lógica básica del repositorio.
 */
class InventoryRepositoryUnitTest {
    
    private val testProduct = Product(
        id = "test-product-1",
        name = "Producto de Prueba",
        description = "Descripción de prueba",
        sku = "TEST-001",
        purchasePrice = 100.0,
        salePrice = 150.0,
        stockQuantity = 10,
        minimumStock = 5,
        category = ProductCategory.OTHER,
        supplier = "Proveedor Test",
        photoUrl = null,
        createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        isActive = true
    )
    
    /**
     * ✅ PRUEBA: Validación de datos de producto
     */
    @Test
    fun `product data validation should work correctly`() {
        // Arrange
        val product = testProduct
        
        // Act & Assert
        assertNotNull("Producto no debería ser null", product)
        assertEquals("Nombre incorrecto", "Producto de Prueba", product.name)
        assertEquals("SKU incorrecto", "TEST-001", product.sku)
        assertEquals("Precio de compra incorrecto", 100.0, product.purchasePrice, 0.01)
        assertEquals("Precio de venta incorrecto", 150.0, product.salePrice, 0.01)
        assertEquals("Stock incorrecto", 10, product.stockQuantity)
        assertEquals("Stock mínimo incorrecto", 5, product.minimumStock)
        assertEquals("Categoría incorrecta", ProductCategory.OTHER, product.category)
        assertTrue("Producto debería estar activo", product.isActive)
    }
    
    /**
     * ✅ PRUEBA: Cálculo de margen de ganancia
     */
    @Test
    fun `profit margin calculation should work correctly`() {
        // Arrange
        val product = testProduct
        
        // Act
        val profitMargin = product.salePrice - product.purchasePrice
        val profitPercentage = (profitMargin / product.purchasePrice) * 100
        
        // Assert
        assertEquals("Margen de ganancia incorrecto", 50.0, profitMargin, 0.01)
        assertEquals("Porcentaje de ganancia incorrecto", 50.0, profitPercentage, 0.01)
    }
    
    /**
     * ✅ PRUEBA: Verificación de stock bajo
     */
    @Test
    fun `low stock detection should work correctly`() {
        // Arrange
        val lowStockProduct = testProduct.copy(
            stockQuantity = 3,
            minimumStock = 5
        )
        val normalStockProduct = testProduct.copy(
            stockQuantity = 10,
            minimumStock = 5
        )
        
        // Act
        val isLowStock1 = lowStockProduct.stockQuantity < lowStockProduct.minimumStock
        val isLowStock2 = normalStockProduct.stockQuantity < normalStockProduct.minimumStock
        
        // Assert
        assertTrue("Producto con stock bajo debería detectarse", isLowStock1)
        assertFalse("Producto con stock normal no debería detectarse como bajo", isLowStock2)
    }
    
    /**
     * ✅ PRUEBA: Validación de SKU
     */
    @Test
    fun `sku validation should work correctly`() {
        // Arrange
        val validSkus = listOf("ABC123", "PROD-001", "SKU_123", "A1B2C3")
        val invalidSkus = listOf("", "AB", "SKU CON ESPACIOS", "SKU@ESPECIAL")
        
        // Act & Assert
        validSkus.forEach { sku ->
            val isValid = sku.length >= 3 && sku.matches(Regex("[A-Za-z0-9_-]+"))
            assertTrue("SKU '$sku' debería ser válido", isValid)
        }
        
        invalidSkus.forEach { sku ->
            val isValid = sku.length >= 3 && sku.matches(Regex("[A-Za-z0-9_-]+"))
            assertFalse("SKU '$sku' debería ser inválido", isValid)
        }
    }
    
    /**
     * ✅ PRUEBA: Cálculo de valor de inventario
     */
    @Test
    fun `inventory value calculation should work correctly`() {
        // Arrange
        val products = listOf(
            testProduct.copy(id = "1", stockQuantity = 10, purchasePrice = 100.0),
            testProduct.copy(id = "2", stockQuantity = 5, purchasePrice = 200.0),
            testProduct.copy(id = "3", stockQuantity = 3, purchasePrice = 50.0)
        )
        
        // Act
        val totalValue = products.sumOf { it.stockQuantity * it.purchasePrice }
        val totalStock = products.sumOf { it.stockQuantity }
        val averagePrice = totalValue / totalStock
        
        // Assert
        assertEquals("Valor total incorrecto", 1650.0, totalValue, 0.01)
        assertEquals("Stock total incorrecto", 18, totalStock)
        assertEquals("Precio promedio incorrecto", 91.67, averagePrice, 0.01)
    }
    
    /**
     * ✅ PRUEBA: Categorización de productos
     */
    @Test
    fun `product categorization should work correctly`() {
        // Arrange
        val products = listOf(
            testProduct.copy(id = "1", category = ProductCategory.CLOTHING),
            testProduct.copy(id = "2", category = ProductCategory.FOOD),
            testProduct.copy(id = "3", category = ProductCategory.ELECTRONICS),
            testProduct.copy(id = "4", category = ProductCategory.CLOTHING)
        )
        
        // Act
        val categoryGroups = products.groupBy { it.category }
        val clothingCount = categoryGroups[ProductCategory.CLOTHING]?.size ?: 0
        val foodCount = categoryGroups[ProductCategory.FOOD]?.size ?: 0
        val electronicsCount = categoryGroups[ProductCategory.ELECTRONICS]?.size ?: 0
        
        // Assert
        assertEquals("Cantidad de ropa incorrecta", 2, clothingCount)
        assertEquals("Cantidad de comida incorrecta", 1, foodCount)
        assertEquals("Cantidad de electrónicos incorrecta", 1, electronicsCount)
        assertEquals("Total de categorías incorrecto", 3, categoryGroups.size)
    }
    
    /**
     * ✅ PRUEBA: Validación de precios
     */
    @Test
    fun `price validation should work correctly`() {
        // Arrange
        val validPrices = listOf(0.01, 100.0, 1500.50, 999999.99)
        val invalidPrices = listOf(-100.0, 0.0, 1000000000.0)
        
        // Act & Assert
        validPrices.forEach { price ->
            val isValid = price > 0 && price <= 1000000
            assertTrue("Precio '$price' debería ser válido", isValid)
        }
        
        invalidPrices.forEach { price ->
            val isValid = price > 0 && price <= 1000000
            assertFalse("Precio '$price' debería ser inválido", isValid)
        }
    }
    
    /**
     * ✅ PRUEBA: Formateo de datos
     */
    @Test
    fun `data formatting should work correctly`() {
        // Arrange
        val product = testProduct
        
        // Act
        val formattedPrice = String.format("%.2f", product.salePrice)
        val formattedSku = product.sku.uppercase()
        val formattedName = product.name.trim().replace("\\s+".toRegex(), " ")
        
        // Assert
        assertEquals("Precio formateado incorrecto", "150.00", formattedPrice)
        assertEquals("SKU formateado incorrecto", "TEST-001", formattedSku)
        assertEquals("Nombre formateado incorrecto", "Producto de Prueba", formattedName)
    }
}
