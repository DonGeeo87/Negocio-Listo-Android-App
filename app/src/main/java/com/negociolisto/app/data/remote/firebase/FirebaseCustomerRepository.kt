package com.negociolisto.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.domain.repository.CustomerRepository
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
 * 🔥 IMPLEMENTACIÓN DE CUSTOMER REPOSITORY CON FIRESTORE
 * 
 * Esta clase implementa el CustomerRepository usando Firebase Firestore
 * para sincronización en la nube.
 */
@Singleton
class FirebaseCustomerRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CustomerRepository {

    private val customersCollection = firestore.collection("customers")

    override fun getAllCustomers(): Flow<List<Customer>> = callbackFlow {
        val listener = customersCollection
            .whereEqualTo("isActive", true)
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val customers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toCustomer()
                } ?: emptyList()

                trySend(customers)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getCustomerById(customerId: String): Customer? {
        return try {
            val doc = customersCollection.document(customerId).get().await()
            if (doc.exists()) doc.toCustomer() else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCustomerByPhone(phone: String): Customer? {
        return try {
            val trimmedPhone = phone.trim()
            if (trimmedPhone.isBlank()) return null

            val directMatch = customersCollection
                .whereEqualTo("phone", trimmedPhone)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.let { doc -> doc.toCustomer() }

            directMatch ?: run {
                val normalizedPhone = trimmedPhone.replace(Regex("[^0-9+]"), "")
                val snapshot = customersCollection.get().await()
                snapshot.documents
                    .mapNotNull { doc -> doc.toCustomer() }
                    .firstOrNull { customer ->
                        val customerPhone = customer.phone?.replace(Regex("[^0-9+]"), "") ?: ""
                        customerPhone == normalizedPhone
                    }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCustomerByEmail(email: String): Customer? {
        return try {
            val trimmedEmail = email.trim()
            if (trimmedEmail.isBlank()) return null

            val directMatch = customersCollection
                .whereEqualTo("email", trimmedEmail)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.let { doc -> doc.toCustomer() }

            directMatch ?: run {
                val normalizedEmail = trimmedEmail.lowercase()
                val snapshot = customersCollection.get().await()
                snapshot.documents
                    .mapNotNull { doc -> doc.toCustomer() }
                    .firstOrNull { customer ->
                        customer.email?.lowercase() == normalizedEmail
                    }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun searchCustomers(query: String): Flow<List<Customer>> = callbackFlow {
        val listener = customersCollection
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val allCustomers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toCustomer()
                } ?: emptyList()

                val filteredCustomers = allCustomers.filter { customer ->
                    customer.name.contains(query, ignoreCase = true) ||
                    customer.email?.contains(query, ignoreCase = true) == true ||
                    customer.phone?.contains(query, ignoreCase = true) == true
                }

                trySend(filteredCustomers)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addCustomer(customer: Customer) {
        try {
            val customerData = customer.toFirestoreMap()
            customersCollection.add(customerData).await()
        } catch (e: Exception) {
            throw Exception("Error al agregar cliente: ${e.message}")
        }
    }

    override suspend fun updateCustomer(customer: Customer) {
        try {
            val customerData = customer.toFirestoreMap()
            customersCollection.document(customer.id).set(customerData).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar cliente: ${e.message}")
        }
    }

    override suspend fun deleteCustomer(id: String) {
        try {
            // Soft delete - marcar como inactivo
            customersCollection.document(id).update("isActive", false).await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar cliente: ${e.message}")
        }
    }

    override suspend fun updateCustomerPurchases(customerId: String, totalPurchases: Double, lastPurchaseDate: kotlinx.datetime.LocalDateTime?) {
        try {
            val updates = mutableMapOf<String, Any>(
                "totalPurchases" to totalPurchases
            )
            lastPurchaseDate?.let {
                updates["lastPurchaseDate"] = it.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            }
            customersCollection.document(customerId).update(updates).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar compras del cliente: ${e.message}")
        }
    }
    
    override suspend fun getTotalCustomerCount(): Int {
        return try {
            val snapshot = customersCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 🔑 BUSCAR CLIENTE POR TOKEN DE ACCESO
     * 
     * Busca un cliente usando su token de acceso único.
     * Esta función es esencial para el portal del cliente.
     * 
     * @param token Token de acceso del cliente
     * @return Cliente encontrado o null si no existe
     */
    suspend fun getCustomerByAccessToken(token: String): Customer? {
        return try {
            if (token.isBlank()) return null
            
            // Buscar cliente con este token
            val snapshot = customersCollection
                .whereEqualTo("accessToken", token)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.let { doc ->
                doc.toCustomer()
            }
        } catch (e: Exception) {
            null
        }
    }

}

/**
 * 🔄 EXTENSIÓN PARA CONVERTIR CUSTOMER A MAP DE FIRESTORE
 * 
 * Convierte un objeto Customer a un Map que puede ser guardado en Firestore.
 * Esto asegura que todos los campos se serialicen correctamente, especialmente
 * los tipos complejos como LocalDateTime.
 */
private fun Customer.toFirestoreMap(): Map<String, Any?> {
    val timeZone = TimeZone.currentSystemDefault()
    val createdAtMillis = createdAt.toInstant(timeZone).toEpochMilliseconds()
    val lastPurchaseDateMillis = lastPurchaseDate?.toInstant(timeZone)?.toEpochMilliseconds()
    
    return mapOf(
        "id" to id,
        "name" to name,
        "companyName" to companyName,
        "phone" to phone,
        "email" to email,
        "address" to address,
        "notes" to notes,
        "createdAt" to createdAtMillis,
        "totalPurchases" to totalPurchases,
        "lastPurchaseDate" to lastPurchaseDateMillis,
        "accessToken" to accessToken,
        "isActive" to true // Campo requerido para queries
    )
}

/**
 * 🔄 EXTENSIÓN PARA CONVERTIR DOCUMENTO DE FIRESTORE A CUSTOMER
 * 
 * Convierte un DocumentSnapshot de Firestore a un objeto Customer.
 * Convierte correctamente los timestamps (Long) a LocalDateTime.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toCustomer(): Customer? {
    return try {
        val timeZone = TimeZone.currentSystemDefault()
        val id = this.id
        val name = getString("name") ?: return null
        val companyName = getString("companyName")
        val phone = getString("phone")
        val email = getString("email")
        val address = getString("address")
        val notes = getString("notes")
        val createdAtMillis = getLong("createdAt") ?: return null
        val totalPurchases = getDouble("totalPurchases") ?: 0.0
        val lastPurchaseDateMillis = getLong("lastPurchaseDate")
        val accessToken = getString("accessToken")
        
        Customer(
            id = id,
            name = name,
            companyName = companyName,
            phone = phone,
            email = email,
            address = address,
            notes = notes,
            createdAt = Instant.fromEpochMilliseconds(createdAtMillis).toLocalDateTime(timeZone),
            totalPurchases = totalPurchases,
            lastPurchaseDate = lastPurchaseDateMillis?.let { 
                Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone) 
            },
            accessToken = accessToken
        )
    } catch (e: Exception) {
        null
    }
}
