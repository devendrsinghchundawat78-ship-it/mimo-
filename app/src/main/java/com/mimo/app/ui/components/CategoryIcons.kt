package com.mimo.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tv
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.app.R
import com.mimo.app.data.model.ItemCategory
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppCardBg
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite
import com.mimo.app.ui.theme.ThemeManager

data class CategoryThemeData(
    val category: ItemCategory,
    val label: String,
    val icon: ImageVector,
    val accentColor: Color,
    val containerLight: Color,
    val containerDark: Color
)

fun getCategoryIcon(category: ItemCategory): ImageVector {
    return when (category) {
        ItemCategory.PLACE -> Icons.Default.Place
        ItemCategory.SOFTWARE -> Icons.Default.Code
        ItemCategory.FILM -> Icons.Default.Movie
        ItemCategory.PRODUCT -> Icons.Default.ShoppingBag
        ItemCategory.RECIPE -> Icons.Default.Restaurant
        ItemCategory.BOOK -> Icons.Default.MenuBook
        ItemCategory.TUTORIAL -> Icons.Default.School
        ItemCategory.NOTE -> Icons.Default.Description
        ItemCategory.PHOTO -> Icons.Default.CameraAlt
        ItemCategory.COLLECTION -> Icons.Default.Bookmark
        ItemCategory.URL -> Icons.Default.Link
        ItemCategory.REVIEW -> Icons.Default.RateReview
        ItemCategory.TV_SHOW -> Icons.Default.Tv
        ItemCategory.MUSIC -> Icons.Default.MusicNote
    }
}

/**
 * Returns the authentic 3D rendered PNG drawable resource for any category.
 */
fun getCategoryDrawableRes(category: ItemCategory): Int {
    return when (category) {
        ItemCategory.PLACE -> R.drawable.ic_cat_places
        ItemCategory.SOFTWARE -> R.drawable.ic_cat_software
        ItemCategory.FILM -> R.drawable.ic_cat_films
        ItemCategory.PRODUCT -> R.drawable.ic_cat_products
        ItemCategory.RECIPE -> R.drawable.ic_cat_recipes
        ItemCategory.BOOK -> R.drawable.ic_cat_books
        ItemCategory.TUTORIAL -> R.drawable.ic_cat_tutorials
        ItemCategory.NOTE -> R.drawable.ic_cat_note
        ItemCategory.PHOTO -> R.drawable.ic_cat_photos
        ItemCategory.COLLECTION -> R.drawable.ic_cat_collection
        ItemCategory.URL -> R.drawable.ic_cat_link
        ItemCategory.REVIEW -> R.drawable.ic_cat_review
        ItemCategory.TV_SHOW -> R.drawable.ic_cat_tv
        ItemCategory.MUSIC -> R.drawable.ic_cat_music
    }
}

