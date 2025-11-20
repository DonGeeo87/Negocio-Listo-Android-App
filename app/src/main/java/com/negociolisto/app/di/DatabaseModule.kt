package com.negociolisto.app.di

import android.content.Context
import com.negociolisto.app.data.local.database.NegocioListoDatabase
import com.negociolisto.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🗄️ MÓDULO DE BASE DE DATOS
 * 
 * Proporciona instancias de la base de datos y DAOs para Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NegocioListoDatabase {
        return NegocioListoDatabase.getDatabase(context)
    }

    @Provides
    fun provideProductDao(database: NegocioListoDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideStockMovementDao(database: NegocioListoDatabase): StockMovementDao {
        return database.stockMovementDao()
    }

    @Provides
    fun provideSaleDao(database: NegocioListoDatabase): SaleDao {
        return database.saleDao()
    }

    @Provides
    fun provideCustomerDao(database: NegocioListoDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    fun provideExpenseDao(database: NegocioListoDatabase): ExpenseDao {
        return database.expenseDao()
    }


    @Provides
    fun provideCollectionDao(database: NegocioListoDatabase): CollectionDao {
        return database.collectionDao()
    }

    @Provides
    fun provideInvoiceDao(database: NegocioListoDatabase): InvoiceDao {
        return database.invoiceDao()
    }

    @Provides
    fun provideCustomCategoryDao(database: NegocioListoDatabase): CustomCategoryDao {
        return database.customCategoryDao()
    }

    @Provides
    fun provideInspirationTipDao(database: NegocioListoDatabase): InspirationTipDao {
        return database.inspirationTipDao()
    }

    @Provides
    fun provideCollectionResponseDao(database: NegocioListoDatabase): CollectionResponseDao {
        return database.collectionResponseDao()
    }
}