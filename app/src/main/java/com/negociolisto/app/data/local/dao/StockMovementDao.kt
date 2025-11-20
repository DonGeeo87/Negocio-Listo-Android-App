package com.negociolisto.app.data.local.dao

import androidx.room.*
import com.negociolisto.app.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📊 DAO DE MOVIMIENTOS DE STOCK
 * 
 * Define las operaciones de base de datos para movimientos de stock.
 */
@Dao
interface StockMovementDao {
    
    // ==========================================
    // CONSULTAS BÁSICAS
    // ==========================================
    
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovementEntity>>
    
    @Query("SELECT * FROM stock_movements WHERE id = :id")
    suspend fun getMovementById(id: String): StockMovementEntity?
    
    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsByProduct(productId: String): Flow<List<StockMovementEntity>>
    
    @Query("SELECT * FROM stock_movements WHERE productId = :productId AND movementType = :movementType ORDER BY timestamp DESC")
    fun getMovementsByProductAndType(
        productId: String, 
        movementType: String
    ): Flow<List<StockMovementEntity>>
    
    @Query("SELECT * FROM stock_movements WHERE reason = :reason ORDER BY timestamp DESC")
    fun getMovementsByReason(reason: String): Flow<List<StockMovementEntity>>
    
    // ==========================================
    // CONSULTAS POR RANGO DE FECHAS
    // ==========================================
    
    @Query("""
        SELECT * FROM stock_movements 
        WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp 
        ORDER BY timestamp DESC
    """)
    fun getMovementsByDateRange(
        startTimestamp: Long, 
        endTimestamp: Long
    ): Flow<List<StockMovementEntity>>
    
    @Query("""
        SELECT * FROM stock_movements 
        WHERE productId = :productId 
        AND timestamp BETWEEN :startTimestamp AND :endTimestamp 
        ORDER BY timestamp DESC
    """)
    fun getMovementsByProductAndDateRange(
        productId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<StockMovementEntity>>
    
    // ==========================================
    // CONSULTAS DE RESUMEN Y ESTADÍSTICAS
    // ==========================================
    
    @Query("""
        SELECT 
            COUNT(*) as movementCount,
            SUM(CASE WHEN movementType = 'IN' THEN quantity ELSE 0 END) as totalIn,
            SUM(CASE WHEN movementType = 'OUT' THEN quantity ELSE 0 END) as totalOut,
            SUM(CASE WHEN movementType = 'IN' THEN (quantity * COALESCE(unitCost, 0)) ELSE 0 END) as totalValueIn,
            SUM(CASE WHEN movementType = 'OUT' THEN (quantity * COALESCE(unitCost, 0)) ELSE 0 END) as totalValueOut
        FROM stock_movements 
        WHERE productId = :productId
    """)
    suspend fun getMovementSummary(productId: String): MovementSummary?
    
    @Query("""
        SELECT 
            COUNT(*) as movementCount,
            SUM(CASE WHEN movementType = 'IN' THEN quantity ELSE 0 END) as totalIn,
            SUM(CASE WHEN movementType = 'OUT' THEN quantity ELSE 0 END) as totalOut,
            SUM(CASE WHEN movementType = 'IN' THEN (quantity * COALESCE(unitCost, 0)) ELSE 0 END) as totalValueIn,
            SUM(CASE WHEN movementType = 'OUT' THEN (quantity * COALESCE(unitCost, 0)) ELSE 0 END) as totalValueOut
        FROM stock_movements 
        WHERE productId = :productId 
        AND timestamp BETWEEN :startTimestamp AND :endTimestamp
    """)
    suspend fun getMovementSummaryByDateRange(
        productId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): MovementSummary?
    
    // ==========================================
    // CONSULTAS DE PRODUCTOS MÁS MOVIDOS
    // ==========================================
    
    @Query("""
        SELECT productId, COUNT(*) as movementCount
        FROM stock_movements 
        WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp
        GROUP BY productId 
        ORDER BY movementCount DESC 
        LIMIT :limit
    """)
    suspend fun getMostMovedProducts(
        startTimestamp: Long,
        endTimestamp: Long,
        limit: Int = 10
    ): List<ProductMovementCount>
    
    // ==========================================
    // OPERACIONES DE ESCRITURA
    // ==========================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovementEntity>)
    
    @Update
    suspend fun updateMovement(movement: StockMovementEntity)
    
    @Delete
    suspend fun deleteMovement(movement: StockMovementEntity)
    
    @Query("DELETE FROM stock_movements WHERE id = :id")
    suspend fun deleteMovementById(id: String)
    
    @Query("DELETE FROM stock_movements WHERE productId = :productId")
    suspend fun deleteMovementsByProduct(productId: String)
    
    // ==========================================
    // CONSULTAS DE LIMPIEZA
    // ==========================================
    
    @Query("DELETE FROM stock_movements WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldMovements(cutoffTimestamp: Long)
    
    @Query("SELECT COUNT(*) FROM stock_movements")
    suspend fun getTotalMovementCount(): Int

    @Query("DELETE FROM stock_movements")
    suspend fun clearAllMovements()
}

/**
 * 📊 RESUMEN DE MOVIMIENTOS
 * 
 * Clase de datos para consultas de resumen de movimientos.
 */
data class MovementSummary(
    val movementCount: Int,
    val totalIn: Int,
    val totalOut: Int,
    val totalValueIn: Double,
    val totalValueOut: Double
)

/**
 * 📈 CONTEO DE MOVIMIENTOS POR PRODUCTO
 * 
 * Clase de datos para consultas de productos más movidos.
 */
data class ProductMovementCount(
    val productId: String,
    val movementCount: Int
)

