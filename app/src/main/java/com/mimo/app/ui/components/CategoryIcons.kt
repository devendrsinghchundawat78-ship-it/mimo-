package com.mimo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mimo.app.data.model.ItemCategory
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppWhite

fun getCategoryIcon(category: ItemCategory): ImageVector {
    return when (category) {
        ItemCategory.URL -> Icons.Default.Link
        ItemCategory.NOTE -> Icons.Default.Description
        ItemCategory.PHOTO -> Icons.Default.CameraAlt
        ItemCategory.COLLECTION -> Icons.Default.Bookmark
        ItemCategory.REVIEW -> Icons.Default.RateReview
        ItemCategory.PRODUCT -> Icons.Default.ShoppingBag
        ItemCategory.FILM -> Icons.Default.Movie
        ItemCategory.SOFTWARE -> Icons.Default.Code
        ItemCategory.TV_SHOW -> Icons.Default.Tv
        ItemCategory.TUTORIAL -> Icons.Default.School
    }
}

@Composable
fun CategoryBadgeIcon(
    category: ItemCategory,
    modifier: Modifier = Modifier,
    size: Int = 22,
    iconSize: Int = 13
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(AppBlack),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIcon(category),
            contentDescription = null,
            tint = AppWhite,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}