fun getCategoryTheme(category: ItemCategory): CategoryThemeData {
    return when (category) {
        ItemCategory.PLACE -> CategoryThemeData(
            category = category,
            label = "Places",
            icon = Icons.Default.Place,
            accentColor = Color(0xFF007AFF),
            containerLight = Color(0xFFE5F1FF),
            containerDark = Color(0xFF152A42)
        )
        ItemCategory.SOFTWARE -> CategoryThemeData(
            category = category,
            label = "Software",
            icon = Icons.Default.Code,
            accentColor = Color(0xFF8B5CF6),
            containerLight = Color(0xFFF3EEFF),
            containerDark = Color(0xFF2C1E4A)
        )
        ItemCategory.FILM -> CategoryThemeData(
            category = category,
            label = "Films",
            icon = Icons.Default.Movie,
            accentColor = Color(0xFFFF3B30),
            containerLight = Color(0xFFFFEEEE),
            containerDark = Color(0xFF45191C)
        )
        ItemCategory.PRODUCT -> CategoryThemeData(
            category = category,
            label = "Products",
            icon = Icons.Default.ShoppingBag,
            accentColor = Color(0xFF34C759),
            containerLight = Color(0xFFEAF9ED),
            containerDark = Color(0xFF14381C)
        )
        ItemCategory.RECIPE -> CategoryThemeData(
            category = category,
            label = "Recipes",
            icon = Icons.Default.Restaurant,
            accentColor = Color(0xFFFF9500),
            containerLight = Color(0xFFFFF2DF),
            containerDark = Color(0xFF44270C)
        )
        ItemCategory.BOOK -> CategoryThemeData(
            category = category,
            label = "Books",
            icon = Icons.Default.MenuBook,
            accentColor = Color(0xFF5856D6),
            containerLight = Color(0xFFEEEBFF),
            containerDark = Color(0xFF201D47)
        )
        ItemCategory.TUTORIAL -> CategoryThemeData(
            category = category,
            label = "Tutorials",
            icon = Icons.Default.School,
            accentColor = Color(0xFF00C7BE),
            containerLight = Color(0xFFE2FAF8),
            containerDark = Color(0xFF0D3836)
        )
        ItemCategory.NOTE -> CategoryThemeData(
            category = category,
            label = "Notes",
            icon = Icons.Default.Description,
            accentColor = Color(0xFFFFCC00),
            containerLight = Color(0xFFFFF9DB),
            containerDark = Color(0xFF42370A)
        )
        ItemCategory.PHOTO -> CategoryThemeData(
            category = category,
            label = "Photos",
            icon = Icons.Default.CameraAlt,
            accentColor = Color(0xFFFF2D55),
            containerLight = Color(0xFFFFE8ED),
            containerDark = Color(0xFF47131F)
        )
        ItemCategory.COLLECTION -> CategoryThemeData(
            category = category,
            label = "Collections",
            icon = Icons.Default.Bookmark,
            accentColor = Color(0xFFAF52DE),
            containerLight = Color(0xFFF7ECFD),
            containerDark = Color(0xFF3B184F)
        )
        ItemCategory.URL -> CategoryThemeData(
            category = category,
            label = "Links",
            icon = Icons.Default.Link,
            accentColor = Color(0xFF32ADE6),
            containerLight = Color(0xFFEBF6FD),
            containerDark = Color(0xFF113247)
        )
        ItemCategory.REVIEW -> CategoryThemeData(
            category = category,
            label = "Reviews",
            icon = Icons.Default.RateReview,
            accentColor = Color(0xFFFF9F0A),
            containerLight = Color(0xFFFFF4E0),
            containerDark = Color(0xFF422607)
        )
        ItemCategory.TV_SHOW -> CategoryThemeData(
            category = category,
            label = "TV Shows",
            icon = Icons.Default.Tv,
            accentColor = Color(0xFF64D2FF),
            containerLight = Color(0xFFE8F8FF),
            containerDark = Color(0xFF143345)
        )
        ItemCategory.MUSIC -> CategoryThemeData(
            category = category,
            label = "Music",
            icon = Icons.Default.MusicNote,
            accentColor = Color(0xFFFF2D55),
            containerLight = Color(0xFFFFE8ED),
            containerDark = Color(0xFF47131F)
        )
    }
}

/**
 * Returns the authentic platform brand icon (or white variant for dark mode)
 * from the official assets. Covers all 68 platforms with precision.
 */
