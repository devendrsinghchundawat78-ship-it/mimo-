package com.mimo.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite
import com.mimo.app.ui.theme.LiquidGlassManager
import com.mimo.app.ui.theme.ThemeManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassSettingsSheet(
    onDismiss: () -> Unit
) {
    val isDark = ThemeManager.isDark
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
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Liquid Glass",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppBlack
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Translucency, blur & navigation motion",
                        fontSize = 13.sp,
                        color = AppLightGrey
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AppBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Live Glass Preview Card
            LiquidGlassLivePreview(isDark = isDark)

            Spacer(modifier = Modifier.height(20.dp))

            // Group 1: Glass Appearance
            SettingsGroupWrapper(title = "Glass Appearance") {
                // Enable Liquid Glass Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Liquid Glass Effect",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppBlack
                        )
                        Text(
                            text = "Translucent frosted material with refraction",
                            fontSize = 12.sp,
                            color = AppLightGrey
                        )
                    }
                    Switch(
                        checked = LiquidGlassManager.isEnabled,
                        onCheckedChange = { LiquidGlassManager.isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppWhite,
                            checkedTrackColor = AppBlack,
                            uncheckedThumbColor = AppLightGrey,
                            uncheckedTrackColor = AppInputBg
                        )
                    )
                }

                AnimatedVisibility(
                    visible = LiquidGlassManager.isEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = AppBorderGrey,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Blur Radius Slider
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Blur Intensity",
                                    fontSize = 13.sp,
                                    color = AppBlack
                                )
                                Text(
                                    text = "${LiquidGlassManager.blurRadiusDp.roundToInt()} dp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppBlack
                                )
                            }
                            Slider(
                                value = LiquidGlassManager.blurRadiusDp,
                                onValueChange = { LiquidGlassManager.blurRadiusDp = it },
                                valueRange = 4f..24f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = AppBlack,
                                    activeTrackColor = AppBlack,
                                    inactiveTrackColor = AppBorderGrey
                                )
                            )
                        }

                        // Surface Opacity Slider
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Glass Opacity",
                                    fontSize = 13.sp,
                                    color = AppBlack
                                )
                                Text(
                                    text = "${(LiquidGlassManager.surfaceOpacity * 100).roundToInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppBlack
                                )
                            }
                            Slider(
                                value = LiquidGlassManager.surfaceOpacity,
                                onValueChange = { LiquidGlassManager.surfaceOpacity = it },
                                valueRange = 0.15f..0.75f,
                                steps = 11,
                                colors = SliderDefaults.colors(
                                    thumbColor = AppBlack,
                                    activeTrackColor = AppBlack,
                                    inactiveTrackColor = AppBorderGrey
                                )
                            )
                        }

                        // Color Vibrancy Slider
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Color Vibrancy",
                                    fontSize = 13.sp,
                                    color = AppBlack
                                )
                                Text(
                                    text = String.format("%.2fx", LiquidGlassManager.vibrancy),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppBlack
                                )
                            }
                            Slider(
                                value = LiquidGlassManager.vibrancy,
                                onValueChange = { LiquidGlassManager.vibrancy = it },
                                valueRange = 1.0f..2.0f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = AppBlack,
                                    activeTrackColor = AppBlack,
                                    inactiveTrackColor = AppBorderGrey
                                )
                            )
                        }

                        // Specular Rim Highlight Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Specular Edge Highlight",
                                    fontSize = 13.sp,
                                    color = AppBlack
                                )
                                Text(
                                    text = "Gleaming light reflection along top glass border",
                                    fontSize = 11.sp,
                                    color = AppLightGrey
                                )
                            }
                            Switch(
                                checked = LiquidGlassManager.specularHighlight,
                                onCheckedChange = { LiquidGlassManager.specularHighlight = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AppWhite,
                                    checkedTrackColor = AppBlack,
                                    uncheckedThumbColor = AppLightGrey,
                                    uncheckedTrackColor = AppInputBg
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Group 2: Navigation Motion
            SettingsGroupWrapper(title = "Navigation Motion") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Compact on Scroll",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Collapses navigation bar into a compact floating pill while scrolling down",
                            fontSize = 12.sp,
                            color = AppLightGrey
                        )
                    }
                    Switch(
                        checked = LiquidGlassManager.compactOnScroll,
                        onCheckedChange = { LiquidGlassManager.compactOnScroll = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppWhite,
                            checkedTrackColor = AppBlack,
                            uncheckedThumbColor = AppLightGrey,
                            uncheckedTrackColor = AppInputBg
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reset Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppInputBg)
                    .clickable { LiquidGlassManager.resetToDefaults() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = AppBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset to Defaults",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppBlack
                    )
                }
            }
        }
    }
}

