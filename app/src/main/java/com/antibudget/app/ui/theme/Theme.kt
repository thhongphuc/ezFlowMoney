package com.antibudget.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Slate950,
    primaryContainer = Emerald700,
    onPrimaryContainer = Emerald100,
    secondary = Gold400,
    onSecondary = Slate950,
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    error = StatusDanger,
    onError = Slate100
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = Slate100,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald700,
    secondary = Gold600,
    onSecondary = Slate100,
    background = Slate100,
    onBackground = Slate900,
    surface = CardSurfaceLight,
    onSurface = Slate900,
    surfaceVariant = Slate200,
    onSurfaceVariant = Slate700,
    error = StatusDanger,
    onError = Slate100
)

@Composable
fun AntiBudgetTheme(
    darkTheme: Boolean = true, // Ưu tiên Dark Theme hiện đại, dịu mắt
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
