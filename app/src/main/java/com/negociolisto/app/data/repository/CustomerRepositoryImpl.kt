package com.negociolisto.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.data.local.dao.CustomerDao
import com.negociolisto.app.data.local.entity.toDomain
import com.negociolisto.app.data.local.entity.toEntity
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 👥 IMPLEMENTACIÓN DEL REPOSITORIO DE CLIENTES
 * 
 * Maneja los datos de clientes usando Room Database.
 */
@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : CustomerRepository {
    
    override fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getCustomerById(customerId: String): Customer? {
        return customerDao.getCustomerById(customerId)?.toDomain()
    }
    
    override suspend fun addCustomer(customer: Customer) {
        // Guardar en Room (fuente de verdad local)
        customerDao.insertCustomer(customer.toEntity())
        
        // Sincronizar con Firestore en segundo plano (ruta privada y pública)
        withContext(Dispatchers.IO) {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val customerData = customer.toFirestoreMap()
                    // Agregar userId para queries del negocio
                    val customerDataWithUserId = customerData + ("userId" to currentUser.id)
                    
                    // Sincronizar en ruta privada: users/{userId}/customers
                    firestore.collection("users/${currentUser.id}/customers")
                        .document(customer.id)
                        .set(customerDataWithUserId)
                        .await()
                    
                    // ✅ Sincronizar también en ruta pública: customers/{customerId} (para el portal web)
                    firestore.collection("customers")
                        .document(customer.id)
                        .set(customerDataWithUserId)
                        .await()
                    
                    println("✅ Cliente ${customer.id} sincronizado automáticamente con Firebase (ruta privada y pública)")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                // Ignorar errores de sincronización, la app funciona offline
                android.util.Log.w("CustomerRepository", "Error sincronizando cliente con Firestore: ${e.message}")
            }
        }
    }

    override suspend fun updateCustomer(customer: Customer) {
        // Guardar en Room (fuente de verdad local)
        customerDao.updateCustomer(customer.toEntity())
        
        // Sincronizar con Firestore en segundo plano (ruta privada y pública)
        withContext(Dispatchers.IO) {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    val customerData = customer.toFirestoreMap()
                    // Agregar userId para queries del negocio
                    val customerDataWithUserId = customerData + ("userId" to currentUser.id)
                    
                    // Sincronizar en ruta privada: users/{userId}/customers
                    firestore.collection("users/${currentUser.id}/customers")
                        .document(customer.id)
                        .set(customerDataWithUserId)
                        .await()
                    
                    // ✅ Sincronizar también en ruta pública: customers/{customerId} (para el portal web)
                    firestore.collection("customers")
                        .document(customer.id)
                        .set(customerDataWithUserId)
                        .await()
                    
                    println("✅ Cliente ${customer.id} actualizado automáticamente en Firebase (ruta privada y pública)")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                // Ignorar errores de sincronización, la app funciona offline
                android.util.Log.w("CustomerRepository", "Error sincronizando cliente con Firestore: ${e.message}")
            }
        }
    }
    
    override suspend fun deleteCustomer(customerId: String) {
        // Eliminar localmente primero
        customerDao.deleteCustomerById(customerId)
        
        // Eliminar de Firebase en segundo plano (ruta privada y pública)
        withContext(Dispatchers.IO) {
            try {
                val currentUser = authRepository.currentUser.first()
                if (currentUser != null) {
                    // Eliminar de ruta privada: users/{userId}/customers
                    firestore.collection("users/${currentUser.id}/customers")
                        .document(customerId)
                        .delete()
                        .await()
                    
                    // ✅ Eliminar también de ruta pública: customers/{customerId}
                    // Usar soft delete (marcar como inactivo) en lugar de eliminar completamente
                    firestore.collection("customers")
                        .document(customerId)
                        .update("isActive", false)
                        .await()
                    
                    println("✅ Cliente $customerId eliminado automáticamente de Firebase (ruta privada y pública)")
                } else {
                    // Usuario no autenticado, no sincronizar
                }
            } catch (e: Exception) {
                android.util.Log.w("CustomerRepository", "Error eliminando cliente de Firestore: ${e.message}")
            }
        }
    }
    
    override fun searchCustomers(query: String): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.filter { 
                it.name.contains(query, ignoreCase = true) ||
                it.email?.contains(query, ignoreCase = true) == true ||
                it.phone?.contains(query, ignoreCase = true) == true
            }.map { it.toDomain() }
        }
    }
    
    override suspend fun updateCustomerPurchases(customerId: String, totalPurchases: Double, lastPurchaseDate: kotlinx.datetime.LocalDateTime?) {
        val lastPurchaseTimestamp = lastPurchaseDate?.let { 
            kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        }
        customerDao.updateCustomerPurchases(customerId, totalPurchases, lastPurchaseTimestamp)
    }
    
    override suspend fun getCustomerByPhone(phone: String): Customer? {
        // Normalizar teléfono removiendo espacios y caracteres especiales
        val normalizedPhone = phone.replace(Regex("[^0-9+]"), "").trim()
        if (normalizedPhone.isBlank()) return null
        
        // Buscar en todos los clientes ya que el teléfono puede tener diferentes formatos
        val allCustomers = customerDao.getAllCustomers().first().map { it.toDomain() }
        return allCustomers.firstOrNull { customer ->
            customer.phone?.replace(Regex("[^0-9+]"), "")?.trim() == normalizedPhone
        }
    }
    
    override suspend fun getCustomerByEmail(email: String): Customer? {
        // Normalizar email a minúsculas
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank()) return null
        
        // SQLite no es case-sensitive por defecto, pero normalizamos para consistencia
        return customerDao.getCustomerByEmail(normalizedEmail)?.toDomain()
    }
    
    override suspend fun getTotalCustomerCount(): Int {
        return customerDao.getTotalCustomerCount()
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
