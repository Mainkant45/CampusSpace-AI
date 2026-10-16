package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = DeepNavy,
    primaryContainer = SlateNavy,
    onPrimaryContainer = Color.White,
    secondary = AccentTeal,
    onSecondary = Color.Black,
    tertiary = PurpleAccent,
    background = DeepNavy,
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF132238),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1C2D42),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
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
    // We prioritize the crisp, professional light-first design direction specified in the brief
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
