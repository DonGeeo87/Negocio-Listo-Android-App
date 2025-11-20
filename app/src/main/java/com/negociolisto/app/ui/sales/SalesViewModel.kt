package com.negociolisto.app.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.domain.model.InvoiceItem
import com.negociolisto.app.domain.model.InvoiceTemplateType
import com.negociolisto.app.domain.util.TaxCalculator
import com.negociolisto.app.domain.repository.SalesRepository
import com.negociolisto.app.ui.invoices.InvoiceSettingsStore
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.InvoiceRepository
import com.negociolisto.app.data.analytics.AnalyticsHelper
import com.negociolisto.app.data.analytics.CrashlyticsHelper
import com.negociolisto.app.data.analytics.PerformanceHelper
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 💰 VIEWMODEL DE VENTAS CON HILT
 * 
 * Maneja la lógica de negocio para ventas.
 */
@HiltViewModel
class SalesViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val inventoryRepository: InventoryRepository,
    private val invoiceRepository: InvoiceRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val crashlyticsHelper: CrashlyticsHelper,
    private val performanceHelper: PerformanceHelper
) : ViewModel() {
    
    val sales: StateFlow<List<Sale>> = salesRepository.getSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())
    
    val products: StateFlow<List<Product>> = inventoryRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())
    
    suspend fun recordSale(sale: Sale) {
        val trace = performanceHelper.startTrace(PerformanceHelper.Traces.SALE_CREATION)
        try {
            trace.putAttribute("sale_id", sale.id)
            trace.putAttribute("item_count", sale.items.size.toString())
            trace.incrementMetric(PerformanceHelper.Metrics.ITEM_COUNT, sale.items.size.toLong())
            
            // El repositorio ahora actualiza el stock automáticamente
            salesRepository.recordSale(sale)
            
            // Analytics
            analyticsHelper.logSaleCreated(sale.total, sale.items.size)
            
        } catch (e: Exception) {
            crashlyticsHelper.recordException(e)
            crashlyticsHelper.log("Error registrando venta: ${sale.id}")
            throw e
        } finally {
            performanceHelper.stopTrace(trace)
        }
    }

    suspend fun updateSale(sale: Sale) {
        val trace = performanceHelper.startTrace(PerformanceHelper.Traces.SALE_CREATION)
        try {
            trace.putAttribute("sale_id", sale.id)
            trace.putAttribute("item_count", sale.items.size.toString())
            trace.incrementMetric(PerformanceHelper.Metrics.ITEM_COUNT, sale.items.size.toLong())
            
            // El repositorio maneja la reversión de stock y la actualización
            salesRepository.updateSale(sale)
            
            // Analytics - podríamos agregar un evento específico para edición
            analyticsHelper.logSaleCreated(sale.total, sale.items.size)
            
        } catch (e: Exception) {
            crashlyticsHelper.recordException(e)
            crashlyticsHelper.log("Error actualizando venta: ${sale.id}")
            throw e
        } finally {
            performanceHelper.stopTrace(trace)
        }
    }

    fun deleteSale(saleId: String) {
        viewModelScope.launch {
            salesRepository.cancelSale(saleId, reason = "Anulación por error")
        }
    }

    fun revertStockForSale(sale: Sale) {
        viewModelScope.launch {
            // Revertir stock por cada item (aumentar existencias)
            sale.items.forEach { item ->
                val product = inventoryRepository.getProductById(item.productId) ?: return@forEach
                val newQty = product.stockQuantity + item.quantity
                // Usar RETURN_FROM_CUSTOMER como motivo para la reversión
                inventoryRepository.updateProductStock(
                    productId = item.productId, 
                    newQuantity = newQty, 
                    reason = "RETURN_FROM_CUSTOMER", 
                    description = "Reversión por anulación de venta ${sale.id}"
                )
            }
        }
    }

    suspend fun generateInvoiceFromSale(saleId: String): Result<String> {
        val trace = performanceHelper.startTrace(PerformanceHelper.Traces.INVOICE_GENERATION)
        return try {
            trace.putAttribute("sale_id", saleId)
            
            // Intentar obtener la venta directamente del repositorio (más confiable que el Flow)
            // Primero intentar obtenerla directamente del repositorio
            var sale = salesRepository.getSaleById(saleId)
            
            // Si no está disponible, esperar un poco y reintentar (Room puede necesitar tiempo)
            if (sale == null) {
                var attempts = 0
                val maxAttempts = 10 // 10 intentos = 2 segundos total
                while (sale == null && attempts < maxAttempts) {
                    kotlinx.coroutines.delay(200) // Esperar 200ms antes de intentar de nuevo
                    sale = salesRepository.getSaleById(saleId)
                    attempts++
                    if (sale != null) {
                        println("✅ Venta encontrada después de ${attempts} intentos")
                        break
                    }
                }
            }
            
            // Si aún no está disponible, intentar desde el StateFlow como respaldo
            if (sale == null) {
                sale = sales.value.firstOrNull { it.id == saleId }
                if (sale != null) {
                    println("✅ Venta encontrada en el StateFlow")
                }
            }
            
            if (sale != null) {
                val invoice = createInvoiceFromSale(sale)
                invoiceRepository.addInvoice(invoice)
                
                // Analytics
                analyticsHelper.logInvoiceGenerated(invoice.number)
                
                trace.putAttribute("invoice_number", invoice.number)
                trace.incrementMetric(PerformanceHelper.Metrics.SALE_COUNT, 1)
                
                println("✅ Factura generada exitosamente para la venta: ${sale.id}, factura ID: ${invoice.id}")
                Result.success(invoice.id)
            } else {
                val error = Exception("No se encontró la venta con ID: $saleId. Por favor, verifica que la venta se haya guardado correctamente.")
                crashlyticsHelper.recordException(error)
                crashlyticsHelper.log("Error: No se encontró la venta con ID: $saleId")
                println("❌ Error: No se encontró la venta con ID: $saleId después de múltiples intentos")
                Result.failure(error)
            }
        } catch (e: Exception) {
            println("❌ Error al generar factura: ${e.message}")
            crashlyticsHelper.recordException(e)
            crashlyticsHelper.log("Error generando factura para venta: $saleId")
            Result.failure(e)
        } finally {
            performanceHelper.stopTrace(trace)
        }
    }

    private fun createInvoiceFromSale(sale: Sale): Invoice {
        val invoiceItems = sale.items.map { saleItem ->
            InvoiceItem(
                description = saleItem.productName,
                quantity = saleItem.quantity,
                unitPrice = saleItem.unitPrice
            )
        }

        // Obtener configuración de precio (neto o con IVA incluido)
        val priceIsNet = InvoiceSettingsStore.settings.value.priceIsNet
        val totals = TaxCalculator.fromInvoiceItems(invoiceItems, priceIsNet)

        // Generar número de factura único y descriptivo
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val invoiceNumber = "INV-%04d%02d%02d-%02d%02d".format(
            now.year, 
            now.monthNumber, 
            now.dayOfMonth, 
            now.hour, 
            now.minute
        )

        return Invoice(
            id = UUID.randomUUID().toString(), // Asegurar que tenga un ID único
            number = invoiceNumber,
            saleId = sale.id,
            customerId = sale.customerId,
            items = invoiceItems,
            subtotal = totals.subtotal,
            tax = totals.tax,
            total = totals.total,
            date = now, // Usar fecha actual en lugar de la fecha de la venta
            template = InvoiceSettingsStore.settings.value.defaultTemplate,
            notes = sale.note
        )
    }
}
