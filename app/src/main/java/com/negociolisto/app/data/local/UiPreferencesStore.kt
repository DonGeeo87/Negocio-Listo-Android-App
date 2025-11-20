package com.negociolisto.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.uiPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "ui_prefs")

@Singleton
class UiPreferencesStore @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        val APP_SCALE = doublePreferencesKey("app_scale")
        const val DEFAULT_SCALE = 1.0
        const val MIN_SCALE = 0.85
        const val MAX_SCALE = 1.15

        // Onboarding flags
        val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
        val INITIAL_CATEGORIES_CONFIGURED_KEY = booleanPreferencesKey("initial_categories_configured")
        
        // Profile setup flags
        val PROFILE_SETUP_COMPLETED_KEY = booleanPreferencesKey("profile_setup_completed")
        val COMPANY_SETUP_COMPLETED_KEY = booleanPreferencesKey("company_setup_completed")
        val EMAIL_VERIFIED_KEY = booleanPreferencesKey("email_verified")
        val PROFILE_REMINDER_DISMISSED_KEY = booleanPreferencesKey("profile_reminder_dismissed")
        val COMPANY_REMINDER_DISMISSED_KEY = booleanPreferencesKey("company_reminder_dismissed")
        val COACH_MARK_DASHBOARD = booleanPreferencesKey("coach_mark_dashboard")
        val COACH_MARK_INVENTORY = booleanPreferencesKey("coach_mark_inventory")
        val COACH_MARK_SALES = booleanPreferencesKey("coach_mark_sales")
        val COACH_MARK_EXPENSES = booleanPreferencesKey("coach_mark_expenses")
        val COACH_MARK_CUSTOMERS = booleanPreferencesKey("coach_mark_customers")
        val COACH_MARK_COLLECTIONS = booleanPreferencesKey("coach_mark_collections")
        val COACH_MARK_INVOICES = booleanPreferencesKey("coach_mark_invoices")
    }

    val appScale: Flow<Double> = context.uiPrefsDataStore.data.map { prefs ->
        (prefs[APP_SCALE] ?: DEFAULT_SCALE).coerceIn(MIN_SCALE, MAX_SCALE)
    }

    suspend fun setAppScale(scale: Double) {
        val clamped = scale.coerceIn(MIN_SCALE, MAX_SCALE)
        context.uiPrefsDataStore.edit { prefs ->
            prefs[APP_SCALE] = clamped
        }
    }

    // Onboarding flags
    val onboardingSeen: Flow<Boolean> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[ONBOARDING_SEEN] ?: false
    }

    suspend fun setOnboardingSeen(seen: Boolean) {
        context.uiPrefsDataStore.edit { prefs ->
            prefs[ONBOARDING_SEEN] = seen
        }
    }

    // Initial categories configuration flags
    suspend fun setInitialCategoriesConfigured(configured: Boolean) {
        context.uiPrefsDataStore.edit { prefs ->
            prefs[INITIAL_CATEGORIES_CONFIGURED_KEY] = configured
        }
    }

    fun hasConfiguredInitialCategories(): Flow<Boolean> {
        return context.uiPrefsDataStore.data.map { prefs ->
            prefs[INITIAL_CATEGORIES_CONFIGURED_KEY] ?: false
        }
    }

    // Profile setup flows
    val profileSetupCompleted: Flow<Boolean> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[PROFILE_SETUP_COMPLETED_KEY] ?: false
    }

    val companySetupCompleted: Flow<Boolean> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[COMPANY_SETUP_COMPLETED_KEY] ?: false
    }

    val emailVerified: Flow<Boolean> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[EMAIL_VERIFIED_KEY] ?: false
    }

    // Profile setup suspend functions
    suspend fun setProfileSetupCompleted(completed: Boolean) {
        context.uiPrefsDataStore.edit { prefs ->
            prefs[PROFILE_SETUP_COMPLETED_KEY] = completed
        }
    }

    suspend fun setCompanySetupCompleted(completed: Boolean) {
        context.uiPrefsDataStore.edit { prefs ->
            prefs[COMPANY_SETUP_COMPLETED_KEY] = completed
        }
    }

    suspend fun setEmailVerified(verified: Boolean) {
        context.uiPrefsDataStore.edit { prefs ->
            prefs[EMAIL_VERIFIED_KEY] = verified
        }
    }

    suspend fun setProfileReminderDismissed(dismissed: Boolean) {
        context.uiPrefsDataStore.edit { prefs ->
            prefs[PROFILE_REMINDER_DISMISSED_KEY] = dismissed
        }
    }

    suspend fun setCompanyReminderDismissed(dismissed: Boolean) {
        context.uiPrefsDataStore.edit { prefs ->
            prefs[COMPANY_REMINDER_DISMISSED_KEY] = dismissed
        }
    }

    suspend fun resetTutorials() {
        context.uiPrefsDataStore.edit { prefs ->
            prefs[ONBOARDING_SEEN] = false
            prefs[INITIAL_CATEGORIES_CONFIGURED_KEY] = false
            prefs[COACH_MARK_DASHBOARD] = false
            prefs[COACH_MARK_INVENTORY] = false
            prefs[COACH_MARK_SALES] = false
            prefs[COACH_MARK_EXPENSES] = false
            prefs[COACH_MARK_CUSTOMERS] = false
            prefs[COACH_MARK_COLLECTIONS] = false
            prefs[COACH_MARK_INVOICES] = false
        }
    }

    // Coach mark flags
    suspend fun setCoachMarkSeen(screen: String, seen: Boolean) {
        val key = when (screen) {
            "dashboard" -> COACH_MARK_DASHBOARD
            "inventory" -> COACH_MARK_INVENTORY
            "sales" -> COACH_MARK_SALES
            "expenses" -> COACH_MARK_EXPENSES
            "customers" -> COACH_MARK_CUSTOMERS
            "collections" -> COACH_MARK_COLLECTIONS
            "invoices" -> COACH_MARK_INVOICES
            else -> return
        }
        context.uiPrefsDataStore.edit { prefs ->
            prefs[key] = seen
        }
    }

    fun isCoachMarkSeen(screen: String): Flow<Boolean> {
        val key = when (screen) {
            "dashboard" -> COACH_MARK_DASHBOARD
            "inventory" -> COACH_MARK_INVENTORY
            "sales" -> COACH_MARK_SALES
            "expenses" -> COACH_MARK_EXPENSES
            "customers" -> COACH_MARK_CUSTOMERS
            "collections" -> COACH_MARK_COLLECTIONS
            "invoices" -> COACH_MARK_INVOICES
            else -> return kotlinx.coroutines.flow.flowOf(false)
        }
        return context.uiPrefsDataStore.data.map { prefs ->
            prefs[key] ?: false
        }
    }
    
    // 🔐 USER-SPECIFIC PREFERENCES (PER USER ID)
    // These preferences are stored per userId to support multiple users on same device
    
    /**
     * Check if initial setup (categories + profile) is completed for a specific user
     */
    fun initialSetupCompletedForUser(userId: String): Flow<Boolean> {
        val key = booleanPreferencesKey("initial_setup_completed_$userId")
        return context.uiPrefsDataStore.data.map { prefs ->
            prefs[key] ?: false
        }
    }
    
    /**
     * Mark initial setup as completed for a specific user
     */
    suspend fun setInitialSetupCompletedForUser(userId: String, completed: Boolean) {
        val key = booleanPreferencesKey("initial_setup_completed_$userId")
        context.uiPrefsDataStore.edit { prefs ->
            prefs[key] = completed
        }
    }
    
    /**
     * Check if onboarding was seen by a specific user
     */
    fun onboardingSeenForUser(userId: String): Flow<Boolean> {
        val key = booleanPreferencesKey("onboarding_seen_$userId")
        return context.uiPrefsDataStore.data.map { prefs ->
            prefs[key] ?: false
        }
    }
    
    /**
     * Mark onboarding as seen for a specific user
     */
    suspend fun setOnboardingSeenForUser(userId: String, seen: Boolean) {
        val key = booleanPreferencesKey("onboarding_seen_$userId")
        context.uiPrefsDataStore.edit { prefs ->
            prefs[key] = seen
        }
    }
}


