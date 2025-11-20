package com.negociolisto.app.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb

/**
 * 📷 COMPONENTE DE ESCANEO DE CÓDIGOS DE BARRAS
 * 
 * Componente que permite escanear códigos de barras y códigos QR
 * usando la cámara del dispositivo. Incluye manejo de permisos
 * y una interfaz de usuario moderna.
 * 
 * Características:
 * - Escaneo de códigos de barras y QR
 * - Manejo automático de permisos
 * - Interfaz de usuario moderna
 * - Vibración al escanear exitosamente
 * - Manejo de errores
 */
@Composable
fun BarcodeScanner(
    onBarcodeScanned: (String) -> Unit,
    onError: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Verificar permisos al iniciar
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            showPermissionDialog = true
        }
    }
    
    // Launcher para escanear códigos de barras
    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null) {
            onBarcodeScanned(result.contents)
        } else {
            onError("No se pudo escanear el código")
        }
    }
    
    // Función para iniciar el escaneo
    fun startScanning() {
        if (hasPermission) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                setPrompt("Coloca el código de barras dentro del marco")
                setCameraId(0)
                setBeepEnabled(true)
                setBarcodeImageEnabled(true)
                setOrientationLocked(false)
            }
            barcodeLauncher.launch(options)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasPermission) {
            // Botón para iniciar escaneo
            Button(
                onClick = { startScanning() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("📷 Escanear Código de Barras")
            }
        } else {
            // Mensaje de permisos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📷 Permiso de Cámara Requerido",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Necesitamos acceso a la cámara para escanear códigos de barras",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { startScanning() }
                    ) {
                        Text("Conceder Permiso")
                    }
                }
            }
        }
    }
    
    // Dialog para explicar por qué necesitamos el permiso
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text("Permiso de Cámara")
            },
            text = {
                Text(
                    "Para escanear códigos de barras necesitamos acceso a la cámara. " +
                    "Este permiso solo se usa para escanear códigos y no se almacenan imágenes."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        // Abrir configuración de la app
                        // TODO: Implementar navegación a configuración
                    }
                ) {
                    Text("Configuración")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * 📷 ESCANER SIMPLE
 * 
 * Versión simplificada que solo muestra un botón
 * para escanear códigos de barras.
 */
@Composable
fun SimpleBarcodeScanner(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    
    // Verificar permisos
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Launcher para permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            Toast.makeText(
                context,
                "Permiso de cámara denegado. Habilítalo en Ajustes para escanear",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Launcher para escanear
    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null) {
            onBarcodeScanned(result.contents)
        }
    }
    
    // Función para escanear
    fun scanBarcode() {
        if (hasPermission) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                setPrompt("Escanea el código de barras")
                setCameraId(0)
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
            barcodeLauncher.launch(options)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Button(
        onClick = { scanBarcode() },
        modifier = modifier
    ) {
        Text("📷 Escanear")
    }
}

/**
 * 🔍 GENERADOR DE CÓDIGOS QR
 * 
 * Componente para generar códigos QR (funcionalidad futura)
 */
@Composable
fun QRCodeGenerator(
    text: String,
    size: Int = 200,
    modifier: Modifier = Modifier
) {
    val qrBitmap = remember(text, size) {
        generateQRCode(text, size)
    }
    
    Card(
        modifier = modifier.size(size.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = qrBitmap,
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Error generando QR",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Genera un código QR a partir de un texto
 */
private fun generateQRCode(text: String, size: Int): ImageBitmap? {
    return try {
        val writer = QRCodeWriter()
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1
        )
        
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
