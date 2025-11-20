package com.negociolisto.app.domain.model

/**
 * 💡 TIPS DE INSPIRACIÓN Y MOTIVACIÓN
 * 
 * Modelo de dominio para los tips que se muestran en la caja sorpresa
 * del dashboard. Cada tip tiene categoría, horario apropiado y estado de uso.
 */

/**
 * 📂 CATEGORÍAS DE TIPS
 * 
 * Diferentes tipos de consejos y motivación para emprendedores.
 */
enum class TipCategory(
    val displayName: String,
    val icon: String,
    val description: String
) {
    MOTIVATION(
        displayName = "Motivación",
        icon = "💪",
        description = "Frases para mantener la motivación alta"
    ),
    BUSINESS_ADVICE(
        displayName = "Consejos de Negocio",
        icon = "💼",
        description = "Tips prácticos para mejorar tu negocio"
    ),
    SALES_TIPS(
        displayName = "Tips de Ventas",
        icon = "💰",
        description = "Estrategias para aumentar las ventas"
    ),
    INVENTORY_MANAGEMENT(
        displayName = "Gestión de Inventario",
        icon = "📦",
        description = "Consejos para optimizar tu inventario"
    ),
    CUSTOMER_SERVICE(
        displayName = "Atención al Cliente",
        icon = "🤝",
        description = "Mejores prácticas de servicio al cliente"
    ),
    FINANCES(
        displayName = "Finanzas",
        icon = "📊",
        description = "Consejos financieros para tu negocio"
    )
}

/**
 * ⏰ HORARIOS DEL DÍA
 * 
 * Define los diferentes momentos del día para mostrar tips apropiados.
 */
enum class TimeOfDay(
    val displayName: String,
    val startHour: Int,
    val endHour: Int,
    val description: String
) {
    DAWN(
        displayName = "Madrugada",
        startHour = 0,
        endHour = 5,
        description = "Momento de preparación y planificación"
    ),
    MORNING(
        displayName = "Mañana",
        startHour = 6,
        endHour = 11,
        description = "Energía y enfoque para comenzar el día"
    ),
    AFTERNOON(
        displayName = "Tarde",
        startHour = 12,
        endHour = 17,
        description = "Productividad y gestión activa"
    ),
    NIGHT(
        displayName = "Noche",
        startHour = 18,
        endHour = 23,
        description = "Reflexión y preparación para el descanso"
    );

    /**
     * 🕐 OBTENER HORARIO ACTUAL
     * 
     * Determina el horario del día basado en la hora actual.
     */
    companion object {
        fun getCurrentTimeOfDay(): TimeOfDay {
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return when (currentHour) {
                in DAWN.startHour..DAWN.endHour -> DAWN
                in MORNING.startHour..MORNING.endHour -> MORNING
                in AFTERNOON.startHour..AFTERNOON.endHour -> AFTERNOON
                else -> NIGHT
            }
        }
    }
}

/**
 * 💡 TIP DE INSPIRACIÓN
 * 
 * Modelo principal que representa un consejo o frase motivacional.
 */
data class InspirationTip(
    val id: Long = 0,
    val content: String,
    val category: TipCategory,
    val timeOfDay: TimeOfDay,
    val isUsed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)











