package com.negociolisto.app.data.repository

import com.negociolisto.app.domain.model.BusinessMetrics
import com.negociolisto.app.domain.model.DailySales
import com.negociolisto.app.domain.model.LowStockProduct
import com.negociolisto.app.domain.model.TopCustomer
import com.negociolisto.app.domain.model.TopProduct
import com.negociolisto.app.domain.model.CollectionStatus
import com.negociolisto.app.domain.repository.DashboardRepository
import com.negociolisto.app.domain.repository.CollectionRepository
import com.negociolisto.app.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.*
import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.domain.model.Expense
import com.negociolisto.app.domain.model.Collection
import com.negociolisto.app.domain.model.Invoice
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holder temporal para combinar flows
 */
private data class DataHolder(
    val sales: List<Sale>,
    val products: List<Product>,
    val customers: List<Customer>,
    val expenses: List<Expense>,
    val collections: List<Collection>
)

/**
 * 📊 IMPLEMENTACIÓN DEL REPOSITORIO DE DASHBOARD
 * 
 * Combina datos de múltiples repositorios para generar métricas de negocio.
 */
@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val salesRepository: SalesRepositoryImpl,
    private val inventoryRepository: InventoryRepositoryImpl,
    private val customerRepository: CustomerRepositoryImpl,
    private val expenseRepository: ExpenseRepositoryImpl,
    private val collectionRepository: CollectionRepository,
    private val invoiceRepository: InvoiceRepository,
) : DashboardRepository {
    
    override fun getBusinessMetrics(): Flow<BusinessMetrics> {
        val salesFlow = salesRepository.getSales()
        val productsFlow = inventoryRepository.getAllProducts()
        val customersFlow = customerRepository.getAllCustomers()
        val expensesFlow = expenseRepository.getAllExpenses()
        val collectionsFlow = collectionRepository.getCollections()
        val invoicesFlow = invoiceRepository.getInvoices()
        
        // Combinar primeros 5 flows
        val baseCombine = combine(
            salesFlow,
            productsFlow,
            customersFlow,
            expensesFlow,
            collectionsFlow
        ) { sales, products, customers, expenses, collections ->
            DataHolder(sales, products, customers, expenses, collections)
        }
        
        // Combinar con el último flow
        return combine(baseCombine, invoicesFlow) { holder, invoices ->
            val sales = holder.sales
            val products = holder.products
            val customers = holder.customers
            val expenses = holder.expenses
            val collections = holder.collections
            
            // Filtrar solo ventas activas (excluir canceladas)
            val activeSales = sales.filter { it.status == com.negociolisto.app.domain.model.SaleStatus.ACTIVE }
            
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val today = now.date
            val yesterday = today - DatePeriod(days = 1)
            // Esta semana: últimos 7 días (incluyendo hoy)
            val thisWeekStart = today - DatePeriod(days = 6) // 7 días total (0-6 = 7 días)
            // Semana pasada: los 7 días anteriores a esta semana (días 7-13)
            val lastWeekEnd = thisWeekStart - DatePeriod(days = 1) // Un día antes de thisWeekStart
            val lastWeekStart = lastWeekEnd - DatePeriod(days = 6) // 7 días antes de lastWeekEnd
            val thisMonth = LocalDate(now.year, now.monthNumber, 1)
            val lastMonth = if (now.monthNumber == 1) {
                LocalDate(now.year - 1, 12, 1)
            } else {
                LocalDate(now.year, now.monthNumber - 1, 1)
            }
            val lastMonthEnd = thisMonth - DatePeriod(days = 1)
            
            // Métricas básicas - usar solo ventas activas
            val totalSales = activeSales.sumOf { it.total }
            val totalExpenses = expenses.sumOf { it.amount }
            val lowStockCount = products.count { it.hasLowStock() }
            
            // Métricas temporales - Ventas (solo activas)
            val salesToday = activeSales.filter { it.date.date == today }.sumOf { it.total }
            val salesYesterday = activeSales.filter { it.date.date == yesterday }.sumOf { it.total }
            val salesThisWeek = activeSales.filter { it.date.date >= thisWeekStart }.sumOf { it.total }
            val salesLastWeek = activeSales.filter { 
                val saleDate = it.date.date
                saleDate >= lastWeekStart && saleDate < thisWeekStart
            }.sumOf { it.total }
            val salesThisMonth = activeSales.filter { it.date.date >= thisMonth }.sumOf { it.total }
            val salesLastMonth = activeSales.filter {
                val saleDate = it.date.date
                saleDate >= lastMonth && saleDate < thisMonth
            }.sumOf { it.total }
            
            // Métricas temporales - Gastos
            // Usar todas las fechas de los gastos, no solo la fecha del campo date
            val expensesToday = expenses.filter { expense ->
                val expenseDate = expense.date.date
                expenseDate == today
            }.sumOf { it.amount }
            
            val expensesThisWeek = expenses.filter { expense ->
                val expenseDate = expense.date.date
                expenseDate >= thisWeekStart
            }.sumOf { it.amount }
            
            val expensesLastWeek = expenses.filter { expense ->
                val expenseDate = expense.date.date
                expenseDate >= lastWeekStart && expenseDate < thisWeekStart
            }.sumOf { it.amount }
            
            val expensesThisMonth = expenses.filter { expense ->
                val expenseDate = expense.date.date
                expenseDate >= thisMonth
            }.sumOf { it.amount }
            
            val expensesLastMonth = expenses.filter { expense ->
                val expenseDate = expense.date.date
                expenseDate >= lastMonth && expenseDate < thisMonth
            }.sumOf { it.amount }
            
            // Debug: Log para verificar gastos
            println("🔍 DEBUG Dashboard: Total gastos: ${expenses.size}, Esta semana: ${expensesThisWeek}, Este mes: ${expensesThisMonth}")
            expenses.take(5).forEach { expense ->
                println("🔍 DEBUG Dashboard: Gasto ${expense.id} - Fecha: ${expense.date.date}, Monto: ${expense.amount}")
            }
            
            // Cálculo de crecimiento de ventas (mensual)
            val salesGrowth = if (salesLastMonth > 0) {
                ((salesThisMonth - salesLastMonth) / salesLastMonth) * 100.0
            } else if (salesThisMonth > 0) {
                100.0 // Crecimiento del 100% si no había ventas el mes pasado
            } else {
                0.0
            }
            
            // Cálculo de crecimiento de gastos (mensual)
            val expenseGrowth = if (expensesLastMonth > 0) {
                ((expensesThisMonth - expensesLastMonth) / expensesLastMonth) * 100.0
            } else if (expensesThisMonth > 0) {
                100.0 // Crecimiento del 100% si no había gastos el mes pasado
            } else {
                0.0
            }
            
            // Análisis de tendencias (solo ventas activas)
            val averageDailySales = if (activeSales.isNotEmpty()) {
                val daysWithSales = activeSales.map { it.date.date }.distinct().size
                if (daysWithSales > 0) totalSales / daysWithSales else 0.0
            } else 0.0
            
            val averageTicketSize = if (activeSales.isNotEmpty()) totalSales / activeSales.size else 0.0
            
            // Top productos por cantidad vendida y valor (solo ventas activas)
            val quantitiesByProduct = mutableMapOf<String, Int>()
            val amountByProduct = mutableMapOf<String, Double>()
            val productNames = mutableMapOf<String, String>()
            
            activeSales.forEach { sale ->
                sale.items.forEach { item ->
                    quantitiesByProduct[item.productId] = (quantitiesByProduct[item.productId] ?: 0) + item.quantity
                    amountByProduct[item.productId] = (amountByProduct[item.productId] ?: 0.0) + item.lineTotal
                    productNames[item.productId] = item.productName
                }
            }
            
            val topProducts = quantitiesByProduct.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { (pid, qty) ->
                    TopProduct(pid, productNames[pid] ?: "Producto desconocido", qty, amountByProduct[pid] ?: 0.0)
                }
            
            // Top clientes por compras totales
            val topCustomers = customers
                .sortedByDescending { it.totalPurchases }
                .take(5)
                .map { c -> TopCustomer(c.id, c.name, c.totalPurchases) }
            
            // Métricas de negocio
            val totalProducts = products.size
            val totalCustomers = customers.size
            val activeCollections = collections.count { it.status == CollectionStatus.ACTIVE || it.status == CollectionStatus.SHARED }
            val pendingOrders = 0 // TODO: Implementar cuando haya método para obtener todos los pedidos pendientes
            
            // Facturas del mes actual
            val invoicesThisMonth = invoices.filter { 
                it.date.date >= thisMonth 
            }.size
            
            // Productos con stock bajo (lista completa)
            val lowStockProducts = products
                .filter { it.hasLowStock() }
                .map { product ->
                    LowStockProduct(
                        productId = product.id,
                        name = product.name,
                        currentStock = product.stockQuantity,
                        minStock = product.minimumStock
                    )
                }
            
            // Ventas diarias para gráficos (últimos 7 días)
            val sevenDaysAgo = today - DatePeriod(days = 6) // Incluye hoy (7 días total)
            val dailySalesMap = mutableMapOf<LocalDate, Double>()
            
            // Inicializar todos los días de la última semana con 0
            for (i in 0..6) {
                val date = today - DatePeriod(days = i)
                dailySalesMap[date] = 0.0
            }
            
            // Agregar ventas reales (solo activas)
            activeSales.filter { it.date.date >= sevenDaysAgo }.forEach { sale ->
                val saleDate = sale.date.date
                dailySalesMap[saleDate] = (dailySalesMap[saleDate] ?: 0.0) + sale.total
            }
            
            // Convertir a lista ordenada por fecha (más antigua primero)
            val dailySales = dailySalesMap.entries
                .sortedBy { it.key }
                .map { (date, amount) ->
                    DailySales(date = date, amount = amount)
                }
            
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
                salesToday = salesToday,
                salesYesterday = salesYesterday,
                salesThisWeek = salesThisWeek,
                salesLastWeek = salesLastWeek,
                salesThisMonth = salesThisMonth,
                salesLastMonth = salesLastMonth,
                expensesToday = expensesToday,
                expensesThisWeek = expensesThisWeek,
                expensesLastWeek = expensesLastWeek,
                expensesThisMonth = expensesThisMonth,
                expensesLastMonth = expensesLastMonth,
                salesGrowth = salesGrowth,
                expenseGrowth = expenseGrowth,
                totalProducts = totalProducts,
                totalCustomers = totalCustomers,
                activeCollections = activeCollections,
                pendingOrders = pendingOrders,
                invoicesThisMonth = invoicesThisMonth,
                averageTicketSize = averageTicketSize,
                averageDailySales = averageDailySales,
                dailySales = dailySales,
                lowStockProducts = lowStockProducts
            )
        }
    }
    
}
