package com.mimo.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.app.R
import com.mimo.app.data.model.ItemCategory
import com.mimo.app.data.model.SaveItem
import com.mimo.app.data.network.LinkMetadataFetcher
import com.mimo.app.data.network.MimoServerPreview
import com.mimo.app.data.network.SupabaseSessionManager
import com.mimo.app.data.repository.SaveRepository
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite
import kotlinx.coroutines.launch
import java.util.UUID

enum class HubSheetMode {
    HUB_MAIN,
    INPUT_URL,
    INPUT_NOTE,
    INPUT_PHOTO,
    INPUT_COLLECTION,
    INPUT_REVIEW,
    INPUT_PRODUCT,
    INPUT_LOCATION,
    INPUT_MANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveHubBottomSheet(
    onDismiss: () -> Unit,
    onItemSaved: (SaveItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var currentMode by remember { mutableStateOf(HubSheetMode.HUB_MAIN) }
    var manualCategoryName by remember { mutableStateOf("") }
    var manualCategoryType by remember { mutableStateOf(ItemCategory.FILM) }

    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var pendingRetryItem by remember { mutableStateOf<SaveItem?>(null) }

    fun executeSave(item: SaveItem) {
        val currentUserId = SupabaseSessionManager.getUserId().orEmpty()
        if (currentUserId.isBlank()) {
            saveError = "Please sign in to save items to cloud"
            return
        }

        // Validate item
        if (item.title.isBlank() && item.url.isBlank() && item.noteContent.isNullOrBlank()) {
            saveError = "Please provide a title, link, or note content"
            return
        }

        scope.launch {
            isSaving = true
            saveError = null
            pendingRetryItem = item

            val result = SaveRepository.saveItem(item)
            isSaving = false

            if (result.isSuccess) {
                val savedItem = result.getOrNull() ?: item
                onItemSaved(savedItem)
                onDismiss()
            } else {
                saveError = result.exceptionOrNull()?.message ?: "Unable to save to cloud. Tap retry to attempt again."
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        sheetState = sheetState,
        containerColor = AppWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Live Cloud Save Progress Indicator
            if (isSaving) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppBlack,
                    trackColor = AppBorderGrey
                )
            }

            // Real Error Banner with Genuine Retry Option (No Fake Success)
            AnimatedVisibility(visible = saveError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF0F0))
                        .border(1.dp, AppAccentRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = saveError.orEmpty(),
                        color = AppAccentRed,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        lineHeight = 17.sp
                    )
                    if (pendingRetryItem != null) {
                        Text(
                            text = "Retry",
                            color = AppAccentRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    pendingRetryItem?.let { executeSave(it) }
                                }
                                .padding(start = 12.dp)
                        )
                    }
                }
            }

            when (currentMode) {
                HubSheetMode.HUB_MAIN -> {
                    SaveHubMainContent(
                        onSelectUrl = { currentMode = HubSheetMode.INPUT_URL },
                        onSelectNotes = { currentMode = HubSheetMode.INPUT_NOTE },
                        onSelectPhotos = { currentMode = HubSheetMode.INPUT_PHOTO },
                        onSelectCollections = { currentMode = HubSheetMode.INPUT_COLLECTION },
                        onSelectReview = { currentMode = HubSheetMode.INPUT_REVIEW },
                        onSelectProduct = { currentMode = HubSheetMode.INPUT_PRODUCT },
                        onSelectLocation = { currentMode = HubSheetMode.INPUT_LOCATION },
                        onSelectManualCategory = { name, cat ->
                            manualCategoryName = name
                            manualCategoryType = cat
                            currentMode = HubSheetMode.INPUT_MANUAL
                        }
                    )
                }

                HubSheetMode.INPUT_URL -> {
                    UrlSaveContent(
                        isSaving = isSaving,
                        onBack = { currentMode = HubSheetMode.HUB_MAIN },
                        onSave = { title, url, source, description, imageUrl, videoUrl ->
                            val newItem = SaveItem(
                                id = UUID.randomUUID().toString(),
                                title = title.ifBlank { url },
                                subtitle = description,
                                url = url,
                                imageUrl = imageUrl,
                                videoUrl = videoUrl,
                                category = ItemCategory.URL,
                                sourcePlatform = source,
                                dateAdded = "Just now"
                            )
                            executeSave(newItem)
                        }
                    )
                }

                HubSheetMode.INPUT_NOTE -> {
                    NoteSaveContent(
                        isSaving = isSaving,
                        onBack = { currentMode = HubSheetMode.HUB_MAIN },
                        onSave = { title, note ->
                            val newItem = SaveItem(
                                id = UUID.randomUUID().toString(),
                                title = title.ifBlank { "Untitled Note" },
                                noteContent = note,
                                category = ItemCategory.NOTE,
                                sourcePlatform = "Notes",
                                dateAdded = "Just now"
                            )
                            executeSave(newItem)
                        }
                    )
                }

                HubSheetMode.INPUT_PHOTO -> {
                    GenericSaveContent(
                        titleLabel = "Photo Title",
                        detailLabel = "Image URL or Caption",
                        categoryTitle = "Save Photo",
                        isSaving = isSaving,
                        onBack = { currentMode = HubSheetMode.HUB_MAIN },
                        onSave = { title, detail ->
                            val newItem = SaveItem(
                                id = UUID.randomUUID().toString(),
                                title = title.ifBlank { "Photo" },
                                subtitle = detail,
                                imageUrl = if (detail.startsWith("http://") || detail.startsWith("https://")) detail else null,
                                category = ItemCategory.PHOTO,
                                sourcePlatform = "Photos",
                                dateAdded = "Just now"
                            )
                            executeSave(newItem)
                        }
                    )
                }

                HubSheetMode.INPUT_COLLECTION -> {
                    GenericSaveContent(
                        titleLabel = "Collection Name",
                        detailLabel = "Description (Optional)",
                        categoryTitle = "New Collection",
                        isSaving = isSaving,
                        onBack = { currentMode = HubSheetMode.HUB_MAIN },
                        onSave = { title, detail ->
                            scope.launch {
                                isSaving = true
                                saveError = null
                                val colResult = SaveRepository.createCollection(title, detail)
                                if (colResult.isSuccess) {
                                    val col = colResult.getOrNull()!!
                                    val newItem = SaveItem(
                                        id = UUID.randomUUID().toString(),
                                        title = col.name,
                                        subtitle = col.description,
                                        category = ItemCategory.COLLECTION,
                                        sourcePlatform = "Collections",
                                        collectionId = col.id,
                                        dateAdded = "Just now"
                                    )
                                    executeSave(newItem)
                                } else {
                                    isSaving = false
                                    saveError = colResult.exceptionOrNull()?.message ?: "Failed to create collection"
                                }
                            }
                        }
                    )
                }

                HubSheetMode.INPUT_REVIEW -> {
                    GenericSaveContent(
                        titleLabel = "Item or Place",
                        detailLabel = "Your Review / Feedback",
                        categoryTitle = "Write Review",
                        isSaving = isSaving,
                        onBack = { currentMode = HubSheetMode.HUB_MAIN },
                        onSave = { title, detail ->
                            val newItem = SaveItem(
                                id = UUID.randomUUID().toString(),
                                title = title.ifBlank { "Review" },
                                subtitle = detail,
                                category = ItemCategory.REVIEW,
                                sourcePlatform = "Review",
                                dateAdded = "Just now"
                            )
                            executeSave(newItem)
                        }
                    )
                }

                HubSheetMode.INPUT_PRODUCT -> {
                    GenericSaveContent(
                        titleLabel = "Product Name",
                        detailLabel = "Product Link or Price",
                        categoryTitle = "Save Product",
                        isSaving = isSaving,
                        onBack = { currentMode = HubSheetMode.HUB_MAIN },
                        onSave = { title, detail ->
                            val newItem = SaveItem(
                                id = UUID.randomUUID().toString(),
                                title = title.ifBlank { "Product" },
                                url = detail,
                                category = ItemCategory.PRODUCT,
                                sourcePlatform = "Product",
                                dateAdded = "Just now"
                            )
                            executeSave(newItem)
                        }
                    )
                }

                HubSheetMode.INPUT_LOCATION -> {
                    LocationSaveContent(
                        isSaving = isSaving,
                        onBack = { currentMode = HubSheetMode.HUB_MAIN },
                        onSave = { placeName, address ->
                            val newItem = SaveItem(
                                id = UUID.randomUUID().toString(),
                                title = placeName.ifBlank { "Saved Location" },
                                subtitle = address,
                                noteContent = address,
                                category = ItemCategory.PLACE,
                                sourcePlatform = "Places",
                                dateAdded = "Just now"
                            )
                            executeSave(newItem)
                        }
                    )
                }

                HubSheetMode.INPUT_MANUAL -> {
                    GenericSaveContent(
                        titleLabel = "$manualCategoryName Title",
                        detailLabel = "Link, Author or Notes",
                        categoryTitle = "Add $manualCategoryName",
                        isSaving = isSaving,
                        onBack = { currentMode = HubSheetMode.HUB_MAIN },
                        onSave = { title, detail ->
                            val newItem = SaveItem(
                                id = UUID.randomUUID().toString(),
                                title = title.ifBlank { manualCategoryName },
                                subtitle = detail,
                                category = manualCategoryType,
                                sourcePlatform = manualCategoryName,
                                dateAdded = "Just now"
                            )
                            executeSave(newItem)
                        }
                    )
                }
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
    onSelectLocation: () -> Unit,
    onSelectManualCategory: (String, ItemCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Grid of Action Boxes with Authentic 3D Category Assets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    drawableRes = R.drawable.ic_cat_link,
                    title = "Paste any URL",
                    subtitle = "Articles, TikTok, Instagram & more",
                    onClick = onSelectUrl
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    drawableRes = R.drawable.ic_cat_note,
                    title = "Notes",
                    subtitle = "Thoughts, ideas & drafts",
                    onClick = onSelectNotes
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    drawableRes = R.drawable.ic_cat_photos,
                    title = "Photos",
                    subtitle = "Images, screenshots & media",
                    onClick = onSelectPhotos
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    drawableRes = R.drawable.ic_cat_collection,
                    title = "Collections",
                    subtitle = "Folders & grouped items",
                    onClick = onSelectCollections
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    drawableRes = R.drawable.ic_cat_review,
                    title = "Review",
                    subtitle = "Ratings & feedback",
                    onClick = onSelectReview
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    drawableRes = R.drawable.ic_cat_products,
                    title = "Product",
                    subtitle = "Items, links & shopping",
                    onClick = onSelectProduct
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SaveActionBox(
                    drawableRes = R.drawable.ic_cat_places,
                    title = "Location",
                    subtitle = "Places, pins & spots",
                    onClick = onSelectLocation
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Divider
        HorizontalDivider(thickness = 0.5.dp, color = AppBorderGrey)

        Spacer(modifier = Modifier.height(24.dp))

        // Manual Categories
        Text(
            text = "Categories",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AppLightGrey,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ManualCategoryChip("Films", ItemCategory.FILM) { onSelectManualCategory("Films", ItemCategory.FILM) }
            ManualCategoryChip("TV Shows", ItemCategory.TV_SHOW) { onSelectManualCategory("TV Shows", ItemCategory.TV_SHOW) }
            ManualCategoryChip("Music", ItemCategory.MUSIC) { onSelectManualCategory("Music", ItemCategory.MUSIC) }
            ManualCategoryChip("Books", ItemCategory.BOOK) { onSelectManualCategory("Books", ItemCategory.BOOK) }
            ManualCategoryChip("Recipes", ItemCategory.RECIPE) { onSelectManualCategory("Recipes", ItemCategory.RECIPE) }
            ManualCategoryChip("Software", ItemCategory.SOFTWARE) { onSelectManualCategory("Software", ItemCategory.SOFTWARE) }
            ManualCategoryChip("Tutorials", ItemCategory.TUTORIAL) { onSelectManualCategory("Tutorials", ItemCategory.TUTORIAL) }
        }
    }
}

@Composable
fun SaveActionBox(
    drawableRes: Int? = null,
    icon: ImageVector? = null,
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
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = title,
                modifier = Modifier.size(38.dp),
                contentScale = ContentScale.Fit
            )
        } else if (icon != null) {
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
    category: ItemCategory,
    onClick: () -> Unit
) {
    val theme = getCategoryTheme(category)
    val catDrawable = getCategoryDrawableRes(category)
    val isDark = com.mimo.app.ui.theme.ThemeManager.isDark

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDark) theme.containerDark else theme.containerLight)
            .border(
                1.dp,
                theme.accentColor.copy(alpha = if (isDark) 0.40f else 0.25f),
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = catDrawable),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) AppWhite else AppBlack
        )
    }
}

