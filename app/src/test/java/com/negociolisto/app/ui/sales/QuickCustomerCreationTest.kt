package com.negociolisto.app.ui.sales

import com.negociolisto.app.data.repository.CustomerRepositoryImpl
import com.negociolisto.app.domain.model.Customer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class QuickCustomerCreationTest {
    @Test
    fun create_quick_customer_with_fields() = runBlocking {
        val repo = CustomerRepositoryImpl(object: com.negociolisto.app.data.local.dao.CustomerDao {
            private val list = mutableListOf<com.negociolisto.app.data.local.entity.CustomerEntity>()
            override fun getAllCustomers() = kotlinx.coroutines.flow.flowOf(list)
            override suspend fun getCustomerById(id: String) = list.firstOrNull { it.id == id }
            override suspend fun getCustomerByEmail(email: String) = list.firstOrNull { it.email == email }
            override suspend fun getCustomerByPhone(phone: String) = list.firstOrNull { it.phone == phone }
            override fun searchCustomers(query: String) = kotlinx.coroutines.flow.flowOf(list)
            override suspend fun insertCustomer(customer: com.negociolisto.app.data.local.entity.CustomerEntity) { list.add(customer) }
            override suspend fun updateCustomer(customer: com.negociolisto.app.data.local.entity.CustomerEntity) { /* no-op */ }
            override suspend fun deleteCustomerById(id: String) { list.removeIf { it.id == id } }
            override suspend fun getCustomerCount(): Int = list.size
        })
        val customer = Customer(
            id = UUID.randomUUID().toString(),
            name = "Juan",
            companyName = "Acme Ltda",
            phone = "+56 9 1111 1111",
            email = "juan@example.com",
            address = null,
            notes = null,
            createdAt = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
            totalPurchases = 0.0,
            lastPurchaseDate = null
        )
        repo.addCustomer(customer)
        val fetched = repo.getCustomerById(customer.id)!!
        assertEquals("Juan", fetched.name)
        assertEquals("Acme Ltda", fetched.companyName)
        assertEquals("juan@example.com", fetched.email)
        assertEquals("+56 9 1111 1111", fetched.phone)
    }
}
