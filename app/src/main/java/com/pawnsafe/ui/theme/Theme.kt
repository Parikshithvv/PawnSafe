package com.pawnsafe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PawnSafeColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = SurfaceWhite,
    primaryContainer = TerracottaLight,
    onPrimaryContainer = TerracottaDark,
    background = WarmPeach,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = WarmPeachCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    outlineVariant = DividerColor,
)

@Composable
fun PawnSafeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PawnSafeColorScheme,
        typography = PawnSafeTypography,
        content = content
    )
}