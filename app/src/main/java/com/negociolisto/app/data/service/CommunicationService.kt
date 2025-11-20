package com.negociolisto.app.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📱 SERVICIO DE COMUNICACIÓN
 * 
 * Maneja las acciones de comunicación con clientes:
 * - Abrir WhatsApp
 * - Hacer llamadas telefónicas
 * - Enviar emails
 */
@Singleton
class CommunicationService @Inject constructor() {
    
    /**
     * 💬 ABRIR WHATSAPP
     * 
     * Abre WhatsApp con el número del cliente para enviar un mensaje.
     * Formato: https://wa.me/[número]?text=[mensaje]
     */
    fun openWhatsApp(context: Context, phoneNumber: String, message: String = "") {
        try {
            // Limpiar el número de teléfono (remover espacios, guiones, etc.)
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            
            // Construir URL de WhatsApp
            val whatsappUrl = if (message.isNotEmpty()) {
                "https://wa.me/$cleanNumber?text=${Uri.encode(message)}"
            } else {
                "https://wa.me/$cleanNumber"
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(whatsappUrl)
                setPackage("com.whatsapp") // Intentar abrir la app de WhatsApp
            }
            
            // Si WhatsApp no está instalado, abrir en el navegador
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback: abrir en el navegador
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            // En caso de error, intentar abrir en el navegador
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneNumber"))
                context.startActivity(webIntent)
            } catch (webError: Exception) {
                // Error final - no se puede abrir WhatsApp
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 📞 HACER LLAMADA
     * 
     * Abre la app de llamadas con el número del cliente.
     */
    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 📧 ENVIAR EMAIL
     * 
     * Abre la app de email con el email del cliente.
     * Usa ACTION_SEND para mejor compatibilidad y selector de apps.
     */
    fun sendEmail(context: Context, email: String, subject: String = "", body: String = "") {
        try {
            // Intent principal con ACTION_SEND para mejor compatibilidad
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822" // Tipo MIME para email
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                if (subject.isNotEmpty()) {
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                }
                if (body.isNotEmpty()) {
                    putExtra(Intent.EXTRA_TEXT, body)
                }
            }
            
            // Verificar si hay apps de email disponibles
            if (intent.resolveActivity(context.packageManager) != null) {
                // Crear un chooser para que el usuario seleccione la app de email
                val chooser = Intent.createChooser(intent, "Enviar email con...")
                context.startActivity(chooser)
            } else {
                // Fallback: intentar con ACTION_SENDTO
                val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                    if (subject.isNotEmpty()) {
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                    }
                    if (body.isNotEmpty()) {
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                }
                
                if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(fallbackIntent)
                } else {
                    // Último recurso: abrir el navegador con un enlace mailto
                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("mailto:$email?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
                    }
                    context.startActivity(webIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // En caso de error, intentar abrir el navegador
            try {
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("mailto:$email?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
                }
                context.startActivity(webIntent)
            } catch (webError: Exception) {
                webError.printStackTrace()
            }
        }
    }
    
    /**
     * 📱 ABRIR WHATSAPP BUSINESS
     * 
     * Intenta abrir WhatsApp Business si está disponible.
     */
    fun openWhatsAppBusiness(context: Context, phoneNumber: String, message: String = "") {
        try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val whatsappUrl = if (message.isNotEmpty()) {
                "https://wa.me/$cleanNumber?text=${Uri.encode(message)}"
            } else {
                "https://wa.me/$cleanNumber"
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(whatsappUrl)
                setPackage("com.whatsapp.w4b") // WhatsApp Business
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback a WhatsApp normal
                openWhatsApp(context, phoneNumber, message)
            }
        } catch (e: Exception) {
            openWhatsApp(context, phoneNumber, message)
        }
    }
}
