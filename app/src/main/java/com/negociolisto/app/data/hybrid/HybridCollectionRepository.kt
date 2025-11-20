package com.negociolisto.app.data.hybrid

import com.negociolisto.app.data.remote.firebase.FirebaseCollectionRepository
import com.negociolisto.app.data.repository.CollectionRepositoryImpl
import com.negociolisto.app.domain.model.Collection
import com.negociolisto.app.domain.model.CollectionWebTemplate
import com.negociolisto.app.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HybridCollectionRepository @Inject constructor(
    private val localRepository: CollectionRepositoryImpl,
    private val cloudRepository: FirebaseCollectionRepository
) : CollectionRepository {

    override fun getCollections(): Flow<List<Collection>> {
        // Retornar directamente el Flow reactivo de Room para que se actualice automáticamente
        // cuando se eliminen o modifiquen colecciones
        return localRepository.getCollections()
            .onStart {
                // Sincronización inicial: solo si no hay datos locales, restaurar desde la nube
                // Esto se ejecuta antes de la primera emisión del Flow
                val localCollections = localRepository.getCollections().first()
                if (localCollections.isEmpty()) {
                    try {
                        val cloud = cloudRepository.getCollections().first()
                        if (cloud.isNotEmpty()) {
                            // Restaurar desde la nube si no hay datos locales
                            for (cloudCol in cloud) {
                                localRepository.addCollection(cloudCol)
                            }
                        }
                    } catch (_: Exception) {
                        // Ignorar errores de sincronización inicial
                    }
                }
            }
    }

    override fun searchCollections(query: String): Flow<List<Collection>> {
        // Retornar directamente el Flow reactivo de Room para búsquedas
        return localRepository.searchCollections(query)
    }

    override suspend fun getById(id: String): Collection? {
        // Priorizar local, mantener items
        return localRepository.getById(id)
    }

    override suspend fun addCollection(collection: Collection) {
        localRepository.addCollection(collection)
        try { cloudRepository.addCollection(collection) } catch (_: Exception) { }
    }

    override suspend fun updateCollection(collection: Collection) {
        localRepository.updateCollection(collection)
        try { cloudRepository.updateCollection(collection) } catch (_: Exception) { }
    }

    override suspend fun deleteCollection(id: String) {
        // Eliminar primero de la nube para evitar que se restaure
        try { cloudRepository.deleteCollection(id) } catch (_: Exception) { }
        // Luego eliminar localmente - esto hará que el Flow emita automáticamente
        localRepository.deleteCollection(id)
        // El Flow de Room debería emitir automáticamente después de la eliminación
    }
    
    override suspend fun updateTemplateForCustomer(customerId: String, template: CollectionWebTemplate) {
        // Actualizar local y nube en paralelo
        localRepository.updateTemplateForCustomer(customerId, template)
        try { 
            cloudRepository.updateTemplateForCustomer(customerId, template) 
        } catch (e: Exception) {
            // Si falla la nube, continuar con local
        }
    }
    
    override suspend fun getTotalCollectionCount(): Int {
        // Usar el conteo local como fuente de verdad
        return localRepository.getTotalCollectionCount()
    }

    /**
     * 🔄 SINCRONIZAR CON LA NUBE
     */
    suspend fun syncWithCloud(): Boolean {
        return try {
            // Sincronizar datos locales con la nube
            val localCollections = localRepository.getCollections().first()
            
            for (collection in localCollections) {
                try {
                    cloudRepository.addCollection(collection)
                } catch (e: Exception) {
                    // Continuar con la siguiente colección si falla una
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 🔄 RESTAURAR DATOS DESDE LA NUBE
     */
    suspend fun restoreFromCloud(): Boolean {
        return try {
            val cloudCollections = cloudRepository.getCollections().first()
            for (collection in cloudCollections) {
                try {
                    localRepository.addCollection(collection)
                } catch (e: Exception) {
                    // Continuar con la siguiente colección si falla una
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 🗑️ LIMPIAR TODAS LAS COLECCIONES DE LA NUBE
     * Útil para resolver problemas de sincronización
     */
    suspend fun clearCloudCollections(): Boolean {
        return try {
            val cloudCollections = cloudRepository.getCollections().first()
            for (collection in cloudCollections) {
                try {
                    cloudRepository.deleteCollection(collection.id)
                } catch (e: Exception) {
                    // Continuar con la siguiente colección si falla una
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}


