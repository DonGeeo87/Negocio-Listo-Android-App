package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * 👤 MODELO DE CLIENTE
 * 
 * Esta clase representa a un cliente de nuestro negocio.
 * Es como una "ficha de cliente" que contiene toda su información importante.
 * 
 * Los clientes son personas que compran nuestros productos.
 * Mantener su información nos ayuda a:
 * - Brindar mejor servicio personalizado
 * - Hacer seguimiento de sus compras
 * - Crear catálogos específicos para ellos
 * - Enviarles facturas y recordatorios
 */
data class Customer(
    /**
     * 🆔 ID ÚNICO DEL CLIENTE
     * Cada cliente tiene un identificador único, como su número de cédula.
     * Nunca hay dos clientes con el mismo ID.
     */
    val id: String,
    
    /**
     * 👤 NOMBRE COMPLETO DEL CLIENTE
     * El nombre que usamos para dirigirnos al cliente.
     * Ejemplo: "Juan Pérez", "María García", "Empresa ABC S.A."
     */
    val name: String,
    
    /**
     * 🏢 NOMBRE DE EMPRESA (OPCIONAL)
     * Razón social o empresa asociada al cliente.
     */
    val companyName: String? = null,
    
    /**
     * 📞 TELÉFONO (OPCIONAL)
     * Número de contacto del cliente.
     * Puede ser null si no tenemos su teléfono.
     * Útil para confirmar pedidos o enviar notificaciones por WhatsApp.
     */
    val phone: String?,
    
    /**
     * 📧 EMAIL (OPCIONAL)
     * Correo electrónico del cliente.
     * Puede ser null si no tenemos su email.
     * Útil para enviar facturas digitales y promociones.
     */
    val email: String?,
    
    /**
     * 🏠 DIRECCIÓN (OPCIONAL)
     * Dirección física del cliente.
     * Puede ser null si no la conocemos.
     * Útil para entregas a domicilio o envío de productos.
     */
    val address: String?,
    
    /**
     * 📝 NOTAS ADICIONALES (OPCIONAL)
     * Información extra sobre el cliente que queremos recordar.
     * Ejemplos: "Prefiere pago en efectivo", "Cliente VIP", "Compra solo los martes"
     */
    val notes: String?,
    
    /**
     * 📅 FECHA DE CREACIÓN
     * Cuándo agregamos este cliente a nuestro sistema por primera vez.
     * Nos ayuda a saber qué tan "nuevo" o "antiguo" es el cliente.
     */
    val createdAt: LocalDateTime,
    
    /**
     * 💰 TOTAL DE COMPRAS REALIZADAS
     * Cuánto dinero ha gastado este cliente en total desde que lo conocemos.
     * Se calcula automáticamente sumando todas sus compras.
     */
    val totalPurchases: Double,
    
    /**
     * 🛒 FECHA DE ÚLTIMA COMPRA (OPCIONAL)
     * Cuándo fue la última vez que este cliente compró algo.
     * Puede ser null si nunca ha comprado nada.
     * Útil para identificar clientes inactivos.
     */
    val lastPurchaseDate: LocalDateTime?,
    
    /**
     * 🔑 TOKEN DE ACCESO ÚNICO (OPCIONAL)
     * Token único y persistente que permite al cliente acceder a su portal
     * sin necesidad de autenticación compleja. Este token no cambia aunque
     * se modifiquen las colecciones asociadas al cliente.
     */
    val accessToken: String? = null
) {
    
    /**
     * ⭐ VERIFICAR SI ES CLIENTE VIP
     * 
     * Un cliente VIP es alguien que ha gastado mucho dinero con nosotros.
     * Consideramos VIP a clientes que han gastado más de $500.000.
     * 
     * @return true si es cliente VIP, false si no
     */
    fun isVipCustomer(): Boolean {
        return totalPurchases >= 500000.0
    }
    
    /**
     * 😴 VERIFICAR SI ES CLIENTE INACTIVO
     * 
     * Un cliente inactivo es alguien que no ha comprado en mucho tiempo.
     * Consideramos inactivo si no ha comprado en los últimos 90 días.
     * 
     * @param currentDate La fecha actual para comparar
     * @return true si está inactivo, false si está activo
     */
    fun isInactiveCustomer(currentDate: LocalDateTime): Boolean {
        // Si nunca ha comprado, no está inactivo (es nuevo)
        if (lastPurchaseDate == null) return false
        
        // Calculamos los días desde su última compra
        // (Simplificado - en una app real usaríamos una librería de fechas más robusta)
        val daysSinceLastPurchase = currentDate.dayOfYear - lastPurchaseDate.dayOfYear
        return daysSinceLastPurchase > 90
    }
    
    /**
     * 📊 CALCULAR PROMEDIO DE COMPRA
     * 
     * Si sabemos cuántas veces ha comprado, podemos calcular
     * cuánto gasta en promedio cada vez.
     * 
     * @param totalPurchaseCount Número total de compras realizadas
     * @return El promedio de gasto por compra
     */
    fun getAveragePurchaseAmount(totalPurchaseCount: Int): Double {
        return if (totalPurchaseCount > 0) {
            totalPurchases / totalPurchaseCount
        } else {
            0.0
        }
    }
    
    /**
     * 📱 OBTENER MÉTODO DE CONTACTO PREFERIDO
     * 
     * Determina la mejor forma de contactar a este cliente
     * basándose en la información que tenemos.
     * 
     * @return El método de contacto recomendado
     */
    fun getPreferredContactMethod(): ContactMethod {
        return when {
            phone != null && email != null -> ContactMethod.BOTH
            phone != null -> ContactMethod.PHONE
            email != null -> ContactMethod.EMAIL
            else -> ContactMethod.NONE
        }
    }
    
    /**
     * ✅ VERIFICAR SI TIENE INFORMACIÓN COMPLETA
     * 
     * Nos dice si tenemos toda la información básica del cliente.
     * Útil para identificar fichas de clientes que necesitan completarse.
     * 
     * @return true si tiene información completa, false si falta algo
     */
    fun hasCompleteInfo(): Boolean {
        return name.isNotBlank() && 
               (phone != null || email != null) && 
               address != null
    }
}

