package com.negociolisto.app.ui.components

import kotlinx.datetime.LocalDateTime
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

object Formatters {
    private val clLocale: Locale = Locale("es", "CL")
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(clLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    
    /**
     * Formateador de números para pesos chilenos (formato: $1.000)
     * Usa punto como separador de miles y sin decimales
     */
    val clpNumberFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(clLocale)
        symbols.groupingSeparator = '.' // Punto como separador de miles
        DecimalFormat("#,##0", symbols).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }
    
    /**
     * Formateador de números con decimales para pesos chilenos (formato: $1.000,00)
     */
    val clpNumberFormatWithDecimals: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(clLocale)
        symbols.groupingSeparator = '.' // Punto como separador de miles
        symbols.decimalSeparator = ',' // Coma como separador decimal
        DecimalFormat("#,##0.00", symbols)
    }

    fun formatClp(amount: Double): String {
        return currencyFormat.format(amount)
    }
    
    /**
     * Formatea un número como peso chileno sin símbolo de moneda (formato: 1.000)
     */
    fun formatClpNumber(amount: Double): String {
        return clpNumberFormat.format(amount)
    }
    
    /**
     * Formatea un número como peso chileno con símbolo (formato: $1.000)
     */
    fun formatClpWithSymbol(amount: Double): String {
        return "$${formatClpNumber(amount)}"
    }
    
    /**
     * Formatea un valor de entrada mientras el usuario escribe (formato: 1.000)
     * Elimina caracteres no numéricos y agrega puntos como separadores de miles
     */
    fun formatInputValue(input: String): String {
        // Eliminar todo excepto números
        val numbersOnly = input.filter { it.isDigit() }
        if (numbersOnly.isEmpty()) return ""
        
        // Convertir a número y formatear
        val number = numbersOnly.toLongOrNull() ?: return input
        return clpNumberFormat.format(number)
    }
    
    /**
     * Limpia un valor formateado para convertirlo a número
     * Elimina puntos y otros caracteres de formato
     */
    fun cleanFormattedValue(formatted: String): String {
        return formatted.replace(".", "").replace(",", "").replace("$", "").trim()
    }

    /**
     * Formatea una fecha y hora usando kotlinx.datetime directamente
     */
    fun formatDate(dateTime: LocalDateTime): String {
        val day = dateTime.dayOfMonth.toString().padStart(2, '0')
        val month = dateTime.monthNumber.toString().padStart(2, '0')
        val year = dateTime.year
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        return "$day/$month/$year $hour:$minute"
    }
}


