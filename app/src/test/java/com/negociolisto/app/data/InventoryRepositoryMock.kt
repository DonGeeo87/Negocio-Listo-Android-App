package com.negociolisto.app.data

import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.ProductCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object InventoryRepositoryMock {
    
    private val productsFlow = MutableStateFlow(
        listOf(
        Product(
            id = "1",
            name = "Coca Cola 600ml",
            description = "Refresco de cola 600ml",
            sku = "COC-600-001",
            purchasePrice = 600.0,
            salePrice = 990.0,
            stockQuantity = 50,
            minimumStock = 10,
            category = ProductCategory.BEBIDAS,
            supplier = "Coca Cola FEMSA",
            photoUrl = "https://images.unsplash.com/photo-1583338917451-327c288ef3fd?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        ),
        Product(
            id = "2",
            name = "Pan Blanco Bimbo",
            description = "Pan de caja blanco grande",
            sku = "PAN-BLA-001",
            purchasePrice = 1400.0,
            salePrice = 1890.0,
            stockQuantity = 8,
            minimumStock = 15,
            category = ProductCategory.PANADERIA,
            supplier = "Grupo Bimbo",
            photoUrl = "https://images.unsplash.com/photo-1542834369-f10ebf06d3cb?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        ),
        Product(
            id = "3",
            name = "Leche Lala 1L",
            description = "Leche entera ultrapasteurizada",
            sku = "LEC-LAL-001",
            purchasePrice = 850.0,
            salePrice = 1190.0,
            stockQuantity = 25,
            minimumStock = 10,
            category = ProductCategory.LACTEOS,
            supplier = "Grupo Lala",
            photoUrl = "https://images.unsplash.com/photo-1550581190-9c1c48d21d6c?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        ),
        Product(
            id = "4",
            name = "Arroz Verde Valle 1kg",
            description = "Arroz blanco grano largo",
            sku = "ARR-VER-001",
            purchasePrice = 1250.0,
            salePrice = 1590.0,
            stockQuantity = 3,
            minimumStock = 5,
            category = ProductCategory.ABARROTES,
            supplier = "Verde Valle",
            photoUrl = "https://images.unsplash.com/photo-1604908811456-0e96fbee6c93?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        ),
        Product(
            id = "5",
            name = "Jabón Zote Rosa",
            description = "Jabón para lavar ropa",
            sku = "JAB-ZOT-001",
            purchasePrice = 700.0,
            salePrice = 990.0,
            stockQuantity = 20,
            minimumStock = 8,
            category = ProductCategory.LIMPIEZA,
            supplier = "Jabones Zote",
            photoUrl = "https://images.unsplash.com/photo-1624221164743-0d297927651c?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        ),
        Product(
            id = "6",
            name = "Queso Gouda 500g",
            description = "Queso maduro tipo Gouda",
            sku = "QUE-GOU-500",
            purchasePrice = 3500.0,
            salePrice = 4590.0,
            stockQuantity = 12,
            minimumStock = 6,
            category = ProductCategory.LACTEOS,
            supplier = "Colún",
            photoUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        ),
        Product(
            id = "7",
            name = "Manzanas Rojas 1kg",
            description = "Manzana Red Delicious a granel",
            sku = "MAN-ROJ-1KG",
            purchasePrice = 900.0,
            salePrice = 1490.0,
            stockQuantity = 18,
            minimumStock = 10,
            category = ProductCategory.FRUTAS_VERDURAS,
            supplier = "Distribuidora Frutera",
            photoUrl = "https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        ),
        Product(
            id = "8",
            name = "Detergente Líquido 3L",
            description = "Detergente para ropa aroma fresco",
            sku = "DET-LIQ-3L",
            purchasePrice = 6000.0,
            salePrice = 7990.0,
            stockQuantity = 7,
            minimumStock = 5,
            category = ProductCategory.LIMPIEZA,
            supplier = "Ariel",
            photoUrl = "https://images.unsplash.com/photo-1585386959984-a4155223168f?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        ),
        Product(
            id = "9",
            name = "Galletas Chocolate 12u",
            description = "Pack familiar galletas con chips",
            sku = "GAL-CHO-012",
            purchasePrice = 1800.0,
            salePrice = 2490.0,
            stockQuantity = 40,
            minimumStock = 10,
            category = ProductCategory.DULCES,
            supplier = "Nestlé",
            photoUrl = "https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        ),
        Product(
            id = "10",
            name = "Café Molido 500g",
            description = "Café 100% arábica tostado medio",
            sku = "CAF-MOL-500",
            purchasePrice = 6500.0,
            salePrice = 7990.0,
            stockQuantity = 14,
            minimumStock = 6,
            category = ProductCategory.ABARROTES,
            supplier = "Juan Valdez",
            photoUrl = "https://images.unsplash.com/photo-1459755486867-b55449bb39ff?w=640",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isActive = true
        )
        )
    )
    
    fun getAllProducts(): Flow<List<Product>> = productsFlow.asStateFlow()
    
    fun searchProducts(query: String): Flow<List<Product>> {
        return kotlinx.coroutines.flow.flowOf(
            productsFlow.value.filter { 
                it.name.contains(query, ignoreCase = true) ||
                it.description?.contains(query, ignoreCase = true) == true ||
                it.sku.contains(query, ignoreCase = true)
            }
        )
    }
    
    fun getLowStockProducts(): Flow<List<Product>> {
        return kotlinx.coroutines.flow.flowOf(productsFlow.value.filter { it.hasLowStock() })
    }

    fun addProduct(product: Product) {
        productsFlow.update { it + product }
    }

    fun reduceStock(productId: String, quantity: Int) {
        productsFlow.update { list ->
            list.map { p ->
                if (p.id == productId) p.copy(stockQuantity = (p.stockQuantity - quantity).coerceAtLeast(0)) else p
            }
        }
    }

    // --- NUEVAS OPERACIONES CRUD ---
    fun getProductById(productId: String): Product? {
        return productsFlow.value.firstOrNull { it.id == productId }
    }

    fun updateProduct(updated: Product) {
        productsFlow.update { list ->
            list.map { p ->
                if (p.id == updated.id) {
                    // Mantener createdAt original si viene vacío
                    updated.copy(
                        createdAt = updated.createdAt,
                        updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                } else p
            }
        }
    }

    fun deleteProduct(productId: String) {
        productsFlow.update { list -> list.filterNot { it.id == productId } }
    }
}