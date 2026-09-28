package com.mimo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mimo.app.data.model.ItemCategory
import com.mimo.app.data.model.SaveItem
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppCardBg
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite

/** Owned saved photos only. No device gallery access or automatic upload. */
@Composable
fun PhotoGalleryScreen(
    savedItems: List<SaveItem>,
    onBack: () -> Unit,
    onOpen: (SaveItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val photos = savedItems.filter { it.category == ItemCategory.PHOTO && !it.isArchived }
        .sortedByDescending { it.createdAt }
    Column(modifier = modifier.fillMaxSize().background(AppWhite)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to library", tint = AppBlack)
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text("Photos", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = AppBlack)
                Text("${photos.size} saved", fontSize = 13.sp, color = AppLightGrey)
            }
        }
        if (photos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 110.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AppLightGrey, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No saved photos yet", color = AppBlack, fontWeight = FontWeight.SemiBold)
                    Text("Your photo saves will appear here", color = AppLightGrey, fontSize = 13.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    Column(modifier = Modifier.clickable { onOpen(photo) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.82f)
                                .clip(RoundedCornerShape(18.dp)).background(AppInputBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!photo.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = photo.imageUrl,
                                    contentDescription = photo.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AppLightGrey, modifier = Modifier.size(38.dp))
                            }
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(photo.title, color = AppBlack, fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(photo.sourcePlatform, color = AppLightGrey, fontSize = 12.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
