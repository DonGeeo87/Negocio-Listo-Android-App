package com.negociolisto.app.di

import com.negociolisto.app.data.remote.firebase.FirebaseAuthRepository
import com.negociolisto.app.data.repository.InventoryRepositoryImpl
import com.negociolisto.app.data.repository.SalesRepositoryImpl
import com.negociolisto.app.data.repository.CustomerRepositoryImpl
import com.negociolisto.app.data.remote.firebase.FirebaseCustomerRepository
import com.negociolisto.app.data.repository.ExpenseRepositoryImpl
import com.negociolisto.app.data.repository.DashboardRepositoryImpl
import com.negociolisto.app.data.repository.CollectionRepositoryImpl
import com.negociolisto.app.data.remote.firebase.FirebaseCollectionRepository
import com.negociolisto.app.data.hybrid.HybridCollectionRepository
import com.negociolisto.app.data.repository.InvoiceRepositoryImpl
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.SalesRepository
import com.negociolisto.app.domain.repository.CustomerRepository
import com.negociolisto.app.domain.repository.ExpenseRepository
import com.negociolisto.app.domain.repository.DashboardRepository
import com.negociolisto.app.domain.repository.CollectionRepository
import com.negociolisto.app.domain.repository.InvoiceRepository
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import com.negociolisto.app.data.repository.CustomCategoryRepositoryImpl
import com.negociolisto.app.domain.repository.InspirationRepository
import com.negociolisto.app.data.repository.InspirationRepositoryImpl
import com.negociolisto.app.domain.repository.BackupRepository
import com.negociolisto.app.data.repository.BackupRepositoryImpl
import com.negociolisto.app.domain.repository.ChatRepository
import com.negociolisto.app.data.repository.ChatRepositoryImpl
import com.negociolisto.app.domain.repository.CollectionResponseRepository
import com.negociolisto.app.data.repository.CollectionResponseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🏗️ MÓDULO DE REPOSITORIOS
 * 
 * Configura la inyección de dependencias para los repositorios reales.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindInventoryRepository(
        inventoryRepositoryImpl: InventoryRepositoryImpl
    ): InventoryRepository
    
    @Binds
    @Singleton
    abstract fun bindSalesRepository(
        salesRepositoryImpl: SalesRepositoryImpl
    ): SalesRepository
    
    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl
    ): CustomerRepository
    
    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository
    
    
    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository
    
    @Binds
    @Singleton
    abstract fun bindCollectionRepository(
        hybridCollectionRepository: HybridCollectionRepository
    ): CollectionRepository
    
    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        invoiceRepositoryImpl: InvoiceRepositoryImpl
    ): InvoiceRepository
    
    @Binds
    @Singleton
    abstract fun bindCustomCategoryRepository(
        customCategoryRepositoryImpl: CustomCategoryRepositoryImpl
    ): CustomCategoryRepository
    
    @Binds
    @Singleton
    abstract fun bindInspirationRepository(
        inspirationRepositoryImpl: InspirationRepositoryImpl
    ): InspirationRepository
    
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository
    
    @Binds
    @Singleton
    abstract fun bindCollectionResponseRepository(
        collectionResponseRepositoryImpl: CollectionResponseRepositoryImpl
    ): CollectionResponseRepository
    
    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        backupRepositoryImpl: BackupRepositoryImpl
    ): BackupRepository
}