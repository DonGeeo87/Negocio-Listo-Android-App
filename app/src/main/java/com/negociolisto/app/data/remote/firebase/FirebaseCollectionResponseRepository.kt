package com.negociolisto.app.data.remote.firebase

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.domain.model.CollectionResponse
import com.negociolisto.app.domain.model.OrderItem
import com.negociolisto.app.domain.model.OrderStatus
import com.negociolisto.app.domain.model.OrderLocation
import com.negociolisto.app.domain.repository.CollectionResponseRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📋 IMPLEMENTACIÓN DEL REPOSITORIO DE RESPUESTAS CON FIRESTORE
 * 
 * Gestiona los pedidos/respuestas en tiempo real usando Firestore.
 * Los pedidos se almacenan en: collections/{collectionId}/responses/{responseId}
 */
@Singleton
class FirebaseCollectionResponseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CollectionResponseRepository {

    private fun getResponsesRef(collectionId: String) =
        firestore.collection("collections").document(collectionId).collection("responses")

    override fun getResponses(collectionId: String): Flow<List<CollectionResponse>> = callbackFlow {
        val listener = getResponsesRef(collectionId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val responses = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toCollectionResponse(collectionId)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(responses)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getResponseById(responseId: String): CollectionResponse? {
        return try {
            val snapshot = firestore.collectionGroup("responses")
                .whereEqualTo(FieldPath.documentId(), responseId)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull() ?: return null

            val parentCollectionId = snapshot.reference.parent.parent?.id ?: return null
            snapshot.toCollectionResponse(parentCollectionId)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addResponse(response: CollectionResponse) {
        try {
            val data = response.toFirestoreMap()
            getResponsesRef(response.collectionId)
                .document(response.id)
                .set(data)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al crear respuesta: ${e.message}")
        }
    }

    override suspend fun updateResponse(response: CollectionResponse) {
        try {
            val data = response.toFirestoreMap()
            getResponsesRef(response.collectionId)
                .document(response.id)
                .set(data)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar respuesta: ${e.message}")
        }
    }

    override suspend fun updateStatus(responseId: String, status: OrderStatus) {
        try {
            val response = getResponseById(responseId) ?: return
            
            val updatedResponse = response.copy(
                status = status,
                updatedAt = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )

            updateResponse(updatedResponse)
        } catch (e: Exception) {
            throw Exception("Error al actualizar estado: ${e.message}")
        }
    }
}

// Extension functions para convertir entre Firestore y Domain models

private fun com.google.firebase.firestore.DocumentSnapshot.toCollectionResponse(collectionId: String): CollectionResponse {
    val itemsMapRaw = (get("items") as? Map<*, *>) ?: emptyMap<Any?, Any?>()
    val itemsMap = buildMap<String, OrderItem> {
        itemsMapRaw.forEach { (key, value) ->
            val itemMap = value as? Map<*, *> ?: return@forEach
            val productId = key as? String ?: return@forEach
            val orderItem = OrderItem(
                quantity = (itemMap["quantity"] as? Long)?.toInt() ?: 0,
                rating = (itemMap["rating"] as? Long)?.toInt(),
                notes = itemMap["notes"] as? String,
                customization = itemMap["customization"] as? String
            )
            put(productId, orderItem)
        }
    }

    val locationMap = get("location") as? Map<*, *>
    val location = locationMap?.let {
        OrderLocation(
            city = it["city"] as? String,
            region = it["region"] as? String
        )
    }

    return CollectionResponse(
        id = id,
        collectionId = collectionId,
        clientName = getString("clientName") ?: "",
        clientEmail = getString("clientEmail") ?: "",
        clientPhone = getString("clientPhone") ?: "",
        deliveryMethod = getString("deliveryMethod") ?: "",
        address = getString("address"),
        paymentMethod = getString("paymentMethod") ?: "",
        desiredDate = (getString("desiredDate"))?.let { 
            Instant.parse(it).toLocalDateTime(TimeZone.currentSystemDefault()) 
        },
        urgent = getBoolean("urgent") ?: false,
        observations = getString("observations"),
        items = itemsMap,
        subtotal = when (val subtotalValue = get("subtotal")) {
            is Number -> subtotalValue.toDouble()
            else -> 0.0
        },
        itemCount = (get("itemCount") as? Long)?.toInt() ?: 0,
        status = OrderStatus.valueOf(getString("status") ?: "APPROVED"),
        feedbackComments = getString("feedbackComments"),
        consentToContact = getBoolean("consentToContact") ?: false,
        businessNotes = getString("businessNotes"),
        location = location,
        tags = (get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        createdAt = Instant.parse(getString("createdAt") ?: throw Exception("createdAt requerido"))
            .toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = Instant.parse(getString("updatedAt") ?: throw Exception("updatedAt requerido"))
            .toLocalDateTime(TimeZone.currentSystemDefault())
    )
}

private fun CollectionResponse.toFirestoreMap(): Map<String, Any?> {
    val itemsMap: Map<String, Map<String, Any?>> = items.mapValues { (_, item) ->
        mapOf<String, Any?>(
            "quantity" to item.quantity,
            "rating" to item.rating,
            "notes" to item.notes,
            "customization" to item.customization
        )
    }

    val locationMap = location?.let {
        mapOf(
            "city" to it.city,
            "region" to it.region
        )
    }

    return mapOf(
        "collectionId" to collectionId,
        "clientName" to clientName,
        "clientEmail" to clientEmail,
        "clientPhone" to clientPhone,
        "deliveryMethod" to deliveryMethod,
        "address" to address,
        "paymentMethod" to paymentMethod,
        "desiredDate" to desiredDate?.toInstant(TimeZone.currentSystemDefault())?.toString(),
        "urgent" to urgent,
        "observations" to observations,
        "items" to itemsMap,
        "subtotal" to subtotal,
        "itemCount" to itemCount,
        "status" to status.name,
        "feedbackComments" to feedbackComments,
        "consentToContact" to consentToContact,
        "businessNotes" to businessNotes,
        "location" to locationMap,
        "tags" to tags,
        "createdAt" to createdAt.toInstant(TimeZone.currentSystemDefault()).toString(),
        "updatedAt" to updatedAt.toInstant(TimeZone.currentSystemDefault()).toString()
    )
}
