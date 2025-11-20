package com.negociolisto.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.negociolisto.app.ui.components.ModernLoadingScreen

@Composable
fun AfterRegisterScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        try {
            // Pequeña espera para simular preparación de cuenta
            delay(1200)
            onContinue()
        } catch (e: Exception) {
            // En caso de error, continuar de todas formas
            onContinue()
        }
    }

    ModernLoadingScreen(
        message = "Preparando tu cuenta...",
        modifier = modifier
    )
}


