package com.negociolisto.app.data.local.dao

import androidx.room.*
import com.negociolisto.app.data.local.entity.InspirationTipEntity
import kotlinx.coroutines.flow.Flow

/**
 * 🗃️ DAO PARA TIPS DE INSPIRACIÓN
 * 
 * Data Access Object que maneja todas las operaciones de base de datos
 * para los tips de inspiración.
 */

@Dao
interface InspirationTipDao {

    /**
     * 📖 OBTENER TODOS LOS TIPS
     * 
     * Flow para observar cambios en tiempo real.
     */
    @Query("SELECT * FROM inspiration_tips ORDER BY createdAt DESC")
    fun getAllTips(): Flow<List<InspirationTipEntity>>

    /**
     * 🎲 OBTENER TIP ALEATORIO POR HORARIO
     * 
     * Selecciona un tip aleatorio no usado para el horario específico.
     */
    @Query("""
        SELECT * FROM inspiration_tips 
        WHERE timeOfDay = :timeOfDay 
        AND isUsed = 0 
        ORDER BY RANDOM() 
        LIMIT 1
    """)
    suspend fun getRandomUnusedTipByTime(timeOfDay: String): InspirationTipEntity?

    /**
     * 🎯 OBTENER TIP ALEATORIO EXCLUYENDO CATEGORÍA
     * 
     * Selecciona un tip aleatorio que no sea de la categoría especificada.
     */
    @Query("""
        SELECT * FROM inspiration_tips 
        WHERE timeOfDay = :timeOfDay 
        AND category != :excludeCategory 
        AND isUsed = 0 
        ORDER BY RANDOM() 
        LIMIT 1
    """)
    suspend fun getRandomTipExcludingCategory(
        timeOfDay: String, 
        excludeCategory: String
    ): InspirationTipEntity?

    /**
     * ✅ MARCAR TIP COMO USADO
     * 
     * Actualiza el estado de un tip específico.
     */
    @Update
    suspend fun markTipAsUsed(tip: InspirationTipEntity)

    /**
     * 🔄 RESETEAR TODOS LOS TIPS
     * 
     * Marca todos los tips como no usados para reiniciar el ciclo.
     */
    @Query("UPDATE inspiration_tips SET isUsed = 0")
    suspend fun resetAllTips()

    /**
     * 📊 CONTAR TIPS DISPONIBLES
     * 
     * Cuenta cuántos tips no usados hay para un horario específico.
     */
    @Query("""
        SELECT COUNT(*) FROM inspiration_tips 
        WHERE timeOfDay = :timeOfDay AND isUsed = 0
    """)
    suspend fun countAvailableTips(timeOfDay: String): Int

    /**
     * 📊 CONTAR TIPS TOTALES POR HORARIO
     * 
     * Cuenta todos los tips para un horario específico.
     */
    @Query("SELECT COUNT(*) FROM inspiration_tips WHERE timeOfDay = :timeOfDay")
    suspend fun countTotalTips(timeOfDay: String): Int

    /**
     * ➕ INSERTAR TIP
     * 
     * Inserta un nuevo tip en la base de datos.
     */
    @Insert
    suspend fun insertTip(tip: InspirationTipEntity): Long

    /**
     * ➕ INSERTAR MÚLTIPLES TIPS
     * 
     * Inserta múltiples tips de una vez.
     */
    @Insert
    suspend fun insertTips(tips: List<InspirationTipEntity>)

    /**
     * 🗑️ ELIMINAR TODOS LOS TIPS
     * 
     * Elimina todos los tips (útil para testing o reset completo).
     */
    @Query("DELETE FROM inspiration_tips")
    suspend fun deleteAllTips()

    /**
     * 🔍 OBTENER TIP POR ID
     * 
     * Obtiene un tip específico por su ID.
     */
    @Query("SELECT * FROM inspiration_tips WHERE id = :id")
    suspend fun getTipById(id: Long): InspirationTipEntity?
}











