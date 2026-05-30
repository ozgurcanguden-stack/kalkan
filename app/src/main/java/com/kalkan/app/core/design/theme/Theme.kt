package com.kalkan.app.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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

@Composable
fun KalkanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = KalkanTypography,
        content = content,
    )
}
