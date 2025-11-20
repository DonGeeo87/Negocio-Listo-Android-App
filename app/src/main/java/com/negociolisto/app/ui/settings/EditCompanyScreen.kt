package com.negociolisto.app.ui.settings

import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.components.ModernDropdown

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import com.negociolisto.app.ui.design.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import coil.request.ImageRequest
import com.negociolisto.app.domain.model.BusinessType
import com.negociolisto.app.domain.model.BusinessSocialMedia

/**
 * 🏢 PANTALLA DE EDICIÓN DE EMPRESA
 * 
 * Permite al usuario editar la información de su empresa y logo.
 * Incluye funcionalidad para tomar fotos o seleccionar desde galería.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCompanyScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditCompanyViewModel = hiltViewModel()
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
            viewModel.selectLogo(it)
        }
    }
    
    // Launcher para tomar foto con cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            uiState.cameraUri?.let { viewModel.selectLogo(it) }
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
        viewModel.showLogoPicker()
    }
    
    // Manejo de eventos de UI
    LaunchedEffect(uiState.isSaved) {
        println("🔍 DEBUG: LaunchedEffect isSaved = ${uiState.isSaved}")
        if (uiState.isSaved) {
            println("✅ DEBUG: Ejecutando onSave()")
            // Mostrar toast de confirmación
            android.widget.Toast.makeText(
                context,
                "✅ Empresa guardada exitosamente",
                android.widget.Toast.LENGTH_SHORT
            ).show()
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
            // Sección de logo de empresa - Modernizada
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏢 Logo de la Empresa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
                    
                    // Logo de empresa
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showImageOptions() },
                        contentAlignment = Alignment.Center
                    ) {
                        val hasImage = uiState.selectedLogoUri != null || !uiState.currentUser?.businessLogoUrl.isNullOrBlank()
                        
                        when {
                            uiState.selectedLogoUri != null -> {
                                com.negociolisto.app.ui.components.OptimizedImage(
                                    imageUrl = uiState.selectedLogoUri.toString(),
                                    contentDescription = "Logo de empresa",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }
                            !uiState.currentUser?.businessLogoUrl.isNullOrBlank() -> {
                                com.negociolisto.app.ui.components.OptimizedImage(
                                    imageUrl = uiState.currentUser?.businessLogoUrl,
                                    contentDescription = "Logo de empresa actual",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }
                            uiState.currentUser?.businessName != null -> {
                                // Mostrar iniciales del negocio si no hay logo
                                Text(
                                    text = uiState.currentUser?.businessName?.take(2)?.uppercase() ?: "LO",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Filled.Business,
                                    contentDescription = "Sin logo",
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
                                    .clip(RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showImageOptions() },
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.CameraAlt,
                                            contentDescription = "Cambiar logo",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Sin imagen - mostrar overlay completo como antes
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                                    .clickable { showImageOptions() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.CameraAlt,
                                    contentDescription = "Cambiar logo",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(DesignTokens.itemSpacing))
                    
                    Text(
                        text = "Toca para cambiar el logo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Formulario de información de empresa - Modernizado
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.sectionSpacing)
                ) {
                    Text(
                        text = "🏢 Información de la Empresa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
                    
                    // Campo de nombre del negocio
                    UnifiedTextField(
                        value = uiState.businessName,
                        onValueChange = viewModel::updateBusinessName,
                        label = "Nombre del negocio",
                        isError = uiState.businessNameError != null,
                        errorMessage = uiState.businessNameError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Campo de tipo de negocio
                    ModernDropdown(
                        items = BusinessType.values().toList(),
                        selectedItem = uiState.selectedBusinessType,
                        onItemSelected = { businessType ->
                            viewModel.selectBusinessType(businessType)
                        },
                        itemText = { it.displayName },
                        label = "Tipo de negocio",
                        placeholder = "Seleccionar tipo de negocio",
                        modifier = Modifier.fillMaxWidth(),
                        itemIcon = { businessType ->
                            {
                                Icon(
                                    imageVector = when (businessType) {
                                        BusinessType.RETAIL -> Icons.Filled.Store
                                        BusinessType.RESTAURANT -> Icons.Filled.Restaurant
                                        BusinessType.SERVICES -> Icons.Filled.Build
                                        BusinessType.WHOLESALE -> Icons.Filled.Inventory
                                        BusinessType.MANUFACTURING -> Icons.Filled.PrecisionManufacturing
                                        BusinessType.AGRICULTURE -> Icons.Filled.Grass
                                        BusinessType.TECHNOLOGY -> Icons.Filled.Computer
                                        BusinessType.HEALTH -> Icons.Filled.LocalHospital
                                        BusinessType.EDUCATION -> Icons.Filled.School
                                        BusinessType.TRANSPORTATION -> Icons.Filled.LocalShipping
                                        BusinessType.CONSTRUCTION -> Icons.Filled.Build
                                        BusinessType.BEAUTY -> Icons.Filled.Face
                                        BusinessType.FITNESS -> Icons.Filled.FitnessCenter
                                        BusinessType.ENTERTAINMENT -> Icons.Filled.TheaterComedy
                                        BusinessType.OTHER -> Icons.Filled.Business
                                    },
                                    contentDescription = businessType.displayName,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                    
                    
                    // Campo de dirección
                    UnifiedTextField(
                        value = uiState.businessAddress,
                        onValueChange = viewModel::updateBusinessAddress,
                        label = "Dirección (opcional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Campo de teléfono del negocio
                    UnifiedTextField(
                        value = uiState.businessPhone,
                        onValueChange = viewModel::updateBusinessPhone,
                        label = "Teléfono del negocio (opcional)",
                        isError = uiState.businessPhoneError != null,
                        errorMessage = uiState.businessPhoneError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Campo de correo de la empresa
                    UnifiedTextField(
                        value = uiState.businessEmail,
                        onValueChange = viewModel::updateBusinessEmail,
                        label = "Correo de la empresa (opcional)",
                        isError = uiState.businessEmailError != null,
                        errorMessage = uiState.businessEmailError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Campo de RUT de la empresa
                    UnifiedTextField(
                        value = uiState.businessRut,
                        onValueChange = viewModel::updateBusinessRut,
                        label = "RUT de la empresa (opcional)",
                        isError = uiState.businessRutError != null,
                        errorMessage = uiState.businessRutError,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Sección de redes sociales - Modernizada
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.sectionSpacing)
                ) {
                    Text(
                        text = "🌐 Redes Sociales",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
                    
                    // Facebook
                    UnifiedTextField(
                        value = uiState.businessSocialMedia.facebook ?: "",
                        onValueChange = { facebook ->
                            viewModel.updateSocialMedia(
                                uiState.businessSocialMedia.copy(facebook = facebook.takeIf { it.isNotBlank() })
                            )
                        },
                        label = "Facebook (opcional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Instagram
                    UnifiedTextField(
                        value = uiState.businessSocialMedia.instagram ?: "",
                        onValueChange = { instagram ->
                            viewModel.updateSocialMedia(
                                uiState.businessSocialMedia.copy(instagram = instagram.takeIf { it.isNotBlank() })
                            )
                        },
                        label = "Instagram (opcional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Twitter/X
                    UnifiedTextField(
                        value = uiState.businessSocialMedia.twitter ?: "",
                        onValueChange = { twitter ->
                            viewModel.updateSocialMedia(
                                uiState.businessSocialMedia.copy(twitter = twitter.takeIf { it.isNotBlank() })
                            )
                        },
                        label = "Twitter/X (opcional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // LinkedIn
                    UnifiedTextField(
                        value = uiState.businessSocialMedia.linkedin ?: "",
                        onValueChange = { linkedin ->
                            viewModel.updateSocialMedia(
                                uiState.businessSocialMedia.copy(linkedin = linkedin.takeIf { it.isNotBlank() })
                            )
                        },
                        label = "LinkedIn (opcional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // TikTok
                    UnifiedTextField(
                        value = uiState.businessSocialMedia.tiktok ?: "",
                        onValueChange = { tiktok ->
                            viewModel.updateSocialMedia(
                                uiState.businessSocialMedia.copy(tiktok = tiktok.takeIf { it.isNotBlank() })
                            )
                        },
                        label = "TikTok (opcional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Sitio Web
                    UnifiedTextField(
                        value = uiState.businessSocialMedia.website ?: "",
                        onValueChange = { website ->
                            viewModel.updateSocialMedia(
                                uiState.businessSocialMedia.copy(website = website.takeIf { it.isNotBlank() })
                            )
                        },
                        label = "Sitio Web (opcional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Mostrar errores generales
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(DesignTokens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.smallSpacing))
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
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
            ) {
                UnifiedOutlinedButton(
                    text = "Cancelar",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                
                if (uiState.isLoading) {
                    UnifiedButton(
                        text = "💾 Guardando...",
                        onClick = { /* No hacer nada */ },
                        modifier = Modifier.weight(1f)
                    )
                } else if (uiState.businessName.isNotBlank()) {
                    UnifiedButton(
                        text = "💾 Guardar",
                        onClick = { viewModel.saveCompany(context) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    UnifiedOutlinedButton(
                        text = "💾 Completa el nombre",
                        onClick = { /* No hacer nada */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    
    // Dialog para seleccionar fuente de imagen
    if (uiState.shouldShowLogoPicker) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLogoPicker() },
            title = { Text("Seleccionar logo") },
            text = { Text("¿Cómo quieres cambiar el logo de tu empresa?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hideLogoPicker()
                        galleryLauncher.launch(arrayOf("image/*"))
                    }
                ) {
                    Text("Galería")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.hideLogoPicker()
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
