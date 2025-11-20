package com.negociolisto.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.domain.model.BusinessMetrics
import com.negociolisto.app.domain.model.User
import com.negociolisto.app.domain.repository.AuthRepository
import com.negociolisto.app.domain.repository.DashboardRepository
import com.negociolisto.app.data.service.LoginTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 📊 VIEWMODEL DE DASHBOARD CON HILT
 * 
 * Maneja la lógica de negocio para el dashboard principal.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val authRepository: AuthRepository,
    private val loginTrackingService: LoginTrackingService
) : ViewModel() {
    
    val businessMetrics: StateFlow<BusinessMetrics> = dashboardRepository.getBusinessMetrics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), BusinessMetrics(
            totalSales = 0.0,
            totalExpenses = 0.0,
            grossMargin = 0.0,
            grossMarginPercent = 0.0,
            lowStockCount = 0,
            topProducts = emptyList(),
            topCustomers = emptyList()
        ))
    
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), null)
    
    // Estadísticas de login
    private val _loginCount = MutableStateFlow(0)
    val loginCount: StateFlow<Int> = _loginCount.asStateFlow()
    
    private val _lastLogin = MutableStateFlow<String?>(null)
    val lastLogin: StateFlow<String?> = _lastLogin.asStateFlow()
    
    private val _firstLogin = MutableStateFlow<String?>(null)
    val firstLogin: StateFlow<String?> = _firstLogin.asStateFlow()
    
    init {
        // Cargar estadísticas de login al inicializar
        loadLoginStats()
    }
    
    private fun loadLoginStats() {
        _loginCount.value = loginTrackingService.getLoginCount()
        _lastLogin.value = loginTrackingService.getLastLogin()?.toString()
        _firstLogin.value = loginTrackingService.getFirstLogin()?.toString()
    }
}
