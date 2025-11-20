package com.negociolisto.app.domain.repository

import com.negociolisto.app.ui.settings.BackupInfo
import com.negociolisto.app.ui.settings.FirebaseBackupData

/**
 * 🗃️ REPOSITORIO DE BACKUP
 * 
 * Interface que define las operaciones para backup y restauración
 * de datos desde Firebase.
 */
interface BackupRepository {

    /**
     * 📊 OBTENER INFORMACIÓN DEL BACKUP
     * 
     * Obtiene el estado actual del backup y la última fecha de respaldo.
     */
    suspend fun getLastBackupInfo(): BackupInfo

    /**
     * 📥 OBTENER DATOS DE BACKUP DESDE FIREBASE
     * 
     * Descarga todos los datos respaldados desde Firebase.
     */
    suspend fun fetchBackupData(): FirebaseBackupData

    /**
     * 📤 REALIZAR BACKUP MANUAL
     * 
     * Sube los datos locales actuales a Firebase.
     */
    suspend fun performBackup(): Result<Unit>

    /**
     * 🗑️ LIMPIAR DATOS LOCALES
     * 
     * Elimina todos los datos locales antes de restaurar.
     */
    suspend fun clearLocalData()

    /**
     * 💾 INSERTAR DATOS RESTAURADOS
     * 
     * Inserta los datos restaurados desde Firebase en la base de datos local.
     */
    suspend fun insertRestoredData(data: FirebaseBackupData)

    /**
     * ✅ VERIFICAR CONEXIÓN CON FIREBASE
     * 
     * Verifica si hay conexión activa con Firebase.
     */
    suspend fun checkFirebaseConnection(): Boolean
}











