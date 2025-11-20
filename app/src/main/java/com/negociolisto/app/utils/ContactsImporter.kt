package com.negociolisto.app.utils

import android.content.Context
import android.provider.ContactsContract
import com.negociolisto.app.ui.setup.CustomerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 📞 IMPORTADOR DE CONTACTOS
 * 
 * Utilidad para leer contactos del dispositivo y convertirlos a CustomerData.
 * Permite importar contactos durante la configuración inicial de clientes.
 */
object ContactsImporter {
    
    /**
     * Obtener todos los contactos del dispositivo (síncrono - mantener para compatibilidad)
     * 
     * @param context Contexto de la aplicación
     * @return Lista de contactos convertidos a CustomerData
     * @deprecated Usar getDeviceContactsAsync para mejor rendimiento
     */
    fun getDeviceContacts(context: Context): List<CustomerData> {
        val contacts = mutableListOf<CustomerData>()
        
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            ),
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            
            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: ""
                val phone = it.getString(phoneIndex) ?: ""
                val contactId = it.getString(contactIdIndex) ?: ""
                
                // Solo agregar si tiene nombre y teléfono
                if (name.isNotBlank() && phone.isNotBlank()) {
                    // Obtener email del contacto si existe
                    val email = getContactEmail(context, contactId)
                    
                    contacts.add(
                        CustomerData(
                            name = name,
                            phone = phone,
                            email = email
                        )
                    )
                }
            }
        }
        
        // Eliminar duplicados basándose en el nombre
        return contacts.distinctBy { it.name }
    }
    
    /**
     * Obtener todos los contactos del dispositivo de forma asíncrona con indicador de progreso
     * 
     * @param context Contexto de la aplicación
     * @param onProgress Callback que se llama con el progreso (0.0 a 1.0) y el total de contactos
     * @return Lista de contactos convertidos a CustomerData
     */
    suspend fun getDeviceContactsAsync(
        context: Context,
        onProgress: (Float, Int) -> Unit = { _, _ -> }
    ): List<CustomerData> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<CustomerData>()
        
        // Primero obtener el total de contactos para calcular el progreso
        val countCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
            null,
            null,
            null
        )
        
        val totalContacts = countCursor?.count ?: 0
        countCursor?.close()
        
        if (totalContacts == 0) {
            return@withContext emptyList()
        }
        
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            ),
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            
            var processedCount = 0
            
            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: ""
                val phone = it.getString(phoneIndex) ?: ""
                val contactId = it.getString(contactIdIndex) ?: ""
                
                // Solo agregar si tiene nombre y teléfono
                if (name.isNotBlank() && phone.isNotBlank()) {
                    // Obtener email del contacto si existe
                    val email = getContactEmail(context, contactId)
                    
                    contacts.add(
                        CustomerData(
                            name = name,
                            phone = phone,
                            email = email
                        )
                    )
                }
                
                processedCount++
                // Actualizar progreso cada 10 contactos para no saturar el hilo UI
                if (processedCount % 10 == 0 || processedCount == totalContacts) {
                    val progress = processedCount.toFloat() / totalContacts.toFloat()
                    onProgress(progress, totalContacts)
                }
            }
        }
        
        // Eliminar duplicados basándose en el nombre
        contacts.distinctBy { it.name }
    }
    
    /**
     * Obtener el email de un contacto específico
     * 
     * @param context Contexto de la aplicación
     * @param contactId ID del contacto
     * @return Email del contacto o null si no existe
     */
    private fun getContactEmail(context: Context, contactId: String): String? {
        val emailCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )
        
        emailCursor?.use {
            if (it.moveToFirst()) {
                val emailIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                return it.getString(emailIndex)
            }
        }
        
        return null
    }
    
    /**
     * Obtener contactos con filtro de búsqueda
     * 
     * @param context Contexto de la aplicación
     * @param query Término de búsqueda
     * @return Lista de contactos filtrados
     */
    fun searchContacts(context: Context, query: String): List<CustomerData> {
        if (query.isBlank()) return getDeviceContacts(context)
        
        val contacts = mutableListOf<CustomerData>()
        
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?",
            arrayOf("%$query%", "%$query%"),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            
            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: ""
                val phone = it.getString(phoneIndex) ?: ""
                val contactId = it.getString(contactIdIndex) ?: ""
                
                if (name.isNotBlank() && phone.isNotBlank()) {
                    val email = getContactEmail(context, contactId)
                    
                    contacts.add(
                        CustomerData(
                            name = name,
                            phone = phone,
                            email = email
                        )
                    )
                }
            }
        }
        
        return contacts.distinctBy { it.name }
    }
    
    /**
     * Verificar si hay contactos disponibles en el dispositivo
     * 
     * @param context Contexto de la aplicación
     * @return true si hay contactos disponibles
     */
    fun hasContacts(context: Context): Boolean {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
            null,
            null,
            null
        )
        
        val hasContacts = cursor?.count ?: 0 > 0
        cursor?.close()
        
        return hasContacts
    }
}
