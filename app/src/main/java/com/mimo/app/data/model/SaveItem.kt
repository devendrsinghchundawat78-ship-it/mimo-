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
    TUTORIAL
}

data class SaveItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val url: String = "",
    val category: ItemCategory = ItemCategory.URL,
    val sourcePlatform: String = "Web", // e.g. Instagram, TikTok, Web, Note, Media
    val dateAdded: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val noteContent: String? = null,
    val isFavorite: Boolean = false
)
