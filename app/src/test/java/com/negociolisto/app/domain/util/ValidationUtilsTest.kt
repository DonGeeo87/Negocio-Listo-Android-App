package com.negociolisto.app.domain.util

import org.junit.Test
import org.junit.Assert.*

/**
 * 🧪 PRUEBAS UNITARIAS PARA VALIDACIONES
 * 
 * Esta clase contiene pruebas para verificar que nuestras validaciones
 * funcionen correctamente. Es como tener un "laboratorio de pruebas"
 * donde verificamos que cada función haga exactamente lo que esperamos.
 * 
 * ¿Por qué hacer pruebas?
 * - Asegurar que las validaciones funcionen correctamente
 * - Detectar errores antes de que lleguen a los usuarios
 * - Documentar cómo deben comportarse las funciones
 * - Facilitar cambios futuros sin romper funcionalidad existente
 * - Dar confianza al equipo de desarrollo
 */
class ValidationUtilsTest {
    
    /**
     * 📧 PRUEBAS DE VALIDACIÓN DE EMAIL
     */
    @Test
    fun `validateEmail should return success for valid emails`() {
        // Arrange (Preparar): Definimos emails válidos
        val validEmails = listOf(
            "juan@gmail.com",
            "maria.garcia@empresa.co",
            "admin@negocio-listo.com",
            "usuario123@dominio.org",
            "test+tag@example.net"
        )
        
        // Act & Assert (Actuar y Verificar): Probamos cada email
        validEmails.forEach { email ->
            val result = ValidationUtils.validateEmail(email)
            assertTrue("Email '$email' debería ser válido", result.isSuccess())
        }
    }
    
    @Test
    fun `validateEmail should return error for invalid emails`() {
        // Arrange: Definimos emails inválidos y sus errores esperados
        val invalidEmails = mapOf(
            "" to "El email es obligatorio",
            "   " to "El email es obligatorio",
            "juan" to "El formato del email no es válido",
            "juan@" to "El formato del email no es válido",
            "juan@gmail" to "El formato del email no es válido",
            "@gmail.com" to "El formato del email no es válido",
            "juan..garcia@gmail.com" to "El formato del email no es válido"
        )
        
        // Act & Assert: Probamos cada email inválido
        invalidEmails.forEach { (email, expectedError) ->
            val result = ValidationUtils.validateEmail(email)
            assertTrue("Email '$email' debería ser inválido", result.isError())
            assertEquals("Mensaje de error incorrecto para '$email'", 
                expectedError, result.getErrorMessage())
        }
    }
    
    /**
     * 📞 PRUEBAS DE VALIDACIÓN DE TELÉFONO
     */
    @Test
    fun `validatePhone should return success for valid Colombian phones`() {
        // Arrange: Teléfonos válidos en diferentes formatos
        val validPhones = listOf(
            "3001234567",           // Formato básico
            "+573001234567",        // Con código de país
            "573001234567",         // Con código sin +
            "(300) 123-4567",       // Con paréntesis y guiones
            "300 123 4567",         // Con espacios
            "3101234567",           // Otro operador
            "3201234567"            // Otro operador
        )
        
        validPhones.forEach { phone ->
            val result = ValidationUtils.validatePhone(phone)
            assertTrue("Teléfono '$phone' debería ser válido", result.isSuccess())
        }
    }
    
    @Test
    fun `validatePhone should return error for invalid phones`() {
        val invalidPhones = mapOf(
            "" to "El teléfono es obligatorio",
            "123" to "El teléfono debe tener al menos 10 dígitos",
            "12345678901234" to "El teléfono es demasiado largo",
            "2001234567" to "El formato del teléfono no es válido", // No empieza con 3
            "abcd123456" to "El formato del teléfono no es válido"  // Contiene letras
        )
        
        invalidPhones.forEach { (phone, expectedError) ->
            val result = ValidationUtils.validatePhone(phone)
            assertTrue("Teléfono '$phone' debería ser inválido", result.isError())
            assertEquals("Mensaje de error incorrecto para '$phone'", 
                expectedError, result.getErrorMessage())
        }
    }
    
    /**
     * 🔐 PRUEBAS DE VALIDACIÓN DE CONTRASEÑA
     */
    @Test
    fun `validatePassword should return success for valid passwords`() {
        val validPasswords = listOf(
            "abc123",              // Mínimo válido
            "MiContraseña123",     // Con mayúsculas
            "password2024!",       // Con símbolos
            "123456a"              // Números y letra
        )
        
        validPasswords.forEach { password ->
            val result = ValidationUtils.validatePassword(password)
            assertTrue("Contraseña '$password' debería ser válida", result.isSuccess())
        }
    }
    
