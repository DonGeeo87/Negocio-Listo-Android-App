package com.negociolisto.app.ui.settings

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.negociolisto.app.R
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.UnifiedGradientHeader

/**
 * 📝 PANTALLA DE EDICIÓN DE PERFIL
 * 
 * Permite al usuario editar su información personal y foto de perfil.
 * Incluye funcionalidad para tomar fotos o seleccionar desde galería.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Launcher para seleccionar imagen desde galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
            viewModel.selectImage(it)
        }
    }
    
    // Launcher para tomar foto con cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            uiState.cameraUri?.let { viewModel.selectImage(it) }
        }
    }
    
    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si se otorga el permiso, abrir la cámara
            val cameraUri = viewModel.prepareCameraUri(context)
            cameraUri?.let { cameraLauncher.launch(it) }
        }
    }
    
    // Función para mostrar opciones de imagen
    fun showImageOptions() {
        viewModel.showImagePicker()
    }
    
    // Manejo de eventos de UI
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            // Mostrar toast de confirmación
            android.widget.Toast.makeText(
                context,
                "✅ Perfil guardado exitosamente",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            // Forzar actualización visual de imágenes en toda la app
            com.negociolisto.app.ui.components.ImageRefreshBus.bump()
            onSave()
        }
    }
    
    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.smallSpacing)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.sectionSpacing)
        ) {
            // Header unificado de perfil
            com.negociolisto.app.ui.components.HeaderProfile(
                title = uiState.name.ifBlank { uiState.email },
                subtitle = "Edita tu información personal",
                avatarEmoji = "👤",
                modifier = Modifier.fillMaxWidth()
            )

            // Sección de foto de perfil - Unificada
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header con gradiente unificado
                    UnifiedGradientHeader(
                        title = "📸 Foto de Perfil",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                    
                    // Foto de perfil
                    Box(
                        modifier = Modifier
                            .size(DesignTokens.avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showImageOptions() },
                        contentAlignment = Alignment.Center
                    ) {
                        val hasImage = uiState.selectedImageUri != null || !uiState.currentUser?.profilePhotoUrl.isNullOrBlank()
                        
                        when {
                            uiState.selectedImageUri != null -> {
                                com.negociolisto.app.ui.components.OptimizedProfileImage(
                                    imageUrl = uiState.selectedImageUri.toString(),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            uiState.currentUser?.profilePhotoUrl != null -> {
                                com.negociolisto.app.ui.components.OptimizedProfileImage(
                                    imageUrl = uiState.currentUser?.profilePhotoUrl,
                                    contentDescription = "Foto de perfil actual",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Sin foto",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Overlay solo cuando hay imagen - transparente con ícono pequeño en esquina
                        if (hasImage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { showImageOptions() },
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.CameraAlt,
                                            contentDescription = "Cambiar foto",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Sin imagen - mostrar overlay completo como antes
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                    .clickable { showImageOptions() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(DesignTokens.smallSpacing))
                    
                    Text(
                        text = "Toca para cambiar la foto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Formulario de datos personales - Unificado
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                ) {
                    // Header con gradiente unificado
                    UnifiedGradientHeader(
                        title = "👤 Información Personal",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Campo de nombre
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Nombre completo") },
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null)
                        },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Campo de email (solo lectura)
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null)
                        },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("El email no se puede cambiar") }
                    )
                    
                    // Campo de teléfono
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = viewModel::updatePhone,
                        label = { Text("Teléfono (opcional)") },
                        leadingIcon = {
                            Icon(Icons.Filled.Phone, contentDescription = null)
                        },
                        isError = uiState.phoneError != null,
                        supportingText = uiState.phoneError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Mostrar errores generales
            uiState.errorMessage?.let { error ->
                UnifiedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Cancelar",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Botón con gradiente premium - SIN contenedor doble
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF009FE3),
                                    Color(0xFF312783)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            enabled = !uiState.isLoading && uiState.name.isNotBlank()
                        ) {
                            if (!uiState.isLoading && uiState.name.isNotBlank()) {
                                viewModel.saveProfile(context)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (uiState.isLoading) "💾 Guardando..." else "💾 Guardar",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
    
    // Dialog para seleccionar fuente de imagen
    if (uiState.shouldShowImagePicker) {
        AlertDialog(
            onDismissRequest = { viewModel.hideImagePicker() },
            title = { Text("Seleccionar imagen") },
            text = { Text("¿Cómo quieres cambiar tu foto de perfil?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hideImagePicker()
                    galleryLauncher.launch(arrayOf("image/*"))
                    }
                ) {
                    Text("Galería")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.hideImagePicker()
                        // Solicitar permiso de cámara antes de abrir
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Cámara")
                }
            }
        )
    }
}
