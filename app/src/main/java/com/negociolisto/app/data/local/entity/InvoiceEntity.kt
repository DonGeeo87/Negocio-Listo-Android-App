package com.negociolisto.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.negociolisto.app.data.local.converter.DateTimeConverter
import com.negociolisto.app.data.local.converter.InvoiceItemConverter
import com.negociolisto.app.data.local.converter.InvoiceTemplateTypeConverter
import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.domain.model.InvoiceItem
import com.negociolisto.app.domain.model.InvoiceTemplateType
import kotlinx.datetime.LocalDateTime

/**
 * 📄 ENTIDAD DE FACTURA
 * 
 * Representa una factura en la base de datos local.
 */
@Entity(tableName = "invoices")
@TypeConverters(
    DateTimeConverter::class,
    InvoiceItemConverter::class,
    InvoiceTemplateTypeConverter::class
)
data class InvoiceEntity(
    @PrimaryKey
    val id: String,
    val number: String,
    val saleId: String?,
    val customerId: String?,
    val items: List<InvoiceItem>,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val date: LocalDateTime,
    val template: InvoiceTemplateType,
    val notes: String?
)

/**
 * 🔄 CONVERSIÓN DE ENTIDAD A DOMINIO
 */
fun InvoiceEntity.toDomain(): Invoice {
    return Invoice(
        id = id,
        number = number,
        saleId = saleId,
        customerId = customerId,
        items = items,
        subtotal = subtotal,
        tax = tax,
        total = total,
        date = date,
        template = template,
        notes = notes
    )
}

/**
 * 🔄 CONVERSIÓN DE DOMINIO A ENTIDAD
 */
fun Invoice.toEntity(): InvoiceEntity {
    return InvoiceEntity(
        id = id,
        number = number,
        saleId = saleId,
        customerId = customerId,
        items = items,
        subtotal = subtotal,
        tax = tax,
        total = total,
        date = date,
        template = template,
        notes = notes
    )
}

