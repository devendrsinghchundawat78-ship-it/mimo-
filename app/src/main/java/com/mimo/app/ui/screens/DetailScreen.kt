package com.mimo.app.ui.screens

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.mimo.app.data.model.SaveItem
import com.mimo.app.ui.components.CategoryBadgeIcon
import com.mimo.app.ui.components.getCategoryIcon
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppCardBg
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite
import com.mimo.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    item: SaveItem,
    allSavedItems: List<SaveItem>,
    onBack: () -> Unit,
    onSelectItem: (SaveItem) -> Unit,
    onToggleFavorite: (SaveItem) -> Unit,
    onDelete: (SaveItem) -> Unit,
    onItemLongPress: (SaveItem) -> Unit = {}
) {
    val context = LocalContext.current
    val isDark = ThemeManager.isDark

    var showMenu by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(true) }

    // Intercept hardware back button
    BackHandler {
        onBack()
    }

    // Related / Other saves (excluding current item)
    val otherSaves = remember(item, allSavedItems) {
        allSavedItems.filter { it.id != item.id }
    }

    // Liquid Glass styling for floating action buttons
    val glassBg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xD9222222), Color(0xB3141414)))
    } else {
        Brush.verticalGradient(listOf(Color(0xE6FFFFFF), Color(0xCCECECEC)))
    }
    val glassBorder = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x40FFFFFF), Color(0x10FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0x99FFFFFF), Color(0x26000000)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppWhite)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Media Header (Photo / In-App Video Stream)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(390.dp)
                        .background(if (isDark) Color(0xFF000000) else Color(0xFFEBEBEB))
                ) {
                    val hasVideo = !item.videoUrl.isNullOrBlank() ||
                            item.url.endsWith(".mp4") ||
                            item.url.contains(".mp4?") ||
                            item.url.endsWith(".webm")

                    if (hasVideo) {
                        // Direct In-App Video Player (Auto-play without sound / muted by default)
                        val videoStreamUrl = item.videoUrl ?: item.url
                        InAppVideoPlayer(
                            videoUrl = videoStreamUrl,
                            isMuted = isMuted,
                            modifier = Modifier.fillMaxSize(),
                            onToggleSound = { isMuted = !isMuted }
                        )

                        // Sound Toggle Pill Overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 60.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                                .clickable { isMuted = !isMuted }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (!item.imageUrl.isNullOrBlank()) {
                        // Direct In-App Photo Stream
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback Clean Graphic Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AppInputBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(item.category),
                                contentDescription = null,
                                tint = AppLightGrey,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    // Bottom Gradient Scrim & Blur Effect
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Liquid Glass Platform Source Chip with Arrow
                    if (item.url.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp, bottom = 16.dp)
                        ) {
                            LiquidGlassSourceChip(
                                platform = item.sourcePlatform,
                                onClick = {
                                    try {
                                        val targetUri = if (!item.url.startsWith("http://") && !item.url.startsWith("https://")) {
                                            "https://${item.url}"
                                        } else {
                                            item.url
                                        }
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUri)))
                                    } catch (_: Exception) {}
                                }
                            )
                        }
                    }
                }
            }

            // 2. Main Content Details Area
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // Category & Date Added Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryBadgeIcon(category = item.category, size = 24, iconSize = 12)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.sourcePlatform,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppLightGrey
                            )
                        }

                        Text(
                            text = item.dateAdded,
                            fontSize = 12.sp,
                            color = AppLightGrey
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stylized Distinct Title (Serif Typography per instruction)
                    Text(
                        text = item.title,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = AppBlack,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Full Description / Subtitle
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            fontSize = 15.sp,
                            color = AppLightGrey,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Notes Content (if available)
                    if (!item.noteContent.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppInputBg)
                                .border(1.dp, AppBorderGrey, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = item.noteContent,
                                fontSize = 14.sp,
                                color = AppBlack,
                                lineHeight = 21.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = AppBorderGrey)
                }
            }

            // 3. "More Saves" Section (Grid of other saved items)
            if (otherSaves.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "More Saves",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppBlack
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                val chunkedSaves = otherSaves.chunked(2)
                items(chunkedSaves) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (other in rowItems) {
                            Box(modifier = Modifier.weight(1f)) {
                                RelatedSaveCard(
                                    item = other,
                                    onClick = { onSelectItem(other) },
                                    onLongPress = { onItemLongPress(other) }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 4. Floating Liquid Glass Top Bar (Back, Share, and Three Dots)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Floating Liquid Glass Back Button
            LiquidGlassCircleButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                bgBrush = glassBg,
                borderBrush = glassBorder
            )

            // Right Action Buttons (Share & More Dots)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share Button
                LiquidGlassCircleButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Share",
                    onClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${item.title}\n${item.url}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Save"))
                        } catch (_: Exception) {}
                    },
                    bgBrush = glassBg,
                    borderBrush = glassBorder
                )

                // More Dots (...) Button
                Box {
                    LiquidGlassCircleButton(
                        icon = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        onClick = { showMenu = true },
                        bgBrush = glassBg,
                        borderBrush = glassBorder
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(AppCardBg)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (item.isFavorite) "Remove from Favorites" else "Add to Favorites",
                                    color = AppBlack
                                )
                            },
                            onClick = {
                                onToggleFavorite(item)
                                showMenu = false
                            }
                        )
                        if (item.url.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text("Open in Browser", color = AppBlack) },
                                onClick = {
                                    showMenu = false
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                                    } catch (_: Exception) {}
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete Save", color = AppAccentRed) },
                            onClick = {
                                showMenu = false
                                onDelete(item)
                                onBack()
                            }
                        )
                    }
                }
            }
        }
    }
}

