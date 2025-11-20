package com.negociolisto.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.InspirationTip
import com.negociolisto.app.domain.model.TimeOfDay
import com.negociolisto.app.domain.model.TipCategory
import com.negociolisto.app.domain.repository.InspirationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🎯 VIEWMODEL PARA LA CAJA SORPRESA DE TIPS
 * 
 * Maneja el estado y la lógica de negocio para mostrar tips
 * de inspiración dinámicos según la hora del día.
 */
@HiltViewModel
class InspirationBoxViewModel @Inject constructor(
    private val inspirationRepository: InspirationRepository
) : ViewModel() {

    private val _currentTip = MutableStateFlow<InspirationTip?>(null)
    val currentTip: StateFlow<InspirationTip?> = _currentTip.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentTimeOfDay = MutableStateFlow(TimeOfDay.getCurrentTimeOfDay())
    val currentTimeOfDay: StateFlow<TimeOfDay> = _currentTimeOfDay.asStateFlow()

    private var lastUsedCategory: TipCategory? = null

    init {
        initializeTips()
    }

    /**
     * 🌱 INICIALIZAR TIPS
     * 
     * Carga el primer tip y asegura que la base de datos esté poblada.
     */
    private fun initializeTips() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Asegurar que la base de datos tenga datos iniciales
                inspirationRepository.initializeIfEmpty()
                
                // Obtener el primer tip
                getNewRandomTip()
            } catch (e: Exception) {
                // En caso de error, mostrar un tip de respaldo
                _currentTip.value = createFallbackTip()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 🎲 OBTENER NUEVO TIP ALEATORIO
     * 
     * Obtiene un nuevo tip aleatorio, evitando repetir la categoría anterior.
     */
    fun getNewRandomTip() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val timeOfDay = TimeOfDay.getCurrentTimeOfDay()
                _currentTimeOfDay.value = timeOfDay
                
                val newTip = inspirationRepository.getRandomTip(
                    timeOfDay = timeOfDay,
                    excludeCategory = lastUsedCategory
                )
                
                if (newTip != null) {
                    // Marcar el tip anterior como usado
                    _currentTip.value?.let { previousTip ->
                        if (previousTip.id != 0L) {
                            inspirationRepository.markTipAsUsed(previousTip.id)
                        }
                    }
                    
                    _currentTip.value = newTip
                    lastUsedCategory = newTip.category
                } else {
                    // Si no hay tips disponibles, crear uno de respaldo
                    _currentTip.value = createFallbackTip()
                }
            } catch (e: Exception) {
                _currentTip.value = createFallbackTip()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 📊 OBTENER ESTADÍSTICAS
     * 
     * Obtiene información sobre los tips disponibles para el horario actual.
     */
    fun getTipStatistics(): kotlinx.coroutines.flow.Flow<Pair<Int, Int>> {
        return kotlinx.coroutines.flow.flow {
            val timeOfDay = TimeOfDay.getCurrentTimeOfDay()
            val stats = inspirationRepository.getTipStatistics(timeOfDay)
            emit(stats)
        }
    }

    /**
     * 🔄 REFRESCAR HORARIO
     * 
     * Actualiza el horario actual y obtiene un tip apropiado.
     */
    fun refreshTimeOfDay() {
        val newTimeOfDay = TimeOfDay.getCurrentTimeOfDay()
        if (newTimeOfDay != _currentTimeOfDay.value) {
            _currentTimeOfDay.value = newTimeOfDay
            getNewRandomTip()
        }
    }

    /**
     * 🆘 CREAR TIP DE RESPALDO
     * 
     * Crea un tip de respaldo en caso de error o cuando no hay datos.
     */
    private fun createFallbackTip(): InspirationTip {
        val timeOfDay = TimeOfDay.getCurrentTimeOfDay()
        return when (timeOfDay) {
            TimeOfDay.DAWN -> InspirationTip(
                content = "La madrugada es de los emprendedores. ¡Aprovecha la tranquilidad para planificar tu día!",
                category = TipCategory.MOTIVATION,
                timeOfDay = timeOfDay
            )
            TimeOfDay.MORNING -> InspirationTip(
                content = "El éxito comienza con el primer cliente del día. ¡Dale la bienvenida con una sonrisa!",
                category = TipCategory.CUSTOMER_SERVICE,
                timeOfDay = timeOfDay
            )
            TimeOfDay.AFTERNOON -> InspirationTip(
                content = "Revisa tus números del mediodía. ¿Cómo van las ventas? ¿Qué puedes mejorar?",
                category = TipCategory.FINANCES,
                timeOfDay = timeOfDay
            )
            TimeOfDay.NIGHT -> InspirationTip(
                content = "Celebra tus logros del día, por pequeños que sean. ¡Cada paso cuenta hacia el éxito!",
                category = TipCategory.MOTIVATION,
                timeOfDay = timeOfDay
            )
        }
    }
}











