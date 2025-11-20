package com.negociolisto.app.data

import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.domain.model.SaleItem
import com.negociolisto.app.domain.model.PaymentMethod
import com.negociolisto.app.domain.repository.SalesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object SalesRepositoryMock : SalesRepository {
    private val sales = MutableStateFlow<List<Sale>>(emptyList())

    override fun getSales(): Flow<List<Sale>> = sales.asStateFlow()

    override suspend fun recordSale(sale: Sale) {
        sales.update { it + sale }
    }

    override suspend fun cancelSale(id: String, reason: String?) {
        sales.update { list ->
            list.map { s -> if (s.id == id) s.copy(status = com.negociolisto.app.domain.model.SaleStatus.CANCELED, canceledReason = reason, canceledAt = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())) else s }
        }
    }

    override suspend fun deleteSale(id: String) {
        sales.update { it.filterNot { s -> s.id == id } }
    }

    fun recordQuickSale(productId: String, name: String, quantity: Int, unitPrice: Double, customerId: String? = null) {
        val item = SaleItem(productId, name, quantity, unitPrice)
        val sale = Sale(
            customerId = customerId,
            items = listOf(item),
            total = item.lineTotal,
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            paymentMethod = PaymentMethod.CASH
        )
        sales.update { it + sale }
    }

}


