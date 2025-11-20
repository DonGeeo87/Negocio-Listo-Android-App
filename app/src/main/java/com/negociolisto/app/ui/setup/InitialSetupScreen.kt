package com.negociolisto.app.ui.setup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.components.UnifiedGradientTopAppBar
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.GradientTokens
import com.negociolisto.app.ui.setup.components.useImagePicker
import com.negociolisto.app.ui.setup.components.ContactSelectorDialog

/**
 * 🚀 CONFIGURACIÓN INICIAL COMPLETA
 * 
 * Pantalla de configuración inicial que guía al usuario a través de múltiples pasos:
 * 1. Bienvenida y explicación
 * 2. Configuración de categorías
 * 3. Configuración de perfil (opcional)
 * 4. Configuración de empresa (opcional)
 * 5. Configuración de clientes iniciales (opcional)
 * 6. Finalización
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSetupScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InitialSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
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
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .offset(y = slideInOffset)
            .alpha(fadeInAlpha)
    ) {
        // Header con progreso
        UnifiedGradientTopAppBar(
            title = "Configuración Inicial",
            subtitle = "Paso ${currentStep.ordinal + 1} de ${InitialSetupStep.values().size}",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Barra de progreso
        LinearProgressIndicator(
            progress = { (currentStep.ordinal + 1).toFloat() / InitialSetupStep.values().size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = BrandColors.white,
            trackColor = BrandColors.white.copy(alpha = 0.3f)
        )
        
        // Contenido principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (currentStep) {
                InitialSetupStep.WELCOME -> {
                    WelcomeStep(
                        onNext = { viewModel.nextStep() }
                    )
                }
                InitialSetupStep.CATEGORIES -> {
                    val categoryViewModel: InitialCategorySetupViewModel = hiltViewModel()
                    val categoryUiState by categoryViewModel.uiState.collectAsStateWithLifecycle()
                    
                    // Observar cuando se completan las categorías
                    LaunchedEffect(categoryUiState.isCompleted) {
                        if (categoryUiState.isCompleted) {
                            viewModel.nextStep()
                        }
                    }
                    
                    InitialCategorySetupScreen(
                        onComplete = { /* No hacer nada aquí, se maneja arriba */ }
                    )
                }
                InitialSetupStep.PROFILE -> {
                    ProfileSetupStep(
                        viewModel = viewModel,
                        onNext = { viewModel.nextStep() }
                    )
                }
                InitialSetupStep.COMPANY -> {
                    CompanySetupStep(
                        viewModel = viewModel,
                        onNext = { viewModel.nextStep() }
                    )
                }
                InitialSetupStep.CUSTOMERS -> {
                    CustomersSetupStep(
                        viewModel = viewModel,
                        onNext = { viewModel.nextStep() }
                    )
                }
                InitialSetupStep.COMPLETE -> {
                    CompleteStep(
                        viewModel = viewModel,
                        onFinish = onComplete
                    )
                }
            }
        }
    }
}

/**
 * 🎉 PASO DE BIENVENIDA
 */
@Composable
private fun WelcomeStep(
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono de bienvenida con gradiente
        Card(
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 24.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
        
        Text(
            text = "¡Bienvenido a NegocioListo!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Vamos a configurar tu aplicación para que puedas comenzar a gestionar tu negocio de manera eficiente.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Lista de pasos
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configuraremos:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SetupStepItem(
                    icon = "📦",
                    title = "Categorías de productos",
                    description = "Organiza tus productos por categorías"
                )
                
                SetupStepItem(
                    icon = "👤",
                    title = "Tu perfil",
                    description = "Información personal básica"
                )
                
                SetupStepItem(
                    icon = "🏢",
                    title = "Tu empresa",
                    description = "Datos de tu negocio"
                )
                
                SetupStepItem(
                    icon = "👥",
                    title = "Clientes iniciales",
                    description = "Agrega tus primeros clientes"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Comenzar configuración",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Continuar",
                modifier = Modifier.size(DesignTokens.mediumIconSize)
            )
        }
    }
}

/**
 * 👤 PASO DE CONFIGURACIÓN DE PERFIL
 */
