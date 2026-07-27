package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CinemaColorScheme = darkColorScheme(
    primary = CinemaRed,
    onPrimary = CinemaTextPrimary,
    primaryContainer = CinemaBrightRed,
    secondary = CinemaGold,
    onSecondary = CinemaDarkBg,
    background = CinemaDarkBg,
    onBackground = CinemaTextPrimary,
    surface = CinemaSurface,
    onSurface = CinemaTextPrimary,
    surfaceVariant = CinemaSurfaceVariant,
    onSurfaceVariant = CinemaTextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinemaColorScheme,
        typography = Typography,
        content = content
    )
}
