package com.negociolisto.app.data.service

import android.content.Context
import com.negociolisto.app.data.local.database.NegocioListoDatabase
import androidx.room.withTransaction
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.data.preferences.ThemeManager
import com.negociolisto.app.data.preferences.ThemeConfig
import com.negociolisto.app.data.parsing.SocialMediaParser
import com.negociolisto.app.data.parsing.BusinessSocialMedia
import com.negociolisto.app.data.service.LoginTrackingService
import com.negociolisto.app.data.service.LoginInfo
import com.negociolisto.app.data.remote.firebase.FirebaseBackupRepository
import com.negociolisto.app.data.service.ImageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * 💾 SERVICIO DE BACKUP UNIFICADO
 * 
 * Servicio principal de backup que usa Firebase como sistema único de backup en la nube.
 * 
 * ESTRATEGIA:
 * - Firebase: Backup primario automático y continuo
 * - Room: Almacenamiento local principal
 */
@Singleton
class BackupService @Inject constructor(
    private val context: Context,
    private val customCategoryRepository: CustomCategoryRepository,
    private val authRepository: AuthRepository,
    private val themeManager: ThemeManager,
    private val socialMediaParser: SocialMediaParser,
    private val loginTrackingService: LoginTrackingService,
    private val firebaseBackupRepository: FirebaseBackupRepository,
    private val imageService: ImageService
) {
    
    /**
     * 🔄 BACKUP PRIMARIO A FIREBASE
     * 
     * Realiza backup automático a Firebase como sistema primario
     */
    suspend fun performFirebaseBackup(
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Obtener usuario actual
            val currentUser = authRepository.currentUser.first()
            if (currentUser == null) {
                return@withContext Result.failure(Exception("Usuario no autenticado"))
            }
            
            // Realizar backup a Firebase
            val result = firebaseBackupRepository.createFullBackup(currentUser.id, onProgress)
            
            if (result.isSuccess) {
                // Guardar última fecha de backup
                context.getSharedPreferences("backup_data", Context.MODE_PRIVATE)
                    .edit()
                    .putLong("last_firebase_backup_time", System.currentTimeMillis())
                    .apply()
            }
            
            result
            
        } catch (e: Exception) {
            Result.failure(Exception("Error en backup de Firebase: ${e.message}"))
        }
    }
    
    
    /**
     * 🔄 HACER BACKUP COMPLETO (MÉTODO PRINCIPAL)
     * 
     * Realiza backup a Firebase como sistema único de backup en la nube
     */
    suspend fun performFullBackup(
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Realizar backup a Firebase
            val firebaseResult = performFirebaseBackup(onProgress)
            
            if (firebaseResult.isSuccess) {
                // Guardar última fecha de backup
                context.getSharedPreferences("backup_data", Context.MODE_PRIVATE)
                    .edit()
                    .putLong("last_backup_time", System.currentTimeMillis())
                    .apply()
            }
            
            firebaseResult
            
        } catch (e: Exception) {
            Result.failure(Exception("Error en backup: ${e.message}"))
        }
    }
    
    /**
     * 🔄 HACER BACKUP COMPLETO (versión sin progreso para compatibilidad)
     */
    suspend fun performFullBackup(): Result<String> = performFullBackup { _, _ -> }
    
    /**
     * 🔄 RESTAURAR DATOS DESDE BACKUP
     * 
     * Restaura datos reales desde JSON a la base de datos Room
     */
    suspend fun restoreFromBackup(
        jsonContent: String,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            println("🔄 DEBUG: Iniciando restauración real...")
            onProgress(0, "Iniciando restauración...")
            
            val database = NegocioListoDatabase.getDatabase(context)
            
            onProgress(20, "Parseando datos del backup...")
            // Parsear datos reales del backup JSON
            val backupData = parseBackupData(jsonContent)
            
            onProgress(40, "Limpiando e insertando datos (transacción)...")
            // Obtener userId antes de la transacción
            val currentUserId = getCurrentUserId() ?: ""
            // Ejecutar limpieza e inserciones en una única transacción para asegurar invalidación atómica
            database.withTransaction {
                // Limpiar datos existentes
                database.productDao().clearAllProducts()
                database.customerDao().clearAllCustomers()
                database.saleDao().clearAllSales()
                database.expenseDao().clearAllExpenses()
                // ✅ Limpiar colecciones del usuario actual
                if (currentUserId.isNotBlank()) {
                    database.collectionDao().clearAllCollections(currentUserId)
                }
                database.invoiceDao().clearAllInvoices()
                // Limpiar movimientos de stock
                database.stockMovementDao().clearAllMovements()
                // Eliminar categorías personalizadas del usuario actual
                if (currentUserId.isNotBlank()) {
                    customCategoryRepository.deleteAllCategoriesByUser(currentUserId)
                }

                // Restaurar productos
                if (backupData.products.isNotEmpty()) {
                    database.productDao().insertProducts(backupData.products)
                }

                // Restaurar clientes
                if (backupData.customers.isNotEmpty()) {
                    database.customerDao().insertCustomers(backupData.customers)
                }

                // Restaurar ventas
                if (backupData.sales.isNotEmpty()) {
                    database.saleDao().insertSales(backupData.sales)
                }

                // Restaurar gastos
                if (backupData.expenses.isNotEmpty()) {
                    database.expenseDao().insertExpenses(backupData.expenses)
                }

                // Restaurar colecciones con manejo de errores mejorado
                if (backupData.collections.isNotEmpty()) {
                    try {
                        database.collectionDao().insertCollections(backupData.collections)
                        println("✅ Colecciones restauradas: ${backupData.collections.size}")
                    } catch (e: Exception) {
                        println("❌ Error restaurando colecciones: ${e.message}")
                        // Intentar restaurar una por una para identificar problemas
                        backupData.collections.forEach { collection ->
                            try {
                                database.collectionDao().insertCollection(collection)
                            } catch (e: Exception) {
                                println("❌ Error restaurando colección ${collection.id}: ${e.message}")
                            }
                        }
                    }
                }
                
                // Restaurar items de colecciones con manejo de errores mejorado
                if (backupData.collectionItems.isNotEmpty()) {
                    try {
                        database.collectionDao().insertItems(backupData.collectionItems)
                        println("✅ Items de colecciones restaurados: ${backupData.collectionItems.size}")
                    } catch (e: Exception) {
                        println("❌ Error restaurando items de colecciones: ${e.message}")
                        // Intentar restaurar uno por uno para identificar problemas
                        backupData.collectionItems.forEach { item ->
                            try {
                                database.collectionDao().insertItems(listOf(item))
                            } catch (e: Exception) {
                                println("❌ Error restaurando item ${item.collectionId}: ${e.message}")
                            }
                        }
                    }
                }

                // Restaurar facturas
                if (backupData.invoices.isNotEmpty()) {
                    database.invoiceDao().insertInvoices(backupData.invoices)
                }

                // Restaurar movimientos de stock
                if (backupData.stockMovements.isNotEmpty()) {
                    database.stockMovementDao().insertMovements(backupData.stockMovements)
                }
            }
            
            onProgress(92, "Restaurando categorías personalizadas...")
            if (backupData.customCategories.isNotEmpty()) {
                // Restaurar categorías personalizadas
                backupData.customCategories.forEach { category ->
                    try {
                        // Verificar si la categoría ya existe
                        val existing = customCategoryRepository.getCategoryByName(category.userId, category.name)
                        if (existing != null) {
                            // Si existe, actualizar en lugar de crear duplicado
                            customCategoryRepository.updateCategory(category)
                            println("✅ Categoría actualizada: ${category.name}")
                        } else {
                            // Si no existe, crear nueva
                            customCategoryRepository.addCategory(category)
                            println("✅ Categoría creada: ${category.name}")
                        }
                    } catch (e: Exception) {
                        println("⚠️ Error restaurando categoría ${category.name}: ${e.message}")
                    }
                }
            }
            
            onProgress(94, "Restaurando información del usuario...")
            if (backupData.user != null) {
                try {
                    // Guardar información del usuario en SharedPreferences
                    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putString("user_name", backupData.user.name)
                    editor.putString("user_email", backupData.user.email)
                    editor.putString("user_phone", backupData.user.phone)
                    editor.putString("business_name", backupData.user.businessName)
                    editor.putString("business_type", backupData.user.businessType?.name)
                    editor.putString("business_rut", backupData.user.businessRut)
                    editor.putString("business_address", backupData.user.businessAddress)
                    editor.putString("business_phone", backupData.user.businessPhone)
                    editor.putString("business_email", backupData.user.businessEmail)
                    editor.putString("business_logo_url", backupData.user.businessLogoUrl)
                    
                    // Restaurar redes sociales del negocio
                    backupData.user.businessSocialMedia?.let { socialMedia ->
                        val parsingSocialMedia = com.negociolisto.app.data.parsing.BusinessSocialMedia(
                            instagram = socialMedia.instagram,
                            facebook = socialMedia.facebook,
                            twitter = socialMedia.twitter,
                            linkedin = socialMedia.linkedin,
                            tiktok = socialMedia.tiktok,
                            youtube = null,
                            website = socialMedia.website,
                            whatsapp = null,
                            telegram = null
                        )
                        val socialMediaJson = socialMediaParser.toJson(parsingSocialMedia)
                        if (socialMediaJson != null) {
                            editor.putString("business_social_media", socialMediaJson.toString())
                        }
                    }
                    
                    editor.putString("profile_photo_url", backupData.user.profilePhotoUrl)
                    
                    // Restaurar información de login
                    val lastLoginInstant = backupData.user.lastLoginAt?.let { localDateTime ->
                        localDateTime.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault())
                    }
                    val loginInfo = LoginInfo(
                        lastLogin = lastLoginInstant,
                        firstLogin = null, // Se puede agregar si se incluye en el modelo
                        loginCount = 0 // Se puede agregar si se incluye en el modelo
                    )
                    loginTrackingService.restoreLoginInfo(loginInfo)
                    
                    editor.apply()
                    println("✅ Información del usuario restaurada")
                } catch (e: Exception) {
                    println("⚠️ Error restaurando información del usuario: ${e.message}")
                }
            }
            
            onProgress(95, "Aplicando configuraciones...")
            applySettings(backupData.settings)
            
            onProgress(100, "Restauración completada")
            
            // TODO: Recalcular totales de compras de clientes basándose en facturas existentes
            
            Result.success("✅ Restauración completada exitosamente")
            
        } catch (e: Exception) {
            println("❌ DEBUG: Error general: ${e.message}")
            onProgress(0, "Error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * 🔄 RESTAURAR DATOS DESDE BACKUP (versión sin progreso para compatibilidad)
     */
    suspend fun restoreFromBackup(): Result<String> = restoreFromBackup("") { _, _ -> }
    
    /**
     * 📊 OBTENER ESTADO DEL BACKUP
     */
    suspend fun getBackupStatus(): Result<BackupStatus> = withContext(Dispatchers.IO) {
        try {
            val database = NegocioListoDatabase.getDatabase(context)
            
            // Verificar si hay datos locales
            val hasLocalData = try {
                val productCount = database.productDao().getTotalProductCount()
                productCount > 0
            } catch (e: Exception) {
                false
            }
            
            // Verificar si hay backups en la nube (simulado por ahora)
            val hasCloudData = false
            
            // Obtener conteo de datos locales
            val dataCount = try {
                val productCount = database.productDao().getTotalProductCount()
                val customerCount = database.customerDao().getTotalCustomerCount()
                val saleCount = database.saleDao().getTotalSaleCount()
                val expenseCount = database.expenseDao().getTotalExpenseCount()
                
                DataCount(
                    products = productCount,
                    customers = customerCount,
                    sales = saleCount,
                    expenses = expenseCount
                )
            } catch (e: Exception) {
                DataCount(0, 0, 0, 0)
            }
            
            val status = BackupStatus(
                hasLocalData = hasLocalData,
                hasCloudData = hasCloudData,
                lastBackupTime = getLastBackupTime(),
                dataCount = dataCount
            )
            
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * ⏰ OBTENER ÚLTIMA VEZ QUE SE HIZO BACKUP
     */
    private fun getLastBackupTime(): Long? {
        val prefs = context.getSharedPreferences("backup_data", Context.MODE_PRIVATE)
        return if (prefs.contains("last_backup_time")) {
            prefs.getLong("last_backup_time", 0)
        } else {
            null
        }
    }
    
    /**
     * 📊 CREAR DATOS DESDE BACKUP
     */
    private suspend fun createDataFromBackup(jsonContent: String): BackupData {
        return try {
            // Parsear el JSON real del backup
            val jsonObject = org.json.JSONObject(jsonContent)
            parseBackupDataFromObject(jsonObject)
        } catch (e: Exception) {
            println("❌ Error parseando backup JSON: ${e.message}")
            BackupData() // Retornar datos vacíos en caso de error
        }
    }
    
    /**
     * ⚙️ APLICAR CONFIGURACIONES
     */
    private fun applySettings(settings: Map<String, String>) {
        try {
            val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            settings.forEach { (key, value) ->
                editor.putString(key, value)
            }
            
            editor.apply()
            
            // Aplicar escala de la interfaz si está disponible
            settings["app_scale"]?.let { scaleString ->
                try {
                    val scale = scaleString.toFloat()
                    val uiPrefs = context.getSharedPreferences("ui_prefs_mirror", Context.MODE_PRIVATE)
                    uiPrefs.edit().putFloat("app_scale", scale).apply()
                    
                    // También guardar en las preferencias principales para asegurar que se aplique
                    val mainPrefs = context.getSharedPreferences("ui_preferences", Context.MODE_PRIVATE)
                    mainPrefs.edit().putFloat("app_scale", scale).apply()
                    
                    println("✅ Escala de interfaz restaurada: $scale")
                } catch (e: Exception) {
                    println("⚠️ Error restaurando escala de interfaz: ${e.message}")
                }
            }
            
            // Restaurar configuración de temas
            settings["theme"]?.let { themeJson ->
                try {
                    val themeData = org.json.JSONObject(themeJson.toString())
                    val themeConfig = ThemeConfig(
                        theme = themeData.optString("current_theme", "system"),
                        primaryColor = themeData.optInt("primary_color", -1),
                        secondaryColor = themeData.optInt("secondary_color", -1),
                        isDark = themeData.optBoolean("is_dark", false),
                        useSystemTheme = themeData.optBoolean("use_system_theme", true)
                    )
                    
                    themeManager.restoreThemeConfig(themeConfig)
                    println("✅ Configuración de temas restaurada: ${themeConfig.theme}")
                } catch (e: Exception) {
                    println("⚠️ Error restaurando configuración de temas: ${e.message}")
                }
            }
        } catch (e: Exception) {
            // No fallar la restauración por configuraciones
        }
    }
    
    // MÉTODOS PARA CREAR DATOS DE EJEMPLO BASADOS EN BACKUP
    private fun createSampleProductsFromBackup(): List<com.negociolisto.app.data.local.entity.ProductEntity> {
        return listOf(
            com.negociolisto.app.data.local.entity.ProductEntity(
                id = "backup_prod_1",
                name = "Producto desde Backup",
                description = "Restaurado desde tu backup de Firebase",
                sku = "GD001",
                purchasePrice = 20.0,
                salePrice = 35.0,
                stockQuantity = 40,
                minimumStock = 5,
                customCategoryId = "default_1", // Usar categoría predefinida
                supplier = "Proveedor Backup",
                photoUrl = null,
                thumbnailUrl = null,
                imageBackupUrl = null,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            com.negociolisto.app.data.local.entity.ProductEntity(
                id = "backup_prod_2",
                name = "Producto Premium Restaurado",
                description = "Producto premium desde backup",
                sku = "GD002",
                purchasePrice = 50.0,
                salePrice = 80.0,
                stockQuantity = 15,
                minimumStock = 2,
                customCategoryId = "default_2", // Usar categoría predefinida
                supplier = "Proveedor Backup",
                photoUrl = null,
                thumbnailUrl = null,
                imageBackupUrl = null,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
    
    private fun createSampleCustomersFromBackup(): List<com.negociolisto.app.data.local.entity.CustomerEntity> {
        return listOf(
            com.negociolisto.app.data.local.entity.CustomerEntity(
                id = "backup_cust_1",
                name = "Cliente desde Backup",
                companyName = "Empresa Restaurada",
                email = "cliente@restaurado.com",
                phone = "555-0001",
                address = "Dirección restaurada 123",
                totalPurchases = 150.0,
                lastPurchaseDate = System.currentTimeMillis(),
                notes = "Cliente restaurado desde backup",
                createdAt = System.currentTimeMillis()
            ),
            com.negociolisto.app.data.local.entity.CustomerEntity(
                id = "backup_cust_2",
                name = "Cliente VIP Restaurado",
                companyName = "Empresa VIP",
                email = "vip@restaurado.com",
                phone = "555-0002",
                address = "Dirección VIP 456",
                totalPurchases = 500.0,
                lastPurchaseDate = System.currentTimeMillis(),
                notes = "Cliente VIP restaurado desde backup",
                createdAt = System.currentTimeMillis()
            )
        )
    }
    
    private fun createSampleSalesFromBackup(): List<com.negociolisto.app.data.local.entity.SaleEntity> {
        return listOf(
            com.negociolisto.app.data.local.entity.SaleEntity(
                id = "backup_sale_1",
                customerId = "backup_cust_1",
                items = """[{"productId":"backup_prod_1","productName":"Producto desde Backup","quantity":2,"unitPrice":35.0}]""",
                total = 70.0,
                date = System.currentTimeMillis(),
                paymentMethod = "card",
                note = "Venta restaurada desde backup",
                status = "completed",
                canceledAt = null,
                canceledReason = ""
            )
        )
    }
    
    private fun createSampleExpensesFromBackup(): List<com.negociolisto.app.data.local.entity.ExpenseEntity> {
        return listOf(
            com.negociolisto.app.data.local.entity.ExpenseEntity(
                id = "backup_exp_1",
                description = "Gasto restaurado desde backup",
                amount = 45.0,
                category = "General", // Este es para Expense, no Product
                date = System.currentTimeMillis(),
                notes = "Gasto restaurado desde backup",
                supplier = "Proveedor Restaurado",
                receiptNumber = "GD-001",
                status = "completed",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
    
    private suspend fun createSampleCollectionsFromBackup(): List<com.negociolisto.app.data.local.entity.CollectionEntity> {
        val userId = getCurrentUserId() ?: "unknown"
        return listOf(
            com.negociolisto.app.data.local.entity.CollectionEntity(
                id = "backup_coll_1",
                userId = userId, // ✅ Agregar userId
                name = "Colección Restaurada",
                description = "Colección restaurada desde backup",
                associatedCustomerIds = """["backup_cust_1","backup_cust_2"]""",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                status = "active",
                color = "#4CAF50"
            )
        )
    }
    
    private fun createSampleInvoicesFromBackup(): List<com.negociolisto.app.data.local.entity.InvoiceEntity> {
        return listOf(
            com.negociolisto.app.data.local.entity.InvoiceEntity(
                id = "backup_inv_1",
                number = "INV-GD-001",
                saleId = "backup_sale_1",
                customerId = "backup_cust_1",
                items = emptyList(),
                subtotal = 70.0,
                tax = 7.0,
                total = 77.0,
                date = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis())
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
                template = com.negociolisto.app.domain.model.InvoiceTemplateType.CLASSIC,
                notes = "Factura restaurada desde backup"
            )
        )
    }
    
    /**
     * 📊 PARSEAR DATOS DE BACKUP DESDE JSONOBJECT
     */
    private suspend fun parseBackupDataFromObject(jsonObject: org.json.JSONObject): BackupData {
        return try {
            val currentUserId = getCurrentUserId()
            BackupData(
                products = parseProducts(jsonObject.optJSONArray("products")),
                customers = parseCustomers(jsonObject.optJSONArray("customers")),
                sales = parseSales(jsonObject.optJSONArray("sales")),
                expenses = parseExpenses(jsonObject.optJSONArray("expenses")),
                collections = parseCollections(jsonObject.optJSONArray("collections")),
                invoices = parseInvoices(jsonObject.optJSONArray("invoices")),
                customCategories = parseCustomCategories(jsonObject.optJSONArray("customCategories"), currentUserId),
                user = parseUser(jsonObject.optJSONObject("user"), currentUserId),
                settings = parseSettings(jsonObject),
                metadata = parseMetadata(jsonObject)
            )
        } catch (e: Exception) {
            println("❌ Error parseando backup: ${e.message}")
            BackupData() // Retornar datos vacíos en caso de error
        }
    }
    
    /**
     * 📊 PARSEAR DATOS DE BACKUP
     */
    private suspend fun parseBackupData(jsonContent: String): BackupData {
        return try {
            val jsonObject = org.json.JSONObject(jsonContent)
            val currentUserId = getCurrentUserId()
            BackupData(
                products = parseProducts(jsonObject.optJSONArray("products")),
                customers = parseCustomers(jsonObject.optJSONArray("customers")),
                sales = parseSales(jsonObject.optJSONArray("sales")),
                expenses = parseExpenses(jsonObject.optJSONArray("expenses")),
                collections = parseCollections(jsonObject.optJSONArray("collections")),
                invoices = parseInvoices(jsonObject.optJSONArray("invoices")),
                customCategories = parseCustomCategories(jsonObject.optJSONArray("customCategories"), currentUserId),
                user = parseUser(jsonObject.optJSONObject("user"), currentUserId),
                settings = parseSettings(jsonObject),
                metadata = parseMetadata(jsonObject)
            )
        } catch (e: Exception) {
            println("❌ Error parseando backup: ${e.message}")
            BackupData() // Retornar datos vacíos en caso de error
        }
    }
    
    private fun parseProducts(productsArray: org.json.JSONArray?): List<com.negociolisto.app.data.local.entity.ProductEntity> {
        return try {
            if (productsArray == null) return emptyList()
            
            val products = mutableListOf<com.negociolisto.app.data.local.entity.ProductEntity>()
            for (i in 0 until productsArray.length()) {
                val productObj = productsArray.getJSONObject(i)
                val product = com.negociolisto.app.data.local.entity.ProductEntity(
                    id = productObj.optString("id", ""),
                    name = productObj.optString("name", ""),
                    description = productObj.optString("description", ""),
                    sku = productObj.optString("sku", ""),
                    purchasePrice = productObj.optDouble("purchasePrice", 0.0),
                    salePrice = productObj.optDouble("salePrice", 0.0),
                    stockQuantity = productObj.optInt("stockQuantity", 0),
                    minimumStock = productObj.optInt("minimumStock", 0),
                    customCategoryId = productObj.optString("customCategoryId", "default_1"),
                    supplier = productObj.optString("supplier", ""),
                    photoUrl = productObj.optString("photoUrl", ""),
                    thumbnailUrl = productObj.optString("thumbnailUrl").takeIf { it.isNotEmpty() },
                    imageBackupUrl = productObj.optString("imageBackupUrl").takeIf { it.isNotEmpty() },
                    isActive = productObj.optBoolean("isActive", true),
                    createdAt = productObj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = productObj.optLong("updatedAt", System.currentTimeMillis())
                )
                products.add(product)
            }
            products
        } catch (e: Exception) {
            println("❌ Error parseando productos: ${e.message}")
            emptyList()
        }
    }
    
    private fun parseCustomers(customersArray: org.json.JSONArray?): List<com.negociolisto.app.data.local.entity.CustomerEntity> {
        return try {
            if (customersArray == null) return emptyList()
            
            val customers = mutableListOf<com.negociolisto.app.data.local.entity.CustomerEntity>()
            for (i in 0 until customersArray.length()) {
                val customerObj = customersArray.getJSONObject(i)
                val customer = com.negociolisto.app.data.local.entity.CustomerEntity(
                    id = customerObj.optString("id", ""),
                    name = customerObj.optString("name", ""),
                    companyName = customerObj.optString("companyName", ""),
                    email = customerObj.optString("email", ""),
                    phone = customerObj.optString("phone", ""),
                    address = customerObj.optString("address", ""),
                    totalPurchases = customerObj.optDouble("totalPurchases", 0.0),
                    lastPurchaseDate = if (customerObj.has("lastPurchaseDate") && !customerObj.isNull("lastPurchaseDate") && customerObj.optString("lastPurchaseDate").isNotEmpty()) {
                        try {
                            customerObj.optString("lastPurchaseDate").toLong()
                        } catch (e: Exception) {
                            null
                        }
                    } else null,
                    notes = customerObj.optString("notes", ""),
                    createdAt = if (customerObj.has("createdAt") && !customerObj.isNull("createdAt") && customerObj.optString("createdAt").isNotEmpty()) {
                        try {
                            customerObj.optString("createdAt").toLong()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                    } else System.currentTimeMillis()
                )
                customers.add(customer)
            }
            customers
        } catch (e: Exception) {
            println("❌ Error parseando clientes: ${e.message}")
            emptyList()
        }
    }
    
    private fun parseSales(salesArray: org.json.JSONArray?): List<com.negociolisto.app.data.local.entity.SaleEntity> {
        return try {
            if (salesArray == null) return emptyList()
            
            val sales = mutableListOf<com.negociolisto.app.data.local.entity.SaleEntity>()
            for (i in 0 until salesArray.length()) {
                val saleObj = salesArray.getJSONObject(i)
                val sale = com.negociolisto.app.data.local.entity.SaleEntity(
                    id = saleObj.optString("id", ""),
                    customerId = saleObj.optString("customerId", ""),
                    items = saleObj.optString("items", "[]"),
                    total = saleObj.optDouble("total", 0.0),
                    date = saleObj.optLong("date", System.currentTimeMillis()),
                    paymentMethod = saleObj.optString("paymentMethod", "cash"),
                    note = saleObj.optString("note", ""),
                    status = saleObj.optString("status", "completed"),
                    canceledAt = if (saleObj.has("canceledAt") && !saleObj.isNull("canceledAt")) {
                        saleObj.optLong("canceledAt")
                    } else null,
                    canceledReason = saleObj.optString("canceledReason", "")
                )
                sales.add(sale)
            }
            sales
        } catch (e: Exception) {
            println("❌ Error parseando ventas: ${e.message}")
            emptyList()
        }
    }
    
    private fun parseExpenses(expensesArray: org.json.JSONArray?): List<com.negociolisto.app.data.local.entity.ExpenseEntity> {
        return try {
            if (expensesArray == null) return emptyList()
            
            val expenses = mutableListOf<com.negociolisto.app.data.local.entity.ExpenseEntity>()
            for (i in 0 until expensesArray.length()) {
                val expenseObj = expensesArray.getJSONObject(i)
                val expense = com.negociolisto.app.data.local.entity.ExpenseEntity(
                    id = expenseObj.optString("id", ""),
                    description = expenseObj.optString("description", ""),
                    amount = expenseObj.optDouble("amount", 0.0),
                    category = expenseObj.optString("category", ""),
                    date = expenseObj.optLong("date", System.currentTimeMillis()),
                    notes = expenseObj.optString("notes", ""),
                    supplier = expenseObj.optString("supplier", ""),
                    receiptNumber = expenseObj.optString("receiptNumber", ""),
                    status = expenseObj.optString("status", "completed"),
                    createdAt = expenseObj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = expenseObj.optLong("updatedAt", System.currentTimeMillis())
                )
                expenses.add(expense)
            }
            expenses
        } catch (e: Exception) {
            println("❌ Error parseando gastos: ${e.message}")
            emptyList()
        }
    }
    
    private suspend fun parseCollections(collectionsArray: org.json.JSONArray?): List<com.negociolisto.app.data.local.entity.CollectionEntity> {
        return try {
            if (collectionsArray == null) return emptyList()
            
            // ✅ Obtener userId del usuario actual
            val userId = getCurrentUserId() ?: "unknown"
            
            val collections = mutableListOf<com.negociolisto.app.data.local.entity.CollectionEntity>()
            for (i in 0 until collectionsArray.length()) {
                val collectionObj = collectionsArray.getJSONObject(i)
                // associatedCustomerIds puede venir como arreglo JSON o como string
                val associatedCustomerIdsStr = try {
                    val arr = collectionObj.optJSONArray("associatedCustomerIds")
                    if (arr != null) {
                        val ids = mutableListOf<String>()
                        for (j in 0 until arr.length()) {
                            ids.add(arr.optString(j))
                        }
                        ids.joinToString(",")
                    } else {
                        // Fallback: podría venir como string ya jointeado
                        collectionObj.optString("associatedCustomerIds", "")
                    }
                } catch (_: Exception) {
                    collectionObj.optString("associatedCustomerIds", "")
                }

                // createdAt/updatedAt pueden venir como epoch (long) o como string ISO
                val createdAtEpoch = try {
                    if (collectionObj.has("createdAt") && !collectionObj.isNull("createdAt")) {
                        val createdAny = collectionObj.get("createdAt")
                        when (createdAny) {
                            is Number -> createdAny.toLong()
                            is String -> parseLocalDateTime(createdAny).let { ldt ->
                                // Convert LocalDateTime to epoch ms using current TZ
                                ldt.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
                            }
                            else -> System.currentTimeMillis()
                        }
                    } else System.currentTimeMillis()
                } catch (_: Exception) { System.currentTimeMillis() }

                val updatedAtEpoch = try {
                    if (collectionObj.has("updatedAt") && !collectionObj.isNull("updatedAt")) {
                        val updatedAny = collectionObj.get("updatedAt")
                        when (updatedAny) {
                            is Number -> updatedAny.toLong()
                            is String -> parseLocalDateTime(updatedAny).let { ldt ->
                                ldt.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
                            }
                            else -> System.currentTimeMillis()
                        }
                    } else System.currentTimeMillis()
                } catch (_: Exception) { System.currentTimeMillis() }

                val collection = com.negociolisto.app.data.local.entity.CollectionEntity(
                    id = collectionObj.optString("id", ""),
                    userId = collectionObj.optString("userId", userId), // ✅ Usar userId del backup o del usuario actual
                    name = collectionObj.optString("name", ""),
                    description = collectionObj.optString("description", ""),
                    associatedCustomerIds = associatedCustomerIdsStr,
                    createdAt = createdAtEpoch,
                    updatedAt = updatedAtEpoch,
                    status = collectionObj.optString("status", "active"),
                    color = collectionObj.optString("color", "#FF5722")
                )
                collections.add(collection)
            }
            collections
        } catch (e: Exception) {
            println("❌ Error parseando colecciones: ${e.message}")
            emptyList()
        }
    }
    
    private fun parseCustomCategories(categoriesArray: org.json.JSONArray?, currentUserId: String? = null): List<com.negociolisto.app.domain.model.CustomCategory> {
        return try {
            if (categoriesArray == null) return emptyList()

            val defaultUserId = currentUserId ?: throw IllegalStateException("userId requerido para parsear categorías")
            val categories = mutableListOf<com.negociolisto.app.domain.model.CustomCategory>()
            for (i in 0 until categoriesArray.length()) {
                val categoryObj = categoriesArray.getJSONObject(i)
                val category = com.negociolisto.app.domain.model.CustomCategory(
                    id = categoryObj.optString("id", ""),
                    name = categoryObj.optString("name", ""),
                    icon = categoryObj.optString("icon", "📦"),
                    color = categoryObj.optString("color", "#9E9E9E"),
                    description = categoryObj.optString("description", ""),
                    createdAt = parseLocalDateTime(categoryObj.optString("createdAt", "")),
                    updatedAt = parseLocalDateTime(categoryObj.optString("updatedAt", "")),
                    isActive = categoryObj.optBoolean("isActive", true),
                    userId = categoryObj.optString("userId", defaultUserId),
                    sortOrder = categoryObj.optInt("sortOrder", 0)
                )
                categories.add(category)
            }
            categories
        } catch (e: Exception) {
            println("❌ Error parseando categorías personalizadas: ${e.message}")
            emptyList()
        }
    }

    private fun parseUser(userObj: org.json.JSONObject?, currentUserId: String? = null): com.negociolisto.app.domain.model.User? {
        return try {
            if (userObj == null) return null

            val defaultUserId = currentUserId ?: userObj.optString("id").takeIf { it.isNotEmpty() }
            if (defaultUserId == null) {
                throw IllegalStateException("userId requerido para parsear usuario")
            }

            com.negociolisto.app.domain.model.User(
                id = userObj.optString("id", defaultUserId),
                name = userObj.optString("name", ""),
                email = userObj.optString("email", ""),
                phone = userObj.optString("phone").takeIf { it.isNotEmpty() },
                businessName = userObj.optString("businessName").takeIf { it.isNotEmpty() },
                businessType = parseBusinessType(userObj.optString("businessType").takeIf { it.isNotEmpty() }),
                businessRut = userObj.optString("businessRut").takeIf { it.isNotEmpty() },
                businessAddress = userObj.optString("businessAddress").takeIf { it.isNotEmpty() },
                businessPhone = userObj.optString("businessPhone").takeIf { it.isNotEmpty() },
                businessEmail = userObj.optString("businessEmail").takeIf { it.isNotEmpty() },
                businessLogoUrl = userObj.optString("businessLogoUrl").takeIf { it.isNotEmpty() },
                businessSocialMedia = socialMediaParser.parseSocialMedia(userObj.opt("businessSocialMedia"))?.let { parsingSocialMedia ->
                    com.negociolisto.app.domain.model.BusinessSocialMedia(
                        facebook = parsingSocialMedia.facebook,
                        instagram = parsingSocialMedia.instagram,
                        twitter = parsingSocialMedia.twitter,
                        linkedin = parsingSocialMedia.linkedin,
                        tiktok = parsingSocialMedia.tiktok,
                        website = parsingSocialMedia.website
                    )
                },
                profilePhotoUrl = userObj.optString("profilePhotoUrl").takeIf { it.isNotEmpty() },
                isEmailVerified = userObj.optBoolean("isEmailVerified", false),
                createdAt = parseLocalDateTime(userObj.optString("createdAt", "")),
                updatedAt = parseLocalDateTime(userObj.optString("updatedAt", "")),
                lastLoginAt = loginTrackingService.getLastLogin()?.let { instant ->
                    instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                },
                isCloudSyncEnabled = userObj.optBoolean("isCloudSyncEnabled", true),
                preferences = com.negociolisto.app.domain.model.UserPreferences() // Valores por defecto
            )
        } catch (e: Exception) {
            println("❌ Error parseando usuario: ${e.message}")
            null
        }
    }

    private fun parseBusinessType(businessTypeString: String?): com.negociolisto.app.domain.model.BusinessType? {
        return try {
            if (businessTypeString == null) return null
            com.negociolisto.app.domain.model.BusinessType.valueOf(businessTypeString)
        } catch (e: Exception) {
            println("❌ Error parseando tipo de negocio: ${e.message}")
            null
        }
    }
    
    private fun parseInvoices(invoicesArray: org.json.JSONArray?): List<com.negociolisto.app.data.local.entity.InvoiceEntity> {
        return try {
            if (invoicesArray == null) return emptyList()
            
            val invoices = mutableListOf<com.negociolisto.app.data.local.entity.InvoiceEntity>()
            for (i in 0 until invoicesArray.length()) {
                val invoiceObj = invoicesArray.getJSONObject(i)
                val itemsArray = invoiceObj.optJSONArray("items")
                val items = mutableListOf<com.negociolisto.app.domain.model.InvoiceItem>()
                if (itemsArray != null) {
                    for (j in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(j)
                        items.add(
                            com.negociolisto.app.domain.model.InvoiceItem(
                                description = itemObj.optString("description", ""),
                                quantity = itemObj.optInt("quantity", 0),
                                unitPrice = itemObj.optDouble("unitPrice", 0.0)
                            )
                        )
                    }
                }
                val invoice = com.negociolisto.app.data.local.entity.InvoiceEntity(
                    id = invoiceObj.optString("id", ""),
                    number = invoiceObj.optString("number", ""),
                    saleId = invoiceObj.optString("saleId", ""),
                    customerId = invoiceObj.optString("customerId", ""),
                    items = items,
                    subtotal = invoiceObj.optDouble("subtotal", 0.0),
                    tax = invoiceObj.optDouble("tax", 0.0),
                    total = invoiceObj.optDouble("total", 0.0),
                    date = parseLocalDateTime(invoiceObj.optString("date", "")),
                    template = parseInvoiceTemplate(invoiceObj.optString("template", "CLASSIC")),
                    notes = invoiceObj.optString("notes", "")
                )
                invoices.add(invoice)
            }
            invoices
        } catch (e: Exception) {
            println("❌ Error parseando facturas: ${e.message}")
            emptyList()
        }
    }

    private fun parseCollectionItems(itemsArray: org.json.JSONArray?): List<com.negociolisto.app.data.local.entity.CollectionItemEntity> {
        return try {
            if (itemsArray == null) return emptyList()
            val items = mutableListOf<com.negociolisto.app.data.local.entity.CollectionItemEntity>()
            for (i in 0 until itemsArray.length()) {
                val obj = itemsArray.getJSONObject(i)
                items.add(
                    com.negociolisto.app.data.local.entity.CollectionItemEntity(
                        collectionId = obj.optString("collectionId", ""),
                        productId = obj.optString("productId", ""),
                        notes = obj.optString("notes").takeIf { it.isNotEmpty() },
                        displayOrder = obj.optInt("displayOrder", 0),
                        isFeatured = obj.optBoolean("isFeatured", false),
                        specialPrice = if (obj.has("specialPrice") && !obj.isNull("specialPrice")) obj.optDouble("specialPrice") else null
                    )
                )
            }
            items
        } catch (e: Exception) {
            println("❌ Error parseando items de colección: ${e.message}")
            emptyList()
        }
    }

    private fun parseStockMovements(movementsArray: org.json.JSONArray?): List<com.negociolisto.app.data.local.entity.StockMovementEntity> {
        return try {
            if (movementsArray == null) return emptyList()
            val movements = mutableListOf<com.negociolisto.app.data.local.entity.StockMovementEntity>()
            for (i in 0 until movementsArray.length()) {
                val obj = movementsArray.getJSONObject(i)
                movements.add(
                    com.negociolisto.app.data.local.entity.StockMovementEntity(
                        id = obj.optString("id", ""),
                        productId = obj.optString("productId", ""),
                        movementType = obj.optString("movementType", "IN"),
                        quantity = obj.optInt("quantity", 0),
                        reason = obj.optString("reason", "MANUAL_ADJUSTMENT"),
                        description = obj.optString("description").takeIf { it.isNotEmpty() },
                        referenceId = obj.optString("referenceId").takeIf { it.isNotEmpty() },
                        unitCost = if (obj.has("unitCost") && !obj.isNull("unitCost")) obj.optDouble("unitCost") else null,
                        previousStock = obj.optInt("previousStock", 0),
                        newStock = obj.optInt("newStock", 0),
                        userId = obj.optString("userId").takeIf { it.isNotEmpty() },
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        notes = obj.optString("notes").takeIf { it.isNotEmpty() }
                    )
                )
            }
            movements
        } catch (e: Exception) {
            println("❌ Error parseando movimientos de stock: ${e.message}")
            emptyList()
        }
    }
    
    private fun parseSettings(jsonObject: org.json.JSONObject): Map<String, String> {
        val settings = mutableMapOf<String, String>()
        
        try {
            // Parsear ajustes de usuario
            val userSettings = jsonObject.optJSONObject("userSettings")
            userSettings?.let {
                it.keys().forEach { key ->
                    settings["user_$key"] = it.optString(key, "")
                }
            }
            
            // Parsear ajustes de empresa
            val companySettings = jsonObject.optJSONObject("companySettings")
            companySettings?.let {
                it.keys().forEach { key ->
                    settings["company_$key"] = it.optString(key, "")
                }
            }
            
            // Parsear ajustes de facturación
            val invoiceSettings = jsonObject.optJSONObject("invoiceSettings")
            invoiceSettings?.let {
                it.keys().forEach { key ->
                    settings["invoice_$key"] = it.optString(key, "")
                }
            }
            
            // Parsear configuraciones de la aplicación
            val appSettings = jsonObject.optJSONObject("settings")
            appSettings?.let {
                it.keys().forEach { key ->
                    settings[key] = it.optString(key, "")
                }
            }
            
        } catch (e: Exception) {
            println("❌ Error parseando configuraciones: ${e.message}")
        }
        
        return settings
    }
    
    private fun parseMetadata(jsonObject: org.json.JSONObject): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        
        try {
            metadata["backupDate"] = jsonObject.optLong("backupDate", 0).toString()
            metadata["appVersion"] = jsonObject.optString("appVersion", "1.0")
            metadata["userEmail"] = jsonObject.optString("userEmail", "unknown")
        } catch (e: Exception) {
            println("❌ Error parseando metadatos: ${e.message}")
        }
        
        return metadata
    }
    
    private suspend fun getCurrentUserId(): String? {
        return try {
            authRepository.currentUser.first()?.id
                ?: context.getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("user_id", null)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseLocalDateTime(dateString: String): kotlinx.datetime.LocalDateTime {
        return try {
            if (dateString.isBlank()) {
                kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis())
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            } else {
                // Intentar parsear como timestamp
                dateString.toLongOrNull()?.let { timestamp ->
                    kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                } ?: kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis())
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            }
        } catch (e: Exception) {
            kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis())
                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        }
    }
    
    private fun parseInvoiceTemplate(templateString: String): com.negociolisto.app.domain.model.InvoiceTemplateType {
        return try {
            com.negociolisto.app.domain.model.InvoiceTemplateType.valueOf(templateString.uppercase())
        } catch (e: Exception) {
            com.negociolisto.app.domain.model.InvoiceTemplateType.CLASSIC
        }
    }
    
    /**
     * 📄 CREAR BACKUP JSON
     * 
     * Crea el contenido JSON del backup (método auxiliar)
     */
    private suspend fun createBackupJson(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val database = NegocioListoDatabase.getDatabase(context)
            
            val products = database.productDao().getAllProducts().first()
            val customers = database.customerDao().getAllCustomers().first()
            val sales = database.saleDao().getAllSales().first()
            val expenses = database.expenseDao().getAllExpenses().first()
            // ✅ Obtener userId para filtrar colecciones
            val userId = getCurrentUserId() ?: ""
            // Para las colecciones, necesitamos extraer solo las entidades base
            val collectionsWithItems = if (userId.isNotBlank()) {
                database.collectionDao().getCollections(userId).first()
            } else {
                emptyList()
            }
            val collections = collectionsWithItems.map { it.collection }
            val invoices = database.invoiceDao().getAllInvoices().first()
            // Obtener movimientos de stock
            val stockMovements = database.stockMovementDao().getAllMovements().first()
            
            // Obtener categorías personalizadas para el usuario actual
            val currentUserId = getCurrentUserId() ?: ""
            val customCategories = customCategoryRepository.getActiveCategoriesByUser(currentUserId).first()
            
            val backupJson = org.json.JSONObject().apply {
                put("version", "1.0")
                put("timestamp", System.currentTimeMillis())
                put("metadata", org.json.JSONObject().apply {
                    put("appVersion", "1.0")
                    put("backupType", "complete")
                    put("includesImages", true)
                })
                
                // Productos
                put("products", org.json.JSONArray().apply {
                    products.forEach { product: com.negociolisto.app.data.local.entity.ProductEntity ->
                        put(org.json.JSONObject().apply {
                            put("id", product.id)
                            put("name", product.name)
                            put("description", product.description ?: "")
                            put("sku", product.sku)
                            put("purchasePrice", product.purchasePrice)
                            put("salePrice", product.salePrice)
                            put("stockQuantity", product.stockQuantity)
                            put("minimumStock", product.minimumStock)
                            put("customCategoryId", product.customCategoryId)
                            put("supplier", product.supplier ?: "")
                            put("photoUrl", product.photoUrl ?: "")
                            put("isActive", product.isActive)
                            put("createdAt", product.createdAt)
                            put("updatedAt", product.updatedAt)
                        })
                    }
                })
                // Mapeo de fotos de productos (productId -> fileName)
                put("productPhotos", org.json.JSONArray().apply {
                    products.forEach { p ->
                        val path = p.photoUrl
                        if (!path.isNullOrBlank()) {
                            val fileName = java.io.File(path).name
                            put(org.json.JSONObject().apply {
                                put("productId", p.id)
                                put("fileName", fileName)
                            })
                        }
                    }
                })
                
                // Clientes
                put("customers", org.json.JSONArray().apply {
                    customers.forEach { customer: com.negociolisto.app.data.local.entity.CustomerEntity ->
                        put(org.json.JSONObject().apply {
                            put("id", customer.id)
                            put("name", customer.name)
                            put("companyName", customer.companyName ?: "")
                            put("email", customer.email ?: "")
                            put("phone", customer.phone ?: "")
                            put("address", customer.address ?: "")
                            put("totalPurchases", customer.totalPurchases)
                            put("lastPurchaseDate", customer.lastPurchaseDate?.toString() ?: "")
                            put("createdAt", customer.createdAt.toString())
                            put("notes", customer.notes ?: "")
                        })
                    }
                })
                
                // Ventas
                put("sales", org.json.JSONArray().apply {
                    sales.forEach { sale: com.negociolisto.app.data.local.entity.SaleEntity ->
                        put(org.json.JSONObject().apply {
                            put("id", sale.id)
                            put("customerId", sale.customerId ?: "")
                            put("items", sale.items)
                            put("total", sale.total)
                            put("date", sale.date.toString())
                            put("paymentMethod", sale.paymentMethod.toString())
                            put("note", sale.note ?: "")
                            put("status", sale.status.toString())
                            put("canceledAt", sale.canceledAt?.toString() ?: "")
                            put("canceledReason", sale.canceledReason ?: "")
                        })
                    }
                })
                
                // Gastos
                put("expenses", org.json.JSONArray().apply {
                    expenses.forEach { expense: com.negociolisto.app.data.local.entity.ExpenseEntity ->
                        put(org.json.JSONObject().apply {
                            put("id", expense.id)
                            put("description", expense.description)
                            put("amount", expense.amount)
                            put("category", expense.category.toString())
                            put("supplier", expense.supplier ?: "")
                            put("date", expense.date.toString())
                            put("receiptNumber", expense.receiptNumber ?: "")
                            put("notes", expense.notes ?: "")
                            put("status", expense.status.toString())
                        })
                    }
                })
                
                // Colecciones
                put("collections", org.json.JSONArray().apply {
                    collections.forEach { collection: com.negociolisto.app.data.local.entity.CollectionEntity ->
                        put(org.json.JSONObject().apply {
                            put("id", collection.id)
                            put("name", collection.name)
                            put("description", collection.description ?: "")
                            // Guardar associatedCustomerIds como arreglo JSON explícito
                            run {
                                val idsArray = org.json.JSONArray()
                                if (!collection.associatedCustomerIds.isNullOrBlank()) {
                                    collection.associatedCustomerIds.split(',').filter { it.isNotBlank() }.forEach { idStr ->
                                        idsArray.put(idStr)
                                    }
                                }
                                put("associatedCustomerIds", idsArray)
                            }
                            put("status", collection.status.toString())
                            put("color", collection.color ?: "")
                            // Escribir createdAt/updatedAt como epoch millis para round-trip robusto
                            put("createdAt", collection.createdAt)
                            put("updatedAt", collection.updatedAt)
                        })
                    }
                })
                
                // Items de colecciones
                put("collectionItems", org.json.JSONArray().apply {
                    collectionsWithItems.forEach { rel ->
                        rel.items.forEach { item ->
                            put(org.json.JSONObject().apply {
                                put("collectionId", item.collectionId)
                                put("productId", item.productId)
                                put("notes", item.notes ?: org.json.JSONObject.NULL)
                                put("displayOrder", item.displayOrder)
                                put("isFeatured", item.isFeatured)
                                put("specialPrice", item.specialPrice ?: org.json.JSONObject.NULL)
                            })
                        }
                    }
                })
                
                // Facturas
                put("invoices", org.json.JSONArray().apply {
                    invoices.forEach { invoice: com.negociolisto.app.data.local.entity.InvoiceEntity ->
                        put(org.json.JSONObject().apply {
                            put("id", invoice.id)
                            put("number", invoice.number)
                            put("saleId", invoice.saleId ?: "")
                            put("customerId", invoice.customerId ?: "")
                            put("items", org.json.JSONArray().apply {
                                invoice.items.forEach { invItem ->
                                    put(org.json.JSONObject().apply {
                                        put("description", invItem.description)
                                        put("quantity", invItem.quantity)
                                        put("unitPrice", invItem.unitPrice)
                                    })
                                }
                            })
                            put("subtotal", invoice.subtotal)
                            put("tax", invoice.tax)
                            put("total", invoice.total)
                            put("date", invoice.date.toString())
                            put("template", invoice.template.toString())
                            put("notes", invoice.notes ?: "")
                        })
                    }
                })

                // Movimientos de stock
                put("stockMovements", org.json.JSONArray().apply {
                    stockMovements.forEach { mv ->
                        put(org.json.JSONObject().apply {
                            put("id", mv.id)
                            put("productId", mv.productId)
                            put("movementType", mv.movementType)
                            put("quantity", mv.quantity)
                            put("reason", mv.reason)
                            put("description", mv.description ?: org.json.JSONObject.NULL)
                            put("referenceId", mv.referenceId ?: org.json.JSONObject.NULL)
                            put("unitCost", mv.unitCost ?: org.json.JSONObject.NULL)
                            put("previousStock", mv.previousStock)
                            put("newStock", mv.newStock)
                            put("userId", mv.userId ?: org.json.JSONObject.NULL)
                            put("timestamp", mv.timestamp)
                            put("notes", mv.notes ?: org.json.JSONObject.NULL)
                        })
                    }
                })
                
                // Categorías personalizadas
                put("customCategories", org.json.JSONArray().apply {
                    customCategories.forEach { category: com.negociolisto.app.domain.model.CustomCategory ->
                        put(org.json.JSONObject().apply {
                            put("id", category.id)
                            put("name", category.name)
                            put("icon", category.icon)
                            put("color", category.color)
                            put("description", category.description ?: "")
                            put("createdAt", category.createdAt.toString())
                            put("updatedAt", category.updatedAt.toString())
                            put("isActive", category.isActive)
                            put("userId", category.userId)
                            put("sortOrder", category.sortOrder)
                        })
                    }
                })
                
                // Información del usuario
                put("user", org.json.JSONObject().apply {
                    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                    put("id", getCurrentUserId() ?: "")
                    put("name", prefs.getString("user_name", "") ?: "")
                    put("email", prefs.getString("user_email", "") ?: "")
                    put("phone", prefs.getString("user_phone", null))
                    put("businessName", prefs.getString("business_name", null))
                    put("businessType", prefs.getString("business_type", null))
                    put("businessRut", prefs.getString("business_rut", null))
                    put("businessAddress", prefs.getString("business_address", null))
                    put("businessPhone", prefs.getString("business_phone", null))
                    put("businessEmail", prefs.getString("business_email", null))
                    put("businessLogoUrl", prefs.getString("business_logo_url", null))
                    
                    // Redes sociales del negocio
                    val socialMediaJson = prefs.getString("business_social_media", null)
                    if (socialMediaJson != null) {
                        put("businessSocialMedia", org.json.JSONObject(socialMediaJson))
                    } else {
                        put("businessSocialMedia", org.json.JSONObject.NULL)
                    }
                    
                    put("profilePhotoUrl", prefs.getString("profile_photo_url", null))
                    // Nombres de archivo (para re-mapear en restauración)
                    prefs.getString("profile_photo_url", null)?.let { path ->
                        val fileName = java.io.File(path).name
                        put("profilePhotoFileName", fileName)
                    }
                    prefs.getString("business_logo_url", null)?.let { path ->
                        val fileName = java.io.File(path).name
                        put("businessLogoFileName", fileName)
                    }
                    put("isEmailVerified", prefs.getBoolean("email_verified", false))
                    
                    // Información de login
                    val loginInfo = loginTrackingService.getLoginInfo()
                    put("lastLoginAt", loginInfo.lastLogin?.toEpochMilliseconds()?.toString() ?: org.json.JSONObject.NULL)
                    put("firstLoginAt", loginInfo.firstLogin?.toEpochMilliseconds()?.toString() ?: org.json.JSONObject.NULL)
                    put("loginCount", loginInfo.loginCount)
                    
                    put("createdAt", System.currentTimeMillis().toString())
                    put("updatedAt", System.currentTimeMillis().toString())
                })
                
                // Configuraciones de la aplicación
                put("settings", org.json.JSONObject().apply {
                    val uiPrefs = context.getSharedPreferences("ui_prefs_mirror", Context.MODE_PRIVATE)
                    val mainPrefs = context.getSharedPreferences("ui_preferences", Context.MODE_PRIVATE)
                    val scale = uiPrefs.getFloat("app_scale", mainPrefs.getFloat("app_scale", 1.0f))
                    put("app_scale", scale)
                    
                    // Configuración de temas
                    val themeConfig = themeManager.getThemeConfig()
                    put("theme", org.json.JSONObject().apply {
                        put("current_theme", themeConfig.theme)
                        put("primary_color", themeConfig.primaryColor)
                        put("secondary_color", themeConfig.secondaryColor)
                        put("is_dark", themeConfig.isDark)
                        put("use_system_theme", themeConfig.useSystemTheme)
                    })
                })
            }
            
            Result.success(backupJson.toString(2))
        } catch (e: Exception) {
            println("❌ DEBUG: Error creando JSON de backup: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * 🖼️ CREAR BACKUP CON IMÁGENES
     * 
     * Crea un archivo ZIP que incluye tanto los datos JSON como las imágenes
     */
    suspend fun createBackupWithImages(
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando backup con imágenes...")
            
            // 1. Crear backup de datos JSON
            onProgress(20, "Generando datos JSON...")
            val jsonContent = createBackupJson()
            if (jsonContent.isFailure) {
                return@withContext Result.failure(jsonContent.exceptionOrNull() ?: Exception("Error generando JSON"))
            }
            
            // 1.5. Descargar imágenes desde Firebase Storage
            onProgress(30, "Descargando imágenes desde la nube...")
            val downloadedImages = downloadImagesFromFirebase(imageService, onProgress)
            println("📸 DEBUG: ${downloadedImages.size} imágenes descargadas desde Firebase Storage")
            
            // 2. Buscar imágenes en cacheDir y en directorios específicos
            onProgress(40, "Buscando imágenes locales...")
            val imageFiles = findImageFiles()
            val profileImages = findProfileImages()
            val businessImages = findBusinessImages()
            val allImages = imageFiles + profileImages + businessImages + downloadedImages
            println("📸 DEBUG: Encontradas ${allImages.size} imágenes para respaldar (${imageFiles.size} inventario, ${profileImages.size} perfil, ${businessImages.size} empresa, ${downloadedImages.size} desde nube)")
            
            // 3. Crear archivo ZIP
            onProgress(60, "Creando archivo ZIP...")
            val backupFile = File(context.getExternalFilesDir(null), "backup_completo_${System.currentTimeMillis()}.zip")
            
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
                // Agregar archivo JSON
                val jsonEntry = ZipEntry("backup_data.json")
                zipOut.putNextEntry(jsonEntry)
                zipOut.write(jsonContent.getOrNull()?.toByteArray())
                zipOut.closeEntry()
                
                // Agregar imágenes por categoría
                var imageCount = 0
                
                // Imágenes de inventario
                imageFiles.forEach { imageFile ->
                    if (imageFile.exists()) {
                        val entryName = "images/inventory/${imageFile.name}"
                        val imageEntry = ZipEntry(entryName)
                        zipOut.putNextEntry(imageEntry)
                        
                        FileInputStream(imageFile).use { fileIn ->
                            fileIn.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                        imageCount++
                    }
                }
                
                // Imágenes de perfil de usuario
                profileImages.forEach { imageFile ->
                    if (imageFile.exists()) {
                        val entryName = "images/profile/${imageFile.name}"
                        val imageEntry = ZipEntry(entryName)
                        zipOut.putNextEntry(imageEntry)
                        
                        FileInputStream(imageFile).use { fileIn ->
                            fileIn.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                        imageCount++
                    }
                }
                
                // Imágenes de empresa
                businessImages.forEach { imageFile ->
                    if (imageFile.exists()) {
                        val entryName = "images/business/${imageFile.name}"
                        val imageEntry = ZipEntry(entryName)
                        zipOut.putNextEntry(imageEntry)
                        
                        FileInputStream(imageFile).use { fileIn ->
                            fileIn.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                        imageCount++
                    }
                }
                
                println("✅ DEBUG: Backup creado con $imageCount imágenes")
            }
            
            onProgress(100, "Backup con imágenes completado")
            // Guardar última fecha de backup
            context.getSharedPreferences("backup_data", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_backup_time", System.currentTimeMillis())
                .apply()
            Result.success(backupFile)
            
        } catch (e: Exception) {
            println("❌ DEBUG: Error creando backup con imágenes: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * 🖼️ RESTAURAR BACKUP CON IMÁGENES
     * 
     * Restaura tanto datos JSON como imágenes desde un archivo ZIP
     */
    suspend fun restoreBackupWithImages(
        backupFile: File,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Iniciando restauración con imágenes...")
            
            if (!backupFile.exists()) {
                return@withContext Result.failure(Exception("Archivo de backup no encontrado"))
            }
            
            var jsonContent: String? = null
            var restoredImages = 0
            
            // 1. Extraer archivos del ZIP
            onProgress(20, "Extrayendo archivos...")
            ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                
                while (entry != null) {
                    when {
                        entry.name == "backup_data.json" -> {
                            // Extraer JSON
                            jsonContent = zipIn.readBytes().toString(Charsets.UTF_8)
                            onProgress(40, "Datos JSON extraídos")
                        }
                        entry.name.startsWith("images/inventory/") -> {
                            // Extraer imagen de inventario
                            val imageName = entry.name.substringAfter("images/inventory/")
                            val inventoryDir = File(context.getExternalFilesDir(null), "images/inventory")
                            inventoryDir.mkdirs() // Crear directorio si no existe
                            val imageFile = File(inventoryDir, imageName)
                            
                            // Asegurar que el directorio padre existe
                            imageFile.parentFile?.mkdirs()
                            
                            FileOutputStream(imageFile).use { fileOut ->
                                zipIn.copyTo(fileOut)
                            }
                            restoredImages++
                            println("📸 DEBUG: Imagen de inventario restaurada: $imageName")
                        }
                        entry.name.startsWith("images/profile/") -> {
                            // Extraer imagen de perfil
                            val imageName = entry.name.substringAfter("images/profile/")
                            val imageFile = File(context.cacheDir, imageName)
                            
                            // Asegurar que el directorio padre existe
                            imageFile.parentFile?.mkdirs()
                            
                            FileOutputStream(imageFile).use { fileOut ->
                                zipIn.copyTo(fileOut)
                            }
                            restoredImages++
                            println("👤 DEBUG: Imagen de perfil restaurada: $imageName")
                            
                            // Actualizar la URL de la imagen de perfil en SharedPreferences
                            val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("profile_photo_url", imageFile.absolutePath)
                                .apply()
                            println("👤 DEBUG: URL de imagen de perfil actualizada: ${imageFile.absolutePath}")
                        }
                        entry.name.startsWith("images/business/") -> {
                            // Extraer imagen de empresa
                            val imageName = entry.name.substringAfter("images/business/")
                            val imageFile = File(context.cacheDir, imageName)
                            
                            // Asegurar que el directorio padre existe
                            imageFile.parentFile?.mkdirs()
                            
                            FileOutputStream(imageFile).use { fileOut ->
                                zipIn.copyTo(fileOut)
                            }
                            restoredImages++
                            println("🏢 DEBUG: Imagen de empresa restaurada: $imageName")
                            
                            // Actualizar la URL de la imagen de empresa en SharedPreferences
                            val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("business_logo_url", imageFile.absolutePath)
                                .apply()
                            println("🏢 DEBUG: URL de imagen de empresa actualizada: ${imageFile.absolutePath}")
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
            
            // 2. Restaurar datos JSON
            val jsonContentFinal = jsonContent
            if (jsonContentFinal != null) {
                onProgress(60, "Restaurando datos...")
                val restoreResult = restoreFromBackup(jsonContentFinal) { progress, status ->
                    onProgress(60 + (progress * 0.3).toInt(), status)
                }
                
                if (restoreResult.isFailure) {
                    return@withContext Result.failure(restoreResult.exceptionOrNull() ?: Exception("Error restaurando datos"))
                }

                // Reasignar photoUrl de productos en base a productPhotos
                runCatching {
                    val json = org.json.JSONObject(jsonContentFinal)
                    val productPhotos = json.optJSONArray("productPhotos")
                    if (productPhotos != null) {
                        val database = NegocioListoDatabase.getDatabase(context)
                        for (i in 0 until productPhotos.length()) {
                            val obj = productPhotos.getJSONObject(i)
                            val productId = obj.optString("productId", "")
                            val fileName = obj.optString("fileName", "")
                            if (productId.isNotBlank() && fileName.isNotBlank()) {
                                val imageFile = java.io.File(java.io.File(context.getExternalFilesDir(null), "images/inventory"), fileName)
                                if (imageFile.exists()) {
                                    val product = database.productDao().getProductById(productId)
                                    if (product != null) {
                                        val updated = product.copy(photoUrl = imageFile.absolutePath)
                                        database.productDao().updateProduct(updated)
                                    }
                                }
                            }
                        }
                    }
                }

                // Reasignar imágenes de perfil y logo a rutas persistentes si existen
                runCatching {
                    val json = org.json.JSONObject(jsonContentFinal)
                    val userObj = json.optJSONObject("user")
                    if (userObj != null) {
                        val profileName = userObj.optString("profilePhotoFileName", "")
                        val logoName = userObj.optString("businessLogoFileName", "")
                        val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                        val editor = prefs.edit()
                        if (profileName.isNotBlank()) {
                            val f = java.io.File(java.io.File(context.getExternalFilesDir(null), "images/profile"), profileName)
                            if (f.exists()) editor.putString("profile_photo_url", f.absolutePath)
                        }
                        if (logoName.isNotBlank()) {
                            val f = java.io.File(java.io.File(context.getExternalFilesDir(null), "images/business"), logoName)
                            if (f.exists()) editor.putString("business_logo_url", f.absolutePath)
                        }
                        editor.apply()
                    }
                }
            }
            
            onProgress(100, "Restauración completada con $restoredImages imágenes")
            Result.success("✅ Restauración completada: datos + $restoredImages imágenes")
            
        } catch (e: Exception) {
            println("❌ DEBUG: Error restaurando backup con imágenes: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * ☁️ DESCARGAR IMÁGENES DESDE FIREBASE STORAGE
     * 
     * Descarga las imágenes de productos que están en Firebase Storage
     * para incluirlas en el backup ZIP
     */
    private suspend fun downloadImagesFromFirebase(
        @Suppress("UNUSED_PARAMETER") imageService: ImageService,
        onProgress: (Int, String) -> Unit
    ): List<File> = withContext(Dispatchers.IO) {
        val downloadedFiles = mutableListOf<File>()
        try {
            val database = NegocioListoDatabase.getDatabase(context)
            val productsFlow = database.productDao().getAllProducts()
            val products = productsFlow.first() // Obtener la lista del Flow
            
            var downloadCount = 0
            val totalProducts = products.size
            
            for (product in products) {
                val photoUrl = product.photoUrl
                
                // Verificar si es una URL de Firebase Storage
                if (!photoUrl.isNullOrBlank() && 
                    (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) &&
                    photoUrl.contains("firebasestorage.googleapis.com")) {
                    try {
                        val fileName = product.id + ".jpg"
                        val inventoryDir = File(context.getExternalFilesDir(null), "images/inventory")
                        inventoryDir.mkdirs()
                        
                        val localFile = File(inventoryDir, fileName)
                        
                        // Extraer la ruta del storage desde la URL
                        // Formato: https://firebasestorage.googleapis.com/v0/b/[PROJECT]/o/[PATH]?alt=media
                        val urlParts = photoUrl.split("/o/")
                        if (urlParts.size == 2) {
                            val pathPart = urlParts[1].split("?")[0]
                            val decodedPath = java.net.URLDecoder.decode(pathPart, "UTF-8")
                            
                            // Descargar imagen desde Firebase Storage
                            val storage = FirebaseStorage.getInstance()
                            val storageRef = storage.reference.child(decodedPath)
                            
                            storageRef.getFile(localFile).await()
                            downloadedFiles.add(localFile)
                            downloadCount++
                            
                            println("✅ Imagen descargada: ${product.name} -> $fileName")
                            
                            val progress = 30 + (downloadCount * 20.0 / totalProducts).toInt()
                            onProgress(progress, "Descargadas $downloadCount/$totalProducts imágenes")
                        }
                    } catch (e: Exception) {
                        println("⚠️ No se pudo descargar imagen de ${product.name}: ${e.message}")
                    }
                }
            }
            
            println("✅ Se descargaron $downloadCount imágenes desde Firebase Storage")
        } catch (e: Exception) {
            println("❌ Error descargando imágenes: ${e.message}")
        }
        
        downloadedFiles
    }
    
    /**
     * 🔍 BUSCAR ARCHIVOS DE IMAGEN DE INVENTARIO
     * 
     * Busca todos los archivos de imagen en múltiples ubicaciones
     */
    private fun findImageFiles(): List<File> {
        val imageFiles = mutableListOf<File>()
        
        // 1. Buscar en cacheDir (imágenes temporales)
        val cacheDir = context.cacheDir
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && isImageFile(file)) {
                if (!file.name.contains("profile") && !file.name.contains("company_logo") && !file.name.contains("business")) {
                    imageFiles.add(file)
                }
            }
        }
        
        // 2. Buscar en getExternalFilesDir/Pictures (imágenes de productos)
        val picturesDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        picturesDir?.let { dir ->
            if (dir.exists()) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && isImageFile(file)) {
                        if (!file.name.contains("profile") && !file.name.contains("company_logo") && !file.name.contains("business")) {
                            imageFiles.add(file)
                        }
                    }
                }
            }
        }
        
        // 3. Buscar en directorio de inventario específico
        val inventoryDir = File(context.getExternalFilesDir(null), "images/inventory")
        if (inventoryDir.exists()) {
            inventoryDir.listFiles()?.forEach { file ->
                if (file.isFile && isImageFile(file)) {
                    imageFiles.add(file)
                }
            }
        }
        
        // 4. Buscar imágenes referenciadas por productos en la base de datos
        // Nota: Esta parte se maneja en la función de backup principal donde ya tenemos acceso a los productos
        
        return imageFiles.distinctBy { it.absolutePath }
    }
    
    /**
     * 👤 BUSCAR IMÁGENES DE PERFIL DE USUARIO
     */
    private fun findProfileImages(): List<File> {
        val cacheDir = context.cacheDir
        val imageFiles = mutableListOf<File>()
        
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && isImageFile(file) && file.name.contains("profile")) {
                imageFiles.add(file)
            }
        }
        
        return imageFiles
    }
    
    /**
     * 🏢 BUSCAR IMÁGENES DE EMPRESA
     */
    private fun findBusinessImages(): List<File> {
        val cacheDir = context.cacheDir
        val imageFiles = mutableListOf<File>()
        
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && isImageFile(file) && (file.name.contains("company_logo") || file.name.contains("business"))) {
                imageFiles.add(file)
            }
        }
        
        return imageFiles
    }
    
    /**
     * 🖼️ VERIFICAR SI ES ARCHIVO DE IMAGEN
     */
    private fun isImageFile(file: File): Boolean {
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        val extension = file.extension.lowercase()
        return imageExtensions.contains(extension)
    }
    
    /**
     * 🔍 DIAGNÓSTICO DE IMÁGENES
     * 
     * Analiza qué productos tienen imágenes y cuáles no
     */
    suspend fun diagnoseProductImages(): Map<String, Any> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<String, Any>()
        
        try {
            val database = NegocioListoDatabase.getDatabase(context)
            val productsFlow = database.productDao().getAllProducts()
            val products = productsFlow.first() // Obtener lista del Flow
            
            val productsWithImages = mutableListOf<String>()
            val productsWithoutImages = mutableListOf<String>()
            val productsWithFirebaseImages = mutableListOf<String>()
            val productsWithLocalImages = mutableListOf<String>()
            
            for (product in products) {
                when {
                    product.photoUrl.isNullOrBlank() -> {
                        productsWithoutImages.add("${product.name} (ID: ${product.id})")
                    }
                    product.photoUrl.startsWith("http://") || product.photoUrl.startsWith("https://") -> {
                        productsWithImages.add("${product.name} (ID: ${product.id})")
                        productsWithFirebaseImages.add("${product.name} (ID: ${product.id})")
                    }
                    else -> {
                        productsWithImages.add("${product.name} (ID: ${product.id})")
                        productsWithLocalImages.add("${product.name} (ID: ${product.id})")
                    }
                }
            }
            
            result["totalProducts"] = products.size
            result["withImages"] = productsWithImages.size
            result["withoutImages"] = productsWithoutImages.size
            result["withFirebaseImages"] = productsWithFirebaseImages.size
            result["withLocalImages"] = productsWithLocalImages.size
            result["productsWithoutImages"] = productsWithoutImages
            result["productsWithFirebaseImages"] = productsWithFirebaseImages
            result["productsWithLocalImages"] = productsWithLocalImages
            
        } catch (e: Exception) {
            result["error"] = e.message ?: "Error desconocido"
        }
        
        result
    }
    
    /**
     * 🔄 RESINCRONIZAR IMÁGENES DESDE FIREBASE STORAGE
     * 
     * Descarga las imágenes que están en Firebase Storage
     * y las almacena localmente para que aparezcan en el inventario
     */
    suspend fun resyncImagesFromFirebase(
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Iniciando resincronización de imágenes...")
            
            val database = NegocioListoDatabase.getDatabase(context)
            val productsFlow = database.productDao().getAllProducts()
            val products = productsFlow.first() // Obtener lista del Flow
            
            var successCount = 0
            var failCount = 0
            val totalProducts = products.size
            
            for ((index, product) in products.withIndex()) {
                val photoUrl = product.photoUrl
                
                // Solo procesar URLs de Firebase Storage
                if (!photoUrl.isNullOrBlank() && 
                    (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) &&
                    photoUrl.contains("firebasestorage.googleapis.com")) {
                    try {
                        val fileName = "${product.id}.jpg"
                        val inventoryDir = File(context.getExternalFilesDir(null), "images/inventory")
                        inventoryDir.mkdirs()
                        
                        val localFile = File(inventoryDir, fileName)
                        
                        // Extraer la ruta del storage desde la URL
                        val urlParts = photoUrl.split("/o/")
                        if (urlParts.size == 2) {
                            val pathPart = urlParts[1].split("?")[0]
                            val decodedPath = java.net.URLDecoder.decode(pathPart, "UTF-8")
                            
                            // Descargar imagen desde Firebase Storage
                            val storage = FirebaseStorage.getInstance()
                            val storageRef = storage.reference.child(decodedPath)
                            
                            storageRef.getFile(localFile).await()
                            
                            // Actualizar producto con ruta local
                            val updatedProduct = product.copy(photoUrl = localFile.absolutePath)
                            database.productDao().updateProduct(updatedProduct)
                            
                            successCount++
                            println("✅ Imagen resincronizada: ${product.name}")
                        }
                    } catch (e: Exception) {
                        failCount++
                        println("⚠️ No se pudo resincronizar imagen de ${product.name}: ${e.message}")
                    }
                }
                
                val progress = if (totalProducts > 0) ((index + 1) * 100 / totalProducts) else 0
                onProgress(progress, "Procesando ${product.name}...")
            }
            
            val message = "✅ Resincronización completada: $successCount exitosas, $failCount fallidas"
            onProgress(100, message)
            Result.success(message)
            
        } catch (e: Exception) {
            val error = "Error en resincronización: ${e.message}"
            onProgress(100, error)
            Result.failure(Exception(error))
        }
    }
    
}


/**
 * 📊 DATOS DE BACKUP PARSEADOS
 */
data class BackupData(
    val products: List<com.negociolisto.app.data.local.entity.ProductEntity> = emptyList(),
    val customers: List<com.negociolisto.app.data.local.entity.CustomerEntity> = emptyList(),
    val sales: List<com.negociolisto.app.data.local.entity.SaleEntity> = emptyList(),
    val expenses: List<com.negociolisto.app.data.local.entity.ExpenseEntity> = emptyList(),
    val collections: List<com.negociolisto.app.data.local.entity.CollectionEntity> = emptyList(),
    val invoices: List<com.negociolisto.app.data.local.entity.InvoiceEntity> = emptyList(),
    val customCategories: List<com.negociolisto.app.domain.model.CustomCategory> = emptyList(),
    val user: com.negociolisto.app.domain.model.User? = null,
    val settings: Map<String, String> = emptyMap(),
    val metadata: Map<String, String> = emptyMap(),
    val collectionItems: List<com.negociolisto.app.data.local.entity.CollectionItemEntity> = emptyList(),
    val stockMovements: List<com.negociolisto.app.data.local.entity.StockMovementEntity> = emptyList()
)

/**
 * 📊 ESTADO DEL BACKUP
 */
data class BackupStatus(
    val hasLocalData: Boolean,
    val hasCloudData: Boolean,
    val lastBackupTime: Long?,
    val dataCount: DataCount
)


/**
 * 📊 CONTEO DE DATOS
 */
data class DataCount(
    val products: Int,
    val customers: Int,
    val sales: Int,
    val expenses: Int
)