@Composable
private fun ProfileSetupStep(
    viewModel: InitialSetupViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showImagePicker = useImagePicker { uri ->
        viewModel.updateProfilePhoto(uri)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👤",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Configura tu perfil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Esta información te ayudará a personalizar tu experiencia en la aplicación.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        
        // Formulario de información personal
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Información Personal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Campo de nombre
                OutlinedTextField(
                    value = uiState.userName,
                    onValueChange = { viewModel.updateUserName(it) },
                    label = { Text("Nombre completo") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = "Nombre")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Campo de teléfono
                OutlinedTextField(
                    value = uiState.userPhone,
                    onValueChange = { viewModel.updateUserPhone(it) },
                    label = { Text("Teléfono (opcional)") },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = "Teléfono")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
        
        // Sección de foto de perfil
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Foto de perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Opcional - Puedes agregarla después",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Contenedor de vista previa de foto de perfil
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showImagePicker() },
                    contentAlignment = Alignment.Center
                ) {
                    uiState.profilePhotoUri?.let { uri ->
                        com.negociolisto.app.ui.components.OptimizedProfileImage(
                            imageUrl = uri.toString(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } ?: run {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Sin foto",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Button(
                    onClick = showImagePicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = "Seleccionar foto",
                        modifier = Modifier.size(DesignTokens.mediumIconSize)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar foto")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuar")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Continuar",
                modifier = Modifier.size(DesignTokens.mediumIconSize)
            )
        }
    }
}

/**
 * 🏢 PASO DE CONFIGURACIÓN DE EMPRESA
 */
@Composable
private fun CompanySetupStep(
    viewModel: InitialSetupViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showLogoPicker = useImagePicker { uri ->
        viewModel.updateBusinessLogo(uri)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏢",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Información de tu empresa",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Agrega los datos de tu negocio para personalizar facturas y documentos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Información básica de la empresa
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Información Básica",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Nombre de la empresa
                OutlinedTextField(
                    value = uiState.businessName,
                    onValueChange = { viewModel.updateBusinessName(it) },
                    label = { Text("Nombre de la empresa") },
                    leadingIcon = {
                        Icon(Icons.Filled.Business, contentDescription = "Empresa")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // RUT de la empresa
                OutlinedTextField(
                    value = uiState.businessRut,
                    onValueChange = { viewModel.updateBusinessRut(it) },
                    label = { Text("RUT (opcional)") },
                    leadingIcon = {
                        Icon(Icons.Filled.Badge, contentDescription = "RUT")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
        
        // Información de contacto
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Información de Contacto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Dirección
                OutlinedTextField(
                    value = uiState.businessAddress,
                    onValueChange = { viewModel.updateBusinessAddress(it) },
                    label = { Text("Dirección (opcional)") },
                    leadingIcon = {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Dirección")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Teléfono de la empresa
                OutlinedTextField(
                    value = uiState.businessPhone,
                    onValueChange = { viewModel.updateBusinessPhone(it) },
                    label = { Text("Teléfono (opcional)") },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = "Teléfono")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Email de la empresa
                OutlinedTextField(
                    value = uiState.businessEmail,
                    onValueChange = { viewModel.updateBusinessEmail(it) },
                    label = { Text("Email (opcional)") },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = "Email")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
        
        // Logo de la empresa
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Logo de la empresa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Opcional - Puedes agregarlo después",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Contenedor de vista previa del logo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showLogoPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    uiState.businessLogoUri?.let { uri ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Logo de empresa",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Icon(
                            Icons.Filled.BusinessCenter,
                            contentDescription = "Sin logo",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Button(
                    onClick = showLogoPicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = "Seleccionar logo",
                        modifier = Modifier.size(DesignTokens.mediumIconSize)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar logo")
                }
                
                if (uiState.businessLogoUri != null) {
                    Text(
                        text = "✓ Logo seleccionado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuar")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Continuar",
                modifier = Modifier.size(DesignTokens.mediumIconSize)
            )
        }
    }
}

/**
 * 👥 PASO DE CONFIGURACIÓN DE CLIENTES
 */
@Composable
private fun CustomersSetupStep(
    viewModel: InitialSetupViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showContactSelector by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    
    // Estado para almacenar los contactos del dispositivo
    var deviceContacts by remember { mutableStateOf<List<CustomerData>>(emptyList()) }
    var isLoadingContacts by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableStateOf(0f) }
    var totalContacts by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    // Launcher para permisos de contactos
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("🔍 DEBUG InitialSetupScreen: Permisos de contactos otorgados")
            // Cargar contactos de forma asíncrona con progreso
            isLoadingContacts = true
            loadingProgress = 0f
            showContactSelector = true
            
            scope.launch {
                try {
                    val contacts = com.negociolisto.app.utils.ContactsImporter.getDeviceContactsAsync(
                        context = context,
                        onProgress = { progress, total ->
                            loadingProgress = progress.coerceIn(0f, 1f)
                            totalContacts = total
                        }
                    )
                    deviceContacts = contacts
                    isLoadingContacts = false
                    println("🔍 DEBUG InitialSetupScreen: Contactos obtenidos: ${contacts.size}")
                } catch (e: Exception) {
                    println("❌ DEBUG InitialSetupScreen: Error al cargar contactos: ${e.message}")
                    isLoadingContacts = false
                    deviceContacts = emptyList()
                }
            }
        } else {
            println("❌ DEBUG InitialSetupScreen: Permisos de contactos denegados")
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👥",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "Agrega tus primeros clientes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Puedes agregar hasta 3 clientes ahora o hacerlo más tarde.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Lista de clientes agregados
        if (uiState.customers.isNotEmpty()) {
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Clientes agregados (${uiState.customers.size}/3)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    uiState.customers.forEachIndexed { index, customer ->
                        CustomerItem(
                            customer = customer,
                            onRemove = { viewModel.removeCustomer(index) }
                        )
                    }
                }
            }
        }
        
        // Botones de acción
        if (uiState.customers.size < 3) {
            UnifiedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Agregar cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.Contacts,
                                contentDescription = "Importar contactos",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Importar")
                        }
                        
                        Button(
                            onClick = { showAddCustomerDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                Icons.Filled.PersonAdd,
                                contentDescription = "Agregar manual",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manual")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuar")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Continuar",
                modifier = Modifier.size(DesignTokens.mediumIconSize)
            )
        }
    }
    
    // Diálogo de selección de contactos con carga asíncrona
    if (showContactSelector) {
        ContactSelectorDialog(
            contacts = deviceContacts,
            isLoading = isLoadingContacts,
            loadingProgress = loadingProgress,
            totalContacts = totalContacts,
            onDismiss = { 
                showContactSelector = false
                isLoadingContacts = false
                loadingProgress = 0f
            },
            onContactsSelected = { selectedContacts ->
                selectedContacts.forEach { contact ->
                    viewModel.addCustomer(contact)
                }
                showContactSelector = false // Cerrar el diálogo después de seleccionar
                isLoadingContacts = false
                loadingProgress = 0f
            },
            maxSelection = 3 - uiState.customers.size
        )
    }
    
    // Diálogo para agregar cliente manualmente
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onCustomerAdded = { customer ->
                viewModel.addCustomer(customer)
            }
        )
    }
}

