package com.negociolisto.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.negociolisto.app.domain.model.CollectionResponse
import com.negociolisto.app.domain.model.OrderItem
import com.negociolisto.app.domain.model.OrderLocation
import com.negociolisto.app.domain.model.OrderStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Entity(
    tableName = "collection_responses",
    indices = [
        Index(value = ["collectionId"]),
        Index(value = ["status"]),
        Index(value = ["needsSync"]),
        Index(value = ["createdAt"])
    ]
)
data class CollectionResponseEntity(
    @PrimaryKey val id: String,
    val collectionId: String,
    val customerId: String?,
    val accessToken: String?,
    val clientName: String,
    val clientEmail: String,
    val clientPhone: String,
    val deliveryMethod: String,
    val address: String?,
    val paymentMethod: String,
    val desiredDate: Long?,
    val urgent: Boolean,
    val observations: String?,
    val subtotal: Double,
    val itemCount: Int,
    val status: String,
    val feedbackComments: String?,
    val consentToContact: Boolean,
    val businessNotes: String?,
    val locationCity: String?,
    val locationRegion: String?,
    val tags: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val needsSync: Boolean = false,
    val lastSyncError: String? = null
)

@Entity(
    tableName = "collection_response_items",
    primaryKeys = ["responseId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionResponseEntity::class,
            parentColumns = ["id"],
            childColumns = ["responseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["responseId"]),
        Index(value = ["productId"])
    ]
)
data class CollectionResponseItemEntity(
    val responseId: String,
    val productId: String,
    val quantity: Int,
    val rating: Int?,
    val notes: String?,
    val customization: String?
)

data class CollectionResponseWithItemsEntity(
    @Embedded val response: CollectionResponseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "responseId"
    )
    val items: List<CollectionResponseItemEntity>
)

fun CollectionResponseWithItemsEntity.toDomain(): CollectionResponse {
    return response.toDomain(items)
}

fun CollectionResponseEntity.toDomain(
    items: List<CollectionResponseItemEntity>
): CollectionResponse {
    val created = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault())
    val updated = Instant.fromEpochMilliseconds(updatedAt).toLocalDateTime(TimeZone.currentSystemDefault())
    val desired = desiredDate?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) }

    val mappedItems = items.associate { entity ->
        entity.productId to OrderItem(
            quantity = entity.quantity,
            rating = entity.rating,
            notes = entity.notes,
            customization = entity.customization
        )
    }

    val tagList = tags
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?: emptyList()

    val location = if (locationCity != null || locationRegion != null) {
        OrderLocation(city = locationCity, region = locationRegion)
    } else {
        null
    }

    return CollectionResponse(
        id = id,
        collectionId = collectionId,
        customerId = customerId,
        accessToken = accessToken,
        clientName = clientName,
        clientEmail = clientEmail,
        clientPhone = clientPhone,
        deliveryMethod = deliveryMethod,
        address = address,
        paymentMethod = paymentMethod,
        desiredDate = desired,
        urgent = urgent,
        observations = observations,
        items = mappedItems,
        subtotal = subtotal,
        itemCount = itemCount,
        status = OrderStatus.valueOf(status),
        feedbackComments = feedbackComments,
        consentToContact = consentToContact,
        businessNotes = businessNotes,
        location = location,
        tags = tagList,
        createdAt = created,
        updatedAt = updated
    )
}

fun CollectionResponse.toEntity(
    needsSync: Boolean = false,
    lastSyncError: String? = null
): Pair<CollectionResponseEntity, List<CollectionResponseItemEntity>> {
    val createdMillis = createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    val updatedMillis = updatedAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    val desiredMillis = desiredDate?.toInstant(TimeZone.currentSystemDefault())?.toEpochMilliseconds()

    val entity = CollectionResponseEntity(
        id = id,
        collectionId = collectionId,
        customerId = customerId,
        accessToken = accessToken,
        clientName = clientName,
        clientEmail = clientEmail,
        clientPhone = clientPhone,
        deliveryMethod = deliveryMethod,
        address = address,
        paymentMethod = paymentMethod,
        desiredDate = desiredMillis,
        urgent = urgent,
        observations = observations,
        subtotal = subtotal,
        itemCount = itemCount,
        status = status.name,
        feedbackComments = feedbackComments,
        consentToContact = consentToContact,
        businessNotes = businessNotes,
        locationCity = location?.city,
        locationRegion = location?.region,
        tags = if (tags.isNotEmpty()) tags.joinToString(",") else null,
        createdAt = createdMillis,
        updatedAt = updatedMillis,
        needsSync = needsSync,
        lastSyncError = lastSyncError
    )

    val itemEntities = items.map { (productId, orderItem) ->
        CollectionResponseItemEntity(
            responseId = id,
            productId = productId,
            quantity = orderItem.quantity,
            rating = orderItem.rating,
            notes = orderItem.notes,
            customization = orderItem.customization
        )
    }

    return entity to itemEntities
}

