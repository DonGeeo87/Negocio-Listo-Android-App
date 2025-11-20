package com.negociolisto.app.ui.free_tools

import android.content.Context
import android.net.Uri
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * 📚 REPOSITORIO DE HERRAMIENTAS GRATUITAS
 * 
 * Carga y gestiona el catálogo de herramientas desde assets.
 */
object FreeToolsRepository {

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }
    }

    /**
     * 📂 CARGAR DESDE ASSETS
     * 
     * Lee el archivo JSON de herramientas desde assets.
     */
    fun loadFromAssets(context: Context, assetPath: String = "free_tools.json"): List<FreeTool> {
        val text = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }

    /**
     * 🌐 OBTENER URL DEL ICONO
     * 
     * Construye la URL del icono según su tipo.
     */
    fun iconUrlFor(icon: ToolIcon, fallbackDomain: String? = null): String =
        when (icon.type.lowercase()) {
            "simpleicons" ->
                "https://cdn.simpleicons.org/${icon.slug ?: "android"}"
            "googlefavicon" ->
                "https://www.google.com/s2/favicons?sz=128&domain=${icon.domain ?: (fallbackDomain ?: "example.com")}"
            else ->
                "https://www.google.com/s2/favicons?sz=128&domain=${fallbackDomain ?: "example.com"}"
        }

    /**
     * 🔍 EXTRAER DOMINIO DE URL
     * 
     * Obtiene el dominio de una URL para usarlo como fallback.
     */
    fun domainFromUrl(url: String): String? =
        runCatching { Uri.parse(url).host }.getOrNull()
}

