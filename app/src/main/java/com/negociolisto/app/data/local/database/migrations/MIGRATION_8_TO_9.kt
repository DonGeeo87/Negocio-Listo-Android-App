package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE VERSIÓN 8 A 9
 * 
 * Agrega la tabla de categorías personalizadas de productos.
 */
val MIGRATION_8_TO_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Crear tabla de categorías personalizadas
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS custom_categories (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                icon TEXT NOT NULL,
                color TEXT NOT NULL,
                description TEXT,
                createdAt TEXT NOT NULL,
                updatedAt TEXT NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1,
                userId TEXT NOT NULL,
                sortOrder INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // Crear índices para mejorar el rendimiento
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_custom_categories_userId 
            ON custom_categories(userId)
        """)
        
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_custom_categories_isActive 
            ON custom_categories(isActive)
        """)
        
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_custom_categories_sortOrder 
            ON custom_categories(sortOrder)
        """)
        
        // Crear índice compuesto para consultas frecuentes
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_custom_categories_userId_isActive_sortOrder 
            ON custom_categories(userId, isActive, sortOrder)
        """)
    }
}
