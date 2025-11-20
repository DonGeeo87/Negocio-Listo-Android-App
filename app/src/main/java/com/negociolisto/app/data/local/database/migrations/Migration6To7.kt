package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE VERSIÓN 6 A 7
 * 
 * Esta migración agrega las tablas de colecciones:
 * - collections: Para almacenar las colecciones de productos
 * - collection_items: Para almacenar los items individuales de cada colección
 */
val MIGRATION_6_TO_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Agregar campo associatedCustomerIds a la tabla collections existente
        db.execSQL("ALTER TABLE collections ADD COLUMN associatedCustomerIds TEXT NOT NULL DEFAULT ''")
        
        // Crear índices para la tabla collections
        db.execSQL("CREATE INDEX IF NOT EXISTS index_collections_name ON collections (name)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_collections_status ON collections (status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_collections_createdAt ON collections (createdAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_collections_updatedAt ON collections (updatedAt)")
        
        // Crear tabla de items de colección
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS collection_items (
                localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                collectionId TEXT NOT NULL,
                productId TEXT NOT NULL,
                notes TEXT,
                displayOrder INTEGER NOT NULL,
                isFeatured INTEGER NOT NULL,
                specialPrice REAL,
                FOREIGN KEY(collectionId) REFERENCES collections(id) ON DELETE CASCADE,
                FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE
            )
        """)
        
        // Crear índices para la tabla de items de colección
        db.execSQL("CREATE INDEX IF NOT EXISTS index_collection_items_collectionId ON collection_items (collectionId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_collection_items_productId ON collection_items (productId)")
    }
}
