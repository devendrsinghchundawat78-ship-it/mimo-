package com.mimo.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mimo.app.data.model.SaveItem
import com.mimo.app.ui.components.CategoryBadgeIcon
import com.mimo.app.ui.components.getCategoryIcon
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite

data class SearchCategory(
    val title: String,
    val icon: ImageVector,
    val filterKey: String
)

@Composable
fun SearchScreen(
    savedItems: List<SaveItem>,
    modifier: Modifier = Modifier,
    onItemLongPress: (SaveItem) -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categories = remember {
        listOf(
            SearchCategory("Books", Icons.Default.MenuBook, "Book"),
            SearchCategory("Movies", Icons.Default.Movie, "Film"),
            SearchCategory("Music", Icons.Default.MusicNote, "Music"),
            SearchCategory("Games", Icons.Default.SportsEsports, "Game"),
            SearchCategory("Softwares", Icons.Default.Code, "Software"),
            SearchCategory("Images", Icons.Default.Image, "Photo"),
            SearchCategory("Articles", Icons.Default.Article, "Article"),
            SearchCategory("Places", Icons.Default.Place, "Place")
        )
    }

    val searchBarHeight by animateDpAsState(
        targetValue = if (isSearchFocused) 52.dp else 48.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "SearchBarHeight"
    )

    // Back handler to collapse search when focused
    BackHandler(enabled = isSearchFocused || selectedCategory != null) {
        if (isSearchFocused) {
            focusManager.clearFocus()
            isSearchFocused = false
        } else {
            selectedCategory = null
        }
    }

    // Filter items based on search query and category
    val filteredResults = remember(searchQuery, selectedCategory, savedItems) {
        savedItems.filter { item ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true) ||
                item.url.contains(searchQuery, ignoreCase = true) ||
                item.sourcePlatform.contains(searchQuery, ignoreCase = true)
            }

            val matchesCategory = if (selectedCategory == null) true else {
                item.sourcePlatform.contains(selectedCategory!!, ignoreCase = true) ||
                item.category.name.contains(selectedCategory!!, ignoreCase = true) ||
                item.title.contains(selectedCategory!!, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppWhite)
            .padding(horizontal = 20.dp)
            .animateContentSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Heading (smoothly fades when search is actively focused)
        AnimatedVisibility(
            visible = !isSearchFocused,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            Column {
                Text(
                    text = "Search",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = AppBlack,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Expandable Animated Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(searchBarHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppInputBg)
                    .border(
                        width = if (isSearchFocused) 1.5.dp else 0.dp,
                        color = if (isSearchFocused) AppBlack else AppBorderGrey,
                        shape = RoundedCornerShape(16.dp)
                    )
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
                        tint = if (isSearchFocused) AppBlack else AppLightGrey,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = if (selectedCategory != null) "Search in $selectedCategory..." else "Search saves, links, notes...",
                                color = AppLightGrey,
                                fontSize = 14.sp
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    isSearchFocused = focusState.isFocused
                                }
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(22.dp)
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

            // Animated "Cancel" button sliding in when search bar is focused
            AnimatedVisibility(
                visible = isSearchFocused,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                Text(
                    text = "Cancel",
                    color = AppBlack,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            focusManager.clearFocus()
                            isSearchFocused = false
                            searchQuery = ""
                        }
                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                )
            }
        }

        // Active Category Filter Chip (if selected)
        if (selectedCategory != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppBlack)
                    .clickable { selectedCategory = null }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedCategory!!,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppWhite
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove filter",
                    tint = AppWhite,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Content Area: Search Results or Category Explore Grid
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // If user is searching or has a category selected: Show Results
            if (searchQuery.isNotBlank() || selectedCategory != null) {
                item {
                    Text(
                        text = if (filteredResults.isEmpty()) "Results" else "Results (${filteredResults.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppBlack,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (filteredResults.isEmpty()) {
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
                                    text = "No results found",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try searching for another term or category",
                                    fontSize = 13.sp,
                                    color = AppLightGrey,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredResults, key = { "search_" + it.id }) { item ->
                        SearchResultCard(
                            item = item,
                            onLongPress = { onItemLongPress(item) },
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
                }
            } else {
                // Default View: Categories Section (Books, Movies, Music, Games, Softwares, Images, etc.)
                item {
                    Text(
                        text = "Categories",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppBlack,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // 2-Column Grid of Categories
                    val chunked = categories.chunked(2)
                    chunked.forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (cat in rowCategories) {
                                Box(modifier = Modifier.weight(1f)) {
                                    CategoryTileCard(
                                        category = cat,
                                        count = savedItems.count {
                                            it.sourcePlatform.contains(cat.filterKey, ignoreCase = true) ||
                                            it.category.name.contains(cat.filterKey, ignoreCase = true)
                                        },
                                        onClick = { selectedCategory = cat.title }
                                    )
                                }
                            }
                            if (rowCategories.size == 1) {
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

@Composable
fun CategoryTileCard(
    category: SearchCategory,
    count: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppInputBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AppBlack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = AppWhite,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (count > 0) "$count saves" else "Explore",
                fontSize = 11.sp,
                color = AppLightGrey
            )
        }
    }
}

@Composable
fun SearchResultCard(
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
        Box(modifier = Modifier.size(50.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppInputBg),
                contentAlignment = Alignment.Center
            ) {
                if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = getCategoryIcon(item.category),
                        contentDescription = null,
                        tint = AppBlack,
                        modifier = Modifier.size(22.dp)
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
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
                        modifier = Modifier.size(13.dp)
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