    @Test
    fun `validatePassword should return error for invalid passwords`() {
        val invalidPasswords = mapOf(
            "" to "La contraseña es obligatoria",
            "123" to "La contraseña debe tener al menos 6 caracteres",
            "123456" to "La contraseña debe contener al menos una letra",
            "abcdef" to "La contraseña debe contener al menos un número"
        )
        
        invalidPasswords.forEach { (password, expectedError) ->
            val result = ValidationUtils.validatePassword(password)
            assertTrue("Contraseña '$password' debería ser inválida", result.isError())
            assertEquals("Mensaje de error incorrecto para '$password'", 
                expectedError, result.getErrorMessage())
        }
    }
    
    /**
     * 👤 PRUEBAS DE VALIDACIÓN DE NOMBRE
     */
    @Test
    fun `validateName should return success for valid names`() {
        val validNames = listOf(
            "Juan",
            "María García",
            "José Luis",
            "Ana-Sofía",
            "O'Connor",
            "Empresa S.A.S."
        )
        
        validNames.forEach { name ->
            val result = ValidationUtils.validateName(name)
            assertTrue("Nombre '$name' debería ser válido", result.isSuccess())
        }
    }
    
    @Test
    fun `validateName should return error for invalid names`() {
        val invalidNames = mapOf(
            "" to "El nombre es obligatorio",
            "A" to "El nombre debe tener al menos 2 caracteres",
            "Juan123" to "El nombre contiene caracteres no válidos",
            "María@García" to "El nombre contiene caracteres no válidos"
        )
        
        invalidNames.forEach { (name, expectedError) ->
            val result = ValidationUtils.validateName(name)
            assertTrue("Nombre '$name' debería ser inválido", result.isError())
            assertEquals("Mensaje de error incorrecto para '$name'", 
                expectedError, result.getErrorMessage())
        }
    }
    
    /**
     * 🔢 PRUEBAS DE VALIDACIÓN DE SKU
     */
    @Test
    fun `validateSku should return success for valid SKUs`() {
        val validSkus = listOf(
            "ABC123",
            "PROD-001",
            "CAM_AZUL_M",
            "SKU123456",
            "A1B2C3"
        )
        
        validSkus.forEach { sku ->
            val result = ValidationUtils.validateSku(sku)
            assertTrue("SKU '$sku' debería ser válido", result.isSuccess())
        }
    }
    
    @Test
    fun `validateSku should return error for invalid SKUs`() {
        val invalidSkus = mapOf(
            "" to "El SKU es obligatorio",
            "AB" to "El SKU debe tener al menos 3 caracteres",
            "SKU CON ESPACIOS" to "El SKU solo puede contener letras, números, guiones y guiones bajos",
            "SKU@ESPECIAL" to "El SKU solo puede contener letras, números, guiones y guiones bajos"
        )
        
        invalidSkus.forEach { (sku, expectedError) ->
            val result = ValidationUtils.validateSku(sku)
            assertTrue("SKU '$sku' debería ser inválido", result.isError())
            assertEquals("Mensaje de error incorrecto para '$sku'", 
                expectedError, result.getErrorMessage())
        }
    }
    
    /**
     * 💰 PRUEBAS DE VALIDACIÓN DE PRECIO
     */
    @Test
    fun `validatePrice should return success for valid prices`() {
        val validPrices = listOf(
            0.01,      // Precio mínimo
            100.0,     // Precio normal
            1500.50,   // Con decimales
            999999.99  // Precio alto pero válido
        )
        
        validPrices.forEach { price ->
            val result = ValidationUtils.validatePrice(price)
            assertTrue("Precio '$price' debería ser válido", result.isSuccess())
        }
    }
    
    @Test
    fun `validatePrice should return error for invalid prices`() {
        val invalidPrices = mapOf(
            -100.0 to "El precio no puede ser negativo",
            0.0 to "El precio debe ser mayor a cero",
            1000000000.0 to "El precio es demasiado alto"
        )
        
        invalidPrices.forEach { (price, expectedError) ->
            val result = ValidationUtils.validatePrice(price)
            assertTrue("Precio '$price' debería ser inválido", result.isError())
            assertEquals("Mensaje de error incorrecto para '$price'", 
                expectedError, result.getErrorMessage())
        }
    }
    
    /**
     * 📦 PRUEBAS DE VALIDACIÓN DE STOCK
     */
    @Test
    fun `validateStock should return success for valid stock quantities`() {
        val validStocks = listOf(0, 1, 100, 1000, 999999)
        
        validStocks.forEach { stock ->
            val result = ValidationUtils.validateStock(stock)
            assertTrue("Stock '$stock' debería ser válido", result.isSuccess())
        }
    }
    
