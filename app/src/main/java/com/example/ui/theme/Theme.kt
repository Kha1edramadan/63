package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val KineticDarkColorScheme = darkColorScheme(
  primary = Primary,
  onPrimary = OnPrimary,
  secondary = Primary,
  onSecondary = OnPrimary,
  tertiary = Primary,
  onTertiary = OnPrimary,
  background = Surface,
  onBackground = OnSurface,
  surface = Surface,
  onSurface = OnSurface,
  surfaceVariant = SurfaceVariant,
  onSurfaceVariant = OnSurfaceVariant,
  surfaceContainer = SurfaceContainer,
  surfaceContainerHigh = SurfaceContainerHigh,
  surfaceContainerHighest = SurfaceContainerHighest,
  surfaceContainerLow = SurfaceContainerLow,
  surfaceContainerLowest = SurfaceContainerLowest,
  error = Error,
  onError = OnError,
  outline = Outline,
  outlineVariant = OutlineVariant
)

@Composable
fun KineticTheme(
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    MaterialTheme(
      colorScheme = KineticDarkColorScheme,
      typography = Typography,
      content = content
    )
  }
}
