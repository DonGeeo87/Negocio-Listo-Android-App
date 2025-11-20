package com.negociolisto.app.data.local.entity

import androidx.room.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Entity(
    tableName = "collections",
    indices = [
        Index(value = ["name"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
        Index(value = ["updatedAt"]),
        Index(value = ["userId"]) // ✅ Índice para filtrar por usuario
    ]
)
data class CollectionEntity(
    @PrimaryKey val id: String,
    val userId: String, // ✅ ID del usuario propietario de la colección
    val name: String,
    val description: String?,
    val associatedCustomerIds: String, // JSON string de lista de IDs
    val createdAt: Long,
    val updatedAt: Long,
    val status: String,
    val color: String?,
    val enableChat: Boolean = true,
    val webTemplate: String = "MODERN" // CollectionWebTemplate.name
)

@Entity(
    tableName = "collection_items",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("collectionId"), Index("productId")]
)
data class CollectionItemEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val collectionId: String,
    val productId: String,
    val notes: String?,
    val displayOrder: Int,
    val isFeatured: Boolean,
    val specialPrice: Double?
)

data class CollectionWithItemsEntity(
    @Embedded val collection: CollectionEntity,
    @Relation(parentColumn = "id", entityColumn = "collectionId")
    val items: List<CollectionItemEntity>
)

fun CollectionEntity.toDomain(
    items: List<CollectionItemEntity>
): com.negociolisto.app.domain.model.Collection {
    return com.negociolisto.app.domain.model.Collection(
        id = id,
        name = name,
        description = description,
        items = items.map { it.toDomain() },
        associatedCustomerIds = if (associatedCustomerIds.isBlank()) {
            emptyList()
        } else {
            associatedCustomerIds.split(",").filter { it.isNotBlank() }
        },
        createdAt = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt).toLocalDateTime(TimeZone.currentSystemDefault()),
        status = com.negociolisto.app.domain.model.CollectionStatus.valueOf(status),
        color = color,
        enableChat = enableChat,
        webTemplate = try {
            com.negociolisto.app.domain.model.CollectionWebTemplate.valueOf(webTemplate)
        } catch (e: Exception) {
            com.negociolisto.app.domain.model.CollectionWebTemplate.MODERN
        }
    )
}

fun CollectionItemEntity.toDomain(): com.negociolisto.app.domain.model.CollectionItem = com.negociolisto.app.domain.model.CollectionItem(
    productId = productId,
    notes = notes,
    displayOrder = displayOrder,
    isFeatured = isFeatured,
    specialPrice = specialPrice
)

fun com.negociolisto.app.domain.model.Collection.toEntity(userId: String): Pair<CollectionEntity, List<CollectionItemEntity>> {
    val col = CollectionEntity(
        id = id,
        userId = userId, // ✅ Incluir userId del propietario
        name = name,
        description = description,
        associatedCustomerIds = associatedCustomerIds.joinToString(","),
        createdAt = createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        updatedAt = updatedAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        status = status.name,
        color = color,
        enableChat = enableChat,
        webTemplate = webTemplate.name
    )
    val itemsEntities = items.map { item ->
        CollectionItemEntity(
            collectionId = id,
            productId = item.productId,
            notes = item.notes,
            displayOrder = item.displayOrder,
            isFeatured = item.isFeatured,
            specialPrice = item.specialPrice
        )
    }
    return col to itemsEntities
}


