package com.negociolisto.app.data.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para gestionar backups automáticos usando WorkManager
 */
@Singleton
class AutoBackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val AUTO_BACKUP_WORK_NAME = "auto_backup_work"
        private const val DAILY_INTERVAL_HOURS = 24L
        private const val WEEKLY_INTERVAL_HOURS = 24L * 7L
        private const val MONTHLY_INTERVAL_HOURS = 24L * 30L
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Programa un backup automático según la frecuencia especificada
     * @param frequency Frecuencia del backup: "daily", "weekly", "monthly"
     */
    fun scheduleAutoBackup(frequency: String) {
        println("📅 AutoBackupManager: Programando backup automático con frecuencia: $frequency")
        
        val intervalHours = when (frequency) {
            "daily" -> DAILY_INTERVAL_HOURS
            "weekly" -> WEEKLY_INTERVAL_HOURS
            "monthly" -> MONTHLY_INTERVAL_HOURS
            else -> WEEKLY_INTERVAL_HOURS // Default
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Solo con conexión a internet
            .setRequiresBatteryNotLow(true) // Solo si la batería no está baja
            .build()
        
        val autoBackupWork = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag("auto_backup")
            .build()
        
        // Cancelar trabajo existente y programar nuevo
        workManager.enqueueUniquePeriodicWork(
            AUTO_BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            autoBackupWork
        )
        
        println("✅ AutoBackupManager: Backup automático programado cada $intervalHours horas")
    }
    
    /**
     * Cancela el backup automático
     */
    fun cancelAutoBackup() {
        println("🚫 AutoBackupManager: Cancelando backup automático")
        workManager.cancelUniqueWork(AUTO_BACKUP_WORK_NAME)
    }
    
    /**
     * Verifica si hay un backup automático programado
     */
    fun isAutoBackupScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(AUTO_BACKUP_WORK_NAME)
        return try {
            val workInfo = workInfos.get()
            workInfo.isNotEmpty() && workInfo.any { !it.state.isFinished }
        } catch (e: Exception) {
            println("❌ AutoBackupManager: Error verificando estado del backup: ${e.message}")
            false
        }
    }
}
