package com.negociolisto.app.data.local.dao

import androidx.room.*
import com.negociolisto.app.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * 👥 DAO DE CLIENTES
 */
@Dao
interface CustomerDao {
    
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE email = :email")
    suspend fun getCustomerByEmail(email: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE phone = :phone")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)
    
    @Update
    suspend fun updateCustomer(customer: CustomerEntity)
    
    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)
    
    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int
    
    @Query("DELETE FROM customers")
    suspend fun clearAllCustomers()
    
    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getTotalCustomerCount(): Int
    
    @Query("UPDATE customers SET totalPurchases = :totalPurchases, lastPurchaseDate = :lastPurchaseDate WHERE id = :customerId")
    suspend fun updateCustomerPurchases(customerId: String, totalPurchases: Double, lastPurchaseDate: Long?)
}
