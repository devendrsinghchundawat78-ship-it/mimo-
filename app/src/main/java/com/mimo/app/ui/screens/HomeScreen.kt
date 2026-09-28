package com.mimo.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mimo.app.data.model.ItemCategory
import com.mimo.app.data.model.SaveItem
import com.mimo.app.data.model.UserCollection
import com.mimo.app.ui.components.AppLogo
import com.mimo.app.ui.components.CardFloatingPill
import com.mimo.app.ui.components.CategoryBadgeIcon
import com.mimo.app.ui.components.ItemPreviewPopup
import com.mimo.app.ui.components.PlatformBadgeIcon
import com.mimo.app.ui.components.SaveHubBottomSheet
import com.mimo.app.ui.components.getCategoryIcon
import com.mimo.app.ui.components.getCategoryDrawableRes
import com.mimo.app.ui.components.getCategoryTheme
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppCardBg
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import com.mimo.app.ui.theme.LiquidGlassManager
import com.mimo.app.ui.theme.ThemeManager

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

    // Scroll-driven compact mode connection matching Echo-Music
    val density = LocalDensity.current
    val scrollThresholdPx = with(density) { 36.dp.toPx() }
    var isBarCompact by remember { mutableStateOf(false) }
    var accumulatedScroll by remember { mutableStateOf(0f) }

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!LiquidGlassManager.compactOnScroll) {
                    if (isBarCompact) isBarCompact = false
                    return Offset.Zero
                }
                val scrollDelta = available.y
                if ((accumulatedScroll > 0 && scrollDelta < 0) || (accumulatedScroll < 0 && scrollDelta > 0)) {
                    accumulatedScroll = 0f
                }
                accumulatedScroll += scrollDelta

                if (accumulatedScroll <= -scrollThresholdPx && !isBarCompact) {
                    isBarCompact = true
                    accumulatedScroll = 0f
                } else if (accumulatedScroll >= scrollThresholdPx && isBarCompact) {
                    isBarCompact = false
                    accumulatedScroll = 0f
                }
                return Offset.Zero
            }
        }
    }

    // State for Instagram-style hold preview
    var previewItem by remember { mutableStateOf<SaveItem?>(null) }
    var previewAnchor by remember { mutableStateOf<Offset?>(null) }

    // State for Detail Screen (in-app photo/video streaming)
    var selectedDetailItem by remember { mutableStateOf<SaveItem?>(null) }

    val scope = rememberCoroutineScope()
    val savedItems = com.mimo.app.data.repository.SaveRepository.saves
    val collections = com.mimo.app.data.repository.SaveRepository.collections
    val isLoadingData = com.mimo.app.data.repository.SaveRepository.isLoading
    val loadError = com.mimo.app.data.repository.SaveRepository.errorMessage

    // Sort/Filter modes: 0: All (Newest First), 1: Favorites, 2: Collections, 3: Archived
    var sortFilterMode by remember { mutableIntStateOf(0) }
    var selectedCollectionId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        com.mimo.app.data.repository.SaveRepository.loadData()
    }

    var selectedCategory by remember { mutableStateOf<ItemCategory?>(null) }
    var selectedHomeCollectionId by remember { mutableStateOf<String?>(null) }

    val filteredItems = savedItems.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.subtitle.contains(searchQuery, ignoreCase = true) ||
            item.url.contains(searchQuery, ignoreCase = true) ||
            item.sourcePlatform.contains(searchQuery, ignoreCase = true) ||
            (item.noteContent?.contains(searchQuery, ignoreCase = true) == true)

        val matchesCategory = selectedCategory == null || item.category == selectedCategory
        val matchesHomeCollection = selectedHomeCollectionId == null || item.collectionId == selectedHomeCollectionId

        val matchesFilter = when (sortFilterMode) {
            1 -> item.isFavorite
            2 -> if (selectedCollectionId != null) item.collectionId == selectedCollectionId else true
            3 -> item.isArchived
            else -> !item.isArchived
        }

        matchesQuery && matchesCategory && matchesHomeCollection && matchesFilter
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
                        onItemClick = { selectedDetailItem = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                4 -> {
                    // INSTAGRAM-STYLE PROFILE SCREEN
                    ProfileScreen(
                        savedItems = savedItems,
                        onItemLongPress = { previewItem = it },
                        onItemClick = { selectedDetailItem = it },
                        onSignOut = onSignOut,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    if (selectedCategory == ItemCategory.PHOTO && selectedHomeCollectionId == null) {
                        PhotoGalleryScreen(
                            savedItems = savedItems,
                            onBack = { selectedCategory = null },
                            onOpen = { selectedDetailItem = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual library destinations under search. A collection is a real saved list;
                        // a category is a typed filter, never a fabricated travel collection.
                        HomeDestinationRow(
                            selectedCategory = selectedCategory,
                            selectedCollectionId = selectedHomeCollectionId,
                            collections = collections,
                            onCategory = { selectedHomeCollectionId = null; selectedCategory = it },
                            onCollection = { selectedHomeCollectionId = it; selectedCategory = null }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Scrollable Content
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(scrollConnection),
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
                                                onLongPress = { offset ->
                                                    previewItem = item
                                                    previewAnchor = offset
                                                },
                                                onClick = {
                                                    selectedDetailItem = item
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // 4. All Saves & Collections Header with Filter Modes & Box/List Switcher
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Error Banner if cloud fetch failed
                                    if (loadError != null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFFF0F0))
                                                .border(1.dp, AppAccentRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = loadError.orEmpty(),
                                                color = AppAccentRed,
                                                fontSize = 12.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "Retry",
                                                color = AppAccentRed,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clickable {
                                                        scope.launch {
                                                            com.mimo.app.data.repository.SaveRepository.loadData()
                                                        }
                                                    }
                                                    .padding(start = 8.dp)
                                            )
                                        }
                                    }

                                    // Sort / Filter Pills Row (Newest first, Favorites, Collections, Archived)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val filterOptions = listOf("All", "Favorites", "Collections", "Archived")
                                        filterOptions.forEachIndexed { index, title ->
                                            val isSelected = sortFilterMode == index
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(if (isSelected) AppBlack else AppInputBg)
                                                    .clickable {
                                                        sortFilterMode = index
                                                        if (index != 2) selectedCollectionId = null
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = title,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) AppWhite else AppBlack
                                                )
                                            }
                                        }
                                    }

                                    // If Collections Filter Selected: Show Horizontal Collections Row
                                    if (sortFilterMode == 2 && collections.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            items(collections, key = { it.id }) { col ->
                                                val isColActive = selectedCollectionId == col.id
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(if (isColActive) AppBlack else AppInputBg)
                                                        .border(
                                                            1.dp,
                                                            if (isColActive) AppBlack else AppBorderGrey,
                                                            RoundedCornerShape(16.dp)
                                                        )
                                                        .clickable {
                                                            selectedCollectionId = if (isColActive) null else col.id
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = col.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isColActive) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isColActive) AppWhite else AppBlack
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Main Header Title and Grid/List Switcher
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = when (sortFilterMode) {
                                                    1 -> "Favorites"
                                                    2 -> if (selectedCollectionId != null) {
                                                        collections.find { it.id == selectedCollectionId }?.name ?: "Collection"
                                                    } else "Collections"
                                                    3 -> "Archived Saves"
                                                    else -> "All Saves"
                                                },
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppBlack
                                            )

                                            if (isLoadingData) {
                                                Spacer(modifier = Modifier.width(10.dp))
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = AppBlack
                                                )
                                            }
                                        }

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
                                                text = when {
                                                    searchQuery.isNotEmpty() -> "No matching saves found"
                                                    selectedHomeCollectionId != null -> "No saves in this collection"
                                                    selectedCategory != null -> "No saves in this category"
                                                    else -> "No saves yet"
                                                },
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
                                        onLongPress = { offset ->
                                            previewItem = item
                                            previewAnchor = offset
                                        },
                                        onClick = {
                                            selectedDetailItem = item
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            } else {
                                // Grid View with uniform cards and real thumbnails
                                val chunks = filteredItems.chunked(2)
                                items(chunks) { rowItems ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Max),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        for (item in rowItems) {
                                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                GridSaveCard(
                                                    item = item,
                                                    onLongPress = { offset ->
                                                        previewItem = item
                                                        previewAnchor = offset
                                                    },
                                                    onClick = {
                                                        selectedDetailItem = item
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
        }

        // 5. iOS-Style Liquid Glass Bottom Bar (anchored over Home & Map views)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            LiquidGlassBottomBar(
                selectedTab = activeTab,
                isCompact = isBarCompact,
                onExpand = { isBarCompact = false },
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
                onItemSaved = { _ ->
                    // SaveRepository automatically updates reactive state and local cache
                }
            )
        }

        // 7. Instagram-Style Long-Press / Hold Preview Pop-Up
        if (previewItem != null) {
            ItemPreviewPopup(
                item = previewItem,
                anchorOffset = previewAnchor,
                onDismiss = {
                    previewItem = null
                    previewAnchor = null
                },
                onOpen = { item ->
                    selectedDetailItem = item
                    previewItem = null
                    previewAnchor = null
                },
                onToggleFavorite = { item ->
                    scope.launch {
                        com.mimo.app.data.repository.SaveRepository.toggleFavorite(item)
                        val updated = item.copy(isFavorite = !item.isFavorite)
                        previewItem = updated
                        if (selectedDetailItem?.id == item.id) {
                            selectedDetailItem = updated
                        }
                    }
                },
                onDetails = { item ->
                    selectedDetailItem = item
                    previewItem = null
                    previewAnchor = null
                },
                onDelete = { item ->
                    scope.launch {
                        com.mimo.app.data.repository.SaveRepository.deleteItem(item)
                    }
                    if (selectedDetailItem?.id == item.id) {
                        selectedDetailItem = null
                    }
                    previewItem = null
                    previewAnchor = null
                }
            )
        }

        // 8. In-App Detail Screen (Full photo/video streaming, floating liquid glass controls)
        AnimatedVisibility(
            visible = selectedDetailItem != null,
            enter = fadeIn(animationSpec = tween(220)) + scaleIn(
                animationSpec = spring(
                    dampingRatio = 0.76f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialScale = 0.84f
            ),
            exit = fadeOut(animationSpec = tween(180)) + scaleOut(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                targetScale = 0.88f
            )
        ) {
            selectedDetailItem?.let { currentDetailItem ->
                DetailScreen(
                    item = currentDetailItem,
                    allSavedItems = savedItems,
                    onBack = { selectedDetailItem = null },
                    onSelectItem = { nextItem ->
                        selectedDetailItem = nextItem
                    },
                    onToggleFavorite = { itemToFav ->
                        scope.launch {
                            com.mimo.app.data.repository.SaveRepository.toggleFavorite(itemToFav)
                            if (selectedDetailItem?.id == itemToFav.id) {
                                selectedDetailItem = itemToFav.copy(isFavorite = !itemToFav.isFavorite)
                            }
                        }
                    },
                    onDelete = { itemToDel ->
                        scope.launch {
                            com.mimo.app.data.repository.SaveRepository.deleteItem(itemToDel)
                        }
                        selectedDetailItem = null
                    },
                    onUpdateItem = { itemToUpdate ->
                        scope.launch {
                            com.mimo.app.data.repository.SaveRepository.updateItem(itemToUpdate)
                            selectedDetailItem = itemToUpdate
                        }
                    },
                    onItemLongPress = { item, anchor ->
                        previewAnchor = anchor
                        previewItem = item
                    }
                )
            }
        }
    }
}

