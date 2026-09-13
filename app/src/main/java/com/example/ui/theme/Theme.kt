package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorbitDarkScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF2B2B2B),
    onPrimaryContainer = NeonGreen,
    secondary = NeonCyan,
    onSecondary = Color(0xFF121212),
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = NeonCyan,
    tertiary = NeonOrange,
    background = ColorbitBg,
    onBackground = TextPrimary,
    surface = ColorbitCard,
    onSurface = TextPrimary,
    surfaceVariant = ColorbitCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = ColorbitBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ColorbitDarkScheme,
        typography = Typography,
        content = content
    )
}
