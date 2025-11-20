package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * 👤 MODELO DE USUARIO
 * 
 * Esta clase representa a un usuario de nuestra aplicación NegocioListo.
 * Es como la "cédula de identidad digital" de cada persona que usa la app.
 * 
 * Un usuario es el dueño del negocio que:
 * - Maneja sus productos, clientes y ventas
 * - Accede a reportes y estadísticas
 * - Configura su perfil y preferencias
 * - Sincroniza datos en la nube (opcional)
 */
data class User(
    /**
     * 🆔 ID ÚNICO DEL USUARIO
     * 
     * Identificador único que nunca cambia.
     * Puede ser generado localmente o venir de Firebase.
     */
    val id: String,
    
    /**
     * 👤 NOMBRE COMPLETO
     * 
     * El nombre real del usuario, como aparecerá en la app.
     * Ejemplo: "Juan Carlos Pérez", "María Empresa S.A.S."
     */
    val name: String,
    
    /**
     * 📧 EMAIL
     * 
     * Dirección de correo electrónico única del usuario.
     * Se usa para login y comunicaciones importantes.
     */
    val email: String,
    
    /**
     * 📞 TELÉFONO (OPCIONAL)
     * 
     * Número de contacto del usuario.
     * Útil para recuperación de cuenta y notificaciones.
     */
    val phone: String?,
    
    /**
     * 🏢 NOMBRE DEL NEGOCIO (OPCIONAL)
     * 
     * El nombre comercial del negocio que maneja.
     * Ejemplo: "Tienda La Esperanza", "Restaurante El Buen Sabor"
     */
    val businessName: String?,
    
    /**
     * 📂 TIPO DE NEGOCIO (OPCIONAL)
     * 
     * Categoría del negocio para personalizar la experiencia.
     */
    val businessType: BusinessType?,
    
    /**
     * 🆔 RUT DE LA EMPRESA (OPCIONAL)
     * 
     * RUT o identificación fiscal de la empresa.
     * Ejemplo: "12.345.678-9", "12345678-9"
     */
    val businessRut: String?,
    
    /**
     * 📍 DIRECCIÓN DE LA EMPRESA (OPCIONAL)
     * 
     * Dirección física del negocio.
     * Ejemplo: "Av. Principal 123, Santiago, Chile"
     */
    val businessAddress: String?,
    
    /**
     * 📝 DESCRIPCIÓN DE LA EMPRESA (OPCIONAL)
     * 
     * Breve reseña o lema del negocio para mostrar en documentos.
     */
    val businessDescription: String? = null,
    
    /**
     * 📞 TELÉFONO DE LA EMPRESA (OPCIONAL)
     * 
     * Número de contacto del negocio.
     * Ejemplo: "+56 9 1234 5678"
     */
    val businessPhone: String?,
    
    /**
     * 📧 CORREO DE LA EMPRESA (OPCIONAL)
     * 
     * Email de contacto del negocio.
     * Puede ser diferente al email personal del usuario.
     * Ejemplo: "ventas@miempresa.cl", "contacto@negocio.com"
     */
    val businessEmail: String?,
    
    /**
     * 🌐 REDES SOCIALES DE LA EMPRESA (OPCIONAL)
     * 
     * URLs de las redes sociales del negocio.
     */
    val businessSocialMedia: BusinessSocialMedia?,
    
    /**
     * 🖼️ URL DE LOGO DE EMPRESA (OPCIONAL)
     * 
     * Dirección donde está guardado el logo de la empresa.
     * Puede ser local o en la nube.
     */
    val businessLogoUrl: String?,
    
    /**
     * 🖼️ URL DE FOTO DE PERFIL (OPCIONAL)
     * 
     * Dirección donde está guardada la foto del usuario.
     * Puede ser local o en la nube.
     */
    val profilePhotoUrl: String?,
    
    /**
     * ✅ SI EL EMAIL ESTÁ VERIFICADO
     * 
     * Indica si el usuario confirmó su email.
     * Importante para funciones como recuperar contraseña.
     */
    val isEmailVerified: Boolean,
    
    /**
     * 📅 FECHA DE REGISTRO
     * 
     * Cuándo se creó la cuenta del usuario.
     */
    val createdAt: LocalDateTime?,
    
    /**
     * 🔄 FECHA DE ÚLTIMA ACTUALIZACIÓN
     * 
     * Cuándo se modificó el perfil por última vez.
     */
    val updatedAt: LocalDateTime?,
    
    /**
     * 🔄 FECHA DE ÚLTIMO LOGIN
     * 
     * Cuándo se conectó por última vez.
     */
    val lastLoginAt: LocalDateTime?,
    
    /**
     * 🌐 SI USA SINCRONIZACIÓN EN LA NUBE
     * 
     * Si el usuario tiene habilitada la sincronización con Firebase.
     */
    val isCloudSyncEnabled: Boolean,
    
    /**
     * ⚙️ PREFERENCIAS DEL USUARIO
     * 
     * Configuraciones personalizadas de la app.
     */
    val preferences: UserPreferences
) {
    
    /**
     * ✅ VERIFICAR SI EL PERFIL ESTÁ COMPLETO
     * 
     * Un perfil completo tiene toda la información básica necesaria.
     * Útil para mostrar sugerencias de completar perfil.
     * 
     * @return true si el perfil tiene información completa
     */
    fun hasCompleteProfile(): Boolean {
        return name.isNotBlank() && 
               email.isNotBlank() && 
               businessName != null && 
               businessType != null
    }
    
    /**
     * 🆕 VERIFICAR SI ES USUARIO NUEVO
     * 
     * Un usuario nuevo es alguien que se registró recientemente.
     * Útil para mostrar tutoriales o tours de la app.
     * 
     * @param currentDate La fecha actual para comparar
     * @return true si se registró hace menos de 7 días
     */
    fun isNewUser(currentDate: LocalDateTime): Boolean {
        // Simplificado - en una app real usaríamos cálculos de fecha más robustos
        val createdAt = this.createdAt ?: return false
        val daysSinceRegistration = currentDate.dayOfYear - createdAt.dayOfYear
        return daysSinceRegistration <= 7
    }
    
    /**
     * 📱 OBTENER NOMBRE PARA MOSTRAR
     * 
     * Devuelve el mejor nombre para mostrar en la UI.
     * Prioriza el nombre del negocio si existe.
     * 
     * @return El nombre más apropiado para mostrar
     */
    fun getDisplayName(): String {
        return when {
            businessName != null && businessName.isNotBlank() -> businessName
            name.isNotBlank() -> name
            else -> email.substringBefore("@") // Como último recurso, usar parte del email
        }
    }
    
    /**
     * 🔐 VERIFICAR SI NECESITA VERIFICAR EMAIL
     * 
     * Determina si debemos mostrar avisos para verificar el email.
     * 
     * @return true si el email no está verificado
     */
    fun needsEmailVerification(): Boolean {
        return !isEmailVerified
    }
    
    /**
     * ⚙️ VERIFICAR SI PUEDE USAR FUNCIONES PREMIUM
     * 
     * Algunas funciones requieren email verificado o perfil completo.
     * 
     * @return true si puede acceder a todas las funciones
     */
    fun canUsePremiumFeatures(): Boolean {
        return isEmailVerified && hasCompleteProfile()
    }
}

