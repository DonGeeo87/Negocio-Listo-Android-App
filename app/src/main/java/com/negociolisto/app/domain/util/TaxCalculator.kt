package com.negociolisto.app.domain.util

import com.negociolisto.app.domain.model.InvoiceItem
import com.negociolisto.app.domain.model.SaleItem

data class TaxBreakdown(
    val subtotal: Double,
    val tax: Double,
    val total: Double
)

object TaxCalculator {
    private const val DEFAULT_TAX_RATE = 0.19

    fun fromInvoiceItems(
        items: List<InvoiceItem>,
        priceIsNet: Boolean,
        taxRate: Double = DEFAULT_TAX_RATE
    ): TaxBreakdown {
        val linesTotal = items.sumOf { it.total }
        return fromLineTotal(linesTotal, priceIsNet, taxRate)
    }

    fun fromSaleItems(
        items: List<SaleItem>,
        priceIsNet: Boolean,
        taxRate: Double = DEFAULT_TAX_RATE
    ): TaxBreakdown {
        val linesTotal = items.sumOf { it.lineTotal }
        return fromLineTotal(linesTotal, priceIsNet, taxRate)
    }

    fun fromLineTotal(
        lineTotal: Double,
        priceIsNet: Boolean,
        taxRate: Double = DEFAULT_TAX_RATE
    ): TaxBreakdown {
        return if (priceIsNet) {
            val subtotal = lineTotal
            val tax = (subtotal * taxRate).coerceAtLeast(0.0)
            TaxBreakdown(subtotal, tax, subtotal + tax)
        } else {
            val total = lineTotal
            val subtotal = (total / (1 + taxRate)).coerceAtLeast(0.0)
            val tax = total - subtotal
            TaxBreakdown(subtotal, tax, total)
        }
    }
}

