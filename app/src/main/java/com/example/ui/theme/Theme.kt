package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NordDarkColorScheme = darkColorScheme(
    primary = Nord8,
    onPrimary = Nord0,
    primaryContainer = Nord10,
    onPrimaryContainer = Nord6,
    secondary = Nord9,
    onSecondary = Nord0,
    secondaryContainer = Nord2,
    onSecondaryContainer = Nord4,
    tertiary = Nord7,
    onTertiary = Nord0,
    background = Nord0,
    onBackground = Nord6,
    surface = Nord1,
    onSurface = Nord6,
    surfaceVariant = Nord2,
    onSurfaceVariant = Nord4,
    outline = Nord3,
    outlineVariant = Nord2,
    error = Nord11,
    onError = Nord6,
    errorContainer = Color(0xFF4C2A2F),
    onErrorContainer = Color(0xFFFFD9DD)
)

private val NordLightColorScheme = lightColorScheme(
    primary = Nord10,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3F4),
    onPrimaryContainer = Nord0,
    secondary = Nord9,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2EAF4),
    onSecondaryContainer = Nord1,
    tertiary = Nord7,
    onTertiary = Color.White,
    background = Nord6,
    onBackground = Nord0,
    surface = Nord5,
    onSurface = Nord0,
    surfaceVariant = Nord4,
    onSurfaceVariant = Nord1,
    outline = Nord3,
    outlineVariant = Color(0xFFD0D7E1),
    error = Nord11,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NordDarkColorScheme else NordLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