fun getPlatformDrawableRes(platform: String, url: String = "", isDark: Boolean = false): Int? {
    val combined = (platform + " " + url).lowercase()
    return when {
        "youtube" in combined || "youtu.be" in combined -> if (isDark) R.drawable.ic_brand_youtube_white else R.drawable.ic_brand_youtube
        "instagram" in combined || "instagr.am" in combined -> if (isDark) R.drawable.ic_brand_instagram_white else R.drawable.ic_brand_instagram
        "tiktok" in combined -> if (isDark) R.drawable.ic_brand_tiktok_white else R.drawable.ic_brand_tiktok
        "twitter" in combined || "x.com" in combined || combined.startsWith("x ") || combined == "x" -> if (isDark) R.drawable.ic_brand_x_white else R.drawable.ic_brand_x
        "spotify" in combined -> if (isDark) R.drawable.ic_brand_spotify_white else R.drawable.ic_brand_spotify
        "reddit" in combined -> if (isDark) R.drawable.ic_brand_reddit_white else R.drawable.ic_brand_reddit
        "pinterest" in combined -> if (isDark) R.drawable.ic_brand_pinterest_white else R.drawable.ic_brand_pinterest
        "linkedin" in combined -> if (isDark) R.drawable.ic_brand_linkedin_white else R.drawable.ic_brand_linkedin
        "github" in combined -> if (isDark) R.drawable.ic_brand_github_white else R.drawable.ic_brand_github
        "netflix" in combined -> if (isDark) R.drawable.ic_brand_netflix_white else R.drawable.ic_brand_netflix
        "applemusic" in combined || "music.apple" in combined -> if (isDark) R.drawable.ic_brand_applemusic_white else R.drawable.ic_brand_applemusic
        "applepodcasts" in combined || "podcasts.apple" in combined -> if (isDark) R.drawable.ic_brand_applepodcasts_white else R.drawable.ic_brand_applepodcasts
        "appletv" in combined || "tv.apple" in combined -> if (isDark) R.drawable.ic_brand_appletv_white else R.drawable.ic_brand_appletv
        "amazon" in combined -> if (isDark) R.drawable.ic_brand_amazon_white else R.drawable.ic_brand_amazon
        "twitch" in combined -> if (isDark) R.drawable.ic_brand_twitch_white else R.drawable.ic_brand_twitch
        "discord" in combined -> if (isDark) R.drawable.ic_brand_discord_white else R.drawable.ic_brand_discord
        "telegram" in combined || "t.me" in combined -> if (isDark) R.drawable.ic_brand_telegram_white else R.drawable.ic_brand_telegram
        "whatsapp" in combined || "wa.me" in combined -> if (isDark) R.drawable.ic_brand_whatsapp_white else R.drawable.ic_brand_whatsapp
        "threads" in combined -> if (isDark) R.drawable.ic_brand_threads_white else R.drawable.ic_brand_threads
        "snapchat" in combined -> if (isDark) R.drawable.ic_brand_snapchat_white else R.drawable.ic_brand_snapchat
        "notion" in combined -> if (isDark) R.drawable.ic_brand_notion_white else R.drawable.ic_brand_notion
        "googledrive" in combined || "drive.google" in combined -> if (isDark) R.drawable.ic_brand_googledrive_white else R.drawable.ic_brand_googledrive
        "googledocs" in combined || "docs.google" in combined -> if (isDark) R.drawable.ic_brand_googledocs_white else R.drawable.ic_brand_googledocs
        "googlemaps" in combined || "maps.google" in combined -> if (isDark) R.drawable.ic_brand_googlemaps_white else R.drawable.ic_brand_googlemaps
        "googlephotos" in combined || "photos.google" in combined -> if (isDark) R.drawable.ic_brand_googlephotos_white else R.drawable.ic_brand_googlephotos
        "goodreads" in combined -> if (isDark) R.drawable.ic_brand_goodreads_white else R.drawable.ic_brand_goodreads
        "imdb" in combined -> if (isDark) R.drawable.ic_brand_imdb_white else R.drawable.ic_brand_imdb
        "letterboxd" in combined -> if (isDark) R.drawable.ic_brand_letterboxd_white else R.drawable.ic_brand_letterboxd
        "medium" in combined -> if (isDark) R.drawable.ic_brand_medium_white else R.drawable.ic_brand_medium
        "substack" in combined -> if (isDark) R.drawable.ic_brand_substack_white else R.drawable.ic_brand_substack
        "soundcloud" in combined -> if (isDark) R.drawable.ic_brand_soundcloud_white else R.drawable.ic_brand_soundcloud
        "bandcamp" in combined -> if (isDark) R.drawable.ic_brand_bandcamp_white else R.drawable.ic_brand_bandcamp
        "vimeo" in combined -> if (isDark) R.drawable.ic_brand_vimeo_white else R.drawable.ic_brand_vimeo
        "figma" in combined -> if (isDark) R.drawable.ic_brand_figma_white else R.drawable.ic_brand_figma
        "dribbble" in combined -> if (isDark) R.drawable.ic_brand_dribbble_white else R.drawable.ic_brand_dribbble
        "behance" in combined -> if (isDark) R.drawable.ic_brand_behance_white else R.drawable.ic_brand_behance
        "unsplash" in combined -> if (isDark) R.drawable.ic_brand_unsplash_white else R.drawable.ic_brand_unsplash
        "airbnb" in combined -> if (isDark) R.drawable.ic_brand_airbnb_white else R.drawable.ic_brand_airbnb
        "booking" in combined -> if (isDark) R.drawable.ic_brand_bookingdotcom_white else R.drawable.ic_brand_bookingdotcom
        "yelp" in combined -> if (isDark) R.drawable.ic_brand_yelp_white else R.drawable.ic_brand_yelp
        "zomato" in combined -> if (isDark) R.drawable.ic_brand_zomato_white else R.drawable.ic_brand_zomato
        "swiggy" in combined -> if (isDark) R.drawable.ic_brand_swiggy_white else R.drawable.ic_brand_swiggy
        "etsy" in combined -> if (isDark) R.drawable.ic_brand_etsy_white else R.drawable.ic_brand_etsy
        "producthunt" in combined -> if (isDark) R.drawable.ic_brand_producthunt_white else R.drawable.ic_brand_producthunt
        "mastodon" in combined -> if (isDark) R.drawable.ic_brand_mastodon_white else R.drawable.ic_brand_mastodon
        "bluesky" in combined -> if (isDark) R.drawable.ic_brand_bluesky_white else R.drawable.ic_brand_bluesky
        "crunchyroll" in combined -> if (isDark) R.drawable.ic_brand_crunchyroll_white else R.drawable.ic_brand_crunchyroll
        "hbomax" in combined || "hbo" in combined || "max.com" in combined -> if (isDark) R.drawable.ic_brand_hbomax_white else R.drawable.ic_brand_hbomax
        "paramount" in combined -> if (isDark) R.drawable.ic_brand_paramountplus_white else R.drawable.ic_brand_paramountplus
        "plex" in combined -> if (isDark) R.drawable.ic_brand_plex_white else R.drawable.ic_brand_plex
        "roku" in combined -> if (isDark) R.drawable.ic_brand_roku_white else R.drawable.ic_brand_roku
        "tubi" in combined -> if (isDark) R.drawable.ic_brand_tubi_white else R.drawable.ic_brand_tubi
        "mubi" in combined -> if (isDark) R.drawable.ic_brand_mubi_white else R.drawable.ic_brand_mubi
        "bilibili" in combined -> if (isDark) R.drawable.ic_brand_bilibili_white else R.drawable.ic_brand_bilibili
        "dailymotion" in combined -> if (isDark) R.drawable.ic_brand_dailymotion_white else R.drawable.ic_brand_dailymotion
        "deviantart" in combined -> if (isDark) R.drawable.ic_brand_deviantart_white else R.drawable.ic_brand_deviantart
        "flickr" in combined -> if (isDark) R.drawable.ic_brand_flickr_white else R.drawable.ic_brand_flickr
        "instapaper" in combined -> if (isDark) R.drawable.ic_brand_instapaper_white else R.drawable.ic_brand_instapaper
        "kofi" in combined || "ko-fi" in combined -> if (isDark) R.drawable.ic_brand_kofi_white else R.drawable.ic_brand_kofi
        "line" in combined -> if (isDark) R.drawable.ic_brand_line_white else R.drawable.ic_brand_line
        "patreon" in combined -> if (isDark) R.drawable.ic_brand_patreon_white else R.drawable.ic_brand_patreon
        "pixiv" in combined -> if (isDark) R.drawable.ic_brand_pixiv_white else R.drawable.ic_brand_pixiv
        "quora" in combined -> if (isDark) R.drawable.ic_brand_quora_white else R.drawable.ic_brand_quora
        "signal" in combined -> if (isDark) R.drawable.ic_brand_signal_white else R.drawable.ic_brand_signal
        "tumblr" in combined -> if (isDark) R.drawable.ic_brand_tumblr_white else R.drawable.ic_brand_tumblr
        "wechat" in combined -> if (isDark) R.drawable.ic_brand_wechat_white else R.drawable.ic_brand_wechat
        "youtubetv" in combined -> if (isDark) R.drawable.ic_brand_youtubetv_white else R.drawable.ic_brand_youtubetv
        else -> null
    }
}

