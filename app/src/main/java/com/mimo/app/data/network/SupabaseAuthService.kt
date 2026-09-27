package com.mimo.app.data.network

import android.content.Context
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

data class SupabaseUser(
    val id: String,
    val email: String,
    val fullName: String? = null
)

sealed class AuthResult {
    data class Success(val user: SupabaseUser) : AuthResult()
    data class RequiresEmailConfirmation(val email: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Handles Supabase GoTrue authentication endpoints.
 *
 * Security Requirements:
 * 1. Google Client Secret is NEVER embedded in APK.
 * 2. Supabase service-role key is NEVER embedded in APK.
 * 3. Only the public Supabase 'anon' key is used for client auth requests.
 * 4. Auth tokens are stored securely in SupabaseSessionManager and cleared on logout.
 */
object SupabaseAuthService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun initialize(context: Context) {
        SupabaseConfig.initialize(context)
        SupabaseSessionManager.initialize(context)
    }

    fun clearError() {
        errorMessage = null
    }

    /**
     * Authenticates an existing user via Supabase GoTrue Auth:
     * POST /auth/v1/token?grant_type=password
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            val err = "Email and password are required"
            withContext(Dispatchers.Main) { errorMessage = err }
            return@withContext AuthResult.Error(err)
        }

        val anonKey = SupabaseConfig.getAnonKey()
        if (anonKey.isBlank()) {
            val err = "Authentication key is not configured"
            withContext(Dispatchers.Main) { errorMessage = err }
            return@withContext AuthResult.Error(err)
        }

        withContext(Dispatchers.Main) {
            isLoading = true
            errorMessage = null
        }

        try {
            val jsonBody = JSONObject().apply {
                put("email", trimmedEmail)
                put("password", trimmedPassword)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.AUTH_URL}/token?grant_type=password")
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseText = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val parsedError = parseAuthError(responseText, response.code)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = parsedError
                }
                return@withContext AuthResult.Error(parsedError)
            }

            val json = JSONObject(responseText)
            val accessToken = json.getString("access_token")
            val refreshToken = json.optString("refresh_token")
            val expiresIn = json.optLong("expires_in", 3600)

            val userObj = json.getJSONObject("user")
            val userId = userObj.getString("id")
            val userEmail = userObj.optString("email", trimmedEmail)
            val userMeta = userObj.optJSONObject("user_metadata")
            val fullName = userMeta?.optString("full_name")

            // Persist authenticated Supabase session
            SupabaseSessionManager.setSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
                email = userEmail,
                name = fullName,
                expiresInSeconds = expiresIn
            )

            withContext(Dispatchers.Main) {
                isLoading = false
                errorMessage = null
            }

            AuthResult.Success(SupabaseUser(id = userId, email = userEmail, fullName = fullName))
        } catch (e: Exception) {
            val err = "Unable to connect. Please check your network connection."
            withContext(Dispatchers.Main) {
                isLoading = false
                errorMessage = err
            }
            AuthResult.Error(err)
        }
    }

    /**
     * Registers a new user via Supabase GoTrue Auth:
     * POST /auth/v1/signup
     */
    suspend fun signUpWithEmail(name: String, email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            val err = "Email and password are required"
            withContext(Dispatchers.Main) { errorMessage = err }
            return@withContext AuthResult.Error(err)
        }

        if (trimmedPassword.length < 6) {
            val err = "Password must be at least 6 characters"
            withContext(Dispatchers.Main) { errorMessage = err }
            return@withContext AuthResult.Error(err)
        }

        val anonKey = SupabaseConfig.getAnonKey()
        if (anonKey.isBlank()) {
            val err = "Authentication key is not configured"
            withContext(Dispatchers.Main) { errorMessage = err }
            return@withContext AuthResult.Error(err)
        }

        withContext(Dispatchers.Main) {
            isLoading = true
            errorMessage = null
        }

        try {
            val jsonBody = JSONObject().apply {
                put("email", trimmedEmail)
                put("password", trimmedPassword)
                if (trimmedName.isNotBlank()) {
                    put("data", JSONObject().apply {
                        put("full_name", trimmedName)
                    })
                }
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.AUTH_URL}/signup")
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseText = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val parsedError = parseAuthError(responseText, response.code)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = parsedError
                }
                return@withContext AuthResult.Error(parsedError)
            }

