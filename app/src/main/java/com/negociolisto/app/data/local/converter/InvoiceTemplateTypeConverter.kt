package com.negociolisto.app.data.local.converter

import androidx.room.TypeConverter
import com.negociolisto.app.domain.model.InvoiceTemplateType

/**
 * 🔄 CONVERTER PARA INVOICE TEMPLATE TYPE
 * 
 * Convierte InvoiceTemplateType a String y viceversa.
 */
class InvoiceTemplateTypeConverter {
    
    @TypeConverter
    fun fromTemplateType(template: InvoiceTemplateType): String {
        return template.name
    }
    
    @TypeConverter
    fun toTemplateType(templateString: String): InvoiceTemplateType {
        return InvoiceTemplateType.valueOf(templateString)
    }
}

