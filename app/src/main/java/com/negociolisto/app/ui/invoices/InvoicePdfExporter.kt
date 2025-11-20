package com.negociolisto.app.ui.invoices

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.negociolisto.app.domain.model.Invoice
import com.negociolisto.app.ui.components.Formatters
import java.io.File
import java.io.FileOutputStream

object InvoicePdfExporter {
    
    // Constantes para márgenes y dimensiones
    private const val PAGE_WIDTH = 595f
    private const val PAGE_HEIGHT = 842f
    private const val LEFT_MARGIN = 50f
    private const val RIGHT_MARGIN = 545f
    private const val TOP_MARGIN = 50f
    private const val BOTTOM_MARGIN = 50f
    private const val CONTENT_WIDTH = RIGHT_MARGIN - LEFT_MARGIN
    
    /**
     * Función auxiliar para manejar textos largos con wrap automático
     */
    private fun drawTextWithWrap(
        canvas: android.graphics.Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: android.graphics.Paint,
        lineHeight: Float = 15f
    ): Float {
        val words = text.split(" ")
        var currentLine = ""
        var currentY = y
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)
            
            if (testWidth <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, x, currentY, paint)
                    currentY += lineHeight
                }
                currentLine = word
            }
        }
        
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, currentY, paint)
            currentY += lineHeight
        }
        
        return currentY
    }
    
    fun generate(
        context: Context,
        invoice: Invoice,
        settings: InvoiceSettings,
        template: com.negociolisto.app.domain.model.InvoiceTemplateType,
        customerName: String? = null
    ): Uri? {
        return try {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 72dpi approx
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas
            
            // Aplicar el template seleccionado
            when (template) {
                com.negociolisto.app.domain.model.InvoiceTemplateType.CLASSIC -> 
                    generateClassicTemplate(canvas, invoice, settings, customerName)
                com.negociolisto.app.domain.model.InvoiceTemplateType.MODERN -> 
                    generateModernTemplate(canvas, invoice, settings, customerName)
                com.negociolisto.app.domain.model.InvoiceTemplateType.MINIMAL -> 
                    generateMinimalTemplate(canvas, invoice, settings, customerName)
            }

            doc.finishPage(page)

            val outDir = File(context.cacheDir, "invoices").apply { mkdirs() }
            val file = File(outDir, "${invoice.number}.pdf")
            if (file.exists()) file.delete()
            FileOutputStream(file).use { fos -> doc.writeTo(fos) }
            doc.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Throwable) {
            null
        }
    }
    
    private fun generateClassicTemplate(
        canvas: android.graphics.Canvas,
        invoice: Invoice,
        settings: InvoiceSettings,
        customerName: String?
    ) {
        val paint = android.graphics.Paint()
        var y = TOP_MARGIN
        
        // === DISEÑO MODERNO Y LIMPIO ===
        
        // Header minimalista con línea superior
        paint.color = android.graphics.Color.parseColor("#009FE3")
        paint.strokeWidth = 4f
        canvas.drawLine(LEFT_MARGIN, TOP_MARGIN - 10f, RIGHT_MARGIN, TOP_MARGIN - 10f, paint)
        
        // Nombre de empresa - tipografía grande y limpia
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#1A1A1A")
        canvas.drawText(settings.companyName, LEFT_MARGIN, TOP_MARGIN + 20f, paint)
        
        // Información de contacto - tipografía pequeña y consistente
        paint.isFakeBoldText = false
        paint.textSize = 10f
        paint.color = android.graphics.Color.parseColor("#666666")
        y = TOP_MARGIN + 35
        
        // Dirección
        y = drawTextWithWrap(canvas, settings.companyAddress, LEFT_MARGIN, y.toFloat(), CONTENT_WIDTH, paint, 12f)
        
        // Información adicional sin iconos (más limpio)
        settings.companyRut?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("RUT: $it", LEFT_MARGIN, y.toFloat(), paint)
            y += 12
        }
        settings.companyPhone?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("Tel: $it", LEFT_MARGIN, y.toFloat(), paint)
            y += 12
        }
        settings.companyEmail?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText(it, LEFT_MARGIN, y.toFloat(), paint)
            y += 12
        }
        
        y += 30
        
        // === INFORMACIÓN DE FACTURA - DISEÑO LIMPIO ===
        
        // Título de factura - grande y prominente
        paint.textSize = 20f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#1A1A1A")
        canvas.drawText("FACTURA ${invoice.number}", LEFT_MARGIN, y.toFloat(), paint)
        paint.isFakeBoldText = false
        y += 25
        
        // Información de factura - tipografía consistente
        paint.textSize = 11f
        paint.color = android.graphics.Color.parseColor("#333333")
        canvas.drawText("Fecha: ${Formatters.formatDate(invoice.date)}", LEFT_MARGIN, y.toFloat(), paint)
        y += 15
        val customerLabel = customerName ?: invoice.customerId ?: "Cliente General"
        canvas.drawText("Cliente: $customerLabel", LEFT_MARGIN, y.toFloat(), paint)
        y += 30
        
        // === TABLA DE PRODUCTOS - DISEÑO MINIMALISTA ===
        
        // Línea separadora sutil
        paint.color = android.graphics.Color.parseColor("#E0E0E0")
        paint.strokeWidth = 1f
        canvas.drawLine(LEFT_MARGIN, y.toFloat(), RIGHT_MARGIN, y.toFloat(), paint)
        y += 20
        
        // Header de tabla - tipografía pequeña y consistente
        paint.color = android.graphics.Color.parseColor("#666666")
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("DESCRIPCIÓN", LEFT_MARGIN, y.toFloat(), paint)
        canvas.drawText("CANT.", 350f, y.toFloat(), paint)
        canvas.drawText("PRECIO UNIT.", 400f, y.toFloat(), paint)
        canvas.drawText("TOTAL", 500f, y.toFloat(), paint)
        paint.isFakeBoldText = false
        y += 20
        
        // Items - diseño limpio sin fondos alternados
        paint.color = android.graphics.Color.parseColor("#1A1A1A")
        paint.textSize = 11f
        invoice.items.forEach { item ->
            canvas.drawText(item.description, LEFT_MARGIN, y.toFloat(), paint)
            canvas.drawText(item.quantity.toString(), 350f, y.toFloat(), paint)
            canvas.drawText(Formatters.formatClp(item.unitPrice), 400f, y.toFloat(), paint)
            canvas.drawText(Formatters.formatClp(item.total), 500f, y.toFloat(), paint)
            y += 18
        }
        y += 20
        
        // === TOTALES - DISEÑO PROFESIONAL ===
        
        // Línea separadora para totales
        paint.color = android.graphics.Color.parseColor("#E0E0E0")
        paint.strokeWidth = 1f
        canvas.drawLine(LEFT_MARGIN, y.toFloat(), RIGHT_MARGIN, y.toFloat(), paint)
        y += 20
        
        // Totales - tipografía consistente, alineados a la derecha
        paint.color = android.graphics.Color.parseColor("#333333")
        paint.textSize = 11f
        
        // Subtotal
        val subtotalText = "Subtotal: ${Formatters.formatClp(invoice.subtotal)}"
        val subtotalWidth = paint.measureText(subtotalText)
        canvas.drawText(subtotalText, RIGHT_MARGIN - subtotalWidth, y.toFloat(), paint)
        y += 15
        
        // IVA
        val taxText = "IVA (19%): ${Formatters.formatClp(invoice.tax)}"
        val taxWidth = paint.measureText(taxText)
        canvas.drawText(taxText, RIGHT_MARGIN - taxWidth, y.toFloat(), paint)
        y += 20
        
        // Total - destacado pero no exagerado
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#1A1A1A")
        val totalText = "Total: ${Formatters.formatClp(invoice.total)}"
        val totalWidth = paint.measureText(totalText)
        canvas.drawText(totalText, RIGHT_MARGIN - totalWidth, y.toFloat(), paint)
        paint.isFakeBoldText = false
        
        // === NOTAS - SI EXISTEN ===
        invoice.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            y += 40
            paint.color = android.graphics.Color.parseColor("#666666")
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("Notas:", LEFT_MARGIN, y.toFloat(), paint)
            paint.isFakeBoldText = false
            y += 15
            y = drawTextWithWrap(canvas, notes, LEFT_MARGIN, y.toFloat(), CONTENT_WIDTH, paint, 12f)
        }
        
        // === FOOTER MINIMALISTA ===
        y += 40
        paint.color = android.graphics.Color.parseColor("#999999")
        paint.textSize = 9f
        canvas.drawText("Gracias por su preferencia", LEFT_MARGIN, y.toFloat(), paint)
    }
    
    private fun generateModernTemplate(
        canvas: android.graphics.Canvas,
        invoice: Invoice,
        settings: InvoiceSettings,
        customerName: String?
    ) {
        val paint = android.graphics.Paint()
        var y = 50
        
        // Header moderno con gradiente simulado
        paint.color = android.graphics.Color.parseColor("#009FE3")
        canvas.drawRect(50f, 30f, 545f, 100f, paint)
        
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText(settings.companyName, 60f, 60f, paint)
        paint.isFakeBoldText = false
        paint.textSize = 12f
        canvas.drawText(settings.companyAddress, 60f, 80f, paint)
        var infoY = 110
        settings.companyRut?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("RUT: $it", 60f, infoY.toFloat(), paint); infoY += 16
        }
        settings.companyPhone?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("Tel: $it", 60f, infoY.toFloat(), paint); infoY += 16
        }
        settings.companyEmail?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText(it, 60f, infoY.toFloat(), paint)
        }
        
        y = 130
        paint.color = android.graphics.Color.BLACK
        
        // Información de factura moderna
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("FACTURA #${invoice.number}", 60f, y.toFloat(), paint)
        paint.isFakeBoldText = false
        y += 25
        
        paint.textSize = 12f
        canvas.drawText("Fecha: ${Formatters.formatDate(invoice.date)}", 60f, y.toFloat(), paint)
        y += 20
        val customerLabel = customerName ?: invoice.customerId ?: "N/A"
        canvas.drawText("Cliente: $customerLabel", 60f, y.toFloat(), paint)
        y += 30
        
        // Header de tabla moderna
        paint.color = android.graphics.Color.parseColor("#009FE3")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("DESCRIPCIÓN", 60f, y.toFloat(), paint)
        canvas.drawText("CANT.", 300f, y.toFloat(), paint)
        canvas.drawText("PRECIO", 350f, y.toFloat(), paint)
        canvas.drawText("TOTAL", 450f, y.toFloat(), paint)
        paint.isFakeBoldText = false
        y += 20
        
        // Línea bajo header
        paint.color = android.graphics.Color.parseColor("#009FE3")
        canvas.drawLine(60f, y.toFloat(), 545f, y.toFloat(), paint)
        y += 15
        
        // Items con fondo alternado mejorado
        paint.color = android.graphics.Color.parseColor("#F8F9FA")
        var itemIndex = 0
        invoice.items.forEach { item ->
            if (itemIndex % 2 == 1) {
                canvas.drawRect(60f, (y - 8).toFloat(), 545f, (y + 12).toFloat(), paint)
            }
            
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 11f
            canvas.drawText(item.description, 60f, y.toFloat(), paint)
            canvas.drawText(item.quantity.toString(), 300f, y.toFloat(), paint)
            canvas.drawText(Formatters.formatClp(item.unitPrice), 350f, y.toFloat(), paint)
            canvas.drawText(Formatters.formatClp(item.total), 450f, y.toFloat(), paint)
            y += 20
            itemIndex++
        }
        
        y += 15
        // Total destacado con gradiente simulado
        paint.color = android.graphics.Color.parseColor("#312783")
        canvas.drawRect(60f, (y - 8).toFloat(), 545f, (y + 25).toFloat(), paint)
        
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL: ${Formatters.formatClp(invoice.total)}", 70f, (y + 12).toFloat(), paint)
        paint.isFakeBoldText = false
        
        // Desglose de totales
        y += 40
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        canvas.drawText("Subtotal: ${Formatters.formatClp(invoice.subtotal)}", 400f, y.toFloat(), paint)
        y += 15
        canvas.drawText("IVA (19%): ${Formatters.formatClp(invoice.tax)}", 400f, y.toFloat(), paint)
        
        // Notas si existen
        invoice.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            y += 30
            paint.color = android.graphics.Color.parseColor("#666666")
            paint.textSize = 10f
            canvas.drawText("Notas:", 60f, y.toFloat(), paint)
            y += 15
            canvas.drawText(notes, 60f, y.toFloat(), paint)
        }
    }
    
    private fun generateMinimalTemplate(
        canvas: android.graphics.Canvas,
        invoice: Invoice,
        settings: InvoiceSettings,
        customerName: String?
    ) {
        val paint = android.graphics.Paint()
        var y = 80
        
        // Header minimalista elegante
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#2C3E50")
        canvas.drawText(settings.companyName, 60f, y.toFloat(), paint)
        paint.isFakeBoldText = false
        y += 30
        
        paint.textSize = 11f
        paint.color = android.graphics.Color.parseColor("#7F8C8D")
        canvas.drawText(settings.companyAddress, 60f, y.toFloat(), paint)
        settings.companyRut?.takeIf { it.isNotBlank() }?.let {
            y += 16; canvas.drawText("RUT: $it", 60f, y.toFloat(), paint)
        }
        settings.companyPhone?.takeIf { it.isNotBlank() }?.let {
            y += 16; canvas.drawText("Tel: $it", 60f, y.toFloat(), paint)
        }
        settings.companyEmail?.takeIf { it.isNotBlank() }?.let {
            y += 16; canvas.drawText(it, 60f, y.toFloat(), paint)
        }
        y += 50
        
        // Línea sutil elegante
        paint.color = android.graphics.Color.parseColor("#BDC3C7")
        paint.strokeWidth = 1f
        canvas.drawLine(60f, y.toFloat(), 535f, y.toFloat(), paint)
        y += 25
        
        // Información de factura minimalista
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 14f
        val customerLabel = customerName ?: invoice.customerId ?: "N/A"
        canvas.drawText("Cliente: $customerLabel", 60f, y.toFloat(), paint)
        canvas.drawText("Factura ${invoice.number}", 400f, y.toFloat(), paint)
        y += 22
        canvas.drawText("Fecha: ${Formatters.formatDate(invoice.date)}", 60f, y.toFloat(), paint)
        y += 40
        
        // Items con formato minimalista mejorado
        paint.textSize = 12f
        invoice.items.forEach { item ->
            // Descripción del producto alineada a la izquierda
            canvas.drawText(item.description, 60f, y.toFloat(), paint)
            
            // Cantidad y precio unitario alineados a la derecha
            val quantityPriceText = "${item.quantity} × ${Formatters.formatClp(item.unitPrice)}"
            val quantityPriceWidth = paint.measureText(quantityPriceText)
            canvas.drawText(quantityPriceText, 535f - quantityPriceWidth, y.toFloat(), paint)
            
            // Total del item en la línea siguiente, alineado a la derecha
            y += 18
            val itemTotalText = Formatters.formatClp(item.total)
            val itemTotalWidth = paint.measureText(itemTotalText)
            canvas.drawText(itemTotalText, 535f - itemTotalWidth, y.toFloat(), paint)
            
            y += 20
        }
        
        y += 20
        // Línea final elegante
        paint.color = android.graphics.Color.parseColor("#2C3E50")
        paint.strokeWidth = 2f
        canvas.drawLine(60f, y.toFloat(), 535f, y.toFloat(), paint)
        y += 25
        
        // Total minimalista pero destacado, alineado a la izquierda
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#2C3E50")
        canvas.drawText("Total: ${Formatters.formatClp(invoice.total)}", 60f, y.toFloat(), paint)
        paint.isFakeBoldText = false
        
        // Desglose sutil alineado a la derecha
        y += 30
        paint.textSize = 10f
        paint.color = android.graphics.Color.parseColor("#7F8C8D")
        
        val subtotalDetailText = "Subtotal: ${Formatters.formatClp(invoice.subtotal)}"
        val subtotalDetailWidth = paint.measureText(subtotalDetailText)
        canvas.drawText(subtotalDetailText, 535f - subtotalDetailWidth, y.toFloat(), paint)
        
        y += 12
        val taxDetailText = "IVA (19%): ${Formatters.formatClp(invoice.tax)}"
        val taxDetailWidth = paint.measureText(taxDetailText)
        canvas.drawText(taxDetailText, 535f - taxDetailWidth, y.toFloat(), paint)
        
        // Notas si existen
        invoice.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            y += 40
            paint.color = android.graphics.Color.parseColor("#7F8C8D")
            paint.textSize = 10f
            canvas.drawText("Notas:", 60f, y.toFloat(), paint)
            y += 15
            canvas.drawText(notes, 60f, y.toFloat(), paint)
        }
    }
}


