package com.negociolisto.app.data.analytics

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⚡ Helper para Firebase Performance Monitoring
 * 
 * Centraliza el monitoreo de rendimiento de la aplicación
 */
@Singleton
class PerformanceHelper @Inject constructor() {
    
    private val performance: FirebasePerformance = FirebasePerformance.getInstance()
    
    /**
     * Inicia un trace personalizado
     */
    fun startTrace(traceName: String): Trace {
        return performance.newTrace(traceName).apply {
            start()
        }
    }
    
    /**
     * Detiene un trace
     */
    fun stopTrace(trace: Trace) {
        trace.stop()
    }
    
    /**
     * Ejecuta un bloque de código y mide su rendimiento
     */
    inline fun <T> measureTrace(traceName: String, block: () -> T): T {
        val trace = startTrace(traceName)
        return try {
            block()
        } finally {
            stopTrace(trace)
        }
    }
    
    // Nota: Trace ya tiene métodos putAttribute() e incrementMetric()
    // Se pueden usar directamente desde la instancia de Trace retornada por startTrace()
    
    /**
     * Traces comunes para operaciones de la app
     */
    object Traces {
        const val SCREEN_LOAD = "screen_load"
        const val PRODUCT_LIST_LOAD = "product_list_load"
        const val PRODUCT_DETAIL_LOAD = "product_detail_load"
        const val SALE_CREATION = "sale_creation"
        const val INVOICE_GENERATION = "invoice_generation"
        const val FIREBASE_SYNC = "firebase_sync"
        const val BACKUP_OPERATION = "backup_operation"
        const val RESTORE_OPERATION = "restore_operation"
        const val IMAGE_UPLOAD = "image_upload"
        const val DATABASE_QUERY = "database_query"
    }
    
    /**
     * Métricas comunes
     */
    object Metrics {
        const val PRODUCT_COUNT = "product_count"
        const val CUSTOMER_COUNT = "customer_count"
        const val SALE_COUNT = "sale_count"
        const val ITEM_COUNT = "item_count"
        const val FILE_SIZE = "file_size"
        const val QUERY_RESULTS = "query_results"
    }
}

