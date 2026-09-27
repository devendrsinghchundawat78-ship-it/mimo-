package com.mimo.app.data.network

import android.content.Context
import android.content.SharedPreferences
import com.mimo.app.BuildConfig

/**
 * Supabase configuration constants and active client credentials.
 *
 * Security Requirements:
 * 1. Google Client Secret is strictly NEVER stored or embedded here.
 * 2. Supabase service-role key is strictly NEVER stored or embedded here.
 * 3. The publishable key is the public client key designed for mobile apps to connect
 *    to Supabase GoTrue Auth and Row-Level Security (RLS).
 */
object SupabaseConfig {
    const val PROJECT_ID = "ugomjglbzcjekibnupin"
    const val BASE_URL = "https://$PROJECT_ID.supabase.co"
    const val AUTH_URL = "$BASE_URL/auth/v1"
    const val GOOGLE_DRIVE_START_URL = "$BASE_URL/functions/v1/google-drive-oauth-start"

    // Verified Mino Supabase publishable key
    const val DEFAULT_PUBLISHABLE_KEY = "sb_publishable_kaZZeuEpwHELHVSCbO4Gww_MX30CV-A"

    private const val PREFS_NAME = "mimo_supabase_config"
    private const val KEY_CUSTOM_ANON_KEY = "custom_anon_key"

    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Retrieves the Supabase publishable / anon public key.
     * Order of resolution:
     * 1. Runtime preference override (if configured)
     * 2. BuildConfig.SUPABASE_ANON_KEY (from local.properties / gradle.properties)
     * 3. Verified DEFAULT_PUBLISHABLE_KEY
     */
    fun getAnonKey(): String {
        val stored = prefs?.getString(KEY_CUSTOM_ANON_KEY, null)
        if (!stored.isNullOrBlank()) return stored
        if (BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) return BuildConfig.SUPABASE_ANON_KEY
        return DEFAULT_PUBLISHABLE_KEY
    }

    fun setAnonKey(key: String) {
        prefs?.edit()?.putString(KEY_CUSTOM_ANON_KEY, key.trim())?.apply()
    }
}
