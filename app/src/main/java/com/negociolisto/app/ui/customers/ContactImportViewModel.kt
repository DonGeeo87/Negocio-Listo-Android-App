package com.negociolisto.app.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.service.ContactImportService
import com.negociolisto.app.data.service.ContactInfo
import com.negociolisto.app.data.service.ContactPaginationResult
import com.negociolisto.app.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🧠 VIEWMODEL PARA IMPORTACIÓN DE CONTACTOS
 * 
 * Maneja la lógica de negocio para importar contactos desde la agenda del teléfono
 */
@HiltViewModel
class ContactImportViewModel @Inject constructor(
    private val contactImportService: ContactImportService,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactImportUiState())
    val uiState: StateFlow<ContactImportUiState> = _uiState.asStateFlow()

    /**
     * 📱 CARGAR CONTACTOS
     * 
     * Carga contactos con paginación y filtros de búsqueda
     */
    fun loadContacts(searchQuery: String? = null, page: Int = 0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val result = contactImportService.getContactsPaginated(
                    page = page,
                    pageSize = _uiState.value.pageSize,
                    searchQuery = searchQuery
                )
                
                if (result.isSuccess) {
                    val paginationResult = result.getOrNull()
                    if (paginationResult != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            contacts = paginationResult.contacts,
                            currentPage = paginationResult.currentPage,
                            totalPages = paginationResult.totalPages,
                            totalCount = paginationResult.totalCount,
                            hasNextPage = paginationResult.hasNextPage,
                            hasPreviousPage = paginationResult.hasPreviousPage,
                            searchQuery = searchQuery,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No se pudieron cargar los contactos"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error inesperado"
                )
            }
        }
    }

    /**
     * 🔍 BUSCAR CONTACTOS
     * 
     * Busca contactos con el query proporcionado
     */
    fun searchContacts(query: String) {
        loadContacts(searchQuery = query, page = 0)
    }

    /**
     * 📄 CARGAR PÁGINA SIGUIENTE
     */
    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.hasNextPage && !currentState.isLoading) {
            loadContacts(
                searchQuery = currentState.searchQuery,
                page = currentState.currentPage + 1
            )
        }
    }

    /**
     * 📄 CARGAR PÁGINA ANTERIOR
     */
    fun loadPreviousPage() {
        val currentState = _uiState.value
        if (currentState.hasPreviousPage && !currentState.isLoading) {
            loadContacts(
                searchQuery = currentState.searchQuery,
                page = currentState.currentPage - 1
            )
        }
    }

    /**
     * 📄 IR A PÁGINA ESPECÍFICA
     */
    fun goToPage(page: Int) {
        val currentState = _uiState.value
        if (page >= 0 && page < currentState.totalPages && !currentState.isLoading) {
            loadContacts(
                searchQuery = currentState.searchQuery,
                page = page
            )
        }
    }

    /**
     * 🔄 LIMPIAR BÚSQUEDA
     */
    fun clearSearch() {
        loadContacts(searchQuery = null, page = 0)
    }

    /**
     * ✅ TOGGLE SELECCIÓN DE CONTACTO
     * 
     * Agrega o quita un contacto de la selección
     */
    fun toggleContactSelection(contact: ContactInfo) {
        val currentSelected = _uiState.value.selectedContacts.toMutableSet()
        
        if (currentSelected.contains(contact)) {
            currentSelected.remove(contact)
        } else {
            currentSelected.add(contact)
        }
        
        _uiState.value = _uiState.value.copy(
            selectedContacts = currentSelected.toList()
        )
    }

    /**
     * 📥 IMPORTAR CONTACTOS SELECCIONADOS
     * 
     * Convierte los contactos seleccionados en clientes y los guarda
     */
    fun importSelectedContacts() {
        viewModelScope.launch {
            val selectedContacts = _uiState.value.selectedContacts
            if (selectedContacts.isEmpty()) return@launch
            
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                error = null
            )
            
            try {
                // Convertir contactos a clientes
                val customers = contactImportService.convertContactsToCustomers(selectedContacts)
                
                // Guardar clientes en la base de datos
                var importedCount = 0
                customers.forEach { customer ->
                    try {
                        customerRepository.addCustomer(customer)
                        importedCount++
                    } catch (e: Exception) {
                        println("⚠️ Error importando cliente ${customer.name}: ${e.message}")
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importedCount = importedCount,
                    showSuccessDialog = true,
                    selectedContacts = emptyList() // Limpiar selección
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = e.message ?: "Error importando contactos"
                )
            }
        }
    }

    /**
     * ❌ ESTABLECER ERROR
     */
    fun setError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    /**
     * ✅ DISMISS DIALOGO DE ÉXITO
     */
    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(
            showSuccessDialog = false,
            importedCount = 0
        )
    }
}

/**
 * 📊 ESTADO DE LA UI DE IMPORTACIÓN DE CONTACTOS
 */
data class ContactImportUiState(
    val isLoading: Boolean = false,
    val isImporting: Boolean = false,
    val contacts: List<ContactInfo> = emptyList(),
    val selectedContacts: List<ContactInfo> = emptyList(),
    val error: String? = null,
    val showSuccessDialog: Boolean = false,
    val importedCount: Int = 0,
    // Propiedades de paginación
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalCount: Int = 0,
    val pageSize: Int = 10,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val searchQuery: String? = null
)
