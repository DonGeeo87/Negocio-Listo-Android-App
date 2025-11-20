package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE VERSIÓN 7 A 8
 * 
 * Agrega la tabla de facturas (invoices).
 */
val MIGRATION_7_TO_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Crear tabla de facturas
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS invoices (
                id TEXT NOT NULL PRIMARY KEY,
                number TEXT NOT NULL,
                saleId TEXT,
                customerId TEXT,
                items TEXT NOT NULL,
                subtotal REAL NOT NULL,
                tax REAL NOT NULL,
                total REAL NOT NULL,
                date TEXT NOT NULL,
                template TEXT NOT NULL,
                notes TEXT
            )
        """)
    }
}

