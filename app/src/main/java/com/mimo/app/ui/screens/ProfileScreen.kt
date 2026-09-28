package com.mimo.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.json.JSONArray
import org.json.JSONObject
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.mimo.app.data.network.GoogleDriveService
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mimo.app.data.model.ItemCategory
import com.mimo.app.data.model.SaveItem
import com.mimo.app.ui.components.CategoryBadgeIcon
import com.mimo.app.ui.components.getCategoryIcon
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite
import com.mimo.app.ui.theme.ThemeManager
import com.mimo.app.data.local.AppPreferences

data class ProfileData(
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val websiteUrl: String = "",
    val avatarUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    savedItems: List<SaveItem>,
    modifier: Modifier = Modifier,
    onItemLongPress: (SaveItem) -> Unit = {},
    onItemClick: (SaveItem) -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current

    // Mutable profile state wired to real Supabase session data
    val sessionEmail = com.mimo.app.data.network.SupabaseSessionManager.currentUserEmail
    val sessionName = com.mimo.app.data.network.SupabaseSessionManager.currentUserName
    var profile by remember {
        mutableStateOf(
            ProfileData(
                displayName = sessionName ?: sessionEmail?.substringBefore('@')?.replaceFirstChar { it.uppercase() } ?: "",
                username = sessionEmail?.substringBefore('@') ?: ""
            )
        )
    }

    // Bottom sheet states
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }

    // Active tab in Profile (0: Saves Grid, 1: Favorites, 2: Collections)
    var selectedProfileTab by remember { mutableIntStateOf(0) }

    val favorites = remember(savedItems) {
        savedItems.filter { it.isFavorite }
    }

    val collections = remember(savedItems) {
        savedItems.filter { it.category == ItemCategory.COLLECTION }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppWhite)
    ) {
        // 1. Top Bar: Username on Left, Notification & Settings Icons on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AppBlack,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = profile.username.ifBlank { "Profile" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Notifications Button
                IconButton(
                    onClick = { showNotificationsSheet = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = AppBlack,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Settings Button
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = AppBlack,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }

        // Profile Content Scrollable List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Header: Avatar + Counts
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Picture Avatar
                    Box(
                        modifier = Modifier.size(86.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, AppBorderGrey, CircleShape)
                                .background(AppInputBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profile.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile.avatarUrl,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AppBlack,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }

                        // Edit Avatar small badge button
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(AppBlack)
                                .border(2.dp, AppWhite, CircleShape)
                                .clickable { showEditProfileSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = AppWhite,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Stats row: Saves, Favorites, Collections
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStatColumn(
                            count = savedItems.size.toString(),
                            label = "Saves"
                        )
                        ProfileStatColumn(
                            count = favorites.size.toString(),
                            label = "Favorites"
                        )
                        ProfileStatColumn(
                            count = collections.size.toString(),
                            label = "Collections"
                        )
                    }
                }
            }

            // Bio & Details Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    // Display Name
                    if (profile.displayName.isNotBlank()) {
                        Text(
                            text = profile.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppBlack
                        )
                    }

                    // Bio Text
                    if (profile.bio.isNotBlank()) {
                        if (profile.displayName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = profile.bio,
                            fontSize = 13.sp,
                            color = AppBlack,
                            lineHeight = 18.sp
                        )
                    }

                    // Website / Social URL in Bio
                    if (profile.websiteUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val linkColor = if (ThemeManager.isDark) Color(0xFF64B5F6) else Color(0xFF0066CC)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                try {
                                    val formattedUrl = if (!profile.websiteUrl.startsWith("http://") && !profile.websiteUrl.startsWith("https://")) {
                                        "https://${profile.websiteUrl}"
                                    } else {
                                        profile.websiteUrl
                                    }
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)))
                                } catch (_: Exception) {}
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Link",
                                tint = linkColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = profile.websiteUrl.removePrefix("https://").removePrefix("http://"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = linkColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Action Buttons: "Edit Profile" and "Share Profile"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Edit Profile Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppInputBg)
                            .border(1.dp, AppBorderGrey, RoundedCornerShape(10.dp))
                            .clickable { showEditProfileSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Edit Profile",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppBlack
                        )
                    }

                    // Share Profile Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppInputBg)
                            .border(1.dp, AppBorderGrey, RoundedCornerShape(10.dp))
                            .clickable {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Check out my curated saves on Mimo: @${profile.username}\n${profile.websiteUrl}"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Profile"))
                                } catch (_: Exception) {}
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Share Profile",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppBlack
                        )
                    }
                }
            }

            // Instagram-Style Content Tabs: Grid (Saves), Heart (Favorites), Folder (Collections)
            item {
                TabRow(
                    selectedTabIndex = selectedProfileTab,
                    containerColor = AppWhite,
                    contentColor = AppBlack,
                    indicator = { tabPositions ->
                        if (selectedProfileTab < tabPositions.size) {
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedProfileTab])
                                    .height(2.dp)
                                    .background(AppBlack)
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(thickness = 0.5.dp, color = AppBorderGrey)
                    }
                ) {
                    Tab(
                        selected = selectedProfileTab == 0,
                        onClick = { selectedProfileTab = 0 },
                        icon = {
                            Icon(
                                imageVector = if (selectedProfileTab == 0) Icons.Filled.GridOn else Icons.Outlined.GridOn,
                                contentDescription = "All Saves",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                    Tab(
                        selected = selectedProfileTab == 1,
                        onClick = { selectedProfileTab = 1 },
                        icon = {
                            Icon(
                                imageVector = if (selectedProfileTab == 1) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Favorites",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                    Tab(
                        selected = selectedProfileTab == 2,
                        onClick = { selectedProfileTab = 2 },
                        icon = {
                            Icon(
                                imageVector = if (selectedProfileTab == 2) Icons.Filled.Folder else Icons.Outlined.Folder,
                                contentDescription = "Collections",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                }
            }

            // Grid Content for Selected Tab
            val currentList = when (selectedProfileTab) {
                1 -> favorites
                2 -> collections
                else -> savedItems
            }

            if (currentList.isEmpty()) {
                item {
                    val emptyTitle = when (selectedProfileTab) {
                        1 -> "No Favorites Yet"
                        2 -> "No Collections Yet"
                        else -> "No Saves Yet"
                    }
                    val emptySubtitle = when (selectedProfileTab) {
                        1 -> "Items you mark as favorite will show up here"
                        2 -> "Create collections from the add hub"
                        else -> "Save links, notes, articles and photos"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp, bottom = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 30.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(AppInputBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (selectedProfileTab) {
                                        1 -> Icons.Filled.Bookmark
                                        2 -> Icons.Filled.Folder
                                        else -> Icons.Filled.GridOn
                                    },
                                    contentDescription = null,
                                    tint = AppLightGrey,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = emptyTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = emptySubtitle,
                                fontSize = 13.sp,
                                color = AppLightGrey,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Instagram 3-column square grid
                val chunkedItems = currentList.chunked(3)
                items(chunkedItems.size) { rowIndex ->
                    val rowItems = chunkedItems[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (item in rowItems) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProfileGridSquare(
                                    item = item,
                                    onLongPress = { onItemLongPress(item) },
                                    onClick = { onItemClick(item) }
                                )
                            }
                        }
                        // Fill empty slots in the row
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // 2. Edit Profile Modal Bottom Sheet
    if (showEditProfileSheet) {
        EditProfileBottomSheet(
            currentProfile = profile,
            onDismiss = { showEditProfileSheet = false },
            onSave = { updated ->
                profile = updated
                showEditProfileSheet = false
            }
        )
    }

    // 3. Settings Modal Bottom Sheet (Categorized according to Rule 3)
    if (showSettingsSheet) {
        SettingsBottomSheet(
            savedItems = savedItems,
            onDismiss = { showSettingsSheet = false },
            onSignOut = {
                showSettingsSheet = false
                onSignOut()
            }
        )
    }

    // 4. Notifications Modal Bottom Sheet
    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            onDismiss = { showNotificationsSheet = false }
        )
    }
}

// Profile Stat Item Column
@Composable
private fun ProfileStatColumn(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppBlack
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppLightGrey
        )
    }
}

// Instagram-Style Square Grid Item
@Composable
fun ProfileGridSquare(
    item: SaveItem,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(AppInputBg)
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (!item.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = getCategoryIcon(item.category),
                contentDescription = null,
                tint = AppLightGrey,
                modifier = Modifier.size(28.dp)
            )
        }

        // Small category badge overlay
        CategoryBadgeIcon(
            category = item.category,
            size = 18,
            iconSize = 10,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        )
    }
}

// Edit Profile Bottom Sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    currentProfile: ProfileData,
    onDismiss: () -> Unit,
    onSave: (ProfileData) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(currentProfile.displayName) }
    var username by remember { mutableStateOf(currentProfile.username) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var websiteUrl by remember { mutableStateOf(currentProfile.websiteUrl) }
    var avatarUrl by remember { mutableStateOf(currentProfile.avatarUrl ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header Row: Cancel and Done
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 15.sp,
                    color = AppLightGrey,
                    modifier = Modifier.clickable { onDismiss() }
                )

                Text(
                    text = "Edit Profile",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBlack
                )

                Text(
                    text = "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBlack,
                    modifier = Modifier.clickable {
                        onSave(
                            currentProfile.copy(
                                displayName = name.trim(),
                                username = username.trim(),
                                bio = bio.trim(),
                                websiteUrl = websiteUrl.trim(),
                                avatarUrl = avatarUrl.trim().ifEmpty { null }
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Picture Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AppInputBg)
                        .border(1.dp, AppBorderGrey, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AppBlack,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Fields
            ProfileInputField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "Your Name"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileInputField(
                label = "Username",
                value = username,
                onValueChange = { username = it },
                placeholder = "username"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileInputField(
                label = "Bio",
                value = bio,
                onValueChange = { bio = it },
                placeholder = "Write a bio...",
                singleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileInputField(
                label = "Links & Social",
                value = websiteUrl,
                onValueChange = { websiteUrl = it },
                placeholder = "e.g. instagram.com/username or your website"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileInputField(
                label = "Avatar Image URL",
                value = avatarUrl,
                onValueChange = { avatarUrl = it },
                placeholder = "https://example.com/avatar.jpg"
            )
        }
    }
}

// Clean Input Field for Profile Editing
@Composable
private fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppLightGrey
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppInputBg)
                .border(1.dp, AppBorderGrey, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = if (singleLine) 12.dp else 10.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = AppLightGrey,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                minLines = minLines,
                cursorBrush = SolidColor(AppBlack),
                textStyle = TextStyle(
                    color = AppBlack,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Settings Bottom Sheet Categorized strictly by Rule 3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    savedItems: List<SaveItem>,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var page by remember { mutableStateOf("Settings") }
    var showLegalNotice by remember { mutableStateOf<String?>(null) }
    var exportContent by remember { mutableStateOf("") }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(exportContent.toByteArray(Charsets.UTF_8))
                } ?: error("Could not open export file")
            }.onFailure { showLegalNotice = "Export failed" }
        }
    }

    if (showLegalNotice != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLegalNotice = null },
            title = { Text(showLegalNotice.orEmpty()) },
            text = { Text(if (showLegalNotice == "Export failed")
                "Could not write the file. Your saves have not changed."
                else "This document has not been published yet. No legal terms are being represented here.") },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { showLegalNotice = null }) { Text("OK") } }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = AppWhite, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 36.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(page, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppBlack)
                IconButton(onClick = { if (page == "Settings") onDismiss() else page = "Settings" }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = AppBlack)
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                when (page) {
                    "Archived saves" -> {
                        val archived = savedItems.filter { it.isArchived }
                        if (archived.isEmpty()) item { Text("No archived saves", color = AppLightGrey) }
                        items(archived.size) { index ->
                            val save = archived[index]
                            SettingsSection("${index + 1}") {
                                SettingsRow(save.title, value = save.category.name.lowercase())
                            }
                        }
                    }
                    "Appearance" -> item {
                        SettingsSection("Appearance") {
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dark mode", color = AppBlack)
                                Switch(checked = ThemeManager.isDark,
                                    onCheckedChange = { ThemeManager.isDark = it; AppPreferences.setDarkTheme(it) })
                            }
                        }
                    }
                    else -> {
                        item {
                            SettingsSection("General") {
                                SettingsRow("Archived saves", onClick = { page = "Archived saves" })
                                SettingsRow("Appearance", value = if (ThemeManager.isDark) "Dark" else "Light",
                                    onClick = { page = "Appearance" })
                            }
                        }
                        item {
                            SettingsSection("Resources") {
                                SettingsRow("Export your saves", onClick = {
                                    val export = JSONArray()
                                    savedItems.forEach { save ->
                                        export.put(JSONObject().apply {
                                            put("title", save.title)
                                            put("description", save.subtitle)
                                            put("url", save.url)
                                            put("type", save.category.name)
                                            put("note", save.noteContent ?: "")
                                            put("archived", save.isArchived)
                                        })
                                    }
                                    exportContent = export.toString(2)
                                    exportLauncher.launch("mimo-saves.json")
                                })
                            }
                        }
                        item {
                            SettingsSection("Storage & Data") {
                                SettingsRow("Google Drive", value = "Connection not verified")
                            }
                        }
                        item {
                            SettingsSection("Helpful links") {
                                SettingsRow("Terms of Service", value = "Not published",
                                    onClick = { showLegalNotice = "Terms of Service" })
                                SettingsRow("Privacy Policy", value = "Not published",
                                    onClick = { showLegalNotice = "Privacy Policy" })
                            }
                        }
                        item {
                            Text("Data sources: Movie of the Night, Wikidata, Wikipedia, YouTube",
                                fontSize = 11.sp, color = AppLightGrey)
                            Spacer(Modifier.height(12.dp))
                            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                                .background(AppInputBg).clickable { onSignOut() },
                                contentAlignment = Alignment.Center) {
                                Text("Sign Out", fontSize = 14.sp, color = AppAccentRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Section wrapper for settings groups
@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AppLightGrey,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppInputBg)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

// Settings Row with label and optional value or chevron
@Composable
private fun SettingsRow(
    label: String,
    value: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = AppBlack
        )
        if (value != null) {
            Text(
                text = value,
                fontSize = 13.sp,
                color = AppLightGrey
            )
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = AppLightGrey,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// Notifications Bottom Sheet with genuine empty state
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBlack
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AppBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Genuine empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AppInputBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = AppLightGrey,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Notifications",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You're all caught up with your saves and activity",
                        fontSize = 13.sp,
                        color = AppLightGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
