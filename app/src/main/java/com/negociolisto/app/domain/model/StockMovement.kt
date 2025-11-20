package com.negociolisto.app.domain.model

import kotlinx.datetime.LocalDateTime
import java.util.UUID

/**
 * 📊 MODELO DE MOVIMIENTO DE STOCK
 * 
 * Esta clase registra todos los movimientos de entrada y salida
 * de productos en el inventario. Es como un "libro de registro"
 * que documenta cada cambio en el stock.
 * 
 * Los movimientos nos ayudan a:
 * - Rastrear el historial de cambios en el inventario
 * - Identificar patrones de consumo
 * - Auditar discrepancias en el stock
 * - Generar reportes de rotación de inventario
 */
data class StockMovement(
    /**
     * 🆔 ID ÚNICO DEL MOVIMIENTO
     * Identificador único para cada transacción de stock
     */
    val id: String = UUID.randomUUID().toString(),
    
    /**
     * 📦 ID DEL PRODUCTO
     * Referencia al producto que se está moviendo
     */
    val productId: String,
    
    /**
     * 🔄 TIPO DE MOVIMIENTO
     * Si es entrada (IN) o salida (OUT) de stock
     */
    val movementType: StockMovementType,
    
    /**
     * 🔢 CANTIDAD
     * Cuántas unidades se movieron (siempre positivo)
     */
    val quantity: Int,
    
    /**
     * 📝 MOTIVO DEL MOVIMIENTO
     * Por qué se hizo este movimiento
     */
    val reason: StockMovementReason,
    
    /**
     * 📋 DESCRIPCIÓN ADICIONAL (OPCIONAL)
     * Información extra sobre el movimiento
     * Ejemplo: "Compra a proveedor ABC", "Venta a cliente Juan"
     */
    val description: String?,
    
    /**
     * 🧾 REFERENCIA EXTERNA (OPCIONAL)
     * ID de la venta, compra u otra transacción relacionada
     */
    val referenceId: String?,
    
    /**
     * 💰 COSTO UNITARIO (OPCIONAL)
     * Precio por unidad en este movimiento específico
     */
    val unitCost: Double?,
    
    /**
     * 📦 STOCK ANTERIOR
     * Cantidad que había antes de este movimiento
     */
    val previousStock: Int,
    
    /**
     * 📦 STOCK RESULTANTE
     * Cantidad que quedó después de este movimiento
     */
    val newStock: Int,
    
    /**
     * 👤 USUARIO RESPONSABLE (OPCIONAL)
     * Quién hizo este movimiento
     */
    val userId: String?,
    
    /**
     * 📅 FECHA Y HORA DEL MOVIMIENTO
     * Cuándo ocurrió exactamente
     */
    val timestamp: LocalDateTime,
    
    /**
     * 📝 NOTAS ADICIONALES (OPCIONAL)
     * Cualquier información extra relevante
     */
    val notes: String?
) {
    
    /**
     * 💰 CALCULAR VALOR TOTAL DEL MOVIMIENTO
     * 
     * Calcula el valor monetario total de este movimiento.
     * 
     * @return El valor total (cantidad × costo unitario)
     */
    fun getTotalValue(): Double {
        return unitCost?.let { it * quantity } ?: 0.0
    }
    
    /**
     * 📊 OBTENER IMPACTO EN EL STOCK
     * 
     * Calcula el cambio neto en el stock (positivo para entradas, negativo para salidas).
     * 
     * @return El cambio en el stock
     */
    fun getStockImpact(): Int {
        return when (movementType) {
            StockMovementType.IN -> quantity
            StockMovementType.OUT -> -quantity
        }
    }
    
    /**
     * ✅ VALIDAR MOVIMIENTO
     * 
     * Verifica que el movimiento tenga datos consistentes.
     * 
     * @return Lista de errores encontrados (vacía si es válido)
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (productId.isBlank()) {
            errors.add("El ID del producto es obligatorio")
        }
        
        if (quantity <= 0) {
            errors.add("La cantidad debe ser mayor a cero")
        }
        
        if (previousStock < 0) {
            errors.add("El stock anterior no puede ser negativo")
        }
        
        if (newStock < 0) {
            errors.add("El stock resultante no puede ser negativo")
        }
        
        // Validar consistencia del cálculo
        val expectedNewStock = previousStock + getStockImpact()
        if (newStock != expectedNewStock) {
            errors.add("El stock resultante no coincide con el cálculo esperado")
        }
        
        if (unitCost != null && unitCost < 0) {
            errors.add("El costo unitario no puede ser negativo")
        }
        
        return errors
    }
    
    /**
     * 🎨 OBTENER COLOR PARA LA UI
     * 
     * Devuelve un color apropiado para mostrar este movimiento.
     * 
     * @return Color hex para entradas (verde) o salidas (rojo)
     */
    fun getDisplayColor(): String {
        return when (movementType) {
            StockMovementType.IN -> "#4CAF50"  // Verde para entradas
            StockMovementType.OUT -> "#F44336" // Rojo para salidas
        }
    }
    
    /**
     * 📱 OBTENER TEXTO PARA MOSTRAR
     * 
     * Genera un texto descriptivo para mostrar en la UI.
     * 
     * @return Texto descriptivo del movimiento
     */
    fun getDisplayText(): String {
        val action = when (movementType) {
            StockMovementType.IN -> "Entrada"
            StockMovementType.OUT -> "Salida"
        }
        
        val reasonText = reason.displayName
        
        return "$action: $quantity unidades - $reasonText"
    }
}

