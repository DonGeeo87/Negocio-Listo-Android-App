package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.Invoice
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface InvoiceRepository {
    fun getInvoices(): Flow<List<Invoice>>
    fun getInvoicesByDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<Invoice>>
    fun getInvoicesByCustomer(customerId: String): Flow<List<Invoice>>
    suspend fun getById(id: String): Invoice?
    suspend fun addInvoice(invoice: Invoice)
    suspend fun updateInvoice(invoice: Invoice)
    suspend fun deleteInvoice(id: String)
}


