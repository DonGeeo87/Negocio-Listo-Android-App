package com.negociolisto.app.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.local.UiPreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val uiPreferencesStore = UiPreferencesStore(context)

    fun completeOnboarding() {
        viewModelScope.launch {
            uiPreferencesStore.setOnboardingSeen(true)
        }
    }
}
