package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🚚 Migración 3→4: agrega columna opcional companyName a tabla customers
 */
val MIGRATION_3_TO_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE customers ADD COLUMN companyName TEXT")
        // Índice opcional si se requiere búsqueda por companyName en el futuro
        // db.execSQL("CREATE INDEX IF NOT EXISTS index_customers_companyName ON customers(companyName)")
    }
}


