package com.negociolisto.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 🚨 BANNER DE RECORDATORIO DE PERFIL INCOMPLETO
 * 
 * Banner que aparece en MainScreen cuando el perfil no está completo
 * para recordar al usuario que complete su información.
 */
@Composable
fun ProfileReminderBanner(
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showPersonalReminder: Boolean = false,
    showCompanyReminder: Boolean = false
) {
    if (showPersonalReminder || showCompanyReminder) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Completa tu perfil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        val reminderText = when {
                            showPersonalReminder && showCompanyReminder -> 
                                "Agrega tu información personal y empresarial para aprovechar todas las funciones"
                            showPersonalReminder -> 
                                "Agrega tu información personal para una mejor experiencia"
                            showCompanyReminder -> 
                                "Agrega información de tu negocio para funciones avanzadas"
                            else -> ""
                        }
                        
                        Text(
                            reminderText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Completar ahora",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Banner específico para recordatorio de perfil personal
 */
@Composable
fun PersonalProfileReminderBanner(
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileReminderBanner(
        onComplete = onComplete,
        onDismiss = onDismiss,
        modifier = modifier,
        showPersonalReminder = true
    )
}

/**
 * Banner específico para recordatorio de perfil empresarial
 */
@Composable
fun CompanyProfileReminderBanner(
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileReminderBanner(
        onComplete = onComplete,
        onDismiss = onDismiss,
        modifier = modifier,
        showCompanyReminder = true
    )
}

/**
 * Banner que combina ambos recordatorios
 */
@Composable
fun CombinedProfileReminderBanner(
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileReminderBanner(
        onComplete = onComplete,
        onDismiss = onDismiss,
        modifier = modifier,
        showPersonalReminder = true,
        showCompanyReminder = true
    )
}










