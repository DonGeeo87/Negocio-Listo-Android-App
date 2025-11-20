package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class InvoiceItem(
    val description: String,
    val quantity: Int,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

enum class InvoiceTemplateType(val displayName: String) {
    CLASSIC("Clásica"),
    MODERN("Moderna"),
    MINIMAL("Minimalista")
}

@Serializable
data class Invoice(
    val id: String = UUID.randomUUID().toString(),
    val number: String,
    val saleId: String? = null,
    val customerId: String? = null,
    val items: List<InvoiceItem>,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val date: LocalDateTime,
    val template: InvoiceTemplateType = InvoiceTemplateType.CLASSIC,
    val notes: String? = null
)