data class PlatformThemeData(
    val name: String,
    val brandColor: Color,
    val gradientBrush: Brush?,
    val icon: ImageVector,
    val glyphText: String? = null
)

fun getPlatformTheme(platform: String, url: String = ""): PlatformThemeData {
    val combined = (platform + " " + url).lowercase()
    return when {
        "instagram" in combined || "instagr.am" in combined -> PlatformThemeData(
            name = "Instagram",
            brandColor = Color(0xFFE1306C),
            gradientBrush = Brush.linearGradient(
                listOf(
                    Color(0xFF833AB4),
                    Color(0xFFFD1D1D),
                    Color(0xFFFCAF45)
                )
            ),
            icon = Icons.Default.CameraAlt
        )
        "tiktok" in combined -> PlatformThemeData(
            name = "TikTok",
            brandColor = Color(0xFF000000),
            gradientBrush = null,
            icon = Icons.Default.MusicNote
        )
        "youtube" in combined || "youtu.be" in combined -> PlatformThemeData(
            name = "YouTube",
            brandColor = Color(0xFFFF0000),
            gradientBrush = null,
            icon = Icons.Default.PlayArrow
        )
        "reddit" in combined -> PlatformThemeData(
            name = "Reddit",
            brandColor = Color(0xFFFF4500),
            gradientBrush = null,
            icon = Icons.Default.AutoAwesome
        )
        "pinterest" in combined -> PlatformThemeData(
            name = "Pinterest",
            brandColor = Color(0xFFE60023),
            gradientBrush = null,
            icon = Icons.Default.Bookmark,
            glyphText = "P"
        )
        "linkedin" in combined -> PlatformThemeData(
            name = "LinkedIn",
            brandColor = Color(0xFF0A66C2),
            gradientBrush = null,
            icon = Icons.Default.Link,
            glyphText = "in"
        )
        "twitter" in combined || "x.com" in combined -> PlatformThemeData(
            name = "X",
            brandColor = Color(0xFF111111),
            gradientBrush = null,
            icon = Icons.Default.Code,
            glyphText = "𝕏"
        )
        "github" in combined -> PlatformThemeData(
            name = "GitHub",
            brandColor = Color(0xFF24292E),
            gradientBrush = null,
            icon = Icons.Default.Code
        )
        "photos" in combined || "photo" in combined -> PlatformThemeData(
            name = "Photos",
            brandColor = Color(0xFFFF2D55),
            gradientBrush = null,
            icon = Icons.Default.PhotoCamera
        )
        "notes" in combined || "note" in combined -> PlatformThemeData(
            name = "Notes",
            brandColor = Color(0xFFFFCC00),
            gradientBrush = null,
            icon = Icons.Default.Description
        )
        else -> PlatformThemeData(
            name = "Web",
            brandColor = Color(0xFF007AFF),
            gradientBrush = null,
            icon = Icons.Default.Language
        )
    }
}

