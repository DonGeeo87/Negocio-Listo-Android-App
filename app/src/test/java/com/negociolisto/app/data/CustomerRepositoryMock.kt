package com.negociolisto.app.data

import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

object CustomerRepositoryMock : CustomerRepository {
    private val customers = MutableStateFlow<List<Customer>>(emptyList())

    override fun getAllCustomers(): Flow<List<Customer>> = customers.asStateFlow()

    override fun searchCustomers(query: String): Flow<List<Customer>> =
        customers.asStateFlow().map { list ->
            val q = query.trim().lowercase()
            if (q.isEmpty()) list else list.filter {
                it.name.lowercase().contains(q) ||
                (it.phone?.lowercase()?.contains(q) == true) ||
                (it.email?.lowercase()?.contains(q) == true)
            }
        }

    override suspend fun getCustomerById(id: String): Customer? = customers.value.firstOrNull { it.id == id }

    override suspend fun addCustomer(customer: Customer) {
        customers.update { it + customer }
    }

    override suspend fun updateCustomer(customer: Customer) {
        customers.update { list -> list.map { if (it.id == customer.id) customer else it } }
    }

    override suspend fun deleteCustomer(id: String) {
        customers.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun updateCustomerPurchases(customerId: String, totalPurchases: Double, lastPurchaseDate: kotlinx.datetime.LocalDateTime?) {
        customers.update { list ->
            list.map { customer ->
                if (customer.id == customerId) {
                    customer.copy(
                        totalPurchases = totalPurchases,
                        lastPurchaseDate = lastPurchaseDate
                    )
                } else {
                    customer
                }
            }
        }
    }

    private fun seed(name: String, phone: String?, email: String?): Customer {
        return Customer(
            id = UUID.randomUUID().toString(),
            name = name,
            phone = phone,
            email = email,
            address = listOf("Santiago", "Providencia", "Las Condes", "Ñuñoa").random() + ", Chile",
            notes = "Cliente frecuente",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            totalPurchases = 0.0, // Cliente nuevo sin compras
            lastPurchaseDate = null
        )
    }

    private fun seedWithId(id: String, name: String, phone: String?, email: String?): Customer {
        val totalPurchases = when (id) {
            "CUST_1" -> 9970.0  // Juan Pérez
            "CUST_2" -> 6360.0  // María García
            "CUST_3" -> 3370.0  // Panadería La Espiga
            else -> 0.0
        }
        
        return Customer(
            id = id,
            name = name,
            phone = phone,
            email = email,
            address = listOf("Santiago", "Providencia", "Las Condes", "Ñuñoa").random() + ", Chile",
            notes = "Cliente frecuente",
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            totalPurchases = totalPurchases,
            lastPurchaseDate = if (totalPurchases > 0) Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) else null
        )
    }

}


