package com.mimo.app.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages general app-level user preferences and first-launch/onboarding state.
 */
object AppPreferences {
    private const val PREFS_NAME = "mimo_app_preferences"
    private const val KEY_HAS_SEEN_WELCOME = "has_seen_welcome"
    private const val KEY_DARK_THEME = "dark_theme"

    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Checks if the user has completed the welcome/continue screen.
     * Once true, the welcome screen is never shown again on restart.
     */
    fun hasSeenWelcome(): Boolean {
        return prefs?.getBoolean(KEY_HAS_SEEN_WELCOME, false) ?: false
    }

    fun isDarkTheme(): Boolean = prefs?.getBoolean(KEY_DARK_THEME, true) ?: true

    fun setDarkTheme(isDark: Boolean) {
        prefs?.edit()?.putBoolean(KEY_DARK_THEME, isDark)?.apply()
    }

    fun setHasSeenWelcome(seen: Boolean = true) {
        prefs?.edit()?.putBoolean(KEY_HAS_SEEN_WELCOME, seen)?.apply()
    }
}
