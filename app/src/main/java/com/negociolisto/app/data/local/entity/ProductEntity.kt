package com.negociolisto.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.negociolisto.app.domain.model.Product
import com.negociolisto.app.domain.model.CustomCategory
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

/**
 * Entidad de producto para Room
 */
@Entity(
    tableName = "products",
    indices = [
        Index(value = ["name"]),
        Index(value = ["sku"]),
        Index(value = ["customCategoryId"]),
        Index(value = ["isActive"])
    ]
)
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val sku: String,
    val purchasePrice: Double,
    val salePrice: Double,
    val stockQuantity: Int,
    val minimumStock: Int,
    val customCategoryId: String, // ID de la categoría personalizada
    val supplier: String?,
    val photoUrl: String?,
    val thumbnailUrl: String?,
    val imageBackupUrl: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    description = description,
    sku = sku,
    purchasePrice = purchasePrice,
    salePrice = salePrice,
    stockQuantity = stockQuantity,
    minimumStock = minimumStock,
    customCategoryId = customCategoryId,
    supplier = supplier,
    photoUrl = photoUrl,
    thumbnailUrl = thumbnailUrl,
    imageBackupUrl = imageBackupUrl,
    createdAt = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault()),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt).toLocalDateTime(TimeZone.currentSystemDefault()),
    isActive = isActive
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    sku = sku,
    purchasePrice = purchasePrice,
    salePrice = salePrice,
    stockQuantity = stockQuantity,
    minimumStock = minimumStock,
    customCategoryId = customCategoryId,
    supplier = supplier,
    photoUrl = photoUrl,
    thumbnailUrl = thumbnailUrl,
    imageBackupUrl = imageBackupUrl,
    isActive = isActive,
    createdAt = createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
    updatedAt = updatedAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
)


