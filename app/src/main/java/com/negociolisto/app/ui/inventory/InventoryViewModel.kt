package com.negociolisto.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.Product
// import com.negociolisto.app.domain.model.ProductCategory // Eliminado - usar solo CustomCategory
import com.negociolisto.app.domain.model.CustomCategory
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.data.service.ImageService
import com.negociolisto.app.data.service.UsageLimitsService
import com.negociolisto.app.data.analytics.AnalyticsHelper
import com.negociolisto.app.data.analytics.CrashlyticsHelper
import com.negociolisto.app.data.analytics.PerformanceHelper
import android.content.Context
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 📦 VIEWMODEL DE INVENTARIO
 * 
 * Maneja el estado y la lógica de la pantalla de inventario.
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val customCategoryRepository: CustomCategoryRepository,
    private val authRepository: AuthRepository,
    private val imageService: ImageService,
    private val analyticsHelper: AnalyticsHelper,
    private val crashlyticsHelper: CrashlyticsHelper,
    private val performanceHelper: PerformanceHelper,
    private val usageLimitsService: UsageLimitsService
) : ViewModel() {
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    // Estados para upload de imágenes
    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress = _uploadProgress.asStateFlow()
    
    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage = _isUploadingImage.asStateFlow()
    
    private val _uploadStatus = MutableStateFlow<String?>(null)
    val uploadStatus = _uploadStatus.asStateFlow()
    
    // Categorías personalizadas reactivas al UID real
    private val currentUserIdFlow: StateFlow<String?> = authRepository.currentUser
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val customCategories: StateFlow<List<CustomCategory>> = currentUserIdFlow
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(emptyList()) else customCategoryRepository.getActiveCategoriesByUser(uid)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(1000),
            initialValue = emptyList()
        )
    
    // Productos filtrados por categoría y búsqueda
    val products: StateFlow<List<Product>> = combine(
        inventoryRepository.getAllProducts(),
        customCategories,
        selectedCategory,
        searchQuery
    ) { allProducts, customCategories, category, query ->
        // Combinar productos con categorías personalizadas
        val productsWithCustomCategories = allProducts.map { product ->
            if (product.customCategoryId.isNotEmpty()) {
                // Buscar la categoría personalizada por el customCategoryId si existe
                // Esto es un workaround temporal
                product
            } else {
                product
            }
        }
        
        var filtered = productsWithCustomCategories
        
        // Filtrar por categoría si está seleccionada
        if (category != null) {
            filtered = filtered.filter { it.customCategoryId == category }
        }
        
        // Filtrar por búsqueda si hay query
        if (query.isNotBlank()) {
            filtered = filtered.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                product.description?.contains(query, ignoreCase = true) == true ||
                product.sku.contains(query, ignoreCase = true)
            }
        }
        
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Productos con stock bajo
    val lowStockProducts: StateFlow<List<Product>> = inventoryRepository.getLowStockProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(1000),
            initialValue = emptyList()
        )
    
    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun addProduct(product: Product) {
        viewModelScope.launch {
            val trace = performanceHelper.startTrace(PerformanceHelper.Traces.PRODUCT_LIST_LOAD)
            try {
                _isLoading.value = true
                _error.value = null
                trace.putAttribute("operation", "add_product")
                trace.putAttribute("product_name", product.name)
                
                // Verificar límite antes de agregar
                val limitCheck = usageLimitsService.checkProductLimit()
                if (!limitCheck.canAdd) {
                    _error.value = limitCheck.message ?: "Has alcanzado el límite de productos permitidos."
                    return@launch
                }
                
                inventoryRepository.addProduct(product)
                
                // Analytics
                analyticsHelper.logProductAdded(product.name, product.customCategoryId)
                
                // Performance
                trace.incrementMetric(PerformanceHelper.Metrics.PRODUCT_COUNT, 1)
                
            } catch (e: Exception) {
                _error.value = "Error al agregar producto: ${e.message}"
                crashlyticsHelper.recordException(e)
                crashlyticsHelper.log("Error agregando producto: ${product.name}")
            } finally {
                _isLoading.value = false
                performanceHelper.stopTrace(trace)
            }
        }
    }
    
    /**
     * 🖼️ AGREGAR PRODUCTO CON IMAGEN COMPRIMIDA
     * 
     * Agrega un producto con compresión automática de imagen
     */
    fun addProductWithImage(
        context: Context,
        product: Product,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            val trace = performanceHelper.startTrace(PerformanceHelper.Traces.IMAGE_UPLOAD)
            try {
                _isLoading.value = true
                _isUploadingImage.value = true
                _error.value = null
                _uploadStatus.value = "Comprimiendo imagen..."
                _uploadProgress.value = 0
                
                trace.putAttribute("operation", "add_product_with_image")
                trace.putAttribute("product_name", product.name)
                
                // 1. Subir imagen con compresión automática
                val uploadResult = imageService.uploadProductImage(
                    context = context,
                    imageUri = imageUri,
                    productId = product.id,
                    onProgress = { progress ->
                        _uploadProgress.value = progress
                        _uploadStatus.value = "Subiendo imagen... $progress%"
                    }
                )
                
                if (uploadResult.isSuccess) {
                    val imageUrl = uploadResult.getOrThrow()
                    
                    // 2. Generar thumbnail
                    _uploadStatus.value = "Generando miniatura..."
                    val thumbnailResult = imageService.generateThumbnail(
                        context = context,
                        imageUri = imageUri,
                        size = 200,
                        quality = 70
                    )
                    
                    val thumbnailUrl = if (thumbnailResult.isSuccess) {
                        // Subir thumbnail a Storage
                        val thumbnailUpload = imageService.uploadWithProgress(
                            file = thumbnailResult.getOrThrow(),
                            path = "products/thumbnails/${product.id}_thumb.jpg",
                            onProgress = { /* Progreso del thumbnail */ }
                        )
                        thumbnailUpload.getOrNull()
                    } else null
                    
                    // 3. Actualizar producto con URLs
                    val updatedProduct = product.copy(
                        photoUrl = imageUrl,
                        thumbnailUrl = thumbnailUrl
                    )
                    
                    // 4. Verificar límite antes de guardar
                    val limitCheck = usageLimitsService.checkProductLimit()
                    if (!limitCheck.canAdd) {
                        _error.value = limitCheck.message ?: "Has alcanzado el límite de productos permitidos."
                        return@launch
                    }
                    
                    // 5. Guardar producto en base de datos
                    _uploadStatus.value = "Guardando producto..."
                    inventoryRepository.addProduct(updatedProduct)
                    
                    // Analytics
                    analyticsHelper.logProductAdded(updatedProduct.name, updatedProduct.customCategoryId)
                    
                    _uploadStatus.value = "✅ Producto agregado exitosamente"
                } else {
                    val error = uploadResult.exceptionOrNull()
                    _error.value = "Error subiendo imagen: ${error?.message}"
                    error?.let { crashlyticsHelper.recordException(it) }
                    crashlyticsHelper.log("Error subiendo imagen de producto: ${product.name}")
                }
                
            } catch (e: Exception) {
                _error.value = "Error al agregar producto: ${e.message}"
                crashlyticsHelper.recordException(e)
                crashlyticsHelper.log("Error agregando producto con imagen: ${product.name}")
            } finally {
                _isLoading.value = false
                _isUploadingImage.value = false
                _uploadProgress.value = 0
                performanceHelper.stopTrace(trace)
                // Limpiar status después de 3 segundos
                viewModelScope.launch {
                    kotlinx.coroutines.delay(3000)
                    _uploadStatus.value = null
                }
            }
        }
    }
    
    /**
     * 🖼️ ACTUALIZAR PRODUCTO CON NUEVA IMAGEN
     * 
     * Actualiza un producto con nueva imagen comprimida
     */
    fun updateProductWithImage(
        context: Context,
        product: Product,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            val trace = performanceHelper.startTrace(PerformanceHelper.Traces.IMAGE_UPLOAD)
            try {
                _isLoading.value = true
                _isUploadingImage.value = true
                _error.value = null
                _uploadStatus.value = "Comprimiendo imagen..."
                _uploadProgress.value = 0
                
                trace.putAttribute("operation", "update_product_with_image")
                trace.putAttribute("product_name", product.name)
                
                // 1. Subir nueva imagen con compresión
                val uploadResult = imageService.uploadProductImage(
                    context = context,
                    imageUri = imageUri,
                    productId = product.id,
                    onProgress = { progress ->
                        _uploadProgress.value = progress
                        _uploadStatus.value = "Subiendo imagen... $progress%"
                    }
                )
                
                if (uploadResult.isSuccess) {
                    val imageUrl = uploadResult.getOrThrow()
                    
                    // 2. Generar thumbnail
                    _uploadStatus.value = "Generando miniatura..."
                    val thumbnailResult = imageService.generateThumbnail(
                        context = context,
                        imageUri = imageUri,
                        size = 200,
                        quality = 70
                    )
                    
                    val thumbnailUrl = if (thumbnailResult.isSuccess) {
                        // Subir thumbnail a Storage
                        val thumbnailUpload = imageService.uploadWithProgress(
                            file = thumbnailResult.getOrThrow(),
                            path = "products/thumbnails/${product.id}_thumb.jpg",
                            onProgress = { /* Progreso del thumbnail */ }
                        )
                        thumbnailUpload.getOrNull()
                    } else null
                    
                    // 3. Actualizar producto con nuevas URLs
                    val updatedProduct = product.copy(
                        photoUrl = imageUrl,
                        thumbnailUrl = thumbnailUrl
                    )
                    
                    // 4. Actualizar producto en base de datos
                    _uploadStatus.value = "Guardando cambios..."
                    inventoryRepository.updateProduct(updatedProduct)
                    
                    // Analytics
                    analyticsHelper.logProductUpdated(updatedProduct.name)
                    
                    _uploadStatus.value = "✅ Producto actualizado exitosamente"
                } else {
                    val error = uploadResult.exceptionOrNull()
                    _error.value = "Error subiendo imagen: ${error?.message}"
                    error?.let { crashlyticsHelper.recordException(it) }
                    crashlyticsHelper.log("Error subiendo nueva imagen de producto: ${product.name}")
                }
                
            } catch (e: Exception) {
                _error.value = "Error al actualizar producto: ${e.message}"
                crashlyticsHelper.recordException(e)
                crashlyticsHelper.log("Error actualizando producto con imagen: ${product.name}")
            } finally {
                _isLoading.value = false
                _isUploadingImage.value = false
                _uploadProgress.value = 0
                performanceHelper.stopTrace(trace)
                // Limpiar status después de 3 segundos
                viewModelScope.launch {
                    kotlinx.coroutines.delay(3000)
                    _uploadStatus.value = null
                }
            }
        }
    }
    
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                inventoryRepository.updateProduct(product)
                
                // Analytics
                analyticsHelper.logProductUpdated(product.name)
                
            } catch (e: Exception) {
                _error.value = "Error al actualizar producto: ${e.message}"
                crashlyticsHelper.recordException(e)
                crashlyticsHelper.log("Error actualizando producto: ${product.name}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Obtener nombre del producto antes de eliminarlo para analytics
                val product = inventoryRepository.getProductById(productId)
                val productName = product?.name ?: "Unknown"
                
                inventoryRepository.deleteProduct(productId)
                
                // Analytics
                analyticsHelper.logProductDeleted(productName)
                
            } catch (e: Exception) {
                _error.value = "Error al eliminar producto: ${e.message}"
                crashlyticsHelper.recordException(e)
                crashlyticsHelper.log("Error eliminando producto: $productId")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addStock(productId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Obtener el producto actual para calcular la nueva cantidad
                val product = inventoryRepository.getProductById(productId)
                if (product == null) {
                    _error.value = "Producto no encontrado"
                    return@launch
                }
                
                val newQuantity = product.stockQuantity + quantity
                inventoryRepository.updateProductStock(
                    productId = productId,
                    newQuantity = newQuantity,
                    reason = "MANUAL_ADDITION",
                    description = "Adición manual de $quantity unidades"
                )
                
            } catch (e: Exception) {
                _error.value = "Error al agregar stock: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    suspend fun getProduct(productId: String): Product? {
        return inventoryRepository.getProductById(productId)
    }
    
    fun clearSearchQuery() {
        _searchQuery.value = ""
    }
    
    fun clearCategoryFilter() {
        _selectedCategory.value = null
    }
}