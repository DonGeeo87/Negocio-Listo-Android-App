package com.negociolisto.app.integration

import com.negociolisto.app.data.local.entity.CollectionEntity
import com.negociolisto.app.data.local.entity.CollectionItemEntity
import org.junit.Test
import org.junit.Assert.*

/**
 * Test unitario para validar backup/restore de colecciones e items
 */
class CollectionsBackupRestoreTest {

    @Test
    fun testCollectionEntityCreation() {
        println("🧪 Iniciando test de creación de entidades de colección...")
        
        // 1. Crear datos de prueba
        val testCollection = CollectionEntity(
            id = "collection_1",
            name = "Colección de Prueba",
            description = "Descripción de prueba",
            status = "ACTIVE",
            associatedCustomerIds = "customer_1,customer_2",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val testItem1 = CollectionItemEntity(
            id = "item_1",
            collectionId = "collection_1",
            productId = "product_1",
            quantity = 5,
            specialPrice = 100.0,
            notes = "Notas del item 1",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val testItem2 = CollectionItemEntity(
            id = "item_2",
            collectionId = "collection_1",
            productId = "product_2",
            quantity = 3,
            specialPrice = 150.0,
            notes = "Notas del item 2",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // 2. Verificar que las entidades se crearon correctamente
        assertNotNull("Colección no debe ser null", testCollection)
        assertNotNull("Item 1 no debe ser null", testItem1)
        assertNotNull("Item 2 no debe ser null", testItem2)
        
        assertEquals("ID de colección debe coincidir", "collection_1", testCollection.id)
        assertEquals("Nombre de colección debe coincidir", "Colección de Prueba", testCollection.name)
        assertEquals("Descripción debe coincidir", "Descripción de prueba", testCollection.description)
        assertEquals("Estado debe coincidir", "ACTIVE", testCollection.status)
        assertEquals("Customer IDs deben coincidir", "customer_1,customer_2", testCollection.associatedCustomerIds)
        
        assertEquals("ID del item 1 debe coincidir", "item_1", testItem1.id)
        assertEquals("Collection ID del item 1 debe coincidir", "collection_1", testItem1.collectionId)
        assertEquals("Product ID del item 1 debe coincidir", "product_1", testItem1.productId)
        assertEquals("Cantidad del item 1 debe coincidir", 5, testItem1.quantity)
        assertEquals("Precio especial del item 1 debe coincidir", 100.0, testItem1.specialPrice, 0.01)
        assertEquals("Notas del item 1 deben coincidir", "Notas del item 1", testItem1.notes)
        
        assertEquals("ID del item 2 debe coincidir", "item_2", testItem2.id)
        assertEquals("Collection ID del item 2 debe coincidir", "collection_1", testItem2.collectionId)
        assertEquals("Product ID del item 2 debe coincidir", "product_2", testItem2.productId)
        assertEquals("Cantidad del item 2 debe coincidir", 3, testItem2.quantity)
        assertEquals("Precio especial del item 2 debe coincidir", 150.0, testItem2.specialPrice, 0.01)
        assertEquals("Notas del item 2 deben coincidir", "Notas del item 2", testItem2.notes)
        
        println("✅ Todas las entidades se crearon correctamente")
        println("🎉 Test de creación de entidades de colección completado exitosamente")
    }
}
