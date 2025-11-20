package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.InspirationTip
import com.negociolisto.app.domain.model.TimeOfDay
import com.negociolisto.app.domain.model.TipCategory

/**
 * 📚 REPOSITORIO DE TIPS DE INSPIRACIÓN
 * 
 * Interface que define las operaciones para obtener y gestionar
 * los tips de inspiración de la caja sorpresa.
 */
interface InspirationRepository {

    /**
     * 🎲 OBTENER TIP ALEATORIO
     * 
     * Obtiene un tip aleatorio para el horario actual, excluyendo
     * la categoría especificada para evitar repeticiones inmediatas.
     * 
     * @param timeOfDay Horario del día actual
     * @param excludeCategory Categoría a excluir (opcional)
     * @return Tip aleatorio o null si no hay disponibles
     */
    suspend fun getRandomTip(
        timeOfDay: TimeOfDay,
        excludeCategory: TipCategory? = null
    ): InspirationTip?

    /**
     * ✅ MARCAR TIP COMO USADO
     * 
     * Marca un tip específico como usado para evitar repeticiones.
     * 
     * @param tipId ID del tip a marcar
     */
    suspend fun markTipAsUsed(tipId: Long)

    /**
     * 🔄 RESETEAR TODOS LOS TIPS
     * 
     * Marca todos los tips como no usados para reiniciar el ciclo.
     */
    suspend fun resetAllTips()

    /**
     * 📊 OBTENER ESTADÍSTICAS
     * 
     * Obtiene información sobre los tips disponibles y usados.
     * 
     * @param timeOfDay Horario específico
     * @return Par con (disponibles, totales)
     */
    suspend fun getTipStatistics(timeOfDay: TimeOfDay): Pair<Int, Int>

    /**
     * 🌱 INICIALIZAR DATOS
     * 
     * Pobla la base de datos con los tips iniciales si está vacía.
     */
    suspend fun initializeIfEmpty()
}
