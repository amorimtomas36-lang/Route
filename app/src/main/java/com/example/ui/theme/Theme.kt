package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RouteBoostColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005144),
    onPrimaryContainer = Color(0xFF70FCE0),

    secondary = OrangeBoost,
    onSecondary = Color(0xFF4A1800),
    secondaryContainer = Color(0xFF672400),
    onSecondaryContainer = Color(0xFFFFDBCF),

    tertiary = CyanPrimaryDark,
    onTertiary = Color.White,

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = OutlineBorder,
    outlineVariant = OutlineBorderLight,

    error = PingRed,
    onError = Color.White
)

@Composable
fun RouteBoostTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RouteBoostColorScheme,
        typography = Typography,
        content = content
    )
}
