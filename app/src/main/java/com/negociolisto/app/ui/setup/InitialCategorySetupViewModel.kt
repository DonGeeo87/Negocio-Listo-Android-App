package com.negociolisto.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.CustomCategory
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.data.local.UiPreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * 🏗️ VIEWMODEL PARA CONFIGURACIÓN INICIAL DE CATEGORÍAS
 * 
 * Maneja la lógica de configuración inicial de categorías personalizadas,
 * incluyendo auto-asignación de iconos y colores.
 */
@HiltViewModel
class InitialCategorySetupViewModel @Inject constructor(
    private val categoryRepository: CustomCategoryRepository,
    private val uiPreferencesStore: UiPreferencesStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InitialCategorySetupUiState())
    val uiState: StateFlow<InitialCategorySetupUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryPreview>>(emptyList())
    val categories: StateFlow<List<CategoryPreview>> = _categories.asStateFlow()

    // Obtener el userId actual del usuario autenticado
    private val currentUserIdFlow: StateFlow<String?> = authRepository.currentUser
        .map { it?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // Verificar si ya existen categorías cuando el usuario esté disponible
        viewModelScope.launch {
            currentUserIdFlow.collect { userId ->
                if (userId != null) {
                    try {
                        val existingCategories = categoryRepository.getActiveCategoriesByUser(userId)
                            .first()
                        if (existingCategories.isNotEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                hasError = true,
                                errorMessage = "Ya tienes ${existingCategories.size} categorías configuradas. Puedes gestionarlas desde Ajustes."
                            )
                        }
                    } catch (e: Exception) {
                        // Silenciar error - es solo validación
                    }
                }
            }
        }
    }

    /**
     * 📝 ACTUALIZAR INPUT DE CATEGORÍAS
     */
    fun updateCategoriesInput(input: String) {
        _uiState.value = _uiState.value.copy(
            categoriesInput = input,
            hasError = false,
            errorMessage = ""
        )
        
        parseCategories(input)
    }

    /**
     * 🔍 PARSEAR CATEGORÍAS DESDE INPUT
     */
    private fun parseCategories(input: String) {
        if (input.isBlank()) {
            _categories.value = emptyList()
            return
        }

        val categoryNames = input.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val parsedCategories = categoryNames.map { name ->
            val (icon, color) = autoAssignIconAndColor(name)
            CategoryPreview(
                name = name,
                icon = icon,
                colorHex = color
            )
        }

        _categories.value = parsedCategories

        // Validar cantidad
        when {
            parsedCategories.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    hasError = false,
                    errorMessage = ""
                )
            }
            parsedCategories.size < 5 -> {
                _uiState.value = _uiState.value.copy(
                    hasError = true,
                    errorMessage = "Necesitas al menos 5 categorías. Agregaste ${parsedCategories.size}."
                )
            }
            else -> {
                _uiState.value = _uiState.value.copy(
                    hasError = false,
                    errorMessage = ""
                )
            }
        }
    }

    /**
     * 🎨 AUTO-ASIGNAR ICONO Y COLOR BASADO EN NOMBRE
     */
    private fun autoAssignIconAndColor(categoryName: String): Pair<String, String> {
        val normalizedName = categoryName.lowercase().trim()
        
        return when {
            normalizedName.contains("bebida") || normalizedName.contains("drink") -> "🥤" to "#2196F3"
            normalizedName.contains("pan") || normalizedName.contains("bread") -> "🍞" to "#FF9800"
            normalizedName.contains("carn") || normalizedName.contains("meat") -> "🥩" to "#F44336"
            normalizedName.contains("fruta") || normalizedName.contains("fruit") -> "🍎" to "#8BC34A"
            normalizedName.contains("verd") || normalizedName.contains("vegeta") -> "🥬" to "#4CAF50"
            normalizedName.contains("lact") || normalizedName.contains("dairy") -> "🥛" to "#4CAF50"
            normalizedName.contains("dulce") || normalizedName.contains("candy") -> "🍬" to "#E91E63"
            normalizedName.contains("limpieza") || normalizedName.contains("clean") -> "🧽" to "#00BCD4"
            normalizedName.contains("ropa") || normalizedName.contains("cloth") -> "👕" to "#9C27B0"
            normalizedName.contains("zapato") || normalizedName.contains("shoe") -> "👞" to "#795548"
            normalizedName.contains("electron") || normalizedName.contains("electr") -> "⚡" to "#FF5722"
            normalizedName.contains("ferrete") || normalizedName.contains("tool") -> "🔧" to "#607D8B"
            normalizedName.contains("juguete") || normalizedName.contains("toy") -> "🧸" to "#FF6F00"
            normalizedName.contains("libro") || normalizedName.contains("book") -> "📚" to "#3F51B5"
            normalizedName.contains("salud") || normalizedName.contains("health") -> "💊" to "#009688"
            normalizedName.contains("belleza") || normalizedName.contains("beauty") -> "💄" to "#F06292"
            normalizedName.contains("abarrote") || normalizedName.contains("grocery") -> "🛒" to "#795548"
            normalizedName.contains("farmacia") || normalizedName.contains("pharmacy") -> "💊" to "#009688"
            normalizedName.contains("papeleria") || normalizedName.contains("stationery") -> "📝" to "#3F51B5"
            normalizedName.contains("deporte") || normalizedName.contains("sport") -> "⚽" to "#4CAF50"
            normalizedName.contains("cocina") || normalizedName.contains("kitchen") -> "🍳" to "#FF5722"
            normalizedName.contains("baño") || normalizedName.contains("bathroom") -> "🚿" to "#00BCD4"
            normalizedName.contains("jardin") || normalizedName.contains("garden") -> "🌱" to "#8BC34A"
            normalizedName.contains("mascota") || normalizedName.contains("pet") -> "🐕" to "#FF9800"
            normalizedName.contains("auto") || normalizedName.contains("car") -> "🚗" to "#607D8B"
            normalizedName.contains("casa") || normalizedName.contains("home") -> "🏠" to "#9E9E9E"
            normalizedName.contains("oficina") || normalizedName.contains("office") -> "💼" to "#3F51B5"
            normalizedName.contains("musica") || normalizedName.contains("music") -> "🎵" to "#E91E63"
            normalizedName.contains("arte") || normalizedName.contains("art") -> "🎨" to "#9C27B0"
            normalizedName.contains("navidad") || normalizedName.contains("christmas") -> "🎄" to "#4CAF50"
            normalizedName.contains("halloween") -> "🎃" to "#FF9800"
            normalizedName.contains("cumple") || normalizedName.contains("birthday") -> "🎂" to "#F06292"
            normalizedName.contains("otro") || normalizedName.contains("other") -> "📦" to "#9E9E9E"
            else -> "📦" to "#9E9E9E" // Default
        }
    }

    /**
     * 💾 GUARDAR CATEGORÍAS Y CONTINUAR
     */
    fun saveCategoriesAndContinue(onComplete: () -> Unit) {
        val currentCategories = _categories.value
        
        if (currentCategories.size < 5) {
            _uiState.value = _uiState.value.copy(
                hasError = true,
                errorMessage = "Necesitas al menos 5 categorías para continuar."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val currentUserId = currentUserIdFlow.value
                if (currentUserId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasError = true,
                        errorMessage = "Usuario no autenticado. Por favor, inicia sesión primero."
                    )
                    return@launch
                }
                
                val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                
                // Verificar categorías existentes antes de guardar
                val existingCategories = categoryRepository.getActiveCategoriesByUser(currentUserId)
                    .first()
                
                if (existingCategories.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasError = true,
                        errorMessage = "Ya tienes categorías configuradas. Ve a Ajustes para gestionarlas."
                    )
                    return@launch
                }
                
                // Crear categorías personalizadas
                val customCategories = currentCategories.mapIndexed { index, preview ->
                    CustomCategory(
                        name = preview.name,
                        icon = preview.icon,
                        color = preview.colorHex,
                        description = "Categoría inicial configurada por el usuario",
                        createdAt = currentTime,
                        updatedAt = currentTime,
                        isActive = true,
                        userId = currentUserId, // Usar el userId real del usuario autenticado
                        sortOrder = index
                    )
                }

                // Guardar en repositorio
                customCategories.forEach { category ->
                    categoryRepository.addCategory(category)
                }

                // Marcar configuración como completada
                uiPreferencesStore.setInitialCategoriesConfigured(true)

                // Completar - NO llamar onComplete aquí, dejar que el InitialSetupScreen lo maneje
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCompleted = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasError = true,
                    errorMessage = "Error al guardar las categorías: ${e.message}"
                )
            }
        }
    }

    /**
     * ⏭️ SALTAR CONFIGURACIÓN
     */
    fun skipSetup(onComplete: () -> Unit) {
        viewModelScope.launch {
            uiPreferencesStore.setInitialCategoriesConfigured(true)
            onComplete()
        }
    }
}

/**
 * 📊 ESTADO DE LA UI
 */
data class InitialCategorySetupUiState(
    val categoriesInput: String = "",
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val isCompleted: Boolean = false
)

/**
 * 📋 MODELO DE VISTA PREVIA DE CATEGORÍA
 */
data class CategoryPreview(
    val name: String,
    val icon: String,
    val colorHex: String
)
