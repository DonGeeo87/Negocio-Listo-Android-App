package com.negociolisto.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.negociolisto.app.domain.model.StockMovement
import com.negociolisto.app.domain.model.StockMovementType
import com.negociolisto.app.domain.model.StockMovementReason
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

/**
 * 📊 ENTIDAD DE MOVIMIENTO DE STOCK PARA ROOM
 * 
 * Representa un movimiento de stock en la base de datos local.
 * Registra todas las entradas y salidas de inventario.
 */
@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["timestamp"]),
        Index(value = ["movementType"]),
        Index(value = ["reason"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey
    val id: String,
    val productId: String,
    val movementType: String, // StockMovementType.name
    val quantity: Int,
    val reason: String, // StockMovementReason.name
    val description: String?,
    val referenceId: String?,
    val unitCost: Double?,
    val previousStock: Int,
    val newStock: Int,
    val userId: String?,
    val timestamp: Long, // Timestamp en milisegundos
    val notes: String?
)

/**
 * 🔄 EXTENSIONES PARA CONVERSIÓN
 */
fun StockMovementEntity.toDomain(): StockMovement = StockMovement(
    id = id,
    productId = productId,
    movementType = StockMovementType.valueOf(movementType),
    quantity = quantity,
    reason = StockMovementReason.valueOf(reason),
    description = description,
    referenceId = referenceId,
    unitCost = unitCost,
    previousStock = previousStock,
    newStock = newStock,
    userId = userId,
    timestamp = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault()),
    notes = notes
)

fun StockMovement.toEntity(): StockMovementEntity = StockMovementEntity(
    id = id,
    productId = productId,
    movementType = movementType.name,
    quantity = quantity,
    reason = reason.name,
    description = description,
    referenceId = referenceId,
    unitCost = unitCost,
    previousStock = previousStock,
    newStock = newStock,
    userId = userId,
    timestamp = timestamp.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
    notes = notes
)