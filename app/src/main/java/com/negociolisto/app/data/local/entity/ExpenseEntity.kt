package com.negociolisto.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.negociolisto.app.domain.model.Expense
import com.negociolisto.app.domain.model.ExpenseCategory
import com.negociolisto.app.domain.model.ExpenseStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

/**
 * 💸 ENTIDAD DE GASTO PARA ROOM
 */
@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["category"]),
        Index(value = ["date"]),
        Index(value = ["status"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val description: String,
    val amount: Double,
    val category: String, // ExpenseCategory.name
    val date: Long,
    val notes: String?,
    val supplier: String?,
    val receiptNumber: String?,
    val status: String, // ExpenseStatus.name
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 🔄 EXTENSIONES PARA CONVERSIÓN
 */
fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    description = description,
    amount = amount,
    category = ExpenseCategory.valueOf(category),
    date = Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault()),
    notes = notes,
    supplier = supplier,
    receiptNumber = receiptNumber,
    status = ExpenseStatus.valueOf(status)
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    description = description,
    amount = amount,
    category = category.name,
    date = date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
    notes = notes,
    supplier = supplier,
    receiptNumber = receiptNumber,
    status = status.name,
    createdAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis()
)
