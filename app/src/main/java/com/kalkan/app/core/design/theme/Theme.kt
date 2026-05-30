package com.kalkan.app.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = KalkanNavy,
    secondary = KalkanBlue,
    tertiary = KalkanGreen,
    error = KalkanRed,
    background = KalkanSurface,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = KalkanNavy,
    onSurface = KalkanNavy,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE2E8F0),
    secondary = Color(0xFF60A5FA),
    tertiary = Color(0xFF4ADE80),
    error = Color(0xFFF87171),
    background = KalkanNavy,
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onPrimary = KalkanNavy,
    onSecondary = KalkanNavy,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1),
)

@Composable
fun KalkanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KalkanTypography,
        content = content,
    )
}
