package com.negociolisto.app.data.repository

import com.negociolisto.app.data.local.dao.ExpenseDao
import com.negociolisto.app.data.local.entity.toDomain
import com.negociolisto.app.data.local.entity.toEntity
import com.negociolisto.app.data.remote.firebase.FirebaseBackupRepository
import com.negociolisto.app.domain.model.Expense
import com.negociolisto.app.domain.model.ExpenseCategory
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 💸 IMPLEMENTACIÓN DEL REPOSITORIO DE GASTOS
 * 
 * Maneja los datos de gastos usando Room Database con sincronización automática a Firebase.
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val firebaseBackupRepository: FirebaseBackupRepository,
    private val authRepository: AuthRepository
) : ExpenseRepository {
    
    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> {
        return expenseDao.getExpensesByCategory(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getExpensesByDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<Expense>> {
        val startMillis = start.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = end.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        return expenseDao.getExpensesByDateRange(startMillis, endMillis).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getMonthlyTotals(): Flow<Map<ExpenseCategory, Double>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.groupBy { ExpenseCategory.valueOf(it.category) }
                .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
        }
    }
    
    override suspend fun getExpenseById(id: String): Expense? {
        return expenseDao.getExpenseById(id)?.toDomain()
    }
    
    override suspend fun addExpense(expense: Expense) {
        // Guardar localmente primero
        expenseDao.insertExpense(expense.toEntity())
        
        // Sincronizar automáticamente con Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val expenseEntity = expense.toEntity()
                    val expenseMap = hashMapOf<String, Any?>(
                        "id" to expenseEntity.id,
                        "description" to expenseEntity.description,
                        "amount" to expenseEntity.amount,
                        "category" to expenseEntity.category,
                        "date" to expenseEntity.date,
                        "notes" to (expenseEntity.notes ?: ""),
                        "supplier" to (expenseEntity.supplier ?: ""),
                        "receiptNumber" to (expenseEntity.receiptNumber ?: ""),
                        "status" to expenseEntity.status,
                        "createdAt" to expenseEntity.createdAt,
                        "updatedAt" to expenseEntity.updatedAt
                    )
                    
                    // Usar el método de sincronización directa
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore
                        .collection("users/${currentUser.id}/expenses")
                        .document(expenseEntity.id)
                        .set(expenseMap)
                        .await()
                    
                    println("✅ Gasto ${expense.id} sincronizado automáticamente con Firebase")
                    
                    // Actualizar metadata de backup
                    firebaseBackupRepository.updateBackupMetadata(currentUser.id)
                }
            } catch (e: Exception) {
                println("⚠️ Error sincronizando gasto con Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }
    
    override suspend fun updateExpense(expense: Expense) {
        // Actualizar localmente primero
        expenseDao.updateExpense(expense.toEntity())
        
        // Sincronizar automáticamente con Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val expenseEntity = expense.toEntity()
                    val expenseMap = hashMapOf<String, Any?>(
                        "id" to expenseEntity.id,
                        "description" to expenseEntity.description,
                        "amount" to expenseEntity.amount,
                        "category" to expenseEntity.category,
                        "date" to expenseEntity.date,
                        "notes" to (expenseEntity.notes ?: ""),
                        "supplier" to (expenseEntity.supplier ?: ""),
                        "receiptNumber" to (expenseEntity.receiptNumber ?: ""),
                        "status" to expenseEntity.status,
                        "createdAt" to expenseEntity.createdAt,
                        "updatedAt" to expenseEntity.updatedAt
                    )
                    
                    // Usar el método de sincronización directa
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore
                        .collection("users/${currentUser.id}/expenses")
                        .document(expenseEntity.id)
                        .set(expenseMap)
                        .await()
                    
                    println("✅ Gasto ${expense.id} actualizado automáticamente en Firebase")
                    
                    // Actualizar metadata de backup
                    firebaseBackupRepository.updateBackupMetadata(currentUser.id)
                }
            } catch (e: Exception) {
                println("⚠️ Error sincronizando actualización de gasto con Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }
    
    override suspend fun deleteExpense(id: String) {
        // Eliminar localmente primero
        expenseDao.deleteExpenseById(id)
        
        // Eliminar de Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    // Usar el método de sincronización directa
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore
                        .collection("users/${currentUser.id}/expenses")
                        .document(id)
                        .delete()
                        .await()
                    
                    println("✅ Gasto $id eliminado automáticamente de Firebase")
                    
                    // Actualizar metadata de backup
                    firebaseBackupRepository.updateBackupMetadata(currentUser.id)
                }
            } catch (e: Exception) {
                println("⚠️ Error eliminando gasto de Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }
}
