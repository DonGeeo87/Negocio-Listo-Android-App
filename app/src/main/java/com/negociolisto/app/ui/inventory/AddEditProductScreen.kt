package com.negociolisto.app.ui.inventory

import com.negociolisto.app.ui.components.UnifiedCard

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.negociolisto.app.ui.components.AnimatedButton
import android.content.Context
import com.negociolisto.app.ui.components.SuccessAnimation
import com.negociolisto.app.ui.components.SimpleBarcodeScanner
import com.negociolisto.app.ui.components.OptimizedProductImage
import coil.compose.AsyncImage
import com.negociolisto.app.ui.design.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.provider.MediaStore
import android.content.ContentValues
import android.os.Build
import java.io.File
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.datetime.toLocalDateTime
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.CustomCategory
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import com.negociolisto.app.ui.components.ModernDropdown
import com.negociolisto.app.ui.components.Formatters
import com.negociolisto.app.ui.components.FixedBottomBar
import com.negociolisto.app.ui.settings.ImageBackupViewModel
import com.negociolisto.app.ui.settings.ImageBackupState
import com.negociolisto.app.ui.inventory.components.CustomCategoryCarousel

/**
 * 📸 FUNCIÓN PARA RESPALDO AUTOMÁTICO DE IMÁGENES
 * 
 * Maneja el respaldo automático de imágenes de productos a Google Drive
 * cuando el usuario guarda un producto con imagen.
 */
