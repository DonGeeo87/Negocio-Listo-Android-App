package com.negociolisto.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎨 GESTOR DE TEMAS
 * 
 * Maneja la configuración de temas de la aplicación
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
    
    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_AUTO = "auto"
    }
    
    /**
     * Obtener tema actual
     */
    fun getCurrentTheme(): String {
        return prefs.getString("current_theme", THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    /**
     * Establecer tema
     */
    fun setTheme(theme: String) {
        prefs.edit().putString("current_theme", theme).apply()
    }
    
    /**
     * Obtener esquema de colores personalizado
     */
    fun getCustomColorScheme(): ColorScheme? {
        val primaryColor = prefs.getInt("primary_color", -1)
        val secondaryColor = prefs.getInt("secondary_color", -1)
        val isDark = prefs.getBoolean("is_dark_theme", false)
        
        if (primaryColor == -1 || secondaryColor == -1) return null
        
        return if (isDark) {
            darkColorScheme(
                primary = Color(primaryColor),
                secondary = Color(secondaryColor)
            )
        } else {
            lightColorScheme(
                primary = Color(primaryColor),
                secondary = Color(secondaryColor)
            )
        }
    }
    
    /**
     * Establecer esquema de colores personalizado
     */
    fun setCustomColorScheme(primaryColor: Color, secondaryColor: Color, isDark: Boolean) {
        prefs.edit()
            .putInt("primary_color", primaryColor.toArgb())
            .putInt("secondary_color", secondaryColor.toArgb())
            .putBoolean("is_dark_theme", isDark)
            .apply()
    }
    
    /**
     * Obtener configuración de tema completa
     */
    fun getThemeConfig(): ThemeConfig {
        return ThemeConfig(
            theme = getCurrentTheme(),
            primaryColor = prefs.getInt("primary_color", -1),
            secondaryColor = prefs.getInt("secondary_color", -1),
            isDark = prefs.getBoolean("is_dark_theme", false),
            useSystemTheme = getCurrentTheme() == THEME_SYSTEM
        )
    }
    
    /**
     * Restaurar configuración de tema desde backup
     */
    fun restoreThemeConfig(config: ThemeConfig) {
        prefs.edit()
            .putString("current_theme", config.theme)
            .putInt("primary_color", config.primaryColor)
            .putInt("secondary_color", config.secondaryColor)
            .putBoolean("is_dark_theme", config.isDark)
            .apply()
    }
}

/**
 * 📋 CONFIGURACIÓN DE TEMA
 */
data class ThemeConfig(
    val theme: String,
    val primaryColor: Int,
    val secondaryColor: Int,
    val isDark: Boolean,
    val useSystemTheme: Boolean
)