// Sleek Recent Save Card in Horizontal Sliding Carousel with Albo floating pills
@Composable
fun RecentSaveCarouselCard(
    item: SaveItem,
    onLongPress: (Offset) -> Unit,
    onClick: () -> Unit
) {
    var cardCenter by remember { mutableStateOf(Offset.Zero) }
    val catTheme = getCategoryTheme(item.category)
    val isDark = ThemeManager.isDark

    Column(
        modifier = Modifier
            .width(150.dp)
            .height(207.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(AppCardBg)
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.boundsInRoot()
                cardCenter = pos.center
            }
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPress(cardCenter) },
                    onTap = { onClick() }
                )
            }
            .padding(9.dp)
            .animateContentSize()
    ) {
        // Media Preview with bottom-left floating brand + category pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(151.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) catTheme.containerDark else catTheme.containerLight),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(getCategoryDrawableRes(item.category)),
                    fallback = painterResource(getCategoryDrawableRes(item.category))
                )

                // Subtle dark vignette at bottom of image for badge legibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0x66000000))
                            )
                        )
                )
            } else {
                Icon(
                    imageVector = catTheme.icon,
                    contentDescription = null,
                    tint = catTheme.accentColor,
                    modifier = Modifier.size(38.dp)
                )
            }

            // Albo-style Floating Bottom-Left Pill
            CardFloatingPill(
                platform = item.sourcePlatform,
                category = item.category,
                url = item.url,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )

            // Favorite heart badge if favorited
            if (item.isFavorite) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.50f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = AppAccentRed,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title and 3-dots Menu Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = AppLightGrey,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onLongPress(cardCenter) }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = if (item.subtitle.isNotBlank()) item.subtitle else "${item.sourcePlatform} • ${item.dateAdded}",
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
    onLongPress: (Offset) -> Unit,
    onClick: () -> Unit
) {
    var cardCenter by remember { mutableStateOf(Offset.Zero) }
    val catTheme = getCategoryTheme(item.category)
    val isDark = ThemeManager.isDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppCardBg)
            .border(1.dp, AppBorderGrey, RoundedCornerShape(18.dp))
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.boundsInRoot()
                cardCenter = pos.center
            }
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPress(cardCenter) },
                    onTap = { onClick() }
                )
            }
            .padding(12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with Platform Badge in corner
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isDark) catTheme.containerDark else catTheme.containerLight),
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
                    imageVector = catTheme.icon,
                    contentDescription = null,
                    tint = catTheme.accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Platform badge
            PlatformBadgeIcon(
                platform = item.sourcePlatform,
                url = item.url,
                size = 18,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (item.isFavorite) {
                    Spacer(modifier = Modifier.width(6.dp))
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

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Category Tag Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDark) catTheme.containerDark else catTheme.containerLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = catTheme.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = catTheme.accentColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${item.sourcePlatform} • ${item.dateAdded}",
                    fontSize = 11.sp,
                    color = AppLightGrey
                )
            }
        }

        // 3-dots icon
        IconButton(
            onClick = { onLongPress(cardCenter) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = AppLightGrey,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// Albo-style Staggered/Grid Save Card with floating brand & category pill
@Composable
fun GridSaveCard(
    item: SaveItem,
    onLongPress: (Offset) -> Unit,
    onClick: () -> Unit
) {
    var cardCenter by remember { mutableStateOf(Offset.Zero) }
    val catTheme = getCategoryTheme(item.category)
    val isDark = ThemeManager.isDark

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppCardBg)
            .border(1.dp, AppBorderGrey, RoundedCornerShape(20.dp))
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.boundsInRoot()
                cardCenter = pos.center
            }
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPress(cardCenter) },
                    onTap = { onClick() }
                )
            }
            .padding(10.dp)
            .animateContentSize()
    ) {
        // Media Section with generous height & bottom-left floating pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) catTheme.containerDark else catTheme.containerLight),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark bottom vignette scrim for contrast
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0x66000000))
                            )
                        )
                )
            } else {
                Icon(
                    imageVector = catTheme.icon,
                    contentDescription = null,
                    tint = catTheme.accentColor,
                    modifier = Modifier.size(42.dp)
                )
            }

            // Albo-style Floating Bottom-Left Pill
            CardFloatingPill(
                platform = item.sourcePlatform,
                category = item.category,
                url = item.url,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )

            // Top-end Favorite indicator
            if (item.isFavorite) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.50f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = AppAccentRed,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title and 3-dots Menu Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = AppLightGrey,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onLongPress(cardCenter) }
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = if (item.subtitle.isNotBlank()) item.subtitle else "${item.sourcePlatform} • ${item.dateAdded}",
            fontSize = 11.sp,
            color = AppLightGrey,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Data item representing navigation bar tabs
