package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_TO_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS collections (id TEXT NOT NULL, name TEXT NOT NULL, description TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, status TEXT NOT NULL, color TEXT, PRIMARY KEY(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS collection_items (localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, collectionId TEXT NOT NULL, productId TEXT NOT NULL, notes TEXT, displayOrder INTEGER NOT NULL, isFeatured INTEGER NOT NULL, specialPrice REAL, FOREIGN KEY(collectionId) REFERENCES collections(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_collection_items_collectionId ON collection_items(collectionId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_collection_items_productId ON collection_items(productId)")
    }
}


