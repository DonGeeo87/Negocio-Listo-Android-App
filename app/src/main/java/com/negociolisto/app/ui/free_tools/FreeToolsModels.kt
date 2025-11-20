package com.negociolisto.app.ui.free_tools

import kotlinx.serialization.Serializable

/**
 * 🛠️ MODELOS DE HERRAMIENTAS GRATUITAS
 * 
 * Esta clase representa una herramienta gratuita o recurso útil
 * para el negocio del usuario.
 */
@Serializable
data class FreeTool(
    val name: String,
    val description: String = "", // Descripción breve de la herramienta (10-15 palabras)
    val category: String,
    val url: String,
    val icon: ToolIcon,
    val pricing: String? = null // "free" | "free-tier" | "paid" (opcional)
)

/**
 * 🎨 ICONO DE HERRAMIENTA
 * 
 * Soporta dos tipos de iconos:
 * - Simple Icons: SVGs desde CDN
 * - Google Favicons: Favicons de sitios web
 */
@Serializable
data class ToolIcon(
    val type: String,      // "simpleicons" | "googlefavicon"
    val slug: String? = null,
    val domain: String? = null
)

