package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = LanternNeonGreen,
  onPrimary = LanternDarkGreen,
  primaryContainer = LanternContainerGreen,
  onPrimaryContainer = LanternBrightGreen,
  secondary = LanternCyan,
  onSecondary = Color(0xFF002F48),
  secondaryContainer = LanternCyanContainer,
  onSecondaryContainer = Color(0xFFBAE6FD),
  tertiary = LanternAmber,
  onTertiary = Color(0xFF451A03),
  background = LanternObsidianBg,
  onBackground = LanternTextPrimary,
  surface = LanternObsidianSurface,
  onSurface = LanternTextPrimary,
  surfaceVariant = LanternObsidianCard,
  onSurfaceVariant = LanternTextSecondary,
  outline = LanternObsidianBorder,
  error = LanternCrimson,
  onError = Color.White
)

private val LightColorScheme = darkColorScheme(
  // Default IDE appearance remains pro-dark for developer ergonomics
  primary = LanternNeonGreen,
  onPrimary = LanternDarkGreen,
  primaryContainer = LanternContainerGreen,
  onPrimaryContainer = LanternBrightGreen,
  secondary = LanternCyan,
  background = LanternObsidianBg,
  surface = LanternObsidianSurface,
  surfaceVariant = LanternObsidianCard,
  outline = LanternObsidianBorder
)

@Composable
fun LANternCodeTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  LANternCodeTheme(darkTheme = darkTheme, content = content)
}
