package com.habitflow.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary Palette (Lavender-based) ────────────────────────
val Lavender50 = Color(0xFFF5F3FF)
val Lavender100 = Color(0xFFEDE9FE)
val Lavender200 = Color(0xFFDDD6FE)
val Lavender300 = Color(0xFFC4B5FD)
val Lavender400 = Color(0xFFA78BFA)
val Lavender500 = Color(0xFF8B5CF6)
val Lavender600 = Color(0xFF7C3AED)
val Lavender700 = Color(0xFF6D28D9)

// ── iOS-Inspired Accent Colors ──────────────────────────────
val IOSBlue = Color(0xFF007AFF)
val IOSGreen = Color(0xFF34C759)
val IOSOrange = Color(0xFFFF9500)
val IOSRed = Color(0xFFFF3B30)
val IOSPink = Color(0xFFFF2D55)
val IOSTeal = Color(0xFF5AC8FA)
val IOSIndigo = Color(0xFF5856D6)
val IOSYellow = Color(0xFFFFCC00)

// ── Backgrounds (Light) ─────────────────────────────────────
val LightBackground = Color(0xFFF8F7FC)        // Soft off-white with lavender tint
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF2F1F6)
val LightCardBackground = Color(0xFFFFFFFF)
val LightOutline = Color(0xFFE5E5EA)

// ── Backgrounds (Dark) ──────────────────────────────────────
val DarkBackground = Color(0xFF1C1B22)
val DarkSurface = Color(0xFF2C2B33)
val DarkSurfaceVariant = Color(0xFF39383F)
val DarkCardBackground = Color(0xFF2C2B33)
val DarkOutline = Color(0xFF48474E)

// ── Text Colors ─────────────────────────────────────────────
val LightTextPrimary = Color(0xFF1C1B1F)
val LightTextSecondary = Color(0xFF6B6B73)
val LightTextTertiary = Color(0xFFA0A0A8)

val DarkTextPrimary = Color(0xFFF4F4F5)
val DarkTextSecondary = Color(0xFFA1A1AA)
val DarkTextTertiary = Color(0xFF71717A)

// ── Habit Colors (Pastels for cards) ────────────────────────
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

// ── Streak & Achievement Colors ─────────────────────────────
val StreakFire = Color(0xFFFF6B35)
val StreakGold = Color(0xFFFFD700)
val StreakSilver = Color(0xFFC0C0C0)

// ── Functional ──────────────────────────────────────────────
val SuccessGreen = Color(0xFF34D399)
val WarningOrange = Color(0xFFFB923C)
val ErrorRed = Color(0xFFF87171)

/**
 * Helper to parse "#RRGGBB" hex strings to Compose Color.
 * Returns Lavender400 as fallback if parsing fails.
 */
fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val longColor = cleanHex.toLong(16)
        Color(0xFF000000 or longColor)
    } catch (_: NumberFormatException) {
        Lavender400
    }
}
