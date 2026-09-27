package com.mimo.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mimo.app.data.model.SaveItem
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppCardBg
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ItemPreviewPopup(
    item: SaveItem?,
    anchorOffset: Offset? = null,
    onDismiss: () -> Unit,
    onOpen: (SaveItem) -> Unit,
    onToggleFavorite: (SaveItem) -> Unit,
    onDetails: (SaveItem) -> Unit,
    onDelete: (SaveItem) -> Unit
) {
    if (item == null) return

    val coroutineScope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(false) }

    // Unified animation progress: 0f = collapsed at origin folder, 1f = fully expanded in center
    val animProgress = remember { Animatable(0f) }
    val actionsAlpha = remember { Animatable(0f) }
    val actionsTranslationY = remember { Animatable(30f) }

    LaunchedEffect(item) {
        if (item != null) {
            launch {
                // Smooth physical expansion from the origin folder
                animProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = 180f // Gentle, elegant fluid motion
                    )
                )
            }
            delay(120)
            launch {
                actionsAlpha.animateTo(1f, tween(180))
            }
            launch {
                actionsTranslationY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 220f
                    )
                )
            }
        }
    }

    val closeWithAnimation: () -> Unit = {
        if (!isClosing) {
            isClosing = true
            coroutineScope.launch {
                launch { actionsAlpha.animateTo(0f, tween(100)) }
                launch { actionsTranslationY.animateTo(25f, tween(120)) }
                launch {
                    animProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = 240f
                        )
                    )
                }
                delay(180)
                onDismiss()
            }
        }
    }

    // Intercept back button to dismiss smoothly
    BackHandler {
        closeWithAnimation()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f * animProgress.value))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = closeWithAnimation
            ),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val startTranslationX = remember(anchorOffset, screenWidthPx) {
            if (anchorOffset != null && anchorOffset.x > 0f) {
                anchorOffset.x - (screenWidthPx / 2f)
            } else 0f
        }
        val startTranslationY = remember(anchorOffset, screenHeightPx) {
            if (anchorOffset != null && anchorOffset.y > 0f) {
                anchorOffset.y - (screenHeightPx / 2f)
            } else 0f
        }

        val currentScale = 0.35f + 0.65f * animProgress.value
        val currentTransX = startTranslationX * (1f - animProgress.value)
        val currentTransY = startTranslationY * (1f - animProgress.value)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .graphicsLayer {
                    translationX = currentTransX
                    translationY = currentTransY
                    scaleX = currentScale
                    scaleY = currentScale
                    alpha = animProgress.value.coerceIn(0f, 1f)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instagram-Style Scaled Pop-Up Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(AppCardBg)
                    .border(1.dp, AppBorderGrey, RoundedCornerShape(22.dp))
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Media preview container with real AsyncImage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppInputBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!item.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = getCategoryIcon(item.category),
                                contentDescription = null,
                                tint = AppLightGrey,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppBlack,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.subtitle,
                            fontSize = 13.sp,
                            color = AppLightGrey,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (item.url.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.url,
                            fontSize = 12.sp,
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

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons Bar below the pop-up (Instagram style)
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = actionsTranslationY.value
                        alpha = actionsAlpha.value
                    }
                    .shadow(16.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppCardBg)
                    .border(1.dp, AppBorderGrey, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action 1: Open link
                PopupActionButton(
                    icon = Icons.Default.OpenInNew,
                    label = "Open",
                    onClick = {
                        closeWithAnimation()
                        onOpen(item)
                    }
                )

                // Action 2: Favorite toggle
                PopupActionButton(
                    icon = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = if (item.isFavorite) "Saved" else "Favorite",
                    tint = if (item.isFavorite) AppAccentRed else AppBlack,
                    onClick = {
                        onToggleFavorite(item)
                    }
                )

                // Action 3: Details
                PopupActionButton(
                    icon = Icons.Default.Info,
                    label = "Details",
                    onClick = {
                        closeWithAnimation()
                        onDetails(item)
                    }
                )

                // Action 4: Delete
                PopupActionButton(
                    icon = Icons.Default.DeleteOutline,
                    label = "Delete",
                    tint = AppAccentRed,
                    onClick = {
                        closeWithAnimation()
                        onDelete(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun PopupActionButton(
    icon: ImageVector,
    label: String,
    tint: Color = AppBlack,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AppInputBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}
