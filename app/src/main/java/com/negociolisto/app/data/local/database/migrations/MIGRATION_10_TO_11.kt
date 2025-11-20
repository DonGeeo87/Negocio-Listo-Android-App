package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE VERSIÓN 10 A 11
 * 
 * Agrega el campo imageBackupUrl a la tabla products para almacenar
 * las URLs de respaldo de imágenes en Google Drive.
 */
val MIGRATION_10_TO_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE products 
            ADD COLUMN imageBackupUrl TEXT
        """)
    }
}











