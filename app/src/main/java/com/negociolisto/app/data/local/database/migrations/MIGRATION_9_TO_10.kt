package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE VERSIÓN 9 A 10
 * 
 * Agrega la tabla de tips de inspiración para la caja sorpresa del dashboard.
 */
val MIGRATION_9_TO_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Crear tabla de tips de inspiración (sin índices en la migración)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS inspiration_tips (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                content TEXT NOT NULL,
                category TEXT NOT NULL,
                timeOfDay TEXT NOT NULL,
                isUsed INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL
            )
        """)
    }
}
