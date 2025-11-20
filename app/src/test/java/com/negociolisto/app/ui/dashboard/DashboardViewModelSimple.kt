package com.negociolisto.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.DashboardRepositoryMock
import com.negociolisto.app.domain.model.BusinessMetrics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModelSimple : ViewModel() {
    val metrics: StateFlow<BusinessMetrics> = DashboardRepositoryMock.getBusinessMetrics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessMetrics(0.0,0.0,0.0,0.0,0, emptyList(), emptyList(), emptyList()))
}


