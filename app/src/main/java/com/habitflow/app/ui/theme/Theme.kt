package com.habitflow.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Lavender500,
    onPrimary = Color.White,
    primaryContainer = Lavender100,
    onPrimaryContainer = Lavender700,
    secondary = IOSTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF004D56),
    tertiary = IOSGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD7F5E3),
    onTertiaryContainer = Color(0xFF00391C),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = Color(0xFFD1D1D6),
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkTextPrimary,
    surfaceTint = Lavender400
)

private val DarkColorScheme = darkColorScheme(
    primary = Lavender400,
    onPrimary = Lavender700,
    primaryContainer = Lavender600,
    onPrimaryContainer = Lavender100,
    secondary = IOSTeal,
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF004D61),
    onSecondaryContainer = Color(0xFFB8EAFF),
    tertiary = IOSGreen,
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Color(0xFF005228),
    onTertiaryContainer = Color(0xFF7CF8A1),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = Color(0xFF49454F),
    inverseSurface = LightSurface,
    inverseOnSurface = LightTextPrimary,
    surfaceTint = Lavender400
)

@Composable
fun HabitFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HabitFlowTypography,
        content = content
    )
}
