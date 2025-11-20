package com.negociolisto.app.ui.setup.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.negociolisto.app.ui.setup.CustomerData
import com.negociolisto.app.ui.components.UnifiedCard
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.BrandColors

/**
 * 📞 DIÁLOGO DE SELECCIÓN DE CONTACTOS
 * 
 * Componente para seleccionar múltiples contactos de la lista de contactos del dispositivo.
 * Permite búsqueda y selección múltiple con checkboxes.
 */
@Composable
fun ContactSelectorDialog(
    contacts: List<CustomerData>,
    isLoading: Boolean = false,
    loadingProgress: Float = 0f,
    totalContacts: Int = 0,
    onDismiss: () -> Unit,
    onContactsSelected: (List<CustomerData>) -> Unit,
    maxSelection: Int = 3,
    title: String = "Seleccionar contactos"
) {
    var selectedContacts by remember { mutableStateOf(setOf<CustomerData>()) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Filtrar contactos basándose en la búsqueda
    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                contact.phone?.contains(searchQuery, ignoreCase = true) == true ||
                contact.email?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        UnifiedCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignTokens.cardPadding)
            ) {
                // Título con gradiente
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Selecciona hasta $maxSelection contactos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
                
                // Barra de búsqueda mejorada
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar contactos...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
                
                // Indicador de carga con progreso
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                strokeWidth = 4.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Barra de progreso
                            LinearProgressIndicator(
                                progress = { loadingProgress },
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            
                            Text(
                                text = "Cargando contactos...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            if (totalContacts > 0) {
                                val percentage = (loadingProgress * 100).toInt()
                                Text(
                                    text = "$percentage% • $totalContacts contactos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Lista de contactos
                else if (filteredContacts.isEmpty()) {
                    // Mensaje cuando no hay contactos
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Sin contactos",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "No se encontraron contactos",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Por favor, vuelve a intentar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Reducido de itemSpacing (12dp) a 8dp
                    ) {
                        items(filteredContacts) { contact ->
                            ContactItem(
                                contact = contact,
                                isSelected = selectedContacts.contains(contact),
                                onToggle = { 
                                    if (selectedContacts.contains(contact)) {
                                        selectedContacts = selectedContacts - contact
                                    } else if (selectedContacts.size < maxSelection) {
                                        selectedContacts = selectedContacts + contact
                                    }
                                },
                                canSelect = selectedContacts.size < maxSelection || selectedContacts.contains(contact)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.sectionSpacing))
                
                // Botones de acción mejorados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(DesignTokens.buttonHeight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancelar",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Button(
                        onClick = {
                            onContactsSelected(selectedContacts.toList())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(DesignTokens.buttonHeight),
                        enabled = selectedContacts.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Seleccionar (${selectedContacts.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 📋 ITEM DE CONTACTO
 * 
 * Componente individual para mostrar un contacto en la lista de selección.
 */
@Composable
private fun ContactItem(
    contact: CustomerData,
    isSelected: Boolean,
    onToggle: () -> Unit,
    canSelect: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canSelect) { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(10.dp), // Reducido de 12dp a 10dp
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 1.5.dp, // Reducido de 2dp a 1.5dp
                color = MaterialTheme.colorScheme.primary
            )
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp // Reducido de 4dp/1dp a 2dp/0dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp), // Reducido de cardPadding (16dp) a 12dp horizontal, 10dp vertical
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox compacto
            Checkbox(
                checked = isSelected,
                onCheckedChange = if (canSelect) { _ -> onToggle() } else null,
                enabled = canSelect,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            
            Spacer(modifier = Modifier.width(10.dp)) // Reducido de itemSpacing (12dp) a 10dp
            
            // Avatar más compacto
            Box(
                modifier = Modifier
                    .size(36.dp) // Reducido de 48dp a 36dp
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp), // Reducido de 24dp a 18dp
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp)) // Reducido de itemSpacing (12dp) a 10dp
            
            // Información del contacto - layout más compacto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium, // Reducido de bodyLarge a bodyMedium
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, // Cambiado de Bold/Medium a SemiBold/Normal
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Mostrar teléfono y email en la misma línea si es posible, o en líneas separadas más compactas
                if (!contact.phone.isNullOrBlank() || !contact.email.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp)) // Reducido de 4dp a 2dp
                    
                    if (!contact.phone.isNullOrBlank()) {
                        Text(
                            text = contact.phone,
                            style = MaterialTheme.typography.bodySmall, // Reducido de bodyMedium a bodySmall
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    if (!contact.email.isNullOrBlank() && contact.phone.isNullOrBlank()) {
                        Text(
                            text = contact.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // Indicador de selección más compacto
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    modifier = Modifier.size(20.dp), // Reducido de 24dp a 20dp
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 🎯 HOOK PARA SELECCIÓN DE CONTACTOS
 * 
 * Hook personalizado que maneja la lógica de selección de contactos.
 */
@Composable
fun useContactSelector(
    contacts: List<CustomerData>,
    onContactsSelected: (List<CustomerData>) -> Unit,
    maxSelection: Int = 3
): () -> Unit {
    var showDialog by remember { mutableStateOf(false) }
    
    if (showDialog) {
        ContactSelectorDialog(
            contacts = contacts,
            onDismiss = { showDialog = false },
            onContactsSelected = onContactsSelected,
            maxSelection = maxSelection
        )
    }
    
    return { showDialog = true }
}
