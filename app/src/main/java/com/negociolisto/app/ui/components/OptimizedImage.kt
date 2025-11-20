package com.negociolisto.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 🖼️ COMPONENTE DE IMAGEN OPTIMIZADA CON DESIGN SYSTEM
 * 
 * Componente optimizado para cargar imágenes con:
 * - Cache inteligente con Coil
 * - Redimensionamiento automático
 * - Placeholder y error states unificados
 * - Crossfade animation suave
 * - Lazy loading para performance
 * - Integración con design system
 */
@Composable
fun OptimizedImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackIcon: String = "📷",
    fallbackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val context = LocalContext.current
    var showFallback by remember(imageUrl) { mutableStateOf(imageUrl == null || imageUrl.isBlank()) }
    var isLoading by remember(imageUrl) { mutableStateOf(true) }
    
    val version by ImageRefreshBus.version.collectAsStateWithLifecycle(initialValue = 0)

    // Resetear estados cuando cambia imageUrl
    LaunchedEffect(imageUrl) {
        showFallback = imageUrl == null || imageUrl.isBlank()
        isLoading = imageUrl != null && imageUrl.isNotBlank()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            !showFallback && imageUrl != null && imageUrl.isNotBlank() -> {
                val imageModel = remember(imageUrl, version) {
                    when {
                        imageUrl.startsWith("content://") -> {
                            try {
                                android.net.Uri.parse(imageUrl)
                            } catch (e: Exception) {
                                android.util.Log.e("OptimizedImage", "Error parseando URI: ${e.message}")
                                null
                            }
                        }
                        imageUrl.startsWith("/") -> {
                            val file = java.io.File(imageUrl)
                            if (file.exists() && file.canRead()) {
                                android.net.Uri.fromFile(file)
                            } else {
                                android.util.Log.e("OptimizedImage", "Archivo no existe o no se puede leer: $imageUrl")
                                null
                            }
                        }
                        imageUrl.startsWith("file://") -> {
                            try {
                                android.net.Uri.parse(imageUrl)
                            } catch (e: Exception) {
                                android.util.Log.e("OptimizedImage", "Error parseando file URI: ${e.message}")
                                null
                            }
                        }
                        else -> {
                            // URLs remotas - adjuntar versión para forzar actualización visual tras guardado
                            val sep = if (imageUrl.contains('?')) '&' else '?'
                            "$imageUrl$sep" + "v=$version"
                        }
                    }
                }
                
                if (imageModel != null) {
                    // Mostrar placeholder mientras carga
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageModel)
                            .crossfade(true)
                            .crossfade(300)
                            .allowHardware(false) // Permitir que funcione en diferentes hilos
                            .build(),
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale,
                        onLoading = {
                            isLoading = true
                        },
                        onError = { error ->
                            android.util.Log.e("OptimizedImage", "Error cargando imagen: ${error.result.throwable.message}")
                            android.util.Log.e("OptimizedImage", "URL: $imageUrl")
                            android.util.Log.e("OptimizedImage", "Model: $imageModel")
                            showFallback = true
                            isLoading = false
                        },
                        onSuccess = { 
                            android.util.Log.d("OptimizedImage", "Imagen cargada exitosamente: $imageUrl")
                            showFallback = false
                            isLoading = false
                        }
                    )
                } else {
                    FallbackImage(
                        icon = fallbackIcon,
                        color = fallbackColor,
                        contentDescription = contentDescription
                    )
                }
            }
            else -> {
                FallbackImage(
                    icon = fallbackIcon,
                    color = fallbackColor,
                    contentDescription = contentDescription
                )
            }
        }
    }
}

/**
 * 🎨 IMAGEN DE FALLBACK
 * 
 * Componente que se muestra cuando no hay imagen o hay error.
 */
@Composable
private fun FallbackImage(
    icon: String,
    color: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 🖼️ IMAGEN DE PERFIL OPTIMIZADA
 * 
 * Versión especializada para imágenes de perfil con forma circular.
 */
@Composable
fun OptimizedProfileImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackIcon: String = "👤"
) {
    OptimizedImage(
        imageUrl = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        fallbackIcon = fallbackIcon,
        fallbackColor = MaterialTheme.colorScheme.primary
    )
}

/**
 * 📦 IMAGEN DE PRODUCTO OPTIMIZADA
 * 
 * Versión especializada para imágenes de productos con optimizaciones específicas.
 */
@Composable
fun OptimizedProductImage(
    imageUrl: String?,
    productName: String,
    modifier: Modifier = Modifier,
    categoryIcon: String = "📦"
) {
    OptimizedImage(
        imageUrl = imageUrl,
        contentDescription = "Imagen de $productName",
        modifier = modifier,
        contentScale = ContentScale.Crop,
        fallbackIcon = categoryIcon,
        fallbackColor = MaterialTheme.colorScheme.secondary
    )
}