// Live Interactive Preview of the Liquid Glass Bar
@Composable
private fun LiquidGlassLivePreview(isDark: Boolean) {
    var previewSelectedTab by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    if (isDark) {
                        listOf(Color(0xFF2C1B4D), Color(0xFF1B3B36), Color(0xFF141416))
                    } else {
                        listOf(Color(0xFFFFDFD3), Color(0xFFE2F0CB), Color(0xFFC7CEEA))
                    }
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val glassBg = if (LiquidGlassManager.isEnabled) {
            val alpha = LiquidGlassManager.surfaceOpacity
            if (isDark) {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF222226).copy(alpha = (alpha + 0.20f).coerceAtMost(0.95f)),
                        Color(0xFF141416).copy(alpha = alpha)
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF).copy(alpha = (alpha + 0.20f).coerceAtMost(0.95f)),
                        Color(0xFFF0F0F5).copy(alpha = alpha)
                    )
                )
            }
        } else {
            if (isDark) Brush.verticalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF121212)))
            else Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7)))
        }

        val glassBorder = if (LiquidGlassManager.isEnabled && LiquidGlassManager.specularHighlight) {
            if (isDark) {
                Brush.verticalGradient(
                    listOf(Color(0x66FFFFFF), Color(0x20FFFFFF), Color(0x10FFFFFF))
                )
            } else {
                Brush.verticalGradient(
                    listOf(Color(0xE6FFFFFF), Color(0x55FFFFFF), Color(0x26000000))
                )
            }
        } else {
            if (isDark) Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x11FFFFFF)))
            else Brush.verticalGradient(listOf(Color(0x33000000), Color(0x1A000000)))
        }

        // Mini preview capsule
        Box(
            modifier = Modifier
                .width(260.dp)
                .height(52.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(percent = 50),
                    ambientColor = if (isDark) Color(0x66000000) else Color(0x1A000000),
                    spotColor = if (isDark) Color(0x80000000) else Color(0x26000000)
                )
                .clip(RoundedCornerShape(percent = 50))
                .background(glassBg)
                .border(1.2.dp, glassBorder, RoundedCornerShape(percent = 50))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val pillOffset by animateDpAsState(
                targetValue = when (previewSelectedTab) {
                    0 -> 4.dp
                    1 -> 72.dp
                    2 -> 140.dp
                    else -> 200.dp
                },
                animationSpec = spring(
                    dampingRatio = 0.88f,
                    stiffness = 450f
                ),
                label = "PreviewPill"
            )

            // Luminous Inner Glass Pill
            Box(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(52.dp)
                    .height(40.dp)
                    .shadow(3.dp, RoundedCornerShape(percent = 50))
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

            // Mini Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Home
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { previewSelectedTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = if (previewSelectedTab == 0) (if (isDark) Color.White else Color.Black) else AppLightGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Place
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { previewSelectedTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        tint = if (previewSelectedTab == 1) (if (isDark) Color.White else Color.Black) else AppLightGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White else Color.Black)
                        .clickable { previewSelectedTab = 2 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isDark) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Profile
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { previewSelectedTab = 3 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (previewSelectedTab == 3) (if (isDark) Color.White else Color.Black) else AppLightGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGroupWrapper(
    title: String,
    content: @Composable () -> Unit
) {
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
