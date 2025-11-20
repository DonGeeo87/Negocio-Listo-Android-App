package com.negociolisto.app.ui.design

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.negociolisto.app.ui.components.UnifiedPrimaryButton
import com.negociolisto.app.ui.components.UnifiedOutlineButton

/**
 * 📝 COMPONENTES DE FORMULARIOS UNIFICADOS
 * 
 * Componentes específicos para formularios que mantienen consistencia
 * visual y de comportamiento en toda la aplicación.
 */

// 📝 FORMULARIO BASE
@Composable
fun UnifiedForm(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header del formulario
        FormHeader(
            title = title,
            subtitle = subtitle,
            onBackClick = onBackClick,
            actions = actions
        )
        
        // Contenido del formulario
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
        ) {
            content()
        }
    }
}

// 📋 HEADER DE FORMULARIO
@Composable
private fun FormHeader(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(DesignTokens.cardElevation, DesignTokens.cardShape),
        shape = DesignTokens.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    onBackClick?.let {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                actions?.invoke()
            }
            subtitle?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 📝 GRUPO DE CAMPOS
@Composable
fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DesignTokens.itemSpacing)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        content()
    }
}

// 🔢 CAMPO NUMÉRICO
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: ImageVector? = null,
    isEnabled: Boolean = true,
    prefix: String? = null,
    suffix: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            enabled = isEnabled,
            leadingIcon = leadingIcon?.let { 
                { Icon(imageVector = it, contentDescription = null) }
            },
            prefix = prefix?.let { { Text(it) } },
            suffix = suffix?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = DesignTokens.buttonShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )
        errorMessage?.let { message ->
            if (isError) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// 📧 CAMPO DE EMAIL
@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Email",
    modifier: Modifier = Modifier,
    placeholder: String? = "ejemplo@correo.com",
    isError: Boolean = false,
    errorMessage: String? = null,
    isEnabled: Boolean = true
) {
    UnifiedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        leadingIcon = Icons.Filled.Email,
        isEnabled = isEnabled,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
        ),
        modifier = modifier
    )
}

// 🔒 CAMPO DE CONTRASEÑA
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Contraseña",
    modifier: Modifier = Modifier,
    placeholder: String? = "Ingresa tu contraseña",
    isError: Boolean = false,
    errorMessage: String? = null,
    isEnabled: Boolean = true
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    UnifiedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        leadingIcon = Icons.Filled.Lock,
        isEnabled = isEnabled,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
        ),
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        }
    )
}

// 📝 CAMPO DE TEXTO LARGO
@Composable
fun TextAreaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    isEnabled: Boolean = true,
    maxLines: Int = 4
) {
    UnifiedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        isEnabled = isEnabled,
        singleLine = false,
        modifier = modifier
    )
}

// 🎯 SELECTOR DROPDOWN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownField(
    value: T?,
    onValueChange: (T) -> Unit,
    label: String,
    options: List<T>,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    isEnabled: Boolean = true,
    itemContent: @Composable (T) -> Unit = { Text(it.toString()) }
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value?.toString() ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text(label) },
                placeholder = placeholder?.let { { Text(it) } },
                isError = isError,
                enabled = isEnabled,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = DesignTokens.buttonShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { itemContent(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        errorMessage?.let { message ->
            if (isError) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// 📅 SELECTOR DE FECHA
@Composable
fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Fecha",
    modifier: Modifier = Modifier,
    placeholder: String? = "DD/MM/AAAA",
    isError: Boolean = false,
    errorMessage: String? = null,
    isEnabled: Boolean = true
) {
    UnifiedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        leadingIcon = Icons.Filled.CalendarToday,
        isEnabled = isEnabled,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        ),
        modifier = modifier
    )
}

// 🏷️ CAMPO DE TAGS
@Composable
fun TagsField(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    label: String = "Etiquetas",
    modifier: Modifier = Modifier,
    placeholder: String? = "Agregar etiqueta",
    isEnabled: Boolean = true
) {
    var newTag by remember { mutableStateOf("") }
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Tags existentes
        if (tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(tags) { tag ->
                    UnifiedChip(
                        text = tag,
                        onClick = { onTagsChange(tags.filter { it != tag }) },
                        isSelected = false,
                        isEnabled = isEnabled
                    )
                }
            }
        }
        
        // Campo para agregar nuevo tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                placeholder = { Text(placeholder ?: "Agregar etiqueta") },
                enabled = isEnabled,
                modifier = Modifier.weight(1f),
                shape = DesignTokens.buttonShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (newTag.isNotBlank() && !tags.contains(newTag)) {
                        onTagsChange(tags + newTag)
                        newTag = ""
                    }
                },
                enabled = isEnabled && newTag.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar etiqueta",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// 🎯 SISTEMA DE TABS PARA FORMULARIOS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedTabForm(
    tabs: List<TabData>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TabRow
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = { 
                        Text(
                            text = tab.title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        ) 
                    },
                    icon = tab.icon?.let { { Icon(imageVector = it, contentDescription = null) } }
                )
            }
        }
        
        // Contenido del tab seleccionado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            content(selectedTab)
        }
    }
}

// 🧙‍♂️ SISTEMA DE WIZARD PARA FORMULARIOS COMPLEJOS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedWizardForm(
    steps: List<WizardStep>,
    currentStep: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onFinish: () -> Unit,
    canProceed: Boolean = true,
    content: @Composable (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Indicador de progreso
        LinearProgressIndicator(
            progress = (currentStep + 1).toFloat() / steps.size,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        // Header del wizard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.cardPadding)
            ) {
                Text(
                    text = "Paso ${currentStep + 1} de ${steps.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = steps[currentStep].title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                steps[currentStep].description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Contenido del paso actual
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            content(currentStep)
        }
        
        // Navegación del wizard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón anterior
            if (currentStep > 0) {
                UnifiedOutlineButton(
                    text = "Anterior",
                    onClick = onPrevious,
                    icon = Icons.AutoMirrored.Filled.ArrowBack
                )
            } else {
                Spacer(modifier = Modifier.width(120.dp))
            }
            
            // Botón siguiente/finalizar
            if (currentStep < steps.size - 1) {
                UnifiedPrimaryButton(
                    text = "Siguiente",
                    onClick = onNext,
                    enabled = canProceed,
                    icon = Icons.AutoMirrored.Filled.ArrowForward
                )
            } else {
                UnifiedPrimaryButton(
                    text = "Finalizar",
                    onClick = onFinish,
                    enabled = canProceed,
                    icon = Icons.Filled.Check
                )
            }
        }
    }
}

// 📊 DATOS PARA TABS
data class TabData(
    val title: String,
    val icon: ImageVector? = null
)

// 📋 DATOS PARA WIZARD STEPS
data class WizardStep(
    val title: String,
    val description: String? = null,
    val icon: ImageVector? = null
)