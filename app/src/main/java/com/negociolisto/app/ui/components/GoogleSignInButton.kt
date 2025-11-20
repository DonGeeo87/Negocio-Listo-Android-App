package com.negociolisto.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 🔐 BOTÓN DE INICIO DE SESIÓN CON GOOGLE
 * 
 * Componente reutilizable para autenticación con Google
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    text: String = "Continuar con Google",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icono de Google (usando un icono genérico por ahora)
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Google",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF4285F4) // Color azul de Google
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 🔐 BOTÓN DE REGISTRO CON GOOGLE
 * 
 * Componente específico para registro con Google
 */
@Composable
fun GoogleSignUpButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    GoogleSignInButton(
        onClick = onClick,
        text = "Registrarse con Google",
        modifier = modifier,
        enabled = enabled
    )
}

/**
 * 🔐 BOTÓN DE INICIO DE SESIÓN CON GOOGLE
 * 
 * Componente específico para inicio de sesión con Google
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    GoogleSignInButton(
        onClick = onClick,
        text = "Iniciar sesión con Google",
        modifier = modifier,
        enabled = enabled
    )
}

/**
 * 📱 TARJETA DE AUTENTICACIÓN CON GOOGLE
 * 
 * Tarjeta completa con opciones de autenticación con Google
 */
@Composable
fun GoogleAuthCard(
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Acceso Rápido",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Descripción
            Text(
                text = "Usa tu cuenta de Google para acceder rápidamente y sincronizar tus datos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // Botones
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GoogleSignInButton(
                    onClick = onSignInClick,
                    enabled = !isLoading
                )
                
                GoogleSignUpButton(
                    onClick = onSignUpClick,
                    enabled = !isLoading
                )
            }
            
            // Indicador de carga
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Procesando...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}