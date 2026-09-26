package com.mimo.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.app.data.model.SaveItem
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite

@Composable
fun ItemPreviewPopup(
    item: SaveItem?,
    onDismiss: () -> Unit,
    onOpen: (SaveItem) -> Unit,
    onToggleFavorite: (SaveItem) -> Unit,
    onDetails: (SaveItem) -> Unit,
    onDelete: (SaveItem) -> Unit
) {
    if (item == null) return

    val interactionSource = remember { MutableInteractionSource() }

    // Full screen overlay with frosted blur effect
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instagram-Style Scaled Pop-Up Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(AppWhite)
                    .padding(20.dp)
            ) {
                Column {
                    // Header row: Category Icon Badge + Platform / Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryBadgeIcon(category = item.category, size = 26, iconSize = 14)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.sourcePlatform,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppBlack
                            )
                        }

                        Text(
                            text = item.dateAdded,
                            fontSize = 12.sp,
                            color = AppLightGrey
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Media preview container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppInputBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(item.category),
                            contentDescription = null,
                            tint = AppLightGrey,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = item.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppBlack,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.url.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.url,
                            fontSize = 13.sp,
                            color = AppLightGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!item.noteContent.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.noteContent,
                            fontSize = 13.sp,
                            color = AppBlack,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Bar below the pop-up
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(AppWhite)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Open
                PopupActionButton(
                    icon = Icons.Default.OpenInNew,
                    label = "Open",
                    onClick = {
                        onOpen(item)
                        onDismiss()
                    }
                )

                // 2. Favorite
                PopupActionButton(
                    icon = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = if (item.isFavorite) "Saved" else "Fav.",
                    iconTint = if (item.isFavorite) AppAccentRed else AppBlack,
                    onClick = { onToggleFavorite(item) }
                )

                // 3. Details
                PopupActionButton(
                    icon = Icons.Default.Info,
                    label = "Details",
                    onClick = {
                        onDetails(item)
                        onDismiss()
                    }
                )

                // 4. Delete
                PopupActionButton(
                    icon = Icons.Default.DeleteOutline,
                    label = "Delete",
                    iconTint = AppAccentRed,
                    textColor = AppAccentRed,
                    onClick = {
                        onDelete(item)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun PopupActionButton(
    icon: ImageVector,
    label: String,
    iconTint: Color = AppBlack,
    textColor: Color = AppBlack,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
