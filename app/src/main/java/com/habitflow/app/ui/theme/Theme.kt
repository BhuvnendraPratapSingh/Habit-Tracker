package com.habitflow.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light Color Scheme ──────────────────────────────────────────
// Integrates Electric Blue + Cantaloupe Orange as accents
// while preserving the existing light palette structure.
private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueLight,
    onPrimaryContainer = ElectricBlueDark,

    secondary = IOSTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF004D56),

    tertiary = CantaloupeOrange,
    onTertiary = Color.White,
    tertiaryContainer = CantaloupeLight,
    onTertiaryContainer = CantaloupeDark,

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
    inverseSurface = SurfaceLevel1,
    inverseOnSurface = TextPrimaryDark,
    surfaceTint = ElectricBlue
)

// ── Dark Color Scheme ───────────────────────────────────────────
// Uses the new design system tokens:
//   Canvas:     MidnightBlack (#02070A)
//   Surface L1: #0A131A  (cards, sheets)
//   Surface L2: #121E27  (inputs, active containers)
//   Border:     #1E313E
//   Text:       #F3F7FA / #8B9BA8
private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueDark,
    onPrimaryContainer = ElectricBlueLight,

    secondary = IOSTeal,
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF004D61),
    onSecondaryContainer = Color(0xFFB8EAFF),

    tertiary = CantaloupeOrange,
    onTertiary = Color.White,
    tertiaryContainer = CantaloupeDark,
    onTertiaryContainer = CantaloupeLight,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = MidnightBlack,
    onBackground = TextPrimaryDark,
    surface = SurfaceLevel1,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceLevel2,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderStroke,
    outlineVariant = Color(0xFF2A3F4E),
    inverseSurface = LightSurface,
    inverseOnSurface = LightTextPrimary,
    surfaceTint = ElectricBlue
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
