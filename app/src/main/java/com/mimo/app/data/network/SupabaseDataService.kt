package com.mimo.app.data.network

import com.mimo.app.data.model.ItemCategory
import com.mimo.app.data.model.SaveItem
import com.mimo.app.data.model.UserCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Handles real cloud CRUD operations to Supabase PostgREST endpoints.
 *
 * Security:
 * - Uses publishable/anon public key + authenticated user Bearer JWT.
 * - Enforces Row Level Security (RLS) on user_id.
 * - No service-role key or secrets are embedded.
 */
object SupabaseDataService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private const val REST_URL = "${SupabaseConfig.BASE_URL}/rest/v1"

    private val refreshMutex = Mutex()

    /** Refresh ahead of expiry, serializing callers because Supabase rotates refresh tokens. */
    private suspend fun getAuthHeaders(): Map<String, String>? {
        val token = refreshMutex.withLock {
            val current = SupabaseSessionManager.getAccessToken() ?: return@withLock null
            if (!SupabaseSessionManager.tokenExpiresWithin(60_000)) return@withLock current
            val refresh = SupabaseSessionManager.getRefreshToken()
            if (refresh.isNullOrBlank()) return@withLock null
            try {
                val payload = JSONObject().put("refresh_token", refresh).toString()
                val request = Request.Builder()
                    .url("${SupabaseConfig.AUTH_URL}/token?grant_type=refresh_token")
                    .header("apikey", SupabaseConfig.getAnonKey())
                    .header("Content-Type", "application/json")
                    .post(payload.toRequestBody("application/json".toMediaType()))
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        // Only a rejected refresh token calls for re-auth. Network/server trouble
                        // must not silently log out the user or erase the in-progress save.
                        // Keep the current editor and its unsaved content visible. The caller
                        // will surface a sign-in prompt rather than redirecting mid-save.
                        return@withLock null
                    }
                    val json = JSONObject(body)
                    val access = json.getString("access_token")
                    val replacement = json.optString("refresh_token").ifBlank { refresh }
                    SupabaseSessionManager.setSession(
                        accessToken = access,
                        refreshToken = replacement,
                        expiresInSeconds = json.optLong("expires_in", 3600).coerceAtLeast(1)
                    )
                    access
                }
            } catch (_: Exception) {
                null
            }
        } ?: return null
        return mapOf(
            "apikey" to SupabaseConfig.getAnonKey(),
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json"
        )
    }

    // ==================== SAVES CRUD ====================

    /**
     * Fetches all cloud saves belonging to the authenticated user.
     * GET /rest/v1/saves?user_id=eq.{userId}&order=created_at.desc
     */
    suspend fun fetchSaves(userId: String): Result<List<SaveItem>> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))
        if (userId.isBlank()) return@withContext Result.failure(Exception("Invalid user ID"))

        try {
            val url = "$REST_URL/saves?user_id=eq.$userId&order=created_at.desc"
            val requestBuilder = Request.Builder().url(url).get()
            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to fetch saves: HTTP ${response.code}"))
            }

            val list = mutableListOf<SaveItem>()
            val jsonArray = JSONArray(body)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(parseSaveItem(obj))
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inserts a new save record to Supabase.
     * POST /rest/v1/saves
     */
    suspend fun insertSave(item: SaveItem, userId: String): Result<SaveItem> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))
        if (userId.isBlank()) return@withContext Result.failure(Exception("Invalid user ID"))

        try {
            val validId = if (isValidUuid(item.id)) item.id else UUID.randomUUID().toString()
            val jsonPayload = JSONObject().apply {
                put("id", validId)
                put("user_id", userId)
                put("title", item.title.ifBlank { "Untitled" })
                put("description", item.subtitle)
                put("content", item.noteContent ?: "")
                put("url", item.url)
                put("source_platform", item.sourcePlatform)
                put("thumbnail_url", item.imageUrl ?: item.videoUrl ?: "")
                put("type", item.category.name)
                put("is_favorite", item.isFavorite)
                put("is_archived", item.isArchived)
                put("status", "active")
            }

            val requestBuilder = Request.Builder()
                .url("$REST_URL/saves")
                .header("Prefer", "return=representation")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to save: HTTP ${response.code} - $body"))
            }

            val returnedArray = JSONArray(body)
            if (returnedArray.length() > 0) {
                val createdItem = parseSaveItem(returnedArray.getJSONObject(0))
                Result.success(createdItem)
            } else {
                Result.success(item.copy(id = validId, userId = userId, syncPending = false))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing save in Supabase.
     * PATCH /rest/v1/saves?id=eq.{id}&user_id=eq.{userId}
     */
    suspend fun updateSave(item: SaveItem, userId: String): Result<SaveItem> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))
        if (userId.isBlank()) return@withContext Result.failure(Exception("Invalid user ID"))

        try {
            val jsonPayload = JSONObject().apply {
                put("title", item.title)
                put("description", item.subtitle)
                put("content", item.noteContent ?: "")
                put("url", item.url)
                put("source_platform", item.sourcePlatform)
                if (item.imageUrl != null || item.videoUrl != null) {
                    put("thumbnail_url", item.imageUrl ?: item.videoUrl ?: "")
                }
                put("type", item.category.name)
                put("is_favorite", item.isFavorite)
                put("is_archived", item.isArchived)
            }

            val requestBuilder = Request.Builder()
                .url("$REST_URL/saves?id=eq.${item.id}&user_id=eq.$userId")
                .header("Prefer", "return=representation")
                .patch(jsonPayload.toString().toRequestBody("application/json".toMediaType()))

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to update save: HTTP ${response.code}"))
            }

            val returnedArray = JSONArray(body)
            if (returnedArray.length() > 0) {
                Result.success(parseSaveItem(returnedArray.getJSONObject(0)))
            } else {
                Result.success(item)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a save in Supabase.
     * DELETE /rest/v1/saves?id=eq.{id}&user_id=eq.{userId}
     */
    suspend fun deleteSave(saveId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))
        if (userId.isBlank() || saveId.isBlank()) return@withContext Result.failure(Exception("Invalid parameters"))

        try {
            val requestBuilder = Request.Builder()
                .url("$REST_URL/saves?id=eq.$saveId&user_id=eq.$userId&select=id")
                .header("Prefer", "return=representation")
                .delete()

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to delete save: HTTP ${response.code}"))
            }
            // A 204 alone is not proof: RLS/filter mismatches can delete zero rows.
            val rows = JSONArray(body)
            if (rows.length() != 1 || rows.getJSONObject(0).optString("id") != saveId) {
                return@withContext Result.failure(Exception("Save was not removed from server"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggles favorite status in Supabase.
     * PATCH /rest/v1/saves?id=eq.{id}&user_id=eq.{userId}
     */
    suspend fun setFavorite(saveId: String, isFavorite: Boolean, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))
        if (userId.isBlank() || saveId.isBlank()) return@withContext Result.failure(Exception("Invalid parameters"))

        try {
            val jsonPayload = JSONObject().apply {
                put("is_favorite", isFavorite)
            }

            val requestBuilder = Request.Builder()
                .url("$REST_URL/saves?id=eq.$saveId&user_id=eq.$userId")
                .patch(jsonPayload.toString().toRequestBody("application/json".toMediaType()))

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to toggle favorite: HTTP ${response.code}"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== COLLECTIONS CRUD ====================

    suspend fun fetchCollections(userId: String): Result<List<UserCollection>> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))
        if (userId.isBlank()) return@withContext Result.failure(Exception("Invalid user ID"))

        try {
            val url = "$REST_URL/collections?user_id=eq.$userId&order=created_at.desc"
            val requestBuilder = Request.Builder().url(url).get()
            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to fetch collections: HTTP ${response.code}"))
            }

            val list = mutableListOf<UserCollection>()
            val jsonArray = JSONArray(body)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(parseCollection(obj))
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertCollection(col: UserCollection, userId: String): Result<UserCollection> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))
        if (userId.isBlank()) return@withContext Result.failure(Exception("Invalid user ID"))

        try {
            val validId = if (isValidUuid(col.id)) col.id else UUID.randomUUID().toString()
            val jsonPayload = JSONObject().apply {
                put("id", validId)
                put("user_id", userId)
                put("name", col.name.ifBlank { "Untitled Collection" })
                put("description", col.description)
                put("color", col.color)
                put("icon", col.icon)
                put("is_archived", col.isArchived)
            }

            val requestBuilder = Request.Builder()
                .url("$REST_URL/collections")
                .header("Prefer", "return=representation")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to create collection: HTTP ${response.code}"))
            }

            val returnedArray = JSONArray(body)
            if (returnedArray.length() > 0) {
                Result.success(parseCollection(returnedArray.getJSONObject(0)))
            } else {
                Result.success(col.copy(id = validId, userId = userId))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCollection(collectionId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))
        if (userId.isBlank() || collectionId.isBlank()) return@withContext Result.failure(Exception("Invalid parameters"))

        try {
            val requestBuilder = Request.Builder()
                .url("$REST_URL/collections?id=eq.$collectionId&user_id=eq.$userId")
                .delete()

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to delete collection: HTTP ${response.code}"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== COLLECTION ITEMS (JOIN TABLE) ====================

    suspend fun fetchCollectionItemMappings(): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))

        try {
            val requestBuilder = Request.Builder()
                .url("$REST_URL/collection_items?select=*")
                .get()

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to fetch collection mappings"))
            }

            val map = mutableMapOf<String, String>() // save_id -> collection_id
            val jsonArray = JSONArray(body)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val cId = obj.optString("collection_id")
                val sId = obj.optString("save_id")
                if (cId.isNotBlank() && sId.isNotBlank()) {
                    map[sId] = cId
                }
            }
            Result.success(map)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSaveToCollection(collectionId: String, saveId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))

        try {
            val jsonPayload = JSONObject().apply {
                put("collection_id", collectionId)
                put("save_id", saveId)
            }

            val requestBuilder = Request.Builder()
                .url("$REST_URL/collection_items")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful && response.code != 409) { // 409 conflict = already added
                return@withContext Result.failure(Exception("Failed to link item to collection"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeSaveFromCollection(collectionId: String, saveId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val headers = getAuthHeaders() ?: return@withContext Result.failure(Exception("Session expired or unavailable. Please sign in again before retrying; your unsaved item is still here."))

        try {
            val requestBuilder = Request.Builder()
                .url("$REST_URL/collection_items?collection_id=eq.$collectionId&save_id=eq.$saveId")
                .delete()

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to remove item from collection"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== PARSING HELPERS ====================

    private fun parseSaveItem(obj: JSONObject): SaveItem {
        val typeStr = obj.optString("type", ItemCategory.URL.name)
        val cat = try {
            ItemCategory.valueOf(typeStr)
        } catch (_: Exception) {
            ItemCategory.URL
        }

        val rawDate = obj.optString("created_at")
        val formattedDate = formatIsoDate(rawDate)
        val thumb = obj.optString("thumbnail_url").takeIf { it.isNotBlank() }
        val isVideo = cat == ItemCategory.TV_SHOW || cat == ItemCategory.FILM ||
                obj.optString("url").contains("youtube", ignoreCase = true) ||
                obj.optString("url").contains("tiktok", ignoreCase = true)

        return SaveItem(
            id = obj.optString("id"),
            userId = obj.optString("user_id"),
            title = obj.optString("title"),
            subtitle = obj.optString("description"),
            url = obj.optString("url"),
            category = cat,
            sourcePlatform = obj.optString("source_platform", "Web"),
            dateAdded = formattedDate,
            imageUrl = thumb,
            videoUrl = if (isVideo) obj.optString("url").takeIf { it.isNotBlank() } else null,
            noteContent = obj.optString("content").takeIf { it.isNotBlank() },
            isFavorite = obj.optBoolean("is_favorite", false),
            isArchived = obj.optBoolean("is_archived", false),
            collectionId = null,
            createdAt = parseIsoTimestamp(rawDate),
            syncPending = false
        )
    }

    private fun parseCollection(obj: JSONObject): UserCollection {
        return UserCollection(
            id = obj.optString("id"),
            userId = obj.optString("user_id"),
            name = obj.optString("name"),
            description = obj.optString("description"),
            color = obj.optString("color", "#FF6B6B"),
            icon = obj.optString("icon", "folder"),
            isArchived = obj.optBoolean("is_archived", false),
            sortOrder = obj.optInt("order", obj.optInt("sort_order", 0)),
            createdAt = parseIsoTimestamp(obj.optString("created_at"))
        )
    }

    private fun formatIsoDate(isoString: String): String {
        if (isoString.isBlank()) return "Recently"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = parser.parse(isoString.substringBefore(".")) ?: Date()
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            formatter.format(date)
        } catch (_: Exception) {
            "Recently"
        }
    }

    private fun parseIsoTimestamp(isoString: String): Long {
        if (isoString.isBlank()) return System.currentTimeMillis()
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            parser.parse(isoString.substringBefore("."))?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun isValidUuid(str: String): Boolean {
        return try {
            UUID.fromString(str)
            true
        } catch (_: Exception) {
            false
        }
    }
}
