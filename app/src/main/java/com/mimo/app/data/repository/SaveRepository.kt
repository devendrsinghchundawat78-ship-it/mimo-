package com.mimo.app.data.repository

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mimo.app.data.local.LocalDataCache
import com.mimo.app.data.model.ItemCategory
import com.mimo.app.data.model.SaveItem
import com.mimo.app.data.model.UserCollection
import com.mimo.app.data.network.SupabaseDataService
import com.mimo.app.data.network.SupabaseSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Central repository providing reactive single source of truth for:
 * - Cloud persistent saves & collections
 * - Fast local cache
 * - Offline sync queue
 * - Filter & search states
 */
object SaveRepository {

    private val scope = CoroutineScope(Dispatchers.Main)

    // Reactive Compose states for UI binding
    val saves = mutableStateListOf<SaveItem>()
    val collections = mutableStateListOf<UserCollection>()

    var isLoading by mutableStateOf(false)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            LocalDataCache.initialize(context)
            SupabaseSessionManager.initialize(context)
            isInitialized = true

            // Instantly load cached items for fast render
            val userId = SupabaseSessionManager.getUserId().orEmpty()
            if (userId.isNotBlank()) {
                val cachedSaves = LocalDataCache.getCachedSaves(userId)
                if (cachedSaves.isNotEmpty()) {
                    saves.clear()
                    saves.addAll(cachedSaves)
                }
                val cachedCols = LocalDataCache.getCachedCollections(userId)
                if (cachedCols.isNotEmpty()) {
                    collections.clear()
                    collections.addAll(cachedCols)
                }
            }
        }
    }

    /**
     * Loads cloud data for the current user and synchronizes local cache.
     * Flow: Login/session -> current user -> Supabase data fetch -> local cache -> UI
     */
    suspend fun loadData(forceRefresh: Boolean = false): Unit = withContext(Dispatchers.Main) {
        val userId = SupabaseSessionManager.getUserId().orEmpty()
        if (userId.isBlank()) {
            saves.clear()
            collections.clear()
            isLoading = false
            return@withContext
        }

        // If in-memory is empty, first restore from local cache
        if (saves.isEmpty()) {
            val cached = LocalDataCache.getCachedSaves(userId)
            if (cached.isNotEmpty()) {
                saves.addAll(cached)
            }
            val cachedCols = LocalDataCache.getCachedCollections(userId)
            if (cachedCols.isNotEmpty()) {
                collections.addAll(cachedCols)
            }
        }

        isLoading = true
        errorMessage = null

        withContext(Dispatchers.IO) {
            // First flush pending offline queue if online
            syncPendingQueue(userId)

            val savesResult = SupabaseDataService.fetchSaves(userId)
            val collectionsResult = SupabaseDataService.fetchCollections(userId)
            val mappingsResult = SupabaseDataService.fetchCollectionItemMappings()

            withContext(Dispatchers.Main) {
                isLoading = false

                if (savesResult.isSuccess) {
                    val cloudSaves = savesResult.getOrNull().orEmpty()
                    val mappings = mappingsResult.getOrNull().orEmpty()

                    val mappedSaves = cloudSaves.map { item ->
                        val colId = mappings[item.id]
                        if (colId != null) item.copy(collectionId = colId) else item
                    }

                    saves.clear()
                    saves.addAll(mappedSaves)
                    LocalDataCache.saveCachedSaves(userId, mappedSaves)
                    errorMessage = null
                } else {
                    val err = savesResult.exceptionOrNull()?.message ?: "Unable to sync with cloud"
                    // If we have cached items, show cache without blocking UI with full error screen
                    if (saves.isEmpty()) {
                        errorMessage = err
                    }
                }

                if (collectionsResult.isSuccess) {
                    val cloudCollections = collectionsResult.getOrNull().orEmpty()
                    collections.clear()
                    collections.addAll(cloudCollections)
                    LocalDataCache.saveCachedCollections(userId, cloudCollections)
                }
            }
        }
    }

    /**
     * Saves a new item to cloud.
     * Flow: User -> Validate -> Supabase -> Success -> UI update
     */
    suspend fun saveItem(item: SaveItem): Result<SaveItem> = withContext(Dispatchers.IO) {
        val userId = SupabaseSessionManager.getUserId().orEmpty()
        if (userId.isBlank()) {
            val err = "Please sign in to save items"
            withContext(Dispatchers.Main) { errorMessage = err }
            return@withContext Result.failure(Exception(err))
        }

        // Validation
        if (item.title.isBlank() && item.url.isBlank() && item.noteContent.isNullOrBlank()) {
            val err = "Cannot save empty item"
            return@withContext Result.failure(Exception(err))
        }

        val validId = if (item.id.isBlank() || !item.id.contains("-")) UUID.randomUUID().toString() else item.id
        val itemToSave = item.copy(id = validId, userId = userId)

        withContext(Dispatchers.Main) {
            isSyncing = true
            errorMessage = null
        }

        val cloudResult = SupabaseDataService.insertSave(itemToSave, userId)

        return@withContext withContext(Dispatchers.Main) {
            isSyncing = false
            if (cloudResult.isSuccess) {
                val persistedItem = cloudResult.getOrNull() ?: itemToSave
                saves.add(0, persistedItem)
                LocalDataCache.saveCachedSaves(userId, saves)

                // If collection was specified, link it
                if (!itemToSave.collectionId.isNullOrBlank()) {
                    scope.launch(Dispatchers.IO) {
                        SupabaseDataService.addSaveToCollection(itemToSave.collectionId, persistedItem.id)
                    }
                }
                Result.success(persistedItem)
            } else {
                // If offline or network error, save to pending queue
                val isNetworkError = cloudResult.exceptionOrNull() is java.io.IOException ||
                        cloudResult.exceptionOrNull()?.message?.contains("Unable to resolve host", ignoreCase = true) == true

                if (isNetworkError) {
                    val pendingItem = itemToSave.copy(syncPending = true)
                    LocalDataCache.addPendingSave(userId, pendingItem)
                    saves.add(0, pendingItem)
                    LocalDataCache.saveCachedSaves(userId, saves)
                    Result.success(pendingItem)
                } else {
                    val err = cloudResult.exceptionOrNull()?.message ?: "Failed to save to cloud"
                    errorMessage = err
                    Result.failure(Exception(err))
                }
            }
        }
    }

    /**
     * Updates an existing save in cloud.
     */
    suspend fun updateItem(item: SaveItem): Result<SaveItem> = withContext(Dispatchers.IO) {
        val userId = SupabaseSessionManager.getUserId().orEmpty()
        if (userId.isBlank()) return@withContext Result.failure(Exception("Not authenticated"))

        val result = SupabaseDataService.updateSave(item, userId)

        return@withContext withContext(Dispatchers.Main) {
            if (result.isSuccess) {
                val updated = result.getOrNull() ?: item
                val index = saves.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    saves[index] = updated
                }
                LocalDataCache.saveCachedSaves(userId, saves)
                Result.success(updated)
            } else {
                // Optimistic local update with pending flag if offline
                val index = saves.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    val pending = item.copy(syncPending = true)
                    saves[index] = pending
                    LocalDataCache.addPendingSave(userId, pending)
                    LocalDataCache.saveCachedSaves(userId, saves)
                }
                Result.failure(result.exceptionOrNull() ?: Exception("Update failed"))
            }
        }
    }

    /**
     * Deletes an item from cloud and local state.
     */
    suspend fun deleteItem(item: SaveItem): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = SupabaseSessionManager.getUserId().orEmpty()
        if (userId.isBlank()) return@withContext Result.failure(Exception("Not authenticated"))

        val result = SupabaseDataService.deleteSave(item.id, userId)

        return@withContext withContext(Dispatchers.Main) {
            saves.removeAll { it.id == item.id }
            LocalDataCache.removePendingSave(userId, item.id)
            LocalDataCache.saveCachedSaves(userId, saves)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Delete failed"))
            }
        }
    }

    /**
     * Toggles favorite status in cloud and updates local state.
     */
    suspend fun toggleFavorite(item: SaveItem): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = SupabaseSessionManager.getUserId().orEmpty()
        if (userId.isBlank()) return@withContext Result.failure(Exception("Not authenticated"))

        val newFav = !item.isFavorite
        withContext(Dispatchers.Main) {
            val index = saves.indexOfFirst { it.id == item.id }
            if (index != -1) {
                saves[index] = saves[index].copy(isFavorite = newFav)
            }
        }

        val result = SupabaseDataService.setFavorite(item.id, newFav, userId)
        if (result.isSuccess) {
            withContext(Dispatchers.Main) {
                LocalDataCache.saveCachedSaves(userId, saves)
            }
        }
        return@withContext result
    }

    /**
     * Creates a new cloud collection.
     */
    suspend fun createCollection(
        name: String,
        description: String = "",
        color: String = "#FF6B6B",
        icon: String = "folder"
    ): Result<UserCollection> = withContext(Dispatchers.IO) {
        val userId = SupabaseSessionManager.getUserId().orEmpty()
        if (userId.isBlank()) return@withContext Result.failure(Exception("Not authenticated"))

        val newCol = UserCollection(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name.ifBlank { "New Collection" },
            description = description,
            color = color,
            icon = icon
        )

        val result = SupabaseDataService.insertCollection(newCol, userId)
        return@withContext withContext(Dispatchers.Main) {
            if (result.isSuccess) {
                val created = result.getOrNull() ?: newCol
                collections.add(0, created)
                LocalDataCache.saveCachedCollections(userId, collections)
                Result.success(created)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to create collection"))
            }
        }
    }

    /**
     * Deletes a collection.
     */
    suspend fun deleteCollection(collectionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = SupabaseSessionManager.getUserId().orEmpty()
        if (userId.isBlank()) return@withContext Result.failure(Exception("Not authenticated"))

        val result = SupabaseDataService.deleteCollection(collectionId, userId)
        return@withContext withContext(Dispatchers.Main) {
            collections.removeAll { it.id == collectionId }
            LocalDataCache.saveCachedCollections(userId, collections)
            result
        }
    }

    /**
     * Adds an existing save to a collection.
     */
    suspend fun addItemToCollection(collectionId: String, saveId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val result = SupabaseDataService.addSaveToCollection(collectionId, saveId)
        withContext(Dispatchers.Main) {
            val index = saves.indexOfFirst { it.id == saveId }
            if (index != -1) {
                saves[index] = saves[index].copy(collectionId = collectionId)
                val userId = SupabaseSessionManager.getUserId().orEmpty()
                LocalDataCache.saveCachedSaves(userId, saves)
            }
        }
        return@withContext result
    }

    /**
     * Removes a save from a collection.
     */
    suspend fun removeItemFromCollection(collectionId: String, saveId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val result = SupabaseDataService.removeSaveFromCollection(collectionId, saveId)
        withContext(Dispatchers.Main) {
            val index = saves.indexOfFirst { it.id == saveId }
            if (index != -1) {
                saves[index] = saves[index].copy(collectionId = null)
                val userId = SupabaseSessionManager.getUserId().orEmpty()
                LocalDataCache.saveCachedSaves(userId, saves)
            }
        }
        return@withContext result
    }

    /**
     * Flushes offline queued saves to Supabase when network is back.
     */
    private suspend fun syncPendingQueue(userId: String) {
        val pending = LocalDataCache.getPendingSaves(userId)
        if (pending.isEmpty()) return

        for (item in pending) {
            val res = SupabaseDataService.insertSave(item, userId)
            if (res.isSuccess) {
                LocalDataCache.removePendingSave(userId, item.id)
            }
        }
    }

    /**
     * Clears all in-memory user data (used on logout).
     */
    fun clearUserData() {
        val userId = SupabaseSessionManager.getUserId().orEmpty()
        if (userId.isNotBlank()) {
            LocalDataCache.clearUserCache(userId)
        }
        saves.clear()
        collections.clear()
        isLoading = false
        isSyncing = false
        errorMessage = null
    }
}
