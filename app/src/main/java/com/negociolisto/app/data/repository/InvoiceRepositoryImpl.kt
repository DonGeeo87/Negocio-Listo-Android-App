package com.negociolisto.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.data.local.dao.InvoiceDao
import com.negociolisto.app.data.local.entity.toDomain
import com.negociolisto.app.data.local.entity.toEntity
import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.domain.model.InvoiceItem
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.InvoiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📄 IMPLEMENTACIÓN DEL REPOSITORIO DE FACTURAS
 * 
 * Maneja los datos de facturas usando Room Database con sincronización automática a Firebase.
 */
@Singleton
class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : InvoiceRepository {

    override fun getInvoices(): Flow<List<Invoice>> {
        return invoiceDao.getAllInvoices().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getInvoicesByDateRange(start: LocalDateTime, end: LocalDateTime): Flow<List<Invoice>> {
        return invoiceDao.getInvoicesByDateRange(start.toString(), end.toString()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getInvoicesByCustomer(customerId: String): Flow<List<Invoice>> {
        return invoiceDao.getInvoicesByCustomer(customerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): Invoice? {
        return invoiceDao.getInvoiceById(id)?.toDomain()
    }

    override suspend fun addInvoice(invoice: Invoice) {
        // Guardar localmente primero
        invoiceDao.insertInvoice(invoice.toEntity())
        
        // Sincronizar automáticamente con Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val invoiceEntity = invoice.toEntity()
                    val invoiceMap = invoiceEntity.toFirestoreMap()
                    
                    firestore.collection("users/${currentUser.id}/invoices")
                        .document(invoiceEntity.id)
                        .set(invoiceMap)
                        .await()
                    
                    println("✅ Factura ${invoice.id} sincronizada automáticamente con Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error sincronizando factura con Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }

    override suspend fun updateInvoice(invoice: Invoice) {
        // Actualizar localmente primero
        invoiceDao.updateInvoice(invoice.toEntity())
        
        // Sincronizar automáticamente con Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val invoiceEntity = invoice.toEntity()
                    val invoiceMap = invoiceEntity.toFirestoreMap()
                    
                    firestore.collection("users/${currentUser.id}/invoices")
                        .document(invoiceEntity.id)
                        .set(invoiceMap)
                        .await()
                    
                    println("✅ Factura ${invoice.id} actualizada automáticamente en Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error sincronizando actualización de factura con Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }

    override suspend fun deleteInvoice(id: String) {
        // Eliminar localmente primero
        invoiceDao.deleteInvoiceById(id)
        
        // Eliminar de Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    firestore.collection("users/${currentUser.id}/invoices")
                        .document(id)
                        .delete()
                        .await()
                    
                    println("✅ Factura $id eliminada automáticamente de Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error eliminando factura de Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }
    
    /**
     * 🔄 EXTENSIÓN PARA CONVERTIR INVOICE ENTITY A MAP DE FIRESTORE
     */
    private fun com.negociolisto.app.data.local.entity.InvoiceEntity.toFirestoreMap(): Map<String, Any?> {
        val timeZone = TimeZone.currentSystemDefault()
        val dateMillis = date.toInstant(timeZone).toEpochMilliseconds()
        
        val itemsList = items.map { item ->
            mapOf<String, Any?>(
                "description" to item.description,
                "quantity" to item.quantity,
                "unitPrice" to item.unitPrice,
                "total" to (item.quantity * item.unitPrice)
            )
        }
        
        return mapOf(
            "id" to id,
            "number" to number,
            "saleId" to saleId,
            "customerId" to customerId,
            "items" to itemsList,
            "subtotal" to subtotal,
            "tax" to tax,
            "total" to total,
            "date" to dateMillis,
            "template" to template.name,
            "notes" to (notes ?: "")
        )
    }
    
    suspend fun getInvoiceBySaleId(saleId: String): Invoice? {
        return invoiceDao.getInvoiceBySaleId(saleId)?.toDomain()
    }
    
    suspend fun updateAllInvoiceTemplates(template: com.negociolisto.app.domain.model.InvoiceTemplateType) {
        invoiceDao.updateAllInvoiceTemplates(template.name)
    }
}
