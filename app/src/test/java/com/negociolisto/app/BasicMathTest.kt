package com.negociolisto.app

import org.junit.Test
import org.junit.Assert.*

/**
 * 🧪 PRUEBAS BÁSICAS DE MATEMÁTICAS
 * 
 * Estas son pruebas muy simples para verificar que la configuración
 * básica de testing funcione correctamente.
 */
class BasicMathTest {
    
    @Test
    fun `addition should work correctly`() {
        // Arrange
        val a = 5
        val b = 3
        
        // Act
        val result = a + b
        
        // Assert
        assertEquals("5 + 3 debería ser 8", 8, result)
    }
    
    @Test
    fun `subtraction should work correctly`() {
        // Arrange
        val a = 10
        val b = 4
        
        // Act
        val result = a - b
        
        // Assert
        assertEquals("10 - 4 debería ser 6", 6, result)
    }
    
    @Test
    fun `multiplication should work correctly`() {
        // Arrange
        val a = 7
        val b = 6
        
        // Act
        val result = a * b
        
        // Assert
        assertEquals("7 * 6 debería ser 42", 42, result)
    }
    
    @Test
    fun `division should work correctly`() {
        // Arrange
        val a = 15
        val b = 3
        
        // Act
        val result = a / b
        
        // Assert
        assertEquals("15 / 3 debería ser 5", 5, result)
    }
    
    @Test
    fun `modulo should work correctly`() {
        // Arrange
        val a = 17
        val b = 5
        
        // Act
        val result = a % b
        
        // Assert
        assertEquals("17 % 5 debería ser 2", 2, result)
    }
    
    @Test
    fun `power should work correctly`() {
        // Arrange
        val base = 2
        val exponent = 3
        
        // Act
        val result = Math.pow(base.toDouble(), exponent.toDouble()).toInt()
        
        // Assert
        assertEquals("2^3 debería ser 8", 8, result)
    }
    
    @Test
    fun `square root should work correctly`() {
        // Arrange
        val number = 16
        
        // Act
        val result = Math.sqrt(number.toDouble()).toInt()
        
        // Assert
        assertEquals("√16 debería ser 4", 4, result)
    }
    
    @Test
    fun `absolute value should work correctly`() {
        // Arrange
        val negativeNumber = -15
        val positiveNumber = 15
        
        // Act
        val result1 = Math.abs(negativeNumber)
        val result2 = Math.abs(positiveNumber)
        
        // Assert
        assertEquals("|-15| debería ser 15", 15, result1)
        assertEquals("|15| debería ser 15", 15, result2)
    }
    
    @Test
    fun `max and min should work correctly`() {
        // Arrange
        val a = 10
        val b = 20
        
        // Act
        val max = Math.max(a, b)
        val min = Math.min(a, b)
        
        // Assert
        assertEquals("max(10, 20) debería ser 20", 20, max)
        assertEquals("min(10, 20) debería ser 10", 10, min)
    }
    
    @Test
    fun `rounding should work correctly`() {
        // Arrange
        val number1 = 3.4
        val number2 = 3.6
        
        // Act
        val rounded1 = Math.round(number1).toInt()
        val rounded2 = Math.round(number2).toInt()
        
        // Assert
        assertEquals("3.4 redondeado debería ser 3", 3, rounded1)
        assertEquals("3.6 redondeado debería ser 4", 4, rounded2)
    }
}
