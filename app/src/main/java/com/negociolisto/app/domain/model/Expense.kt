package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalDate

/**
 * 💸 MODELO DE GASTO
 * 
 * Esta clase representa un gasto de nuestro negocio.
 * Es como un "comprobante de gasto" que registra todo el dinero
 * que gastamos para mantener el negocio funcionando.
 * 
 * Los gastos son importantes porque:
 * - Nos ayudan a calcular la rentabilidad real
 * - Son necesarios para declaraciones de impuestos
 * - Nos permiten controlar y optimizar costos
 * - Identifican en qué gastamos más dinero
 */
data class Expense(
    /**
     * 🆔 ID ÚNICO DEL GASTO
     * Cada gasto tiene un identificador único, como el número de factura.
     * Nunca hay dos gastos con el mismo ID.
     */
    val id: String,
    
    /**
     * 📝 DESCRIPCIÓN DEL GASTO
     * Una explicación clara de en qué gastamos el dinero.
     * Ejemplos: "Compra de mercancía", "Pago de arriendo", "Servicios públicos"
     */
    val description: String,
    
    /**
     * 💰 MONTO DEL GASTO
     * Cuánto dinero gastamos. Siempre debe ser un valor positivo.
     * Ejemplo: 150000.0 (representa $150.000)
     */
    val amount: Double,
    
    /**
     * 📂 CATEGORÍA DEL GASTO
     * Tipo de gasto para organizarlos y hacer reportes.
     * Ejemplos: INVENTORY, RENT, UTILITIES, MARKETING, etc.
     */
    val category: ExpenseCategory,
    
    /**
     * 🏪 PROVEEDOR O BENEFICIARIO (OPCIONAL)
     * A quién le pagamos este gasto.
     * Puede ser null si no es relevante o no lo sabemos.
     * Ejemplos: "Almacén XYZ", "Empresa de Servicios", "Juan Pérez"
     */
    val supplier: String?,
    
    /**
     * 📅 FECHA DEL GASTO
     * Cuándo se realizó este gasto.
     * Importante para reportes mensuales y anuales.
     */
    val date: LocalDateTime,
    
    /**
     * 🧾 NÚMERO DE FACTURA O RECIBO (OPCIONAL)
     * El número del documento que respalda este gasto.
     * Útil para auditorías y organización de documentos físicos.
     */
    val receiptNumber: String?,
    
    /**
     * 📝 NOTAS ADICIONALES (OPCIONAL)
     * Información extra que queremos recordar sobre este gasto.
     * Ejemplos: "Pago adelantado", "Incluye descuento del 10%", "Urgente"
     */
    val notes: String?,
    
    /**
     * ✅ ESTADO DEL GASTO
     * Si el gasto está pendiente, pagado, o cancelado.
     */
    val status: ExpenseStatus
) {
    
    /**
     * 📅 OBTENER MES Y AÑO DEL GASTO
     * 
     * Extrae el mes y año para agrupar gastos en reportes mensuales.
     * 
     * @return Un par con el año y mes (ejemplo: Pair(2024, 3) para marzo 2024)
     */
    fun getYearMonth(): Pair<Int, Int> {
        return Pair(date.year, date.monthNumber)
    }
    
    /**
     * 📊 VERIFICAR SI ES GASTO RECURRENTE
     * 
     * Algunos gastos se repiten cada mes (arriendo, servicios).
     * Esta función ayuda a identificarlos basándose en la categoría.
     * 
     * @return true si es un gasto que típicamente se repite cada mes
     */
    fun isRecurringExpense(): Boolean {
        return when (category) {
            ExpenseCategory.RENT,
            ExpenseCategory.UTILITIES,
            ExpenseCategory.INSURANCE,
            ExpenseCategory.SUBSCRIPTIONS,
            ExpenseCategory.SALARIES -> true
            else -> false
        }
    }
    
    /**
     * 💼 VERIFICAR SI ES DEDUCIBLE DE IMPUESTOS
     * 
     * Algunos gastos se pueden descontar en la declaración de renta.
     * Esta función ayuda a identificarlos.
     * 
     * @return true si típicamente es deducible de impuestos
     */
    fun isTaxDeductible(): Boolean {
        return when (category) {
            ExpenseCategory.OFFICE_SUPPLIES,
            ExpenseCategory.MARKETING,
            ExpenseCategory.PROFESSIONAL_SERVICES,
            ExpenseCategory.EQUIPMENT,
            ExpenseCategory.RENT,
            ExpenseCategory.UTILITIES,
            ExpenseCategory.INSURANCE -> true
            ExpenseCategory.PERSONAL,
            ExpenseCategory.ENTERTAINMENT -> false
            else -> true // La mayoría de gastos de negocio son deducibles
        }
    }
    
    /**
     * ⚠️ VERIFICAR SI ES GASTO ALTO
     * 
     * Identifica gastos que están por encima del promedio normal.
     * Útil para alertas y revisión de gastos inusuales.
     * 
     * @param averageExpenseAmount El promedio de gastos en esta categoría
     * @return true si este gasto es significativamente alto
     */
    fun isHighExpense(averageExpenseAmount: Double): Boolean {
        return amount > (averageExpenseAmount * 2.0) // Más del doble del promedio
    }
    
    /**
     * ✅ VERIFICAR SI EL GASTO ES VÁLIDO
     * 
     * Valida que el gasto tenga datos consistentes:
     * - Descripción no vacía
     * - Monto positivo
     * - Fecha no en el futuro (muy lejano)
     * 
     * @return true si el gasto es válido, false si hay problemas
     */
    fun isValid(): Boolean {
        if (description.isBlank()) return false
        if (amount <= 0) return false
        
        // No permitir gastos con fecha muy en el futuro (más de 1 día)
        val now = LocalDateTime.parse("2024-01-01T00:00:00") // Simplificado para el ejemplo
        val daysDifference = date.dayOfYear - now.dayOfYear
        if (daysDifference > 1) return false
        
        return true
    }
}

