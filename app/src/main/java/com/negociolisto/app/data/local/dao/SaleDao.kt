package com.negociolisto.app.data.local.dao

import androidx.room.*
import com.negociolisto.app.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

/**
 * 💰 DAO DE VENTAS
 */
@Dao
interface SaleDao {
    
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): SaleEntity?
    
    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesByCustomer(customerId: String): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<SaleEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<SaleEntity>)
    
    @Update
    suspend fun updateSale(sale: SaleEntity)
    
    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: String)

    @Query("UPDATE sales SET status = :status, canceledAt = :canceledAt, canceledReason = :reason WHERE id = :id")
    suspend fun cancelSale(id: String, status: String, canceledAt: Long?, reason: String?)
    
    @Query("SELECT SUM(total) FROM sales WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSalesInRange(startDate: Long, endDate: Long): Double?
    
    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getTotalSaleCount(): Int
    
    @Query("DELETE FROM sales")
    suspend fun clearAllSales()
}
