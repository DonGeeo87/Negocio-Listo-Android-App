package com.negociolisto.app.ui.setup.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import java.io.File

/**
 * 📷 DIÁLOGO DE SELECCIÓN DE IMAGEN
 * 
 * Componente reutilizable para seleccionar imágenes desde cámara o galería.
 * Maneja permisos automáticamente y devuelve la URI de la imagen seleccionada.
 */
@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (String?) -> Unit,
    title: String = "Seleccionar imagen",
    cameraButtonText: String = "Cámara",
    galleryButtonText: String = "Galería"
) {
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<String?>(null) }
    
    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                onImageSelected(uri)
                onDismiss()
            }
        }
    }
    
    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            onImageSelected(it.toString())
            onDismiss()
        }
    }
    
    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Crear archivo temporal para la foto
            val tempFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            tempImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            ).toString()
            cameraLauncher.launch(FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            ))
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón de cámara
                Button(
                    onClick = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(cameraButtonText)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Botón de galería
                Button(
                    onClick = {
                        galleryLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(galleryButtonText)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón cancelar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

/**
 * 🎯 HOOK PARA SELECCIÓN DE IMAGEN
 * 
 * Hook personalizado que maneja la lógica de selección de imagen.
 * Útil para reutilizar en diferentes pantallas.
 */
@Composable
fun useImagePicker(
    onImageSelected: (String?) -> Unit
): () -> Unit {
    var showDialog by remember { mutableStateOf(false) }
    
    if (showDialog) {
        ImagePickerDialog(
            onDismiss = { showDialog = false },
            onImageSelected = onImageSelected
        )
    }
    
    return { showDialog = true }
}
