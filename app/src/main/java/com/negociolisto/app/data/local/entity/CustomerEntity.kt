package com.negociolisto.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.negociolisto.app.domain.model.Customer
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

/**
 * 👥 ENTIDAD DE CLIENTE PARA ROOM
 */
@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["email"]),
        Index(value = ["phone"]),
        Index(value = ["name"])
    ]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val companyName: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val totalPurchases: Double,
    val lastPurchaseDate: Long?,
    val notes: String?,
    val createdAt: Long,
    val accessToken: String? = null
)

/**
 * 🔄 EXTENSIONES PARA CONVERSIÓN
 */
fun CustomerEntity.toDomain(): Customer = Customer(
    id = id,
    name = name,
    companyName = companyName,
    email = email,
    phone = phone,
    address = address,
    totalPurchases = totalPurchases,
    lastPurchaseDate = lastPurchaseDate?.let { 
        Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) 
    },
    notes = notes,
    createdAt = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault()),
    accessToken = accessToken
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    id = id,
    name = name,
    companyName = companyName,
    email = email,
    phone = phone,
    address = address,
    totalPurchases = totalPurchases,
    lastPurchaseDate = lastPurchaseDate?.toInstant(TimeZone.currentSystemDefault())?.toEpochMilliseconds(),
    notes = notes,
    createdAt = createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
    accessToken = accessToken
)
