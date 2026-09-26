package com.mimo.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mimo.app.data.model.SaveItem
import com.mimo.app.ui.components.AppLogo
import com.mimo.app.ui.components.CategoryBadgeIcon
import com.mimo.app.ui.components.ItemPreviewPopup
import com.mimo.app.ui.components.SaveHubBottomSheet
import com.mimo.app.ui.components.getCategoryIcon
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    // State for Instagram-style hold preview
    var previewItem by remember { mutableStateOf<SaveItem?>(null) }

    // Real live state collection for saves
    val savedItems = remember { mutableStateListOf<SaveItem>() }

    val filteredItems = if (searchQuery.isBlank()) {
        savedItems
    } else {
        savedItems.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true) ||
            it.url.contains(searchQuery, ignoreCase = true) ||
            it.sourcePlatform.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppWhite)
    ) {
        // Tab Content Switcher with Crossfade
        Crossfade(
            targetState = activeTab,
            animationSpec = tween(durationMillis = 300),
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (previewItem != null) Modifier.blur(20.dp) else Modifier
                ),
            label = "TabCrossfade"
        ) { tabIndex ->
            when (tabIndex) {
                1 -> {
                    // REAL ACTUAL MAP SCREEN
                    MapScreen(
                        savedItems = savedItems,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                3 -> {
                    // SEARCH SCREEN
                    SearchScreen(
                        savedItems = savedItems,
                        onItemLongPress = { previewItem = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                4 -> {
                    // INSTAGRAM-STYLE PROFILE SCREEN
                    ProfileScreen(
                        savedItems = savedItems,
                        onItemLongPress = { previewItem = it },
                        onSignOut = onSignOut,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // HOME SCREEN CONTENT
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. Top Bar: Left Logo + Stylized "Mimo" font, Right '+' Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppLogo(size = 36)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Mimo",
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = AppBlack,
                                    letterSpacing = 1.2.sp
                                )
                            }

                            // Add button (+)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AppBlack)
                                    .clickable { showAddDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Save",
                                    tint = AppWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // 2. Search Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(AppInputBg, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = AppLightGrey,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search saves...",
                                            color = AppLightGrey,
                                            fontSize = 15.sp
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        singleLine = true,
                                        cursorBrush = SolidColor(AppBlack),
                                        textStyle = TextStyle(
                                            color = AppBlack,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.SansSerif
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = AppLightGrey,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Scrollable Content
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 110.dp)
                        ) {
                            // 3. Recently Saved Section - Horizontal Sliding Carousel
                            item {
                                Text(
                                    text = "Recently Saved",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppBlack,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                if (savedItems.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(AppInputBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No recent saves",
                                            fontSize = 14.sp,
                                            color = AppLightGrey
                                        )
                                    }
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(savedItems.take(5), key = { "recent_" + it.id }) { item ->
                                            RecentSaveCarouselCard(
                                                item = item,
                                                onLongPress = { previewItem = item },
                                                onClick = {
                                                    if (item.url.isNotBlank()) {
                                                        try {
                                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                                                        } catch (_: Exception) {}
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // 4. All Saves & Collections Header with Box/List Switcher
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "All Saves",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppBlack
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(AppInputBg)
                                            .clickable { isGridView = !isGridView }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                                contentDescription = "Toggle View",
                                                tint = AppBlack,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isGridView) "List" else "Grid",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppBlack
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // All Saves List / Grid
                            if (filteredItems.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(AppInputBg)
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (searchQuery.isEmpty()) "No saves yet" else "No matching saves found",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppBlack
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Tap the + button to save your first link or note",
                                                fontSize = 13.sp,
                                                color = AppLightGrey,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            } else if (!isGridView) {
                                // List View with real thumbnails and descriptions
                                items(filteredItems, key = { it.id }) { item ->
                                    ListSaveCard(
                                        item = item,
                                        onLongPress = { previewItem = item },
                                        onClick = {
                                            if (item.url.isNotBlank()) {
                                                try {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            } else {
                                // Grid View with real thumbnails and descriptions
                                val chunks = filteredItems.chunked(2)
                                items(chunks) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        for (item in rowItems) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                GridSaveCard(
                                                    item = item,
                                                    onLongPress = { previewItem = item },
                                                    onClick = {
                                                        if (item.url.isNotBlank()) {
                                                            try {
                                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                                                            } catch (_: Exception) {}
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. iOS-Style Liquid Glass Bottom Bar (anchored over Home & Map views)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            LiquidGlassBottomBar(
                selectedTab = activeTab,
                onTabSelect = { tabIndex ->
                    if (tabIndex == 2) {
                        showAddDialog = true
                    } else {
                        activeTab = tabIndex
                        if (tabIndex == 4) {
                            onProfileClick()
                        }
                    }
                }
            )
        }

        // 6. Save Hub Bottom Sheet (Paste any URL with real metadata fetch, Notes, etc.)
        if (showAddDialog) {
            SaveHubBottomSheet(
                onDismiss = { showAddDialog = false },
                onItemSaved = { newItem ->
                    savedItems.add(0, newItem)
                }
            )
        }

        // 7. Instagram-Style Long-Press / Hold Preview Pop-Up
        if (previewItem != null) {
            ItemPreviewPopup(
                item = previewItem,
                onDismiss = { previewItem = null },
                onOpen = { item ->
                    if (item.url.isNotBlank()) {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                        } catch (_: Exception) {}
                    }
                },
                onToggleFavorite = { item ->
                    val index = savedItems.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        val updated = item.copy(isFavorite = !item.isFavorite)
                        savedItems[index] = updated
                        previewItem = updated
                    }
                },
                onDetails = { item ->
                    // Details action
                },
                onDelete = { item ->
                    savedItems.removeAll { it.id == item.id }
                    previewItem = null
                }
            )
        }
    }
}

// Sleek Recent Save Card in Horizontal Sliding Carousel
@Composable
fun RecentSaveCarouselCard(
    item: SaveItem,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppWhite)
            .border(1.dp, AppBorderGrey, RoundedCornerShape(16.dp))
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { onClick() }
                )
            }
            .padding(10.dp)
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
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

        Spacer(modifier = Modifier.height(10.dp))

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

@Composable
fun ListSaveCard(
    item: SaveItem,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppWhite)
            .border(1.dp, AppBorderGrey, RoundedCornerShape(16.dp))
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { onClick() }
                )
            }
            .padding(12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(54.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
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
                        tint = AppBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            CategoryBadgeIcon(
                category = item.category,
                size = 18,
                iconSize = 10,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (item.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = AppAccentRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (item.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = AppLightGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${item.sourcePlatform} • ${item.dateAdded}",
                fontSize = 11.sp,
                color = AppLightGrey
            )
        }
    }
}

@Composable
fun GridSaveCard(
    item: SaveItem,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppWhite)
            .border(1.dp, AppBorderGrey, RoundedCornerShape(16.dp))
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { onClick() }
                )
            }
            .padding(12.dp)
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
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
                    tint = AppBlack,
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

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = item.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (item.subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                fontSize = 11.sp,
                color = AppLightGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

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

// iOS Style Liquid Glass Bottom Bar
@Composable
fun LiquidGlassBottomBar(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    val items = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Map, Icons.Outlined.Map),
        Pair(Icons.Filled.Add, Icons.Filled.Add), // Center Plus
        Pair(Icons.Filled.Search, Icons.Outlined.Search),
        Pair(Icons.Filled.Person, Icons.Outlined.Person)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0x1A000000),
                spotColor = Color(0x26000000)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xE6FFFFFF))
            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(32.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, pair ->
                val isSelected = selectedTab == index
                val isCenterPlus = index == 2

                if (isCenterPlus) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AppBlack)
                            .clickable { onTabSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save",
                            tint = AppWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) AppBlack else AppLightGrey,
                        label = "IconTint"
                    )

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .clickable { onTabSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) pair.first else pair.second,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
