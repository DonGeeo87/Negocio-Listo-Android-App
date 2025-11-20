package com.negociolisto.app

import org.junit.Test
import org.junit.Assert.*

/**
 * 🧪 PRUEBA DE CONFIGURACIÓN DE TESTING
 * 
 * Esta es una prueba muy simple para verificar que la configuración
 * básica de testing esté funcionando correctamente.
 */
class TestConfiguration {
    
    @Test
    fun `basic configuration test should pass`() {
        // Arrange
        val expected = true
        
        // Act
        val actual = true
        
        // Assert
        assertEquals("La configuración básica debería funcionar", expected, actual)
    }
    
    @Test
    fun `math operations test should work`() {
        // Arrange
        val a = 10
        val b = 5
        
        // Act
        val sum = a + b
        val difference = a - b
        val product = a * b
        val quotient = a / b
        
        // Assert
        assertEquals("Suma incorrecta", 15, sum)
        assertEquals("Resta incorrecta", 5, difference)
        assertEquals("Multiplicación incorrecta", 50, product)
        assertEquals("División incorrecta", 2, quotient)
    }
    
    @Test
    fun `string operations test should work`() {
        // Arrange
        val baseString = "NegocioListo"
        
        // Act
        val upperCase = baseString.uppercase()
        val lowerCase = baseString.lowercase()
        val length = baseString.length
        
        // Assert
        assertEquals("Mayúsculas incorrectas", "NEGOCIOLISTO", upperCase)
        assertEquals("Minúsculas incorrectas", "negociolisto", lowerCase)
        assertEquals("Longitud incorrecta", 11, length)
    }
}
