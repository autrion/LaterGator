package com.latergator.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LaterGatorColorScheme = lightColorScheme(
    primary = GatorGreen,
    onPrimary = Color.White,
    primaryContainer = GatorGreenPale,
    onPrimaryContainer = GatorGreen,
    secondary = GatorTeal,
    onSecondary = Color.White,
    background = GatorGreenPale,
    onBackground = Color(0xFF1B1B1B),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1B),
    surfaceVariant = GatorSurface,
    onSurfaceVariant = Color(0xFF444444)
)

@Composable
fun LaterGatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LaterGatorColorScheme,
        typography = LaterGatorTypography,
        content = content
    )
}
