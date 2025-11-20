package com.negociolisto.app.ui.inventory

import com.negociolisto.app.ui.components.Formatters

/**
 * Modelo simple para estadísticas que consume la UI.
 */
data class InventoryStats(
    val totalProducts: Int,
    val totalInventoryValue: Double
) {
    fun getFormattedValue(): String = Formatters.formatClpWithSymbol(totalInventoryValue)
}


