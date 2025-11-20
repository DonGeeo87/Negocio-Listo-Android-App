package com.negociolisto.app.data.service

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.negociolisto.app.domain.model.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 📱 SERVICIO DE IMPORTACIÓN DE CONTACTOS
 * 
 * Este servicio maneja la importación de contactos desde la agenda del teléfono
 * con la posibilidad de seleccionar solo los contactos deseados.
 */
@Singleton
class ContactImportService @Inject constructor(
    private val context: Context
) {
    
    private val contentResolver: ContentResolver = context.contentResolver
    
    /**
     * 📋 OBTENER TODOS LOS CONTACTOS
     * 
     * Obtiene todos los contactos de la agenda del teléfono
     * @deprecated Usar getContactsPaginated para mejor rendimiento
     */
    suspend fun getAllContacts(): Result<List<ContactInfo>> = withContext(Dispatchers.IO) {
        try {
            val contacts = mutableListOf<ContactInfo>()
            
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
                ),
                null,
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
            )
            
            cursor?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                    val hasPhone = c.getInt(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
                    
                    if (name != null && name.isNotBlank()) {
                        val contactInfo = ContactInfo(
                            id = id,
                            name = name,
                            phone = if (hasPhone) getPhoneNumber(id) else null,
                            email = getEmail(id)
                        )
                        contacts.add(contactInfo)
                    }
                }
            }
            
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 📋 OBTENER CONTACTOS CON PAGINACIÓN Y FILTROS
     * 
     * Obtiene contactos de forma paginada con filtros de búsqueda para mejor rendimiento
     */
    suspend fun getContactsPaginated(
        page: Int = 0,
        pageSize: Int = 10,
        searchQuery: String? = null
    ): Result<ContactPaginationResult> = withContext(Dispatchers.IO) {
        try {
            // Construir la query con filtros
            val selection = if (!searchQuery.isNullOrBlank()) {
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
            } else null
            
            val selectionArgs = if (!searchQuery.isNullOrBlank()) {
                arrayOf("%$searchQuery%")
            } else null

            // Primero obtener el total de contactos que coinciden con el filtro
            // Usamos una consulta simple para contar manualmente
            var totalCount = 0
            val countCursor: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts._ID),
                selection,
                selectionArgs,
                null
            )
            
            countCursor?.use { cursor ->
                totalCount = cursor.count
            }
            
            // Obtener los contactos de la página actual
            val contacts = mutableListOf<ContactInfo>()
            val offset = page * pageSize
            
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
                ),
                selection,
                selectionArgs,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC LIMIT $pageSize OFFSET $offset"
            )
            
            cursor?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                    val hasPhone = c.getInt(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
                    
                    if (name != null && name.isNotBlank()) {
                        val contactInfo = ContactInfo(
                            id = id,
                            name = name,
                            phone = if (hasPhone) getPhoneNumber(id) else null,
                            email = getEmail(id)
                        )
                        contacts.add(contactInfo)
                    }
                }
            }
            
            val totalPages = if (totalCount > 0) ((totalCount - 1) / pageSize) + 1 else 0
            
            Result.success(
                ContactPaginationResult(
                    contacts = contacts,
                    currentPage = page,
                    pageSize = pageSize,
                    totalCount = totalCount,
                    totalPages = totalPages,
                    hasNextPage = page < totalPages - 1,
                    hasPreviousPage = page > 0
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📞 OBTENER NÚMERO DE TELÉFONO
     */
    private fun getPhoneNumber(contactId: Long): String? {
        val phoneCursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )
        
        phoneCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }
        return null
    }
    
    /**
     * 📧 OBTENER EMAIL
     */
    private fun getEmail(contactId: Long): String? {
        val emailCursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )
        
        emailCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
            }
        }
        return null
    }
    
    /**
     * 🔄 CONVERTIR CONTACTOS A CLIENTES
     * 
     * Convierte una lista de contactos seleccionados en objetos Customer
     */
    fun convertContactsToCustomers(selectedContacts: List<ContactInfo>): List<Customer> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return selectedContacts.map { contact ->
            Customer(
                id = java.util.UUID.randomUUID().toString(),
                name = contact.name,
                phone = contact.phone,
                email = contact.email,
                address = null,
                companyName = null,
                totalPurchases = 0.0,
                lastPurchaseDate = null,
                notes = "Importado desde agenda del teléfono",
                createdAt = now
            )
        }
    }
}

/**
 * 📋 INFORMACIÓN DE CONTACTO
 */
data class ContactInfo(
    val id: Long,
    val name: String,
    val phone: String?,
    val email: String?
)

/**
 * 📄 RESULTADO DE PAGINACIÓN DE CONTACTOS
 */
data class ContactPaginationResult(
    val contacts: List<ContactInfo>,
    val currentPage: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean
)