            val json = JSONObject(responseText)
            val accessToken = json.optString("access_token")
            val userObj = json.optJSONObject("user") ?: json

            val userId = userObj.optString("id")
            val userEmail = userObj.optString("email", trimmedEmail)

            withContext(Dispatchers.Main) {
                isLoading = false
                errorMessage = null
            }

            if (accessToken.isNotBlank()) {
                val refreshToken = json.optString("refresh_token")
                val expiresIn = json.optLong("expires_in", 3600)
                SupabaseSessionManager.setSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = userId,
                    email = userEmail,
                    name = trimmedName.ifBlank { null },
                    expiresInSeconds = expiresIn
                )
                AuthResult.Success(SupabaseUser(id = userId, email = userEmail, fullName = trimmedName))
            } else {
                // Supabase requires email verification
                AuthResult.RequiresEmailConfirmation(userEmail)
            }
        } catch (e: Exception) {
            val err = "Unable to connect. Please check your network connection."
            withContext(Dispatchers.Main) {
                isLoading = false
                errorMessage = err
            }
            AuthResult.Error(err)
        }
    }

    /**
     * Signs out from Supabase Auth and clears all stored session data.
     * POST /auth/v1/logout
     */
    suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        val accessToken = SupabaseSessionManager.getAccessToken()
        val anonKey = SupabaseConfig.getAnonKey()

        try {
            if (!accessToken.isNullOrBlank() && anonKey.isNotBlank()) {
                val request = Request.Builder()
                    .url("${SupabaseConfig.AUTH_URL}/logout")
                    .header("apikey", anonKey)
                    .header("Authorization", "Bearer $accessToken")
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .build()
                httpClient.newCall(request).execute().close()
            }
        } catch (_: Exception) {
            // Ignore network errors on logout
        } finally {
            withContext(Dispatchers.Main) {
                SupabaseSessionManager.clearSession()
                errorMessage = null
                isLoading = false
            }
        }
    }

    private fun parseAuthError(responseText: String, statusCode: Int): String {
        return try {
            val json = JSONObject(responseText)
            val msg = json.optString("msg").ifBlank {
                json.optString("error_description").ifBlank {
                    json.optString("message")
                }
            }

            when {
                msg.contains("Invalid login credentials", ignoreCase = true) ->
                    "Invalid email or password"
                msg.contains("Email not confirmed", ignoreCase = true) ->
                    "Please confirm your email address"
                msg.contains("already registered", ignoreCase = true) ->
                    "An account with this email already exists"
                msg.contains("Password should be at least", ignoreCase = true) ->
                    "Password must be at least 6 characters"
                msg.contains("rate limit", ignoreCase = true) ->
                    "Too many attempts. Please try again later."
                msg.isNotBlank() -> msg
                statusCode == 400 -> "Invalid credentials provided"
                statusCode == 401 -> "Invalid email or password"
                else -> "Authentication failed. Please try again."
            }
        } catch (_: Exception) {
            "Authentication failed. Please try again."
        }
    }

    /**
     * Builds the Google OAuth URL for Supabase authentication.
     */
    fun getGoogleOAuthUrl(): String {
        return "${SupabaseConfig.AUTH_URL}/authorize?provider=google&redirect_to=mimo://auth-callback"
    }

    /**
     * Handles Supabase OAuth redirect deep-links:
     * mimo://auth-callback#access_token=...&refresh_token=...
     */
    fun handleAuthCallback(uri: Uri?): Boolean {
        if (uri == null) return false
        val scheme = uri.scheme
        val host = uri.host

        if (scheme == "mimo" && host == "auth-callback") {
            val fragment = uri.fragment
            val params = if (!fragment.isNullOrBlank()) {
                fragment.split("&").associate {
                    val parts = it.split("=")
                    if (parts.size >= 2) parts[0] to parts[1] else parts[0] to ""
                }
            } else {
                emptyMap()
            }

            val accessToken = params["access_token"] ?: uri.getQueryParameter("access_token")
            val refreshToken = params["refresh_token"] ?: uri.getQueryParameter("refresh_token")
            val expiresIn = params["expires_in"]?.toLongOrNull() ?: 3600L

            if (!accessToken.isNullOrBlank()) {
                SupabaseSessionManager.setSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresInSeconds = expiresIn
                )
                errorMessage = null
                isLoading = false
                return true
            }
        }
        return false
    }
}

