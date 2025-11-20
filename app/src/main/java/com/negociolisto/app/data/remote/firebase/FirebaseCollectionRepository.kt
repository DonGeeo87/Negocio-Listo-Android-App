package com.negociolisto.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.utils.AppLogger
import com.google.firebase.firestore.WriteBatch
import com.negociolisto.app.domain.model.Collection
import com.negociolisto.app.domain.model.CollectionItem
import com.negociolisto.app.domain.repository.CollectionRepository
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.CustomerRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCollectionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val inventoryRepository: InventoryRepository, // ✅ Para obtener datos completos de productos
    private val customerRepository: CustomerRepository, // ✅ Para obtener datos del cliente asociado
    private val firebaseAuth: FirebaseAuth // ✅ Para obtener userId del usuario actual
) : CollectionRepository {

    private val collectionsRef = firestore.collection("collections")

    private fun getItemsRef(collectionId: String) =
        collectionsRef.document(collectionId).collection("items")

    override fun getCollections(): Flow<List<Collection>> = callbackFlow {
        // ✅ Obtener userId del usuario actual
        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid
        
        if (userId == null || userId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        // ✅ Filtrar colecciones por userId
        val listener = collectionsRef
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val collections = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val description = doc.getString("description")
                    val associatedCustomerIds = (doc.get("associatedCustomerIds") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList()
                    val customerAccessTokens = (doc.get("customerAccessTokens") as? Map<*, *>)
                        ?.mapKeys { it.key.toString() }
                        ?.mapValues { it.value.toString() }
                        ?.toMap() ?: emptyMap()
                    val status = doc.getString("status") ?: "Draft"
                    val color = doc.getString("color")
                    val enableChat = doc.getBoolean("enableChat") ?: true
                    val webTemplateString = doc.getString("webTemplate") ?: "MODERN"
                    val webTemplate = try {
                        com.negociolisto.app.domain.model.CollectionWebTemplate.valueOf(webTemplateString)
                    } catch (e: Exception) {
                        com.negociolisto.app.domain.model.CollectionWebTemplate.MODERN
                    }

                    Collection(
                        id = doc.id,
                        name = name,
                        description = description,
                        items = emptyList(), // Los items se cargan por separado si es necesario
                        associatedCustomerIds = associatedCustomerIds,
                        customerAccessTokens = customerAccessTokens,
                        createdAt = parseDateTime(doc.getString("createdAt")),
                        updatedAt = parseDateTime(doc.getString("updatedAt")),
                        status = com.negociolisto.app.domain.model.CollectionStatus.valueOf(status),
                        color = color,
                        enableChat = enableChat,
                        webTemplate = webTemplate
                    )
                } catch (_: Exception) {
                    null
                }
            } ?: emptyList()

            trySend(collections)
        }

        awaitClose { listener.remove() }
    }

    override fun searchCollections(query: String): Flow<List<Collection>> = getCollections()

    override suspend fun getById(id: String): Collection? {
        return try {
            // ✅ Obtener userId del usuario actual
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            
            if (userId == null || userId.isEmpty()) {
                return null
            }
            
            val doc = collectionsRef.document(id).get().await()
            if (!doc.exists()) return null
            
            // ✅ Verificar que la colección pertenece al usuario actual
            val docUserId = doc.getString("userId")
            if (docUserId != userId) {
                AppLogger.w("FirebaseCollection", "⚠️ Intento de acceso a colección de otro usuario: $id")
                return null
            }

            val name = doc.getString("name") ?: return null
            val description = doc.getString("description")
            val associatedCustomerIds = (doc.get("associatedCustomerIds") as? List<*>)
                ?.filterIsInstance<String>() ?: emptyList()
            val customerAccessTokens = (doc.get("customerAccessTokens") as? Map<*, *>)
                ?.mapKeys { it.key.toString() }
                ?.mapValues { it.value.toString() }
                ?.toMap() ?: emptyMap()
            val status = doc.getString("status") ?: "Draft"
            val color = doc.getString("color")
            val enableChat = doc.getBoolean("enableChat") ?: true
            val webTemplateString = doc.getString("webTemplate") ?: "MODERN"
            val webTemplate = try {
                com.negociolisto.app.domain.model.CollectionWebTemplate.valueOf(webTemplateString)
            } catch (e: Exception) {
                com.negociolisto.app.domain.model.CollectionWebTemplate.MODERN
            }

            // Cargar items desde subcolección
            val itemsSnapshot = getItemsRef(id).get().await()
            val items = itemsSnapshot.documents.mapNotNull { itemDoc ->
                try {
                    CollectionItem(
                        productId = itemDoc.id,
                        notes = itemDoc.getString("notes"),
                        displayOrder = (itemDoc.getLong("displayOrder") ?: 0).toInt(),
                        isFeatured = itemDoc.getBoolean("isFeatured") ?: false,
                        specialPrice = itemDoc.getDouble("specialPrice")
                    )
                } catch (_: Exception) {
                    null
                }
            }

            Collection(
                id = doc.id,
                name = name,
                description = description,
                items = items,
                associatedCustomerIds = associatedCustomerIds,
                customerAccessTokens = customerAccessTokens,
                createdAt = parseDateTime(doc.getString("createdAt")),
                updatedAt = parseDateTime(doc.getString("updatedAt")),
                status = com.negociolisto.app.domain.model.CollectionStatus.valueOf(status),
                color = color,
                enableChat = enableChat,
                webTemplate = webTemplate
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addCollection(collection: Collection) {
        try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val nowString = now.toInstant(TimeZone.currentSystemDefault()).toString()

            // ✅ Obtener datos completos de productos para incluir en items
            val itemsWithProductData = collection.items.mapNotNull { item ->
                try {
                    val product = inventoryRepository.getProductById(item.productId)
                    if (product != null) {
                        AppLogger.d("FirebaseCollection", "Producto encontrado: ${product.id} - ${product.name}")
                        item to product
                    } else {
                        AppLogger.w("FirebaseCollection", "Producto no encontrado: ${item.productId}")
                        item to null
                    }
                } catch (e: Exception) {
                    AppLogger.e("FirebaseCollection", "Error obteniendo producto ${item.productId}", e)
                    item to null
                }
            }

            // ✅ Obtener userId del usuario actual
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            
            // Preparar datos del documento de colección
            val collectionData = hashMapOf<String, Any>(
                "name" to collection.name,
                "description" to (collection.description ?: ""),
                "associatedCustomerIds" to collection.associatedCustomerIds,
                "status" to collection.status.name,
                "createdAt" to (collection.createdAt.toInstant(TimeZone.currentSystemDefault()).toString()),
                "updatedAt" to nowString,
                "enableChat" to collection.enableChat,
                "webTemplate" to collection.webTemplate.name,
                "isPublic" to (collection.status.name == "SHARED" || collection.status.name == "ACTIVE"),
                "public" to (collection.status.name == "SHARED" || collection.status.name == "ACTIVE")
            )
            
            // ✅ Agregar userId si está disponible
            if (userId != null) {
                collectionData["userId"] = userId
                AppLogger.d("FirebaseCollection", "✅ userId agregado a colección: $userId")
            } else {
                AppLogger.w("FirebaseCollection", "⚠️ No hay usuario autenticado, userId no agregado")
            }
            
            // Asegurar que customerAccessTokens sea un HashMap para Firestore
            if (collection.customerAccessTokens.isNotEmpty()) {
                val tokensMap = hashMapOf<String, String>()
                collection.customerAccessTokens.forEach { (key, value) ->
                    tokensMap[key] = value
                }
                collectionData["customerAccessTokens"] = tokensMap
            } else {
                collectionData["customerAccessTokens"] = hashMapOf<String, String>()
            }
            
            collection.color?.let { collectionData["color"] = it }
            
            // ✅ Obtener y guardar datos del cliente asociado (para el portal web)
            if (collection.associatedCustomerIds.isNotEmpty()) {
                try {
                    val customerId = collection.associatedCustomerIds.first()
                    val customer = customerRepository.getCustomerById(customerId)
                    if (customer != null) {
                        collectionData["associatedCustomerId"] = customer.id
                        collectionData["associatedCustomerName"] = customer.name
                        collectionData["associatedCustomerEmail"] = (customer.email ?: "")
                        collectionData["associatedCustomerPhone"] = (customer.phone ?: "")
                        AppLogger.d("FirebaseCollection", "✅ Datos del cliente asociado guardados: ${customer.name}")
                    }
                } catch (e: Exception) {
                    AppLogger.w("FirebaseCollection", "⚠️ No se pudieron obtener datos del cliente asociado: ${e.message}")
                }
            }

            // ✅ Preparar items como mapa con datos completos del producto
            val itemsMap = itemsWithProductData.associate { (item, product) ->
                val itemData = hashMapOf<String, Any?>(
                    "productId" to item.productId,
                    "id" to item.productId,
                    "notes" to item.notes,
                    "displayOrder" to item.displayOrder,
                    "isFeatured" to item.isFeatured,
                    "specialPrice" to item.specialPrice
                )
                
                // Si tenemos el producto completo, incluir todos sus datos
                if (product != null) {
                    itemData["name"] = product.name
                    itemData["sku"] = product.sku
                    itemData["description"] = (product.description ?: "")
                    itemData["salePrice"] = product.salePrice
                    itemData["purchasePrice"] = product.purchasePrice
                    itemData["stockQuantity"] = product.stockQuantity
                    itemData["currentStock"] = product.stockQuantity
                    itemData["minimumStock"] = product.minimumStock
                    itemData["photoUrl"] = (product.photoUrl ?: "")
                    itemData["imageUrl"] = (product.photoUrl ?: "") // Compatibilidad: la mini-web busca imageUrl
                    itemData["thumbnailUrl"] = (product.thumbnailUrl ?: "")
                    itemData["customCategoryId"] = product.customCategoryId
                    itemData["supplier"] = (product.supplier ?: "")
                    itemData["isActive"] = true
                }
                
                item.productId to itemData
            }
            collectionData["items"] = itemsMap

            // Usar batch para transacción atómica
            val batch = firestore.batch()
            val collectionRef = collectionsRef.document(collection.id)
            
            // Guardar documento de colección
            batch.set(collectionRef, collectionData)

            // ✅ Guardar items en subcolección con datos completos
            val itemsRef = getItemsRef(collection.id)
            itemsWithProductData.forEach { (item, product) ->
                val itemData = hashMapOf<String, Any?>(
                    "productId" to item.productId,
                    "id" to item.productId,
                    "notes" to item.notes,
                    "displayOrder" to item.displayOrder,
                    "isFeatured" to item.isFeatured,
                    "specialPrice" to item.specialPrice
                )
                
                // Incluir datos completos del producto si está disponible
                if (product != null) {
                    itemData["name"] = product.name
                    itemData["sku"] = product.sku
                    itemData["description"] = (product.description ?: "")
                    itemData["salePrice"] = product.salePrice
                    itemData["purchasePrice"] = product.purchasePrice
                    itemData["stockQuantity"] = product.stockQuantity
                    itemData["currentStock"] = product.stockQuantity
                    itemData["minimumStock"] = product.minimumStock
                    itemData["photoUrl"] = (product.photoUrl ?: "")
                    itemData["imageUrl"] = (product.photoUrl ?: "")
                    itemData["thumbnailUrl"] = (product.thumbnailUrl ?: "")
                    itemData["customCategoryId"] = product.customCategoryId
                    itemData["supplier"] = (product.supplier ?: "")
                    itemData["isActive"] = true
                }
                
                batch.set(itemsRef.document(item.productId), itemData)
            }

            batch.commit().await()
            AppLogger.d("FirebaseCollection", "Colección guardada con datos completos de productos: ${collection.id}")
        } catch (e: Exception) {
            AppLogger.e("FirebaseCollection", "Error al agregar colección: ${collection.id}", e)
            throw Exception("Error al agregar colección: ${e.message}")
        }
    }

    override suspend fun updateCollection(collection: Collection) {
        try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                .toInstant(TimeZone.currentSystemDefault()).toString()

            // ✅ Obtener datos completos de productos para incluir en items
            val itemsWithProductData = collection.items.mapNotNull { item ->
                try {
                    val product = inventoryRepository.getProductById(item.productId)
                    if (product != null) {
                        AppLogger.d("FirebaseCollection", "Producto encontrado: ${product.id} - ${product.name}")
                        item to product
                    } else {
                        AppLogger.w("FirebaseCollection", "Producto no encontrado: ${item.productId}")
                        item to null
                    }
                } catch (e: Exception) {
                    AppLogger.e("FirebaseCollection", "Error obteniendo producto ${item.productId}", e)
                    item to null
                }
            }

            // ✅ Obtener userId del usuario actual
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            
            // Preparar datos del documento de colección
            val collectionData = hashMapOf<String, Any>(
                "name" to collection.name,
                "description" to (collection.description ?: ""),
                "associatedCustomerIds" to collection.associatedCustomerIds,
                "status" to collection.status.name,
                "updatedAt" to now,
                "enableChat" to collection.enableChat,
                "webTemplate" to collection.webTemplate.name,
                "isPublic" to (collection.status.name == "SHARED" || collection.status.name == "ACTIVE"),
                "public" to (collection.status.name == "SHARED" || collection.status.name == "ACTIVE")
            )
            
            // ✅ Agregar userId si está disponible
            if (userId != null) {
                collectionData["userId"] = userId
                AppLogger.d("FirebaseCollection", "✅ userId agregado a colección actualizada: $userId")
            } else {
                AppLogger.w("FirebaseCollection", "⚠️ No hay usuario autenticado, userId no agregado")
            }
            
            // Asegurar que customerAccessTokens sea un HashMap para Firestore
            if (collection.customerAccessTokens.isNotEmpty()) {
                val tokensMap = hashMapOf<String, String>()
                collection.customerAccessTokens.forEach { (key, value) ->
                    tokensMap[key] = value
                }
                collectionData["customerAccessTokens"] = tokensMap
            } else {
                collectionData["customerAccessTokens"] = hashMapOf<String, String>()
            }
            
            collection.color?.let { collectionData["color"] = it }
            
            // ✅ Obtener y guardar datos del cliente asociado (para el portal web)
            if (collection.associatedCustomerIds.isNotEmpty()) {
                try {
                    val customerId = collection.associatedCustomerIds.first()
                    val customer = customerRepository.getCustomerById(customerId)
                    if (customer != null) {
                        collectionData["associatedCustomerId"] = customer.id
                        collectionData["associatedCustomerName"] = customer.name
                        collectionData["associatedCustomerEmail"] = (customer.email ?: "")
                        collectionData["associatedCustomerPhone"] = (customer.phone ?: "")
                        AppLogger.d("FirebaseCollection", "✅ Datos del cliente asociado actualizados: ${customer.name}")
                    }
                } catch (e: Exception) {
                    AppLogger.w("FirebaseCollection", "⚠️ No se pudieron obtener datos del cliente asociado: ${e.message}")
                }
            }

            // ✅ Preparar items como mapa con datos completos del producto
            val itemsMap = itemsWithProductData.associate { (item, product) ->
                val itemData = hashMapOf<String, Any?>(
                    "productId" to item.productId,
                    "id" to item.productId,
                    "notes" to item.notes,
                    "displayOrder" to item.displayOrder,
                    "isFeatured" to item.isFeatured,
                    "specialPrice" to item.specialPrice
                )
                
                // Si tenemos el producto completo, incluir todos sus datos
                if (product != null) {
                    itemData["name"] = product.name
                    itemData["sku"] = product.sku
                    itemData["description"] = (product.description ?: "")
                    itemData["salePrice"] = product.salePrice
                    itemData["purchasePrice"] = product.purchasePrice
                    itemData["stockQuantity"] = product.stockQuantity
                    itemData["currentStock"] = product.stockQuantity
                    itemData["minimumStock"] = product.minimumStock
                    itemData["photoUrl"] = (product.photoUrl ?: "")
                    itemData["imageUrl"] = (product.photoUrl ?: "") // Compatibilidad: la mini-web busca imageUrl
                    itemData["thumbnailUrl"] = (product.thumbnailUrl ?: "")
                    itemData["customCategoryId"] = product.customCategoryId
                    itemData["supplier"] = (product.supplier ?: "")
                    itemData["isActive"] = true
                }
                
                item.productId to itemData
            }
            collectionData["items"] = itemsMap

            // Usar batch para transacción atómica
            val batch = firestore.batch()
            val collectionRef = collectionsRef.document(collection.id)
            
            // Actualizar documento de colección
            batch.update(collectionRef, collectionData)

            // Eliminar items antiguos de la subcolección
            val itemsRef = getItemsRef(collection.id)
            val existingItems = itemsRef.get().await()
            existingItems.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // ✅ Guardar nuevos items en subcolección con datos completos
            itemsWithProductData.forEach { (item, product) ->
                val itemData = hashMapOf<String, Any?>(
                    "productId" to item.productId,
                    "id" to item.productId,
                    "notes" to item.notes,
                    "displayOrder" to item.displayOrder,
                    "isFeatured" to item.isFeatured,
                    "specialPrice" to item.specialPrice
                )
                
                // Incluir datos completos del producto si está disponible
                if (product != null) {
                    itemData["name"] = product.name
                    itemData["sku"] = product.sku
                    itemData["description"] = (product.description ?: "")
                    itemData["salePrice"] = product.salePrice
                    itemData["purchasePrice"] = product.purchasePrice
                    itemData["stockQuantity"] = product.stockQuantity
                    itemData["currentStock"] = product.stockQuantity
                    itemData["minimumStock"] = product.minimumStock
                    itemData["photoUrl"] = (product.photoUrl ?: "")
                    itemData["imageUrl"] = (product.photoUrl ?: "")
                    itemData["thumbnailUrl"] = (product.thumbnailUrl ?: "")
                    itemData["customCategoryId"] = product.customCategoryId
                    itemData["supplier"] = (product.supplier ?: "")
                    itemData["isActive"] = true
                }
                
                batch.set(itemsRef.document(item.productId), itemData)
            }

            batch.commit().await()
            AppLogger.d("FirebaseCollection", "Colección actualizada con datos completos de productos: ${collection.id}")
        } catch (e: Exception) {
            AppLogger.e("FirebaseCollection", "Error al actualizar colección: ${collection.id}", e)
            throw Exception("Error al actualizar colección: ${e.message}")
        }
    }

    override suspend fun deleteCollection(id: String) {
        try {
            // Eliminar items de la subcolección
            val itemsRef = getItemsRef(id)
            val itemsSnapshot = itemsRef.get().await()
            val batch = firestore.batch()
            
            itemsSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // Eliminar documento de colección
            batch.delete(collectionsRef.document(id))
            
            batch.commit().await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar colección: ${e.message}")
        }
    }

    override suspend fun updateTemplateForCustomer(customerId: String, template: com.negociolisto.app.domain.model.CollectionWebTemplate) {
        try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                .toInstant(TimeZone.currentSystemDefault()).toString()
            
            // Buscar todas las colecciones asociadas al cliente
            val collectionsSnapshot = collectionsRef
                .whereArrayContains("associatedCustomerIds", customerId)
                .get()
                .await()
            
            // Actualizar el template de todas las colecciones en un batch
            val batch = firestore.batch()
            collectionsSnapshot.documents.forEach { doc ->
                batch.update(doc.reference, mapOf(
                    "webTemplate" to template.name,
                    "updatedAt" to now
                ))
            }
            
            batch.commit().await()
            AppLogger.d("FirebaseCollection", "Template actualizado para ${collectionsSnapshot.documents.size} colecciones del cliente $customerId")
        } catch (e: Exception) {
            AppLogger.e("FirebaseCollection", "Error al actualizar template por cliente: $customerId", e)
            throw Exception("Error al actualizar template por cliente: ${e.message}")
        }
    }
    
    override suspend fun getTotalCollectionCount(): Int {
        return try {
            // ✅ Obtener userId del usuario actual
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            
            if (userId == null || userId.isEmpty()) {
                return 0
            }
            
            // ✅ Filtrar por userId
            val snapshot = collectionsRef
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            AppLogger.e("FirebaseCollection", "Error obteniendo conteo de colecciones", e)
            0
        }
    }

    private fun parseDateTime(dateString: String?): kotlinx.datetime.LocalDateTime {
        return if (dateString != null) {
            try {
                Instant.parse(dateString).toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (_: Exception) {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }
        } else {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}


