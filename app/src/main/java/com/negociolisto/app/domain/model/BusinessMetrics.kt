package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDate

/**
 * 📊 Métricas de negocio para el Dashboard
 */
data class BusinessMetrics(
    // Métricas básicas existentes
    val totalSales: Double,
    val totalExpenses: Double,
    val grossMargin: Double,
    val grossMarginPercent: Double,
    val lowStockCount: Int,
    val topProducts: List<TopProduct>,
    val topCustomers: List<TopCustomer>,
    
    // Métricas temporales
    val salesToday: Double = 0.0,
    val salesYesterday: Double = 0.0,
    val salesThisWeek: Double = 0.0,
    val salesLastWeek: Double = 0.0,
    val salesThisMonth: Double = 0.0,
    val salesLastMonth: Double = 0.0,
    
    val expensesToday: Double = 0.0,
    val expensesThisWeek: Double = 0.0,
    val expensesLastWeek: Double = 0.0,
    val expensesThisMonth: Double = 0.0,
    val expensesLastMonth: Double = 0.0,
    
    // Porcentajes de crecimiento
    val salesGrowth: Double = 0.0, // % de crecimiento de ventas
    val expenseGrowth: Double = 0.0, // % de crecimiento de gastos
    
    // Métricas de negocio
    val totalProducts: Int = 0,
    val totalCustomers: Int = 0,
    val activeCollections: Int = 0,
    val pendingOrders: Int = 0, // Pedidos pendientes de colecciones
    val invoicesThisMonth: Int = 0,
    
    // Métricas calculadas
    val averageTicketSize: Double = 0.0,
    val averageDailySales: Double = 0.0,
    
    // Datos para gráficos (últimos 7 días)
    val dailySales: List<DailySales> = emptyList(),
    
    // Productos con stock bajo (lista completa)
    val lowStockProducts: List<LowStockProduct> = emptyList()
)

data class TopProduct(
    val productId: String,
    val name: String,
    val totalQuantitySold: Int,
    val totalSalesAmount: Double
)

data class TopCustomer(
    val customerId: String,
    val name: String,
    val totalSalesAmount: Double
)

/**
 * 📦 Producto con stock bajo
 */
data class LowStockProduct(
    val productId: String,
    val name: String,
    val currentStock: Int,
    val minStock: Int
)

/**
 * 📊 Ventas diarias para gráficos
 */
data class DailySales(
    val date: LocalDate,
    val amount: Double
)



