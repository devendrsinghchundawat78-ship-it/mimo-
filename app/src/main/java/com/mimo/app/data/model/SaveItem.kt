package com.mimo.app.data.model

enum class ItemCategory {
    URL,
    NOTE,
    PHOTO,
    COLLECTION,
    REVIEW,
    PRODUCT,
    FILM,
    SOFTWARE,
    TV_SHOW,
    TUTORIAL,
    PLACE,
    RECIPE,
    BOOK,
    MUSIC
}

data class SaveItem(
    val id: String,
    val userId: String = "",
    val title: String,
    val subtitle: String = "",
    val url: String = "",
    val category: ItemCategory = ItemCategory.URL,
    val sourcePlatform: String = "Web", // e.g. Instagram, TikTok, Web, Note, Media
    val dateAdded: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val noteContent: String? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val collectionId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncPending: Boolean = false
)

data class UserCollection(
    val id: String,
    val userId: String = "",
    val name: String,
    val description: String = "",
    val color: String = "#FF6B6B",
    val icon: String = "folder",
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
