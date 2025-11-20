package com.negociolisto.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.negociolisto.app.data.local.entity.CollectionResponseEntity
import com.negociolisto.app.data.local.entity.CollectionResponseItemEntity
import com.negociolisto.app.data.local.entity.CollectionResponseWithItemsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionResponseDao {

    @Transaction
    @Query(
        "SELECT * FROM collection_responses WHERE collectionId = :collectionId " +
            "ORDER BY createdAt DESC"
    )
    fun observeResponsesByCollection(
        collectionId: String
    ): Flow<List<CollectionResponseWithItemsEntity>>

    @Transaction
    @Query("SELECT * FROM collection_responses WHERE id = :responseId LIMIT 1")
    suspend fun getResponseWithItems(responseId: String): CollectionResponseWithItemsEntity?

    @Query("SELECT * FROM collection_responses WHERE id = :responseId LIMIT 1")
    suspend fun getResponseEntity(responseId: String): CollectionResponseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: CollectionResponseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CollectionResponseItemEntity>)

    @Query("DELETE FROM collection_response_items WHERE responseId = :responseId")
    suspend fun deleteItemsForResponse(responseId: String)

    @Query("DELETE FROM collection_responses WHERE collectionId = :collectionId")
    suspend fun deleteResponsesForCollection(collectionId: String)

    @Query(
        "DELETE FROM collection_responses WHERE collectionId = :collectionId " +
            "AND id NOT IN (:responseIds)"
    )
    suspend fun deleteResponsesNotIn(collectionId: String, responseIds: List<String>)

    @Transaction
    suspend fun upsert(
        response: CollectionResponseEntity,
        items: List<CollectionResponseItemEntity>
    ) {
        insertResponse(response)
        deleteItemsForResponse(response.id)
        if (items.isNotEmpty()) {
            insertItems(items)
        }
    }

    @Query("SELECT * FROM collection_responses WHERE needsSync = 1 ORDER BY updatedAt DESC")
    suspend fun getResponsesPendingSync(): List<CollectionResponseEntity>

    @Query(
        "UPDATE collection_responses SET status = :status, updatedAt = :updatedAt, " +
            "needsSync = :needsSync WHERE id = :responseId"
    )
    suspend fun updateStatus(
        responseId: String,
        status: String,
        updatedAt: Long,
        needsSync: Boolean
    )

    @Query(
        "UPDATE collection_responses SET needsSync = 0, lastSyncError = NULL, " +
            "updatedAt = :updatedAt WHERE id = :responseId"
    )
    suspend fun markSynced(responseId: String, updatedAt: Long)

    @Query("UPDATE collection_responses SET lastSyncError = :error WHERE id = :responseId")
    suspend fun markSyncError(responseId: String, error: String?)
}

