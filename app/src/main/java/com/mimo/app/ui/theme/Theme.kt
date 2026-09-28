package com.mimo.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Global Theme Manager for Dark Mode / Light Mode toggle
object ThemeManager {
    var isDark by mutableStateOf(true)
}

// Adaptive Theme Colors - Automatically update on theme toggle
// AMOLED Dark: Pure #000000 pitch black for OLED screens
val AppWhite: Color
    @Composable
    get() = if (ThemeManager.isDark) Color(0xFF000000) else Color(0xFFFFFFFF)

val AppBlack: Color
    @Composable
    get() = if (ThemeManager.isDark) Color(0xFFFFFFFF) else Color(0xFF111111)

val AppCardBg: Color
    @Composable
    get() = if (ThemeManager.isDark) Color(0xFF121212) else Color(0xFFFFFFFF)

val AppInputBg: Color
    @Composable
    get() = if (ThemeManager.isDark) Color(0xFF1C1C1E) else Color(0xFFF5F5F5)

val AppBorderGrey: Color
    @Composable
    get() = if (ThemeManager.isDark) Color(0xFF2C2C2E) else Color(0xFFE0E0E0)

val AppLightGrey: Color
    @Composable
    get() = if (ThemeManager.isDark) Color(0xFF8E8E93) else Color(0xFF999999)

val AppAccentRed = Color(0xFFFF3B30)

val AppOtpBoxBg: Color
    @Composable
    get() = if (ThemeManager.isDark) Color(0xFF1C1C1E) else Color(0xFFF5F5F5)

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp), // Input fields ~14dp
    large = RoundedCornerShape(50.dp)   // Pill shape for buttons
)

@Composable
fun getAppTypography(): Typography {
    val textPrimary = AppBlack
    val textSecondary = AppLightGrey
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            color = textPrimary
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            color = textPrimary
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = textPrimary
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = textPrimary
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = textSecondary
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = if (ThemeManager.isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
        )
    )
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF111111),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFFFFFFF)
)

@Composable
fun MimoTheme(
    content: @Composable () -> Unit
) {
    val isDark = ThemeManager.isDark
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getAppTypography(),
        shapes = AppShapes,
        content = content
    )
}
