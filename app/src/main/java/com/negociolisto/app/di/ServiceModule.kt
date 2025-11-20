package com.negociolisto.app.di

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.negociolisto.app.data.service.BackupService
import com.negociolisto.app.data.service.CommunicationService
import com.negociolisto.app.data.service.ExportService
import com.negociolisto.app.data.service.ImageService
import com.negociolisto.app.data.service.GoogleAuthService
import com.negociolisto.app.data.service.GoogleSignInService
import com.negociolisto.app.data.sync.SyncQueue
import com.negociolisto.app.data.preferences.ThemeManager
import com.negociolisto.app.data.parsing.SocialMediaParser
import com.negociolisto.app.data.service.LoginTrackingService
import com.negociolisto.app.data.service.ContactImportService
import com.negociolisto.app.data.service.AutoBackupManager
import com.negociolisto.app.data.service.OrderEmailService
import com.negociolisto.app.ui.components.ToastViewModel
import com.negociolisto.app.data.remote.firebase.FirebaseBackupRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.negociolisto.app.data.local.database.NegocioListoDatabase
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.CustomerRepository
import com.negociolisto.app.domain.repository.SalesRepository
import com.negociolisto.app.domain.repository.ExpenseRepository
import com.negociolisto.app.domain.repository.CollectionRepository
import com.negociolisto.app.domain.repository.InvoiceRepository
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.CustomCategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🔧 MÓDULO DE SERVICIOS
 * 
 * Proporciona instancias de servicios para Hilt.
 * Incluye servicios de backup, exportación e imágenes.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideBackupService(
        @ApplicationContext context: Context,
        customCategoryRepository: CustomCategoryRepository,
        authRepository: AuthRepository,
        themeManager: ThemeManager,
        socialMediaParser: SocialMediaParser,
        loginTrackingService: LoginTrackingService,
        firebaseBackupRepository: FirebaseBackupRepository,
        imageService: ImageService
    ): BackupService {
        return BackupService(context, customCategoryRepository, authRepository, themeManager, socialMediaParser, loginTrackingService, firebaseBackupRepository, imageService)
    }

    @Provides
    @Singleton
    fun provideExportService(
        @ApplicationContext context: Context,
        inventoryRepository: InventoryRepository,
        customerRepository: CustomerRepository,
        salesRepository: SalesRepository,
        expenseRepository: ExpenseRepository,
        collectionRepository: CollectionRepository,
        invoiceRepository: InvoiceRepository,
        authRepository: AuthRepository,
        customCategoryRepository: CustomCategoryRepository
    ): ExportService {
        return ExportService(
            context,
            inventoryRepository,
            customerRepository,
            salesRepository,
            expenseRepository,
            collectionRepository,
            invoiceRepository,
            authRepository,
            customCategoryRepository
        )
    }

    @Provides
    @Singleton
    fun provideImageService(firebaseStorage: FirebaseStorage): ImageService {
        return ImageService(firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideGoogleAuthService(
        @ApplicationContext context: Context
    ): GoogleAuthService {
        return GoogleAuthService(context)
    }


    @Provides
    @Singleton
    fun provideGoogleSignInService(
        @ApplicationContext context: Context
    ): GoogleSignInService {
        return GoogleSignInService(context)
    }

    @Provides
    @Singleton
    fun provideCommunicationService(): CommunicationService {
        return CommunicationService()
    }

    @Provides
    @Singleton
    fun provideSyncQueue(): SyncQueue {
        return SyncQueue()
    }

    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context
    ): ThemeManager {
        return ThemeManager(context)
    }

    @Provides
    @Singleton
    fun provideSocialMediaParser(): SocialMediaParser {
        return SocialMediaParser()
    }

    @Provides
    @Singleton
    fun provideLoginTrackingService(
        @ApplicationContext context: Context
    ): LoginTrackingService {
        return LoginTrackingService(context)
    }

    @Provides
    @Singleton
    fun provideToastViewModel(): ToastViewModel {
        return ToastViewModel()
    }

    @Provides
    @Singleton
    fun provideContactImportService(
        @ApplicationContext context: Context
    ): ContactImportService {
        return ContactImportService(context)
    }

    @Provides
    @Singleton
    fun provideAutoBackupManager(
        @ApplicationContext context: Context
    ): AutoBackupManager {
        return AutoBackupManager(context)
    }


    @Provides
    @Singleton
    fun provideFirebaseBackupRepository(
        firestore: FirebaseFirestore,
        database: NegocioListoDatabase,
        imageService: ImageService,
        @ApplicationContext context: Context
    ): FirebaseBackupRepository {
        return FirebaseBackupRepository(firestore, database, imageService, context)
    }

    @Provides
    @Singleton
    fun provideNotificationTokenManager(
        firestore: FirebaseFirestore
    ): com.negociolisto.app.notification.NotificationTokenManager {
        return com.negociolisto.app.notification.NotificationTokenManager(firestore)
    }

    @Provides
    @Singleton
    fun provideNotificationChannelManager(
        @ApplicationContext context: Context
    ): com.negociolisto.app.notification.NotificationChannelManager {
        return com.negociolisto.app.notification.NotificationChannelManager(context)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context,
        notificationChannelManager: com.negociolisto.app.notification.NotificationChannelManager
    ): com.negociolisto.app.notification.NotificationHelper {
        return com.negociolisto.app.notification.NotificationHelper(context, notificationChannelManager)
    }

    @Provides
    @Singleton
    fun provideNotificationTriggerService(
        notificationHelper: com.negociolisto.app.notification.NotificationHelper,
        collectionResponseRepository: com.negociolisto.app.domain.repository.CollectionResponseRepository,
        chatRepository: com.negociolisto.app.domain.repository.ChatRepository,
        inventoryRepository: com.negociolisto.app.domain.repository.InventoryRepository,
        usageLimitsService: com.negociolisto.app.data.service.UsageLimitsService,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): com.negociolisto.app.notification.NotificationTriggerService {
        return com.negociolisto.app.notification.NotificationTriggerService(
            notificationHelper,
            collectionResponseRepository,
            chatRepository,
            inventoryRepository,
            usageLimitsService,
            firebaseAuth,
            firestore
        )
    }

}