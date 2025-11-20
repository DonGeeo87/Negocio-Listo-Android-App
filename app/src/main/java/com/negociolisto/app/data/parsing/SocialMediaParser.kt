package com.negociolisto.app.data.parsing

import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📱 PARSER DE REDES SOCIALES
 * 
 * Maneja el parsing de información de redes sociales desde JSON
 */
@Singleton
class SocialMediaParser @Inject constructor() {
    
    /**
     * Parsear redes sociales desde JSON
     */
    fun parseSocialMedia(socialMediaJson: Any?): BusinessSocialMedia? {
        if (socialMediaJson == null) return null
        
        return try {
            val json = when (socialMediaJson) {
                is String -> JSONObject(socialMediaJson)
                is JSONObject -> socialMediaJson
                else -> return null
            }
            
            // Helper function para obtener string opcional y filtrar vacíos
            fun getOptionalString(key: String): String? {
                return if (json.has(key) && !json.isNull(key)) {
                    val value = json.optString(key, "")
                    if (value.isNotBlank()) value else null
                } else {
                    null
                }
            }
            
            BusinessSocialMedia(
                instagram = getOptionalString("instagram"),
                facebook = getOptionalString("facebook"),
                twitter = getOptionalString("twitter"),
                linkedin = getOptionalString("linkedin"),
                tiktok = getOptionalString("tiktok"),
                youtube = getOptionalString("youtube"),
                website = getOptionalString("website"),
                whatsapp = getOptionalString("whatsapp"),
                telegram = getOptionalString("telegram")
            )
        } catch (e: Exception) {
            println("❌ Error parseando redes sociales: ${e.message}")
            null
        }
    }
    
    /**
     * Convertir redes sociales a JSON
     */
    fun toJson(socialMedia: BusinessSocialMedia?): JSONObject? {
        if (socialMedia == null) return null
        
        return try {
            JSONObject().apply {
                socialMedia.instagram?.let { put("instagram", it) }
                socialMedia.facebook?.let { put("facebook", it) }
                socialMedia.twitter?.let { put("twitter", it) }
                socialMedia.linkedin?.let { put("linkedin", it) }
                socialMedia.tiktok?.let { put("tiktok", it) }
                socialMedia.youtube?.let { put("youtube", it) }
                socialMedia.website?.let { put("website", it) }
                socialMedia.whatsapp?.let { put("whatsapp", it) }
                socialMedia.telegram?.let { put("telegram", it) }
            }
        } catch (e: Exception) {
            println("❌ Error convirtiendo redes sociales a JSON: ${e.message}")
            null
        }
    }
    
    /**
     * Validar URL de red social
     */
    private fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.startsWith("http://") || url.startsWith("https://") || 
               url.startsWith("@") || url.startsWith("+")
    }
}

/**
 * 📱 REDES SOCIALES DEL NEGOCIO
 */
data class BusinessSocialMedia(
    val instagram: String? = null,
    val facebook: String? = null,
    val twitter: String? = null,
    val linkedin: String? = null,
    val tiktok: String? = null,
    val youtube: String? = null,
    val website: String? = null,
    val whatsapp: String? = null,
    val telegram: String? = null
) {
    /**
     * Obtener lista de redes sociales activas
     */
    fun getActiveSocialMedia(): List<Pair<String, String>> {
        return listOfNotNull(
            instagram?.let { "Instagram" to it },
            facebook?.let { "Facebook" to it },
            twitter?.let { "Twitter" to it },
            linkedin?.let { "LinkedIn" to it },
            tiktok?.let { "TikTok" to it },
            youtube?.let { "YouTube" to it },
            website?.let { "Sitio Web" to it },
            whatsapp?.let { "WhatsApp" to it },
            telegram?.let { "Telegram" to it }
        )
    }
    
    /**
     * Verificar si tiene al menos una red social
     */
    fun hasAnySocialMedia(): Boolean {
        return getActiveSocialMedia().isNotEmpty()
    }
}
