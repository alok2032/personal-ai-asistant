package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BidiokDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberBackground,
    primaryContainer = NeonCyanDark,
    onPrimaryContainer = NeonCyan,
    secondary = NeonPurple,
    onSecondary = CyberBackground,
    secondaryContainer = NeonPurpleDark,
    onSecondaryContainer = NeonPurple,
    tertiary = NeonEmerald,
    onTertiary = CyberBackground,
    tertiaryContainer = NeonEmeraldDark,
    onTertiaryContainer = NeonEmerald,
    background = CyberBackground,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberBorder
)

@Composable
fun BidiokTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BidiokDarkColorScheme,
        typography = Typography,
        content = content
    )
}
