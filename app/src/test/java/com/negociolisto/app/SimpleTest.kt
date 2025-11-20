package com.negociolisto.app

import org.junit.Test
import org.junit.Assert.*

/**
 * 🧪 PRUEBA SIMPLE PARA VERIFICAR CONFIGURACIÓN
 * 
 * Esta es una prueba básica para verificar que el sistema de testing
 * esté funcionando correctamente.
 */
class SimpleTest {
    
    @Test
    fun `basic math test should pass`() {
        // Arrange
        val a = 2
        val b = 3
        
        // Act
        val result = a + b
        
        // Assert
        assertEquals("2 + 3 debería ser 5", 5, result)
    }
    
    @Test
    fun `string concatenation test should pass`() {
        // Arrange
        val firstName = "Juan"
        val lastName = "Pérez"
        
        // Act
        val fullName = "$firstName $lastName"
        
        // Assert
        assertEquals("Nombre completo incorrecto", "Juan Pérez", fullName)
    }
    
    @Test
    fun `boolean logic test should pass`() {
        // Arrange
        val isTrue = true
        val isFalse = false
        
        // Act & Assert
        assertTrue("isTrue debería ser verdadero", isTrue)
        assertFalse("isFalse debería ser falso", isFalse)
    }
}