    @Test
    fun `validateStock should return error for invalid stock quantities`() {
        val invalidStocks = mapOf(
            -1 to "El stock no puede ser negativo",
            -100 to "El stock no puede ser negativo",
            1000000 to "El stock es demasiado alto"
        )
        
        invalidStocks.forEach { (stock, expectedError) ->
            val result = ValidationUtils.validateStock(stock)
            assertTrue("Stock '$stock' debería ser inválido", result.isError())
            assertEquals("Mensaje de error incorrecto para '$stock'", 
                expectedError, result.getErrorMessage())
        }
    }
    
    /**
     * ✅ PRUEBAS DE VALIDACIÓN MÚLTIPLE
     */
    @Test
    fun `validateMultiple should return success when all validations pass`() {
        // Arrange: Todas las validaciones exitosas
        val validations = arrayOf(
            ValidationResult.Success,
            ValidationResult.Success,
            ValidationResult.Success
        )
        
        // Act
        val result = ValidationUtils.validateMultiple(*validations)
        
        // Assert
        assertTrue("Validación múltiple debería ser exitosa", result.isSuccess())
    }
    
    @Test
    fun `validateMultiple should return combined errors when validations fail`() {
        // Arrange: Algunas validaciones con error
        val validations = arrayOf(
            ValidationResult.Success,
            ValidationResult.Error("Error 1"),
            ValidationResult.Error("Error 2"),
            ValidationResult.Success
        )
        
        // Act
        val result = ValidationUtils.validateMultiple(*validations)
        
        // Assert
        assertTrue("Validación múltiple debería fallar", result.isError())
        assertEquals("Errores combinados incorrectos", 
            "Error 1, Error 2", result.getErrorMessage())
    }
    
    /**
     * 🧹 PRUEBAS DE FUNCIONES DE LIMPIEZA
     */
    @Test
    fun `cleanPhone should remove non-numeric characters except plus`() {
        val testCases = mapOf(
            "(300) 123-4567" to "3001234567",
            "+57 300 123 4567" to "+573001234567",
            "300.123.4567" to "3001234567",
            "300-123-4567" to "3001234567"
        )
        
        testCases.forEach { (input, expected) ->
            val result = ValidationUtils.cleanPhone(input)
            assertEquals("Limpieza de teléfono incorrecta para '$input'", expected, result)
        }
    }
    
    @Test
    fun `cleanPrice should extract numeric value from string`() {
        val testCases = mapOf(
            "$100.50" to 100.50,
            "1,500.99" to 1500.99,
            "abc123.45def" to 123.45,
            "invalid" to null
        )
        
        testCases.forEach { (input, expected) ->
            val result = ValidationUtils.cleanPrice(input)
            assertEquals("Limpieza de precio incorrecta para '$input'", expected, result)
        }
    }
}

/**
 * 📚 CONCEPTOS IMPORTANTES DE TESTING:
 * 
 * 1. Unit Testing: Pruebas de unidades individuales de código
 * 2. Test Cases: Casos específicos que queremos probar
 * 3. Arrange-Act-Assert: Patrón para organizar pruebas
 * 4. Edge Cases: Casos límite o extremos
 * 5. Test Data: Datos específicos para las pruebas
 * 
 * ESTRUCTURA DE UNA PRUEBA:
 * 
 * ```kotlin
 * @Test
 * fun `nombre descriptivo de lo que prueba`() {
 *     // Arrange (Preparar): Configurar datos de prueba
 *     val input = "dato de entrada"
 *     val expected = "resultado esperado"
 *     
 *     // Act (Actuar): Ejecutar la función que estamos probando
 *     val result = functionToTest(input)
 *     
 *     // Assert (Verificar): Comprobar que el resultado es correcto
 *     assertEquals(expected, result)
 * }
 * ```
 * 
 * TIPOS DE PRUEBAS INCLUIDAS:
 * 
 * 1. **Happy Path**: Casos donde todo funciona correctamente
 *    - Emails válidos → Validación exitosa
 *    - Precios positivos → Validación exitosa
 * 
 * 2. **Error Cases**: Casos donde esperamos errores
 *    - Email sin @ → Error de formato
 *    - Precio negativo → Error de valor
 * 
 * 3. **Edge Cases**: Casos límite
 *    - String vacío → Error de campo obligatorio
 *    - Valores muy grandes → Error de límite
 * 
 * 4. **Data Transformation**: Pruebas de limpieza de datos
 *    - Teléfono con espacios → Solo números
 *    - Precio con símbolos → Solo números
 * 
 * BENEFICIOS DE ESTAS PRUEBAS:
 * - Detectan errores antes de que lleguen a producción
 * - Documentan cómo deben comportarse las funciones
 * - Facilitan refactoring sin miedo a romper funcionalidad
 * - Dan confianza al equipo para hacer cambios
 * - Sirven como ejemplos de uso de las funciones
 */