package com.mimo.app.data.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


/**
 * Handles Google Drive OAuth integration via Supabase Edge Functions.
 *
 * Security Requirements:
 * 1. Google Client Secret is NEVER embedded in APK.
 * 2. Supabase service-role key is NEVER embedded in APK.
 * 3. Google refresh/access tokens are NOT stored in Android app.
 * 4. OAuth is orchestrated exclusively through Supabase Edge Functions.
 */
object GoogleDriveService {

    private const val SUPABASE_PROJECT_ID = "ugomjglbzcjekibnupin"
    private const val EDGE_FUNCTION_START_URL =
        "https://$SUPABASE_PROJECT_ID.supabase.co/functions/v1/google-drive-oauth-start"

    // Reactive Compose state for UI
    var isConnected by mutableStateOf(false)
        private set

    var connectedEmail by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var lastActionSuccessMessage by mutableStateOf<String?>(null)
        private set

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun initialize(context: Context) {
        isConnected = false
        connectedEmail = null
        SupabaseSessionManager.initialize(context)
    }

    /**
     * Starts the Google Drive OAuth flow by calling the Supabase Edge Function
     * and opening the returned authorization_url in a secure Chrome Custom Tab.
     */
    suspend fun startConnectFlow(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        isLoading = true
        errorMessage = null
        lastActionSuccessMessage = null

        try {
            val sessionToken = SupabaseSessionManager.getAccessToken()

            val requestBuilder = Request.Builder()
                .url(EDGE_FUNCTION_START_URL)
                .post("{}".toRequestBody("application/json".toMediaType()))

            if (!sessionToken.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $sessionToken")
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                // If 401 Unauthorized without session, provide human-friendly state
                val errorMsg = if (response.code == 401) {
                    "Sign in required to link storage"
                } else {
                    "Unable to start connection"
                }
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = errorMsg
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val json = JSONObject(responseBody)
            val authUrl = json.optString("authorization_url").ifBlank {
                json.optString("url")
            }

            if (authUrl.isBlank()) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = "Invalid authorization response"
                }
                return@withContext Result.failure(Exception("No authorization URL returned"))
            }

            // Launch secure Custom Tab or Browser
            withContext(Dispatchers.Main) {
                openCustomTabOrBrowser(context, authUrl)
                isLoading = false
            }

            Result.success(Unit)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                isLoading = false
                errorMessage = "Connection error"
            }
            Result.failure(e)
        }
    }

    /**
     * Handles deep link callback from Supabase Edge Function:
     * mimo://google-drive-callback?status=success&email=user@gmail.com
     */
    fun handleAuthCallback(uri: Uri?): Boolean {
        if (uri == null) return false

        val scheme = uri.scheme
        val host = uri.host

        if (scheme == "mimo" && host == "google-drive-callback") {
            val status = uri.getQueryParameter("status")
            val email = uri.getQueryParameter("email")
            val error = uri.getQueryParameter("error")

            if (status == "success" && error.isNullOrBlank() && !email.isNullOrBlank()) {
                // The custom-scheme callback can be invoked by another app; it is not proof
                // that a Google token exists server-side. Wait for a status check API.
                isConnected = false
                connectedEmail = null
                lastActionSuccessMessage = "Google returned to Mimo; connection not verified"
                errorMessage = null
            } else {
                isConnected = false
                connectedEmail = null
                errorMessage = "Drive connection could not be verified"
            }
            isLoading = false
            return true
        }

        return false
    }

    // Local state is not a server token. Do not offer a fake Disconnect button here;
    // a real disconnect must revoke the server-side Google grant and confirm the result.

    fun clearMessages() {
        errorMessage = null
        lastActionSuccessMessage = null
    }

    private fun openCustomTabOrBrowser(context: Context, url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            val bundle = android.os.Bundle()
            bundle.putBinder("android.support.customtabs.extra.SESSION", null)
            bundle.putBinder("androidx.browser.customtabs.extra.SESSION", null)
            putExtras(bundle)
            putExtra("android.support.customtabs.extra.TOOLBAR_COLOR", 0xFFFFFFFF.toInt())
            putExtra("androidx.browser.customtabs.extra.TOOLBAR_COLOR", 0xFFFFFFFF.toInt())
            putExtra("android.support.customtabs.extra.TITLE_VISIBILITY", 1)
            putExtra("android.support.customtabs.extra.ENABLE_URLBAR_HIDING", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }
}
