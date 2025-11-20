package com.negociolisto.app

import org.junit.Test
import org.junit.Assert.*

/**
 * 🧪 PRUEBAS UNITARIAS BÁSICAS
 * 
 * Estas son pruebas muy simples que no dependen de Android
 * para verificar que la configuración básica de testing funcione.
 */
class BasicUnitTest {
    
    @Test
    fun `basic assertion test should pass`() {
        // Esta es la prueba más básica posible
        assertTrue("Esta prueba siempre debería pasar", true)
    }
    
    @Test
    fun `math test should work`() {
        val result = 2 + 2
        assertEquals("2 + 2 debería ser 4", 4, result)
    }
    
    @Test
    fun `string test should work`() {
        val greeting = "Hola"
        val name = "Mundo"
        val message = "$greeting $name"
        
        assertEquals("Mensaje incorrecto", "Hola Mundo", message)
    }
    
    @Test
    fun `list operations test should work`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        
        assertEquals("Tamaño de lista incorrecto", 5, numbers.size)
        assertEquals("Primer elemento incorrecto", 1, numbers.first())
        assertEquals("Último elemento incorrecto", 5, numbers.last())
        assertTrue("Lista debería contener 3", numbers.contains(3))
    }
    
    @Test
    fun `null safety test should work`() {
        val nullableString: String? = null
        val nonNullString: String? = "Hola"
        
        assertNull("String debería ser null", nullableString)
        assertNotNull("String no debería ser null", nonNullString)
        assertEquals("Valor incorrecto", "Hola", nonNullString)
    }
}
