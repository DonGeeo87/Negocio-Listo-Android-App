package com.negociolisto.app.ui.free_tools

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.UnifiedTextField
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.design.DesignTokens

/**
 * 🛠️ PANTALLA DE HERRAMIENTAS GRATUITAS
 * 
 * Muestra un catálogo completo de herramientas y recursos útiles
 * para gestionar el negocio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeToolsScreen(
    @Suppress("UNUSED_PARAMETER") onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Cargar herramientas desde assets con manejo de errores
    val tools = remember {
        runCatching {
            FreeToolsRepository.loadFromAssets(context, "free_tools.json")
        }.getOrDefault(emptyList())
    }

    // Obtener categorías únicas
    val categories = remember(tools) {
        buildList {
            add("Todas")
            addAll(tools.map { it.category }.distinct().sorted())
        }
    }

    var selectedCategory by remember { mutableStateOf("Todas") }
    var searchQuery by remember { mutableStateOf("") }

    // NO usar Scaffold para evitar doble topbar
    // Solo el topbar del MainScreen debe aparecer
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Barra de búsqueda
        UnifiedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            UnifiedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Buscar herramientas...",
                leadingIcon = Icons.Filled.Search,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.cardPadding)
            )
        }
        
        // Chips de filtro con scroll horizontal
        CategoryChips(
            categories = categories,
            selected = selectedCategory,
            onSelect = { selectedCategory = it }
        )

        // Grilla de herramientas con paginación
        FreeToolsGrid(
            tools = tools,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            onOpenUrl = { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        )
    }
}
