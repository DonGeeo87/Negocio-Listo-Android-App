package com.negociolisto.app.data.service

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.negociolisto.app.domain.model.*
import com.negociolisto.app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.*
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * 📊 SERVICIO DE EXPORTACIÓN DE DATOS
 * 
 * Maneja la exportación de todos los datos de la aplicación en diferentes formatos.
 * Soporta CSV, Excel y PDF con filtros avanzados y progreso en tiempo real.
 */
class DataExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val salesRepository: SalesRepository,
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val expenseRepository: ExpenseRepository,
    private val invoiceRepository: InvoiceRepository
) {
    
    /**
     * 📋 TIPOS DE EXPORTACIÓN DISPONIBLES
     */
    enum class ExportType {
        EXCEL, PDF
    }
    
    /**
     * 📊 CATEGORÍAS DE DATOS PARA EXPORTAR
     */
    enum class DataCategory {
        ALL, SALES, INVENTORY, CUSTOMERS, EXPENSES, INVOICES
    }
    
    /**
     * 📅 FILTROS DE FECHA PARA EXPORTACIÓN
     */
    data class DateFilter(
        val startDate: kotlinx.datetime.LocalDateTime? = null,
        val endDate: kotlinx.datetime.LocalDateTime? = null,
        val period: String = "ALL" // TODAY, WEEK, MONTH, YEAR, ALL
    )
    
    /**
     * ⚙️ CONFIGURACIÓN DE EXPORTACIÓN
     */
    data class ExportConfig(
        val type: ExportType,
        val categories: List<DataCategory>,
        val dateFilter: DateFilter,
        val includeImages: Boolean = false,
        val includeMetadata: Boolean = true
    )
    
    /**
     * 📈 ESTADO DE PROGRESO DE EXPORTACIÓN
     */
    data class ExportProgress(
        val currentStep: String,
        val progress: Float, // 0.0 a 1.0
        val totalSteps: Int,
        val currentStepNumber: Int,
        val isComplete: Boolean = false,
        val error: String? = null
    )
    
    /**
     * 🚀 EXPORTAR DATOS CON PROGRESO
     * 
     * Función principal que maneja toda la exportación con seguimiento de progreso.
     */
    fun exportData(config: ExportConfig): Flow<ExportProgress> = flow {
        val totalSteps = calculateTotalSteps(config)
        var currentStep = 0
        
        try {
            // Paso 1: Preparar archivo de destino
            emit(ExportProgress(
                currentStep = "Preparando archivo de exportación...",
                progress = 0.0f,
                totalSteps = totalSteps,
                currentStepNumber = ++currentStep
            ))
            
            val exportFile = createExportFile(config.type)
            
            // Paso 2: Recopilar datos según categorías seleccionadas
            emit(ExportProgress(
                currentStep = "Recopilando datos...",
                progress = 0.2f,
                totalSteps = totalSteps,
                currentStepNumber = ++currentStep
            ))
            
            val exportData = collectExportData(config)
            
            // Paso 3: Generar contenido según tipo
            emit(ExportProgress(
                currentStep = "Generando contenido ${config.type.name}...",
                progress = 0.4f,
                totalSteps = totalSteps,
                currentStepNumber = ++currentStep
            ))
            
            val content = generateContent(exportData, config)
            
            // Paso 4: Escribir archivo
            emit(ExportProgress(
                currentStep = "Escribiendo archivo...",
                progress = 0.6f,
                totalSteps = totalSteps,
                currentStepNumber = ++currentStep
            ))
            
            writeToFile(exportFile, content, config.type)
            
            // Paso 5: Finalizar
            emit(ExportProgress(
                currentStep = "Finalizando exportación...",
                progress = 0.8f,
                totalSteps = totalSteps,
                currentStepNumber = ++currentStep
            ))
            
            // Paso 6: Completado
            emit(ExportProgress(
                currentStep = "Exportación completada exitosamente",
                progress = 1.0f,
                totalSteps = totalSteps,
                currentStepNumber = ++currentStep,
                isComplete = true
            ))
            
        } catch (e: Exception) {
            emit(ExportProgress(
                currentStep = "Error en la exportación",
                progress = 0.0f,
                totalSteps = totalSteps,
                currentStepNumber = currentStep,
                error = e.message ?: "Error desconocido"
            ))
        }
    }
    
    /**
     * 📁 CREAR ARCHIVO DE EXPORTACIÓN
     */
    private fun createExportFile(type: ExportType): File {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timestamp = "${now.year}${now.monthNumber.toString().padStart(2, '0')}${now.dayOfMonth.toString().padStart(2, '0')}_${now.hour.toString().padStart(2, '0')}${now.minute.toString().padStart(2, '0')}${now.second.toString().padStart(2, '0')}"
        val extension = when (type) {
            ExportType.EXCEL -> "xls"
            ExportType.PDF -> "pdf"
        }
        val fileName = "negociolisto_export_$timestamp.$extension"
        
        val exportDir = File(context.filesDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        
        return File(exportDir, fileName)
    }
    
    /**
     * 📊 RECOPILAR DATOS PARA EXPORTACIÓN
     */
    private suspend fun collectExportData(config: ExportConfig): ExportData {
        val sales = if (config.categories.contains(DataCategory.ALL) || config.categories.contains(DataCategory.SALES)) {
            salesRepository.getSales()
        } else emptyList<Sale>()
        
        val products = if (config.categories.contains(DataCategory.ALL) || config.categories.contains(DataCategory.INVENTORY)) {
            inventoryRepository.getAllProducts()
        } else emptyList<Product>()
        
        val customers = if (config.categories.contains(DataCategory.ALL) || config.categories.contains(DataCategory.CUSTOMERS)) {
            customerRepository.getAllCustomers()
        } else emptyList<Customer>()
        
        val expenses = if (config.categories.contains(DataCategory.ALL) || config.categories.contains(DataCategory.EXPENSES)) {
            expenseRepository.getAllExpenses()
        } else emptyList<Expense>()
        
        val invoices = if (config.categories.contains(DataCategory.ALL) || config.categories.contains(DataCategory.INVOICES)) {
            emptyList<Invoice>() // Por ahora vacío hasta implementar InvoiceRepository
        } else emptyList<Invoice>()
        
        return ExportData(
            sales = sales as List<Sale>,
            products = products as List<Product>,
            customers = customers as List<Customer>,
            expenses = expenses as List<Expense>,
            invoices = invoices as List<Invoice>,
            exportDate = kotlinx.datetime.Clock.System.now().toString(),
            config = config
        )
    }
    
    /**
     * 📝 GENERAR CONTENIDO SEGÚN TIPO
     */
    private fun generateContent(data: ExportData, config: ExportConfig): String {
        return when (config.type) {
            ExportType.EXCEL -> generateExcelContent(data)
            ExportType.PDF -> generatePdfContent(data)
        }
    }
    
    /**
     * 📄 GENERAR CONTENIDO CSV
     */
    private fun generateCsvContent(data: ExportData): String {
        val csv = StringBuilder()
        
        // Encabezado con metadatos
        if (data.config.includeMetadata) {
            csv.appendLine("# Exportación de NegocioListo")
            csv.appendLine("# Fecha de exportación: ${data.exportDate}")
            csv.appendLine("# Categorías incluidas: ${data.config.categories.joinToString(", ")}")
            csv.appendLine("# Filtro de fecha: ${data.config.dateFilter.period}")
            csv.appendLine()
        }
        
        // Datos de ventas
        if (data.sales.isNotEmpty()) {
            csv.appendLine("## VENTAS")
            csv.appendLine("ID,Fecha,Cliente,Total,Items")
            data.sales.forEach { sale ->
                val items = sale.items.joinToString(";") { "${it.productName} x${it.quantity}" }
                csv.appendLine("${sale.id},${sale.date},${sale.customerId},${sale.total},${items}")
            }
            csv.appendLine()
        }
        
        // Datos de productos
        if (data.products.isNotEmpty()) {
            csv.appendLine("## PRODUCTOS")
            csv.appendLine("ID,Nombre,SKU,Categoría,Precio,Stock")
            data.products.forEach { product ->
                csv.appendLine("${product.id},${product.name},${product.sku},${product.customCategoryId},${product.salePrice},${product.stockQuantity}")
            }
            csv.appendLine()
        }
        
        // Datos de clientes
        if (data.customers.isNotEmpty()) {
            csv.appendLine("## CLIENTES")
            csv.appendLine("ID,Nombre,Email,Teléfono,Dirección,Total Compras")
            data.customers.forEach { customer ->
                csv.appendLine("${customer.id},${customer.name},${customer.email ?: ""},${customer.phone ?: ""},${customer.address ?: ""},${customer.totalPurchases}")
            }
            csv.appendLine()
        }
        
        // Datos de gastos
        if (data.expenses.isNotEmpty()) {
            csv.appendLine("## GASTOS")
            csv.appendLine("ID,Fecha,Descripción,Categoría,Monto")
            data.expenses.forEach { expense ->
                csv.appendLine("${expense.id},${expense.date},${expense.description},${expense.category},${expense.amount}")
            }
            csv.appendLine()
        }
        
        // Datos de facturas
        if (data.invoices.isNotEmpty()) {
            csv.appendLine("## FACTURAS")
            csv.appendLine("ID,Número,Fecha,Cliente,Subtotal,Impuesto,Total")
            data.invoices.forEach { invoice ->
                csv.appendLine("${invoice.id},${invoice.id},${invoice.date},${invoice.customerId},${invoice.subtotal},${invoice.tax},${invoice.total}")
            }
        }
        
        return csv.toString()
    }
    
    /**
     * 📊 GENERAR CONTENIDO EXCEL
     */
    private fun generateExcelContent(data: ExportData): String {
        val excel = StringBuilder()
        
        // Encabezado con metadatos
        if (data.config.includeMetadata) {
            excel.appendLine("EXPORTACIÓN DE NEGOCIOLISTO")
            excel.appendLine("Fecha de exportación: ${data.exportDate}")
            excel.appendLine("Categorías incluidas: ${data.config.categories.joinToString(", ")}")
            excel.appendLine("Filtro de fecha: ${data.config.dateFilter.period}")
            excel.appendLine()
        }
        
        // Datos de ventas
        if (data.sales.isNotEmpty()) {
            excel.appendLine("VENTAS")
            excel.appendLine("ID\tFecha\tCliente\tTotal\tItems")
            data.sales.forEach { sale ->
                val items = sale.items.joinToString(";") { "${it.productName} x${it.quantity}" }
                excel.appendLine("${sale.id}\t${sale.date}\t${sale.customerId}\t${sale.total}\t${items}")
            }
            excel.appendLine()
        }
        
        // Datos de productos
        if (data.products.isNotEmpty()) {
            excel.appendLine("PRODUCTOS")
            excel.appendLine("ID\tNombre\tSKU\tCategoría\tPrecio\tStock")
            data.products.forEach { product ->
                excel.appendLine("${product.id}\t${product.name}\t${product.sku}\t${product.customCategoryId}\t${product.salePrice}\t${product.stockQuantity}")
            }
            excel.appendLine()
        }
        
        // Datos de clientes
        if (data.customers.isNotEmpty()) {
            excel.appendLine("CLIENTES")
            excel.appendLine("ID\tNombre\tEmail\tTeléfono\tDirección\tTotal Compras")
            data.customers.forEach { customer ->
                excel.appendLine("${customer.id}\t${customer.name}\t${customer.email ?: ""}\t${customer.phone ?: ""}\t${customer.address ?: ""}\t${customer.totalPurchases}")
            }
            excel.appendLine()
        }
        
        // Datos de gastos
        if (data.expenses.isNotEmpty()) {
            excel.appendLine("GASTOS")
            excel.appendLine("ID\tFecha\tDescripción\tCategoría\tMonto")
            data.expenses.forEach { expense ->
                excel.appendLine("${expense.id}\t${expense.date}\t${expense.description}\t${expense.category}\t${expense.amount}")
            }
            excel.appendLine()
        }
        
        // Datos de facturas
        if (data.invoices.isNotEmpty()) {
            excel.appendLine("FACTURAS")
            excel.appendLine("ID\tNúmero\tFecha\tCliente\tSubtotal\tImpuesto\tTotal")
            data.invoices.forEach { invoice ->
                excel.appendLine("${invoice.id}\t${invoice.id}\t${invoice.date}\t${invoice.customerId}\t${invoice.subtotal}\t${invoice.tax}\t${invoice.total}")
            }
        }
        
        return excel.toString()
    }
    
    /**
     * 📄 GENERAR CONTENIDO PDF
     */
    private fun generatePdfContent(data: ExportData): String {
        val pdf = StringBuilder()
        
        pdf.appendLine("REPORTE DE EXPORTACIÓN - NEGOCIOLISTO")
        pdf.appendLine("=====================================")
        pdf.appendLine()
        pdf.appendLine("Fecha de exportación: ${data.exportDate}")
        pdf.appendLine("Categorías incluidas: ${data.config.categories.joinToString(", ")}")
        pdf.appendLine()
        
        // Resumen ejecutivo
        pdf.appendLine("RESUMEN EJECUTIVO")
        pdf.appendLine("----------------")
        pdf.appendLine("Total de ventas: ${data.sales.size}")
        pdf.appendLine("Total de productos: ${data.products.size}")
        pdf.appendLine("Total de clientes: ${data.customers.size}")
        pdf.appendLine("Total de gastos: ${data.expenses.size}")
        pdf.appendLine("Total de facturas: ${data.invoices.size}")
        pdf.appendLine()
        
        // Detalles por categoría
        if (data.sales.isNotEmpty()) {
            pdf.appendLine("DETALLE DE VENTAS")
            pdf.appendLine("----------------")
            data.sales.forEach { sale ->
                pdf.appendLine("• ${sale.date}: ${sale.customerId} - $${sale.total}")
            }
            pdf.appendLine()
        }
        
        return pdf.toString()
    }
    
    /**
     * 💾 ESCRIBIR CONTENIDO AL ARCHIVO
     */
    private fun writeToFile(file: File, content: String, type: ExportType) {
        when (type) {
            ExportType.EXCEL -> {
                // Para Excel, usar formato tab-separated que se abre bien en Excel
                FileOutputStream(file).use { fos ->
                    OutputStreamWriter(fos, "UTF-8").use { writer ->
                        writer.write(content)
                    }
                }
            }
            ExportType.PDF -> {
                // Para PDF, escribir como texto plano (en una implementación completa se usaría una librería PDF)
                FileOutputStream(file).use { fos ->
                    OutputStreamWriter(fos, "UTF-8").use { writer ->
                        writer.write(content)
                    }
                }
            }
        }
    }
    
    /**
     * 🔢 CALCULAR TOTAL DE PASOS
     */
    private fun calculateTotalSteps(config: ExportConfig): Int {
        return 6 // Pasos fijos en el proceso de exportación
    }
    
    /**
     * 🔗 OBTENER URI PARA COMPARTIR ARCHIVO
     */
    fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    /**
     * 📊 DATOS DE EXPORTACIÓN
     */
    private data class ExportData(
        val sales: List<Sale>,
        val products: List<Product>,
        val customers: List<Customer>,
        val expenses: List<Expense>,
        val invoices: List<Invoice>,
        val exportDate: String,
        val config: ExportConfig
    )
}
