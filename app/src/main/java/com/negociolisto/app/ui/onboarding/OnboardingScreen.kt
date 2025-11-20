package com.negociolisto.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.negociolisto.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import com.negociolisto.app.ui.design.DesignTokens
import com.negociolisto.app.ui.design.AnimationTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    // Ocultar topbar del sistema para pantalla completa
    LaunchedEffect(Unit) {
        // Esto se ejecutará cuando se monte el composable
    }
    val pagerState = rememberPagerState(pageCount = { 3 })
    var currentPage by remember { mutableStateOf(0) }
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val slideInOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = tween(AnimationTokens.longDuration, easing = AnimationTokens.decelerateEasing),
        label = "slideIn"
    )
    
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(AnimationTokens.extraLongDuration, easing = AnimationTokens.decelerateEasing),
        label = "fadeIn"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .offset(y = slideInOffset)
            .alpha(fadeInAlpha)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button en la esquina superior derecha (sin topbar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.cardPadding),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        viewModel.completeOnboarding()
                        onComplete()
                    }
                ) {
                    Text(
                        text = "Omitir",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Pager con slides
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingSlide(
                    slide = OnboardingSlides[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Indicadores de página
            Row(
                modifier = Modifier.padding(DesignTokens.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1f,
                        animationSpec = tween(200),
                        label = "indicator_scale"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .scale(scale)
                            .background(
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Botones de navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val scope = rememberCoroutineScope()
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text("Anterior")
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                Button(
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage < 2) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                viewModel.completeOnboarding()
                                onComplete()
                            }
                        }
                    },
                    modifier = Modifier.height(DesignTokens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < 2) "Siguiente" else "Comenzar",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingSlide(
    slide: OnboardingSlideData,
    modifier: Modifier = Modifier
) {
    // Usar AnimatedVisibility para cada elemento
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(slide.title) {
        isVisible = false
        delay(100)
        isVisible = true
    }
    
    val iconScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )
    
    val iconRotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconRotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignTokens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono animado con gradiente (más pequeño)
        Card(
            modifier = Modifier
                .size(100.dp)
                .scale(iconScale)
                .graphicsLayer {
                    rotationZ = iconRotation
                },
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(slide.gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = slide.title,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Título animado
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(AnimationTokens.longDuration, delayMillis = 200, easing = AnimationTokens.decelerateEasing)
            ) + fadeIn(
                animationSpec = tween(AnimationTokens.longDuration, delayMillis = 200, easing = AnimationTokens.decelerateEasing)
            )
        ) {
            Text(
                text = slide.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Descripción animada
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(AnimationTokens.longDuration, delayMillis = 400, easing = AnimationTokens.decelerateEasing)
            ) + fadeIn(
                animationSpec = tween(AnimationTokens.longDuration, delayMillis = 400, easing = AnimationTokens.decelerateEasing)
            )
        ) {
            Text(
                text = slide.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Características animadas
        slide.features.forEachIndexed { index, feature ->
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { it * (index + 1) },
                    animationSpec = tween(AnimationTokens.longDuration, delayMillis = 600 + (index * 100), easing = AnimationTokens.decelerateEasing)
                ) + fadeIn(animationSpec = tween(AnimationTokens.longDuration, delayMillis = 600 + (index * 100)))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(DesignTokens.mediumIconSize),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

data class OnboardingSlideData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: Brush,
    val features: List<String>
)

private val OnboardingSlides = listOf(
    OnboardingSlideData(
        title = "Tu negocio, tu manera",
        description = "Para cualquier persona que quiera organizar y tener una vida directa de venta con sus clientes. Herramienta de uso diario, totalmente gratis y sin depender de un equipo de personas.",
        icon = Icons.Filled.Person,
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF667eea),
                Color(0xFF764ba2)
            )
        ),
        features = listOf(
            "💚 100% Gratis",
            "👤 Sin necesidad de equipo",
            "📱 Herramienta de uso diario",
            "🚀 Pensada por un emprendedor para otros emprendedores"
        )
    ),
    OnboardingSlideData(
        title = "Colecciones que venden solas",
        description = "Sistema de colecciones con creación automática del portal web del cliente. Tus clientes pueden ver tus productos, hacer pedidos online y gestionar sus colecciones, todo sin complicaciones.",
        icon = Icons.Filled.ShoppingCart,
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF4facfe),
                Color(0xFF00f2fe)
            )
        ),
        features = listOf(
            "🌐 Portal web automático para cada cliente",
            "📦 Colecciones personalizadas",
            "🛒 Pedidos online sin complicaciones",
            "✨ Sencillez y automatización"
        )
    ),
    OnboardingSlideData(
        title = "Listo para empezar",
        description = "Evita herramientas de alto costo o gran complejidad. NegocioListo es simple, directo y diseñado para que cualquier emprendedor pueda gestionar su negocio con facilidad.",
        icon = Icons.Filled.CheckCircle,
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF43e97b),
                Color(0xFF38f9d7)
            )
        ),
        features = listOf(
            "✅ Simple y directo",
            "💰 Sin costos ocultos",
            "🎯 Enfocado en emprendedores",
            "💪 Empieza a vender hoy mismo"
        )
    )
)

