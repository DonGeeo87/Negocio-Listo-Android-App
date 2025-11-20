package com.negociolisto.app.data.repository

import com.negociolisto.app.data.local.dao.CollectionResponseDao
import com.negociolisto.app.data.local.entity.CollectionResponseEntity
import com.negociolisto.app.data.local.entity.toDomain
import com.negociolisto.app.data.local.entity.toEntity
import com.negociolisto.app.data.remote.firebase.FirebaseCollectionResponseRepository
import com.negociolisto.app.domain.model.CollectionResponse
import com.negociolisto.app.domain.model.OrderStatus
import com.negociolisto.app.domain.repository.CollectionResponseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📋 IMPLEMENTACIÓN HÍBRIDA DEL REPOSITORIO DE RESPUESTAS
 *
 * Lee y escribe primero en Room para uso offline y se sincroniza con Firestore
 * cuando la red está disponible.
 */
@Singleton
class CollectionResponseRepositoryImpl @Inject constructor(
    private val firebaseCollectionResponseRepository: FirebaseCollectionResponseRepository,
    private val collectionResponseDao: CollectionResponseDao
) : CollectionResponseRepository {

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeSyncJobs = mutableMapOf<String, Job>()

    override fun getResponses(collectionId: String): Flow<List<CollectionResponse>> {
        return collectionResponseDao.observeResponsesByCollection(collectionId)
            .map { list -> list.map { it.toDomain() } }
            .onStart {
                ensureRemoteSync(collectionId)
            }
    }

    override suspend fun getResponseById(responseId: String): CollectionResponse? {
        val local = collectionResponseDao.getResponseWithItems(responseId)?.toDomain()
        if (local != null) {
            return local
        }

        val remote = firebaseCollectionResponseRepository.getResponseById(responseId)
        if (remote != null) {
            cacheResponse(remote, forceNeedsSync = false)
        }
        return remote
    }

    override suspend fun addResponse(response: CollectionResponse) {
        cacheResponse(response, forceNeedsSync = false)
        firebaseCollectionResponseRepository.addResponse(response)
    }

    override suspend fun updateResponse(response: CollectionResponse) {
        cacheResponse(response, forceNeedsSync = true)
        try {
            firebaseCollectionResponseRepository.updateResponse(response)
            markSynced(response.id)
        } catch (e: Exception) {
            collectionResponseDao.markSyncError(response.id, e.message)
            throw e
        }
    }

    override suspend fun updateStatus(responseId: String, status: OrderStatus) {
        val now = Clock.System.now().toEpochMilliseconds()
        collectionResponseDao.updateStatus(
            responseId = responseId,
            status = status.name,
            updatedAt = now,
            needsSync = true
        )

        try {
            firebaseCollectionResponseRepository.updateStatus(responseId, status)
            markSynced(responseId)
        } catch (e: Exception) {
            collectionResponseDao.markSyncError(responseId, e.message)
            println("⚠️ Estado de pedido pendiente de sincronización (${responseId}): ${e.message}")
        }
    }

    private fun ensureRemoteSync(collectionId: String) {
        if (activeSyncJobs.containsKey(collectionId)) return
        val job = syncScope.launch {
            firebaseCollectionResponseRepository.getResponses(collectionId).collect { responses ->
                cacheResponses(collectionId, responses)
                trySyncPendingResponses()
            }
        }
        activeSyncJobs[collectionId] = job
    }

    private suspend fun cacheResponses(
        collectionId: String,
        responses: List<CollectionResponse>
    ) {
        if (responses.isEmpty()) {
            collectionResponseDao.deleteResponsesForCollection(collectionId)
            return
        }

        val ids = responses.map { it.id }
        collectionResponseDao.deleteResponsesNotIn(collectionId, ids)
        responses.forEach { cacheResponse(it, forceNeedsSync = false) }
    }

    private suspend fun cacheResponse(
        response: CollectionResponse,
        forceNeedsSync: Boolean
    ) {
        val existing: CollectionResponseEntity? = collectionResponseDao.getResponseEntity(response.id)
        if (existing?.needsSync == true && !forceNeedsSync) {
            // Mantener la versión local hasta que se sincronice
            return
        }

        val (entity, items) = response.toEntity(
            needsSync = forceNeedsSync || (existing?.needsSync ?: false),
            lastSyncError = if (forceNeedsSync || existing?.needsSync == true) {
                existing?.lastSyncError
            } else {
                null
            }
        )
        collectionResponseDao.upsert(entity, items)
    }

    private suspend fun markSynced(responseId: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        collectionResponseDao.markSynced(responseId, now)
    }

    private suspend fun trySyncPendingResponses() {
        val pending = collectionResponseDao.getResponsesPendingSync()
        if (pending.isEmpty()) return

        for (entity in pending) {
            val status = runCatching { OrderStatus.valueOf(entity.status) }.getOrNull() ?: continue
            try {
                firebaseCollectionResponseRepository.updateStatus(entity.id, status)
                markSynced(entity.id)
            } catch (e: Exception) {
                collectionResponseDao.markSyncError(entity.id, e.message)
            }
        }
    }
}