/**
 * 📞 MÉTODOS DE CONTACTO DISPONIBLES
 * 
 * Enum que define las diferentes formas de contactar a un cliente.
 * Es como una lista de opciones predefinidas.
 */
enum class ContactMethod {
    PHONE,    // Solo teléfono
    EMAIL,    // Solo email
    BOTH,     // Teléfono y email
    NONE      // No tenemos forma de contactarlo
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. Data Class: Clase especial para almacenar datos
 * 2. Nullable Types (String?): Campos que pueden estar vacíos
 * 3. Business Logic: Métodos que implementan reglas de negocio
 * 4. Enum: Lista cerrada de opciones posibles
 * 5. Default Values: Valores por defecto para campos opcionales
 * 
 * ANALOGÍA:
 * 
 * Esta clase es como una "ficha de cliente" en una tienda tradicional:
 * 
 * 1. **Información básica**: Como el nombre y datos de contacto en la ficha
 * 2. **Historial de compras**: Como las anotaciones de qué ha comprado
 * 3. **Notas especiales**: Como recordatorios escritos a mano
 * 4. **Clasificación**: Como etiquetas de "Cliente VIP" o "Cliente Regular"
 * 
 * La diferencia es que nuestra "ficha digital" puede:
 * - Calcular automáticamente totales y promedios
 * - Determinar el mejor método de contacto
 * - Identificar patrones de comportamiento
 * - Nunca se pierde ni se deteriora
 * 
 * REGLAS DE NEGOCIO IMPLEMENTADAS:
 * - Cliente VIP: Más de $500.000 en compras
 * - Cliente inactivo: Sin compras en 90+ días
 * - Información completa: Nombre + contacto + dirección
 * - Método de contacto: Prioriza tener ambos (teléfono y email)
 */