private data class LiquidTabItem(
    val id: Int,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val isAction: Boolean = false
)

// Echo-Music Inspired Authentic Liquid Glass Floating Bottom Bar
// Features luminous translucent sliding glass lens, horizontal active-tab expansion & scroll-driven compact capsule
@Composable
fun LiquidGlassBottomBar(
    selectedTab: Int,
    isCompact: Boolean = false,
    onExpand: () -> Unit = {},
    onTabSelect: (Int) -> Unit
) {
    val items = remember {
        listOf(
            LiquidTabItem(0, "Home", Icons.Filled.Home, Icons.Outlined.Home),
            LiquidTabItem(1, "Map", Icons.Filled.Map, Icons.Outlined.Map),
            LiquidTabItem(2, "Add", Icons.Filled.Add, Icons.Filled.Add, isAction = true),
            LiquidTabItem(3, "Search", Icons.Filled.Search, Icons.Outlined.Search),
            LiquidTabItem(4, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
        )
    }

    val isDark = ThemeManager.isDark
    val isGlassEnabled = LiquidGlassManager.isEnabled
    val surfaceOpacity = LiquidGlassManager.surfaceOpacity
    val specular = LiquidGlassManager.specularHighlight
    val vibrancy = LiquidGlassManager.vibrancy
    val blurRadius = LiquidGlassManager.blurRadiusDp
    val density = LocalDensity.current

    // Glass Background Gradient
    val glassBgBrush = if (isGlassEnabled) {
        if (isDark) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF222226).copy(alpha = (surfaceOpacity + 0.25f).coerceAtMost(0.95f)),
                    Color(0xFF141416).copy(alpha = surfaceOpacity)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF).copy(alpha = (surfaceOpacity + 0.35f).coerceAtMost(0.95f)),
                    Color(0xFFF2F2F7).copy(alpha = surfaceOpacity)
                )
            )
        }
    } else {
        if (isDark) Brush.verticalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF121212)))
        else Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7)))
    }

    // Specular Highlight Rim Reflection
    val glassBorderBrush = if (isGlassEnabled && specular) {
        if (isDark) {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = (0.22f * vibrancy).coerceAtMost(0.70f)),
                    Color.White.copy(alpha = (0.10f * vibrancy).coerceAtMost(0.40f)),
                    Color(0x10FFFFFF)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xE6FFFFFF),
                    Color(0x55FFFFFF),
                    Color(0x26000000)
                )
            )
        }
    } else {
        if (isDark) Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x11FFFFFF)))
        else Brush.verticalGradient(listOf(Color(0x33000000), Color(0x1A000000)))
    }

    // Dynamic animated width between full floating capsule and compact pill
    val animatedCapsuleWidth by animateDpAsState(
        targetValue = if (isCompact) 156.dp else 360.dp,
        animationSpec = spring(
            dampingRatio = 0.88f,
            stiffness = 420f
        ),
        label = "CapsuleWidth"
    )

    // Outer Floating Glass Capsule
    Box(
        modifier = Modifier
            .width(animatedCapsuleWidth)
            .height(60.dp)
            .shadow(
                elevation = (8f + blurRadius).dp,
                shape = RoundedCornerShape(percent = 50),
                ambientColor = if (isDark) Color.Black.copy(alpha = (0.15f + blurRadius / 75f).coerceAtMost(0.60f)) else Color(0x1F000000),
                spotColor = if (isDark) Color(0x80000000) else Color(0x26000000)
            )
            .clip(RoundedCornerShape(percent = 50))
            .background(glassBgBrush)
            .border(1.2.dp, glassBorderBrush, RoundedCornerShape(percent = 50))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isCompact,
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(150))
            },
            contentAlignment = Alignment.Center,
            label = "LiquidBarMode"
        ) { compactMode ->
            if (compactMode) {
                // Compact Floating Capsule: Active Tab Pill + Quick Add '+' Action
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val activeItem = items.firstOrNull { it.id == selectedTab } ?: items[0]

                    // Active Tab button in compact mode (tapping it or bar expands)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.20f)
                                else Color.White.copy(alpha = 0.50f)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.40f)
                                else Color.White.copy(alpha = 0.85f),
                                RoundedCornerShape(percent = 50)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onExpand
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = activeItem.selectedIcon,
                            contentDescription = activeItem.label,
                            tint = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeItem.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color.Black,
                            maxLines = 1
                        )
                    }

                    // Quick Add Action '+' Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Brush.verticalGradient(listOf(Color(0xFF33343A), Color(0xFF202126)))
                                else Brush.verticalGradient(listOf(Color(0xFF222222), Color(0xFF000000)))
                            )
                            .border(1.dp, if (isDark) Color(0x55FFFFFF) else Color(0x33FFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelect(2) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Save",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // Expanded Mode: Full 5 tabs with lookahead sliding glass lens pill
                val tabWidths = remember { mutableStateMapOf<Int, Dp>() }
                val tabOffsets = remember { mutableStateMapOf<Int, Dp>() }

                val targetWidth = tabWidths[selectedTab] ?: 52.dp
                val targetOffset = tabOffsets[selectedTab] ?: 0.dp

                val animatedPillWidth by animateDpAsState(
                    targetValue = targetWidth,
                    animationSpec = spring(
                        dampingRatio = 0.88f,
                        stiffness = 420f
                    ),
                    label = "PillWidth"
                )

                val animatedPillOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = spring(
                        dampingRatio = 0.88f,
                        stiffness = 420f
                    ),
                    label = "PillOffset"
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Luminous Inner Liquid Glass Sliding Lens Pill
                    if (selectedTab != 2 && (tabWidths[selectedTab] ?: 0.dp) > 0.dp) {
                        Box(
                            modifier = Modifier
                                .offset(x = animatedPillOffset)
                                .width(animatedPillWidth)
                                .height(46.dp)
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(percent = 50),
                                    ambientColor = if (isDark) Color(0x40000000) else Color(0x15000000)
                                )
                                .clip(RoundedCornerShape(percent = 50))
                                .background(
                                    if (isDark) Color.White.copy(alpha = 0.20f)
                                    else Color.White.copy(alpha = 0.50f)
                                )
                                .border(
                                    1.dp,
                                    if (isDark) Color.White.copy(alpha = 0.40f)
                                    else Color.White.copy(alpha = 0.85f),
                                    RoundedCornerShape(percent = 50)
                                )
                        )
                    }

                    // Row of Interactive Tabs
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        items.forEach { tab ->
                            if (tab.isAction) {
                                // Center Plus Quick Add Button
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDark) {
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF33343A), Color(0xFF202126))
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF222222), Color(0xFF000000))
                                                )
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isDark) Color(0x55FFFFFF) else Color(0x33FFFFFF),
                                            CircleShape
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { onTabSelect(tab.id) }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Save",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                val isSelected = selectedTab == tab.id
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()

                                val tabScale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.92f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.80f,
                                        stiffness = 500f
                                    ),
                                    label = "TabScale"
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .onGloballyPositioned { coords ->
                                            tabWidths[tab.id] = with(density) { coords.size.width.toDp() }
                                            tabOffsets[tab.id] = with(density) { coords.positionInParent().x.toDp() }
                                        }
                                        .graphicsLayer {
                                            scaleX = tabScale
                                            scaleY = tabScale
                                        }
                                        .clip(RoundedCornerShape(percent = 50))
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null,
                                            onClick = { onTabSelect(tab.id) }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 9.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                        tint = if (isSelected) {
                                            if (isDark) Color.White else Color.Black
                                        } else {
                                            if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
                                        },
                                        modifier = Modifier.size(21.dp)
                                    )

                                    AnimatedVisibility(
                                        visible = isSelected,
                                        enter = expandHorizontally(
                                            expandFrom = Alignment.Start,
                                            animationSpec = spring(
                                                dampingRatio = 0.88f,
                                                stiffness = 420f
                                            )
                                        ) + fadeIn(animationSpec = tween(150)),
                                        exit = shrinkHorizontally(
                                            shrinkTowards = Alignment.Start,
                                            animationSpec = spring(
                                                dampingRatio = 0.88f,
                                                stiffness = 420f
                                            )
                                        ) + fadeOut(animationSpec = tween(100))
                                    ) {
                                        Text(
                                            text = tab.label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color.White else Color.Black,
                                            maxLines = 1,
                                            modifier = Modifier.padding(start = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class LibraryDestination {
    data class Category(val category: ItemCategory, val label: String) : LibraryDestination()
    data class Collection(val collection: UserCollection) : LibraryDestination()
}

/** Scrollable visual destinations, backed only by categories and real user collections. */
@Composable
private fun HomeDestinationRow(
    selectedCategory: ItemCategory?,
    selectedCollectionId: String?,
    collections: List<UserCollection>,
    onCategory: (ItemCategory?) -> Unit,
    onCollection: (String?) -> Unit
) {
    val destinations = remember(collections.toList()) {
        val featured = collections.filter { !it.isArchived }.sortedBy { it.sortOrder }.take(6)
            .map { LibraryDestination.Collection(it) }
        listOf(
            LibraryDestination.Category(ItemCategory.RECIPE, "Recipes"),
            LibraryDestination.Category(ItemCategory.PLACE, "Travel & places"),
            LibraryDestination.Category(ItemCategory.FILM, "Watch"),
            LibraryDestination.Category(ItemCategory.PHOTO, "Photos"),
            LibraryDestination.Category(ItemCategory.MUSIC, "Music"),
            LibraryDestination.Category(ItemCategory.BOOK, "Books")
        ) + featured
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 3.dp)
    ) {
        item {
            val selected = selectedCategory == null && selectedCollectionId == null
            Row(
                modifier = Modifier.height(58.dp).clip(RoundedCornerShape(22.dp))
                    .background(if (selected) AppBlack else AppInputBg)
                    .border(1.dp, AppBorderGrey, RoundedCornerShape(22.dp))
                    .clickable { onCollection(null); onCategory(null) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Home, contentDescription = null,
                    tint = if (selected) AppWhite else AppBlack, modifier = Modifier.size(25.dp))
                Spacer(Modifier.width(9.dp))
                Text("All", color = if (selected) AppWhite else AppBlack,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        items(destinations) { destination ->
            val category = (destination as? LibraryDestination.Category)?.category
            val collection = (destination as? LibraryDestination.Collection)?.collection
            val selected = if (collection != null) selectedCollectionId == collection.id
                else selectedCategory == category && selectedCollectionId == null
            val label = collection?.name ?: (destination as LibraryDestination.Category).label
            Row(
                modifier = Modifier.height(58.dp).clip(RoundedCornerShape(22.dp))
                    .background(if (selected) AppBlack else AppInputBg)
                    .border(1.dp, AppBorderGrey, RoundedCornerShape(22.dp))
                    .clickable {
                        if (collection != null) onCollection(if (selected) null else collection.id)
                        else onCategory(if (selected) null else category)
                    }.padding(start = 10.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (category != null) {
                    Image(painter = painterResource(getCategoryDrawableRes(category)),
                        contentDescription = null, modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit)
                } else {
                    val iconCategory = ItemCategory.COLLECTION
                    Image(painter = painterResource(getCategoryDrawableRes(iconCategory)),
                        contentDescription = null, modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit)
                }
                Spacer(Modifier.width(7.dp))
                Text(label, color = if (selected) AppWhite else AppBlack,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
