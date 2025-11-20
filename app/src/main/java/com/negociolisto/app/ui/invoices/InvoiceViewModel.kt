package com.negociolisto.app.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    val invoices: StateFlow<List<Invoice>> = invoiceRepository.getInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())

    fun addInvoice(invoice: Invoice) {
        viewModelScope.launch {
            invoiceRepository.addInvoice(invoice)
        }
    }

    fun updateInvoice(invoice: Invoice) {
        viewModelScope.launch {
            invoiceRepository.updateInvoice(invoice)
        }
    }

    fun deleteInvoice(id: String) {
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(id)
        }
    }

    suspend fun getInvoiceBySaleId(saleId: String): Invoice? {
        return (invoiceRepository as com.negociolisto.app.data.repository.InvoiceRepositoryImpl)
            .getInvoiceBySaleId(saleId)
    }
    
    fun updateAllInvoiceTemplates(template: com.negociolisto.app.domain.model.InvoiceTemplateType) {
        viewModelScope.launch {
            (invoiceRepository as com.negociolisto.app.data.repository.InvoiceRepositoryImpl)
                .updateAllInvoiceTemplates(template)
        }
    }
    
    suspend fun getInvoiceBySaleIdOld(saleId: String): Invoice? {
        return invoices.value.firstOrNull { it.saleId == saleId }
    }
}
