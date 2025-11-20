package com.negociolisto.app.ui.main

import androidx.lifecycle.ViewModel
import com.negociolisto.app.data.analytics.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel para tracking de navegación
 */
@HiltViewModel
class NavigationTrackingViewModel @Inject constructor(
    val analyticsHelper: AnalyticsHelper
) : ViewModel()

