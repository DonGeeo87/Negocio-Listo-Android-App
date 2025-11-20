package com.negociolisto.app.data.remote.googledrive

import android.content.Context
import android.net.Uri
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📸 SERVICIO DE RESPALDO DE IMÁGENES EN GOOGLE DRIVE - IMPLEMENTACIÓN REAL
 * 
 * Maneja la subida, eliminación y gestión real de imágenes de productos
 * en Google Drive usando Drive API v3.
 */
@Singleton
class ImageBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val googleDriveAuthService: GoogleDriveAuthService
) {
    
    companion object {
        private const val NEGOCIO_LISTO_FOLDER_NAME = "NegocioListo"
        private const val IMAGE_MIME_TYPE = "image/jpeg"
        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }
    
    /**
     * 📤 SUBIR IMAGEN DE PRODUCTO A GOOGLE DRIVE - IMPLEMENTACIÓN REAL
     * 
     * Sube una imagen de producto a Google Drive, creando la carpeta
     * "NegocioListo" si no existe y generando un enlace público.
     * 
     * @param imageUri URI de la imagen local
     * @param productId ID único del producto
     * @param productName Nombre del producto para el archivo
     * @return Result con la URL pública de la imagen o error
     */
    suspend fun uploadProductImage(
        imageUri: Uri, 
        productId: String, 
        productName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val driveService = googleDriveAuthService.getDriveService()
                ?: return@withContext Result.failure(Exception("Google Drive no conectado"))
            
            android.util.Log.d("ImageBackup", "🚀 Iniciando subida de imagen para producto: $productName")
            
            // 1. Crear o obtener carpeta NegocioListo
            val folderId = createOrGetNegocioListoFolder(driveService)
            android.util.Log.d("ImageBackup", "📁 Carpeta NegocioListo ID: $folderId")
            
            // 2. Subir imagen
            val fileId = uploadImageToDrive(driveService, imageUri, productId, productName, folderId)
            android.util.Log.d("ImageBackup", "📤 Imagen subida con ID: $fileId")
            
            // 3. Crear enlace público
            val publicUrl = createPublicLink(driveService, fileId)
            android.util.Log.d("ImageBackup", "✅ Enlace público creado: $publicUrl")
            
            Result.success(publicUrl)
        } catch (e: Exception) {
            android.util.Log.e("ImageBackup", "❌ Error al subir imagen: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 📁 CREAR O OBTENER CARPETA NEGOCIO LISTO - IMPLEMENTACIÓN REAL
     * 
     * Busca la carpeta "NegocioListo" en Google Drive del usuario,
     * o la crea si no existe usando Drive API.
     * 
     * @param driveService Servicio de Google Drive autenticado
     * @return ID de la carpeta NegocioListo
     */
    private suspend fun createOrGetNegocioListoFolder(driveService: Drive): String {
        // Buscar carpeta existente
        val query = "name='$NEGOCIO_LISTO_FOLDER_NAME' and mimeType='$FOLDER_MIME_TYPE' and trashed=false"
        val result = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()
        
        if (result.files.isNotEmpty()) {
            android.util.Log.d("ImageBackup", "📁 Carpeta existente encontrada: ${result.files[0].id}")
            return result.files[0].id
        }
        
        // Crear nueva carpeta
        val folderMetadata = File().apply {
            name = NEGOCIO_LISTO_FOLDER_NAME
            mimeType = FOLDER_MIME_TYPE
        }
        
        val folder = driveService.files().create(folderMetadata)
            .setFields("id")
            .execute()
            
        android.util.Log.d("ImageBackup", "📁 Nueva carpeta creada: ${folder.id}")
        return folder.id
    }
    
    /**
     * 📤 SUBIR IMAGEN A GOOGLE DRIVE - IMPLEMENTACIÓN REAL
     * 
     * Sube una imagen específica a la carpeta de NegocioListo en Google Drive
     * usando InputStreamContent para el contenido del archivo.
     * 
     * @param driveService Servicio de Google Drive autenticado
     * @param imageUri URI de la imagen local
     * @param productId ID del producto
     * @param productName Nombre del producto
     * @param folderId ID de la carpeta destino
     * @return ID del archivo subido
     */
    private suspend fun uploadImageToDrive(
        driveService: Drive,
        imageUri: Uri,
        productId: String,
        productName: String,
        folderId: String
    ): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw Exception("No se pudo abrir la imagen")
        
        val fileMetadata = File().apply {
            name = "${productName.replace(" ", "_")}_${productId}.jpg"
            parents = listOf(folderId)
        }
        
        val mediaContent = InputStreamContent(IMAGE_MIME_TYPE, inputStream)
        
        val file = driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, name, size")
            .execute()
        
        inputStream.close()
        
        android.util.Log.d("ImageBackup", "📤 Archivo creado: ${file.name} (${file.size} bytes)")
        return file.id
    }
    
    /**
     * 🔗 CREAR ENLACE PÚBLICO - IMPLEMENTACIÓN REAL
     * 
     * Crea un enlace público para el archivo subido, permitiendo
     * acceso a cualquiera que tenga el enlace usando Permissions API.
     * 
     * @param driveService Servicio de Google Drive autenticado
     * @param fileId ID del archivo
     * @return URL pública del archivo
     */
    private suspend fun createPublicLink(driveService: Drive, fileId: String): String {
        val permission = Permission().apply {
            type = "anyone"
            role = "reader"
        }
        
        driveService.permissions().create(fileId, permission).execute()
        
        android.util.Log.d("ImageBackup", "🔗 Permiso público creado para archivo: $fileId")
        
        // Retornar URL de visualización directa
        return "https://drive.google.com/uc?export=view&id=$fileId"
    }
    
    /**
     * 🗑️ ELIMINAR IMAGEN DE PRODUCTO - IMPLEMENTACIÓN REAL
     * 
     * Elimina una imagen de producto de Google Drive usando su URL.
     * 
     * @param imageUrl URL pública de la imagen en Google Drive
     * @return Result indicando éxito o fallo
     */
    suspend fun deleteProductImage(imageUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = googleDriveAuthService.getDriveService()
                ?: return@withContext Result.failure(Exception("Google Drive no conectado"))
            
            // Extraer fileId de la URL
            val fileId = extractFileIdFromUrl(imageUrl)
            if (fileId == null) {
                return@withContext Result.failure(Exception("URL inválida"))
            }
            
            android.util.Log.d("ImageBackup", "🗑️ Eliminando archivo: $fileId")
            driveService.files().delete(fileId).execute()
            android.util.Log.d("ImageBackup", "✅ Archivo eliminado exitosamente")
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ImageBackup", "❌ Error al eliminar imagen: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 🔍 EXTRAER FILE ID DE URL
     * 
     * Extrae el ID del archivo de una URL de Google Drive.
     * Soporta formatos: /file/d/{id}/ y ?id={id}
     * 
     * @param url URL de Google Drive
     * @return ID del archivo o null si la URL es inválida
     */
    private fun extractFileIdFromUrl(url: String): String? {
        // Intentar formato: /file/d/{id}/
        val regexFile = "/file/d/([a-zA-Z0-9-_]+)".toRegex()
        regexFile.find(url)?.groupValues?.get(1)?.let { return it }
        
        // Intentar formato: ?id={id}
        val regexId = "[?&]id=([a-zA-Z0-9-_]+)".toRegex()
        regexId.find(url)?.groupValues?.get(1)?.let { return it }
        
        // Intentar formato: /uc?export=view&id={id}
        val regexUc = "id=([a-zA-Z0-9-_]+)".toRegex()
        regexUc.find(url)?.groupValues?.get(1)?.let { return it }
        
        return null
    }
    
    /**
     * 📊 VERIFICAR ESPACIO DISPONIBLE - IMPLEMENTACIÓN REAL
     * 
     * Verifica el espacio disponible en Google Drive del usuario
     * usando About API.
     * 
     * @return Result con el espacio disponible en bytes, o error
     */
    suspend fun checkAvailableSpace(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val driveService = googleDriveAuthService.getDriveService()
                ?: return@withContext Result.failure(Exception("Google Drive no conectado"))
            
            val about = driveService.about().get()
                .setFields("storageQuota")
                .execute()
            
            val storageQuota = about.storageQuota
            val totalSpace = storageQuota.limit ?: 0L
            val usedSpace = storageQuota.usage ?: 0L
            val availableSpace = totalSpace - usedSpace
            
            android.util.Log.d("ImageBackup", "📊 Espacio disponible: ${availableSpace / (1024 * 1024)} MB")
            Result.success(availableSpace)
        } catch (e: Exception) {
            android.util.Log.e("ImageBackup", "❌ Error al verificar espacio: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 📋 LISTAR ARCHIVOS DE PRODUCTOS - IMPLEMENTACIÓN REAL
     * 
     * Lista todos los archivos de productos en la carpeta NegocioListo.
     * 
     * @return Result con lista de nombres de archivos o error
     */
    suspend fun listProductImages(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val driveService = googleDriveAuthService.getDriveService()
                ?: return@withContext Result.failure(Exception("Google Drive no conectado"))
            
            val folderId = createOrGetNegocioListoFolder(driveService)
            
            val query = "'$folderId' in parents and mimeType='$IMAGE_MIME_TYPE' and trashed=false"
            val result = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name, createdTime, size)")
                .execute()
            
            val fileNames = result.files.map { it.name }
            android.util.Log.d("ImageBackup", "📋 Archivos encontrados: ${fileNames.size}")
            
            Result.success(fileNames)
        } catch (e: Exception) {
            android.util.Log.e("ImageBackup", "❌ Error al listar archivos: ${e.message}", e)
            Result.failure(e)
        }
    }
}
