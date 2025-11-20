package com.negociolisto.app.data.service

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.io.File
import kotlin.text.Charsets
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import com.negociolisto.app.domain.repository.InventoryRepository
import com.negociolisto.app.domain.repository.CustomerRepository
import com.negociolisto.app.domain.repository.SalesRepository
import com.negociolisto.app.domain.repository.ExpenseRepository
import com.negociolisto.app.domain.repository.CollectionRepository
import com.negociolisto.app.domain.repository.InvoiceRepository
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.CustomCategoryRepository

/**
 * @deprecated Usar DataExportService.kt en su lugar
 * Este archivo se mantiene solo para compatibilidad temporal
 * 
 * 📊 SERVICIO DE EXPORTACIÓN DE DATOS
 * 
 * Maneja la exportación de datos en diferentes formatos (CSV, PDF, Excel)
 */
@Singleton
class ExportService @Inject constructor(
    private val context: Context,
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val salesRepository: SalesRepository,
    private val expenseRepository: ExpenseRepository,
    private val collectionRepository: CollectionRepository,
    private val invoiceRepository: InvoiceRepository,
    private val authRepository: AuthRepository,
    private val customCategoryRepository: CustomCategoryRepository
) {
    
    /**
     * 📄 EXPORTAR DATOS A CSV
     */
    suspend fun exportToCSV(
        dataType: String,
        data: List<Map<String, Any>>,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando exportación CSV...")
            
            if (data.isEmpty()) {
                return@withContext Result.failure(Exception("No hay datos para exportar"))
            }
            
            onProgress(20, "Generando archivo CSV...")
            
            // Crear archivo CSV con encabezados estables
            val fileName = "${dataType}_export_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                // Determinar encabezados estables (orden alfabético) y localizados
                val headersList = data.first().keys.sorted()
                val headers = headersList.joinToString(",") { localizeHeader(it) }
                writer.write("$headers\n")
                
                // Escribir datos en orden por headersList
                data.forEach { row ->
                    val values = headersList.map { key ->
                        val value = row[key] ?: ""
                        val stringValue = value.toString()
                        // Escapar comillas y envolver en comillas si contiene comas o comillas
                        if (stringValue.contains(',') || stringValue.contains('"')) {
                            "\"${stringValue.replace("\"", "\"\"")}\""
                        } else {
                            stringValue
                        }
                    }.joinToString(",")
                    writer.write("$values\n")
                }
            }
            
            onProgress(100, "Archivo CSV generado exitosamente")
            
            // Crear URI usando FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📋 EXPORTAR DATOS A PDF (REAL con PdfDocument)
     */
    suspend fun exportToPDF(
        dataType: String,
        data: List<Map<String, Any>>,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando exportación PDF...")
            
            if (data.isEmpty()) {
                return@withContext Result.failure(Exception("No hay datos para exportar"))
            }
            
            onProgress(20, "Generando archivo PDF...")
            
            val fileName = "${dataType}_export_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val doc = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 aprox
            var page = doc.startPage(pageInfo)
            var canvas = page.canvas
            val paint = android.graphics.Paint()

            fun newPage() {
                doc.finishPage(page)
                page = doc.startPage(pageInfo)
                canvas = page.canvas
            }

            var y = 40
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("Reporte: ${dataType.uppercase()}", 40f, y.toFloat(), paint)
            paint.isFakeBoldText = false
            paint.textSize = 12f
            y += 18
            canvas.drawText(
                "Fecha: " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                40f,
                y.toFloat(),
                paint
            )
            y += 18
            canvas.drawText("Total de registros: ${data.size}", 40f, y.toFloat(), paint)
            y += 12
            canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint)
            y += 18

            if (data.isNotEmpty()) {
                val headers = data.first().keys.sorted()
                paint.isFakeBoldText = true
                var x = 40f
                val colWidth = 515f / headers.size.coerceAtLeast(1)
                headers.forEach { h ->
                    canvas.drawText(localizeHeader(h), x, y.toFloat(), paint)
                    x += colWidth
                }
                paint.isFakeBoldText = false
                y += 16

                data.forEach { row ->
                    var cx = 40f
                    headers.forEach { h ->
                        val value = row[h]?.toString() ?: ""
                        canvas.drawText(value.take(28), cx, y.toFloat(), paint)
                        cx += colWidth
                    }
                    y += 16
                    if (y > 800) {
                        newPage()
                        y = 40
                    }
                }
            }

            doc.finishPage(page)
            doc.writeTo(java.io.FileOutputStream(file))
            doc.close()
            
            onProgress(100, "Archivo PDF generado exitosamente")
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📄 Exportar CSV a una Uri (SAF)
     */
    suspend fun exportToCSVUri(
        dataType: String,
        data: List<Map<String, Any>>,
        target: Uri,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando CSV...")
            if (data.isEmpty()) return@withContext Result.failure(Exception("No hay datos para exportar"))
            val headersList = data.first().keys.sorted()
            onProgress(20, "Escribiendo CSV...")
            context.contentResolver.openOutputStream(target)?.use { os ->
                os.writer().use { writer ->
                    val headers = headersList.joinToString(",") { localizeHeader(it) }
                    writer.write("$headers\n")
                    data.forEach { row ->
                        val values = headersList.map { key ->
                            val v = row[key]?.toString() ?: ""
                            if (v.contains(',') || v.contains('"')) "\"${v.replace("\"", "\"\"")}\"" else v
                        }.joinToString(",")
                        writer.write("$values\n")
                    }
                }
            } ?: return@withContext Result.failure(IOException("No se pudo abrir el destino"))
            onProgress(100, "CSV exportado")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 📊 Exportar TSV (Excel simple) a una Uri (SAF)
     */
    suspend fun exportToExcelUri(
        dataType: String,
        data: List<Map<String, Any>>,
        target: Uri,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando Excel...")
            if (data.isEmpty()) return@withContext Result.failure(Exception("No hay datos para exportar"))
            val headersList = data.first().keys.sorted()
            onProgress(20, "Escribiendo Excel...")
            context.contentResolver.openOutputStream(target)?.use { os ->
                os.writer().use { writer ->
                    val headers = headersList.joinToString("\t") { localizeHeader(it) }
                    writer.write("$headers\n")
                    data.forEach { row ->
                        val values = headersList.joinToString("\t") { key -> (row[key] ?: "").toString() }
                        writer.write("$values\n")
                    }
                }
            } ?: return@withContext Result.failure(IOException("No se pudo abrir el destino"))
            onProgress(100, "Excel exportado")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 📋 Exportar PDF a una Uri (SAF)
     */
    suspend fun exportToPDFUri(
        dataType: String,
        data: List<Map<String, Any>>,
        target: Uri,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando PDF...")
            if (data.isEmpty()) return@withContext Result.failure(Exception("No hay datos para exportar"))
            onProgress(20, "Escribiendo PDF...")

            val doc = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = doc.startPage(pageInfo)
            var canvas = page.canvas
            val paint = android.graphics.Paint()

            fun newPage() {
                doc.finishPage(page)
                page = doc.startPage(pageInfo)
                canvas = page.canvas
            }

            var y = 40
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("Reporte: ${dataType.uppercase()}", 40f, y.toFloat(), paint)
            paint.isFakeBoldText = false
            paint.textSize = 12f
            y += 18
            canvas.drawText(
                "Fecha: " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                40f,
                y.toFloat(),
                paint
            )
            y += 18
            canvas.drawText("Total de registros: ${data.size}", 40f, y.toFloat(), paint)
            y += 12
            canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint)
            y += 18

            val headers = data.first().keys.sorted()
            paint.isFakeBoldText = true
            var x = 40f
            val colWidth = 515f / headers.size.coerceAtLeast(1)
            headers.forEach { h ->
                canvas.drawText(localizeHeader(h), x, y.toFloat(), paint)
                x += colWidth
            }
            paint.isFakeBoldText = false
            y += 16

            data.forEach { row ->
                var cx = 40f
                headers.forEach { h ->
                    val value = row[h]?.toString() ?: ""
                    canvas.drawText(value.take(28), cx, y.toFloat(), paint)
                    cx += colWidth
                }
                y += 16
                if (y > 800) {
                    newPage()
                    y = 40
                }
            }

            doc.finishPage(page)
            context.contentResolver.openOutputStream(target)?.use { os ->
                doc.writeTo(os)
            } ?: return@withContext Result.failure(IOException("No se pudo abrir el destino"))
            doc.close()

            onProgress(100, "PDF exportado")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 📂 Exportar múltiples tipos a una carpeta (Uri de árbol de SAF)
     */
    suspend fun exportMultipleToTreeUri(
        dataTypes: List<String>,
        treeUri: Uri,
        format: String, // "CSV" | "PDF" | "Excel"
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (dataTypes.isEmpty()) return@withContext Result.failure(Exception("No hay tipos seleccionados"))
            onProgress(0, "Preparando exportación...")

            val docTree = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                ?: return@withContext Result.failure(IOException("Carpeta inválida"))

            val dataResult = getExportData(dataTypes) { progress, status -> onProgress(progress / 2, status) }
            dataResult.onFailure { return@withContext Result.failure(it) }
            val dataMap = dataResult.getOrNull() ?: emptyMap()

            var index = 0
            dataTypes.forEach { type ->
                val data = dataMap[type] ?: emptyList()
                val timestamp = System.currentTimeMillis()
                when (format) {
                    "CSV" -> {
                        val fileName = "${type.lowercase()}_${timestamp}.csv"
                        val file = docTree.createFile("text/csv", fileName)
                            ?: return@withContext Result.failure(IOException("No se pudo crear $fileName"))
                        context.contentResolver.openOutputStream(file.uri)?.use { os ->
                            val headersList = data.firstOrNull()?.keys?.sorted().orEmpty()
                            os.writer().use { writer ->
                                val headers = headersList.joinToString(",") { localizeHeader(it) }
                                writer.write("$headers\n")
                                data.forEach { row ->
                                    val values = headersList.map { key ->
                                        val v = row[key]?.toString() ?: ""
                                        if (v.contains(',') || v.contains('"')) "\"${v.replace("\"", "\"\"")}\"" else v
                                    }.joinToString(",")
                                    writer.write("$values\n")
                                }
                            }
                        } ?: return@withContext Result.failure(IOException("No se pudo escribir $fileName"))
                    }
                    "Excel" -> {
                        val fileName = "${type.lowercase()}_${timestamp}.xlsx"
                        val file = docTree.createFile("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fileName)
                            ?: return@withContext Result.failure(IOException("No se pudo crear $fileName"))
                        context.contentResolver.openOutputStream(file.uri)?.use { os ->
                            val headersList = data.firstOrNull()?.keys?.sorted().orEmpty()
                            os.writer().use { writer ->
                                val headers = headersList.joinToString("\t") { localizeHeader(it) }
                                writer.write("$headers\n")
                                data.forEach { row ->
                                    val values = headersList.joinToString("\t") { key -> (row[key] ?: "").toString() }
                                    writer.write("$values\n")
                                }
                            }
                        } ?: return@withContext Result.failure(IOException("No se pudo escribir $fileName"))
                    }
                    "PDF" -> {
                        val fileName = "${type.lowercase()}_${timestamp}.pdf"
                        val file = docTree.createFile("application/pdf", fileName)
                            ?: return@withContext Result.failure(IOException("No se pudo crear $fileName"))
                        context.contentResolver.openOutputStream(file.uri)?.use { os ->
                            val doc = android.graphics.pdf.PdfDocument()
                            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                            var page = doc.startPage(pageInfo)
                            var canvas = page.canvas
                            val paint = android.graphics.Paint()
                            fun newPage() { doc.finishPage(page); page = doc.startPage(pageInfo); canvas = page.canvas }
                            var y = 40
                            paint.textSize = 16f
                            paint.isFakeBoldText = true
                            canvas.drawText("Reporte: ${type.uppercase()}", 40f, y.toFloat(), paint)
                            paint.isFakeBoldText = false
                            paint.textSize = 12f
                            y += 18
                            canvas.drawText("Fecha: " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()), 40f, y.toFloat(), paint)
                            y += 18
                            canvas.drawText("Total de registros: ${data.size}", 40f, y.toFloat(), paint)
                            y += 12
                            canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint)
                            y += 18
                            val headers = data.firstOrNull()?.keys?.sorted().orEmpty()
                            paint.isFakeBoldText = true
                            var x = 40f
                            val colWidth = 515f / headers.size.coerceAtLeast(1)
                            headers.forEach { h -> canvas.drawText(localizeHeader(h), x, y.toFloat(), paint); x += colWidth }
                            paint.isFakeBoldText = false
                            y += 16
                            data.forEach { row ->
                                var cx = 40f
                                headers.forEach { h ->
                                    val v = row[h]?.toString() ?: ""
                                    canvas.drawText(v.take(28), cx, y.toFloat(), paint)
                                    cx += colWidth
                                }
                                y += 16
                                if (y > 800) { newPage(); y = 40 }
                            }
                            doc.finishPage(page)
                            doc.writeTo(os)
                            doc.close()
                        } ?: return@withContext Result.failure(IOException("No se pudo escribir $fileName"))
                    }
                }
                index++
                val progress = 50 + ((index * 50) / dataTypes.size)
                onProgress(progress, "Exportado $type ($index/${dataTypes.size})")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📊 EXPORTAR DATOS A EXCEL (TSV simple compatible)
     */
    suspend fun exportToExcel(
        dataType: String,
        data: List<Map<String, Any>>,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando exportación Excel compatible...")
            
            if (data.isEmpty()) {
                return@withContext Result.failure(Exception("No hay datos para exportar"))
            }
            
            onProgress(20, "Generando archivo Excel compatible...")
            
            // Crear archivo CSV con extensión .xlsx (Excel lo abre perfectamente)
            val fileName = "${dataType}_export_${System.currentTimeMillis()}.xlsx"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            onProgress(40, "Aplicando formato profesional...")
            
            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                val headersList = data.first().keys.sorted()
                
                // Escribir encabezado con formato especial
                writer.write("\uFEFF") // BOM para UTF-8 (Excel lo reconoce mejor)
                writer.write("sep=,\n") // Separador CSV explícito
                val headers = headersList.joinToString(",") { "\"${localizeHeader(it)}\"" }
                writer.write("$headers\n")
                
                onProgress(60, "Escribiendo datos con formato profesional...")
                
                // Escribir datos con formato
                data.forEachIndexed { rowIndex, row ->
                    val values = headersList.joinToString(",") { key -> 
                        val value = (row[key] ?: "").toString()
                        // Escapar comillas y comas si es necesario
                        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                            "\"${value.replace("\"", "\"\"")}\""
                        } else {
                            "\"$value\""
                        }
                    }
                    writer.write("$values\n")
                    
                    // Actualizar progreso
                    if (rowIndex % 10 == 0) {
                        val progress = 60 + (rowIndex * 30 / data.size)
                        onProgress(progress, "Escribiendo fila ${rowIndex + 1} de ${data.size}...")
                    }
                }
            }
            
            onProgress(100, "Archivo Excel compatible generado exitosamente")
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📊 EXPORTAR DATOS AMIGABLES PARA EL USUARIO
     */
    suspend fun exportUserFriendlyExcel(
        dataTypes: List<String>,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando reporte amigable para el usuario...")
            
            onProgress(10, "Obteniendo datos organizados...")
            
            // Obtener todos los clientes para hacer el mapeo de IDs a nombres
            val customersMap = mutableMapOf<String, String>()
            try {
                val customers = customerRepository.getAllCustomers().first()
                customers.forEach { customer ->
                    customersMap[customer.id] = customer.name
                }
            } catch (e: Exception) {
                println("⚠️ No se pudieron cargar los nombres de clientes: ${e.message}")
            }
            
            // Crear archivo CSV amigable (Excel lo abre perfectamente)
            val timestamp = System.currentTimeMillis()
            val fileName = "Mi_Negocio_Reporte_${timestamp}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                // Encabezado principal
                writer.write("\uFEFF") // BOM para UTF-8
                writer.write("sep=,\n") // Separador CSV explícito
                
                writer.write("\"MI NEGOCIO - REPORTE COMPLETO\"\n")
                writer.write("\"Fecha: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\"\n")
                writer.write("\"Generado por: NegocioListo\"\n")
                writer.write("\n")
                
                onProgress(30, "Organizando datos por categoría...")
                
                // Procesar cada categoría
                dataTypes.forEachIndexed { index, dataType ->
                    val progress = 30 + (index * 60 / dataTypes.size)
                    onProgress(progress, "Procesando $dataType...")
                    
                    val data = when (dataType.lowercase()) {
                        "inventario" -> getInventoryData()
                        "clientes" -> getCustomersData()
                        "ventas" -> getSalesData()
                        "gastos" -> getExpensesData()
                        "categorias" -> getCategoriesData()
                        "colecciones" -> getCollectionsData()
                        "facturas" -> getInvoicesData()
                        else -> emptyList()
                    }
                    
                    if (data.isNotEmpty()) {
                        writeUserFriendlySection(writer, dataType, data, customersMap)
                    }
                }
            }
            
            onProgress(95, "Finalizando reporte amigable...")
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            onProgress(100, "Reporte CSV amigable generado exitosamente")
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📝 ESCRIBIR SECCIÓN AMIGABLE PARA EL USUARIO
     */
    private fun writeUserFriendlySection(
        writer: java.io.Writer,
        dataType: String,
        data: List<Map<String, Any>>,
        customersMap: Map<String, String>
    ) {
        val sectionTitle = getFriendlySectionTitle(dataType)
        writer.write("\"$sectionTitle\"\n")
        writer.write("\"Total: ${data.size} registros\"\n")
        writer.write("\n")
        
        // Headers amigables (sin IDs internos)
        val friendlyHeaders = getFriendlyHeaders(dataType)
        if (friendlyHeaders.isNotEmpty()) {
            val headersLine = friendlyHeaders.joinToString(",") { "\"$it\"" }
            writer.write("$headersLine\n")
            
            // Datos amigables
            data.forEach { row ->
                val friendlyData = getFriendlyData(dataType, row, customersMap)
                val dataLine = friendlyData.joinToString(",") { value ->
                    val cleanValue = cleanUserData(value)
                    if (cleanValue.contains(",") || cleanValue.contains("\"") || cleanValue.contains("\n")) {
                        "\"${cleanValue.replace("\"", "\"\"")}\""
                    } else {
                        "\"$cleanValue\""
                    }
                }
                writer.write("$dataLine\n")
            }
        }
        writer.write("\n")
    }
    
    /**
     * 🏷️ OBTENER TÍTULO AMIGABLE DE SECCIÓN
     */
    private fun getFriendlySectionTitle(dataType: String): String = when (dataType.lowercase()) {
        "inventario" -> "MIS PRODUCTOS"
        "clientes" -> "MIS CLIENTES"
        "ventas" -> "MIS VENTAS"
        "gastos" -> "MIS GASTOS"
        "categorias" -> "MIS CATEGORIAS"
        "colecciones" -> "MIS COLECCIONES"
        "facturas" -> "MIS FACTURAS"
        else -> dataType.uppercase()
    }
    
    /**
     * 📋 OBTENER HEADERS AMIGABLES (SIN IDs INTERNOS)
     */
    private fun getFriendlyHeaders(dataType: String): List<String> = when (dataType.lowercase()) {
        "inventario" -> listOf("Producto", "Precio Venta", "Stock", "Categoria", "Proveedor", "Activo")
        "clientes" -> listOf("Cliente", "Telefono", "Email", "Direccion", "Compras", "Ultima Compra")
        "ventas" -> listOf("Fecha", "Cliente", "Total", "Estado", "Productos", "Metodo Pago")
        "gastos" -> listOf("Fecha", "Descripcion", "Monto", "Categoria", "Proveedor", "Es Deducible")
        "categorias" -> listOf("Nombre", "Descripcion", "Color", "Activa")
        "colecciones" -> listOf("Nombre", "Descripcion", "Productos", "Clientes Asociados", "Estado")
        "facturas" -> listOf("Numero", "Fecha", "Cliente", "Total", "Estado")
        else -> emptyList()
    }
    
    /**
     * 📊 OBTENER DATOS AMIGABLES (SIN IDs INTERNOS)
     */
    private fun getFriendlyData(dataType: String, row: Map<String, Any>, customersMap: Map<String, String>): List<String> = when (dataType.lowercase()) {
        "inventario" -> listOf(
            row["Nombre"]?.toString() ?: "",
            formatPrice(row["Precio Venta"]),
            row["Stock"]?.toString() ?: "",
            row["Categoria"]?.toString() ?: "",
            row["Proveedor"]?.toString() ?: "Sin proveedor",
            row["Activo"]?.toString() ?: ""
        )
        "clientes" -> listOf(
            row["Nombre"]?.toString() ?: "",
            row["Telefono"]?.toString() ?: "",
            row["Email"]?.toString() ?: "",
            row["Direccion"]?.toString() ?: "",
            formatPrice(row["Compras Totales"]),
            formatDate(row["Ultima Compra"])
        )
        "ventas" -> listOf(
            formatDate(row["Fecha"]),
            getCustomerName(row["Cliente ID"]?.toString(), customersMap),
            formatPrice(row["Total"]),
            row["Estado"]?.toString() ?: "",
            "${row["Productos"]} productos",
            row["Metodo Pago"]?.toString() ?: ""
        )
        "gastos" -> listOf(
            formatDate(row["Fecha"]),
            row["Descripcion"]?.toString() ?: "",
            formatPrice(row["Monto"]),
            row["Categoria"]?.toString() ?: "",
            row["Proveedor"]?.toString() ?: "",
            row["Es Deducible"]?.toString() ?: ""
        )
        "categorias" -> listOf(
            row["Nombre"]?.toString() ?: "",
            row["Descripcion"]?.toString() ?: "",
            row["Color"]?.toString() ?: "",
            row["Activa"]?.toString() ?: ""
        )
        "colecciones" -> listOf(
            row["Nombre"]?.toString() ?: "",
            row["Descripcion"]?.toString() ?: "",
            "${row["Productos"]} productos",
            "${row["Clientes Asociados"]} clientes",
            row["Estado"]?.toString() ?: ""
        )
        "facturas" -> listOf(
            row["Numero"]?.toString() ?: "",
            formatDate(row["Fecha"]),
            getCustomerName(row["Cliente ID"]?.toString(), customersMap),
            formatPrice(row["Total"]),
            row["Estado"]?.toString() ?: ""
        )
        else -> emptyList()
    }
    
    /**
     * 👤 OBTENER NOMBRE DEL CLIENTE POR ID
     */
    private fun getCustomerName(customerId: String?, customersMap: Map<String, String>): String {
        if (customerId.isNullOrBlank()) return "Sin cliente"
        val customerName = customersMap[customerId] ?: "Cliente no encontrado"
        return removeAccents(customerName)
    }
    
    /**
     * 🧹 LIMPIAR DATOS PARA EL USUARIO
     */
    private fun cleanUserData(value: Any?): String {
        val stringValue = value?.toString() ?: ""
        return when {
            stringValue.contains("T00:00:00") -> stringValue.substringBefore("T") // Solo fecha, no hora
            stringValue.contains("T") -> stringValue.substringBefore("T") // Solo fecha
            stringValue == "null" || stringValue.isEmpty() -> ""
            else -> removeAccents(stringValue)
        }
    }
    
    /**
     * 🔤 REMOVER ACENTOS Y CARACTERES ESPECIALES
     */
    private fun removeAccents(text: String): String {
        return text
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .replace("Á", "A")
            .replace("É", "E")
            .replace("Í", "I")
            .replace("Ó", "O")
            .replace("Ú", "U")
            .replace("Ñ", "N")
            .replace("ü", "u")
            .replace("Ü", "U")
            .replace("¿", "")
            .replace("¡", "")
            .replace("°", "")
            .replace("$", "")
            .replace("€", "")
            .replace("£", "")
            .replace("¥", "")
    }
    
    /**
     * 💰 FORMATEAR PRECIO AMIGABLE
     */
    private fun formatPrice(value: Any?): String {
        return try {
            val price = value?.toString()?.toDoubleOrNull() ?: 0.0
            "$${String.format("%.0f", price)}"
        } catch (e: Exception) {
            "$0"
        }
    }
    
    /**
     * 📅 FORMATEAR FECHA AMIGABLE
     */
    private fun formatDate(value: Any?): String {
        val dateString = value?.toString() ?: ""
        return if (dateString.isEmpty()) {
            ""
        } else {
            try {
                val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dateString.substringBefore("T"))
                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date)
            } catch (e: Exception) {
                dateString.substringBefore("T") // Fallback simple
            }
        }
    }

    /**
     * 📊 EXPORTAR MÚLTIPLES CATEGORÍAS A EXCEL CON ARCHIVOS SEPARADOS
     */
    suspend fun exportToExcelWithMultipleSheets(
        dataTypes: List<String>,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando Excel con archivos separados...")
            
            onProgress(10, "Obteniendo datos de todas las categorías...")
            
            // Obtener datos de todas las categorías
            val allData = mutableMapOf<String, List<Map<String, Any>>>()
            dataTypes.forEachIndexed { index, dataType ->
                val progress = 10 + (index * 70 / dataTypes.size)
                onProgress(progress, "Obteniendo datos de $dataType...")
                
                val data = when (dataType.lowercase()) {
                    "inventario" -> getInventoryData()
                    "clientes" -> getCustomersData()
                    "ventas" -> getSalesData()
                    "gastos" -> getExpensesData()
                    "categorias" -> getCategoriesData()
                    "colecciones" -> getCollectionsData()
                    "facturas" -> getInvoicesData()
                    else -> emptyList()
                }
                
                if (data.isNotEmpty()) {
                    allData[dataType] = data
                }
            }
            
            onProgress(85, "Creando archivo Excel principal...")
            
            // Crear un archivo Excel principal que contenga la información de todos los archivos
            val timestamp = System.currentTimeMillis()
            val fileName = "Reporte_Completo_${timestamp}.xlsx"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                // Escribir encabezado principal
                writer.write("\uFEFF") // BOM para UTF-8
                writer.write("\"REPORTE COMPLETO DE DATOS\"\n")
                writer.write("\"Exportado el: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\"\n")
                writer.write("\"Generado por: NegocioListo\"\n")
                writer.write("\n")
                
                // Crear índice de archivos generados
                writer.write("\"INDICE DE ARCHIVOS GENERADOS:\"\n")
                allData.forEach { (dataType, data) ->
                    val sheetName = getSheetName(dataType)
                    writer.write("\"📄 $sheetName: ${data.size} registros\"\n")
                }
                writer.write("\n")
                
                // Escribir resumen de cada categoría
                allData.forEach { (dataType, data) ->
                    val sheetName = getSheetName(dataType)
                    writer.write("\"$sheetName\"\n")
                    writer.write("\"Total de registros: ${data.size}\"\n")
                    
                    if (data.isNotEmpty()) {
                        val headersList = data.first().keys.sorted()
                        val headers = headersList.joinToString(",") { "\"${localizeHeader(it)}\"" }
                        writer.write("$headers\n")
                        
                        // Mostrar solo las primeras 5 filas como muestra
                        data.take(5).forEach { row ->
                            val values = headersList.joinToString(",") { key -> 
                                val value = (row[key] ?: "").toString()
                                if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                                    "\"${value.replace("\"", "\"\"")}\""
                                } else {
                                    "\"$value\""
                                }
                            }
                            writer.write("$values\n")
                        }
                        
                        if (data.size > 5) {
                            writer.write("\"... y ${data.size - 5} registros más\"\n")
                        }
                    }
                    writer.write("\n")
                }
            }
            
            onProgress(95, "Guardando archivo Excel principal...")
            
            onProgress(100, "Excel principal generado con resumen de ${allData.size} categorías")
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    /**
     * 🔍 OBTENER DATOS PARA EXPORTACIÓN
     */
    suspend fun getExportData(
        dataTypes: List<String>,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Map<String, List<Map<String, Any>>>> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Obteniendo datos para exportación...")
            
            val exportData = mutableMapOf<String, List<Map<String, Any>>>()
            val totalTypes = dataTypes.size
            var currentType = 0
            
            dataTypes.forEach { dataType ->
                onProgress((currentType * 100) / totalTypes, "Procesando $dataType...")
                
                val data = when (dataType.lowercase()) {
                    "inventario", "productos" -> getInventoryData()
                    "clientes" -> getCustomersData()
                    "ventas" -> getSalesData()
                    "gastos" -> getExpensesData()
                    "colecciones" -> getCollectionsData()
                    "facturas" -> getInvoicesData()
                    "categorías", "categorias" -> getCategoriesData()
                    else -> emptyList()
                }
                
                exportData[dataType] = data
                currentType++
            }
            
            onProgress(100, "Datos obtenidos exitosamente")
            Result.success(exportData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📦 OBTENER DATOS DE INVENTARIO
     */
    private suspend fun getInventoryData(): List<Map<String, Any>> {
        return try {
            val products = inventoryRepository.getAllProducts().first()
            // Obtener categorías para mostrar nombres en lugar de IDs
            val categories = try {
                customCategoryRepository.getActiveCategoriesByUser(
                    authRepository.currentUser.first()?.id ?: ""
                ).first().associateBy { it.id }
            } catch (e: Exception) {
                emptyMap()
            }
            
            products.map { product ->
                val categoryName = categories[product.customCategoryId]?.name ?: product.customCategoryId
                mapOf(
                    "Nombre" to product.name,
                    "SKU" to product.sku,
                    "Categoría" to categoryName,
                    "Precio Compra" to String.format("%.0f", product.purchasePrice),
                    "Precio Venta" to String.format("%.0f", product.salePrice),
                    "Stock Actual" to product.stockQuantity,
                    "Stock Mínimo" to product.minimumStock,
                    "Estado Stock" to if (product.stockQuantity <= product.minimumStock) "Bajo" else "Normal",
                    "Proveedor" to (product.supplier ?: ""),
                    "Descripción" to (product.description ?: "")
                )
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo datos de inventario: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 👥 OBTENER DATOS DE CLIENTES
     */
    private suspend fun getCustomersData(): List<Map<String, Any>> {
        return try {
            val customers = customerRepository.getAllCustomers().first()
            customers.map { customer ->
                val lastPurchase = customer.lastPurchaseDate?.let {
                    val instant = it.toInstant(TimeZone.currentSystemDefault())
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                        java.util.Date(instant.toEpochMilliseconds())
                    )
                } ?: "Sin compras"
                
                mapOf(
                    "Nombre" to customer.name,
                    "Email" to (customer.email ?: ""),
                    "Teléfono" to (customer.phone ?: ""),
                    "Dirección" to (customer.address ?: ""),
                    "Total Compras" to String.format("%.0f", customer.totalPurchases),
                    "Última Compra" to lastPurchase,
                    "Notas" to (customer.notes ?: "")
                )
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo datos de clientes: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 💰 OBTENER DATOS DE VENTAS
     */
    private suspend fun getSalesData(): List<Map<String, Any>> {
        return try {
            val sales = salesRepository.getSales().first()
            // Obtener clientes para mapear nombres
            val customers = try {
                customerRepository.getAllCustomers().first()
            } catch (e: Exception) {
                emptyList()
            }
            
            sales.flatMap { sale ->
                val customerName = sale.customerId?.let { customerId ->
                    customers.firstOrNull { it.id == customerId }?.name ?: "Cliente desconocido"
                } ?: "Sin cliente"
                
                val saleDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                    java.util.Date(sale.date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
                )
                
                // Crear una fila por cada item de la venta
                if (sale.items.isNotEmpty()) {
                    sale.items.mapIndexed { index, item ->
                        mapOf(
                            "Fecha" to saleDate,
                            "Cliente" to customerName,
                            "Producto" to item.productName,
                            "Cantidad" to item.quantity,
                            "Precio Unitario" to String.format("%.0f", item.unitPrice),
                            "Subtotal Item" to String.format("%.0f", item.lineTotal),
                            "Total Venta" to String.format("%.0f", sale.total),
                            "Método de Pago" to sale.paymentMethod.displayName,
                            "Estado" to when (sale.status) {
                                com.negociolisto.app.domain.model.SaleStatus.ACTIVE -> "Activa"
                                com.negociolisto.app.domain.model.SaleStatus.CANCELED -> "Cancelada"
                            },
                            "Notas" to (sale.note ?: "")
                        )
                    }
                } else {
                    listOf(
                        mapOf(
                            "Fecha" to saleDate,
                            "Cliente" to customerName,
                            "Total" to String.format("%.0f", sale.total),
                            "Cantidad Items" to sale.getTotalItemCount(),
                            "Método de Pago" to sale.paymentMethod.displayName,
                            "Estado" to when (sale.status) {
                                com.negociolisto.app.domain.model.SaleStatus.ACTIVE -> "Activa"
                                com.negociolisto.app.domain.model.SaleStatus.CANCELED -> "Cancelada"
                            },
                            "Notas" to (sale.note ?: "")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo datos de ventas: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 💸 OBTENER DATOS DE GASTOS
     */
    private suspend fun getExpensesData(): List<Map<String, Any>> {
        return try {
            val expenses = expenseRepository.getAllExpenses().first()
            expenses.map { expense ->
                val expenseDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                    java.util.Date(expense.date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
                )
                
                mapOf(
                    "Fecha" to expenseDate,
                    "Monto" to String.format("%.0f", expense.amount),
                    "Categoría" to expense.category.displayName,
                    "Descripción" to expense.description,
                    "Proveedor" to (expense.supplier ?: ""),
                    "Número Comprobante" to (expense.receiptNumber ?: ""),
                    "Estado" to expense.status.displayName,
                    "Notas" to (expense.notes ?: "")
                )
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo datos de gastos: ${e.message}")
            emptyList()
        }
    }
    
    
    /**
     * 📚 OBTENER DATOS DE COLECCIONES
     */
    private suspend fun getCollectionsData(): List<Map<String, Any>> {
        return try {
            val collections = collectionRepository.getCollections().first()
            collections.map { collection ->
                mapOf(
                    "Nombre" to collection.name,
                    "Descripción" to (collection.description ?: ""),
                    "Total Productos" to collection.items.size,
                    "Productos Únicos" to collection.getUniqueProductCount(),
                    "Clientes Asociados" to collection.associatedCustomerIds.size,
                    "Estado" to collection.status.displayName
                )
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo datos de colecciones: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 📄 OBTENER DATOS DE FACTURAS
     */
    private suspend fun getInvoicesData(): List<Map<String, Any>> {
        return try {
            val invoices = invoiceRepository.getInvoices().first()
            // Obtener clientes para mapear nombres
            val customers = try {
                customerRepository.getAllCustomers().first()
            } catch (e: Exception) {
                emptyList()
            }
            
            invoices.flatMap { invoice ->
                val customerName = invoice.customerId?.let { customerId ->
                    customers.firstOrNull { it.id == customerId }?.name ?: "Cliente desconocido"
                } ?: "Sin cliente"
                
                val invoiceDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                    java.util.Date(invoice.date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
                )
                
                // Crear una fila por cada item de la factura
                if (invoice.items.isNotEmpty()) {
                    invoice.items.mapIndexed { index, item ->
                        mapOf(
                            "Número Factura" to invoice.number,
                            "Fecha" to invoiceDate,
                            "Cliente" to customerName,
                            "Producto" to item.description,
                            "Cantidad" to item.quantity,
                            "Precio Unitario" to String.format("%.0f", item.unitPrice),
                            "Subtotal Item" to String.format("%.0f", item.total),
                            "Subtotal Factura" to String.format("%.0f", invoice.subtotal),
                            "Impuestos" to String.format("%.0f", invoice.tax),
                            "Total Factura" to String.format("%.0f", invoice.total),
                            "Notas" to (invoice.notes ?: "")
                        )
                    }
                } else {
                    listOf(
                        mapOf(
                            "Número Factura" to invoice.number,
                            "Fecha" to invoiceDate,
                            "Cliente" to customerName,
                            "Subtotal" to String.format("%.0f", invoice.subtotal),
                            "Impuestos" to String.format("%.0f", invoice.tax),
                            "Total" to String.format("%.0f", invoice.total),
                            "Cantidad Items" to invoice.items.size,
                            "Notas" to (invoice.notes ?: "")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo datos de facturas: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 📂 OBTENER DATOS DE CATEGORÍAS PERSONALIZADAS
     */
    private suspend fun getCategoriesData(): List<Map<String, Any>> {
        return try {
            // Obtener el ID del usuario actual
            val currentUserId = authRepository.currentUser.first()?.id ?: ""
            val categories = customCategoryRepository.getActiveCategoriesByUser(currentUserId).first()
            categories.map { category ->
                mapOf(
                    "ID" to category.id,
                    "Nombre" to category.name,
                    "Icono" to category.icon,
                    "Color" to category.color,
                    "Descripcion" to (category.description ?: ""),
                    "Orden" to category.sortOrder,
                    "Fecha Creacion" to category.createdAt.toString(),
                    "Ultima Actualizacion" to category.updatedAt.toString(),
                    "Activa" to if (category.isActive) "Si" else "No",
                    "Usuario ID" to category.userId
                )
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo datos de categorías: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * ⚙️ OBTENER DATOS DE AJUSTES DE USUARIO
     */
    private suspend fun getUserSettingsData(): List<Map<String, Any>> {
        return try {
            val user = authRepository.currentUser.first()
            if (user != null) {
                listOf(
                    mapOf(
                        "Tipo" to "Perfil Usuario",
                        "ID" to user.id,
                        "Nombre" to user.name,
                        "Email" to user.email,
                        "Telefono" to (user.phone ?: ""),
                        "Verificado" to if (user.isEmailVerified) "Si" else "No",
                        "Fecha Creacion" to (user.createdAt?.toString() ?: ""),
                        "Ultimo Login" to (user.lastLoginAt?.toString() ?: "")
                    ),
                    mapOf(
                        "Tipo" to "Empresa",
                        "Nombre" to (user.businessName ?: "Sin nombre"),
                        "RUT" to (user.businessRut ?: ""),
                        "Direccion" to (user.businessAddress ?: ""),
                        "Telefono" to (user.businessPhone ?: ""),
                        "Email" to (user.businessEmail ?: ""),
                        "Tipo Negocio" to (user.businessType?.name ?: ""),
                        "Sincronizacion Nube" to if (user.isCloudSyncEnabled) "Habilitada" else "Deshabilitada"
                    )
                )
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo datos de usuario: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 📄 EXPORTAR PDF ÚNICO CON MÚLTIPLES PÁGINAS
     */
    suspend fun exportToSinglePDF(
        dataTypes: List<String>,
        dataMap: Map<String, List<Map<String, Any>>>,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Preparando PDF único con múltiples páginas...")
            
            if (dataMap.isEmpty()) {
                return@withContext Result.failure(Exception("No hay datos para exportar"))
            }
            
            onProgress(10, "Generando PDF único...")
            
            val fileName = "datos_completos_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            
            val doc = android.graphics.pdf.PdfDocument()
            // Usar formato LANDSCAPE (horizontal) para más espacio horizontal
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(842, 595, 1).create() // A4 Landscape
            var page = doc.startPage(pageInfo)
            var canvas = page.canvas
            val paint = android.graphics.Paint()
            var y = 40f
            val margin = 40f
            val pageWidth = pageInfo.pageWidth.toFloat()
            val pageHeight = pageInfo.pageHeight.toFloat()
            
            // Título principal
            paint.textSize = 18f
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.BLACK
            canvas.drawText("EXPORTACION COMPLETA DE DATOS", margin, y, paint)
            y += 30f
            
            // Fecha de exportación
            paint.textSize = 12f
            paint.isFakeBoldText = false
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            canvas.drawText("Exportado el: ${dateFormat.format(Date())}", margin, y, paint)
            y += 20f
            
            // Línea separadora
            paint.strokeWidth = 2f
            canvas.drawLine(margin, y, pageWidth - margin, y, paint)
            y += 20f
            
            // Procesar cada tipo de datos
            dataTypes.forEachIndexed { typeIndex, dataType ->
                val data = dataMap[dataType] ?: return@forEachIndexed
                
                val sectionStartProgress = 20 + (60 * typeIndex / dataTypes.size)
                onProgress(sectionStartProgress, "Iniciando sección: $dataType...")
                delay(100) // Permitir que la UI se actualice
                
                // Verificar si necesitamos nueva página
                if (y > pageHeight - margin - 100) {
                    doc.finishPage(page)
                    page = doc.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin
                }
                
                // Título de sección
                paint.textSize = 16f
                paint.isFakeBoldText = true
                paint.color = android.graphics.Color.BLUE
                canvas.drawText("=== $dataType ===", margin, y, paint)
                y += 25f
                
                if (data.isEmpty()) {
                    paint.textSize = 12f
                    paint.isFakeBoldText = false
                    paint.color = android.graphics.Color.GRAY
                    canvas.drawText("No hay datos disponibles", margin, y, paint)
                    y += 20f
                    return@forEachIndexed
                }
                
                // Encabezados de la tabla
                paint.isFakeBoldText = true
                paint.color = android.graphics.Color.BLACK
                val headers = data.first().keys.sortedBy { it }.map { localizeHeader(it) }
                
                // Ajustar tamaño de fuente según número de columnas
                val fontSize = if (headers.size > 8) 8f else 10f
                paint.textSize = fontSize
                
                val columnWidth = (pageWidth - 2 * margin) / headers.size
                val maxTextWidth = columnWidth - 4f // Margen dentro de la celda
                
                // Función auxiliar para truncar texto
                fun truncateText(text: String, maxWidth: Float, paint: android.graphics.Paint): String {
                    val measurePaint = android.graphics.Paint(paint)
                    val textWidth = measurePaint.measureText(text)
                    if (textWidth <= maxWidth) {
                        return text
                    }
                    
                    // Calcular cuántos caracteres caben
                    var truncated = text
                    while (truncated.isNotEmpty() && measurePaint.measureText("$truncated...") > maxWidth) {
                        truncated = truncated.dropLast(1)
                    }
                    return if (truncated.isEmpty()) "" else "$truncated..."
                }
                
                // Dibujar encabezados TRUNCADOS para evitar superposición
                headers.forEachIndexed { index, header ->
                    val truncatedHeader = truncateText(header, maxTextWidth, paint)
                    canvas.drawText(truncatedHeader, margin + index * columnWidth, y, paint)
                }
                y += 15f
                canvas.drawLine(margin, y, pageWidth - margin, y, paint)
                y += 10f
                paint.isFakeBoldText = false
                
                // Datos de la tabla
                data.forEachIndexed { rowIndex, row ->
                    // Reportar progreso durante el proceso
                    if (rowIndex % 10 == 0 || rowIndex == data.size - 1) {
                        val sectionProgress = sectionStartProgress
                        val totalRows = data.size
                        val rowProgressPercent = if (totalRows > 0) {
                            (rowIndex.toFloat() / totalRows * 15).toInt()
                        } else 0
                        val currentProgress = sectionProgress + rowProgressPercent
                        val totalProgress = minOf(currentProgress, sectionStartProgress + 15)
                        onProgress(
                            totalProgress, 
                            "Generando PDF: ${dataType.replaceFirstChar { it.uppercase() }} - ${rowIndex + 1}/$totalRows registros..."
                        )
                    }
                    
                    if (y > pageHeight - margin - 20) {
                        doc.finishPage(page)
                        page = doc.startPage(pageInfo)
                        canvas = page.canvas
                        y = margin
                        
                        // Redibujar encabezados en nueva página
                        paint.textSize = fontSize
                        paint.isFakeBoldText = true
                        headers.forEachIndexed { index, header ->
                            val truncatedHeader = truncateText(header, maxTextWidth, paint)
                            canvas.drawText(truncatedHeader, margin + index * columnWidth, y, paint)
                        }
                        y += 15f
                        canvas.drawLine(margin, y, pageWidth - margin, y, paint)
                        y += 10f
                        paint.isFakeBoldText = false
                    }
                    
                    headers.forEachIndexed { index, header ->
                        val originalHeader = localizeHeader(header, reverse = true)
                        val value = row[originalHeader]?.toString() ?: ""
                        val truncatedValue = truncateText(value, maxTextWidth, paint)
                        // Restaurar tamaño de fuente para datos
                        paint.textSize = fontSize
                        canvas.drawText(truncatedValue, margin + index * columnWidth, y, paint)
                    }
                    y += 15f
                }
                
                // Espacio entre secciones
                y += 20f
            }
            
            doc.finishPage(page)
            
            onProgress(90, "Guardando PDF...")
            
            val outputStream = file.outputStream()
            doc.writeTo(outputStream)
            doc.close()
            outputStream.close()
            
            onProgress(100, "PDF único generado exitosamente")
            
            val uri = FileProvider.getUriForFile(context, "com.negociolisto.app.fileprovider", file)
            Result.success(uri)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🔍 FILTRAR DATOS POR RANGO DE FECHAS
     */
    fun filterDataByDateRange(
        data: List<Map<String, Any>>,
        dateRange: Pair<Date?, Date?>
    ): List<Map<String, Any>> {
        if (dateRange.first == null && dateRange.second == null) {
            return data
        }
        
        return data.filter { row ->
            val dateValue = row.values.find { value ->
                value is Date || (value is String && value.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
            }
            
            if (dateValue == null) return@filter true
            
            val date = when (dateValue) {
                is Date -> dateValue
                is String -> try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateValue)
                } catch (e: Exception) {
                    null
                }
                else -> null
            }
            
            if (date == null) return@filter true
            
            val isAfterStart = dateRange.first?.let { date >= it } ?: true
            val isBeforeEnd = dateRange.second?.let { date <= it } ?: true
            
            isAfterStart && isBeforeEnd
        }
    }
    
    /**
     * 📊 CREAR NOMBRE DE HOJA PROFESIONAL
     */
    private fun getSheetName(dataType: String): String = when (dataType.lowercase()) {
        "inventario" -> "📦 Inventario"
        "clientes" -> "👥 Clientes"
        "ventas" -> "💰 Ventas"
        "gastos" -> "💸 Gastos"
        "categorias" -> "🏷️ Categorias"
        "colecciones" -> "📚 Colecciones"
        "facturas" -> "🧾 Facturas"
        else -> dataType.replaceFirstChar { it.uppercase() }
    }
    

    /**
     * 🌐 LOCALIZACIÓN DE ENCABEZADOS A ESPAÑOL
     */
    private fun localizeHeader(key: String, reverse: Boolean = false): String = when (key.lowercase()) {
        "id" -> "ID"
        "name", "nombre" -> "Nombre"
        "price", "precio" -> "Precio"
        "stock" -> "Stock"
        "category", "categoría" -> "Categoria"
        "email" -> "Email"
        "phone", "teléfono" -> "Telefono"
        "date", "fecha" -> "Fecha"
        "total" -> "Total"
        "customer", "cliente" -> "Cliente"
        "number", "número" -> "Numero"
        "status", "estado" -> "Estado"
        else -> if (reverse) {
            // Mapeo inverso para encontrar la clave original
            when (key) {
                "ID" -> "id"
                "Nombre" -> "name"
                "Precio" -> "price"
                "Stock" -> "stock"
                "Categoria" -> "category"
                "Email" -> "email"
                "Telefono" -> "phone"
                "Fecha" -> "date"
                "Total" -> "total"
                "Cliente" -> "customer"
                "Numero" -> "number"
                "Estado" -> "status"
                else -> key.lowercase()
            }
        } else {
            key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }
}
