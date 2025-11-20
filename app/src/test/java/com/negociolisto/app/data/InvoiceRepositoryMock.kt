package com.negociolisto.app.data

import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.domain.model.InvoiceItem
import com.negociolisto.app.domain.model.InvoiceTemplateType
import com.negociolisto.app.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

object InvoiceRepositoryMock : InvoiceRepository {
    private val invoices = MutableStateFlow<List<Invoice>>(emptyList())
    private var series: String = "F001"
    private var nextNumber: Int = 1

    override fun getInvoices(): Flow<List<Invoice>> = invoices.asStateFlow()

    override fun getInvoicesByDateRange(start: kotlinx.datetime.LocalDateTime, end: kotlinx.datetime.LocalDateTime): Flow<List<Invoice>> =
        invoices.asStateFlow().map { list -> list.filter { it.date >= start && it.date <= end } }

    override fun getInvoicesByCustomer(customerId: String): Flow<List<Invoice>> =
        invoices.asStateFlow().map { list -> list.filter { it.customerId == customerId } }

    override suspend fun getById(id: String): Invoice? = invoices.value.firstOrNull { it.id == id }

    override suspend fun addInvoice(invoice: Invoice) { invoices.update { it + invoice } }

    override suspend fun updateInvoice(invoice: Invoice) { invoices.update { list -> list.map { if (it.id == invoice.id) invoice else it } } }

    override suspend fun deleteInvoice(id: String) { invoices.update { it.filterNot { inv -> inv.id == id } } }

    fun getNextInvoiceNumber(): String {
        val num = "%04d".format(nextNumber)
        nextNumber += 1
        return "$series-$num"
    }

    private fun sample(number: String, customerId: String?, template: InvoiceTemplateType): Invoice {
        val items = listOf(
            InvoiceItem("Café Molido 500g", 1, 99.0),
            InvoiceItem("Manzanas Rojas 1kg", 2, 30.0)
        )
        val subtotal = items.sumOf { it.total }
        val tax = subtotal * 0.19
        val total = subtotal + tax
        return Invoice(
            id = UUID.randomUUID().toString(),
            number = number,
            saleId = null,
            customerId = customerId,
            items = items,
            subtotal = subtotal,
            tax = tax,
            total = total,
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            template = template,
            notes = null
        )
    }

}
