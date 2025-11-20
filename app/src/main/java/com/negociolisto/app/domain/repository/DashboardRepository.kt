package com.negociolisto.app.domain.repository

import com.negociolisto.app.domain.model.BusinessMetrics
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getBusinessMetrics(): Flow<BusinessMetrics>
}


