package com.habitflow.app.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// ── Premium Gradient Brushes ────────────────────────────────────
//
// Three reusable gradient brushes for masked text rendering.
// Compose applies these via TextStyle(brush = ...) which uses
// the shader-based gradient mask under the hood — equivalent to
// CSS `background-clip: text; -webkit-text-fill-color: transparent;`

/**
 * Neon Horizon: 135° diagonal from Electric Blue → Purple → Cantaloupe Orange.
 * Use for: hero headings, achievement titles, premium labels.
 */
val NeonHorizonBrush: Brush
    get() = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0297DE),  // Electric Blue
            Color(0xFF7D7BE6),  // Soft Purple
            Color(0xFFFC734D)   // Cantaloupe Orange
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)  // ~135°
    )

/**
 * Ice Glow: Vertical top-to-bottom from White → Light Cyan → Electric Blue.
 * Use for: stats headers, frosted glass overlays, subtle premium text.
 */
val IceGlowBrush: Brush
    get() = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFFFF),  // White
            Color(0xFF70C8F5),  // Light Cyan
            Color(0xFF0297DE)   // Electric Blue
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)  // 180° vertical
    )

/**
 * Amber: 135° diagonal from Peach → Cantaloupe → Deep Orange.
 * Use for: streak milestones, CTA highlights, warm accent text.
 */
val AmberBrush: Brush
    get() = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF997A),  // Peach
            Color(0xFFFC734D),  // Cantaloupe
            Color(0xFFE04E24)   // Deep Orange
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)  // ~135°
    )

// ── Convenience Composable ──────────────────────────────────────

/**
 * Renders text with a gradient brush applied as a shader mask.
 *
 * @param text     The text content to display.
 * @param brush    One of [NeonHorizonBrush], [IceGlowBrush], [AmberBrush], or a custom Brush.
 * @param style    Base text style — font, size, weight. The brush overrides the color.
 * @param modifier Standard Compose modifier.
 *
 * Usage:
 * ```
 * GradientText(
 *     text = "21-Day Streak!",
 *     brush = NeonHorizonBrush,
 *     style = MaterialTheme.typography.headlineMedium
 * )
 * ```
 */
@Composable
fun GradientText(
    text: String,
    brush: Brush,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(brush = brush),
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

// ── Gradient Style Helpers ──────────────────────────────────────
// For when you need the TextStyle directly (e.g. in AnnotatedString builders)

/** Returns a TextStyle with the Neon Horizon gradient applied. */
fun TextStyle.neonHorizon(): TextStyle = copy(brush = NeonHorizonBrush)

/** Returns a TextStyle with the Ice Glow gradient applied. */
fun TextStyle.iceGlow(): TextStyle = copy(brush = IceGlowBrush)

/** Returns a TextStyle with the Amber gradient applied. */
fun TextStyle.amber(): TextStyle = copy(brush = AmberBrush)
