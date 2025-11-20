package com.negociolisto.app.data

import com.negociolisto.app.domain.model.BusinessMetrics
import com.negociolisto.app.domain.model.TopProduct
import com.negociolisto.app.domain.model.TopCustomer
import com.negociolisto.app.domain.model.NextEvent
import com.negociolisto.app.ui.components.Formatters
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.negociolisto.app.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

object DashboardRepositoryMock : DashboardRepository {
    override fun getBusinessMetrics(): Flow<BusinessMetrics> {
        val salesFlow = SalesRepositoryMock.getSales()
        val productsFlow = InventoryRepositoryMock.getAllProducts()
        val customersFlow = CustomerRepositoryMock.getAllCustomers()

        return combine(salesFlow, productsFlow, customersFlow) { sales, products, customers ->
            val totalSales = sales.sumOf { it.total }
            val totalExpenses = 0.0 // Simplificado por ahora
            val lowStockCount = products.count { it.hasLowStock() }

            val quantitiesByProduct = mutableMapOf<String, Int>()
            val amountByProduct = mutableMapOf<String, Double>()
            val productNames = mutableMapOf<String, String>()
            
            sales.forEach { sale ->
                sale.items.forEach { item ->
                    quantitiesByProduct[item.productId] = (quantitiesByProduct[item.productId] ?: 0) + item.quantity
                    amountByProduct[item.productId] = (amountByProduct[item.productId] ?: 0.0) + item.lineTotal
                    // Usar el nombre del producto del item de venta directamente
                    productNames[item.productId] = item.productName
                }
            }
            
            val topProducts = quantitiesByProduct.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { (pid, qty) ->
                    TopProduct(pid, productNames[pid] ?: "Producto desconocido", qty, amountByProduct[pid] ?: 0.0)
                }

            // Top clientes (consistente con la tarjeta de Clientes: usa totalPurchases)
            val topCustomers = customers
                .sortedByDescending { it.totalPurchases }
                .take(3)
                .map { c -> TopCustomer(c.id, c.name, c.totalPurchases) }

            // Próximos eventos (simplificado)
            val nextEvents = emptyList<NextEvent>()

            val grossMargin = totalSales - totalExpenses
            val grossMarginPercent = if (totalSales > 0) (grossMargin / totalSales) * 100.0 else 0.0

            BusinessMetrics(
                totalSales = totalSales,
                totalExpenses = totalExpenses,
                grossMargin = grossMargin,
                grossMarginPercent = grossMarginPercent,
                lowStockCount = lowStockCount,
                topProducts = topProducts,
                topCustomers = topCustomers,
                nextEvents = nextEvents
            )
        }
    }
}


