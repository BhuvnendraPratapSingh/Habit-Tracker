package com.habitflow.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Font Families ───────────────────────────────────────────────
// Uses system sans-serif (Roboto on most Android devices).
// No external dependencies — works on every device including
// Huawei (no GMS), Amazon Fire, and custom ROMs.
//
// If bundled .ttf files are added later to res/font/, swap these
// with Font(R.font.plus_jakarta_sans_semibold, ...) etc.

/** Headings font — system sans-serif with tight heading weights. */
val HeadingFont: FontFamily = FontFamily.SansSerif

/** Body/data font — system sans-serif for readability. */
val BodyFont: FontFamily = FontFamily.SansSerif

// ── Typography Scale ────────────────────────────────────────────
// Display & Headings → HeadingFont (ExtraBold / Bold / SemiBold)
// Body, Labels, Data → BodyFont (Normal / Medium / SemiBold)

val HabitFlowTypography = Typography(

    // ── Display ─────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily = HeadingFont,
        fontSize = 34.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 41.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = HeadingFont,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = HeadingFont,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 30.sp
    ),

    // ── Headlines ───────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = HeadingFont,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HeadingFont,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = HeadingFont,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    ),

    // ── Titles ──────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily = HeadingFont,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFont,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFont,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ── Body ────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontSize = 17.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFont,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFont,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),

    // ── Labels ──────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily = BodyFont,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFont,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFont,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
