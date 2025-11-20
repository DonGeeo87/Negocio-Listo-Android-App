package com.negociolisto.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.negociolisto.app.domain.model.Customer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectionBottomSheet(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCustomerSelected: (Customer) -> Unit,
    onDismiss: () -> Unit,
    onCreateNewClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Filtrar clientes basándose en la búsqueda
    val filteredCustomers = remember(customers, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            customers
        } else {
            customers.filter { customer ->
                customer.name.lowercase().contains(q) ||
                customer.phone?.lowercase()?.contains(q) == true ||
                customer.email?.lowercase()?.contains(q) == true ||
                customer.companyName?.lowercase()?.contains(q) == true
            }
        }
    }
    
    // Paginación para listas grandes
    val pageSize = 50
    var visibleCount by remember { mutableStateOf(pageSize) }
    
    // Resetear paginación cuando cambia la búsqueda o los clientes filtrados
    LaunchedEffect(searchQuery, filteredCustomers.size) {
        visibleCount = pageSize.coerceAtMost(filteredCustomers.size)
    }
    
    val displayedCustomers = remember(filteredCustomers, visibleCount) {
        filteredCustomers.take(visibleCount)
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seleccionar Cliente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }
            
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    onSearchQueryChange(it)
                    visibleCount = pageSize // Reset paginación al buscar
                },
                label = { Text("Buscar cliente...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                )
            )
            
            // Botón crear nuevo
            FilledTonalButton(
                onClick = onCreateNewClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear nuevo cliente")
            }
            
            // Lista de clientes con altura limitada
            if (displayedCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No se encontraron clientes" else "No hay clientes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Crea tu primer cliente para comenzar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedCustomers, key = { it.id }) { customer ->
                        val isSelected = customer.id == selectedCustomer?.id
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCustomerSelected(customer) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 4.dp else 1.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icono de cliente
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                
                                // Información del cliente
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    customer.companyName?.takeIf { it.isNotBlank() }?.let { company ->
                                        Text(
                                            text = company,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    customer.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                                        Text(
                                            text = phone,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                // Indicador de selección
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Botón cargar más
                    if (visibleCount < filteredCustomers.size) {
                        item {
                            TextButton(
                                onClick = { visibleCount = (visibleCount + pageSize).coerceAtMost(filteredCustomers.size) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cargar más (${filteredCustomers.size - visibleCount} restantes)")
                            }
                        }
                    }
                }
            }
        }
    }
}

