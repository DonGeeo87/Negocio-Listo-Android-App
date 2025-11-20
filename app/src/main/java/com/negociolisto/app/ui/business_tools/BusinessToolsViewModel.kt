package com.negociolisto.app.ui.business_tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.repository.SalesRepository
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 🛠️ VIEWMODEL DE HERRAMIENTAS DE NEGOCIO
 * 
 * Maneja la lógica de negocio para las herramientas integradas.
 * Proporciona acceso a los datos necesarios para las calculadoras.
 */
@HiltViewModel
class BusinessToolsViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val inventoryRepository: InventoryRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    
    val sales = salesRepository.getSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val products = inventoryRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val expenses = expenseRepository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

