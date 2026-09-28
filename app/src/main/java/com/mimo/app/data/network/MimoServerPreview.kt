package com.mimo.app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Optional server preview for public YouTube/Vimeo links only. Other sources stay on device. */
object MimoServerPreview {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .build()

    suspend fun fetch(url: String): ParsedMetadata? = withContext(Dispatchers.IO) {
        val bearer = SupabaseSessionManager.getAccessToken() ?: return@withContext null
        val host = runCatching { java.net.URI(url).host?.lowercase() }.getOrNull()
        if (host !in setOf("youtu.be", "youtube.com", "www.youtube.com", "m.youtube.com", "vimeo.com", "www.vimeo.com")) return@withContext null
        return@withContext try {
            val req = Request.Builder()
                .url("${SupabaseConfig.BASE_URL}/functions/v1/mimo-link-details")
                .header("apikey", SupabaseConfig.getAnonKey())
                .header("Authorization", "Bearer $bearer")
                .post(JSONObject().put("url", url).toString().toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(req).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string()?.let(::JSONObject) ?: return@withContext null
                val canonical = body.optString("canonicalUrl")
                val title = body.optString("title")
                if (!canonical.startsWith("https://") || title.isBlank()) return@withContext null
                ParsedMetadata(
                    title = title,
                    description = body.optString("description"),
                    imageUrl = body.optString("imageUrl").takeIf { it.startsWith("https://") },
                    platform = body.optString("platform", "Web"),
                    canonicalUrl = canonical
                )
            }
        } catch (_: Exception) { null }
    }
}
