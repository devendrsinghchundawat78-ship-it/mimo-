package com.mimo.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.app.data.model.SaveItem
import com.mimo.app.ui.components.AppInputField
import com.mimo.app.ui.components.AppLogo
import com.mimo.app.ui.components.PrimaryPillButton
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite
import kotlinx.coroutines.launch

enum class BottomTab {
    HOME,
    MAP,
    ADD,
    SEARCH,
    PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Real state collection for saves (Zero mock data - genuine empty state until added)
    val savedItems = remember { mutableStateListOf<SaveItem>() }

    val filteredItems = if (searchQuery.isBlank()) {
        savedItems
    } else {
        savedItems.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.url.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppWhite)
    ) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

            // Scrollable Content area with padding at bottom for floating nav bar
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                // 3. Recently Saved Section (Vertical)
                item {
                    Text(
                        text = "Recently Saved",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppBlack,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (savedItems.isEmpty()) {
                        // Genuine clean empty state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(AppInputBg, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recent saves",
                                fontSize = 14.sp,
                                color = AppLightGrey
                            )
                        }
                    } else {
                        // Display latest 3 saves vertically
                        savedItems.take(3).forEach { item ->
                            RecentSaveRow(item = item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 4. All Saves & Collections Section Header with Box/List Toggle Button
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

                        // Toggle style button (Box/Grid vs List)
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

                // All Saves content: Empty state or Grid/List view
                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(AppInputBg, RoundedCornerShape(16.dp))
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
                                    text = "Tap the + button to save your first link",
                                    fontSize = 13.sp,
                                    color = AppLightGrey,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else if (!isGridView) {
                    // List View
                    items(filteredItems, key = { it.id }) { item ->
                        ListSaveCard(item = item)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else {
                    // Grid / Box View (2 items per row in LazyColumn chunk)
                    val chunks = filteredItems.chunked(2)
                    items(chunks) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (item in rowItems) {
                                Box(modifier = Modifier.weight(1f)) {
                                    GridSaveCard(item = item)
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

        // 5. iOS-Style Liquid Glass Bottom Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            LiquidGlassBottomBar(
                selectedTab = activeTab,
                onTabSelect = { tabIndex ->
                    if (tabIndex == 2) {
                        // Center + action triggers Add Dialog
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

        // Add Save Bottom Sheet (Strict real data input - Zero mock data)
        if (showAddDialog) {
            AddSaveBottomSheet(
                onDismiss = { showAddDialog = false },
                onSave = { title, url ->
                    val newItem = SaveItem(
                        id = System.currentTimeMillis().toString(),
                        title = title.ifBlank { url },
                        url = url,
                        type = "Link",
                        dateAdded = "Just now"
                    )
                    savedItems.add(0, newItem)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun RecentSaveRow(item: SaveItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppInputBg, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AppWhite, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.title.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = AppBlack,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.url,
                fontSize = 12.sp,
                color = AppLightGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ListSaveCard(item: SaveItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppWhite, RoundedCornerShape(14.dp))
            .border(1.dp, AppBorderGrey, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(AppInputBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.title.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = AppBlack,
                fontSize = 17.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.url,
                fontSize = 13.sp,
                color = AppLightGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GridSaveCard(item: SaveItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppWhite, RoundedCornerShape(14.dp))
            .border(1.dp, AppBorderGrey, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(AppInputBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.title.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = AppBlack,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = item.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.url,
            fontSize = 12.sp,
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

    // Frosted liquid glass card container with blur background effect
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
            .background(Color(0xE6FFFFFF)) // 90% translucent white frosted glass
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
                    // Center prominent circular save button
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
                    // Standard tab item with smooth animation
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaveBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var urlText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Save Link",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppBlack
            )

            Spacer(modifier = Modifier.height(18.dp))

            AppInputField(
                label = "Link URL",
                value = urlText,
                onValueChange = { urlText = it },
                placeholder = "Paste link here..."
            )

            Spacer(modifier = Modifier.height(14.dp))

            AppInputField(
                label = "Title",
                value = titleText,
                onValueChange = { titleText = it },
                placeholder = "Add title (optional)"
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryPillButton(
                text = "Save",
                onClick = {
                    if (urlText.isNotBlank()) {
                        scope.launch {
                            sheetState.hide()
                            onSave(titleText, urlText.trim())
                        }
                    }
                }
            )
        }
    }
}
