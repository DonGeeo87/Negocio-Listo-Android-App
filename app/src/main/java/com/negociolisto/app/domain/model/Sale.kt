package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime
import java.util.UUID

enum class PaymentMethod(
    val displayName: String,
    val hasTransactionFee: Boolean
) {
    CASH("Efectivo", false),
    DEBIT_CARD("Tarjeta Débito", true),
    CREDIT_CARD("Tarjeta Crédito", true),
    BANK_TRANSFER("Transferencia", true),
    DIGITAL_WALLET("Billetera Digital", true),
    CHECK("Cheque", false),
    STORE_CREDIT("Crédito de Tienda", false)
}

enum class SaleStatus { ACTIVE, CANCELED }

data class SaleItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double
) {
    val lineTotal: Double get() = quantity * unitPrice
}

data class Sale(
    val id: String = UUID.randomUUID().toString(),
    val customerId: String? = null,
    val items: List<SaleItem>,
    val total: Double,
    val date: LocalDateTime,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val note: String? = null,
    val status: SaleStatus = SaleStatus.ACTIVE,
    val canceledAt: LocalDateTime? = null,
    val canceledReason: String? = null
) {
    fun getTotalItemCount(): Int = items.sumOf { it.quantity }
}


