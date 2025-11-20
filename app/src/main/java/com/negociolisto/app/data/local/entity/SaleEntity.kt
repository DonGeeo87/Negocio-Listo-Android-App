package com.negociolisto.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.domain.model.SaleItem
import com.negociolisto.app.domain.model.PaymentMethod
import com.negociolisto.app.domain.model.SaleStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

/**
 * 💰 ENTIDAD DE VENTA PARA ROOM
 */
@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["date"])
    ]
)
data class SaleEntity(
    @PrimaryKey
    val id: String,
    val customerId: String?,
    val items: String, // JSON serializado de List<SaleItem>
    val total: Double,
    val date: Long,
    val paymentMethod: String,
    val note: String?,
    val status: String,
    val canceledAt: Long?,
    val canceledReason: String?
)

/**
 * 🔄 EXTENSIONES PARA CONVERSIÓN
 */
private fun serializeItems(items: List<SaleItem>): String {
    return items.joinToString(separator = "||") { item ->
        listOf(
            item.productId,
            item.productName.replace("|", "/"),
            item.quantity.toString(),
            item.unitPrice.toString()
        ).joinToString("|")
    }
}

private fun deserializeItems(serialized: String): List<SaleItem> {
    if (serialized.isBlank()) return emptyList()
    return serialized.split("||").filter { it.isNotBlank() }.mapNotNull { token ->
        val parts = token.split("|")
        if (parts.size < 4) return@mapNotNull null
        val quantity = parts[2].toIntOrNull() ?: return@mapNotNull null
        val unitPrice = parts[3].toDoubleOrNull() ?: return@mapNotNull null
        SaleItem(
            productId = parts[0],
            productName = parts[1],
            quantity = quantity,
            unitPrice = unitPrice
        )
    }
}

fun SaleEntity.toDomain(): Sale = Sale(
    id = id,
    customerId = customerId,
    items = deserializeItems(items),
    total = total,
    date = Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault()),
    paymentMethod = PaymentMethod.valueOf(paymentMethod),
    note = note,
    status = SaleStatus.valueOf(status),
    canceledAt = canceledAt?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) },
    canceledReason = canceledReason
)

fun Sale.toEntity(): SaleEntity = SaleEntity(
    id = id,
    customerId = customerId,
    items = serializeItems(items),
    total = total,
    date = date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
    paymentMethod = paymentMethod.name,
    note = note,
    status = status.name,
    canceledAt = canceledAt?.toInstant(TimeZone.currentSystemDefault())?.toEpochMilliseconds(),
    canceledReason = canceledReason
)