// In-App Video Player Component with Auto-play and Muted Sound
@Composable
fun InAppVideoPlayer(
    videoUrl: String,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    onToggleSound: () -> Unit
) {
    var mediaPlayerInstance by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(isMuted) {
        val volume = if (isMuted) 0f else 1f
        mediaPlayerInstance?.setVolume(volume, volume)
        onDispose { }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggleSound
            ),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setVideoURI(Uri.parse(videoUrl))
                    setOnPreparedListener { mp ->
                        mediaPlayerInstance = mp
                        mp.isLooping = true
                        val volume = if (isMuted) 0f else 1f
                        mp.setVolume(volume, volume)
                        mp.start()
                    }
                    setOnErrorListener { _, _, _ -> true }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// Floating Liquid Glass Circular Action Button
@Composable
fun LiquidGlassCircleButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    bgBrush: Brush,
    borderBrush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color(0x33000000),
                spotColor = Color(0x40000000)
            )
            .clip(CircleShape)
            .background(bgBrush)
            .border(1.5.dp, borderBrush, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppBlack,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Liquid Glass Platform Source Chip with Platform Icon, Name, and Arrow
@Composable
fun LiquidGlassSourceChip(
    platform: String,
    onClick: () -> Unit
) {
    val isDark = ThemeManager.isDark

    val chipBg = if (isDark) {
        Brush.horizontalGradient(listOf(Color(0xD9222222), Color(0xBF181818)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xF0FFFFFF), Color(0xE6EDEDED)))
    }

    val chipBorder = if (isDark) {
        Brush.horizontalGradient(listOf(Color(0x66FFFFFF), Color(0x20FFFFFF)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xCCFFFFFF), Color(0x4D000000)))
    }

    val platformIcon = getPlatformIcon(platform)

    Row(
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(chipBg)
            .border(1.2.dp, chipBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = platformIcon,
            contentDescription = null,
            tint = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111111),
            modifier = Modifier.size(17.dp)
        )

        Text(
            text = platform,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111111)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open in browser",
            tint = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666),
            modifier = Modifier.size(14.dp)
        )
    }
}

// Map platform string to relevant icon
fun getPlatformIcon(platform: String): ImageVector {
    val lower = platform.lowercase()
    return when {
        lower.contains("insta") -> Icons.Default.CameraAlt
        lower.contains("youtube") || lower.contains("yt") -> Icons.Default.PlayArrow
        lower.contains("tiktok") -> Icons.Default.MusicNote
        lower.contains("twitter") || lower.contains("x") -> Icons.Default.Language
        lower.contains("facebook") || lower.contains("fb") -> Icons.Default.Public
        else -> Icons.Default.Language
    }
}

// Related Save Card in "More Saves" section
@Composable
fun RelatedSaveCard(
    item: SaveItem,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppCardBg)
            .border(1.dp, AppBorderGrey, RoundedCornerShape(16.dp))
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { onClick() }
                )
            }
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .clip(RoundedCornerShape(12.dp))
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
                    modifier = Modifier.size(32.dp)
                )
            }

            CategoryBadgeIcon(
                category = item.category,
                size = 20,
                iconSize = 11,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.sourcePlatform,
            fontSize = 11.sp,
            color = AppLightGrey,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