/**
 * 📂 CATEGORÍAS DE GASTOS
 * 
 * Enum que define los diferentes tipos de gastos que puede tener un negocio.
 * Cada categoría ayuda a organizar y analizar los gastos.
 */
enum class ExpenseCategory(
    val displayName: String,        // Nombre que se muestra al usuario
    val isOperational: Boolean,     // Si es un gasto operacional del negocio
    val typicalFrequency: String    // Qué tan frecuente es este tipo de gasto
) {
    // 📦 GASTOS DE INVENTARIO Y PRODUCTOS
    INVENTORY("Inventario/Mercancía", true, "Variable"),
    SUPPLIES("Insumos y Materiales", true, "Semanal"),
    
    // 🏢 GASTOS DE INFRAESTRUCTURA
    RENT("Arriendo/Alquiler", true, "Mensual"),
    UTILITIES("Servicios Públicos", true, "Mensual"),
    INSURANCE("Seguros", true, "Mensual"),
    
    // 👥 GASTOS DE PERSONAL
    SALARIES("Salarios y Sueldos", true, "Mensual"),
    BENEFITS("Prestaciones Sociales", true, "Mensual"),
    
    // 📈 GASTOS DE MARKETING Y VENTAS
    MARKETING("Marketing y Publicidad", true, "Variable"),
    PROMOTIONS("Promociones y Descuentos", true, "Variable"),
    
    // 🛠️ GASTOS DE EQUIPOS Y TECNOLOGÍA
    EQUIPMENT("Equipos y Herramientas", false, "Ocasional"),
    MAINTENANCE("Mantenimiento y Reparaciones", true, "Variable"),
    SUBSCRIPTIONS("Suscripciones y Software", true, "Mensual"),
    
    // 📋 GASTOS ADMINISTRATIVOS
    OFFICE_SUPPLIES("Útiles de Oficina", true, "Mensual"),
    PROFESSIONAL_SERVICES("Servicios Profesionales", true, "Variable"),
    LEGAL("Gastos Legales", false, "Ocasional"),
    
    // 🚗 GASTOS DE TRANSPORTE
    TRANSPORTATION("Transporte y Combustible", true, "Semanal"),
    DELIVERY("Envíos y Domicilios", true, "Variable"),
    
    // 🎉 GASTOS VARIOS
    ENTERTAINMENT("Entretenimiento", false, "Ocasional"),
    PERSONAL("Gastos Personales", false, "Variable"),
    OTHER("Otros Gastos", true, "Variable");
    
    /**
     * 💡 OBTENER COLOR PARA LA CATEGORÍA
     * 
     * Cada categoría tiene un color asociado para gráficos y reportes.
     * Ayuda a identificar visualmente los tipos de gastos.
     * 
     * @return Un código de color hexadecimal
     */
    fun getColor(): String {
        return when (this) {
            INVENTORY, SUPPLIES -> "#FF6B6B"           // Rojo - Productos
            RENT, UTILITIES, INSURANCE -> "#4ECDC4"    // Verde azulado - Infraestructura
            SALARIES, BENEFITS -> "#45B7D1"           // Azul - Personal
            MARKETING, PROMOTIONS -> "#96CEB4"        // Verde - Marketing
            EQUIPMENT, MAINTENANCE -> "#FFEAA7"       // Amarillo - Equipos
            OFFICE_SUPPLIES, PROFESSIONAL_SERVICES -> "#DDA0DD" // Púrpura - Administrativo
            TRANSPORTATION, DELIVERY -> "#98D8C8"     // Verde menta - Transporte
            else -> "#95A5A6"                         // Gris - Otros
        }
    }
    
    /**
     * 🎨 OBTENER ICONO/EMOJI PARA LA CATEGORÍA
     * 
     * Cada categoría tiene un icono emoji asociado para la UI.
     * 
     * @return Un emoji que representa la categoría
     */
    fun getIcon(): String {
        return when (this) {
            INVENTORY -> "📦"
            SUPPLIES -> "📦"
            RENT -> "🏢"
            UTILITIES -> "⚡"
            INSURANCE -> "🛡️"
            SALARIES -> "💼"
            BENEFITS -> "👥"
            MARKETING -> "📢"
            PROMOTIONS -> "🎯"
            EQUIPMENT -> "🛠️"
            MAINTENANCE -> "🔧"
            SUBSCRIPTIONS -> "💻"
            OFFICE_SUPPLIES -> "📋"
            PROFESSIONAL_SERVICES -> "💼"
            LEGAL -> "⚖️"
            TRANSPORTATION -> "🚗"
            DELIVERY -> "🚚"
            ENTERTAINMENT -> "🎉"
            PERSONAL -> "👤"
            OTHER -> "💸"
        }
    }
}

