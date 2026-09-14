package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AIopterCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = AIopterSurfaceVariantDark,
    onPrimaryContainer = AIopterCyanLight,
    secondary = AIopterBlueLight,
    onSecondary = Color(0xFF00325B),
    secondaryContainer = Color(0xFF0D3256),
    onSecondaryContainer = Color(0xFFBFE0FF),
    tertiary = AIopterCyanDark,
    onTertiary = Color.White,
    background = AIopterNavyDark,
    onBackground = Color(0xFFE2E8F0),
    surface = AIopterSurfaceDark,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = AIopterSurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = AIopterBorderDark,
    error = AIopterKillRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AIopterBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4EFFF),
    onPrimaryContainer = Color(0xFF001E35),
    secondary = AIopterCyanDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7F8FF),
    onSecondaryContainer = Color(0xFF002026),
    tertiary = AIopterBlueDark,
    onTertiary = Color.White,
    background = AIopterNavyLight,
    onBackground = Color(0xFF0F172A),
    surface = AIopterSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = AIopterSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = AIopterBorderLight,
    error = AIopterKillRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek tech dark theme for AIopter
    dynamicColor: Boolean = false, // Keep the signature cyan/blue AIopter brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
