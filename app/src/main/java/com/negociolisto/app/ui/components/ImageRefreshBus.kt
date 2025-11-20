package com.negociolisto.app.ui.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Bus simple para invalidar caché visual de imágenes tras guardar cambios.
 */
object ImageRefreshBus {
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version

    fun bump() {
        _version.value = (_version.value + 1) % 1000000
    }
}


