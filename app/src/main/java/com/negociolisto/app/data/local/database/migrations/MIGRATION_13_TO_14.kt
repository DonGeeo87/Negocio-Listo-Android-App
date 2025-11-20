package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE VERSIÓN 13 A 14
 * 
 * Esta migración agrega el campo accessToken a la tabla customers.
 * Este token único permite a los clientes acceder a su portal
 * de forma persistente, independientemente de las colecciones.
 */
val MIGRATION_13_TO_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Agregar columna accessToken a la tabla customers
        db.execSQL("ALTER TABLE customers ADD COLUMN accessToken TEXT")
    }
}

