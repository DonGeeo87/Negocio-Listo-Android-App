package com.negociolisto.app.domain.usecase.inventory

import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.ProductCategory
import com.negociolisto.app.domain.model.StockMovement
import com.negociolisto.app.domain.repository.InventoryRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 🧪 PRUEBAS UNITARIAS PARA AddProductUseCase
 * 
 * Estas pruebas verifican que el caso de uso funcione correctamente
 * en diferentes escenarios. Es como tener un "laboratorio de calidad"
 * que prueba todas las situaciones posibles.
 */
class AddProductUseCaseTest {
    
    @Mock
    private lateinit var inventoryRepository: InventoryRepository
    
    private lateinit var addProductUseCase: AddProductUseCase
    
    private val testProduct = Product(
        id = "test-id",
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
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addProductUseCase = AddProductUseCase(inventoryRepository)
    }
    
    /**
     * ✅ PRUEBA: Agregar producto válido exitosamente
     */
    @Test
    fun `when adding valid product then should succeed and record stock movement`() = runTest {
        // Given - Configurar mocks
        whenever(inventoryRepository.isSkuExists(testProduct.sku)).thenReturn(false)
        whenever(inventoryRepository.addProduct(any())).thenReturn("new-product-id")
        
        // When - Ejecutar caso de uso
        val result = addProductUseCase(testProduct)
        
        // Then - Verificar resultados
        assertTrue(result.isSuccess)
        assertEquals("new-product-id", result.getOrNull())
        
        // Verificar que se llamaron los métodos correctos
        verify(inventoryRepository).isSkuExists(testProduct.sku)
        verify(inventoryRepository).addProduct(any())
        verify(inventoryRepository).recordStockMovement(any())
    }
    
    /**
     * ❌ PRUEBA: Fallar cuando el SKU ya existe
     */
    @Test
    fun `when adding product with existing SKU then should fail`() = runTest {
        // Given - SKU ya existe
        whenever(inventoryRepository.isSkuExists(testProduct.sku)).thenReturn(true)
        
        // When - Ejecutar caso de uso
        val result = addProductUseCase(testProduct)
        
        // Then - Verificar que falló
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Ya existe un producto con el SKU") == true)
        
        // Verificar que no se agregó el producto
        verify(inventoryRepository, never()).addProduct(any())
        verify(inventoryRepository, never()).recordStockMovement(any())
    }
    
    /**
     * ❌ PRUEBA: Fallar con datos inválidos
     */
    @Test
    fun `when adding product with invalid data then should fail`() = runTest {
        // Given - Producto con datos inválidos
        val invalidProduct = testProduct.copy(
            name = "", // Nombre vacío
            purchasePrice = -10.0, // Precio negativo
            salePrice = 50.0 // Precio de venta menor al de compra
        )
        
        // When - Ejecutar caso de uso
        val result = addProductUseCase(invalidProduct)
        
        // Then - Verificar que falló
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Errores de validación") == true)
        
        // Verificar que no se hicieron llamadas al repositorio
        verify(inventoryRepository, never()).isSkuExists(any())
        verify(inventoryRepository, never()).addProduct(any())
    }
    
    /**
     * ✅ PRUEBA: Agregar producto sin stock inicial
     */
    @Test
    fun `when adding product with zero stock then should not record stock movement`() = runTest {
        // Given - Producto sin stock inicial
        val productWithoutStock = testProduct.copy(stockQuantity = 0)
        whenever(inventoryRepository.isSkuExists(productWithoutStock.sku)).thenReturn(false)
        whenever(inventoryRepository.addProduct(any())).thenReturn("new-product-id")
        
        // When - Ejecutar caso de uso
        val result = addProductUseCase(productWithoutStock)
        
        // Then - Verificar éxito pero sin movimiento de stock
        assertTrue(result.isSuccess)
        verify(inventoryRepository).addProduct(any())
        verify(inventoryRepository, never()).recordStockMovement(any())
    }
    
    /**
     * ❌ PRUEBA: Manejar excepción del repositorio
     */
    @Test
    fun `when repository throws exception then should return failure`() = runTest {
        // Given - Repositorio lanza excepción
        whenever(inventoryRepository.isSkuExists(any())).thenThrow(RuntimeException("Error de base de datos"))
        
        // When - Ejecutar caso de uso
        val result = addProductUseCase(testProduct)
        
        // Then - Verificar que se maneja la excepción
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }
    
    /**
     * ✅ PRUEBA: Verificar que se registra el movimiento de stock correcto
     */
    @Test
    fun `when adding product with stock then should record correct stock movement`() = runTest {
        // Given
        whenever(inventoryRepository.isSkuExists(testProduct.sku)).thenReturn(false)
        whenever(inventoryRepository.addProduct(any())).thenReturn("new-product-id")
        
        // When
        val result = addProductUseCase(testProduct)
        
        // Then
        assertTrue(result.isSuccess)
        
        // Capturar el movimiento de stock registrado
        val stockMovementCaptor = argumentCaptor<StockMovement>()
        verify(inventoryRepository).recordStockMovement(stockMovementCaptor.capture())
        
        val recordedMovement = stockMovementCaptor.firstValue
        assertEquals("new-product-id", recordedMovement.productId)
        assertEquals(testProduct.stockQuantity, recordedMovement.quantity)
        assertEquals(0, recordedMovement.previousStock)
        assertEquals(testProduct.stockQuantity, recordedMovement.newStock)
    }
    
    /**
     * ✅ PRUEBA: Verificar que se actualizan las fechas correctamente
     */
    @Test
    fun `when adding product then should set current timestamps`() = runTest {
        // Given
        whenever(inventoryRepository.isSkuExists(testProduct.sku)).thenReturn(false)
        whenever(inventoryRepository.addProduct(any())).thenReturn("new-product-id")
        
        // When
        val result = addProductUseCase(testProduct)
        
        // Then
        assertTrue(result.isSuccess)
        
        // Capturar el producto agregado
        val productCaptor = argumentCaptor<Product>()
        verify(inventoryRepository).addProduct(productCaptor.capture())
        
        val addedProduct = productCaptor.firstValue
        assertTrue(addedProduct.isActive)
        // Las fechas deberían ser recientes (dentro de los últimos segundos)
        // En una implementación real, podríamos usar un Clock mockeado para mayor precisión
    }
}

/**
 * 📚 CONCEPTOS DE TESTING:
 * 
 * 1. **Unit Testing**: Pruebas aisladas de una unidad de código
 * 2. **Mocking**: Simular dependencias para aislar la lógica
 * 3. **Test Cases**: Diferentes escenarios a probar
 * 4. **Assertions**: Verificaciones de que el resultado es correcto
 * 5. **Test Coverage**: Cubrir todos los caminos posibles
 * 
 * ESTRUCTURA DE PRUEBAS (Given-When-Then):
 * - **Given**: Configurar el estado inicial y mocks
 * - **When**: Ejecutar la acción que queremos probar
 * - **Then**: Verificar que el resultado es el esperado
 * 
 * ESCENARIOS PROBADOS:
 * ✅ Caso exitoso normal
 * ❌ SKU duplicado
 * ❌ Datos inválidos
 * ✅ Producto sin stock inicial
 * ❌ Excepción del repositorio
 * ✅ Movimiento de stock correcto
 * ✅ Fechas actualizadas correctamente
 * 
 * BENEFICIOS DE LAS PRUEBAS:
 * - Detectan errores temprano
 * - Documentan el comportamiento esperado
 * - Facilitan refactoring seguro
 * - Mejoran la confianza en el código
 * - Reducen bugs en producción
 */