/**
 * 📝 DATOS PARA REGISTRO DE USUARIO
 * 
 * Esta clase contiene la información mínima necesaria para crear una cuenta.
 * Es como el "formulario de registro" que llena el usuario.
 */
data class UserRegistration(
    /**
     * 👤 NOMBRE COMPLETO
     */
    val name: String,
    
    /**
     * 📧 EMAIL
     */
    val email: String,
    
    /**
     * 🔐 CONTRASEÑA
     */
    val password: String,
    
    /**
     * 📞 TELÉFONO (OPCIONAL)
     */
    val phone: String? = null,
    
    /**
     * 🏢 NOMBRE DEL NEGOCIO (OPCIONAL)
     */
    val businessName: String? = null,
    
    /**
     * 📂 TIPO DE NEGOCIO (OPCIONAL)
     */
    val businessType: BusinessType? = null
) {
    
    /**
     * ✅ VALIDAR DATOS DE REGISTRO
     * 
     * Verifica que todos los datos obligatorios estén presentes y sean válidos.
     * 
     * @return Lista de errores encontrados (vacía si todo está bien)
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("El nombre es obligatorio")
        }
        
        if (email.isBlank()) {
            errors.add("El email es obligatorio")
        } else if (!email.contains("@")) {
            errors.add("El email no tiene formato válido")
        }
        
        if (password.length < 6) {
            errors.add("La contraseña debe tener al menos 6 caracteres")
        }
        
        return errors
    }
    
    /**
     * ✅ VERIFICAR SI LOS DATOS SON VÁLIDOS
     * 
     * @return true si no hay errores de validación
     */
    fun isValid(): Boolean {
        return validate().isEmpty()
    }
}