/**
 * 🔄 TIPOS DE MOVIMIENTO DE STOCK
 * 
 * Define si el movimiento es una entrada o salida de inventario.
 */
enum class StockMovementType(val displayName: String) {
    /**
     * ➕ ENTRADA DE STOCK
     * Cuando agregamos productos al inventario
     */
    IN("Entrada"),
    
    /**
     * ➖ SALIDA DE STOCK
     * Cuando removemos productos del inventario
     */
    OUT("Salida")
}

/**
 * 📋 MOTIVOS DE MOVIMIENTO DE STOCK
 * 
 * Define las razones por las cuales se mueve el stock.
 */
enum class StockMovementReason(
    val displayName: String,
    val movementType: StockMovementType,
    val icon: String
) {
    // ➕ MOTIVOS DE ENTRADA
    PURCHASE("Compra a Proveedor", StockMovementType.IN, "🛒"),
    RETURN_FROM_CUSTOMER("Devolución de Cliente", StockMovementType.IN, "↩️"),
    INITIAL_STOCK("Inventario Inicial", StockMovementType.IN, "📦"),
    ADJUSTMENT_INCREASE("Ajuste de Inventario (+)", StockMovementType.IN, "📈"),
    PRODUCTION("Producción", StockMovementType.IN, "🏭"),
    TRANSFER_IN("Transferencia Entrante", StockMovementType.IN, "📥"),
    
    // ➖ MOTIVOS DE SALIDA
    SALE("Venta a Cliente", StockMovementType.OUT, "💰"),
    RETURN_TO_SUPPLIER("Devolución a Proveedor", StockMovementType.OUT, "📤"),
    DAMAGE("Producto Dañado", StockMovementType.OUT, "💥"),
    EXPIRATION("Producto Vencido", StockMovementType.OUT, "⏰"),
    THEFT("Robo o Pérdida", StockMovementType.OUT, "🚨"),
    ADJUSTMENT_DECREASE("Ajuste de Inventario (-)", StockMovementType.OUT, "📉"),
    SAMPLE("Muestra Gratis", StockMovementType.OUT, "🎁"),
    TRANSFER_OUT("Transferencia Saliente", StockMovementType.OUT, "📤"),
    CONSUMPTION("Consumo Interno", StockMovementType.OUT, "🔧"),
    OTHER("Otro Motivo", StockMovementType.OUT, "❓");
    
    /**
     * 🎨 OBTENER COLOR DEL MOTIVO
     * 
     * Devuelve un color apropiado según el tipo de movimiento.
     */
    fun getColor(): String {
        return when (movementType) {
            StockMovementType.IN -> "#4CAF50"  // Verde para entradas
            StockMovementType.OUT -> "#F44336" // Rojo para salidas
        }
    }
    
    /**
     * ⚠️ VERIFICAR SI ES MOTIVO CRÍTICO
     * 
     * Algunos motivos requieren atención especial.
     */
    fun isCritical(): Boolean {
        return when (this) {
            DAMAGE, EXPIRATION, THEFT -> true
            else -> false
        }
    }
    
    /**
     * 📊 VERIFICAR SI AFECTA COSTOS
     * 
     * Algunos movimientos afectan el costo promedio del inventario.
     */
    fun affectsCost(): Boolean {
        return when (this) {
            PURCHASE, RETURN_FROM_CUSTOMER, PRODUCTION -> true
            else -> false
        }
    }
}

