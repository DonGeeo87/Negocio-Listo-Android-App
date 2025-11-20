package com.negociolisto.app.ui.customers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🧠 VIEWMODEL PARA DETALLES DEL CLIENTE
 * 
 * Maneja la lógica de negocio para mostrar y interactuar con los detalles del cliente
 */
@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerDetailUiState())
    val uiState: StateFlow<CustomerDetailUiState> = _uiState.asStateFlow()

    /**
     * 📱 CARGAR CLIENTE
     * 
     * Carga la información completa del cliente por ID
     */
    fun loadCustomer(customerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val customer = customerRepository.getCustomerById(customerId)
                
                if (customer != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        customer = customer,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Cliente no encontrado"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando cliente"
                )
            }
        }
    }

    /**
     * 📞 LLAMAR AL CLIENTE
     * 
     * Abre la aplicación de teléfono para llamar al cliente
     */
    fun callCustomer(context: Context, customer: Customer) {
        try {
            val phoneNumber = customer.phone?.replace(" ", "")?.replace("-", "")?.replace("+", "")
            if (phoneNumber != null && phoneNumber.isNotBlank()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            println("❌ Error llamando al cliente: ${e.message}")
        }
    }

    /**
     * 📧 ENVIAR EMAIL AL CLIENTE
     * 
     * Abre la aplicación de email para enviar un email al cliente
     */
    fun sendEmail(context: Context, customer: Customer) {
        try {
            val email = customer.email
            if (email != null && email.isNotBlank()) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                    putExtra(Intent.EXTRA_SUBJECT, "Mensaje desde NegocioListo")
                    putExtra(Intent.EXTRA_TEXT, "Hola ${customer.name},\n\n")
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            println("❌ Error enviando email: ${e.message}")
        }
    }

    /**
     * 💬 ABRIR WHATSAPP
     * 
     * Abre WhatsApp para enviar un mensaje al cliente
     */
    fun openWhatsApp(context: Context, customer: Customer, message: String) {
        try {
            val phoneNumber = customer.phone?.replace(" ", "")?.replace("-", "")?.replace("+", "")
            if (phoneNumber != null && phoneNumber.isNotBlank()) {
                val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            println("❌ Error abriendo WhatsApp: ${e.message}")
        }
    }
}

/**
 * 📊 ESTADO DE LA UI DE DETALLES DEL CLIENTE
 */
data class CustomerDetailUiState(
    val isLoading: Boolean = false,
    val customer: Customer? = null,
    val error: String? = null
)