// Colored Category Badge Icon with authentic 3D PNG asset
@Composable
fun CategoryBadgeIcon(
    category: ItemCategory,
    modifier: Modifier = Modifier,
    size: Int = 22,
    iconSize: Int = 14
) {
    val theme = getCategoryTheme(category)
    val isDark = ThemeManager.isDark
    val catDrawable = getCategoryDrawableRes(category)

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (isDark) theme.containerDark else theme.containerLight)
            .border(
                0.8.dp,
                theme.accentColor.copy(alpha = if (isDark) 0.50f else 0.35f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = catDrawable),
            contentDescription = theme.label,
            modifier = Modifier.size(iconSize.dp),
            contentScale = ContentScale.Fit
        )
    }
}

// Authentic Platform Brand Badge Icon with crisp official vector/PNG
@Composable
fun PlatformBadgeIcon(
    platform: String,
    url: String = "",
    size: Int = 20,
    modifier: Modifier = Modifier
) {
    val isDark = ThemeManager.isDark
    val brandDrawable = getPlatformDrawableRes(platform, url, isDark)
    val theme = getPlatformTheme(platform, url)

    if (brandDrawable != null) {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF2C2C2E) else Color.White)
                .border(0.8.dp, if (isDark) Color(0x33FFFFFF) else Color(0x1F000000), CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = brandDrawable),
                contentDescription = platform,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .then(
                    if (theme.gradientBrush != null) Modifier.background(theme.gradientBrush)
                    else Modifier.background(theme.brandColor)
                )
                .border(0.8.dp, Color.White.copy(alpha = 0.50f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!theme.glyphText.isNullOrBlank()) {
                Text(
                    text = theme.glyphText,
                    color = Color.White,
                    fontSize = (size * 0.55f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else {
                Icon(
                    imageVector = theme.icon,
                    contentDescription = theme.name,
                    tint = Color.White,
                    modifier = Modifier.size((size * 0.60f).dp)
                )
            }
        }
    }
}

// Albo-style Floating Bottom-Left Pill on Card Media
// Displays authentic brand icon + illustrated 3D category badge
@Composable
fun CardFloatingPill(
    platform: String,
    category: ItemCategory,
    url: String = "",
    modifier: Modifier = Modifier
) {
    val catDrawable = getCategoryDrawableRes(category)
    val catTheme = getCategoryTheme(category)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.Black.copy(alpha = 0.65f))
            .border(0.8.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(percent = 50))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Platform Icon (Real brand logo)
        PlatformBadgeIcon(
            platform = platform,
            url = url,
            size = 18
        )

        // Category 3D icon
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(catTheme.accentColor.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = catDrawable),
                contentDescription = catTheme.label,
                modifier = Modifier.size(13.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// Albo-Style Top Horizontal Category Navigation Dock
// Displays full-color 3D/illustrated icon boxes with clean label underneath
@Composable
fun AlboCategoryDock(
    selectedCategory: ItemCategory?,
    onSelectCategory: (ItemCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = ThemeManager.isDark

    val categories = remember {
        listOf(
            ItemCategory.FILM,
            ItemCategory.TV_SHOW,
            ItemCategory.MUSIC,
            ItemCategory.BOOK,
            ItemCategory.RECIPE,
            ItemCategory.PLACE,
            ItemCategory.PRODUCT,
            ItemCategory.SOFTWARE,
            ItemCategory.TUTORIAL,
            ItemCategory.PHOTO,
            ItemCategory.NOTE,
            ItemCategory.URL,
            ItemCategory.REVIEW,
            ItemCategory.COLLECTION
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (isDark) Color(0xFF1C1C1E) else AppWhite)
            .border(
                1.dp,
                if (isDark) Color(0x25FFFFFF) else Color(0x12000000),
                RoundedCornerShape(22.dp)
            )
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (isDark) Color(0x40000000) else Color(0x10000000)
            )
            .padding(vertical = 12.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All" Category Chip with signature 3D icon
            item {
                val isAllSelected = selectedCategory == null
                val scale by animateFloatAsState(
                    targetValue = if (isAllSelected) 1.05f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.75f),
                    label = "AllScale"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectCategory(null) }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isAllSelected) {
                                    if (isDark) Color.White else AppBlack
                                } else {
                                    if (isDark) Color(0xFF2C2C2E) else AppInputBg
                                }
                            )
                            .then(
                                if (isAllSelected) {
                                    Modifier.border(
                                        2.dp,
                                        if (isDark) Color.White else AppBlack,
                                        RoundedCornerShape(14.dp)
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cat_favorites),
                            contentDescription = "All",
                            modifier = Modifier.size(26.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "All",
                        fontSize = 11.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAllSelected) {
                            if (isDark) Color.White else AppBlack
                        } else AppLightGrey,
                        maxLines = 1
                    )
                }
            }

            // Categories list (Films, TV Shows, Music, Books, Recipes, Places, Products, Software, Tutorials, etc.)
            items(categories) { category ->
                val theme = getCategoryTheme(category)
                val catRes = getCategoryDrawableRes(category)
                val isSelected = selectedCategory == category

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.75f),
                    label = "CatScale"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (isSelected) onSelectCategory(null)
                                else onSelectCategory(category)
                            }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isDark) theme.containerDark else theme.containerLight
                            )
                            .border(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) theme.accentColor
                                else theme.accentColor.copy(alpha = if (isDark) 0.35f else 0.20f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = catRes),
                            contentDescription = theme.label,
                            modifier = Modifier.size(28.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = theme.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) theme.accentColor else AppLightGrey,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
