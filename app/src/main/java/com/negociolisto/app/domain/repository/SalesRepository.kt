package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SalesRepository {
    fun getSales(): Flow<List<Sale>>
    suspend fun getSaleById(id: String): Sale?
    suspend fun recordSale(sale: Sale)
    suspend fun updateSale(sale: Sale)
    suspend fun cancelSale(id: String, reason: String? = null)
    suspend fun deleteSale(id: String)
}


