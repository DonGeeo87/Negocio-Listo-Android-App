package com.negociolisto.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.CustomCategory
import com.negociolisto.app.domain.model.CustomCategoryFactory
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.negociolisto.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * 📂 VIEWMODEL DE GESTIÓN DE CATEGORÍAS
 * 
 * Maneja el estado y la lógica de la pantalla de gestión de categorías.
 */
@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val customCategoryRepository: CustomCategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // Estado de la UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()
    
    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog = _showEditDialog.asStateFlow()
    
    private val _editingCategory = MutableStateFlow<CustomCategory?>(null)
    val editingCategory = _editingCategory.asStateFlow()
    
    // UID en vivo del usuario autenticado
    private val currentUserIdFlow: StateFlow<String?> = authRepository.currentUser
        .map { it?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Categorías del usuario (reactivo al UID) - ordenadas alfabéticamente
    val categories: StateFlow<List<CustomCategory>> = currentUserIdFlow
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(emptyList()) else customCategoryRepository.getActiveCategoriesByUser(uid)
        }
        .map { list -> 
            list.filter { it.isActive }
                .sortedBy { it.name.lowercase() } // Ordenamiento alfabético
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        // El usuario debe crear sus propias categorías
        // No se inicializan categorías predeterminadas
    }
    
    /**
     * ➕ MOSTRAR DIÁLOGO DE AGREGAR CATEGORÍA
     */
    fun showAddCategoryDialog() {
        _showAddDialog.value = true
        _error.value = null
    }
    
    /**
     * ❌ OCULTAR DIÁLOGO DE AGREGAR CATEGORÍA
     */
    fun hideAddCategoryDialog() {
        _showAddDialog.value = false
    }
    
    /**
     * ✏️ MOSTRAR DIÁLOGO DE EDITAR CATEGORÍA
     */
    fun showEditCategoryDialog(category: CustomCategory) {
        _editingCategory.value = category
        _showEditDialog.value = true
        _error.value = null
    }
    
    /**
     * ❌ OCULTAR DIÁLOGO DE EDITAR CATEGORÍA
     */
    fun hideEditCategoryDialog() {
        _editingCategory.value = null
        _showEditDialog.value = false
    }
    
    /**
     * ➕ AGREGAR NUEVA CATEGORÍA
     */
    fun addCategory(name: String, icon: String, color: String, description: String?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val category = CustomCategoryFactory.createCustomCategory(
                    name = name,
                    icon = icon,
                    color = color,
                    description = description,
                    userId = currentUserIdFlow.value ?: "",
                    createdAt = now
                )
                
                if (category != null) {
                    customCategoryRepository.addCategory(category)
                    _showAddDialog.value = false
                } else {
                    _error.value = "Datos de categoría inválidos"
                }
            } catch (e: Exception) {
                _error.value = "Error al agregar categoría: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * ✏️ ACTUALIZAR CATEGORÍA
     */
    fun updateCategory(name: String, icon: String, color: String, description: String?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val editingCategory = _editingCategory.value
                if (editingCategory != null) {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val updatedCategory = editingCategory.copy(
                        name = name,
                        icon = icon,
                        color = color,
                        description = description,
                        updatedAt = now
                    )
                    
                    customCategoryRepository.updateCategory(updatedCategory)
                    _showEditDialog.value = false
                    _editingCategory.value = null
                } else {
                    _error.value = "No hay categoría seleccionada para editar"
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar categoría: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 🗑️ ELIMINAR CATEGORÍA
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                customCategoryRepository.deleteCategory(categoryId)
            } catch (e: Exception) {
                _error.value = "Error al eliminar categoría: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 🔄 ACTUALIZAR ORDEN DE CATEGORÍAS
     */
    fun updateCategoryOrder(categoryId: String, newOrder: Int) {
        viewModelScope.launch {
            try {
                customCategoryRepository.updateCategoryOrder(categoryId, newOrder)
            } catch (e: Exception) {
                _error.value = "Error al actualizar orden: ${e.message}"
            }
        }
    }
    
    /**
     * 🔄 REORDENAR CATEGORÍAS
     */
    fun reorderCategories(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                val currentCategories = categories.value.toMutableList()
                if (fromIndex in currentCategories.indices && toIndex in currentCategories.indices) {
                    val item = currentCategories.removeAt(fromIndex)
                    currentCategories.add(toIndex, item)
                    
                    // Actualizar el orden en la base de datos
                    currentCategories.forEachIndexed { index, category ->
                        customCategoryRepository.updateCategoryOrder(category.id, index)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error al reordenar categorías: ${e.message}"
            }
        }
    }
    
    /**
     * 🧹 LIMPIAR ERROR
     */
    fun clearError() {
        _error.value = null
    }
}
