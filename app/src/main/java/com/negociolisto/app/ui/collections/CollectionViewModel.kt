package com.negociolisto.app.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.service.CustomerTokenService
import com.negociolisto.app.data.service.UsageLimitsService
import com.negociolisto.app.domain.model.Collection
import com.negociolisto.app.domain.repository.CollectionRepository
import com.negociolisto.app.domain.repository.CollectionResponseRepository
import com.negociolisto.app.domain.repository.CustomerRepository
import com.negociolisto.app.data.analytics.AnalyticsHelper
import com.negociolisto.app.data.analytics.CrashlyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 📚 VIEWMODEL DE COLECCIONES CON HILT
 * 
 * Maneja la lógica de negocio para colecciones.
 */
@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val responseRepository: CollectionResponseRepository,
    private val tokenService: CustomerTokenService,
    private val customerRepository: CustomerRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val crashlyticsHelper: CrashlyticsHelper,
    private val usageLimitsService: UsageLimitsService
) : ViewModel() {
    
    val collections: StateFlow<List<Collection>> = collectionRepository.getCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())
    
    // Cache de StateFlows de conteo de respuestas por collectionId para evitar loops infinitos
    private val responseCountCache = mutableMapOf<String, StateFlow<Int>>()
    
    fun addCollection(collection: Collection) {
        viewModelScope.launch {
            try {
                // Verificar límite antes de agregar
                val limitCheck = usageLimitsService.checkCollectionLimit()
                if (!limitCheck.canAdd) {
                    throw Exception(limitCheck.message ?: "Has alcanzado el límite de colecciones permitidas.")
                }
                
                // Si la colección tiene clientes asociados, aplicar el template globalmente PRIMERO
                // Esto asegura que todas las colecciones del cliente usen el mismo template
                if (collection.associatedCustomerIds.isNotEmpty()) {
                    collection.associatedCustomerIds.forEach { customerId ->
                        // Usar await para asegurar que la actualización global se complete
                        collectionRepository.updateTemplateForCustomer(customerId, collection.webTemplate)
                    }
                }
                
                // Luego agregar la colección (esto sincronizará con la actualización global)
                collectionRepository.addCollection(collection)
            } catch (e: Exception) {
                crashlyticsHelper.recordException(e)
                crashlyticsHelper.log("Error agregando colección: ${collection.name}")
                throw e // Re-lanzar para que la UI pueda mostrar el error
            }
        }
    }
    
    fun updateCollection(collection: Collection) {
        viewModelScope.launch {
            // Ya no necesitamos generar tokens por colección
            // Los tokens ahora son por cliente y se generan automáticamente
            // cuando se obtiene el token del cliente
            
            // Asegurar que los clientes asociados tengan tokens
            collection.associatedCustomerIds.forEach { customerId ->
                launch {
                    val customer = customerRepository.getCustomerById(customerId)
                    if (customer != null) {
                        // Esto generará el token si no existe
                        tokenService.getOrGenerateCustomerToken(customer)
                    }
                }
            }
            
            // Obtener la colección existente para comparar el template
            val existingCollection = collectionRepository.getById(collection.id)
            
            // Si el template cambió y hay un cliente asociado, actualizar globalmente PRIMERO
            if (existingCollection != null && 
                existingCollection.webTemplate != collection.webTemplate &&
                collection.associatedCustomerIds.isNotEmpty()) {
                // Actualizar el template de todas las colecciones del cliente
                // Ejecutar en secuencia para asegurar que se complete antes de continuar
                collection.associatedCustomerIds.forEach { customerId ->
                    // Usar await para asegurar que la actualización global se complete
                    collectionRepository.updateTemplateForCustomer(customerId, collection.webTemplate)
                }
            }
            
            // Actualizar colección individual (esto sincronizará con la actualización global)
            // La colección ya debería tener el template correcto después de la actualización global
            collectionRepository.updateCollection(collection)
        }
    }
    
    fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(collectionId)
            // Limpiar el cache del conteo de respuestas cuando se elimina la colección
            responseCountCache.remove(collectionId)
            // Sin refresco manual: Room/Flow emite y actualiza 'collections'
        }
    }
    
    // refreshCollections eliminado: nos apoyamos en emisiones reactivas del repositorio
    
    /**
     * 🔗 GENERAR LINK PÚBLICO
     * 
     * Genera la URL pública de la colección para compartir con clientes.
     * Incluye el template como parámetro para que la mini-web use el estilo correcto.
     * 
     * @param collectionId ID de la colección
     * @return URL completa para compartir
     */
    fun generatePublicLink(collectionId: String): String {
        val collection = collections.value.firstOrNull { it.id == collectionId }
        val template = collection?.webTemplate?.name ?: "MODERN"
        
        // Analytics - Track cuando se comparte una colección
        if (collection != null) {
            analyticsHelper.logCollectionShared(collectionId, template)
        }
        
        return "https://app-negocio-listo.web.app/collection.html?id=$collectionId&template=$template"
    }
    
    /**
     * 🔑 GENERAR LINK DEL PORTAL DEL CLIENTE
     * 
     * Genera la URL del portal del cliente usando su token de acceso.
     * El token ahora se obtiene del cliente, no de la colección.
     * 
     * @param collectionId ID de la colección (ya no se usa para el token, solo para referencia)
     * @param customerId ID del cliente
     * @return URL completa del portal del cliente, o null si no se puede obtener token
     */
    suspend fun generateCustomerPortalLink(collectionId: String, customerId: String): String? {
        val customer = customerRepository.getCustomerById(customerId)
        if (customer != null) {
            val token = tokenService.getOrGenerateCustomerToken(customer)
            return tokenService.generatePortalUrl(token)
        }
        return null
    }
    
    /**
     * 🔑 GENERAR LINK DEL PORTAL DEL CLIENTE CON TOKEN
     * 
     * Genera la URL del portal del cliente usando un token específico.
     * 
     * @param token Token de acceso del cliente
     * @return URL completa del portal del cliente
     */
    fun generateCustomerPortalLinkWithToken(token: String): String {
        return tokenService.generatePortalUrl(token)
    }
    
    /**
     * 🔑 OBTENER TOKEN DEL CLIENTE
     * 
     * Obtiene el token de acceso de un cliente. El token es único por cliente
     * y válido para todas sus colecciones. Si no existe, lo genera automáticamente.
     * 
     * @param customerId ID del cliente
     * @return Token de acceso del cliente
     */
    suspend fun getOrGenerateCustomerToken(customerId: String): String {
        val customer = customerRepository.getCustomerById(customerId)
        if (customer != null) {
            return tokenService.getOrGenerateCustomerToken(customer)
        }
        
        // Si no existe el cliente, generar un token temporal (no debería pasar)
        return tokenService.generateToken(customerId)
    }
    
    /**
     * 📊 OBTENER CONTEO DE RESPUESTAS
     * 
     * Obtiene el número de pedidos/respuestas para una colección.
     * Los StateFlows se cachean por collectionId para evitar crear nuevos Flows
     * en cada recomposición, lo que causaba loops infinitos.
     * 
     * @param collectionId ID de la colección
     * @return StateFlow con el número de respuestas (cacheado por collectionId)
     */
    fun getResponseCount(collectionId: String): StateFlow<Int> {
        return responseCountCache.getOrPut(collectionId) {
            responseRepository.getResponses(collectionId)
                .map { it.size }
                .distinctUntilChanged() // Evitar emisiones cuando el tamaño no cambia realmente
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), 0)
        }
    }
}
