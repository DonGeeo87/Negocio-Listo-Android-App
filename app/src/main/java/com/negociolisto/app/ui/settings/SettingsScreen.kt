package com.negociolisto.app.ui.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import com.negociolisto.app.ui.components.ModernFormTopAppBar
import kotlinx.coroutines.launch
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.AnimationTokens

// 🎨 CONSTANTES DE DISEÑO UNIFICADAS (Deprecated - Use DesignTokens)
@Deprecated("Use DesignTokens instead")
private object SettingsDesign {
    val cardElevation = DesignTokens.cardElevation
    val cardShape = DesignTokens.cardShape
    val buttonShape = DesignTokens.buttonShape
    val cardPadding = DesignTokens.cardPadding
    val sectionSpacing = DesignTokens.sectionSpacing
    val itemSpacing = DesignTokens.itemSpacing
    val iconSize = DesignTokens.iconSize
    val largeIconSize = DesignTokens.largeIconSize
}

// 🧩 COMPONENTES REUTILIZABLES UNIFICADOS

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SettingsDesign.itemSpacing)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        content()
    }
}

@Composable
private fun SettingsCard(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(SettingsDesign.cardElevation, SettingsDesign.cardShape)
            .clickable { onClick() },
        shape = SettingsDesign.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        content()
    }
}

