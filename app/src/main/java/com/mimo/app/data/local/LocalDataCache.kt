package com.mimo.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.mimo.app.data.model.ItemCategory
import com.mimo.app.data.model.SaveItem
import com.mimo.app.data.model.UserCollection
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local cache manager for offline storage, instant startup loading,
 * and pending offline sync queue.
 *
 * Isolated per authenticated user.id.
 */
object LocalDataCache {
    private const val PREFS_NAME = "mimo_local_data_cache"
    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // ==================== SAVES CACHE ====================

    fun getCachedSaves(userId: String): List<SaveItem> {
        if (userId.isBlank()) return emptyList()
        val rawJson = prefs?.getString("saves_$userId", null) ?: return emptyList()
        return parseSavesJson(rawJson)
    }

    fun saveCachedSaves(userId: String, items: List<SaveItem>) {
        if (userId.isBlank()) return
        val jsonArray = JSONArray()
        for (item in items) {
            jsonArray.put(saveItemToJson(item))
        }
        prefs?.edit()?.putString("saves_$userId", jsonArray.toString())?.apply()
    }

    // ==================== COLLECTIONS CACHE ====================

    fun getCachedCollections(userId: String): List<UserCollection> {
        if (userId.isBlank()) return emptyList()
        val rawJson = prefs?.getString("collections_$userId", null) ?: return emptyList()
        return parseCollectionsJson(rawJson)
    }

    fun saveCachedCollections(userId: String, collections: List<UserCollection>) {
        if (userId.isBlank()) return
        val jsonArray = JSONArray()
        for (col in collections) {
            jsonArray.put(collectionToJson(col))
        }
        prefs?.edit()?.putString("collections_$userId", jsonArray.toString())?.apply()
    }

    // ==================== PENDING OFFLINE QUEUE ====================

    fun getPendingSaves(userId: String): List<SaveItem> {
        if (userId.isBlank()) return emptyList()
        val rawJson = prefs?.getString("pending_saves_$userId", null) ?: return emptyList()
        return parseSavesJson(rawJson)
    }

    fun addPendingSave(userId: String, item: SaveItem) {
        if (userId.isBlank()) return
        val current = getPendingSaves(userId).toMutableList()
        current.removeAll { it.id == item.id }
        current.add(0, item.copy(syncPending = true))
        val jsonArray = JSONArray()
        for (i in current) {
            jsonArray.put(saveItemToJson(i))
        }
        prefs?.edit()?.putString("pending_saves_$userId", jsonArray.toString())?.apply()
    }

    fun removePendingSave(userId: String, itemId: String) {
        if (userId.isBlank()) return
        val current = getPendingSaves(userId).toMutableList()
        current.removeAll { it.id == itemId }
        val jsonArray = JSONArray()
        for (i in current) {
            jsonArray.put(saveItemToJson(i))
        }
        prefs?.edit()?.putString("pending_saves_$userId", jsonArray.toString())?.apply()
    }

    fun clearUserCache(userId: String) {
        if (userId.isBlank()) return
        prefs?.edit()?.apply {
            remove("saves_$userId")
            remove("collections_$userId")
            remove("pending_saves_$userId")
            apply()
        }
    }

    // ==================== JSON SERIALIZATION ====================

    fun saveItemToJson(item: SaveItem): JSONObject {
        return JSONObject().apply {
            put("id", item.id)
            put("userId", item.userId)
            put("title", item.title)
            put("subtitle", item.subtitle)
            put("url", item.url)
            put("category", item.category.name)
            put("sourcePlatform", item.sourcePlatform)
            put("dateAdded", item.dateAdded)
            if (item.imageUrl != null) put("imageUrl", item.imageUrl)
            if (item.videoUrl != null) put("videoUrl", item.videoUrl)
            if (item.noteContent != null) put("noteContent", item.noteContent)
            put("isFavorite", item.isFavorite)
            put("isArchived", item.isArchived)
            if (item.collectionId != null) put("collectionId", item.collectionId)
            put("createdAt", item.createdAt)
            put("syncPending", item.syncPending)
        }
    }

    fun jsonToSaveItem(json: JSONObject): SaveItem {
        val catStr = json.optString("category", ItemCategory.URL.name)
        val cat = try {
            ItemCategory.valueOf(catStr)
        } catch (_: Exception) {
            ItemCategory.URL
        }

        return SaveItem(
            id = json.optString("id"),
            userId = json.optString("userId"),
            title = json.optString("title"),
            subtitle = json.optString("subtitle"),
            url = json.optString("url"),
            category = cat,
            sourcePlatform = json.optString("sourcePlatform", "Web"),
            dateAdded = json.optString("dateAdded", "Just now"),
            imageUrl = if (json.has("imageUrl")) json.optString("imageUrl") else null,
            videoUrl = if (json.has("videoUrl")) json.optString("videoUrl") else null,
            noteContent = if (json.has("noteContent")) json.optString("noteContent") else null,
            isFavorite = json.optBoolean("isFavorite", false),
            isArchived = json.optBoolean("isArchived", false),
            collectionId = if (json.has("collectionId")) json.optString("collectionId") else null,
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            syncPending = json.optBoolean("syncPending", false)
        )
    }

    private fun collectionToJson(col: UserCollection): JSONObject {
        return JSONObject().apply {
            put("id", col.id)
            put("userId", col.userId)
            put("name", col.name)
            put("description", col.description)
            put("color", col.color)
            put("icon", col.icon)
            put("isArchived", col.isArchived)
            put("sortOrder", col.sortOrder)
            put("createdAt", col.createdAt)
        }
    }

    private fun jsonToCollection(json: JSONObject): UserCollection {
        return UserCollection(
            id = json.optString("id"),
            userId = json.optString("userId"),
            name = json.optString("name"),
            description = json.optString("description"),
            color = json.optString("color", "#FF6B6B"),
            icon = json.optString("icon", "folder"),
            isArchived = json.optBoolean("isArchived", false),
            sortOrder = json.optInt("sortOrder", 0),
            createdAt = json.optLong("createdAt", System.currentTimeMillis())
        )
    }

    private fun parseSavesJson(raw: String): List<SaveItem> {
        val list = mutableListOf<SaveItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(jsonToSaveItem(obj))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseCollectionsJson(raw: String): List<UserCollection> {
        val list = mutableListOf<UserCollection>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(jsonToCollection(obj))
            }
        } catch (_: Exception) {}
        return list
    }
}
