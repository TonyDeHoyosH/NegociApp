package com.burritoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryOrangeDark,
    secondary = SecondaryBrown,
    tertiary = AccentYellow,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = ErrorRed,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryOrangeDark,
    secondary = SecondaryBrown,
    tertiary = AccentYellow,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = ErrorRed,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun BurritoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
