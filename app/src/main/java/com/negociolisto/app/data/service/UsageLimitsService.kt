package com.negociolisto.app.data.service

import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.CustomerRepository
import com.negociolisto.app.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔒 SERVICIO DE LÍMITES DE USO
 * 
 * Gestiona los límites de uso basados en las capacidades del plan Spark (gratuito) de Firebase.
 * 
 * Límites de Firebase Spark:
 * - Firestore: 20,000 escrituras/día, 50,000 lecturas/día, 1 GB almacenamiento
 * - Storage: 5 GB, 20,000 cargas/mes, 1 GB descargas/día
 * - Auth: 50,000 usuarios activos mensuales (MAU)
 * 
 * Cálculo de límites por usuario (escenario medio optimizado):
 * - Productos: 100 por usuario (considerando actualizaciones de stock)
 * - Clientes: 50 por usuario
 * - Colecciones: 50 por usuario (menos frecuentes)
 * - Imágenes: ~30 MB por usuario (optimizado para permitir más usuarios)
 * 
 * Estos límites son conservadores para evitar exceder las cuotas de Firebase.
 * Máximo teórico: ~153 usuarios activos (90% de 5 GB Storage).
 */
@Singleton
class UsageLimitsService @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val collectionRepository: CollectionRepository
) {
    
    /**
     * 📊 LÍMITES CONFIGURADOS
     * 
     * Estos límites pueden ajustarse según el crecimiento de usuarios.
     */
    companion object {
        // Límites por usuario
        const val MAX_PRODUCTS_PER_USER = 100
        const val MAX_CUSTOMERS_PER_USER = 50
        const val MAX_COLLECTIONS_PER_USER = 50
        
        // Límites de almacenamiento (en MB)
        const val MAX_STORAGE_PER_USER_MB = 30  // Optimizado para permitir más usuarios
        const val MAX_STORAGE_TOTAL_MB = 5120   // 5 GB en MB
        const val STORAGE_LIMIT_PERCENT = 90     // 90% de capacidad máxima
        
        // Porcentajes de advertencia (mostrar advertencia cuando se alcanza este %)
        const val WARNING_THRESHOLD_PERCENT = 80
        const val CRITICAL_THRESHOLD_PERCENT = 95
    }
    
    /**
     * 📦 VERIFICAR LÍMITE DE PRODUCTOS
     * 
     * @return Resultado de la verificación con mensaje si se excede el límite
     */
    suspend fun checkProductLimit(): LimitCheckResult {
        val currentCount = inventoryRepository.getTotalProductCount()
        return checkLimit(
            currentCount = currentCount,
            maxLimit = MAX_PRODUCTS_PER_USER,
            itemType = "productos"
        )
    }
    
    /**
     * 👥 VERIFICAR LÍMITE DE CLIENTES
     * 
     * @return Resultado de la verificación con mensaje si se excede el límite
     */
    suspend fun checkCustomerLimit(): LimitCheckResult {
        val currentCount = customerRepository.getTotalCustomerCount()
        return checkLimit(
            currentCount = currentCount,
            maxLimit = MAX_CUSTOMERS_PER_USER,
            itemType = "clientes"
        )
    }
    
    /**
     * 📚 VERIFICAR LÍMITE DE COLECCIONES
     * 
     * @return Resultado de la verificación con mensaje si se excede el límite
     */
    suspend fun checkCollectionLimit(): LimitCheckResult {
        val currentCount = collectionRepository.getTotalCollectionCount()
        return checkLimit(
            currentCount = currentCount,
            maxLimit = MAX_COLLECTIONS_PER_USER,
            itemType = "colecciones"
        )
    }
    
    /**
     * ✅ VERIFICAR SI SE PUEDE AGREGAR UN PRODUCTO
     * 
     * @return true si se puede agregar, false si se excedió el límite
     */
    suspend fun canAddProduct(): Boolean {
        return checkProductLimit().canAdd
    }
    
    /**
     * ✅ VERIFICAR SI SE PUEDE AGREGAR UN CLIENTE
     * 
     * @return true si se puede agregar, false si se excedió el límite
     */
    suspend fun canAddCustomer(): Boolean {
        return checkCustomerLimit().canAdd
    }
    
    /**
     * ✅ VERIFICAR SI SE PUEDE AGREGAR UNA COLECCIÓN
     * 
     * @return true si se puede agregar, false si se excedió el límite
     */
    suspend fun canAddCollection(): Boolean {
        return checkCollectionLimit().canAdd
    }
    
    /**
     * 💾 VERIFICAR CAPACIDAD DE STORAGE PARA NUEVO USUARIO
     * 
     * Verifica si hay suficiente espacio en Storage para aceptar un nuevo usuario.
     * Calcula el uso estimado basándose en el número de usuarios existentes.
     * 
     * @param currentUserCount Número de usuarios existentes en el sistema
     * @return Resultado de la verificación
     */
    suspend fun checkStorageCapacityForNewUser(currentUserCount: Int): LimitCheckResult {
        // Convertir a Long primero para evitar overflow
        val maxStorageBytes = (MAX_STORAGE_TOTAL_MB.toLong() * 1024L * 1024L)
        val limitBytes = (maxStorageBytes * STORAGE_LIMIT_PERCENT / 100L) // 90% de 5 GB
        
        // Espacio estimado por usuario (30 MB con límites optimizados)
        val estimatedSpacePerUser = (MAX_STORAGE_PER_USER_MB.toLong() * 1024L * 1024L)
        
        // Calcular uso después de agregar nuevo usuario
        val newUsageBytes = (currentUserCount + 1).toLong() * estimatedSpacePerUser
        
        val percentage = if (limitBytes > 0) {
            ((newUsageBytes.toFloat() / limitBytes.toFloat()) * 100).toInt()
        } else {
            0
        }
        
        val canAdd = newUsageBytes <= limitBytes
        
        val status = when {
            percentage >= CRITICAL_THRESHOLD_PERCENT -> LimitStatus.CRITICAL
            percentage >= WARNING_THRESHOLD_PERCENT -> LimitStatus.WARNING
            else -> LimitStatus.OK
        }
        
        val message = if (!canAdd) {
            "No podemos aceptar nuevos usuarios en este momento. " +
            "El almacenamiento está al ${percentage}% de su capacidad máxima. " +
            "Por favor, intenta más tarde o contacta con soporte."
        } else if (status == LimitStatus.CRITICAL) {
            "El almacenamiento está cerca de su límite (${percentage}%). " +
            "Se aceptarán usuarios hasta alcanzar el 90% de capacidad."
        } else {
            null
        }
        
        return LimitCheckResult(
            canAdd = canAdd,
            currentCount = currentUserCount,
            maxLimit = (limitBytes / estimatedSpacePerUser).toInt(),
            percentage = percentage,
            status = status,
            message = message
        )
    }
    
    /**
     * 📊 OBTENER ESTADÍSTICAS DE USO
     * 
     * @return Estadísticas completas de uso de recursos
     */
    suspend fun getUsageStatistics(): UsageStatistics {
        val products = checkProductLimit()
        val customers = checkCustomerLimit()
        val collections = checkCollectionLimit()
        
        return UsageStatistics(
            products = ItemUsage(
                current = products.currentCount,
                limit = products.maxLimit,
                percentage = products.percentage,
                status = products.status
            ),
            customers = ItemUsage(
                current = customers.currentCount,
                limit = customers.maxLimit,
                percentage = customers.percentage,
                status = customers.status
            ),
            collections = ItemUsage(
                current = collections.currentCount,
                limit = collections.maxLimit,
                percentage = collections.percentage,
                status = collections.status
            )
        )
    }
    
    /**
     * 🔍 VERIFICAR LÍMITE GENÉRICO
     * 
     * @param currentCount Cantidad actual de items
     * @param maxLimit Límite máximo permitido
     * @param itemType Tipo de item (para mensajes)
     * @return Resultado de la verificación
     */
    private fun checkLimit(
        currentCount: Int,
        maxLimit: Int,
        itemType: String
    ): LimitCheckResult {
        val percentage = if (maxLimit > 0) {
            (currentCount.toFloat() / maxLimit.toFloat() * 100).toInt()
        } else {
            0
        }
        
        val status = when {
            percentage >= CRITICAL_THRESHOLD_PERCENT -> LimitStatus.CRITICAL
            percentage >= WARNING_THRESHOLD_PERCENT -> LimitStatus.WARNING
            else -> LimitStatus.OK
        }
        
        val canAdd = currentCount < maxLimit
        val message = if (!canAdd) {
            "Has alcanzado el límite de $maxLimit $itemType. " +
            "Para agregar más, considera actualizar a un plan superior."
        } else if (status == LimitStatus.CRITICAL) {
            "Estás cerca del límite de $maxLimit $itemType ($currentCount/$maxLimit). " +
            "Considera eliminar items no utilizados."
        } else if (status == LimitStatus.WARNING) {
            "Has usado el ${percentage}% de tu límite de $itemType ($currentCount/$maxLimit)."
        } else {
            null
        }
        
        return LimitCheckResult(
            canAdd = canAdd,
            currentCount = currentCount,
            maxLimit = maxLimit,
            percentage = percentage,
            status = status,
            message = message
        )
    }
}

/**
 * 📊 RESULTADO DE VERIFICACIÓN DE LÍMITE
 */
data class LimitCheckResult(
    val canAdd: Boolean,
    val currentCount: Int,
    val maxLimit: Int,
    val percentage: Int,
    val status: LimitStatus,
    val message: String?
)

/**
 * 📈 ESTADO DEL LÍMITE
 */
enum class LimitStatus {
    OK,           // Por debajo del 80%
    WARNING,      // Entre 80% y 95%
    CRITICAL      // Por encima del 95%
}

/**
 * 📊 ESTADÍSTICAS DE USO
 */
data class UsageStatistics(
    val products: ItemUsage,
    val customers: ItemUsage,
    val collections: ItemUsage
)

/**
 * 📦 USO DE UN TIPO DE ITEM
 */
data class ItemUsage(
    val current: Int,
    val limit: Int,
    val percentage: Int,
    val status: LimitStatus
)

