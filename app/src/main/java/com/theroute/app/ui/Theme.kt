package com.theroute.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// The Route design system, from KJ's Emergent spec/screenshots:
// deep purple background, gold primary, teal secondary, orange accent,
// high contrast for direct sunlight, large tap targets.
// Bold everywhere on purpose: KJ asked for bold, visible text.
private val RouteColors = darkColorScheme(
    primary = Color(0xFFFFD700),          // gold
    onPrimary = Color(0xFF1A0A2E),        // deep purple text on gold buttons
    primaryContainer = Color(0xFF4A2E7A),
    onPrimaryContainer = Color(0xFFFFD700),
    secondary = Color(0xFF00D4AA),        // teal
    onSecondary = Color(0xFF1A0A2E),
    secondaryContainer = Color(0xFF004D3D),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFFFF6B35),         // orange
    onTertiary = Color(0xFF1A0A2E),
    tertiaryContainer = Color(0xFF5A2A12),
    onTertiaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF1A0A2E),       // deep purple, not white
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF2D1B4E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF3D2A5E),
    onSurfaceVariant = Color(0xFFE0D6FF),
    outline = Color(0xFFFFD700),
    outlineVariant = Color(0xFF00D4AA),
)

private val BoldTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 57.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 45.sp),
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp),
)

@Composable
fun RouteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RouteColors,
        typography = BoldTypography,
        content = content,
    )
}