@Composable
fun UrlSaveContent(
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: (String, String, String, String, String?, String?) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
            text = "Articles, TikTok, Instagram & more",
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(if (isFetching || isSaving || urlInput.isBlank()) AppLightGrey else AppBlack)
                .clickable(enabled = !isFetching && !isSaving && urlInput.isNotBlank()) {
                    isFetching = true
                    scope.launch {
                        try {
                            val rawUrl = urlInput.trim()
                            val url = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) rawUrl else "https://$rawUrl"
                            val host = runCatching { java.net.URI(url).host?.lowercase() }.getOrNull()
                            val metadata = if (host in setOf("youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be")) {
                                // YouTube saves keep the original link. Do not fetch its metadata or stream URL.
                                com.mimo.app.data.network.ParsedMetadata("YouTube", "", null, platform = "YouTube", canonicalUrl = url)
                            } else MimoServerPreview.fetch(url) ?: LinkMetadataFetcher.fetch(url)
                            val finalTitle = titleInput.ifBlank { metadata.title }
                            onSave(
                                finalTitle,
                                metadata.canonicalUrl,
                                metadata.platform,
                                metadata.description,
                                metadata.imageUrl,
                                metadata.videoUrl
                            )
                        } catch (_: Exception) {
                            val finalTitle = titleInput.ifBlank { urlInput.trim() }
                            onSave(finalTitle, urlInput.trim(), "Web", "", null, null)
                        } finally {
                            isFetching = false
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isFetching || isSaving) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isFetching) "Fetching..." else "Saving...",
                        color = AppWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = "Save Link",
                    color = AppWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun NoteSaveContent(
    isSaving: Boolean,
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
            text = if (isSaving) "Saving..." else "Save Note",
            onClick = {
                if (noteTitle.isNotBlank() || noteBody.isNotBlank()) {
                    onSave(noteTitle, noteBody)
                }
            }
        )
    }
}

@Composable
fun LocationSaveContent(
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var placeName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(
            text = "Save Location",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppBlack
        )

        Spacer(modifier = Modifier.height(18.dp))

        AppInputField(
            label = "Place Name",
            value = placeName,
            onValueChange = { placeName = it },
            placeholder = "e.g. Favorite Cafe, Studio..."
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppInputField(
            label = "Address or Notes",
            value = address,
            onValueChange = { address = it },
            placeholder = "Enter street, city or coordinates..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryPillButton(
            text = if (isSaving) "Saving..." else "Save Location",
            onClick = {
                if (placeName.isNotBlank() || address.isNotBlank()) {
                    onSave(placeName, address)
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
    isSaving: Boolean = false,
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
            text = if (isSaving) "Saving..." else "Save",
            onClick = {
                if (title.isNotBlank() || detail.isNotBlank()) {
                    onSave(title, detail)
                }
            }
        )
    }
}