suspend fun saveProductWithImageBackup(
    product: Product,
    imageUri: Uri?,
    imageBackupVm: ImageBackupViewModel,
    vm: InventoryViewModel,
    isNewProduct: Boolean,
    context: Context
): Result<Product> {
    return try {
        var finalProduct = product
        
        // Si hay imagen, usar las nuevas funciones de compresión automática
        if (imageUri != null) {
            if (isNewProduct) {
                // Usar nueva función con compresión automática
                vm.addProductWithImage(context, product, imageUri)
            } else {
                // Usar nueva función con compresión automática
                vm.updateProductWithImage(context, product, imageUri)
            }
            
            // También hacer respaldo a Google Drive si está conectado
            if (imageBackupVm.state.value.isConnected) {
                val backupResult = imageBackupVm.uploadProductImage(
                    imageUri = imageUri,
                    productId = product.id,
                    productName = product.name
                )
                
                if (backupResult.isSuccess) {
                    val backupUrl = backupResult.getOrNull()
                    // Actualizar el producto con la URL de respaldo
                    finalProduct = product.copy(imageBackupUrl = backupUrl)
                    android.util.Log.d("NegocioListo", "✅ Imagen respaldada exitosamente: $backupUrl")
                } else {
                    android.util.Log.w("NegocioListo", "⚠️ Error en respaldo de imagen: ${backupResult.exceptionOrNull()?.message}")
                    // Continuar sin respaldo - no es crítico
                }
            }
        } else {
            // Si no hay imagen, guardar normalmente
            if (isNewProduct) {
                vm.addProduct(finalProduct)
            } else {
                vm.updateProduct(finalProduct)
            }
        }
        
        Result.success(finalProduct)
    } catch (e: Exception) {
        android.util.Log.e("NegocioListo", "❌ Error al guardar producto: ${e.message}")
        Result.failure(e)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    productId: String? = null,
    onNavigateToCategoryManagement: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var selectedCustomCategoryId by remember { mutableStateOf("") }
    var selectedCustomCategory by remember { mutableStateOf<com.negociolisto.app.domain.model.CustomCategory?>(null) }
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val vm: InventoryViewModel = hiltViewModel()
    val imageBackupVm: ImageBackupViewModel = hiltViewModel()
    val customCategories by vm.customCategories.collectAsStateWithLifecycle()
    @Suppress("UNUSED_VARIABLE")
    val imageBackupState by imageBackupVm.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "fadeIn"
    )

    // Cargar datos si es edición
    var existing by remember { mutableStateOf<com.negociolisto.app.domain.model.Product?>(null) }
    LaunchedEffect(productId) {
        productId?.let { id ->
            launch {
                vm.getProduct(id)?.let { p ->
                    existing = p
                    name = p.name
                    price = Formatters.formatClpNumber(p.salePrice)
                    cost = Formatters.formatClpNumber(p.purchasePrice)
                    stock = p.stockQuantity.toString()
                    photoUrl = p.photoUrl ?: ""
                    // Cargar la categoría personalizada del producto si existe
                    selectedCustomCategoryId = p.customCategoryId
                    selectedCustomCategory = customCategories.find { it.id == p.customCategoryId }
                    
                    // DEBUG: Log para verificar la carga
                    android.util.Log.d("NegocioListo", "Cargando producto:")
                    android.util.Log.d("NegocioListo", "  - name: ${p.name}")
                    android.util.Log.d("NegocioListo", "  - customCategoryId: ${p.customCategoryId}")
                    android.util.Log.d("NegocioListo", "  - selectedCustomCategory: ${selectedCustomCategory?.name}")
                }
            }
        }
    }

    // Lanzadores para galería y cámara
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                // Copiar imagen al caché para backup/restore
                val fileName = "product_${System.currentTimeMillis()}.jpg"
                // Copiar imagen de forma síncrona para evitar problemas de contexto
                try {
                    val imageFile = File(context.cacheDir, fileName)
                    context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                        imageFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    photoUrl = imageFile.absolutePath
                    println("📸 DEBUG: Imagen de producto guardada en caché: $photoUrl")
                } catch (e: Exception) {
                    println("❌ DEBUG: Error guardando imagen en caché: ${e.message}")
                    photoUrl = selectedUri.toString()
                }
            } catch (e: Exception) {
                println("❌ DEBUG: Error con permisos de URI: ${e.message}")
                photoUrl = selectedUri.toString()
            }
        } ?: run {
            photoUrl = ""
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                // La imagen ya está guardada en el URI de la cámara
                photoUrl = uri.toString()
                println("📸 DEBUG: Imagen de cámara guardada: $photoUrl")
            }
        }
    }

    // Permisos para cámara
    var pendingCameraAction by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val readPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        val readGranted = permissions[readPerm] == true || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        if (cameraGranted && readGranted && pendingCameraAction) {
            val uri = createImageUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
        pendingCameraAction = false
    }

    Box(modifier = modifier.fillMaxSize()) {
    Column(
            modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
            .offset(y = slideInOffset)
            .alpha(fadeInAlpha)
    ) {
        // Contenido principal con padding
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de imagen (moved to top)
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding)
                ) {
                    Text(
                        text = "📸 Imagen del Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Previsualización de imagen cuadrada con esquinas redondeadas (60% del ancho)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(1f)
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OptimizedProductImage(
                                imageUrl = photoUrl.ifBlank { null },
                                productName = name.ifBlank { "Producto" },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                categoryIcon = "📦"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UnifiedOutlinedButton(
                            text = "🖼️ Galería",
                            onClick = {
                                galleryLauncher.launch(arrayOf("image/*"))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        UnifiedOutlinedButton(
                            text = "📷 Cámara",
                            onClick = {
                                val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                val readPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
                                val readGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true else ContextCompat.checkSelfPermission(context, readPerm) == PackageManager.PERMISSION_GRANTED
                                if (cameraGranted && readGranted) {
                                    val uri = createImageUri(context)
                                    cameraUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    pendingCameraAction = true
                                    val perms = mutableListOf(Manifest.permission.CAMERA)
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) perms.add(readPerm)
                                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) perms.add(readPerm)
                                    permissionLauncher.launch(perms.toTypedArray())
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Sección de información básica
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding)
                ) {
                        Text(
                            text = "📝 Información del Producto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        UnifiedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Nombre del producto",
                            placeholder = "Ej: Laptop Dell Inspiron"
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Selector de categoría con carrusel (igual que en gastos)
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📂 Categoría del Producto",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(
                                    onClick = { 
                                        // Navegar a la gestión de categorías unificada
                                        onNavigateToCategoryManagement()
                                    }
                                ) {
                                    Text("Gestionar")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (customCategories.isEmpty()) {
                                // Sin categorías - mostrar mensaje
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "No hay categorías configuradas. Ve a Configuración > Gestión de Categorías para crear categorías.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            } else {
                                // Con categorías - mostrar carrusel
                                CustomCategoryCarousel(
                                    categories = customCategories.filter { it.isActive },
                                    selectedCategory = selectedCustomCategory,
                                    onCategorySelected = { category ->
                                        selectedCustomCategory = category
                                        selectedCustomCategoryId = category?.id ?: ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        
                        // Indicador de SKU generado o existente
                        if (name.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = if (existing != null) "SKU:" else "SKU generado:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = existing?.sku ?: generateSmartSKU(name, selectedCustomCategory?.name),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Sección de precios y stock
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.cardPadding)
                    ) {
                        Text(
                            text = "💰 Precios y Stock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            UnifiedTextField(
                                value = price,
                                onValueChange = { newValue ->
                                    // Formatear automáticamente mientras el usuario escribe
                                    price = Formatters.formatInputValue(newValue)
                                },
                                label = "Precio venta (CLP)",
                                placeholder = "0",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            UnifiedTextField(
                                value = stock,
                                onValueChange = { stock = it },
                                label = "Stock",
                                placeholder = "0",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Campo de costo
                        UnifiedTextField(
                            value = cost,
                            onValueChange = { newValue ->
                                // Formatear automáticamente mientras el usuario escribe
                                cost = Formatters.formatInputValue(newValue)
                            },
                            label = "Costo (CLP)",
                            placeholder = "0",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Cálculo de margen automático
                        val cleanedPrice = Formatters.cleanFormattedValue(price)
                        val cleanedCost = Formatters.cleanFormattedValue(cost)
                        val salePriceValue = cleanedPrice.toDoubleOrNull() ?: 0.0
                        val costValue = cleanedCost.toDoubleOrNull() ?: 0.0
                        val profit = salePriceValue - costValue
                        val marginPercentage = if (costValue > 0) (profit / costValue) * 100 else 0.0
                        
                        if (salePriceValue > 0 && costValue > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ganancia:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatClpWithSymbol(profit),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (profit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Margen:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%.1f", marginPercentage)}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        marginPercentage >= 50 -> MaterialTheme.colorScheme.primary
                                        marginPercentage >= 25 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }
                    }
                }

                // Espaciado para el bottom bar
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botón de eliminar (solo si está editando)
                if (productId != null) {
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    UnifiedOutlinedButton(
                        text = "🗑️ Eliminar Producto",
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Eliminar producto") },
                            text = { Text("Esta acción eliminará el producto de forma permanente. ¿Deseas continuar?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteDialog = false
                                    vm.deleteProduct(productId)
                                    onDone()
                                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                            }
                        )
                    }
                }
            }
        }
        
        // Barra inferior fija
        var showSuccess by remember { mutableStateOf(false) }
        FixedBottomBar(
            primaryButtonText = if (productId == null) "💾 Guardar Producto" else "✏️ Actualizar Producto",
            primaryButtonOnClick = {
                            // Validar campos obligatorios
                            val validationErrors = mutableListOf<String>()
                            
                            if (name.isBlank()) {
                                validationErrors.add("El nombre del producto es obligatorio")
                            }
                            
                            // Limpiar formato antes de convertir a número
                            val cleanedPrice = Formatters.cleanFormattedValue(price)
                            val salePriceValue = cleanedPrice.toDoubleOrNull()
                            if (price.isBlank()) {
                                validationErrors.add("El precio de venta es obligatorio")
                            } else if (salePriceValue != null && salePriceValue <= 0) {
                                validationErrors.add("El precio de venta debe ser mayor a 0")
                            }
                            
                            // Mostrar errores si existen
                            if (validationErrors.isNotEmpty()) {
                                android.widget.Toast.makeText(
                                    context,
                                    validationErrors.joinToString("\n"),
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                return@FixedBottomBar
                            }
                            
                            println("🚨 GUARDANDO PRODUCTO:")
                            println("🚨   - name: $name")
                            println("🚨   - selectedCustomCategory: ${selectedCustomCategory?.name}")
                            println("🚨   - selectedCustomCategory: ${selectedCustomCategory?.name}")
                            println("🚨   - customCategoryName: ${selectedCustomCategory?.name}")
                            
                            val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                            val product = if (existing == null) {
                                com.negociolisto.app.domain.model.Product(
                                    id = java.util.UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    description = null,
                                    sku = generateSmartSKU(name, selectedCustomCategory?.name),
                                    purchasePrice = Formatters.cleanFormattedValue(cost).toDoubleOrNull() ?: 0.0,
                                    salePrice = salePriceValue ?: 0.0,
                                    stockQuantity = stock.toIntOrNull() ?: 0,
                                    minimumStock = 1,
                                    customCategoryId = selectedCustomCategory?.id ?: "",
                                    supplier = null,
                                    photoUrl = photoUrl.ifBlank { null },
                                    createdAt = now,
                                    updatedAt = now,
                                    isActive = true
                                )
                            } else {
                                existing!!.copy(
                                    name = name.trim(),
                                    sku = existing!!.sku, // Mantener el SKU original, no regenerar
                                    purchasePrice = Formatters.cleanFormattedValue(cost).toDoubleOrNull() ?: existing!!.purchasePrice,
                                    salePrice = salePriceValue ?: existing!!.salePrice,
                                    stockQuantity = stock.toIntOrNull() ?: existing!!.stockQuantity,
                                    // category removido - usando customCategoryId
                                    customCategoryId = selectedCustomCategory?.id ?: existing!!.customCategoryId,
                                    photoUrl = photoUrl.ifBlank { existing!!.photoUrl },
                                    updatedAt = now
                                )
                            }
                            
                            // Validar usando el método validate() del modelo
                            val modelValidationErrors = product.validate()
                            if (modelValidationErrors.isNotEmpty()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Errores de validación:\n${modelValidationErrors.joinToString("\n")}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                return@FixedBottomBar
                            }
                            
                            println("🚨 PRODUCTO CREADO PARA GUARDAR:")
                            println("🚨   - product.customCategoryId: ${product.customCategoryId}")
                            
                            // Usar la función con respaldo automático
                            coroutineScope.launch {
                                val result = saveProductWithImageBackup(
                                    product = product,
                                    imageUri = cameraUri,
                                    imageBackupVm = imageBackupVm,
                                    vm = vm,
                                    isNewProduct = existing == null,
                                    context = context
                                )
                                
                                if (result.isSuccess) {
                                    android.util.Log.d("NegocioListo", "✅ Producto guardado exitosamente")
                                    showSuccess = true
                                } else {
                                    android.util.Log.e("NegocioListo", "❌ Error al guardar producto: ${result.exceptionOrNull()?.message}")
                                    // TODO: Mostrar error al usuario
                                }
                            }
                        },
            modifier = Modifier.align(Alignment.BottomCenter)
                    )
                    
        // Success animation
                    SuccessAnimation(
                        visible = showSuccess,
                        onAnimationComplete = { onDone() }
                    )
        }
    }

fun createImageUri(context: android.content.Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_$timeStamp.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NegocioListo")
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
    } else {
        val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir
        val imageFile = File(imagesDir, "IMG_$timeStamp.jpg")
        FileProvider.getUriForFile(context, "${'$'}{context.packageName}.fileprovider", imageFile)
    }
}

/**
 * Genera un SKU inteligente basado en el nombre del producto y categoría
 * Se adapta tanto a categorías predefinidas como personalizadas
 */
fun generateSmartSKU(productName: String, customCategoryName: String? = null): String {
    val words = productName.trim().split(" ").filter { it.isNotBlank() }
    val initials = words.take(2).map { it.first().uppercaseChar() }.joinToString("")
    
    // Si hay categoría personalizada, generar código basado en su nombre
    val categoryCode = if (customCategoryName?.isNotBlank() == true) {
        generateCategoryCodeFromName(customCategoryName)
    } else {
        // Código por defecto
        "GEN"
    }
    
    val timestamp = System.currentTimeMillis().toString().takeLast(4)
    return "${initials}${categoryCode}${timestamp}"
}

/**
 * Genera un código de categoría inteligente basado en el nombre de categoría personalizada
 * Ejemplos: "Electrodomésticos" -> "ELE", "Ropa de Verano" -> "ROP", "Herramientas" -> "HER"
 */
fun generateCategoryCodeFromName(categoryName: String): String {
    val cleanName = categoryName.trim().uppercase()
    
    // Si tiene 3 o menos caracteres, usar tal como está
    if (cleanName.length <= 3) {
        return cleanName.padEnd(3, 'X')
    }
    
    // Si tiene palabras, tomar iniciales de las primeras 2 palabras
    val words = cleanName.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        words.size >= 2 -> {
            val firstTwo = words.take(2)
            val initials = firstTwo.map { it.first() }.joinToString("")
            initials.padEnd(3, 'X')
        }
        words.size == 1 -> {
            val word = words[0]
            when {
                word.length >= 3 -> word.take(3)
                else -> word.padEnd(3, 'X')
            }
        }
        else -> "CAT"
    }
}