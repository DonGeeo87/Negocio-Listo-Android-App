package com.negociolisto.app.data.repository

import com.negociolisto.app.data.local.dao.*
import com.negociolisto.app.data.local.entity.toDomain
import com.negociolisto.app.data.remote.firebase.FirebaseBackupRepository
import com.negociolisto.app.domain.repository.BackupRepository
import com.negociolisto.app.ui.settings.BackupInfo
import com.negociolisto.app.ui.settings.FirebaseBackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🗃️ IMPLEMENTACIÓN DEL REPOSITORIO DE BACKUP
 * 
 * Implementación concreta que maneja backup y restauración de datos
 * desde Firebase usando los DAOs locales y FirebaseBackupRepository.
 */
@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val customerDao: CustomerDao,
    private val expenseDao: ExpenseDao,
    private val stockMovementDao: StockMovementDao,
    private val invoiceDao: InvoiceDao,
    private val customCategoryDao: CustomCategoryDao,
    private val firebaseBackupRepository: FirebaseBackupRepository
) : BackupRepository {

    override suspend fun getLastBackupInfo(): BackupInfo = withContext(Dispatchers.IO) {
        // TODO: Implementar obtención real desde Firebase
        // Por ahora, simular información del backup
        BackupInfo(
            isActive = true,
            isAutomatic = true,
            lastBackupDate = System.currentTimeMillis() - (2 * 60 * 60 * 1000), // Hace 2 horas
            totalItems = 0
        )
    }

    override suspend fun fetchBackupData(): FirebaseBackupData = withContext(Dispatchers.IO) {
        // Leer desde datos locales (ya sincronizados con Firebase después del backup)
        // Esto permite verificar que hay datos disponibles sin necesidad de userId
        val products = productDao.getAllProducts().first().map { it.toDomain() }
        val sales = saleDao.getAllSales().first().map { it.toDomain() }
        val customers = customerDao.getAllCustomers().first().map { it.toDomain() }
        val expenses = expenseDao.getAllExpenses().first().map { it.toDomain() }
        
        FirebaseBackupData(
            products = products,
            sales = sales,
            customers = customers,
            expenses = expenses,
            timestamp = System.currentTimeMillis()
        )
    }

    override suspend fun performBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // TODO: Implementar backup real a Firebase
            // Por ahora, simular éxito
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        // Limpiar todos los datos locales usando queries directas
        // TODO: Implementar limpieza real de datos cuando se implemente Firebase
        // Por ahora, solo simular la limpieza
    }

    override suspend fun insertRestoredData(data: FirebaseBackupData) = withContext(Dispatchers.IO) {
        // TODO: Implementar inserción real de datos restaurados
        // Por ahora, simular inserción exitosa
    }

    override suspend fun checkFirebaseConnection(): Boolean = withContext(Dispatchers.IO) {
        // TODO: Implementar verificación real de conexión con Firebase
        // Por ahora, simular conexión exitosa
        true
    }
}
