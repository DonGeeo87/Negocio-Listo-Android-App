package com.negociolisto.app.data.service

import com.negociolisto.app.data.remote.firebase.FirebaseBackupRepository
import com.negociolisto.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔄 ADMINISTRADOR DE SINCRONIZACIÓN
 *
 * Centraliza la restauración de datos desde Firebase para que la UI
 * y otros servicios puedan reutilizar el mismo flujo.
 */
@Singleton
class DataSyncManager @Inject constructor(
    private val firebaseBackupRepository: FirebaseBackupRepository,
    private val authRepository: AuthRepository
) {

    /**
     * Sincroniza todos los datos del usuario autenticado desde Firebase.
     */
    suspend fun syncUserData(
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUser.first()?.id
            ?: return@withContext Result.failure(IllegalStateException("No hay usuario autenticado"))

        firebaseBackupRepository.restoreFromBackup(userId, onProgress)
    }
}

