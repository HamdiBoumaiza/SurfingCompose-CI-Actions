package com.hb.surfingcompose.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Olive,
    onPrimary = Color.White,
    primaryContainer = SoftOlive,
    onPrimaryContainer = Ink,
    secondary = MutedOlive,
    onSecondary = Color.White,
    secondaryContainer = SoftOlive,
    onSecondaryContainer = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = SoftOlive,
    onSurfaceVariant = MutedOlive,
    outline = Outline,
    outlineVariant = Color(0xFFE0E5D9)
)

private val DarkColors = darkColorScheme(
    primary = PaleOlive,
    onPrimary = Color(0xFF1D3218),
    primaryContainer = Color(0xFF313F2B),
    onPrimaryContainer = Color(0xFFE0EDD5),
    secondary = Color(0xFFBAC4B4),
    onSecondary = Color(0xFF263326),
    secondaryContainer = Color(0xFF313F2B),
    onSecondaryContainer = Color(0xFFE0EDD5),
    background = DarkBackground,
    onBackground = Color(0xFFEEF2E9),
    surface = DarkSurface,
    onSurface = Color(0xFFEEF2E9),
    surfaceVariant = Color(0xFF313F2B),
    onSurfaceVariant = Color(0xFFBAC4B4),
    outline = Color(0xFF899480),
    outlineVariant = Color(0xFF3D4637)
)

@Composable
fun SurfingComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }
    MaterialTheme(colorScheme = colors, typography = Typography, shapes = Shapes, content = content)
}
