package com.latergator.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.latergator.app.data.AppSettings
import com.latergator.app.data.DarkModeOption
import java.util.Calendar

private val LightColorScheme = lightColorScheme(
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

private val DarkColorScheme = darkColorScheme(
    primary = GatorGreenDark,
    onPrimary = GatorGreenOnDark,
    primaryContainer = GatorGreen,
    onPrimaryContainer = GatorGreenPale,
    secondary = GatorTealDark,
    onSecondary = Color(0xFF003731),
    background = DarkBackground,
    onBackground = Color(0xFFE2E3DE),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E3DE),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC1C9BF)
)

fun shouldUseDark(settings: AppSettings, isSystemDark: Boolean, currentMinuteOfDay: Int): Boolean =
    when (settings.darkMode) {
        DarkModeOption.SYSTEM -> isSystemDark
        DarkModeOption.LIGHT -> false
        DarkModeOption.DARK -> true
        DarkModeOption.SCHEDULED -> {
            val start = settings.scheduledDarkStartMinutes
            val end = settings.scheduledDarkEndMinutes
            if (start > end) currentMinuteOfDay >= start || currentMinuteOfDay < end
            else currentMinuteOfDay in start until end
        }
    }

@Composable
fun LaterGatorTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = LaterGatorTypography,
        content = content
    )
}
