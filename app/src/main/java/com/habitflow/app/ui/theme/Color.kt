package com.habitflow.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Design System Accent Tokens ─────────────────────────────────
val ElectricBlue = Color(0xFF0297DE)
val CantaloupeOrange = Color(0xFFFC734D)

// ── Primary Palette (Lavender — retained for habit cards) ───────
val Lavender50 = Color(0xFFF5F3FF)
val Lavender100 = Color(0xFFEDE9FE)
val Lavender200 = Color(0xFFDDD6FE)
val Lavender300 = Color(0xFFC4B5FD)
val Lavender400 = Color(0xFFA78BFA)
val Lavender500 = Color(0xFF8B5CF6)
val Lavender600 = Color(0xFF7C3AED)
val Lavender700 = Color(0xFF6D28D9)

// ── iOS-Inspired Accent Colors ──────────────────────────────────
val IOSBlue = Color(0xFF007AFF)
val IOSGreen = Color(0xFF34C759)
val IOSOrange = Color(0xFFFF9500)
val IOSRed = Color(0xFFFF3B30)
val IOSPink = Color(0xFFFF2D55)
val IOSTeal = Color(0xFF5AC8FA)
val IOSIndigo = Color(0xFF5856D6)
val IOSYellow = Color(0xFFFFCC00)

// ── Light Mode Backgrounds ──────────────────────────────────────
val LightBackground = Color(0xFFF8F7FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF2F1F6)
val LightCardBackground = Color(0xFFFFFFFF)
val LightOutline = Color(0xFFE5E5EA)

// ── Dark Mode Design System Tokens ──────────────────────────────
val MidnightBlack = Color(0xFF02070A)        // Base Canvas
val SurfaceLevel1 = Color(0xFF0A131A)        // Cards, Sheets
val SurfaceLevel2 = Color(0xFF121E27)        // Inputs, Active Containers
val BorderStroke = Color(0xFF1E313E)         // Border / Stroke
val TextPrimaryDark = Color(0xFFF3F7FA)      // Primary text on dark
val TextSecondaryDark = Color(0xFF8B9BA8)    // Secondary text on dark

// ── Dark Mode Legacy (retained for backward compat) ─────────────
val DarkBackground = MidnightBlack
val DarkSurface = SurfaceLevel1
val DarkSurfaceVariant = SurfaceLevel2
val DarkCardBackground = SurfaceLevel1
val DarkOutline = BorderStroke

// ── Light Mode Text ─────────────────────────────────────────────
val LightTextPrimary = Color(0xFF1C1B1F)
val LightTextSecondary = Color(0xFF6B6B73)
val LightTextTertiary = Color(0xFFA0A0A8)

// ── Dark Mode Text ──────────────────────────────────────────────
val DarkTextPrimary = TextPrimaryDark
val DarkTextSecondary = TextSecondaryDark
val DarkTextTertiary = Color(0xFF5A6A78)

// ── Habit Colors (Pastels for cards) ────────────────────────────
val HabitLavender = Color(0xFFA78BFA)
val HabitPink = Color(0xFFF472B6)
val HabitOrange = Color(0xFFFB923C)
val HabitAmber = Color(0xFFFBBF24)
val HabitEmerald = Color(0xFF34D399)
val HabitBlue = Color(0xFF60A5FA)
val HabitIndigo = Color(0xFF818CF8)
val HabitRed = Color(0xFFF87171)
val HabitTeal = Color(0xFF2DD4BF)
val HabitLime = Color(0xFFA3E635)
val HabitFuchsia = Color(0xFFE879F9)
val HabitSky = Color(0xFF38BDF8)

// ── Streak & Achievement Colors ─────────────────────────────────
val StreakFire = Color(0xFFFF6B35)
val StreakGold = Color(0xFFFFD700)
val StreakSilver = Color(0xFFC0C0C0)

// ── Functional ──────────────────────────────────────────────────
val SuccessGreen = Color(0xFF34D399)
val WarningOrange = Color(0xFFFB923C)
val ErrorRed = Color(0xFFF87171)

// ── Electric Blue Tint Variants (for containers) ────────────────
val ElectricBlueLight = Color(0xFFD9F0FB)    // Light container
val ElectricBlueDark = Color(0xFF014A6E)     // Dark container

// ── Cantaloupe Tint Variants ────────────────────────────────────
val CantaloupeLight = Color(0xFFFEE4DB)      // Light container
val CantaloupeDark = Color(0xFF7A3826)       // Dark container

/**
 * Helper to parse "#RRGGBB" hex strings to Compose Color.
 * Returns ElectricBlue as fallback to match the design system primary accent.
 */
fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val longColor = cleanHex.toLong(16)
        Color(0xFF000000 or longColor)
    } catch (_: NumberFormatException) {
        ElectricBlue
    }
}
