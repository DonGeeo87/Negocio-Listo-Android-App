package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE VERSIÓN 15 A 16
 *
 * Agrega el campo userId a la tabla collections para aislar datos por usuario.
 * Este campo es crítico para la seguridad y privacidad de los datos.
 */
val MIGRATION_15_TO_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Agregar columna userId a la tabla collections
        db.execSQL("ALTER TABLE `collections` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
        
        // Crear índice para mejorar el rendimiento de consultas filtradas por userId
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_collections_userId` ON `collections`(`userId`)")
        
        // ⚠️ IMPORTANTE: Las colecciones existentes tendrán userId vacío.
        // Se recomienda limpiar la base de datos o migrar manualmente los datos.
        // Para producción, se debe ejecutar un script que asigne userId a las colecciones existentes.
    }
}

