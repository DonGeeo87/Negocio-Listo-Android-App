package com.negociolisto.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.negociolisto.app.data.local.dao.ProductDao
import com.negociolisto.app.data.local.dao.StockMovementDao
import com.negociolisto.app.data.local.dao.SaleDao
import com.negociolisto.app.data.local.dao.CustomerDao
import com.negociolisto.app.data.local.dao.ExpenseDao
import com.negociolisto.app.data.local.dao.InvoiceDao
import com.negociolisto.app.data.local.dao.CustomCategoryDao
import com.negociolisto.app.data.local.dao.InspirationTipDao
import com.negociolisto.app.data.local.dao.CollectionResponseDao
import com.negociolisto.app.data.local.entity.ProductEntity
import com.negociolisto.app.data.local.entity.StockMovementEntity
import com.negociolisto.app.data.local.entity.SaleEntity
import com.negociolisto.app.data.local.entity.CustomerEntity
import com.negociolisto.app.data.local.entity.ExpenseEntity
import com.negociolisto.app.data.local.entity.CollectionEntity
import com.negociolisto.app.data.local.entity.CollectionItemEntity
import com.negociolisto.app.data.local.entity.InvoiceEntity
import com.negociolisto.app.data.local.entity.CustomCategoryEntity
import com.negociolisto.app.data.local.entity.InspirationTipEntity
import com.negociolisto.app.data.local.entity.CollectionResponseEntity
import com.negociolisto.app.data.local.entity.CollectionResponseItemEntity
import com.negociolisto.app.data.local.database.migrations.MIGRATION_1_TO_2
import com.negociolisto.app.data.local.database.migrations.MIGRATION_3_TO_4
import com.negociolisto.app.data.local.database.migrations.MIGRATION_4_TO_5
import com.negociolisto.app.data.local.database.migrations.MIGRATION_5_TO_6
import com.negociolisto.app.data.local.database.migrations.MIGRATION_6_TO_7
import com.negociolisto.app.data.local.database.migrations.MIGRATION_7_TO_8
import com.negociolisto.app.data.local.database.migrations.MIGRATION_8_TO_9
import com.negociolisto.app.data.local.database.migrations.MIGRATION_9_TO_10
import com.negociolisto.app.data.local.database.migrations.MIGRATION_10_TO_11
import com.negociolisto.app.data.local.database.migrations.MIGRATION_11_TO_12
import com.negociolisto.app.data.local.database.migrations.MIGRATION_12_TO_13
import com.negociolisto.app.data.local.database.migrations.MIGRATION_13_TO_14
import com.negociolisto.app.data.local.database.migrations.MIGRATION_14_TO_15
import com.negociolisto.app.data.local.database.migrations.MIGRATION_15_TO_16

/**
 * 🏗️ BASE DE DATOS PRINCIPAL DE NEGOCIO LISTO
 * 
 * Base de datos Room que maneja toda la información local de la app.
 */
@Database(
    entities = [
        ProductEntity::class,
        StockMovementEntity::class,
        SaleEntity::class,
        CustomerEntity::class,
        ExpenseEntity::class,
        CollectionEntity::class,
        CollectionItemEntity::class,
        InvoiceEntity::class,
        CustomCategoryEntity::class,
        InspirationTipEntity::class,
        CollectionResponseEntity::class,
        CollectionResponseItemEntity::class
    ],
    version = 16,
    exportSchema = false
)
abstract class NegocioListoDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun collectionDao(): com.negociolisto.app.data.local.dao.CollectionDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun customCategoryDao(): CustomCategoryDao
    abstract fun inspirationTipDao(): InspirationTipDao
    abstract fun collectionResponseDao(): CollectionResponseDao
    
    companion object {
        const val DATABASE_NAME = "negocio_listo_database"
        
        @Volatile
        private var INSTANCE: NegocioListoDatabase? = null
        
        fun getDatabase(context: Context): NegocioListoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NegocioListoDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(
                    MIGRATION_1_TO_2,
                    MIGRATION_3_TO_4,
                    MIGRATION_4_TO_5,
                    MIGRATION_5_TO_6,
                    MIGRATION_6_TO_7,
                    MIGRATION_7_TO_8,
                    MIGRATION_8_TO_9,
                    MIGRATION_9_TO_10,
                    MIGRATION_10_TO_11,
                    MIGRATION_11_TO_12,
                    MIGRATION_12_TO_13,
                    MIGRATION_13_TO_14,
                    MIGRATION_14_TO_15,
                    MIGRATION_15_TO_16
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}