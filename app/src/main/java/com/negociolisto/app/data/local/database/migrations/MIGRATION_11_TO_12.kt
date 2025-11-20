package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE BASE DE DATOS: VERSIÓN 11 A 12
 * 
 * Esta migración:
 * 1. Crea categorías predefinidas para usuarios existentes
 * 2. Asocia productos sin customCategoryId a categorías creadas
 * 3. Hace customCategoryId NOT NULL
 * 4. Elimina columna 'category' de products
 */
val MIGRATION_11_TO_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Crear tabla temporal con la nueva estructura
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS products_new (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                sku TEXT NOT NULL,
                purchasePrice REAL NOT NULL,
                salePrice REAL NOT NULL,
                stockQuantity INTEGER NOT NULL,
                minimumStock INTEGER NOT NULL,
                customCategoryId TEXT NOT NULL,
                supplier TEXT,
                photoUrl TEXT,
                imageBackupUrl TEXT,
                isActive INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)

        // 2. Categorías predeterminadas eliminadas
        // Los usuarios deben crear sus propias categorías
        // No se insertan categorías automáticamente

        // 3. Migrar datos de productos
        // Primero, obtener todos los productos existentes
        val cursor = db.query("SELECT * FROM products")
        val products = mutableListOf<Array<Any?>>()
        
        cursor.use {
            while (it.moveToNext()) {
                val productId = it.getString(0)
                val name = it.getString(1)
                val description = it.getString(2)
                val sku = it.getString(3)
                val purchasePrice = it.getDouble(4)
                val salePrice = it.getDouble(5)
                val stockQuantity = it.getInt(6)
                val minimumStock = it.getInt(7)
                val category = it.getString(8) // categoria antigua
                val customCategoryId = it.getString(9)
                val supplier = it.getString(10)
                val photoUrl = it.getString(11)
                val imageBackupUrl = it.getString(12)
                val isActive = it.getInt(13)
                val createdAt = it.getLong(14)
                val updatedAt = it.getLong(15)
                
                // Determinar customCategoryId basado en la categoría antigua
                val newCustomCategoryId = when {
                    customCategoryId != null -> customCategoryId // Ya tiene customCategoryId
                    else -> {
                        // Mapear categoría antigua a nueva categoría predefinida
                        when (category) {
                            "BEBIDAS" -> "default_1"
                            "PANADERIA" -> "default_2"
                            "LACTEOS" -> "default_3"
                            "ABARROTES" -> "default_4"
                            "LIMPIEZA" -> "default_5"
                            "CARNES" -> "default_4" // Mapear a Abarrotes
                            "FRUTAS_VERDURAS" -> "default_4" // Mapear a Abarrotes
                            "DULCES" -> "default_4" // Mapear a Abarrotes
                            "OTROS" -> "default_5" // Mapear a Limpieza
                            else -> "default_5" // Default a Limpieza
                        }
                    }
                }
                
                products.add(arrayOf(
                    productId, name, description, sku, purchasePrice, salePrice,
                    stockQuantity, minimumStock, newCustomCategoryId, supplier,
                    photoUrl, imageBackupUrl, isActive, createdAt, updatedAt
                ))
            }
        }

        // 4. Insertar productos en la nueva tabla
        products.forEach { product ->
            db.execSQL("""
                INSERT INTO products_new (
                    id, name, description, sku, purchasePrice, salePrice,
                    stockQuantity, minimumStock, customCategoryId, supplier,
                    photoUrl, imageBackupUrl, isActive, createdAt, updatedAt
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, product)
        }

        // 5. Eliminar tabla antigua
        db.execSQL("DROP TABLE products")

        // 6. Renombrar tabla nueva
        db.execSQL("ALTER TABLE products_new RENAME TO products")

        // 7. Recrear índices (estos deben coincidir exactamente con los definidos en ProductEntity)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_products_name ON products (name)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_products_sku ON products (sku)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_products_customCategoryId ON products (customCategoryId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_products_isActive ON products (isActive)")
    }
}