/**
 * 📋 ITEM DE CLIENTE
 */
@Composable
private fun CustomerItem(
    customer: CustomerData,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (!customer.phone.isNullOrBlank()) {
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Eliminar",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 📝 DIÁLOGO PARA AGREGAR CLIENTE MANUALMENTE
 */
@Composable
private fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onCustomerAdded: (CustomerData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar cliente") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCustomerAdded(
                            CustomerData(
                                name = name,
                                phone = phone.ifBlank { null },
                                email = email.ifBlank { null }
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * ✅ PASO DE FINALIZACIÓN
 */
@Composable
private fun CompleteStep(
    viewModel: InitialSetupViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Guardar datos automáticamente cuando se llega a este paso
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (!uiState.isSetupCompleted) {
            viewModel.saveAllData(context)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono de éxito con gradiente
        Card(
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 24.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
        
        Text(
            text = "¡Configuración completada!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Ya estás listo para comenzar a gestionar tu negocio con NegocioListo.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Próximos pasos:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SetupStepItem(
                    icon = "📦",
                    title = "Agregar productos",
                    description = "Comienza agregando tus productos al inventario"
                )
                
                SetupStepItem(
                    icon = "💰",
                    title = "Registrar ventas",
                    description = "Registra tus primeras ventas"
                )
                
                SetupStepItem(
                    icon = "📊",
                    title = "Ver estadísticas",
                    description = "Revisa el dashboard para ver el rendimiento"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardando...")
            } else {
                Text(
                    text = "Comenzar a usar la app",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Listo",
                    modifier = Modifier.size(DesignTokens.mediumIconSize)
                )
            }
        }
        
        // Mostrar error si hay alguno
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Error: $error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * 🧩 COMPONENTE DE ELEMENTO DE PASO
 */
@Composable
private fun SetupStepItem(
    icon: String,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleLarge
        )
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 📋 ENUM DE PASOS DE CONFIGURACIÓN
 */
enum class InitialSetupStep {
    WELCOME,
    CATEGORIES,
    PROFILE,
    COMPANY,
    CUSTOMERS,
    COMPLETE
}
