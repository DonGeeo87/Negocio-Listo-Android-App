package com.negociolisto.app.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.CollectionResponse
import com.negociolisto.app.domain.model.OrderStatus
import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.domain.model.SaleItem
import com.negociolisto.app.domain.model.PaymentMethod
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.CollectionResponseRepository
import com.negociolisto.app.domain.repository.SalesRepository
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.data.analytics.AnalyticsHelper
import com.negociolisto.app.data.analytics.CrashlyticsHelper
import com.negociolisto.app.domain.util.NegocioListoError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * 📋 VIEWMODEL DE DETALLE DE PEDIDO
 */
@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val responseRepository: CollectionResponseRepository,
    private val authRepository: AuthRepository,
    private val salesRepository: SalesRepository,
    private val inventoryRepository: InventoryRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val crashlyticsHelper: CrashlyticsHelper
) : ViewModel() {
    
    private val _response = kotlinx.coroutines.flow.MutableStateFlow<CollectionResponse?>(null)
    val response: StateFlow<CollectionResponse?> = _response.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(1000),
        null
    )
    
    fun loadResponse(responseId: String) {
        viewModelScope.launch {
            val resp = responseRepository.getResponseById(responseId)
            _response.value = resp
        }
    }
    
    fun updateStatus(status: OrderStatus) {
        val response = _response.value ?: return
        val responseId = response.id
        val previousStatus = response.status
        
        viewModelScope.launch {
            responseRepository.updateStatus(responseId, status)
            
            // Si el pedido se marca como DELIVERED por primera vez, crear una venta y descontar stock
            // Solo crear la venta si el estado anterior no era DELIVERED (evitar duplicados)
            if (status == OrderStatus.DELIVERED && previousStatus != OrderStatus.DELIVERED) {
                createSaleFromOrder(response)
            }
            
            loadResponse(responseId)
        }
    }
    
    /**
     * Convierte un pedido (CollectionResponse) en una venta (Sale) y la registra
     */
    private suspend fun createSaleFromOrder(order: CollectionResponse) {
        try {
            val saleId = "ORDER_${order.id}" // Prefijo para identificar que viene de un pedido
            
            // Verificar si ya existe una venta para este pedido
            val existingSales = salesRepository.getSales().first()
            if (existingSales.any { it.id == saleId }) {
                // Ya existe una venta para este pedido, no crear duplicado
                return
            }
            
            // Obtener los productos del inventario para obtener precios y nombres
            val saleItems = mutableListOf<SaleItem>()
            var total = 0.0
            
            order.items.forEach { (productId, orderItem) ->
                val product = inventoryRepository.getProductById(productId)
                if (product != null) {
                    // ⚠️ VALIDAR STOCK DISPONIBLE antes de crear el item de venta
                    if (product.stockQuantity < orderItem.quantity) {
                        // No hay suficiente stock, lanzar excepción
                        throw NegocioListoError.InsufficientStockError(
                            productId = productId,
                            productName = product.name,
                            requestedQuantity = orderItem.quantity,
                            availableStock = product.stockQuantity
                        )
                    }
                    
                    // Usar el precio del producto del inventario
                    val unitPrice = product.salePrice
                    val saleItem = SaleItem(
                        productId = productId,
                        productName = product.name,
                        quantity = orderItem.quantity,
                        unitPrice = unitPrice
                    )
                    saleItems.add(saleItem)
                    total += saleItem.lineTotal
                } else {
                    // Producto no encontrado
                    throw NegocioListoError.BusinessRuleError(
                        "Producto no encontrado en inventario: $productId"
                    )
                }
            }
            
            // Si no hay items válidos, no crear la venta
            if (saleItems.isEmpty()) {
                return
            }
            
            // Mapear el método de pago del pedido al enum PaymentMethod
            val paymentMethod = mapPaymentMethod(order.paymentMethod)
            
            // Crear la venta
            val sale = Sale(
                id = saleId,
                customerId = order.customerId,
                items = saleItems,
                total = total,
                date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                paymentMethod = paymentMethod,
                note = "Pedido desde colección: ${order.collectionId}. ${order.observations ?: ""}".trim(),
                status = com.negociolisto.app.domain.model.SaleStatus.ACTIVE
            )
            
            // Registrar la venta (esto también descontará el stock automáticamente)
            salesRepository.recordSale(sale)
            
            // Analytics - Track creación de venta desde pedido
            analyticsHelper.logSaleCreated(total, saleItems.size)
            analyticsHelper.logOrderCreated(order.collectionId, total)
            
        } catch (e: Exception) {
            // Log del error (en producción se podría usar un sistema de logging)
            println("Error al crear venta desde pedido: ${e.message}")
            crashlyticsHelper.recordException(e)
            crashlyticsHelper.log("Error creando venta desde pedido: ${order.id}")
        }
    }
    
    /**
     * Mapea el método de pago del pedido al enum PaymentMethod
     */
    private fun mapPaymentMethod(paymentMethod: String): PaymentMethod {
        return when (paymentMethod.lowercase()) {
            "efectivo", "cash" -> PaymentMethod.CASH
            "transferencia", "bank_transfer", "transfer" -> PaymentMethod.BANK_TRANSFER
            "tarjeta débito", "debit_card", "debit" -> PaymentMethod.DEBIT_CARD
            "tarjeta crédito", "credit_card", "credit" -> PaymentMethod.CREDIT_CARD
            "billetera digital", "digital_wallet", "wallet" -> PaymentMethod.DIGITAL_WALLET
            "cheque", "check" -> PaymentMethod.CHECK
            "crédito de tienda", "store_credit", "credit_store" -> PaymentMethod.STORE_CREDIT
            else -> PaymentMethod.CASH // Por defecto
        }
    }
}
