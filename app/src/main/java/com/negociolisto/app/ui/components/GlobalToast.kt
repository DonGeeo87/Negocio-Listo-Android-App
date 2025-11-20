package com.negociolisto.app.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 🍞 TOAST GLOBAL
 * 
 * Componente que muestra las notificaciones toast globales de la aplicación.
 * Se debe incluir en la pantalla principal de la app.
 */
@Composable
fun GlobalToast(
    modifier: Modifier = Modifier,
    viewModel: ToastViewModel = hiltViewModel()
) {
    val toastData by viewModel.toastMessage.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Observar el ciclo de vida para ocultar el toast cuando la pantalla se pause
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.hideToast()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Mostrar el toast si hay datos
    toastData?.let { data ->
        ToastNotification(
            message = data.message,
            type = data.type,
            duration = data.duration,
            onDismiss = { viewModel.hideToast() },
            modifier = modifier
        )
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Global Component**: Componente que se incluye una vez en la app
 * 2. **Lifecycle Aware**: Se oculta automáticamente cuando la pantalla se pausa
 * 3. **State Collection**: Observa el estado del ViewModel
 * 4. **Auto-cleanup**: Limpia recursos cuando se destruye
 * 5. **Hilt Integration**: Integrado con el sistema de inyección de dependencias
 * 
 * ANALOGÍA:
 * 
 * GlobalToast es como un "sistema de notificaciones" en el sistema operativo:
 * 
 * 1. **Siempre presente**: Está disponible en toda la app
 * 2. **Centralizado**: Un solo lugar para mostrar notificaciones
 * 3. **Inteligente**: Se oculta cuando no es necesario
 * 4. **No intrusivo**: No interfiere con la funcionalidad normal
 * 5. **Configurable**: Se adapta al contexto de la app
 * 
 * USO EN LA APP:
 * ```kotlin
 * @Composable
 * fun MainScreen() {
 *     // ... contenido de la pantalla principal
 *     
 *     // Incluir al final para que aparezca sobre todo
 *     GlobalToast()
 * }
 * ```
 * 
 * CARACTERÍSTICAS:
 * ✅ Integrado con Hilt
 * ✅ Consciente del ciclo de vida
 * ✅ Auto-limpieza de recursos
 * ✅ No bloquea la UI
 * ✅ Fácil de integrar
 * ✅ Manejo automático del estado
 */