/**
 * 🏢 TIPOS DE NEGOCIO
 * 
 * Enum que define los diferentes tipos de negocio que maneja la app.
 * Cada tipo puede tener configuraciones y funciones específicas.
 */
enum class BusinessType(
    val displayName: String,
    val icon: String,
    val description: String
) {
    RETAIL("Tienda/Retail", "🛍️", "Venta de productos al por menor"),
    RESTAURANT("Restaurante", "🍽️", "Servicio de comidas y bebidas"),
    SERVICES("Servicios", "🔧", "Prestación de servicios profesionales"),
    WHOLESALE("Mayorista", "📦", "Venta al por mayor"),
    MANUFACTURING("Manufactura", "🏭", "Producción y fabricación"),
    AGRICULTURE("Agricultura", "🌾", "Producción agrícola y ganadera"),
    TECHNOLOGY("Tecnología", "💻", "Servicios y productos tecnológicos"),
    HEALTH("Salud", "🏥", "Servicios de salud y bienestar"),
    EDUCATION("Educación", "📚", "Servicios educativos y formación"),
    TRANSPORTATION("Transporte", "🚛", "Servicios de transporte y logística"),
    CONSTRUCTION("Construcción", "🏗️", "Construcción y obras civiles"),
    BEAUTY("Belleza", "💄", "Servicios de belleza y cuidado personal"),
    FITNESS("Fitness", "💪", "Gimnasios y entrenamiento físico"),
    ENTERTAINMENT("Entretenimiento", "🎭", "Entretenimiento y eventos"),
    OTHER("Otro", "📋", "Otro tipo de negocio");
    
    /**
     * 📊 OBTENER CATEGORÍAS SUGERIDAS DE GASTOS
     * 
     * Cada tipo de negocio tiene gastos típicos diferentes.
     * 
     * @return Lista de categorías de gastos relevantes para este negocio
     */
    fun getSuggestedExpenseCategories(): List<String> {
        return when (this) {
            RETAIL -> listOf("INVENTORY", "RENT", "UTILITIES", "MARKETING", "EQUIPMENT")
            RESTAURANT -> listOf("INVENTORY", "RENT", "UTILITIES", "SALARIES", "EQUIPMENT", "SUPPLIES")
            SERVICES -> listOf("OFFICE_SUPPLIES", "PROFESSIONAL_SERVICES", "MARKETING", "TRANSPORTATION")
            WHOLESALE -> listOf("INVENTORY", "TRANSPORTATION", "EQUIPMENT", "INSURANCE")
            MANUFACTURING -> listOf("SUPPLIES", "EQUIPMENT", "UTILITIES", "SALARIES", "MAINTENANCE")
            else -> listOf("RENT", "UTILITIES", "OFFICE_SUPPLIES", "MARKETING", "EQUIPMENT")
        }
    }
}

/**
 * ⚙️ PREFERENCIAS DEL USUARIO
 * 
 * Configuraciones personalizadas que el usuario puede cambiar.
 */
data class UserPreferences(
    /**
     * 🌙 TEMA OSCURO HABILITADO
     */
    val isDarkThemeEnabled: Boolean = false,
    
    /**
     * 🔔 NOTIFICACIONES HABILITADAS
     */
    val areNotificationsEnabled: Boolean = true,
    
    /**
     * 💰 MONEDA PREFERIDA
     */
    val preferredCurrency: String = "CLP", // Pesos chilenos por defecto
    
    /**
     * 🌐 IDIOMA PREFERIDO
     */
    val preferredLanguage: String = "es", // Español por defecto
    
    /**
     * 📊 MOSTRAR DASHBOARD AVANZADO
     */
    val showAdvancedDashboard: Boolean = false,
    
    /**
     * 🔄 SINCRONIZACIÓN AUTOMÁTICA
     */
    val autoSyncEnabled: Boolean = true,
    
    /**
     * ⚠️ ALERTAS DE STOCK BAJO HABILITADAS
     */
    val lowStockAlertsEnabled: Boolean = true,
    
    /**
     * 📱 USAR BIOMETRÍA PARA LOGIN
     */
    val biometricLoginEnabled: Boolean = false
) {
    
    /**
     * 💱 OBTENER SÍMBOLO DE MONEDA
     * 
     * @return El símbolo de la moneda preferida
     */
    fun getCurrencySymbol(): String {
        return when (preferredCurrency) {
            "COP" -> "$"
            "USD" -> "US$"
            "EUR" -> "€"
            else -> "$"
        }
    }
    
    /**
     * 🌐 OBTENER NOMBRE DEL IDIOMA
     * 
     * @return El nombre del idioma en español
     */
    fun getLanguageName(): String {
        return when (preferredLanguage) {
            "es" -> "Español"
            "en" -> "English"
            "pt" -> "Português"
            else -> "Español"
        }
    }
}

