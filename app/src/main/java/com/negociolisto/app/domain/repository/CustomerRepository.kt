package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

/**
 * Contrato de acceso a clientes.
 */
interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    fun searchCustomers(query: String): Flow<List<Customer>>
    suspend fun getCustomerById(id: String): Customer?
    suspend fun addCustomer(customer: Customer)
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(id: String)
    suspend fun updateCustomerPurchases(customerId: String, totalPurchases: Double, lastPurchaseDate: LocalDateTime?)
    suspend fun getCustomerByPhone(phone: String): Customer?
    suspend fun getCustomerByEmail(email: String): Customer?
    
    /**
     * 📊 OBTENER TOTAL DE CLIENTES
     * 
     * Cuenta el total de clientes en el sistema.
     * 
     * @return Número total de clientes
     */
    suspend fun getTotalCustomerCount(): Int
}


