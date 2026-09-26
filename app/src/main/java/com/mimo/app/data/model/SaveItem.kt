package com.mimo.app.data.model

data class SaveItem(
    val id: String,
    val title: String,
    val url: String,
    val type: String = "Link",
    val dateAdded: String,
    val imageUrl: String? = null
)
