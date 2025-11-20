package com.negociolisto.app.data.local.dao

import androidx.room.*
import com.negociolisto.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Transaction
    @Query("SELECT * FROM collections WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getCollections(userId: String): Flow<List<CollectionWithItemsEntity>>

    @Transaction
    @Query("SELECT * FROM collections WHERE id = :id AND userId = :userId")
    suspend fun getById(id: String, userId: String): CollectionWithItemsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollections(collections: List<CollectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CollectionItemEntity>)

    @Query("DELETE FROM collection_items WHERE collectionId = :collectionId")
    suspend fun deleteItemsForCollection(collectionId: String)

    @Transaction
    suspend fun upsert(collection: CollectionEntity, items: List<CollectionItemEntity>) {
        insertCollection(collection)
        deleteItemsForCollection(collection.id)
        if (items.isNotEmpty()) insertItems(items)
    }

    @Transaction
    suspend fun deleteCollection(id: String) {
        deleteItemsForCollection(id)
        deleteCollectionOnly(id)
    }
    
    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollectionOnly(id: String)
    
    @Query("SELECT COUNT(*) FROM collections WHERE userId = :userId")
    suspend fun getTotalCollectionCount(userId: String): Int
    
    @Query("DELETE FROM collection_items")
    suspend fun clearAllCollectionItems()

    @Query("DELETE FROM collections WHERE userId = :userId")
    suspend fun clearAllCollections(userId: String)
    
    /**
     * 📚 OBTENER COLECCIONES POR CLIENTE
     * 
     * Busca todas las colecciones que están asociadas a un cliente específico.
     * El campo associatedCustomerIds se almacena como string separado por comas.
     */
    @Transaction
    @Query("SELECT * FROM collections WHERE associatedCustomerIds LIKE '%' || :customerId || '%' ORDER BY updatedAt DESC")
    suspend fun getCollectionsByCustomerId(customerId: String): List<CollectionWithItemsEntity>
    
    /**
     * 🎨 ACTUALIZAR TEMPLATE DE COLECCIONES POR CLIENTE
     * 
     * Actualiza el template de todas las colecciones asociadas a un cliente.
     * El campo associatedCustomerIds se almacena como string separado por comas.
     */
    @Query("UPDATE collections SET webTemplate = :template, updatedAt = :updatedAt WHERE associatedCustomerIds LIKE '%' || :customerId || '%'")
    suspend fun updateTemplateForCustomer(customerId: String, template: String, updatedAt: Long)
}


