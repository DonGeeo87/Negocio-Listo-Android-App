package com.negociolisto.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.negociolisto.app.domain.model.CustomCategory
import kotlinx.datetime.LocalDateTime

/**
 * 📂 ENTIDAD DE CATEGORÍAS PERSONALIZADAS
 * 
 * Representación en base de datos de las categorías personalizadas del usuario.
 */
@Entity(tableName = "custom_categories")
data class CustomCategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val description: String?,
    val createdAt: String, // LocalDateTime como String para Room
    val updatedAt: String,
    val isActive: Boolean,
    val userId: String,
    val sortOrder: Int
) {
    /**
     * 🔄 CONVERTIR A MODELO DE DOMINIO
     */
    fun toDomain(): CustomCategory {
        return CustomCategory(
            id = id,
            name = name,
            icon = icon,
            color = color,
            description = description,
            createdAt = LocalDateTime.parse(createdAt),
            updatedAt = LocalDateTime.parse(updatedAt),
            isActive = isActive,
            userId = userId,
            sortOrder = sortOrder
        )
    }
    
    companion object {
        /**
         * 🏭 CREAR DESDE MODELO DE DOMINIO
         */
        fun fromDomain(category: CustomCategory): CustomCategoryEntity {
            return CustomCategoryEntity(
                id = category.id,
                name = category.name,
                icon = category.icon,
                color = category.color,
                description = category.description,
                createdAt = category.createdAt.toString(),
                updatedAt = category.updatedAt.toString(),
                isActive = category.isActive,
                userId = category.userId,
                sortOrder = category.sortOrder
            )
        }
    }
}
