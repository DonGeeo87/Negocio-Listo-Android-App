package com.negociolisto.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.domain.model.Collection
import com.negociolisto.app.domain.model.CollectionWebTemplate
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.CollectionRepository
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.data.local.dao.CollectionDao
import com.negociolisto.app.data.local.entity.toDomain
import com.negociolisto.app.data.local.entity.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📚 IMPLEMENTACIÓN DEL REPOSITORIO DE COLECCIONES
 * 
 * Maneja los datos de colecciones usando Room Database con sincronización automática a Firebase.
 */
@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val inventoryRepository: InventoryRepository
) : CollectionRepository {

    override fun getCollections(): Flow<List<Collection>> {
        return authRepository.currentUser.flatMapLatest { user ->
            if (user != null && user.id.isNotEmpty()) {
                collectionDao.getCollections(user.id).map { list ->
                    list.map { it.collection.toDomain(it.items) }
                }
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
    }

    override suspend fun getById(id: String): Collection? {
        val currentUser = authRepository.currentUser.first()
        if (currentUser == null || currentUser.id.isEmpty()) return null
        val rel = collectionDao.getById(id, currentUser.id) ?: return null
        return rel.collection.toDomain(rel.items)
    }

    override suspend fun addCollection(collection: Collection) {
        // Obtener userId del usuario actual
        val currentUser = authRepository.currentUser.first()
        if (currentUser == null || currentUser.id.isEmpty()) {
            throw IllegalStateException("Usuario no autenticado. No se puede agregar colección.")
        }
        
        // Guardar localmente primero
        val (col, items) = collection.toEntity(currentUser.id)
        collectionDao.upsert(col, items)
        
        // Sincronizar automáticamente con Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    syncCollectionToFirebase(collection, currentUser.id)
                    println("✅ Colección ${collection.id} sincronizada automáticamente con Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error sincronizando colección con Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }

    override suspend fun updateCollection(collection: Collection) {
        // Obtener userId del usuario actual
        val currentUser = authRepository.currentUser.first()
        if (currentUser == null || currentUser.id.isEmpty()) {
            throw IllegalStateException("Usuario no autenticado. No se puede actualizar colección.")
        }
        
        // Actualizar localmente primero
        val (col, items) = collection.toEntity(currentUser.id)
        collectionDao.upsert(col, items)
        
        // Sincronizar automáticamente con Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    syncCollectionToFirebase(collection, currentUser.id)
                    println("✅ Colección ${collection.id} actualizada automáticamente en Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error sincronizando actualización de colección con Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }

    override suspend fun deleteCollection(id: String) {
        // Obtener userId del usuario actual para verificar eliminación
        val currentUser = authRepository.currentUser.first()
        if (currentUser == null || currentUser.id.isEmpty()) {
            println("⚠️ Usuario no autenticado, no se puede eliminar colección")
            return
        }
        
        // Eliminar localmente primero - esto debería hacer que el Flow emita automáticamente
        collectionDao.deleteCollection(id)
        
        // Verificar que la eliminación se completó correctamente
        val deleted = collectionDao.getById(id, currentUser.id)
        if (deleted != null) {
            println("⚠️ Advertencia: La colección $id aún existe después de deleteCollection")
        } else {
            println("✅ Colección $id eliminada correctamente de Room")
        }
        
        // Eliminar de Firebase en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    firestore.collection("users/${currentUser.id}/collections")
                        .document(id)
                        .delete()
                        .await()
                    
                    println("✅ Colección $id eliminada automáticamente de Firebase")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                println("⚠️ Error eliminando colección de Firebase: ${e.message}")
                // No lanzamos excepción para no bloquear la operación local
            }
        }
    }
    
    /**
     * 🔄 SINCRONIZAR COLECCIÓN A FIREBASE
     * 
     * Sincroniza una colección completa con sus items a Firebase.
     */
    private suspend fun syncCollectionToFirebase(collection: Collection, userId: String) {
        try {
            // Obtener datos completos de productos para los items
            val itemsWithProductData = collection.items.map { item ->
                try {
                    val product = inventoryRepository.getProductById(item.productId)
                    item to product
                } catch (e: Exception) {
                    item to null
                }
            }
            
            // Preparar items con datos de productos
            val itemsList = itemsWithProductData.map { (item, product) ->
                val itemData = hashMapOf<String, Any?>(
                    "productId" to item.productId,
                    "notes" to (item.notes ?: ""),
                    "displayOrder" to item.displayOrder,
                    "isFeatured" to item.isFeatured,
                    "specialPrice" to item.specialPrice
                )
                
                // Incluir datos del producto si está disponible
                product?.let {
                    itemData["name"] = it.name
                    itemData["sku"] = it.sku
                    itemData["description"] = (it.description ?: "")
                    itemData["salePrice"] = it.salePrice
                    itemData["photoUrl"] = (it.photoUrl ?: "")
                    itemData["thumbnailUrl"] = (it.thumbnailUrl ?: "")
                }
                
                itemData
            }
            
            val collectionMap = hashMapOf<String, Any?>(
                "id" to collection.id,
                "name" to collection.name,
                "description" to (collection.description ?: ""),
                "status" to collection.status.name,
                "webTemplate" to collection.webTemplate.name,
                "enableChat" to collection.enableChat,
                "color" to (collection.color ?: ""),
                "associatedCustomerIds" to collection.associatedCustomerIds,
                "createdAt" to collection.createdAt.toString(),
                "updatedAt" to collection.updatedAt.toString(),
                "items" to itemsList
            )
            
            firestore.collection("users/$userId/collections")
                .document(collection.id)
                .set(collectionMap)
                .await()
        } catch (e: Exception) {
            println("⚠️ Error en syncCollectionToFirebase: ${e.message}")
            throw e
        }
    }

    override fun searchCollections(query: String): Flow<List<Collection>> {
        val q = query.trim().lowercase()
        return getCollections().map { list ->
            if (q.isEmpty()) list else list.filter {
                it.name.lowercase().contains(q) || (it.description?.lowercase()?.contains(q) == true)
            }
        }
    }
    
    override suspend fun updateTemplateForCustomer(customerId: String, template: CollectionWebTemplate) {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        
        // Usar la función optimizada del DAO que actualiza directamente el template
        // sin necesidad de cargar y reescribir todas las colecciones
        collectionDao.updateTemplateForCustomer(customerId, template.name, now)
    }
    
    override suspend fun getTotalCollectionCount(): Int {
        val currentUser = authRepository.currentUser.first()
        if (currentUser == null || currentUser.id.isEmpty()) return 0
        return collectionDao.getTotalCollectionCount(currentUser.id)
    }
}
