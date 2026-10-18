package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepNavy,
    primaryContainer = SlateNavy,
    onPrimaryContainer = Color.White,
    secondary = AccentTeal,
    onSecondary = Color.Black,
    tertiary = PurpleAccent,
    background = DeepNavy,
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0F1B2E),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF182A45),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = BorderNavy
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = Color.White,
    primaryContainer = SoftCyan,
    onPrimaryContainer = DeepNavy,
    secondary = AccentTeal,
    onSecondary = Color.White,
    tertiary = SlateNavy,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderLight
)

@Composable
fun CampusSpaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