/**
 * 📊 RESUMEN DE MOVIMIENTOS
 * 
 * Data class para mostrar estadísticas de movimientos de un producto.
 */
data class StockMovementSummary(
    /**
     * 📦 ID DEL PRODUCTO
     */
    val productId: String,
    
    /**
     * ➕ TOTAL DE ENTRADAS
     */
    val totalIn: Int,
    
    /**
     * ➖ TOTAL DE SALIDAS
     */
    val totalOut: Int,
    
    /**
     * 🔄 MOVIMIENTO NETO
     */
    val netMovement: Int = totalIn - totalOut,
    
    /**
     * 💰 VALOR TOTAL DE ENTRADAS
     */
    val totalValueIn: Double,
    
    /**
     * 💸 VALOR TOTAL DE SALIDAS
     */
    val totalValueOut: Double,
    
    /**
     * 📅 ÚLTIMO MOVIMIENTO
     */
    val lastMovementDate: LocalDateTime?,
    
    /**
     * 🔢 NÚMERO DE MOVIMIENTOS
     */
    val movementCount: Int
) {
    
    /**
     * 📈 CALCULAR ROTACIÓN DE INVENTARIO
     * 
     * Indica qué tan rápido se mueve el producto.
     * 
     * @param averageStock Stock promedio del período
     * @return Ratio de rotación
     */
    fun getInventoryTurnover(averageStock: Double): Double {
        return if (averageStock > 0) {
            totalOut / averageStock
        } else {
            0.0
        }
    }
    
    /**
     * 💰 CALCULAR VALOR NETO
     * 
     * Diferencia entre el valor de entradas y salidas.
     */
    fun getNetValue(): Double {
        return totalValueIn - totalValueOut
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES:
 * 
 * 1. **Audit Trail**: Rastro de auditoría para todos los cambios
 * 2. **Immutable Records**: Los movimientos no se modifican una vez creados
 * 3. **Business Rules**: Reglas de negocio para validar movimientos
 * 4. **Traceability**: Trazabilidad completa del inventario
 * 5. **Categorization**: Categorización de motivos para análisis
 * 
 * ANALOGÍA:
 * 
 * Un StockMovement es como una "entrada en el libro de registro" de una tienda:
 * 
 * 1. **Fecha y hora**: Cuándo pasó
 * 2. **Producto**: Qué se movió
 * 3. **Cantidad**: Cuánto se movió
 * 4. **Motivo**: Por qué se movió
 * 5. **Responsable**: Quién lo hizo
 * 6. **Stock antes/después**: Estado del inventario
 * 
 * CASOS DE USO REALES:
 * - "Entrada: 50 camisetas - Compra a proveedor XYZ"
 * - "Salida: 3 camisetas - Venta a cliente María"
 * - "Salida: 1 camiseta - Producto dañado"
 * - "Entrada: 10 camisetas - Devolución de cliente"
 * 
 * BENEFICIOS:
 * - Historial completo de cambios
 * - Identificación de patrones de venta
 * - Detección de pérdidas o robos
 * - Cálculo de rotación de inventario
 * - Auditoría y control de calidad
 */