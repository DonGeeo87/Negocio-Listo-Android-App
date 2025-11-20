package com.negociolisto.app.data.remote.firebase

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.data.local.database.NegocioListoDatabase
import com.negociolisto.app.data.local.dao.*
import com.negociolisto.app.data.local.entity.InvoiceEntity
import com.negociolisto.app.data.local.entity.toDomain
import com.negociolisto.app.data.local.entity.toEntity
import com.negociolisto.app.data.service.ImageService
import com.negociolisto.app.domain.model.*
import com.negociolisto.app.domain.model.InvoiceItem
import com.negociolisto.app.domain.model.InvoiceTemplateType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔥 REPOSITORIO DE BACKUP FIREBASE
 * 
 * Maneja la sincronización completa de datos con Firebase Firestore
 * como sistema de backup primario. Implementa estrategia offline-first
 * con sincronización automática.
 */
@Singleton
class FirebaseBackupRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val database: NegocioListoDatabase,
    private val imageService: ImageService,
    @ApplicationContext private val context: Context
) {
    
    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val saleDao = database.saleDao()
    private val expenseDao = database.expenseDao()
    private val collectionDao = database.collectionDao()
    private val invoiceDao = database.invoiceDao()
    private val customCategoryDao = database.customCategoryDao()
    private val stockMovementDao = database.stockMovementDao()
    
    /**
     * 📤 SUBIR IMAGEN LOCAL A FIREBASE STORAGE
     * 
     * Si la URL es local, la sube a Storage y retorna la nueva URL.
     * Si ya es remota, retorna la misma URL.
     * Lanza excepción si hay error para que el backup falle apropiadamente.
     */
    private suspend fun uploadLocalImageIfNeeded(
        imageUrl: String?,
        productId: String,
        imageType: String = "photo"
    ): String? = withContext(Dispatchers.IO) {
        if (imageUrl.isNullOrBlank()) {
            android.util.Log.d("FirebaseBackup", "⚠️ URL de imagen vacía para producto $productId ($imageType)")
            return@withContext null
        }
        
        // Si no es local, retornar la URL original
        if (!imageService.isLocalUrl(imageUrl)) {
            android.util.Log.d("FirebaseBackup", "✅ URL ya es remota para producto $productId ($imageType): $imageUrl")
            return@withContext imageUrl
        }
        
        android.util.Log.d("FirebaseBackup", "📤 Subiendo imagen local para producto $productId ($imageType): $imageUrl")
        
        // Convertir ruta local a File
        val imageFile = when {
            imageUrl.startsWith("/data/") -> File(imageUrl)
            imageUrl.startsWith("file://") -> {
                val uri = android.net.Uri.parse(imageUrl)
                val path = uri.path
                if (path != null) File(path) else {
                    val errorMsg = "No se pudo extraer path del URI: $imageUrl"
                    android.util.Log.e("FirebaseBackup", "❌ $errorMsg")
                    throw Exception(errorMsg)
                }
            }
            imageUrl.startsWith("content://") -> {
                // Convertir content:// URI a File usando ContentResolver
                try {
                    val contentUri = android.net.Uri.parse(imageUrl)
                    val inputStream = context.contentResolver.openInputStream(contentUri)
                    if (inputStream == null) {
                        throw Exception("No se pudo abrir el input stream para content:// URI: $imageUrl")
                    }
                    
                    // Crear archivo temporal
                    val tempFile = File(context.cacheDir, "backup_temp_${System.currentTimeMillis()}_${productId}_$imageType.jpg")
                    tempFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    inputStream.close()
                    
                    android.util.Log.d("FirebaseBackup", "   📄 Content URI copiado a archivo temporal: ${tempFile.absolutePath}")
                    tempFile
                } catch (e: Exception) {
                    val errorMsg = "Error convirtiendo content:// URI a archivo: $imageUrl - ${e.message}"
                    android.util.Log.e("FirebaseBackup", "❌ $errorMsg", e)
                    throw Exception(errorMsg, e)
                }
            }
            else -> File(imageUrl)
        }
        
        if (!imageFile.exists()) {
            val errorMsg = "Archivo de imagen no existe: ${imageFile.absolutePath} (URL original: $imageUrl)"
            android.util.Log.e("FirebaseBackup", "❌ $errorMsg")
            throw Exception(errorMsg)
        }
        
        if (!imageFile.canRead()) {
            val errorMsg = "No se puede leer archivo (permisos insuficientes): ${imageFile.absolutePath}"
            android.util.Log.e("FirebaseBackup", "❌ $errorMsg")
            throw Exception(errorMsg)
        }
        
        if (imageFile.length() == 0L) {
            val errorMsg = "Archivo de imagen está vacío: ${imageFile.absolutePath}"
            android.util.Log.e("FirebaseBackup", "❌ $errorMsg")
            throw Exception(errorMsg)
        }
        
        // Subir a Storage
        val path = "products/$productId/${imageType}_${System.currentTimeMillis()}.jpg"
        android.util.Log.d("FirebaseBackup", "☁️ Subiendo a Storage: $path (tamaño: ${imageFile.length()} bytes)")
        val uploadResult = imageService.uploadImageToCloud(imageFile, path)
        
        if (uploadResult.isSuccess) {
            val remoteUrl = uploadResult.getOrNull()
            android.util.Log.d("FirebaseBackup", "✅ Imagen subida exitosamente: $remoteUrl")
            remoteUrl
        } else {
            val error = uploadResult.exceptionOrNull()
            val errorMsg = "Error subiendo imagen a Firebase Storage: ${error?.message ?: "Error desconocido"}"
            android.util.Log.e("FirebaseBackup", "❌ $errorMsg")
            android.util.Log.e("FirebaseBackup", "   Archivo: ${imageFile.absolutePath}, Tamaño: ${imageFile.length()} bytes")
            android.util.Log.e("FirebaseBackup", "   Producto: $productId, Tipo: $imageType")
            if (error != null) {
                android.util.Log.e("FirebaseBackup", "   Excepción completa:", error)
            }
            throw Exception(errorMsg, error)
        }
    }
    
    /**
     * 💾 CREAR BACKUP COMPLETO A FIREBASE
     * 
     * Sincroniza todos los datos locales con Firebase Firestore
     * usando transacciones para garantizar consistencia.
     */
    suspend fun createFullBackup(
        userId: String,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Iniciando backup a Firebase...")
            
            // Obtener todos los datos locales
            val productEntities = productDao.getAllProducts().first()
            val products = productEntities.map { it.toDomain() }
            val customerEntities = customerDao.getAllCustomers().first()
            val customers = customerEntities.map { it.toDomain() }
            val sales = saleDao.getAllSales().first()
            val expenses = expenseDao.getAllExpenses().first()
            val collections = collectionDao.getCollections(userId).first()
            val invoices = invoiceDao.getAllInvoices().first()
            val customCategories = customCategoryDao.getAllCategoriesByUser(userId).first()
            val stockMovements = stockMovementDao.getAllMovements().first()
            
            onProgress(20, "Preparando datos para sincronización...")
            
            // Crear batch para transacción atómica
            val batch = firestore.batch()
            
            // Sincronizar productos (ruta privada del usuario) y espejo público para la mini‑web
            onProgress(30, "Sincronizando productos...")
            
            // Procesar productos: subir imágenes locales y actualizar URLs
            val processedProducts = products.mapIndexed { index, product ->
                onProgress(30 + (index * 5 / products.size), "Procesando producto ${index + 1}/${products.size}...")
                
                android.util.Log.d("FirebaseBackup", "📦 Procesando producto ${product.id} - ${product.name}")
                
                var updatedProduct = product
                var photoUrlUpdated = false
                var thumbnailUrlUpdated = false
                
                // Subir photoUrl si es local
                if (imageService.isLocalUrl(product.photoUrl)) {
                    android.util.Log.d("FirebaseBackup", "   📸 Foto es local, subiendo...")
                    val uploadedUrl = uploadLocalImageIfNeeded(product.photoUrl, product.id, "photo")
                    if (uploadedUrl != null) {
                        updatedProduct = updatedProduct.copy(photoUrl = uploadedUrl)
                        photoUrlUpdated = true
                        android.util.Log.d("FirebaseBackup", "   ✅ Foto subida exitosamente")
                    } else {
                        throw Exception("No se pudo subir la foto del producto ${product.id} (${product.name}). Ver logs para más detalles.")
                    }
                } else {
                    android.util.Log.d("FirebaseBackup", "   ✅ Foto ya es remota o no existe")
                }
                
                // Subir thumbnailUrl si es local
                if (imageService.isLocalUrl(product.thumbnailUrl)) {
                    android.util.Log.d("FirebaseBackup", "   🖼️ Thumbnail es local, subiendo...")
                    val uploadedUrl = uploadLocalImageIfNeeded(product.thumbnailUrl, product.id, "thumbnail")
                    if (uploadedUrl != null) {
                        updatedProduct = updatedProduct.copy(thumbnailUrl = uploadedUrl)
                        thumbnailUrlUpdated = true
                        android.util.Log.d("FirebaseBackup", "   ✅ Thumbnail subido exitosamente")
                    } else {
                        throw Exception("No se pudo subir el thumbnail del producto ${product.id} (${product.name}). Ver logs para más detalles.")
                    }
                } else {
                    android.util.Log.d("FirebaseBackup", "   ✅ Thumbnail ya es remoto o no existe")
                }
                
                // Si se actualizaron URLs, guardar en la base de datos local
                if (photoUrlUpdated || thumbnailUrlUpdated) {
                    try {
                        productDao.updateProduct(updatedProduct.toEntity())
                        android.util.Log.d("FirebaseBackup", "   💾 Producto actualizado localmente")
                    } catch (e: Exception) {
                        android.util.Log.w("FirebaseBackup", "   ⚠️ No se pudo actualizar producto localmente (continuando): ${e.message}")
                        // No lanzamos excepción aquí porque las imágenes ya están en Firebase
                    }
                }
                
                updatedProduct
            }
            
            // Sincronizar productos procesados con Firestore
            android.util.Log.d("FirebaseBackup", "📦 Sincronizando ${processedProducts.size} productos...")
            processedProducts.forEach { product ->
                val userScopedRef = firestore.collection("users/$userId/products").document(product.id)
                batch.set(userScopedRef, product.toFirestoreMap())

                // Espejo público: top-level /products/{productId}
                val publicRef = firestore.collection("products").document(product.id)
                val publicPayload = hashMapOf(
                    "id" to product.id,
                    "name" to product.name,
                    "sku" to product.sku,
                    "description" to (product.description ?: ""),
                    "salePrice" to product.salePrice,
                    "purchasePrice" to product.purchasePrice,
                    "stockQuantity" to product.stockQuantity,
                    "currentStock" to product.stockQuantity, // Compatibilidad adicional
                    "minimumStock" to product.minimumStock,
                    "photoUrl" to (product.photoUrl ?: ""),
                    "imageUrl" to (product.photoUrl ?: ""), // Compatibilidad: la mini-web busca imageUrl
                    "thumbnailUrl" to (product.thumbnailUrl ?: ""),
                    "customCategoryId" to (product.customCategoryId),
                    "supplier" to (product.supplier ?: ""),
                    "createdAt" to product.createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                    "updatedAt" to product.updatedAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                    "isActive" to true
                )
                batch.set(publicRef, publicPayload)
                android.util.Log.d("FirebaseBackup", "✅ Producto agregado al batch: ${product.id} - ${product.name}")
            }
            android.util.Log.d("FirebaseBackup", "📦 Total productos en batch: ${processedProducts.size}")
            
            // Sincronizar clientes
            onProgress(40, "Sincronizando clientes...")
            customerEntities.forEach { customerEntity ->
                val docRef = firestore.collection("users/$userId/customers").document(customerEntity.id)
                batch.set(docRef, customerEntity)
            }
            
            // Sincronizar ventas
            onProgress(50, "Sincronizando ventas...")
            android.util.Log.d("FirebaseBackup", "📦 Sincronizando ${sales.size} ventas...")
            sales.forEach { saleEntity ->
                val saleMap = mutableMapOf<String, Any>(
                    "id" to saleEntity.id,
                    "items" to saleEntity.items, // String serializado
                    "total" to saleEntity.total,
                    "date" to saleEntity.date, // Long timestamp
                    "paymentMethod" to saleEntity.paymentMethod, // String
                    "status" to saleEntity.status // String
                )
                // Campos opcionales
                if (saleEntity.customerId != null) {
                    saleMap["customerId"] = saleEntity.customerId
                }
                if (saleEntity.note != null) {
                    saleMap["note"] = saleEntity.note
                }
                if (saleEntity.canceledAt != null) {
                    saleMap["canceledAt"] = saleEntity.canceledAt
                }
                if (saleEntity.canceledReason != null) {
                    saleMap["canceledReason"] = saleEntity.canceledReason
                }
                val docRef = firestore.collection("users/$userId/sales").document(saleEntity.id)
                batch.set(docRef, saleMap)
                android.util.Log.d("FirebaseBackup", "✅ Venta ${saleEntity.id} preparada: items=${saleEntity.items.take(50)}...")
            }
            android.util.Log.d("FirebaseBackup", "✅ ${sales.size} ventas preparadas para sincronización")
            
            // Sincronizar gastos
            onProgress(60, "Sincronizando gastos...")
            expenses.forEach { expense ->
                val docRef = firestore.collection("users/$userId/expenses").document(expense.id)
                batch.set(docRef, expense)
            }
            
            // Sincronizar colecciones
            onProgress(70, "Sincronizando colecciones...")
            collections.forEach { collection ->
                val docRef = firestore.collection("users/$userId/collections").document(collection.collection.id)
                batch.set(docRef, collection.collection)
            }
            
            // Sincronizar facturas
            onProgress(80, "Sincronizando facturas...")
            invoices.forEach { invoice ->
                val docRef = firestore.collection("users/$userId/invoices").document(invoice.id)
                batch.set(docRef, invoice.toFirestoreMap())
            }
            
            // Sincronizar categorías personalizadas
            onProgress(85, "Sincronizando categorías...")
            customCategories.forEach { category ->
                val docRef = firestore.collection("users/$userId/customCategories").document(category.id)
                // Convertir CustomCategoryEntity a Map para Firestore
                batch.set(docRef, mapOf(
                    "id" to category.id,
                    "userId" to category.userId,
                    "name" to category.name,
                    "icon" to category.icon,
                    "color" to category.color,
                    "description" to (category.description ?: ""),
                    "createdAt" to LocalDateTime.parse(category.createdAt).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                    "updatedAt" to LocalDateTime.parse(category.updatedAt).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                    "sortOrder" to category.sortOrder,
                    "isActive" to category.isActive
                ))
            }
            
            // Sincronizar movimientos de stock
            onProgress(90, "Sincronizando movimientos de stock...")
            stockMovements.forEach { movement ->
                val docRef = firestore.collection("users/$userId/stockMovements").document(movement.id)
                batch.set(docRef, movement)
            }
            
            // ✅ NUEVO: Sincronizar colecciones completas con sus items, chat y responses
            onProgress(91, "Sincronizando colecciones extendidas...")
            collections.forEach { collectionWithItems ->
                val collectionEntity = collectionWithItems.collection
                val items = collectionWithItems.items
                
                // Convertir CollectionEntity a Collection (dominio) para obtener associatedCustomerIds como List
                val collection = collectionEntity.toDomain(items)
                
                // Guardar colección principal (ruta privada del usuario)
                val collectionRef = firestore.collection("users/$userId/collections").document(collection.id)
                val statusName = collection.status.toString()
                val templateName = collection.webTemplate.toString()
                
                val privateItems = items.map { item ->
                    val product = processedProducts.find { it.id == item.productId }
                    val itemData = hashMapOf<String, Any?>(
                        "productId" to item.productId,
                        "notes" to (item.notes ?: ""),
                        "displayOrder" to item.displayOrder,
                        "isFeatured" to item.isFeatured,
                        "specialPrice" to (item.specialPrice ?: 0.0)
                    )

                    if (product != null) {
                        itemData["name"] = product.name
                        itemData["sku"] = product.sku
                        itemData["description"] = (product.description ?: "")
                        itemData["salePrice"] = product.salePrice
                        itemData["purchasePrice"] = product.purchasePrice
                        itemData["stockQuantity"] = product.stockQuantity
                        itemData["currentStock"] = product.stockQuantity
                        itemData["minimumStock"] = product.minimumStock
                        itemData["photoUrl"] = (product.photoUrl ?: "")
                        itemData["imageUrl"] = (product.photoUrl ?: "")
                        itemData["thumbnailUrl"] = (product.thumbnailUrl ?: "")
                        itemData["customCategoryId"] = product.customCategoryId
                        itemData["supplier"] = (product.supplier ?: "")
                        itemData["isActive"] = true
                    }

                    itemData
                }

                val privateCollectionPayload = hashMapOf(
                    "id" to collection.id,
                    "name" to collection.name,
                    "description" to (collection.description ?: ""),
                    "status" to statusName,
                    "webTemplate" to templateName,
                    "enableChat" to collection.enableChat,
                    "color" to (collection.color ?: ""),
                    "associatedCustomerIds" to collection.associatedCustomerIds,
                    "createdAt" to collection.createdAt.toString(),
                    "updatedAt" to collection.updatedAt.toString(),
                    "items" to privateItems
                )
                batch.set(collectionRef, privateCollectionPayload)
                
                // Espejo público: top-level /collections/{collectionId} para la mini-web
                val publicCollectionRef = firestore.collection("collections").document(collection.id)
                // Determinar si es pública basándose en el status
                val isPublic = statusName == "SHARED" || statusName == "ACTIVE"
                
                // Crear mapa de items con datos completos del producto (para que la mini-web no necesite consultar /products)
                val itemsMap = items.associate { item ->
                    // Buscar el producto completo en la lista de productos procesados
                    val product = processedProducts.find { it.id == item.productId }
                    
                    // Construir el item con todos los datos del producto incluidos
                    val itemData = hashMapOf<String, Any?>(
                        "productId" to item.productId,
                        "id" to item.productId,
                        "displayOrder" to item.displayOrder,
                        "isFeatured" to item.isFeatured,
                        "notes" to item.notes,
                        "specialPrice" to item.specialPrice
                    )
                    
                    // Si encontramos el producto, incluir todos sus datos
                    if (product != null) {
                        itemData["name"] = product.name
                        itemData["sku"] = product.sku
                        itemData["description"] = (product.description ?: "")
                        itemData["salePrice"] = product.salePrice
                        itemData["purchasePrice"] = product.purchasePrice
                        itemData["stockQuantity"] = product.stockQuantity
                        itemData["currentStock"] = product.stockQuantity
                        itemData["minimumStock"] = product.minimumStock
                        itemData["photoUrl"] = (product.photoUrl ?: "")
                        itemData["imageUrl"] = (product.photoUrl ?: "") // Compatibilidad: la mini-web busca imageUrl
                        itemData["thumbnailUrl"] = (product.thumbnailUrl ?: "")
                        itemData["customCategoryId"] = product.customCategoryId
                        itemData["supplier"] = (product.supplier ?: "")
                        itemData["isActive"] = true
                        android.util.Log.d("FirebaseBackup", "✅ Item con datos completos: ${item.productId} - ${product.name}")
                    } else {
                        android.util.Log.w("FirebaseBackup", "⚠️ Producto no encontrado para item: ${item.productId}")
                    }
                    
                    item.productId to itemData
                }
                
                // Obtener datos del cliente asociado si existe
                val associatedCustomerData = hashMapOf<String, Any>()
                if (collection.associatedCustomerIds.isNotEmpty()) {
                    val customerId: String = collection.associatedCustomerIds.first()
                    val customer = customers.find { it.id.equals(customerId) }
                    if (customer != null) {
                        associatedCustomerData["associatedCustomerId"] = customer.id
                        associatedCustomerData["associatedCustomerName"] = customer.name
                        associatedCustomerData["associatedCustomerEmail"] = (customer.email ?: "")
                        associatedCustomerData["associatedCustomerPhone"] = (customer.phone ?: "")
                        android.util.Log.d("FirebaseBackup", "✅ Cliente asociado incluido en colección pública: ${customer.name}")
                    }
                }
                
                val publicCollectionPayload = hashMapOf<String, Any>(
                    "id" to collection.id,
                    "name" to collection.name,
                    "description" to (collection.description ?: ""),
                    "status" to statusName,
                    "isPublic" to isPublic,
                    "public" to isPublic, // Compatibilidad con diferentes nombres
                    "template" to templateName,
                    "webTemplate" to templateName, // Compatibilidad adicional
                    "enableChat" to collection.enableChat,
                    "color" to (collection.color ?: ""),
                    "items" to itemsMap, // Campo items como mapa (para compatibilidad)
                    "associatedCustomerIds" to collection.associatedCustomerIds, // Mantener IDs para compatibilidad
                    "createdAt" to collection.createdAt.toString(),
                    "updatedAt" to collection.updatedAt.toString(),
                    "userId" to userId
                )
                // Agregar datos del cliente asociado si existen
                publicCollectionPayload.putAll(associatedCustomerData)
                batch.set(publicCollectionRef, publicCollectionPayload)
                
                // Guardar items en la ruta pública también (para la mini-web)
                // La mini-web lee desde /collections/{collectionId}/items
                // Incluir datos completos del producto en cada item
                items.forEach { item ->
                    val publicItemRef = publicCollectionRef.collection("items").document(item.productId)
                    val product = processedProducts.find { it.id == item.productId }
                    
                    val itemData = hashMapOf<String, Any?>(
                        "productId" to item.productId,
                        "id" to item.productId,
                        "notes" to (item.notes ?: ""),
                        "displayOrder" to item.displayOrder,
                        "isFeatured" to item.isFeatured,
                        "specialPrice" to (item.specialPrice ?: 0.0)
                    )
                    
                    // Incluir datos completos del producto si está disponible
                    if (product != null) {
                        itemData["name"] = product.name
                        itemData["sku"] = product.sku
                        itemData["description"] = (product.description ?: "")
                        itemData["salePrice"] = product.salePrice
                        itemData["purchasePrice"] = product.purchasePrice
                        itemData["stockQuantity"] = product.stockQuantity
                        itemData["currentStock"] = product.stockQuantity
                        itemData["minimumStock"] = product.minimumStock
                        itemData["photoUrl"] = (product.photoUrl ?: "")
                        itemData["imageUrl"] = (product.photoUrl ?: "")
                        itemData["thumbnailUrl"] = (product.thumbnailUrl ?: "")
                        itemData["customCategoryId"] = product.customCategoryId
                        itemData["supplier"] = (product.supplier ?: "")
                        itemData["isActive"] = true
                    }
                    
                    batch.set(publicItemRef, itemData)
                }
            }

            // Actualizar metadatos de backup
            val backupMetadata = mapOf(
                "lastBackup" to System.currentTimeMillis(),
                "version" to "2.0", // ✅ Actualizado a v2.0 para incluir nuevas funcionalidades
                "totalProducts" to products.size,
                "totalCustomers" to customers.size,
                "totalSales" to sales.size,
                "totalExpenses" to expenses.size,
                "totalCollections" to collections.size,
                "totalInvoices" to invoices.size,
                "totalCustomCategories" to customCategories.size,
                "totalStockMovements" to stockMovements.size
            )
            
            val metadataRef = firestore.collection("users")
                .document(userId)
                .collection("metadata")
                .document("backup")
            batch.set(metadataRef, backupMetadata)
            
            onProgress(94, "Finalizando sincronización principal...")

            // Ejecutar transacción
            android.util.Log.d("FirebaseBackup", "💾 Ejecutando batch commit...")
            batch.commit().await()
            android.util.Log.d("FirebaseBackup", "✅ Batch commit completado exitosamente")

            // ✅ NUEVO: Sincronizar chats y responses en commits separados para evitar errores de permisos ocultos
            onProgress(96, "Sincronizando chats y respuestas...")
            syncChatsAndResponses(userId, onProgress)
            
            onProgress(100, "Backup completado exitosamente")
            
            Result.success("Backup completado: ${products.size} productos, ${customers.size} clientes, ${sales.size} ventas")
            
        } catch (e: Exception) {
            android.util.Log.e("FirebaseBackup", "❌ Error general en backup", e)
            Result.failure(Exception("Error en backup de Firebase: ${e.message}"))
        }
    }
    
    /**
     * 📊 OBTENER ESTADO DE BACKUP
     * 
     * Obtiene información sobre el último backup realizado.
     * Verifica tanto el metadata como la existencia real de datos en las colecciones.
     */
    suspend fun getBackupStatus(userId: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            // Primero verificar si hay datos directamente en las colecciones
            // Esto es más confiable que solo buscar el metadata
            val hasData = try {
                // Verificar si hay productos, clientes, ventas, etc. en Firebase
                val productsSnapshot = firestore.collection("users/$userId/products")
                    .limit(1)
                    .get()
                    .await()
                
                val customersSnapshot = firestore.collection("users/$userId/customers")
                    .limit(1)
                    .get()
                    .await()
                
                val hasProducts = productsSnapshot.documents.isNotEmpty()
                val hasCustomers = customersSnapshot.documents.isNotEmpty()
                
                // Si hay datos en las colecciones, hay backup
                hasProducts || hasCustomers
            } catch (e: Exception) {
                println("⚠️ Error verificando datos en colecciones: ${e.message}")
                false
            }
            
            // También verificar el metadata si existe
            val metadataDoc = firestore.collection("users")
                .document(userId)
                .collection("metadata")
                .document("backup")
                .get()
                .await()
            
            if (metadataDoc.exists()) {
                val metadata = metadataDoc.data ?: emptyMap()
                // Si hay datos en colecciones pero no hay metadata, crear uno básico
                if (hasData && metadata.isEmpty()) {
                    Result.success(mapOf(
                        "status" to "has_data",
                        "hasData" to true,
                        "lastBackup" to System.currentTimeMillis()
                    ))
                } else {
                    Result.success(metadata + mapOf("hasData" to hasData))
                }
            } else if (hasData) {
                // Hay datos pero no metadata - significa que se sincronizaron directamente
                Result.success(mapOf(
                    "status" to "has_data",
                    "hasData" to true,
                    "lastBackup" to System.currentTimeMillis()
                ))
            } else {
                Result.success(mapOf("status" to "no_backup", "hasData" to false))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error obteniendo estado de backup: ${e.message}"))
        }
    }
    
    /**
     * 🔄 ACTUALIZAR METADATA DE BACKUP
     * 
     * Actualiza el metadata de backup cuando se sincronizan datos individuales.
     * Esto asegura que el sistema detecte que hay datos disponibles para restaurar.
     */
    suspend fun updateBackupMetadata(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar si hay datos en las colecciones
            val productsCount = try {
                firestore.collection("users/$userId/products")
                    .limit(1)
                    .get()
                    .await()
                    .documents.size
            } catch (e: Exception) {
                0
            }
            
            val customersCount = try {
                firestore.collection("users/$userId/customers")
                    .limit(1)
                    .get()
                    .await()
                    .documents.size
            } catch (e: Exception) {
                0
            }
            
            // Si hay datos, actualizar metadata
            if (productsCount > 0 || customersCount > 0) {
                val metadataRef = firestore.collection("users")
                    .document(userId)
                    .collection("metadata")
                    .document("backup")
                
                // Obtener metadata existente o crear uno nuevo
                val existingDoc = metadataRef.get().await()
                val existingData = existingDoc.data ?: emptyMap<String, Any>()
                
                val updatedMetadata = existingData.toMutableMap().apply {
                    put("lastBackup", System.currentTimeMillis())
                    put("hasData", true)
                    put("status", "has_data")
                    // Mantener conteos si existen, o establecer valores básicos
                    if (!containsKey("totalProducts")) {
                        put("totalProducts", productsCount)
                    }
                    if (!containsKey("totalCustomers")) {
                        put("totalCustomers", customersCount)
                    }
                }
                
                metadataRef.set(updatedMetadata).await()
                println("✅ Metadata de backup actualizado para usuario $userId")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("⚠️ Error actualizando metadata de backup: ${e.message}")
            Result.failure(Exception("Error actualizando metadata: ${e.message}"))
        }
    }
    
    /**
     * 📥 RESTAURAR DATOS DESDE FIREBASE
     * 
     * Restaura todos los datos del usuario desde Firebase Firestore
     */
    suspend fun restoreFromBackup(
        userId: String,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Iniciando restauración desde Firebase...")
            
            // Limpiar datos locales antes de insertar los nuevos
            onProgress(5, "Limpiando datos locales...")
            try {
                productDao.clearAllProducts()
                customerDao.clearAllCustomers()
                saleDao.clearAllSales()
                expenseDao.clearAllExpenses()
                collectionDao.clearAllCollectionItems()
                collectionDao.clearAllCollections(userId)
                invoiceDao.clearAllInvoices()
                stockMovementDao.clearAllMovements()
                customCategoryDao.deleteAllCategoriesByUser(userId)
            } catch (cleanupError: Exception) {
                println("⚠️ Error limpiando datos locales: ${cleanupError.message}")
            }
            
            // 1. Restaurar productos
            onProgress(10, "Restaurando productos...")
            try {
                val productsSnapshot = firestore.collection("users/$userId/products").get().await()
                val products = productsSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data: Map<String, Any?> = doc.data ?: return@mapNotNull null
                        com.negociolisto.app.data.local.entity.ProductEntity(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String,
                            sku = data["sku"] as? String ?: "",
                            purchasePrice = (data["purchasePrice"] as? Number)?.toDouble() ?: 0.0,
                            salePrice = (data["salePrice"] as? Number)?.toDouble() ?: 0.0,
                            stockQuantity = (data["stockQuantity"] as? Number)?.toInt() ?: 0,
                            minimumStock = (data["minimumStock"] as? Number)?.toInt() ?: 5,
                            customCategoryId = data["customCategoryId"] as? String ?: "",
                            supplier = data["supplier"] as? String,
                            photoUrl = data["photoUrl"] as? String,
                            thumbnailUrl = data["thumbnailUrl"] as? String,
                            imageBackupUrl = data["imageBackupUrl"] as? String,
                            isActive = data["isActive"] as? Boolean ?: true,
                            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        println("⚠️ Error mapeando producto ${doc.id}: ${e.message}")
                        null
                    }
                }
                if (products.isNotEmpty()) {
                    productDao.insertProducts(products)
                    println("✅ Productos restaurados: ${products.size}")
                }
            } catch (e: Exception) {
                println("⚠️ Error restaurando productos: ${e.message}")
            }
            
            // 2. Restaurar clientes
            onProgress(30, "Restaurando clientes...")
            try {
                val customersSnapshot = firestore.collection("users/$userId/customers").get().await()
                val customersFromFirebase = customersSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data: Map<String, Any?> = doc.data ?: return@mapNotNull null
                        com.negociolisto.app.data.local.entity.CustomerEntity(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            companyName = data["companyName"] as? String,
                            email = data["email"] as? String,
                            phone = data["phone"] as? String,
                            address = data["address"] as? String,
                            totalPurchases = (data["totalPurchases"] as? Number)?.toDouble() ?: 0.0,
                            lastPurchaseDate = (data["lastPurchaseDate"] as? Number)?.toLong(),
                            notes = data["notes"] as? String,
                            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        println("⚠️ Error mapeando cliente ${doc.id}: ${e.message}")
                        null
                    }
                }
                
                if (customersFromFirebase.isNotEmpty()) {
                    // Verificar duplicados antes de insertar
                    val existingCustomers = customerDao.getAllCustomers().first()
                    val existingIds = existingCustomers.map { it.id }.toSet()
                    val existingPhones = existingCustomers.mapNotNull { it.phone?.trim()?.lowercase() }.toSet()
                    val existingEmails = existingCustomers.mapNotNull { it.email?.trim()?.lowercase() }.toSet()
                    
                    val customersToInsert = customersFromFirebase.filter { customer ->
                        // Verificar si ya existe por ID (OnConflictStrategy.REPLACE lo manejará, pero logueamos)
                        if (existingIds.contains(customer.id)) {
                            println("🔄 Cliente con ID ${customer.id} ya existe, será reemplazado")
                            true // Permitir reemplazo
                        } else {
                            // Verificar duplicados por teléfono o email
                            val phoneMatch = customer.phone?.trim()?.lowercase()?.let { it in existingPhones } == true
                            val emailMatch = customer.email?.trim()?.lowercase()?.let { it in existingEmails } == true
                            
                            if (phoneMatch || emailMatch) {
                                println("⚠️ Cliente duplicado detectado: ${customer.name} (teléfono: ${customer.phone}, email: ${customer.email})")
                                // Si hay match por teléfono o email, aún así insertar (puede ser el mismo cliente con datos actualizados)
                                true
                            } else {
                                true // Nuevo cliente, insertar
                            }
                        }
                    }
                    
                    if (customersToInsert.isNotEmpty()) {
                        customerDao.insertCustomers(customersToInsert)
                        println("✅ Clientes restaurados: ${customersToInsert.size} de ${customersFromFirebase.size} totales")
                    } else {
                        println("⚠️ No se restauraron clientes (todos eran duplicados)")
                    }
                }
            } catch (e: Exception) {
                println("⚠️ Error restaurando clientes: ${e.message}")
                e.printStackTrace()
            }
            
            // 3. Restaurar colecciones con items
            onProgress(50, "Restaurando colecciones...")
            try {
                val collectionsSnapshot = firestore.collection("users/$userId/collections").get().await()
                collectionsSnapshot.documents.forEach { collectionDoc ->
                    try {
                        val data: Map<String, Any?> = collectionDoc.data ?: return@forEach
                        val collection = com.negociolisto.app.data.local.entity.CollectionEntity(
                            id = collectionDoc.id,
                            userId = userId, // ✅ Agregar userId del propietario
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String,
                            associatedCustomerIds = when (val ids = data["associatedCustomerIds"]) {
                                is List<*> -> ids.filterIsInstance<String>().joinToString(",")
                                else -> ""
                            },
                            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            status = data["status"] as? String ?: "DRAFT",
                            color = data["color"] as? String,
                            enableChat = data["enableChat"] as? Boolean ?: true,
                            webTemplate = data["webTemplate"] as? String ?: "MODERN"
                        )
                        
                        collectionDao.insertCollection(collection)
                        
                        // Restaurar items de la colección (priorizar items embebidos)
                        val embeddedItems = (data["items"] as? List<*>)
                            ?.mapNotNull { rawItem ->
                                try {
                                    @Suppress("UNCHECKED_CAST")
                                    val itemData = rawItem as? Map<String, Any?> ?: return@mapNotNull null
                                    com.negociolisto.app.data.local.entity.CollectionItemEntity(
                                        collectionId = collection.id,
                                        productId = itemData["productId"] as? String ?: return@mapNotNull null,
                                        notes = itemData["notes"] as? String,
                                        displayOrder = (itemData["displayOrder"] as? Number)?.toInt() ?: 0,
                                        isFeatured = itemData["isFeatured"] as? Boolean ?: false,
                                        specialPrice = (itemData["specialPrice"] as? Number)?.toDouble()
                                    )
                                } catch (e: Exception) {
                                    println("⚠️ Error mapeando item embebido de colección ${collection.id}: ${e.message}")
                                    null
                                }
                            }

                        val itemsToPersist = if (!embeddedItems.isNullOrEmpty()) {
                            embeddedItems
                        } else {
                            try {
                                val itemsSnapshot = collectionDoc.reference.collection("items").get().await()
                                itemsSnapshot.documents.mapNotNull { itemDoc ->
                                    try {
                                        val itemData: Map<String, Any?> = itemDoc.data ?: return@mapNotNull null
                                        com.negociolisto.app.data.local.entity.CollectionItemEntity(
                                            collectionId = collection.id,
                                            productId = itemDoc.id,
                                            notes = itemData["notes"] as? String,
                                            displayOrder = (itemData["displayOrder"] as? Number)?.toInt() ?: 0,
                                            isFeatured = itemData["isFeatured"] as? Boolean ?: false,
                                            specialPrice = (itemData["specialPrice"] as? Number)?.toDouble()
                                        )
                                    } catch (e: Exception) {
                                        println("⚠️ Error mapeando item ${itemDoc.id}: ${e.message}")
                                        null
                                    }
                                }
                            } catch (e: Exception) {
                                println("⚠️ Error restaurando items de colección ${collection.id}: ${e.message}")
                                emptyList()
                            }
                        }

                        if (!itemsToPersist.isNullOrEmpty()) {
                            collectionDao.insertItems(itemsToPersist)
                        }
                    } catch (e: Exception) {
                        println("⚠️ Error restaurando colección ${collectionDoc.id}: ${e.message}")
                    }
                }
                println("✅ Colecciones restauradas")
            } catch (e: Exception) {
                println("⚠️ Error restaurando colecciones: ${e.message}")
            }
            
            // 4. Restaurar categorías personalizadas
            onProgress(65, "Restaurando categorías personalizadas...")
            try {
                val categoriesSnapshot = firestore.collection("users/$userId/customCategories").get().await()
                val categories = categoriesSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data: Map<String, Any?> = doc.data ?: return@mapNotNull null
                        
                        // Manejar createdAt: puede venir como Long (timestamp) o String
                        val createdAtStr = when (val createdAtValue = data["createdAt"]) {
                            is Long -> {
                                // Es timestamp, convertir a LocalDateTime y luego a String
                                Instant.fromEpochMilliseconds(createdAtValue)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .toString()
                            }
                            is Number -> {
                                // Es Number (timestamp), convertir igual
                                Instant.fromEpochMilliseconds(createdAtValue.toLong())
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .toString()
                            }
                            is String -> createdAtValue // Ya es String
                            else -> {
                                // Valor por defecto
                                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                            }
                        }
                        
                        // Manejar updatedAt: puede venir como Long (timestamp) o String
                        val updatedAtStr = when (val updatedAtValue = data["updatedAt"]) {
                            is Long -> {
                                Instant.fromEpochMilliseconds(updatedAtValue)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .toString()
                            }
                            is Number -> {
                                Instant.fromEpochMilliseconds(updatedAtValue.toLong())
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .toString()
                            }
                            is String -> updatedAtValue // Ya es String
                            else -> createdAtStr // Usar createdAt si no hay updatedAt
                        }
                        
                        com.negociolisto.app.data.local.entity.CustomCategoryEntity(
                            id = doc.id,
                            userId = data["userId"] as? String ?: userId,
                            name = data["name"] as? String ?: "",
                            icon = data["icon"] as? String ?: "category",
                            color = data["color"] as? String ?: "#6366F1",
                            description = data["description"] as? String,
                            createdAt = createdAtStr,
                            updatedAt = updatedAtStr,
                            sortOrder = (data["sortOrder"] as? Number)?.toInt() ?: 0,
                            isActive = data["isActive"] as? Boolean ?: true
                        )
                    } catch (e: Exception) {
                        println("⚠️ Error mapeando categoría ${doc.id}: ${e.message}")
                        e.printStackTrace()
                        null
                    }
                }
                if (categories.isNotEmpty()) {
                    customCategoryDao.insertCategories(categories)
                    println("✅ Categorías personalizadas restauradas: ${categories.size}")
                }
            } catch (e: Exception) {
                println("⚠️ Error restaurando categorías: ${e.message}")
            }
            
            // 5. Restaurar ventas
            onProgress(75, "Restaurando ventas...")
            try {
                android.util.Log.d("FirebaseBackup", "🔍 Buscando ventas en Firebase para usuario: $userId")
                val salesSnapshot = firestore.collection("users/$userId/sales").get().await()
                android.util.Log.d("FirebaseBackup", "📊 Encontradas ${salesSnapshot.documents.size} ventas en Firebase")
                val sales = salesSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data: Map<String, Any?> = doc.data ?: return@mapNotNull null
                        android.util.Log.d("FirebaseBackup", "🔍 Mapeando venta ${doc.id}, campos: ${data.keys.joinToString()}")
                        
                        // Manejar items: puede venir como String o como List
                        val itemsString = when (val itemsValue = data["items"]) {
                            is String -> {
                                android.util.Log.d("FirebaseBackup", "✅ Items como String: ${itemsValue.take(100)}...")
                                itemsValue
                            }
                            is List<*> -> {
                                android.util.Log.d("FirebaseBackup", "🔄 Items como List, convirtiendo a String...")
                                // Si viene como lista, serializar a String usando el formato esperado
                                itemsValue.joinToString(separator = "||") { item ->
                                    when (item) {
                                        is Map<*, *> -> {
                                            val productId = (item["productId"] ?: item["product_id"])?.toString() ?: ""
                                            val productName = (item["productName"] ?: item["product_name"])?.toString()?.replace("|", "/") ?: ""
                                            val quantity = (item["quantity"] ?: item["qty"])?.toString() ?: "0"
                                            val unitPrice = (item["unitPrice"] ?: item["unit_price"])?.toString() ?: "0.0"
                                            "$productId|$productName|$quantity|$unitPrice"
                                        }
                                        else -> ""
                                    }
                                }
                            }
                            else -> {
                                android.util.Log.w("FirebaseBackup", "⚠️ Items en formato desconocido: ${itemsValue?.javaClass?.simpleName}")
                                ""
                            }
                        }
                        
                        // Verificar que el customerId existe antes de asignarlo
                        val customerIdFromFirebase = data["customerId"] as? String
                        val validCustomerId = if (customerIdFromFirebase != null && customerIdFromFirebase.isNotBlank()) {
                            // Verificar si el cliente existe en la base de datos local
                            val customerExists = try {
                                customerDao.getAllCustomers().first().any { it.id == customerIdFromFirebase }
                            } catch (e: Exception) {
                                android.util.Log.w("FirebaseBackup", "⚠️ Error verificando cliente $customerIdFromFirebase: ${e.message}")
                                false
                            }
                            if (customerExists) {
                                customerIdFromFirebase
                            } else {
                                android.util.Log.w("FirebaseBackup", "⚠️ Cliente $customerIdFromFirebase no existe, estableciendo customerId como null")
                                null
                            }
                        } else {
                            null
                        }
                        
                        val saleEntity = com.negociolisto.app.data.local.entity.SaleEntity(
                            id = doc.id,
                            customerId = validCustomerId, // Usar el customerId validado o null
                            items = itemsString,
                            total = (data["total"] as? Number)?.toDouble() ?: 0.0,
                            date = when (val dateValue = data["date"]) {
                                is Long -> dateValue
                                is Number -> dateValue.toLong()
                                is String -> dateValue.toLongOrNull() ?: System.currentTimeMillis()
                                else -> System.currentTimeMillis()
                            },
                            paymentMethod = data["paymentMethod"] as? String ?: "CASH",
                            note = data["note"] as? String,
                            status = data["status"] as? String ?: "COMPLETED",
                            canceledAt = when (val canceledAtValue = data["canceledAt"]) {
                                is Long -> canceledAtValue
                                is Number -> canceledAtValue.toLong()
                                is String -> canceledAtValue.toLongOrNull()
                                null -> null
                                else -> null
                            },
                            canceledReason = data["canceledReason"] as? String
                        )
                        android.util.Log.d("FirebaseBackup", "✅ Venta ${doc.id} mapeada correctamente: items=${saleEntity.items.take(50)}..., total=${saleEntity.total}")
                        saleEntity
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseBackup", "❌ Error mapeando venta ${doc.id}: ${e.message}", e)
                        e.printStackTrace()
                        null
                    }
                }
                if (sales.isNotEmpty()) {
                    android.util.Log.d("FirebaseBackup", "💾 Insertando ${sales.size} ventas en la base de datos local...")
                    saleDao.insertSales(sales)
                    android.util.Log.d("FirebaseBackup", "✅ ${sales.size} ventas restauradas exitosamente")
                    println("✅ Ventas restauradas: ${sales.size}")
                } else {
                    android.util.Log.w("FirebaseBackup", "⚠️ No se encontraron ventas para restaurar")
                    println("⚠️ No se encontraron ventas para restaurar")
                }
            } catch (e: Exception) {
                android.util.Log.e("FirebaseBackup", "❌ Error restaurando ventas: ${e.message}", e)
                println("⚠️ Error restaurando ventas: ${e.message}")
                e.printStackTrace()
            }
            
            // 6. Restaurar gastos
            onProgress(85, "Restaurando gastos...")
            try {
                val expensesSnapshot = firestore.collection("users/$userId/expenses").get().await()
                println("🔍 DEBUG: Encontrados ${expensesSnapshot.documents.size} gastos en Firebase")
                val expenses = expensesSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data: Map<String, Any?> = doc.data ?: return@mapNotNull null
                        println("🔍 DEBUG: Mapeando gasto ${doc.id}, datos: ${data.keys}")
                        
                        val dateValue = when (val dateValueRaw = data["date"]) {
                            is Long -> dateValueRaw
                            is Number -> dateValueRaw.toLong()
                            is String -> dateValueRaw.toLongOrNull() ?: System.currentTimeMillis()
                            else -> System.currentTimeMillis()
                        }
                        
                        val createdAtValue = when (val createdAtRaw = data["createdAt"]) {
                            is Long -> createdAtRaw
                            is Number -> createdAtRaw.toLong()
                            is String -> createdAtRaw.toLongOrNull() ?: dateValue
                            else -> dateValue
                        }
                        
                        val updatedAtValue = when (val updatedAtRaw = data["updatedAt"]) {
                            is Long -> updatedAtRaw
                            is Number -> updatedAtRaw.toLong()
                            is String -> updatedAtRaw.toLongOrNull() ?: createdAtValue
                            else -> createdAtValue
                        }
                        
                        com.negociolisto.app.data.local.entity.ExpenseEntity(
                            id = doc.id,
                            description = data["description"] as? String ?: "",
                            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                            category = data["category"] as? String ?: "OTHER",
                            supplier = data["supplier"] as? String,
                            date = dateValue,
                            receiptNumber = data["receiptNumber"] as? String,
                            notes = data["notes"] as? String,
                            status = data["status"] as? String ?: "PENDING",
                            createdAt = createdAtValue,
                            updatedAt = updatedAtValue
                        )
                    } catch (e: Exception) {
                        println("⚠️ Error mapeando gasto ${doc.id}: ${e.message}")
                        e.printStackTrace()
                        null
                    }
                }
                if (expenses.isNotEmpty()) {
                    expenseDao.insertExpenses(expenses)
                    println("✅ Gastos restaurados: ${expenses.size}")
                } else {
                    println("⚠️ No se encontraron gastos para restaurar")
                }
            } catch (e: Exception) {
                println("⚠️ Error restaurando gastos: ${e.message}")
                e.printStackTrace()
            }
            
            // 7. Restaurar facturas
            onProgress(90, "Restaurando facturas...")
            try {
                val invoicesSnapshot = firestore.collection("users/$userId/invoices").get().await()
                val invoices = invoicesSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data: Map<String, Any?> = doc.data ?: return@mapNotNull null
                        
                        // Mapear items de la factura
                        val itemsList = (data["items"] as? List<*>)?.mapNotNull { itemData ->
                            try {
                                @Suppress("UNCHECKED_CAST")
                                val itemMap = itemData as? Map<String, Any?> ?: return@mapNotNull null
                                InvoiceItem(
                                    description = itemMap["description"] as? String ?: "",
                                    quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0,
                                    unitPrice = (itemMap["unitPrice"] as? Number)?.toDouble() ?: 0.0
                                )
                            } catch (e: Exception) {
                                println("⚠️ Error mapeando item de factura: ${e.message}")
                                null
                            }
                        } ?: emptyList()
                        
                        // Mapear fecha
                        val dateMillis = (data["date"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        val date = Instant.fromEpochMilliseconds(dateMillis)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        
                        // Mapear template
                        val templateName = data["template"] as? String ?: "CLASSIC"
                        val template = try {
                            InvoiceTemplateType.valueOf(templateName)
                        } catch (e: Exception) {
                            InvoiceTemplateType.CLASSIC
                        }
                        
                        InvoiceEntity(
                            id = doc.id,
                            number = data["number"] as? String ?: "",
                            saleId = data["saleId"] as? String,
                            customerId = data["customerId"] as? String,
                            items = itemsList,
                            subtotal = (data["subtotal"] as? Number)?.toDouble() ?: 0.0,
                            tax = (data["tax"] as? Number)?.toDouble() ?: 0.0,
                            total = (data["total"] as? Number)?.toDouble() ?: 0.0,
                            date = date,
                            template = template,
                            notes = data["notes"] as? String
                        )
                    } catch (e: Exception) {
                        println("⚠️ Error mapeando factura ${doc.id}: ${e.message}")
                        null
                    }
                }
                if (invoices.isNotEmpty()) {
                    invoiceDao.insertInvoices(invoices)
                    println("✅ Facturas restauradas: ${invoices.size}")
                }
            } catch (e: Exception) {
                println("⚠️ Error restaurando facturas: ${e.message}")
            }
            
            // ✅ NOTA: Chat messages y responses se restauran automáticamente desde Firestore
            // ya que están en tiempo real y se sincronizan automáticamente cuando se accede
            
            onProgress(100, "Restauración completada")
            
            Result.success("Restauración completada exitosamente")
            
        } catch (e: Exception) {
            Result.failure(Exception("Error restaurando desde Firebase: ${e.message}"))
        }
    }
    
    /**
     * 🗑️ LIMPIAR BACKUP
     * 
     * Elimina todos los datos de backup del usuario
     */
    suspend fun clearBackup(userId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val batch = firestore.batch()
            
            // Eliminar todas las colecciones del usuario
            val collections = listOf("products", "customers", "sales", "expenses", "collections", "invoices", "customCategories", "stockMovements")
            
            collections.forEach { collection ->
                val snapshot = firestore.collection("users/$userId/$collection").get().await()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
            }
            
            // Eliminar metadatos
            batch.delete(
                firestore.collection("users")
                    .document(userId)
                    .collection("metadata")
                    .document("backup")
            )
            batch.delete(firestore.collection("users/$userId").document("syncMetadata"))
            
            android.util.Log.d("FirebaseBackup", "💾 Ejecutando batch commit...")
            batch.commit().await()
            android.util.Log.d("FirebaseBackup", "✅ Batch commit completado exitosamente")
            
            Result.success("Backup eliminado exitosamente")
            
        } catch (e: Exception) {
            Result.failure(Exception("Error eliminando backup: ${e.message}"))
        }
    }

    private suspend fun syncChatsAndResponses(
        userId: String,
        onProgress: (Int, String) -> Unit
    ) {
        try {
            // ✅ CORREGIDO: Solo sincronizar colecciones del usuario actual
            val firestoreCollections = firestore.collection("users/$userId/collections").get().await()
            val totalCollections = firestoreCollections.size().takeIf { it > 0 } ?: 1
            var processedCollections = 0

            firestoreCollections.documents.forEach { collectionDoc ->
                processedCollections += 1
                val collectionId = collectionDoc.id

                val progressStep = 96 + ((processedCollections * 3) / totalCollections)
                val boundedProgress = progressStep.coerceAtMost(99)
                onProgress(boundedProgress, "Sincronizando chats de colección ${collectionDoc.id}...")

                try {
                    // ✅ CORREGIDO: Usar ruta privada del usuario para chats
                    val collectionRoot = firestore.collection("users/$userId/collections").document(collectionId)
                    val chatMessages = collectionRoot.collection("chat").get().await()

                    chatMessages.documents.forEach { messageDoc ->
                        val payload = (messageDoc.data ?: emptyMap<String, Any>()).toMutableMap()
                        payload["collectionId"] = collectionId
                        payload["sourcePath"] = messageDoc.reference.path

                        firestore.collection("users")
                            .document(userId)
                            .collection("chatMessages")
                            .document("${collectionId}_${messageDoc.id}")
                            .set(payload)
                            .await()
                    }
                } catch (chatError: Exception) {
                    android.util.Log.w(
                        "FirebaseBackup",
                        "⚠️ No se pudieron sincronizar chats de colección $collectionId: ${chatError.message}"
                    )
                }

                try {
                    // ✅ CORREGIDO: Usar ruta privada del usuario para responses
                    val responses = firestore.collection("users/$userId/collections")
                        .document(collectionId)
                        .collection("responses")
                        .get()
                        .await()

                    responses.documents.forEach { responseDoc ->
                        val payload = (responseDoc.data ?: emptyMap<String, Any>()).toMutableMap()
                        payload["collectionId"] = collectionId
                        payload["sourcePath"] = responseDoc.reference.path

                        firestore.collection("users")
                            .document(userId)
                            .collection("collectionResponses")
                            .document("${collectionId}_${responseDoc.id}")
                            .set(payload)
                            .await()
                    }
                } catch (responseError: Exception) {
                    android.util.Log.w(
                        "FirebaseBackup",
                        "⚠️ No se pudieron sincronizar responses de colección $collectionId: ${responseError.message}"
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.w(
                "FirebaseBackup",
                "⚠️ Error general durante la sincronización de chats/responses: ${e.message}"
            )
        }
    }
}

private fun Product.toFirestoreMap(): Map<String, Any?> {
    val createdAtMillis = createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    val updatedAtMillis = updatedAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    return mapOf(
        "id" to id,
        "name" to name,
        "description" to (description ?: ""),
        "sku" to sku,
        "purchasePrice" to purchasePrice,
        "salePrice" to salePrice,
        "stockQuantity" to stockQuantity,
        "minimumStock" to minimumStock,
        "customCategoryId" to customCategoryId,
        "supplier" to (supplier ?: ""),
        "photoUrl" to (photoUrl ?: ""),
        "thumbnailUrl" to (thumbnailUrl ?: ""),
        "imageBackupUrl" to (imageBackupUrl ?: ""),
        "createdAt" to createdAtMillis,
        "updatedAt" to updatedAtMillis,
        "isActive" to isActive
    )
}

private fun InvoiceEntity.toFirestoreMap(): Map<String, Any?> {
    val zone = TimeZone.currentSystemDefault()
    return mapOf(
        "id" to id,
        "number" to number,
        "saleId" to saleId,
        "customerId" to customerId,
        "items" to items.map { item ->
            mapOf(
                "description" to item.description,
                "quantity" to item.quantity,
                "unitPrice" to item.unitPrice,
                "total" to item.total
            )
        },
        "subtotal" to subtotal,
        "tax" to tax,
        "total" to total,
        "date" to date.toInstant(zone).toEpochMilliseconds(),
        "template" to template.name,
        "notes" to (notes ?: "")
    )
}