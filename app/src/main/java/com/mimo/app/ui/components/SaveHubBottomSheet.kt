package com.mimo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.app.data.model.ItemCategory
import com.mimo.app.data.model.SaveItem
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite

enum class HubSheetMode {
    HUB_MAIN,
    INPUT_URL,
    INPUT_NOTE,
    INPUT_PHOTO,
    INPUT_COLLECTION,
    INPUT_REVIEW,
    INPUT_PRODUCT,
    INPUT_MANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveHubBottomSheet(
    onDismiss: () -> Unit,
    onItemSaved: (SaveItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentMode by remember { mutableStateOf(HubSheetMode.HUB_MAIN) }
    var manualCategoryName by remember { mutableStateOf("") }
    var manualCategoryType by remember { mutableStateOf(ItemCategory.FILM) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        when (currentMode) {
            HubSheetMode.HUB_MAIN -> {
                SaveHubMainContent(
                    onSelectUrl = { currentMode = HubSheetMode.INPUT_URL },
                    onSelectNotes = { currentMode = HubSheetMode.INPUT_NOTE },
                    onSelectPhotos = { currentMode = HubSheetMode.INPUT_PHOTO },
                    onSelectCollections = { currentMode = HubSheetMode.INPUT_COLLECTION },
                    onSelectReview = { currentMode = HubSheetMode.INPUT_REVIEW },
                    onSelectProduct = { currentMode = HubSheetMode.INPUT_PRODUCT },
                    onSelectManualCategory = { name, cat ->
                        manualCategoryName = name
                        manualCategoryType = cat
                        currentMode = HubSheetMode.INPUT_MANUAL
                    }
                )
            }

            HubSheetMode.INPUT_URL -> {
                UrlSaveContent(
                    onBack = { currentMode = HubSheetMode.HUB_MAIN },
                    onSave = { title, url, source ->
                        val newItem = SaveItem(
                            id = System.currentTimeMillis().toString(),
                            title = title.ifBlank { url },
                            url = url,
                            category = ItemCategory.URL,
                            sourcePlatform = source,
                            dateAdded = "Just now"
                        )
                        onItemSaved(newItem)
                        onDismiss()
                    }
                )
            }

            HubSheetMode.INPUT_NOTE -> {
                NoteSaveContent(
                    onBack = { currentMode = HubSheetMode.HUB_MAIN },
                    onSave = { title, note ->
                        val newItem = SaveItem(
                            id = System.currentTimeMillis().toString(),
                            title = title.ifBlank { "Untitled Note" },
                            noteContent = note,
                            category = ItemCategory.NOTE,
                            sourcePlatform = "Notes",
                            dateAdded = "Just now"
                        )
                        onItemSaved(newItem)
                        onDismiss()
                    }
                )
            }

            HubSheetMode.INPUT_PHOTO -> {
                GenericSaveContent(
                    titleLabel = "Photo Title",
                    detailLabel = "Image URL or Caption",
                    categoryTitle = "Save Photo",
                    onBack = { currentMode = HubSheetMode.HUB_MAIN },
                    onSave = { title, detail ->
                        val newItem = SaveItem(
                            id = System.currentTimeMillis().toString(),
                            title = title.ifBlank { "Photo" },
                            subtitle = detail,
                            category = ItemCategory.PHOTO,
                            sourcePlatform = "Photos",
                            dateAdded = "Just now"
                        )
                        onItemSaved(newItem)
                        onDismiss()
                    }
                )
            }

            HubSheetMode.INPUT_COLLECTION -> {
                GenericSaveContent(
                    titleLabel = "Collection Name",
                    detailLabel = "Description",
                    categoryTitle = "New Collection",
                    onBack = { currentMode = HubSheetMode.HUB_MAIN },
                    onSave = { title, detail ->
                        val newItem = SaveItem(
                            id = System.currentTimeMillis().toString(),
                            title = title.ifBlank { "Collection" },
                            subtitle = detail,
                            category = ItemCategory.COLLECTION,
                            sourcePlatform = "Collections",
                            dateAdded = "Just now"
                        )
                        onItemSaved(newItem)
                        onDismiss()
                    }
                )
            }

            HubSheetMode.INPUT_REVIEW -> {
                GenericSaveContent(
                    titleLabel = "Item or Place",
                    detailLabel = "Your Review / Feedback",
                    categoryTitle = "Write Review",
                    onBack = { currentMode = HubSheetMode.HUB_MAIN },
                    onSave = { title, detail ->
                        val newItem = SaveItem(
                            id = System.currentTimeMillis().toString(),
                            title = title.ifBlank { "Review" },
                            subtitle = detail,
                            category = ItemCategory.REVIEW,
                            sourcePlatform = "Review",
                            dateAdded = "Just now"
                        )
                        onItemSaved(newItem)
                        onDismiss()
                    }
                )
            }

            HubSheetMode.INPUT_PRODUCT -> {
                GenericSaveContent(
                    titleLabel = "Product Name",
                    detailLabel = "Product Link or Price",
                    categoryTitle = "Save Product",
                    onBack = { currentMode = HubSheetMode.HUB_MAIN },
                    onSave = { title, detail ->
                        val newItem = SaveItem(
                            id = System.currentTimeMillis().toString(),
                            title = title.ifBlank { "Product" },
                            url = detail,
                            category = ItemCategory.PRODUCT,
                            sourcePlatform = "Product",
                            dateAdded = "Just now"
                        )
                        onItemSaved(newItem)
                        onDismiss()
                    }
                )
            }

            HubSheetMode.INPUT_MANUAL -> {
                GenericSaveContent(
                    titleLabel = "$manualCategoryName Title",
                    detailLabel = "Link, Author or Notes",
                    categoryTitle = "Add $manualCategoryName",
                    onBack = { currentMode = HubSheetMode.HUB_MAIN },
                    onSave = { title, detail ->
                        val newItem = SaveItem(
                            id = System.currentTimeMillis().toString(),
                            title = title.ifBlank { manualCategoryName },
                            subtitle = detail,
                            category = manualCategoryType,
                            sourcePlatform = manualCategoryName,
                            dateAdded = "Just now"
                        )
                        onItemSaved(newItem)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaveHubMainContent(
    onSelectUrl: () -> Unit,
    onSelectNotes: () -> Unit,
    onSelectPhotos: () -> Unit,
    onSelectCollections: () -> Unit,
    onSelectReview: () -> Unit,
    onSelectProduct: () -> Unit,
    onSelectManualCategory: (String, ItemCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 2-Column Grid of 6 Action Boxes
        // Row 1: Paste any URL & Notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    icon = Icons.Default.Link,
                    title = "Paste any URL",
                    subtitle = "Articles, blogs, TikTok, Instagram & more",
                    onClick = onSelectUrl
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    icon = Icons.Default.Description,
                    title = "Notes",
                    subtitle = "Thoughts, ideas & drafts",
                    onClick = onSelectNotes
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Photos & Collections
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    icon = Icons.Default.CameraAlt,
                    title = "Photos",
                    subtitle = "Images, screenshots & media",
                    onClick = onSelectPhotos
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    icon = Icons.Default.Bookmark,
                    title = "Collections",
                    subtitle = "Folders & grouped items",
                    onClick = onSelectCollections
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: Review & Product
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    icon = Icons.Default.RateReview,
                    title = "Review",
                    subtitle = "Ratings, thoughts & feedback",
                    onClick = onSelectReview
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    icon = Icons.Default.ShoppingBag,
                    title = "Product",
                    subtitle = "Items, links & shopping",
                    onClick = onSelectProduct
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Divider & Heading: OR MANUALLY SEARCH THESE
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = AppBorderGrey)
            Text(
                text = "OR MANUALLY SEARCH THESE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AppLightGrey,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = AppBorderGrey)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Manual Categories: Films, Software, Products, TV Shows, Tutorials
        val manualCategories = listOf(
            Triple("Films", Icons.Default.Movie, ItemCategory.FILM),
            Triple("Software", Icons.Default.Code, ItemCategory.SOFTWARE),
            Triple("Products", Icons.Default.ShoppingBag, ItemCategory.PRODUCT),
            Triple("TV Shows", Icons.Default.Tv, ItemCategory.TV_SHOW),
            Triple("Tutorials", Icons.Default.School, ItemCategory.TUTORIAL)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            manualCategories.forEach { (name, icon, cat) ->
                ManualCategoryChip(
                    name = name,
                    icon = icon,
                    onClick = { onSelectManualCategory(name, cat) }
                )
            }
        }
    }
}

@Composable
fun SaveActionBox(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppInputBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AppBlack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppWhite,
                modifier = Modifier.size(18.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppBlack,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = AppLightGrey,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun ManualCategoryChip(
    name: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AppWhite)
            .border(1.dp, AppBorderGrey, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppBlack,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppBlack
        )
    }
}

@Composable
fun UrlSaveContent(
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }

    // Automatic platform detection from link
    val detectedSource = remember(urlInput) {
        val lower = urlInput.lowercase()
        when {
            lower.contains("instagram.com") || lower.contains("instagr.am") -> "Instagram"
            lower.contains("tiktok.com") -> "TikTok"
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "YouTube"
            lower.contains("twitter.com") || lower.contains("x.com") -> "X"
            else -> "Web"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(
            text = "Paste any URL",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppBlack
        )
        Text(
            text = "Articles, blogs, TikTok, Instagram & more",
            fontSize = 13.sp,
            color = AppLightGrey
        )

        Spacer(modifier = Modifier.height(20.dp))

        AppInputField(
            label = "Link URL",
            value = urlInput,
            onValueChange = { urlInput = it },
            placeholder = "Paste your link here..."
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppInputField(
            label = "Custom Title (Optional)",
            value = titleInput,
            onValueChange = { titleInput = it },
            placeholder = "Add title or leave blank to auto-fetch"
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryPillButton(
            text = "Save Link",
            onClick = {
                if (urlInput.isNotBlank()) {
                    val finalTitle = titleInput.ifBlank {
                        if (detectedSource != "Web") "$detectedSource Post" else urlInput
                    }
                    onSave(finalTitle, urlInput.trim(), detectedSource)
                }
            }
        )
    }
}

@Composable
fun NoteSaveContent(
    onBack: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var noteTitle by remember { mutableStateOf("") }
    var noteBody by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(
            text = "New Note",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppBlack
        )

        Spacer(modifier = Modifier.height(18.dp))

        AppInputField(
            label = "Title",
            value = noteTitle,
            onValueChange = { noteTitle = it },
            placeholder = "Note title..."
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppInputField(
            label = "Content",
            value = noteBody,
            onValueChange = { noteBody = it },
            placeholder = "Write your thoughts here..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryPillButton(
            text = "Save Note",
            onClick = {
                if (noteTitle.isNotBlank() || noteBody.isNotBlank()) {
                    onSave(noteTitle, noteBody)
                }
            }
        )
    }
}

@Composable
fun GenericSaveContent(
    titleLabel: String,
    detailLabel: String,
    categoryTitle: String,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(
            text = categoryTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppBlack
        )

        Spacer(modifier = Modifier.height(18.dp))

        AppInputField(
            label = titleLabel,
            value = title,
            onValueChange = { title = it },
            placeholder = "Enter $titleLabel..."
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppInputField(
            label = detailLabel,
            value = detail,
            onValueChange = { detail = it },
            placeholder = "Enter details..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryPillButton(
            text = "Save",
            onClick = {
                if (title.isNotBlank() || detail.isNotBlank()) {
                    onSave(title, detail)
                }
            }
        )
    }
}
