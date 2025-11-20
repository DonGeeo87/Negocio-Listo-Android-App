package com.negociolisto.app.data.local.converter

import androidx.room.TypeConverter
import com.negociolisto.app.domain.model.InvoiceItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer

/**
 * 🔄 CONVERTER PARA INVOICE ITEMS
 * 
 * Convierte List<InvoiceItem> a JSON y viceversa.
 */
class InvoiceItemConverter {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private val listSerializer = ListSerializer(serializer<InvoiceItem>())
    
    @TypeConverter
    fun fromInvoiceItems(items: List<InvoiceItem>): String {
        return try {
            json.encodeToString(listSerializer, items)
        } catch (e: Exception) {
            // En caso de error, retornar JSON vacío
            "[]"
        }
    }
    
    @TypeConverter
    fun toInvoiceItems(jsonString: String): List<InvoiceItem> {
        return try {
            if (jsonString.isBlank() || jsonString == "null") {
                emptyList()
            } else {
                json.decodeFromString(listSerializer, jsonString)
            }
        } catch (e: Exception) {
            // En caso de error de deserialización, retornar lista vacía
            emptyList()
        }
    }
}

