package com.negociolisto.app.ui.sales

import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.domain.model.InvoiceItem
import com.negociolisto.app.domain.model.InvoiceTemplateType
import com.negociolisto.app.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeInvoiceRepo : InvoiceRepository {
    private val state = MutableStateFlow<List<Invoice>>(emptyList())
    override fun getInvoices(): Flow<List<Invoice>> = state.asStateFlow()
    override fun getInvoicesByDateRange(start: kotlinx.datetime.LocalDateTime, end: kotlinx.datetime.LocalDateTime): Flow<List<Invoice>> = state.asStateFlow()
    override fun getInvoicesByCustomer(customerId: String): Flow<List<Invoice>> = state.asStateFlow()
    override suspend fun getById(id: String): Invoice? = state.value.firstOrNull { it.id == id }
    override suspend fun addInvoice(invoice: Invoice) { state.value = state.value + invoice }
    override suspend fun updateInvoice(invoice: Invoice) { state.value = state.value.map { if (it.id == invoice.id) invoice else it } }
    override suspend fun deleteInvoice(id: String) { state.value = state.value.filterNot { it.id == id } }
}

class GenerateInvoiceFlowTest {
    @Test
    fun `crea factura desde venta`() = runBlocking {
        val repo = FakeInvoiceRepo()
        val vm = com.negociolisto.app.ui.invoices.InvoiceViewModel(repo)
        val items = listOf(InvoiceItem("Item", 2, 1000.0))
        val subtotal = items.sumOf { it.total }
        val tax = subtotal * 0.19
        val total = subtotal + tax
        val invoice = Invoice(
            number = "F001-0001", saleId = UUID.randomUUID().toString(), customerId = null,
            items = items, subtotal = subtotal, tax = tax, total = total, date = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
            template = InvoiceTemplateType.CLASSIC
        )
        vm.addInvoice(invoice)
        assertTrue(repo.getById(invoice.id) != null)
    }
}


