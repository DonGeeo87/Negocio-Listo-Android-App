package com.negociolisto.app.data.service

import android.content.Context
import android.content.SharedPreferences
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔐 SERVICIO DE TRACKING DE LOGIN
 * 
 * Maneja el seguimiento del último login del usuario
 */
@Singleton
class LoginTrackingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("login_tracking", Context.MODE_PRIVATE)
    
    companion object {
        const val KEY_LAST_LOGIN = "last_login_timestamp"
        const val KEY_LOGIN_COUNT = "login_count"
        const val KEY_FIRST_LOGIN = "first_login_timestamp"
    }
    
    /**
     * Registrar login exitoso
     */
    fun recordLogin() {
        val currentTime = System.currentTimeMillis()
        val loginCount = prefs.getInt(KEY_LOGIN_COUNT, 0) + 1
        
        prefs.edit()
            .putLong(KEY_LAST_LOGIN, currentTime)
            .putInt(KEY_LOGIN_COUNT, loginCount)
            .putLong(KEY_FIRST_LOGIN, prefs.getLong(KEY_FIRST_LOGIN, currentTime))
            .apply()
    }
    
    /**
     * Obtener último login
     */
    fun getLastLogin(): Instant? {
        val timestamp = prefs.getLong(KEY_LAST_LOGIN, -1)
        return if (timestamp > 0) {
            Instant.fromEpochMilliseconds(timestamp)
        } else {
            null
        }
    }
    
    /**
     * Obtener primer login
     */
    fun getFirstLogin(): Instant? {
        val timestamp = prefs.getLong(KEY_FIRST_LOGIN, -1)
        return if (timestamp > 0) {
            Instant.fromEpochMilliseconds(timestamp)
        } else {
            null
        }
    }
    
    /**
     * Obtener número de logins
     */
    fun getLoginCount(): Int {
        return prefs.getInt(KEY_LOGIN_COUNT, 0)
    }
    
    /**
     * Obtener información completa de login
     */
    fun getLoginInfo(): LoginInfo {
        return LoginInfo(
            lastLogin = getLastLogin(),
            firstLogin = getFirstLogin(),
            loginCount = getLoginCount()
        )
    }
    
    /**
     * Restaurar información de login desde backup
     */
    fun restoreLoginInfo(loginInfo: LoginInfo) {
        prefs.edit()
            .putLong(KEY_LAST_LOGIN, loginInfo.lastLogin?.toEpochMilliseconds() ?: -1)
            .putLong(KEY_FIRST_LOGIN, loginInfo.firstLogin?.toEpochMilliseconds() ?: -1)
            .putInt(KEY_LOGIN_COUNT, loginInfo.loginCount)
            .apply()
    }
    
    /**
     * Limpiar información de login
     */
    fun clearLoginInfo() {
        prefs.edit().clear().apply()
    }
}

/**
 * 📊 INFORMACIÓN DE LOGIN
 */
data class LoginInfo(
    val lastLogin: Instant? = null,
    val firstLogin: Instant? = null,
    val loginCount: Int = 0
) {
    /**
     * Verificar si es el primer login
     */
    fun isFirstLogin(): Boolean = loginCount <= 1
    
    /**
     * Obtener días desde el primer login
     */
    fun getDaysSinceFirstLogin(): Long? {
        val first = firstLogin ?: return null
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        return (now.toEpochMilliseconds() - first.toEpochMilliseconds()) / (1000 * 60 * 60 * 24)
    }
    
    /**
     * Obtener días desde el último login
     */
    fun getDaysSinceLastLogin(): Long? {
        val last = lastLogin ?: return null
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        return (now.toEpochMilliseconds() - last.toEpochMilliseconds()) / (1000 * 60 * 60 * 24)
    }
}
