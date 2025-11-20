package com.negociolisto.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.negociolisto.app.domain.model.InspirationTip
import com.negociolisto.app.domain.model.TipCategory
import com.negociolisto.app.domain.model.TimeOfDay

/**
 * 💾 ENTITY DE TIPS DE INSPIRACIÓN
 * 
 * Representación en base de datos para los tips de inspiración.
 * Convierte entre el modelo de dominio y la entidad de Room.
 */

@Entity(tableName = "inspiration_tips")
data class InspirationTipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val category: String, // Serializado como String
    val timeOfDay: String, // Serializado como String
    val isUsed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 🔄 CONVERSIONES ENTRE ENTITY Y DOMAIN MODEL
 */
fun InspirationTipEntity.toDomainModel(): InspirationTip {
    return InspirationTip(
        id = this.id,
        content = this.content,
        category = TipCategory.valueOf(this.category),
        timeOfDay = TimeOfDay.valueOf(this.timeOfDay),
        isUsed = this.isUsed,
        createdAt = this.createdAt
    )
}

fun InspirationTip.toEntity(): InspirationTipEntity {
    return InspirationTipEntity(
        id = this.id,
        content = this.content,
        category = this.category.name,
        timeOfDay = this.timeOfDay.name,
        isUsed = this.isUsed,
        createdAt = this.createdAt
    )
}











