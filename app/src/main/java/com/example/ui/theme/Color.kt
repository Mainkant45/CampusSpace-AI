package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// CampusSpace AI Peak Design System - Obsidian Command & Cyan Telemetry
val DeepNavy = Color(0xFF080F1E)
val SlateNavy = Color(0xFF132238)
val CommandNavy = Color(0xFF1A2E4C)
val BorderNavy = Color(0xFF243B60)

// Vibrant Tech Accents
val AccentTeal = Color(0xFF00ADB5)
val TealDark = Color(0xFF007A80)
val TealLight = Color(0xFF5CE1E6)
val ElectricCyan = Color(0xFF00E5FF)
val CyanGlow = Color(0xFF38BDF8)
val SoftCyan = Color(0xFFE0F7FA)

// Neutral Surfaces & Backgrounds
val BackgroundLight = Color(0xFFF4F7FB)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFEBF1F8)
val CardBorderLight = Color(0xFFDCE4EE)
val CardBorderHighlight = Color(0xFFBAE6FD)

// Text Tokens
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF475569)
val TextMuted = Color(0xFF94A3B8)
val TextInverse = Color(0xFFF8FAFC)

// High-fidelity Semantic Tokens
val StatusSuccess = Color(0xFF10B981)
val StatusSuccessContainer = Color(0xFFD1FAE5)
val StatusSuccessText = Color(0xFF047857)

val StatusWarning = Color(0xFFF59E0B)
val StatusWarningContainer = Color(0xFFFEF3C7)
val StatusWarningText = Color(0xFFB45309)

val StatusError = Color(0xFFEF4444)
val StatusErrorContainer = Color(0xFFFEE2E2)
val StatusErrorText = Color(0xFFB91C1C)

val StatusInfo = Color(0xFF0284C7)
val StatusInfoContainer = Color(0xFFE0F2FE)
val StatusInfoText = Color(0xFF0369A1)

val PurpleAccent = Color(0xFF6366F1)
val PurpleContainer = Color(0xFFEEF2FF)
val IndigoDark = Color(0xFF1E1B4B)

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF080F1E), Color(0xFF132238))
)

val CyanAccentGradient = Brush.horizontalGradient(
    colors = listOf(AccentTeal, ElectricCyan)
)

val GlassBorderGradient = Brush.linearGradient(
    colors = listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.1f))
)
