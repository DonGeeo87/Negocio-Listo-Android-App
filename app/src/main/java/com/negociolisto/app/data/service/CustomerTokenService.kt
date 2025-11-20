package com.negociolisto.app.data.service

import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.domain.repository.CustomerRepository
import com.negociolisto.app.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔑 SERVICIO DE TOKENS DE ACCESO PARA CLIENTES
 * 
 * Genera y gestiona tokens únicos que permiten a los clientes acceder
 * a su portal personalizado sin necesidad de autenticación compleja.
 * 
 * Los tokens son únicos por cliente (no por colección) y permiten:
 * - Ver todas las colecciones asignadas al cliente
 * - Ver el historial completo de pedidos del cliente
 * - Acceder al chat centralizado del cliente
 * - Hacer nuevos pedidos desde cualquier colección del cliente
 * 
 * El token es persistente y no cambia aunque se modifiquen las colecciones.
 */
@Singleton
class CustomerTokenService @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val collectionRepository: CollectionRepository,
    private val firestore: FirebaseFirestore
) {
    
    /**
     * 🔑 GENERAR TOKEN ÚNICO POR CLIENTE
     * 
     * Genera un token único para un cliente. El token no incluye collectionId
     * ya que es válido para todas las colecciones del cliente.
     * 
     * Formato: {customerId_short}_{uuid}
     * 
     * @param customerId ID del cliente
     * @return Token único para el cliente
     */
    fun generateToken(customerId: String): String {
        // Generar UUID único
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        
        // Crear token con formato: customerId_uuid
        // Usamos solo los primeros 8 caracteres del customerId para mantenerlo corto
        val customerShort = customerId.take(8)
        
        return "${customerShort}_$uuid"
    }
    
    /**
     * 🔑 OBTENER O GENERAR TOKEN PARA CLIENTE
     * 
     * Obtiene el token existente del cliente o genera uno nuevo si no existe.
     * Si el cliente tiene un token, lo retorna. Si no, genera uno nuevo,
     * lo guarda en el cliente y lo retorna.
     * 
     * @param customer Cliente para el cual obtener/generar token
     * @return Token del cliente (existente o nuevo)
     */
    suspend fun getOrGenerateCustomerToken(customer: Customer): String {
        // Si el cliente ya tiene un token, retornarlo
        if (!customer.accessToken.isNullOrBlank()) {
            return customer.accessToken
        }
        
        // Generar nuevo token
        val newToken = generateToken(customer.id)
        
        // Actualizar cliente con el nuevo token
        val updatedCustomer = customer.copy(accessToken = newToken)
        customerRepository.updateCustomer(updatedCustomer)
        
        return newToken
    }
    
    /**
     * 🔄 MIGRAR TOKEN DESDE COLECCIONES
     * 
     * Busca tokens existentes del cliente en sus colecciones asociadas
     * y migra el primer token encontrado al cliente. Si no encuentra tokens,
     * genera uno nuevo.
     * 
     * Esta función es útil para migrar tokens existentes al nuevo sistema
     * centralizado por cliente.
     * 
     * @param customerId ID del cliente
     * @return Token migrado o generado
     */
    suspend fun migrateTokenFromCollections(customerId: String): String? {
        // Buscar todas las colecciones asociadas al cliente
        val collections = collectionRepository.getCollections()
            .first()
            .filter { it.associatedCustomerIds.contains(customerId) }
        
        // Buscar el primer token existente en las colecciones
        for (collection in collections) {
            val token = collection.customerAccessTokens[customerId]
            if (!token.isNullOrBlank()) {
                // Token encontrado, migrarlo al cliente
                val customer = customerRepository.getCustomerById(customerId)
                if (customer != null) {
                    val updatedCustomer = customer.copy(accessToken = token)
                    customerRepository.updateCustomer(updatedCustomer)
                    return token
                }
            }
        }
        
        // No se encontró token, retornar null para que se genere uno nuevo
        return null
    }
    
    /**
     * 🔍 VALIDAR TOKEN
     * 
     * Valida que un token tenga el formato correcto.
     * Nuevo formato: {customerId_short}_{uuid} (2 partes)
     * Formato antiguo: {collectionId_short}_{customerId_short}_{uuid} (3 partes)
     * 
     * @param token Token a validar
     * @return true si el token tiene formato válido
     */
    fun isValidTokenFormat(token: String): Boolean {
        val parts = token.split("_")
        // Aceptar tanto el formato nuevo (2 partes) como el antiguo (3+ partes)
        return parts.size >= 2
    }
    
    /**
     * 🔗 GENERAR URL DEL PORTAL
     * 
     * Genera la URL completa del portal del cliente con el token.
     * 
     * @param token Token de acceso
     * @return URL completa del portal
     */
    fun generatePortalUrl(token: String): String {
        return "https://app-negocio-listo.web.app/customer-portal.html?token=$token"
    }
    
    /**
     * 🔗 GENERAR URL DE COLECCIÓN CON TOKEN
     * 
     * Genera la URL de una colección específica con el token del cliente.
     * 
     * @param collectionId ID de la colección
     * @param token Token de acceso del cliente
     * @return URL completa de la colección con token
     */
    fun generateCollectionUrl(collectionId: String, token: String, template: String = "MODERN"): String {
        return "https://app-negocio-listo.web.app/collection.html?id=$collectionId&token=$token&template=$template"
    }
    
    /**
     * 🔄 MIGRAR TODOS LOS TOKENS EXISTENTES
     * 
     * Migra todos los tokens existentes desde las colecciones a los clientes.
     * Esta función debe ejecutarse una vez para migrar el sistema antiguo al nuevo.
     * 
     * @return Número de clientes migrados
     */
    suspend fun migrateAllExistingTokens(): Int {
        var migratedCount = 0
        
        // Obtener todos los clientes
        val customers = customerRepository.getAllCustomers()
            .first()
        
        for (customer in customers) {
            // Si el cliente ya tiene token, saltarlo
            if (!customer.accessToken.isNullOrBlank()) {
                continue
            }
            
            // Intentar migrar token desde colecciones
            val migratedToken = migrateTokenFromCollections(customer.id)
            
            if (migratedToken != null) {
                migratedCount++
            } else {
                // No se encontró token, generar uno nuevo
                getOrGenerateCustomerToken(customer)
                migratedCount++
            }
        }
        
        return migratedCount
    }
}