@Composable
private fun SettingsOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SettingsDesign.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(SettingsDesign.iconSize)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Ir a $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.primary
        ),
        shape = SettingsDesign.buttonShape
    ) {
        Icon(
            icon, 
            contentDescription = null,
            modifier = Modifier.size(DesignTokens.smallIconSize),
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onEditProfile: () -> Unit = {},
    onEditCompany: () -> Unit = {},
    onBackupManagement: () -> Unit = {},
    onCategoryManagement: () -> Unit = {},
    onUIScaleSettings: () -> Unit = {},
    onUsageLimits: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Refrescar datos cuando se regrese a esta pantalla
    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }
    
    // Estados para backup y restore
    val isBackingUp by viewModel.isBackingUp.collectAsStateWithLifecycle()
    val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()
    val backupProgress by viewModel.backupProgress.collectAsStateWithLifecycle()
    val backupStatus by viewModel.backupStatus.collectAsStateWithLifecycle()
    val backupMessage by viewModel.backupMessage.collectAsStateWithLifecycle()
    
    // Estados para configuración de backup automático
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val backupFrequency by viewModel.backupFrequency.collectAsStateWithLifecycle()
    val availableBackups by viewModel.availableBackups.collectAsStateWithLifecycle()
    val isLoadingBackups by viewModel.isLoadingBackups.collectAsStateWithLifecycle()
    
    // Estados de exportación
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val exportStatus by viewModel.exportStatus.collectAsStateWithLifecycle()
    val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()
    val exportUri by viewModel.exportUri.collectAsStateWithLifecycle()

    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(AnimationTokens.longDuration, easing = AnimationTokens.decelerateEasing),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "fadeIn"
    )
    
    // Manejo de errores
    LaunchedEffect(Unit) {
        try {
            // Inicialización segura
        } catch (e: Exception) {
            // En caso de error, volver atrás
            onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .offset(y = slideInOffset)
                .alpha(fadeInAlpha)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(SettingsDesign.sectionSpacing)
        ) {

            // Sección de Perfil y Empresa
            item {
                SettingsSection("👤 Información Personal") {
                    ModernProfileCard(
                        user = user,
                        context = context,
                        onEditClick = onEditProfile
                    )
                    ModernCompanyCard(
                        user = user,
                        context = context,
                        onEditClick = onEditCompany
                    )
                }
            }

            // Sección de Gestión de Datos
            item {
                SettingsSection("📊 Gestión de Datos") {
                    SettingsOption(
                        icon = Icons.Filled.CloudUpload,
                        title = "🔥 Backup con Firebase",
                        subtitle = "Sistema principal de backup automático y continuo",
                        onClick = onBackupManagement
                    )
                    
                    SettingsOption(
                        icon = Icons.Filled.Category,
                        title = "📂 Gestión de Categorías",
                        subtitle = "Crear y personalizar categorías de productos",
                        onClick = onCategoryManagement
                    )
                    
                    SettingsOption(
                        icon = Icons.Filled.Security,
                        title = "🔒 Límites de Uso",
                        subtitle = "Ver límites de productos, clientes y colecciones",
                        onClick = onUsageLimits
                    )
                }
            }

            // Sección de Personalización
            item {
                SettingsSection("🎨 Personalización") {
                    SettingsOption(
                        icon = Icons.Filled.Tune,
                        title = "📏 Escala de la Interfaz",
                        subtitle = "Ajusta el tamaño de la UI para tu preferencia (0.85x - 1.15x)",
                        onClick = onUIScaleSettings
                    )
                }
            }

            // Feedback de exportación
            item(key = "export_feedback") {
                if (isExporting) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = exportStatus, color = MaterialTheme.colorScheme.primary)
                }
                exportMessage?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(text = msg, color = MaterialTheme.colorScheme.primary)
                    exportUri?.let { uri ->
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "*/*"
                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir archivo exportado"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Text("Compartir", color = MaterialTheme.colorScheme.onPrimary) }
                            OutlinedButton(
                                onClick = { viewModel.clearExportMessage() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) { Text("Ocultar") }
                        }
                    } ?: run {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearExportMessage() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) { Text("Cerrar") }
                    }
                }
            }

            // Sección de Acciones
            item {
                SettingsSection("🚪 Acciones") {
                    SettingsActionButton(
                        text = "Cerrar Sesión",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = { viewModel.logout(onLoggedOut) },
                        isDestructive = true
                    )
                }
            }
            
            // Espacio final
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// 👤 TARJETA DE PERFIL MODERNA
@Composable
private fun ModernProfileCard(
    user: com.negociolisto.app.domain.model.User?,
    context: android.content.Context,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(SettingsDesign.cardElevation, SettingsDesign.cardShape)
            .clickable { onEditClick() },
        shape = SettingsDesign.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(SettingsDesign.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 Perfil Personal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Editar perfil",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto de perfil
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        !user?.profilePhotoUrl.isNullOrBlank() -> {
                            com.negociolisto.app.ui.components.OptimizedProfileImage(
                                imageUrl = user?.profilePhotoUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        else -> {
                            Text(
                                text = user?.name?.take(2)?.uppercase() ?: "U",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name ?: "Usuario", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user?.email ?: "-", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    user?.phone?.let { phone ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = phone, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Toca para editar tu información personal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 🏢 TARJETA DE EMPRESA MODERNA
@Composable
private fun ModernCompanyCard(
    user: com.negociolisto.app.domain.model.User?,
    context: android.content.Context,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(SettingsDesign.cardElevation, SettingsDesign.cardShape)
            .clickable { onEditClick() },
        shape = SettingsDesign.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(SettingsDesign.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏢 Información de Empresa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.secondary
                )
                
                Icon(
                    Icons.Filled.Business,
                    contentDescription = "Editar empresa",
                    tint = BrandColors.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo de empresa
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(SettingsDesign.cardShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    BrandColors.secondary.copy(alpha = 0.2f),
                                    BrandColors.secondary.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        !user?.businessLogoUrl.isNullOrBlank() -> {
                            com.negociolisto.app.ui.components.OptimizedImage(
                                imageUrl = user?.businessLogoUrl,
                                contentDescription = "Logo de empresa",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(SettingsDesign.cardShape)
                            )
                        }
                        else -> {
                            Text(
                                text = user?.businessName?.take(2)?.uppercase() ?: "E",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.secondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.businessName ?: "Mi Negocio", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user?.businessType?.displayName ?: "Sin tipo definido", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    user?.businessAddress?.let { address ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = address, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Toca para editar la información de tu empresa",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.secondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
