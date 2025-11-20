package com.negociolisto.app.ui.setup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.ui.components.UnifiedPrimaryButton
import com.negociolisto.app.ui.design.UnifiedTextField
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.components.UnifiedGradientHeaderCard
import com.negociolisto.app.ui.design.BrandColors
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.GradientTokens

/**
 * 🏗️ PANTALLA DE CONFIGURACIÓN DE PERFILES UNIFICADA
 * 
 * Pantalla que combina configuración de perfil personal y empresarial con tabs.
 * Aparece después del registro y antes del onboarding visual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Personal", "Empresa")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header moderno con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(GradientTokens.brandGradient())
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configura tu Perfil",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.white
                    )
                    TextButton(onClick = onSkip) {
                        Text(
                            text = "Omitir",
                            color = BrandColors.white.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Información moderna con gradiente
            UnifiedGradientHeaderCard(
                title = "Información",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                headerIcon = Icons.Filled.Info
            ) {
                Text(
                    text = "Configura tu información personal y empresarial para aprovechar todas las funciones de la app.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        
            // Tabs modernas con gradientes
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BrandColors.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) BrandColors.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = if (index == 0) Icons.Filled.Person else Icons.Filled.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (selectedTab == index) BrandColors.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }
                }
            }
        
        // Content según el tab seleccionado
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> PersonalProfileTab(
                    uiState = uiState,
                    onNameChange = viewModel::updatePersonalName,
                    onPhoneChange = viewModel::updatePersonalPhone,
                    onPhotoChange = viewModel::updatePersonalPhoto,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> CompanyProfileTab(
                    uiState = uiState,
                    onBusinessNameChange = viewModel::updateBusinessName,
                    onBusinessTypeChange = viewModel::updateBusinessType,
                    onBusinessRutChange = viewModel::updateBusinessRut,
                    onBusinessAddressChange = viewModel::updateBusinessAddress,
                    onBusinessPhoneChange = viewModel::updateBusinessPhone,
                    onBusinessEmailChange = viewModel::updateBusinessEmail,
                    onLogoChange = viewModel::updateBusinessLogo,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
            // Botón moderno con gradiente
            UnifiedPrimaryButton(
                text = if (uiState.isSaving) "Guardando..." else "Guardar y Continuar",
                onClick = {
                    viewModel.saveProfiles()
                    onComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = uiState.isValid && !uiState.isSaving,
                loading = uiState.isSaving,
                icon = Icons.Filled.Save
            )
        }
    }
}

@Composable
private fun PersonalProfileTab(
    uiState: com.negociolisto.app.ui.setup.ProfileSetupViewModel.ProfileSetupUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPhotoChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header con gradiente
        UnifiedGradientHeaderCard(
            title = "Información Personal",
            subtitle = "Configura tu información personal básica",
            modifier = Modifier.fillMaxWidth(),
            headerIcon = Icons.Filled.Person
        ) {}
        
        // Campo nombre moderno
        UnifiedTextField(
            value = uiState.personalName,
            onValueChange = onNameChange,
            label = "Nombre completo",
            placeholder = "Ej: Juan Carlos Pérez",
            leadingIcon = Icons.Filled.Person,
            isError = uiState.personalNameError.isNotEmpty(),
            errorMessage = uiState.personalNameError
        )
        
        // Campo teléfono moderno
        UnifiedTextField(
            value = uiState.personalPhone,
            onValueChange = onPhoneChange,
            label = "Teléfono (opcional)",
            placeholder = "Ej: +56 9 1234 5678",
            leadingIcon = Icons.Filled.Phone
        )
        
        // Foto de perfil moderna
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Foto de perfil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Opcional - Puedes agregarla después",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* TODO: Implementar selección de foto */ }
                ) {
                    Text(
                        "Seleccionar foto",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CompanyProfileTab(
    uiState: com.negociolisto.app.ui.setup.ProfileSetupViewModel.ProfileSetupUiState,
    onBusinessNameChange: (String) -> Unit,
    onBusinessTypeChange: (com.negociolisto.app.domain.model.BusinessType?) -> Unit,
    onBusinessRutChange: (String) -> Unit,
    onBusinessAddressChange: (String) -> Unit,
    onBusinessPhoneChange: (String) -> Unit,
    onBusinessEmailChange: (String) -> Unit,
    onLogoChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header con gradiente
        UnifiedGradientHeaderCard(
            title = "Información Empresarial",
            subtitle = "Configura la información de tu negocio (opcional)",
            modifier = Modifier.fillMaxWidth(),
            headerIcon = Icons.Filled.Business
        ) {}
        
        // Campo nombre del negocio moderno
        UnifiedTextField(
            value = uiState.businessName,
            onValueChange = onBusinessNameChange,
            label = "Nombre del negocio",
            placeholder = "Ej: Tienda La Esperanza",
            leadingIcon = Icons.Filled.Business,
            isError = uiState.businessNameError.isNotEmpty(),
            errorMessage = uiState.businessNameError
        )
        
        // Campo RUT moderno
        UnifiedTextField(
            value = uiState.businessRut,
            onValueChange = onBusinessRutChange,
            label = "RUT (opcional)",
            placeholder = "Ej: 12.345.678-9",
            leadingIcon = Icons.AutoMirrored.Filled.Assignment
        )
        
        // Campo dirección moderno
        UnifiedTextField(
            value = uiState.businessAddress,
            onValueChange = onBusinessAddressChange,
            label = "Dirección (opcional)",
            placeholder = "Ej: Av. Principal 123, Santiago",
            leadingIcon = Icons.Filled.LocationOn,
            singleLine = false
        )
        
        // Campo teléfono empresarial moderno
        UnifiedTextField(
            value = uiState.businessPhone,
            onValueChange = onBusinessPhoneChange,
            label = "Teléfono empresarial (opcional)",
            placeholder = "Ej: +56 2 2345 6789",
            leadingIcon = Icons.Filled.Phone
        )
        
        // Campo email empresarial moderno
        UnifiedTextField(
            value = uiState.businessEmail,
            onValueChange = onBusinessEmailChange,
            label = "Email empresarial (opcional)",
            placeholder = "Ej: contacto@miempresa.cl",
            leadingIcon = Icons.Filled.Email
        )
        
        // Logo de empresa moderno
        UnifiedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Logo de empresa",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Opcional - Puedes agregarlo después",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* TODO: Implementar selección de logo */ }
                ) {
                    Text(
                        "Seleccionar logo",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
