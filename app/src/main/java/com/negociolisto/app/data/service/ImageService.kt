package com.negociolisto.app.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🖼️ SERVICIO DE MANEJO DE IMÁGENES
 * 
 * Maneja la subida, procesamiento y almacenamiento de imágenes.
 * Por ahora es una implementación local, pero se puede extender
 * para subir a Firebase Storage.
 */
@Singleton
class ImageService @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) {
    
    /**
     * 📁 GUARDAR IMAGEN LOCALMENTE
     * 
     * Copia una imagen desde un URI a un archivo local.
     * Útil para procesar imágenes antes de subirlas a la nube.
     */
    suspend fun saveImageLocally(
        context: Context,
        imageUri: Uri,
        fileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val imageFile = File(context.cacheDir, fileName)
            
            // Copiar el archivo desde el URI
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(imageFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            Result.success(imageFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * ☁️ SUBIR IMAGEN A FIREBASE STORAGE
     * 
     * Sube una imagen comprimida a Firebase Storage con verificación de integridad
     */
    suspend fun uploadImageToCloud(
        imageFile: File,
        path: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileUri = Uri.fromFile(imageFile)
            val storageRef = firebaseStorage.reference.child(path)
            val uploadTask = storageRef.putFile(fileUri).await()
            val url = storageRef.downloadUrl.await().toString()
            // Limpiar archivo temporal
            imageFile.delete()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📤 SUBIR IMAGEN CON PROGRESO
     * 
     * Sube una imagen a Firebase Storage con callback de progreso
     */
    suspend fun uploadWithProgress(
        file: File,
        path: String,
        onProgress: (Int) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileUri = Uri.fromFile(file)
            val storageRef = firebaseStorage.reference.child(path)
            
            val uploadTask = storageRef.putFile(fileUri)
            
            // Escuchar progreso
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }
            
            val result = uploadTask.await()
            val url = storageRef.downloadUrl.await().toString()
            
            // Limpiar archivo temporal
            file.delete()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 🗑️ ELIMINAR IMAGEN LOCAL
     */
    suspend fun deleteLocalImage(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📏 COMPRIMIR IMAGEN
     * 
     * Comprime una imagen redimensionándola y reduciendo la calidad para optimizar el almacenamiento
     */
    suspend fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 600,
        quality: Int = 85
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Leer la imagen original
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return@withContext Result.failure(Exception("No se pudo abrir la imagen"))
            }
            
            // Decodificar la imagen con opciones para obtener solo las dimensiones
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            // Calcular el factor de escala
            val scaleFactor = calculateInSampleSize(options, maxWidth, maxHeight)
            
            // Decodificar la imagen con el factor de escala
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scaleFactor
                inJustDecodeBounds = false
            }
            
            val newInputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(newInputStream, null, decodeOptions)
            newInputStream?.close()
            
            if (bitmap == null) {
                return@withContext Result.failure(Exception("No se pudo decodificar la imagen"))
            }
            
            // Redimensionar si es necesario
            val resizedBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                resizeBitmap(bitmap, maxWidth, maxHeight)
            } else {
                bitmap
            }
            
            // Guardar la imagen comprimida
            val fileName = "compressed_${System.currentTimeMillis()}.jpg"
            val compressedFile = File(context.cacheDir, fileName)
            
            val outputStream = FileOutputStream(compressedFile)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // Liberar memoria
            bitmap.recycle()
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
            
            Result.success(compressedFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calcular el factor de escala para redimensionar la imagen
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Redimensionar un bitmap manteniendo la proporción
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = minOf(scaleWidth, scaleHeight)
        
        val matrix = Matrix().apply {
            postScale(scale, scale)
        }
        
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }
    
    /**
     * 📱 GENERAR NOMBRE DE ARCHIVO ÚNICO
     */
    fun generateUniqueFileName(prefix: String, extension: String = "jpg"): String {
        return "${prefix}_${System.currentTimeMillis()}.$extension"
    }
    
    /**
     * 🔄 COPIAR IMAGEN LOCAL A CACHE
     * 
     * Copia una imagen desde un URI a la carpeta de caché con un nombre específico
     */
    suspend fun copyImageToCache(
        context: Context,
        imageUri: Uri,
        fileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val imageFile = File(context.cacheDir, fileName)
            
            // Asegurar que el directorio padre existe
            imageFile.parentFile?.mkdirs()
            
            // Copiar el archivo desde el URI
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(imageFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            println("📸 DEBUG: Imagen copiada a caché: ${imageFile.absolutePath}")
            Result.success(imageFile)
        } catch (e: Exception) {
            println("❌ DEBUG: Error copiando imagen a caché: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * 📂 OBTENER ARCHIVO DE IMAGEN DESDE CACHE
     * 
     * Obtiene un archivo de imagen desde la carpeta de caché
     */
    fun getImageFromCache(context: Context, fileName: String): File? {
        val imageFile = File(context.cacheDir, fileName)
        return if (imageFile.exists()) imageFile else null
    }
    
    /**
     * 🛍️ SUBIR IMAGEN DE PRODUCTO CON COMPRESIÓN
     * 
     * Comprime y sube imagen de producto con parámetros optimizados
     */
    suspend fun uploadProductImage(
        context: Context,
        imageUri: Uri,
        productId: String,
        onProgress: (Int) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Comprimir imagen para producto (800x600, 85% calidad)
            val compressed = compressImage(
                context = context,
                imageUri = imageUri,
                maxWidth = 800,
                maxHeight = 600,
                quality = 85
            )
            
            if (compressed.isFailure) {
                return@withContext Result.failure(compressed.exceptionOrNull() ?: Exception("Error comprimiendo imagen"))
            }
            
            val compressedFile = compressed.getOrThrow()
            
            // 2. Subir a Storage con progreso
            // Guardamos la imagen también bajo la carpeta pública de productos para la mini‑web
            val path = "products/${productId}/${System.currentTimeMillis()}.jpg"
            val uploadResult = uploadWithProgress(compressedFile, path, onProgress)
            
            // 3. Limpiar archivo temporal
            deleteLocalImage(compressedFile)
            
            uploadResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📄 SUBIR IMAGEN DE FACTURA CON COMPRESIÓN
     * 
     * Comprime y sube imagen de factura con parámetros optimizados para documentos
     */
    suspend fun uploadInvoiceImage(
        context: Context,
        imageUri: Uri,
        invoiceId: String,
        onProgress: (Int) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Comprimir imagen para factura (1024x768, 90% calidad)
            val compressed = compressImage(
                context = context,
                imageUri = imageUri,
                maxWidth = 1024,
                maxHeight = 768,
                quality = 90
            )
            
            if (compressed.isFailure) {
                return@withContext Result.failure(compressed.exceptionOrNull() ?: Exception("Error comprimiendo imagen"))
            }
            
            val compressedFile = compressed.getOrThrow()
            
            // 2. Subir a Storage con progreso
            val path = "invoices/${invoiceId}_${System.currentTimeMillis()}.jpg"
            val uploadResult = uploadWithProgress(compressedFile, path, onProgress)
            
            // 3. Limpiar archivo temporal
            deleteLocalImage(compressedFile)
            
            uploadResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 👤 SUBIR AVATAR DE USUARIO CON COMPRESIÓN
     * 
     * Comprime y sube avatar de usuario con parámetros optimizados
     */
    suspend fun uploadUserAvatar(
        context: Context,
        imageUri: Uri,
        userId: String,
        onProgress: (Int) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Comprimir imagen para avatar (300x300, 80% calidad)
            val compressed = compressImage(
                context = context,
                imageUri = imageUri,
                maxWidth = 300,
                maxHeight = 300,
                quality = 80
            )
            
            if (compressed.isFailure) {
                return@withContext Result.failure(compressed.exceptionOrNull() ?: Exception("Error comprimiendo imagen"))
            }
            
            val compressedFile = compressed.getOrThrow()
            
            // 2. Subir a Storage con progreso
            val path = "users/${userId}/avatar_${System.currentTimeMillis()}.jpg"
            val uploadResult = uploadWithProgress(compressedFile, path, onProgress)
            
            // 3. Limpiar archivo temporal
            deleteLocalImage(compressedFile)
            
            uploadResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 🖼️ GENERAR THUMBNAIL PARA LISTAS
     * 
     * Genera una miniatura optimizada para mostrar en listas
     */
    suspend fun generateThumbnail(
        context: Context,
        imageUri: Uri,
        size: Int = 200,
        quality: Int = 70
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Comprimir imagen para thumbnail (200x200, 70% calidad)
            compressImage(
                context = context,
                imageUri = imageUri,
                maxWidth = size,
                maxHeight = size,
                quality = quality
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 🔍 VERIFICAR SI IMAGEN EXISTE EN STORAGE
     * 
     * Verifica si una imagen existe en Firebase Storage
     */
    suspend fun imageExistsInStorage(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val storageRef = firebaseStorage.reference.child(path)
            storageRef.metadata.await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 📏 OBTENER TAMAÑO DE IMAGEN EN STORAGE
     * 
     * Obtiene el tamaño en bytes de una imagen en Firebase Storage
     */
    suspend fun getImageSize(path: String): Long = withContext(Dispatchers.IO) {
        try {
            val storageRef = firebaseStorage.reference.child(path)
            val metadata = storageRef.metadata.await()
            metadata.sizeBytes
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 🗑️ ELIMINAR IMAGEN DE STORAGE
     * 
     * Elimina una imagen de Firebase Storage
     */
    suspend fun deleteImageFromStorage(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val storageRef = firebaseStorage.reference.child(path)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🔍 Verifica existencia de imagen por URL de descarga de Firebase Storage
     */
    /**
     * 🔍 VERIFICAR SI UNA URL ES LOCAL O REMOTA
     */
    fun isLocalUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.startsWith("/data/") || 
               url.startsWith("file://") || 
               url.startsWith("content://") ||
               !url.startsWith("http://") && !url.startsWith("https://")
    }
    
    /**
     * ✅ VERIFICAR SI UNA IMAGEN EXISTE EN FIREBASE STORAGE
     * 
     * Retorna false si la URL es local (no se puede verificar remotamente)
     */
    suspend fun imageExistsAtUrl(downloadUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Si es una URL local, no se puede verificar remotamente
            if (isLocalUrl(downloadUrl)) {
                return@withContext false
            }
            
            // Verificar que sea una URL de Firebase Storage
            if (!downloadUrl.contains("firebasestorage.googleapis.com")) {
                return@withContext false
            }
            
            val ref = firebaseStorage.getReferenceFromUrl(downloadUrl)
            ref.metadata.await()
            true
        } catch (e: Exception) {
            false
        }
    }
}