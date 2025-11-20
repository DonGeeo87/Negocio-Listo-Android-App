package com.negociolisto.app.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 🔄 MIGRACIÓN DE VERSIÓN 14 A 15
 *
 * Agrega tablas locales para pedidos (collection_responses) e items (collection_response_items)
 * para habilitar el uso offline de la sección de pedidos.
 */
val MIGRATION_14_TO_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `collection_responses` (
                `id` TEXT NOT NULL,
                `collectionId` TEXT NOT NULL,
                `customerId` TEXT,
                `accessToken` TEXT,
                `clientName` TEXT NOT NULL,
                `clientEmail` TEXT NOT NULL,
                `clientPhone` TEXT NOT NULL,
                `deliveryMethod` TEXT NOT NULL,
                `address` TEXT,
                `paymentMethod` TEXT NOT NULL,
                `desiredDate` INTEGER,
                `urgent` INTEGER NOT NULL,
                `observations` TEXT,
                `subtotal` REAL NOT NULL,
                `itemCount` INTEGER NOT NULL,
                `status` TEXT NOT NULL,
                `feedbackComments` TEXT,
                `consentToContact` INTEGER NOT NULL,
                `businessNotes` TEXT,
                `locationCity` TEXT,
                `locationRegion` TEXT,
                `tags` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `needsSync` INTEGER NOT NULL DEFAULT 0,
                `lastSyncError` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_responses_collectionId` ON `collection_responses`(`collectionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_responses_status` ON `collection_responses`(`status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_responses_needsSync` ON `collection_responses`(`needsSync`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_responses_createdAt` ON `collection_responses`(`createdAt`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `collection_response_items` (
                `responseId` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `quantity` INTEGER NOT NULL,
                `rating` INTEGER,
                `notes` TEXT,
                `customization` TEXT,
                PRIMARY KEY(`responseId`, `productId`),
                FOREIGN KEY(`responseId`) REFERENCES `collection_responses`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_response_items_responseId` ON `collection_response_items`(`responseId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_collection_response_items_productId` ON `collection_response_items`(`productId`)")
    }
}

