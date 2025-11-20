package com.negociolisto.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.ui.components.ModernFormTopAppBar

/**
 * 🏗️ PANTALLA UNIFICADA DE PERFILES
 * 
 * Pantalla con tabs que combina perfil personal y empresarial
 * para reemplazar las pantallas separadas de EditProfileScreen y EditCompanyScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    profileViewModel: EditProfileViewModel = hiltViewModel(),
    companyViewModel: EditCompanyViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Perfil Personal", "Empresa")
    
    // NO usar Scaffold con topBar para evitar doble topbar
    // El MainScreen ya maneja el topbar dinámico
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = {
                        Icon(
                            imageVector = if (index == 0) 
                                Icons.Filled.Person 
                            else 
                                Icons.Filled.Business,
                            contentDescription = null
                        )
                    }
                )
            }
        }
        
        // Content
        when (selectedTab) {
            0 -> PersonalProfileContent(
                viewModel = profileViewModel,
                onSave = { /* Auto-save se maneja en el ViewModel */ }
            )
            1 -> CompanyProfileContent(
                viewModel = companyViewModel,
                onSave = { /* Auto-save se maneja en el ViewModel */ }
            )
        }
    }
}

@Composable
private fun PersonalProfileContent(
    viewModel: EditProfileViewModel,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Información Personal",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        // TODO: Extraer contenido de EditProfileScreen como composable reutilizable
        // Por ahora, mostrar placeholder
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Contenido del perfil personal",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Este contenido se extraerá de EditProfileScreen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompanyProfileContent(
    viewModel: EditCompanyViewModel,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Información Empresarial",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        // TODO: Extraer contenido de EditCompanyScreen como composable reutilizable
        // Por ahora, mostrar placeholder
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Contenido del perfil empresarial",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Este contenido se extraerá de EditCompanyScreen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
