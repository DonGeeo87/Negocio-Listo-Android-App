package com.negociolisto.app.ui.customers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.service.CommunicationService
import com.negociolisto.app.data.service.UsageLimitsService
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.domain.repository.CustomerRepository
import com.negociolisto.app.data.analytics.AnalyticsHelper
import com.negociolisto.app.data.analytics.CrashlyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 👥 VIEWMODEL DE CLIENTES CON HILT
 * 
 * Maneja la lógica de negocio para clientes.
 */
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val communicationService: CommunicationService,
    private val analyticsHelper: AnalyticsHelper,
    private val crashlyticsHelper: CrashlyticsHelper,
    private val usageLimitsService: UsageLimitsService
) : ViewModel() {
    
    val customers: StateFlow<List<Customer>> = customerRepository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())
    
    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                // Verificar límite antes de agregar
                val limitCheck = usageLimitsService.checkCustomerLimit()
                if (!limitCheck.canAdd) {
                    throw Exception(limitCheck.message ?: "Has alcanzado el límite de clientes permitidos.")
                }
                
                customerRepository.addCustomer(customer)
                
                // Analytics
                analyticsHelper.logCustomerAdded(customer.name)
                
            } catch (e: Exception) {
                crashlyticsHelper.recordException(e)
                crashlyticsHelper.log("Error agregando cliente: ${customer.name}")
                throw e // Re-lanzar para que la UI pueda mostrar el error
            }
        }
    }
    
    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                customerRepository.updateCustomer(customer)
            } catch (e: Exception) {
                crashlyticsHelper.recordException(e)
                crashlyticsHelper.log("Error actualizando cliente: ${customer.name}")
            }
        }
    }
    
    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                customerRepository.deleteCustomer(customerId)
            } catch (e: Exception) {
                crashlyticsHelper.recordException(e)
                crashlyticsHelper.log("Error eliminando cliente: $customerId")
            }
        }
    }
    
    fun searchCustomers(query: String): StateFlow<List<Customer>> {
        return customerRepository.searchCustomers(query)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    /**
     * 💬 ABRIR WHATSAPP CON CLIENTE
     */
    fun openWhatsApp(context: Context, customer: Customer, message: String = "") {
        customer.phone?.let { phone ->
            communicationService.openWhatsApp(context, phone, message)
        }
    }
    
    /**
     * 📞 LLAMAR A CLIENTE
     */
    fun callCustomer(context: Context, customer: Customer) {
        customer.phone?.let { phone ->
            communicationService.makePhoneCall(context, phone)
        }
    }
    
    /**
     * 📧 ENVIAR EMAIL A CLIENTE
     */
    fun emailCustomer(context: Context, customer: Customer, subject: String = "", body: String = "") {
        customer.email?.let { email ->
            communicationService.sendEmail(context, email, subject, body)
        }
    }
    
    /**
     * 🔍 VERIFICAR SI EXISTE CLIENTE POR TELÉFONO
     * Normaliza el teléfono removiendo espacios, guiones y otros caracteres especiales
     */
    suspend fun checkCustomerByPhone(phone: String, excludeCustomerId: String? = null): Customer? {
        val normalizedPhone = phone.replace(Regex("[^0-9+]"), "").trim()
        if (normalizedPhone.isBlank()) return null
        
        // Buscar en todos los clientes ya que el teléfono puede tener diferentes formatos
        val allCustomers = customers.value
        val existing = allCustomers.firstOrNull { customer ->
            customer.phone?.replace(Regex("[^0-9+]"), "")?.trim() == normalizedPhone
        }
        return if (existing != null && existing.id != excludeCustomerId) existing else null
    }
    
    /**
     * 🔍 VERIFICAR SI EXISTE CLIENTE POR EMAIL
     * Normaliza el email a minúsculas
     */
    suspend fun checkCustomerByEmail(email: String, excludeCustomerId: String? = null): Customer? {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank()) return null
        
        val existing = customerRepository.getCustomerByEmail(normalizedEmail)
        return if (existing != null && existing.id != excludeCustomerId) existing else null
    }
}
