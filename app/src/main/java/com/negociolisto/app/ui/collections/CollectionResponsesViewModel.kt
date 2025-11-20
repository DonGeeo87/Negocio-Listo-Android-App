package com.negociolisto.app.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.CollectionResponse
import com.negociolisto.app.domain.model.OrderStatus
import com.negociolisto.app.domain.repository.CollectionResponseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionResponsesViewModel @Inject constructor(
    private val responseRepository: CollectionResponseRepository
) : ViewModel() {

    private val _responses = MutableStateFlow<List<CollectionResponse>>(emptyList())
    val responses: StateFlow<List<CollectionResponse>> = _responses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentCollectionId: String? = null
    private var responsesJob: kotlinx.coroutines.Job? = null

    fun loadResponses(collectionId: String) {
        if (currentCollectionId == collectionId && _responses.value.isNotEmpty()) return
        currentCollectionId = collectionId

        // Cancelar suscripción previa si existe para evitar listeners duplicados
        responsesJob?.cancel()

        responsesJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                responseRepository.getResponses(collectionId).collect { list ->
                    _responses.value = list
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar pedidos: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}


