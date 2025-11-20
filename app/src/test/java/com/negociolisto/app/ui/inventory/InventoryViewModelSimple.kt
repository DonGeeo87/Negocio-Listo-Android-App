package com.negociolisto.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.InventoryRepositoryMock
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.ProductCategory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 🧠 VIEWMODEL SIMPLE PARA INVENTARIO (SIN HILT)
 */
class InventoryViewModelSimple : ViewModel() {
    
    private val repository = InventoryRepositoryMock
    
    // Estados de la UI
    private val _uiState = MutableStateFlow(SimpleInventoryUiState())
    val uiState: StateFlow<SimpleInventoryUiState> = _uiState.asStateFlow()
    
    // Productos filtrados
    val filteredProducts: StateFlow<List<Product>> = combine(
        repository.getAllProducts(),
        _uiState.map { it.searchQuery },
        _uiState.map { it.selectedCategory },
        _uiState.map { it.showLowStockOnly }
    ) { products, query, category, showLowStock ->
        products.filter { product ->
            // Filtro por búsqueda
            val matchesSearch = query.isBlank() || 
                product.name.contains(query, ignoreCase = true) ||
                product.description?.contains(query, ignoreCase = true) == true ||
                product.sku.contains(query, ignoreCase = true)
            
            // Filtro por categoría
            val matchesCategory = category == null || product.category == category
            
            // Filtro por stock bajo
            val matchesLowStock = !showLowStock || product.hasLowStock()
            
            matchesSearch && matchesCategory && matchesLowStock
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        loadInventoryStats()
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun selectCategory(category: ProductCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
    
    fun toggleLowStockFilter() {
        _uiState.update { it.copy(showLowStockOnly = !it.showLowStockOnly) }
    }
    
    fun refresh() {
        loadInventoryStats()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun addProduct(product: Product) {
        repository.addProduct(product)
        refresh()
    }

    fun getProduct(productId: String): Product? {
        return repository.getProductById(productId)
    }

    fun updateProduct(product: Product) {
        repository.updateProduct(product)
        refresh()
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(productId)
        refresh()
    }
    
    private fun loadInventoryStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                repository.getAllProducts().collect { products ->
                    val stats = SimpleInventoryStats(
                        totalProducts = products.size,
                        lowStockProducts = products.count { it.hasLowStock() },
                        totalValue = products.sumOf { it.salePrice * it.stockQuantity },
                        categories = products.groupBy { it.category }.size
                    )
                    
                    _uiState.update { 
                        it.copy(
                            inventoryStats = stats,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar inventario: ${e.message}"
                    )
                }
            }
        }
    }
}

data class SimpleInventoryUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: ProductCategory? = null,
    val showLowStockOnly: Boolean = false,
    val inventoryStats: SimpleInventoryStats = SimpleInventoryStats(),
    val error: String? = null
)

data class SimpleInventoryStats(
    val totalProducts: Int = 0,
    val lowStockProducts: Int = 0,
    val totalValue: Double = 0.0,
    val categories: Int = 0
)