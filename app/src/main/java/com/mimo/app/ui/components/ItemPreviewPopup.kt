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
    onDismiss: () -> Unit,
    onOpen: (SaveItem) -> Unit,
    onToggleFavorite: (SaveItem) -> Unit,
    onDetails: (SaveItem) -> Unit,
    onDelete: (SaveItem) -> Unit
) {
    if (item == null) return

    val coroutineScope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(false) }

    // Authentic mobile OS home screen icon-to-window spring animation
    // Starts from small icon size (0.28f scale) and smoothly expands with spring overshoot
    val cardScale = remember { Animatable(0.28f) }
    val cardAlpha = remember { Animatable(0f) }
    val bgAlpha = remember { Animatable(0f) }
    val actionsTranslationY = remember { Animatable(45f) }
    val actionsAlpha = remember { Animatable(0f) }

    LaunchedEffect(item) {
        if (item != null) {
            launch {
                bgAlpha.animateTo(
                    targetValue = 0.65f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
            launch {
                cardAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 150)
                )
            }
            launch {
                // Mobile OS icon-to-window expansion spring
                cardScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.68f, // physical spring bounce
                        stiffness = 380f     // smooth medium stiffness
                    )
                )
            }
            // Actions slide up smoothly right after the card expands
            delay(50)
            launch {
                actionsAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 160)
                )
            }
            launch {
                actionsTranslationY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = 400f
                    )
                )
            }
        }
    }

    val closeWithAnimation: () -> Unit = {
        if (!isClosing) {
            isClosing = true
            coroutineScope.launch {
                launch { bgAlpha.animateTo(0f, tween(160)) }
                launch { cardAlpha.animateTo(0f, tween(140)) }
                launch {
                    cardScale.animateTo(
                        targetValue = 0.28f,
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f)
                    )
                }
                launch { actionsAlpha.animateTo(0f, tween(100)) }
                launch { actionsTranslationY.animateTo(35f, tween(140)) }
                delay(160)
                onDismiss()
            }
        }
    }

    // Intercept back button to dismiss smoothly
    BackHandler {
        closeWithAnimation()
    }

    // Full screen overlay with frosted blur effect
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = bgAlpha.value))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = closeWithAnimation
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .graphicsLayer {
                    scaleX = cardScale.value
                    scaleY = cardScale.value
                    alpha = cardAlpha.value
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
