package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migración de la versión 12 a la 13
 * 
 * Agrega el campo thumbnailUrl a la tabla products para optimizar
 * la carga de imágenes en listas con thumbnails comprimidos.
 */
val MIGRATION_12_TO_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Agregar columna thumbnailUrl a la tabla products
        db.execSQL("ALTER TABLE products ADD COLUMN thumbnailUrl TEXT")
        
        println("✅ MIGRATION_12_TO_13: Campo thumbnailUrl agregado a products")
    }
}


