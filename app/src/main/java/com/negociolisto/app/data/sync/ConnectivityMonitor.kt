package com.negociolisto.app.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📡 MONITOR DE CONECTIVIDAD OPTIMIZADO
 * 
 * Monitorea el estado de la conexión a internet y notifica cambios
 * para activar la sincronización automática.
 * 
 * Optimizado para evitar memory leaks y mejorar rendimiento.
 */
@Singleton
class ConnectivityMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val mutex = Mutex()
    private var isCallbackRegistered = false

    /**
     * Flujo que emite el estado de conectividad (optimizado)
     */
    fun isConnected(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                try {
                    trySend(true)
                } catch (e: Exception) {
                    // Log error but don't crash
                    println("❌ Error en onAvailable: ${e.message}")
                }
            }

            override fun onLost(network: Network) {
                try {
                    trySend(false)
                } catch (e: Exception) {
                    // Log error but don't crash
                    println("❌ Error en onLost: ${e.message}")
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                try {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    trySend(hasInternet)
                } catch (e: Exception) {
                    // Log error but don't crash
                    println("❌ Error en onCapabilitiesChanged: ${e.message}")
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, callback)
            isCallbackRegistered = true

            // Enviar estado inicial de forma segura
            val currentNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            trySend(isConnected)
        } catch (e: Exception) {
            println("❌ Error registrando NetworkCallback: ${e.message}")
            trySend(false)
        }

        awaitClose {
            if (isCallbackRegistered) {
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                    isCallbackRegistered = false
                } catch (e: Exception) {
                    println("❌ Error desregistrando NetworkCallback: ${e.message}")
                }
            }
        }
    }.distinctUntilChanged()

    /**
     * Verificar si hay conexión a internet (método síncrono)
     */
    fun hasInternetConnection(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            println("❌ Error verificando conectividad: ${e.message}")
            false
        }
    }

    /**
     * Verificar si hay conexión WiFi
     */
    fun isWifiConnected(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            println("❌ Error verificando WiFi: ${e.message}")
            false
        }
    }

    /**
     * Verificar si hay conexión móvil
     */
    fun isMobileConnected(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            println("❌ Error verificando conexión móvil: ${e.message}")
            false
        }
    }

    /**
     * Obtener tipo de conexión actual
     */
    fun getConnectionType(): String {
        return when {
            isWifiConnected() -> "WiFi"
            isMobileConnected() -> "Mobile"
            hasInternetConnection() -> "Other"
            else -> "None"
        }
    }
}