/**
 * 🌐 REDES SOCIALES DE LA EMPRESA
 * 
 * Clase que contiene las URLs de las redes sociales del negocio.
 */
data class BusinessSocialMedia(
    /**
     * 📘 FACEBOOK
     */
    val facebook: String? = null,
    
    /**
     * 📷 INSTAGRAM
     */
    val instagram: String? = null,
    
    /**
     * 🐦 TWITTER/X
     */
    val twitter: String? = null,
    
    /**
     * 💼 LINKEDIN
     */
    val linkedin: String? = null,
    
    /**
     * 🎵 TIKTOK
     */
    val tiktok: String? = null,
    
    /**
     * 🌐 SITIO WEB
     */
    val website: String? = null
) {
    /**
     * ✅ VERIFICAR SI TIENE ALGUNA RED SOCIAL
     */
    fun hasAnySocialMedia(): Boolean {
        return facebook != null || instagram != null || twitter != null || 
               linkedin != null || tiktok != null || website != null
    }
    
    /**
     * 📊 OBTENER LISTA DE REDES SOCIALES CON URL
     */
    fun getActiveSocialMedia(): List<Pair<String, String>> {
        val socialMedia = mutableListOf<Pair<String, String>>()
        
        facebook?.let { socialMedia.add("Facebook" to it) }
        instagram?.let { socialMedia.add("Instagram" to it) }
        twitter?.let { socialMedia.add("Twitter/X" to it) }
        linkedin?.let { socialMedia.add("LinkedIn" to it) }
        tiktok?.let { socialMedia.add("TikTok" to it) }
        website?.let { socialMedia.add("Sitio Web" to it) }
        
        return socialMedia
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. Domain Model: Modelo que representa conceptos del negocio
 * 2. Data Validation: Validación de datos en el dominio
 * 3. Business Logic: Lógica específica del negocio en los métodos
 * 4. Enum with Properties: Enums que contienen datos adicionales
 * 5. Composition: User contiene UserPreferences
 * 6. Nullable Fields: Campos opcionales para flexibilidad
 * 
 * ANALOGÍA:
 * 
 * El modelo User es como una "cédula de ciudadanía digital":
 * 
 * 1. **Información básica**: Como nombre, email, teléfono
 * 2. **Información del negocio**: Como el tipo de empresa que maneja
 * 3. **Preferencias**: Como el idioma y tema que prefiere
 * 4. **Estado de la cuenta**: Como si verificó el email
 * 5. **Configuraciones**: Como si usa sincronización en la nube
 * 
 * CASOS DE USO REALES:
 * - "Juan Pérez - Tienda La Esperanza - Retail"
 * - "María García - Restaurante El Buen Sabor - Restaurant"
 * - "Carlos López - Servicios Técnicos - Services"
 * 
 * FUNCIONALIDADES INCLUIDAS:
 * - Validación automática de datos de registro
 * - Detección de usuarios nuevos para tutoriales
 * - Verificación de perfil completo
 * - Configuraciones personalizables por tipo de negocio
 * - Soporte para múltiples monedas e idiomas
 * - Sistema de preferencias flexible
 * 
 * REGLAS DE NEGOCIO:
 * - Email debe estar verificado para funciones premium
 * - Perfil completo desbloquea todas las características
 * - Usuarios nuevos (< 7 días) ven tutoriales
 * - Cada tipo de negocio tiene gastos sugeridos específicos
 */