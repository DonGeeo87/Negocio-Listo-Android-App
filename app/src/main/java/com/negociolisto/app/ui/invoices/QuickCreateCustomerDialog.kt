package com.negociolisto.app.ui.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.domain.model.Customer
import com.negociolisto.app.ui.design.UnifiedTextField
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.negociolisto.app.ui.customers.CustomerViewModel
import java.util.UUID

/**
 * 🚀 DIÁLOGO DE CREACIÓN RÁPIDA DE CLIENTE
 * 
 * Permite crear un cliente rápidamente desde la pantalla de creación de facturas
 */
@Composable
fun QuickCreateCustomerDialog(
    onDismiss: () -> Unit,
    onCustomerCreated: (Customer) -> Unit,
    customersViewModel: CustomerViewModel
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🚀 Crear Cliente Rápido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Crea un cliente rápidamente para continuar con la factura",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                UnifiedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre del Cliente *",
                    modifier = Modifier.fillMaxWidth()
                )
                
                UnifiedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Teléfono",
                    modifier = Modifier.fillMaxWidth()
                )
                
                UnifiedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        isCreating = true
                        val customer = Customer(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            phone = phone.takeIf { it.isNotBlank() },
                            email = email.takeIf { it.isNotBlank() },
                            address = null,
                            companyName = null,
                            totalPurchases = 0.0,
                            lastPurchaseDate = null,
                            notes = "Cliente creado desde factura",
                            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        )
                        
                        customersViewModel.addCustomer(customer)
                        onCustomerCreated(customer)
                    }
                },
                enabled = name.isNotBlank() && !isCreating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Crear Cliente")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
