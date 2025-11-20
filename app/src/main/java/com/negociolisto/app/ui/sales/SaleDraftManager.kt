package com.negociolisto.app.ui.sales

import android.content.Context
import android.content.SharedPreferences
import com.negociolisto.app.domain.model.SaleItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * 💾 GESTOR DE BORRADORES DE VENTAS
 * 
 * Guarda y restaura el estado de una venta en proceso para que el usuario
 * pueda recuperarla si cierra la pantalla accidentalmente.
 */
@Serializable
data class SaleDraft(
    val customerId: String? = null,
    val items: List<SaleItemData> = emptyList(),
    val total: Double = 0.0
)

@Serializable
data class SaleItemData(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double
) {
    fun toSaleItem(): SaleItem {
        return SaleItem(productId, productName, quantity, unitPrice)
    }
}

fun SaleItem.toSaleItemData(): SaleItemData {
    return SaleItemData(productId, productName, quantity, unitPrice)
}

class SaleDraftManager(private val context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("sale_drafts", Context.MODE_PRIVATE)
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Guarda el borrador de la venta actual
     */
    fun saveDraft(draft: SaleDraft) {
        try {
            val draftJson = json.encodeToString(draft)
            prefs.edit()
                .putString("current_draft", draftJson)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("SaleDraftManager", "Error guardando borrador: ${e.message}")
        }
    }
    
    /**
     * Restaura el borrador guardado
     */
    fun loadDraft(): SaleDraft? {
        return try {
            val draftJson = prefs.getString("current_draft", null)
            if (draftJson != null) {
                json.decodeFromString<SaleDraft>(draftJson)
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("SaleDraftManager", "Error cargando borrador: ${e.message}")
            null
        }
    }
    
    /**
     * Elimina el borrador guardado (después de guardar la venta)
     */
    fun clearDraft() {
        prefs.edit()
            .remove("current_draft")
            .apply()
    }
    
    /**
     * Verifica si hay un borrador guardado
     */
    fun hasDraft(): Boolean {
        return prefs.contains("current_draft")
    }
}

