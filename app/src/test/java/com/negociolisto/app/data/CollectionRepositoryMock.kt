package com.negociolisto.app.data

import com.negociolisto.app.domain.model.Collection
import com.negociolisto.app.domain.model.CollectionItem
import com.negociolisto.app.domain.model.CollectionStatus
import com.negociolisto.app.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

object CollectionRepositoryMock : CollectionRepository {
    private val collections = MutableStateFlow<List<Collection>>(emptyList())

    override fun getCollections(): Flow<List<Collection>> = collections.asStateFlow()

    override fun searchCollections(query: String): Flow<List<Collection>> =
        collections.asStateFlow().map { list ->
            val q = query.trim().lowercase()
            if (q.isEmpty()) list else list.filter { it.name.lowercase().contains(q) || (it.description?.lowercase()?.contains(q) == true) }
        }

    override suspend fun getById(id: String): Collection? = collections.value.firstOrNull { it.id == id }

    override suspend fun addCollection(collection: Collection) {
        collections.update { it + collection }
    }

    override suspend fun updateCollection(collection: Collection) {
        collections.update { list -> list.map { if (it.id == collection.id) collection else it } }
    }

    override suspend fun deleteCollection(id: String) {
        collections.update { list -> list.filterNot { it.id == id } }
    }

    private fun seed(name: String, productIds: List<String>): Collection {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return Collection(
            id = UUID.randomUUID().toString(),
            name = name,
            description = null,
            items = productIds.mapIndexed { index, pid ->
                CollectionItem(
                    productId = pid,
                    notes = null,
                    displayOrder = index,
                    isFeatured = index == 0,
                    specialPrice = null
                )
            },
            associatedCustomerIds = emptyList(),
            createdAt = now,
            updatedAt = now,
            status = CollectionStatus.ACTIVE,
            color = null
        )
    }

}
