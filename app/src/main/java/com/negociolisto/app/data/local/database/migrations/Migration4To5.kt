package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🚚 Migración 4→5: agrega columnas de anulación a tabla sales
 */
val MIGRATION_4_TO_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Nuevas columnas en ventas: status, canceledAt, canceledReason
        db.execSQL("ALTER TABLE sales ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
        db.execSQL("ALTER TABLE sales ADD COLUMN canceledAt INTEGER")
        db.execSQL("ALTER TABLE sales ADD COLUMN canceledReason TEXT")
    }
}


