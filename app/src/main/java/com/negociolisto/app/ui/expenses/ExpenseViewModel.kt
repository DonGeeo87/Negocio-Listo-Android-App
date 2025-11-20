package com.negociolisto.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.Expense
import com.negociolisto.app.domain.model.ExpenseCategory
import com.negociolisto.app.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

/**
 * 💸 VIEWMODEL DE GASTOS CON HILT
 * 
 * Maneja la lógica de negocio para gastos.
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    
    val expenses: StateFlow<List<Expense>> = expenseRepository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())
    
    val monthlyTotals: StateFlow<Map<ExpenseCategory, Double>> = expenseRepository.getMonthlyTotals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyMap())
    
    fun getExpensesByCategory(category: ExpenseCategory): StateFlow<List<Expense>> {
        return expenseRepository.getExpensesByCategory(category)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())
    }
    
    fun getExpensesByDateRange(start: LocalDateTime, end: LocalDateTime): StateFlow<List<Expense>> {
        return expenseRepository.getExpensesByDateRange(start, end)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())
    }
    
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.addExpense(expense)
        }
    }
    
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.updateExpense(expense)
        }
    }
    
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expenseId)
        }
    }
}
