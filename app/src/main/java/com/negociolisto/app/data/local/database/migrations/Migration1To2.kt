package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE VERSIÓN 1 A 2
 * 
 * Agrega la tabla de movimientos de stock al esquema existente.
 */
val MIGRATION_1_TO_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Crear tabla de movimientos de stock
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `stock_movements` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `movementType` TEXT NOT NULL,
                `quantity` INTEGER NOT NULL,
                `reason` TEXT NOT NULL,
                `description` TEXT,
                `referenceId` TEXT,
                `unitCost` REAL,
                `previousStock` INTEGER NOT NULL,
                `newStock` INTEGER NOT NULL,
                `userId` TEXT,
                `timestamp` INTEGER NOT NULL,
                `notes` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """)
        
        // Crear índices para mejorar el rendimiento
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_productId` ON `stock_movements` (`productId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_timestamp` ON `stock_movements` (`timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_movementType` ON `stock_movements` (`movementType`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_reason` ON `stock_movements` (`reason`)")
    }
}

