package com.negociolisto.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.negociolisto.app.domain.model.Sale
import com.negociolisto.app.domain.repository.SalesRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔥 IMPLEMENTACIÓN DE SALES REPOSITORY CON FIRESTORE
 * 
 * Esta clase implementa el SalesRepository usando Firebase Firestore
 * para sincronización en la nube.
 */
@Singleton
class FirebaseSalesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : SalesRepository {

    private val salesCollection = firestore.collection("sales")

    override fun getSales(): Flow<List<Sale>> = callbackFlow {
        val listener = salesCollection
            .orderBy("saleDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val sales = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Sale>()?.copy(id = doc.id)
                } ?: emptyList()

                trySend(sales)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getSaleById(id: String): Sale? {
        return try {
            val document = salesCollection.document(id).get().await()
            document.toObject<Sale>()?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun recordSale(sale: Sale) {
        try {
            salesCollection.add(sale).await()
        } catch (e: Exception) {
            throw Exception("Error al registrar venta: ${e.message}")
        }
    }

    override suspend fun updateSale(sale: Sale) {
        try {
            salesCollection.document(sale.id).set(sale).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar venta: ${e.message}")
        }
    }

    override suspend fun cancelSale(id: String, reason: String?) {
        try {
            val data = hashMapOf<String, Any?>(
                "status" to com.negociolisto.app.domain.model.SaleStatus.CANCELED.name,
                "canceledReason" to reason,
                "canceledAt" to com.google.firebase.Timestamp.now()
            )
            salesCollection.document(id).update(data as Map<String, Any>).await()
        } catch (_: Exception) { }
    }

    override suspend fun deleteSale(id: String) {
        try { salesCollection.document(id).delete().await() } catch (_: Exception) { }
    }
}
