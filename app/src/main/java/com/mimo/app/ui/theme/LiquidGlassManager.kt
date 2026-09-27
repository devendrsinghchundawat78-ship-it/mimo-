package com.mimo.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global reactive configuration for the Liquid Glass effect and navigation bar behavior,
 * matching Apple-grade physical glass recipes.
 */
object LiquidGlassManager {
    // Master switch to enable or disable the liquid glass material
    var isEnabled by mutableStateOf(true)

    // Whether the bottom bar collapses into a compact floating pill while scrolling down
    var compactOnScroll by mutableStateOf(true)

    // Blur intensity in dp (default 8dp matches Apple-grade physical recipe)
    var blurRadiusDp by mutableFloatStateOf(8f)

    // Translucency of the glass surface (0.15f to 0.75f, default 0.40f)
    var surfaceOpacity by mutableFloatStateOf(0.40f)

    // Color vibrancy and saturation boost multiplier (1.0x to 2.0x, default 1.25x)
    var vibrancy by mutableFloatStateOf(1.25f)

    // Specular highlight rim reflection on the top curved edge
    var specularHighlight by mutableStateOf(true)

    fun resetToDefaults() {
        isEnabled = true
        compactOnScroll = true
        blurRadiusDp = 8f
        surfaceOpacity = 0.40f
        vibrancy = 1.25f
        specularHighlight = true
    }
}
