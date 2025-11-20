package com.negociolisto.app.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.InvoiceRepositoryMock
import com.negociolisto.app.domain.model.Invoice
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvoiceViewModelSimple : ViewModel() {
    val invoices: StateFlow<List<Invoice>> = InvoiceRepositoryMock.getInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addInvoice(invoice: Invoice) { viewModelScope.launch { InvoiceRepositoryMock.addInvoice(invoice) } }
    fun updateInvoice(invoice: Invoice) { viewModelScope.launch { InvoiceRepositoryMock.updateInvoice(invoice) } }
    fun deleteInvoice(id: String) { viewModelScope.launch { InvoiceRepositoryMock.deleteInvoice(id) } }
}


