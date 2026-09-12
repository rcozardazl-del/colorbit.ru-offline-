package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorbitDarkScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color(0xFF003915),
    primaryContainer = Color(0xFF005322),
    onPrimaryContainer = Color(0xFF6CF89B),
    secondary = NeonCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = NeonOrange,
    background = DarkCyberBg,
    onBackground = TextPrimary,
    surface = DarkCyberCard,
    onSurface = TextPrimary,
    surfaceVariant = DarkCyberCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkCyberBorder
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