/**
 * ✅ ESTADOS DE GASTO
 * 
 * Enum que define los diferentes estados en que puede estar un gasto.
 */
enum class ExpenseStatus(val displayName: String) {
    PENDING("Pendiente"),      // Gasto registrado pero no pagado
    PAID("Pagado"),           // Gasto ya pagado
    CANCELLED("Cancelado"),   // Gasto cancelado (no se pagará)
    OVERDUE("Vencido");       // Gasto pendiente que ya pasó su fecha límite
    
    /**
     * 🎨 OBTENER COLOR PARA EL ESTADO
     * 
     * Cada estado tiene un color para mostrar visualmente el estado.
     * 
     * @return Un código de color hexadecimal
     */
    fun getColor(): String {
        return when (this) {
            PENDING -> "#FFA500"    // Naranja
            PAID -> "#28A745"       // Verde
            CANCELLED -> "#6C757D"  // Gris
            OVERDUE -> "#DC3545"    // Rojo
        }
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. Business Categories: Clasificación de gastos por tipo de negocio
 * 2. Tax Deductibility: Identificación de gastos deducibles
 * 3. Recurring vs One-time: Diferenciación entre gastos recurrentes y únicos
 * 4. Status Management: Control del estado de los gastos
 * 5. Data Validation: Verificación de consistencia de datos
 * 
 * ANALOGÍA:
 * 
 * Un gasto es como un "comprobante de pago" en tu billetera:
 * 
 * 1. **Expense**: Es el comprobante completo
 *    - Fecha de compra
 *    - Descripción de lo que compraste
 *    - Monto pagado
 *    - Lugar donde compraste
 * 
 * 2. **ExpenseCategory**: Es como organizar los comprobantes en sobres
 *    - Sobre "Comida" para gastos de alimentación
 *    - Sobre "Transporte" para gastos de movilidad
 *    - Sobre "Casa" para gastos del hogar
 * 
 * 3. **ExpenseStatus**: Es como marcar los comprobantes
 *    - ✅ "Pagado" con marcador verde
 *    - ⏳ "Pendiente" con marcador amarillo
 *    - ❌ "Cancelado" con marcador rojo
 * 
 * REGLAS DE NEGOCIO IMPLEMENTADAS:
 * - Identificación de gastos recurrentes vs únicos
 * - Clasificación de gastos deducibles de impuestos
 * - Detección de gastos inusualmente altos
 * - Validación de datos de entrada
 * - Organización por categorías de negocio
 */