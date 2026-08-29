package com.debzg.gotasks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GoTasksColorScheme =
  darkColorScheme(
    primary = AccentCoral,
    onPrimary = TextPrimary,
    secondary = AccentCoral,
    background = TrueBlack,
    onBackground = TextPrimary,
    surface = SurfaceElevated,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevatedHigh,
    onSurfaceVariant = TextSecondary,
  )

@Composable
fun GoTasksTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = GoTasksColorScheme, typography = Typography, content = content)
}
