package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.Expense
import com.negociolisto.app.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>
    fun getExpensesByDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<Expense>>
    fun getMonthlyTotals(): Flow<Map<ExpenseCategory, Double>>
    suspend fun getExpenseById(id: String): Expense?
    suspend fun addExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
}


