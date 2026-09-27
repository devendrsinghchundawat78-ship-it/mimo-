package com.mimo.app.data.network

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Manages persistent Supabase authentication session tokens.
 * Security: Service-role keys and third-party secrets are strictly NEVER stored here.
 */
object SupabaseSessionManager {
    private const val PREFS_NAME = "mimo_supabase_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_TOKEN_EXPIRY = "token_expiry"

    private var prefs: SharedPreferences? = null

    // Reactive Compose state for real-time UI synchronization
    var isAuthenticatedState by mutableStateOf(false)
        private set

    var currentUserId by mutableStateOf<String?>(null)
        private set

    var currentUserEmail by mutableStateOf<String?>(null)
        private set

    var currentUserName by mutableStateOf<String?>(null)
        private set

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            syncState()
        }
    }

    private fun syncState() {
        val token = prefs?.getString(KEY_ACCESS_TOKEN, null)
        isAuthenticatedState = !token.isNullOrBlank()
        currentUserId = prefs?.getString(KEY_USER_ID, null)
        currentUserEmail = prefs?.getString(KEY_USER_EMAIL, null)
        currentUserName = prefs?.getString(KEY_USER_NAME, null)
    }

    fun getAccessToken(): String? = prefs?.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs?.getString(KEY_REFRESH_TOKEN, null)
    fun getUserId(): String? = prefs?.getString(KEY_USER_ID, null)
    fun getUserEmail(): String? = prefs?.getString(KEY_USER_EMAIL, null)
    fun getUserName(): String? = prefs?.getString(KEY_USER_NAME, null)

    fun setSession(
        accessToken: String,
        refreshToken: String? = null,
        userId: String? = null,
        email: String? = null,
        name: String? = null,
        expiresInSeconds: Long = 3600
    ) {
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000)
        prefs?.edit()?.apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            if (refreshToken != null) putString(KEY_REFRESH_TOKEN, refreshToken)
            if (userId != null) putString(KEY_USER_ID, userId)
            if (email != null) putString(KEY_USER_EMAIL, email)
            if (name != null) putString(KEY_USER_NAME, name)
            putLong(KEY_TOKEN_EXPIRY, expiryTime)
            apply()
        }
        syncState()
    }

    fun clearSession() {
        prefs?.edit()?.clear()?.apply()
        syncState()
    }

    fun isAuthenticated(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }
}
