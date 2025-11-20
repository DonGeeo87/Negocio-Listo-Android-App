package com.negociolisto.app.data.local.dao

import androidx.room.*
import com.negociolisto.app.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

/**
 * 📄 DAO PARA FACTURAS
 * 
 * Operaciones de base de datos para facturas.
 */
@Dao
interface InvoiceDao {
    
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>
    
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: String): InvoiceEntity?
    
    @Query("SELECT * FROM invoices WHERE saleId = :saleId")
    suspend fun getInvoiceBySaleId(saleId: String): InvoiceEntity?
    
    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY date DESC")
    fun getInvoicesByCustomer(customerId: String): Flow<List<InvoiceEntity>>
    
    @Query("SELECT * FROM invoices WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getInvoicesByDateRange(startDate: String, endDate: String): Flow<List<InvoiceEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)
    
    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)
    
    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)
    
    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: String)
    
    @Query("UPDATE invoices SET template = :template WHERE template != :template")
    suspend fun updateAllInvoiceTemplates(template: String)
    
    @Query("DELETE FROM invoices")
    suspend fun clearAllInvoices()
}
