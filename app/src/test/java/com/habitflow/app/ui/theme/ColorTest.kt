package com.habitflow.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Color tokens and parseHexColor utility.
 */
class ColorTest {

    // ── Design system token values ──────────────────────────────

    @Test
    fun `ElectricBlue has correct hex`() {
        assertEquals(Color(0xFF0297DE), ElectricBlue)
    }

    @Test
    fun `CantaloupeOrange has correct hex`() {
        assertEquals(Color(0xFFFC734D), CantaloupeOrange)
    }

    @Test
    fun `MidnightBlack has correct hex`() {
        assertEquals(Color(0xFF02070A), MidnightBlack)
    }

    @Test
    fun `SurfaceLevel1 has correct hex`() {
        assertEquals(Color(0xFF0A131A), SurfaceLevel1)
    }

    @Test
    fun `SurfaceLevel2 has correct hex`() {
        assertEquals(Color(0xFF121E27), SurfaceLevel2)
    }

    @Test
    fun `BorderStroke has correct hex`() {
        assertEquals(Color(0xFF1E313E), BorderStroke)
    }

    @Test
    fun `TextPrimaryDark has correct hex`() {
        assertEquals(Color(0xFFF3F7FA), TextPrimaryDark)
    }

    @Test
    fun `TextSecondaryDark has correct hex`() {
        assertEquals(Color(0xFF8B9BA8), TextSecondaryDark)
    }

    // ── Legacy aliases point to new tokens ──────────────────────

    @Test
    fun `DarkBackground aliases MidnightBlack`() {
        assertEquals(MidnightBlack, DarkBackground)
    }

    @Test
    fun `DarkSurface aliases SurfaceLevel1`() {
        assertEquals(SurfaceLevel1, DarkSurface)
    }

    @Test
    fun `DarkOutline aliases BorderStroke`() {
        assertEquals(BorderStroke, DarkOutline)
    }

    // ── parseHexColor ───────────────────────────────────────────

    @Test
    fun `parseHexColor with hash prefix`() {
        val color = parseHexColor("#FF0000")
        assertEquals(Color(0xFFFF0000), color)
    }

    @Test
    fun `parseHexColor without hash prefix`() {
        val color = parseHexColor("00FF00")
        assertEquals(Color(0xFF00FF00), color)
    }

    @Test
    fun `parseHexColor with invalid string returns fallback`() {
        val color = parseHexColor("not-a-color")
        assertEquals(ElectricBlue, color)
    }

    @Test
    fun `parseHexColor with empty string returns fallback`() {
        val color = parseHexColor("")
        assertEquals(ElectricBlue, color)
    }

    @Test
    fun `parseHexColor with Electric Blue hex`() {
        val color = parseHexColor("#0297DE")
        assertEquals(ElectricBlue, color)
    }

    // ── WCAG contrast check (approximate) ───────────────────────
    // TextPrimaryDark (#F3F7FA) on MidnightBlack (#02070A)
    // Relative luminance: F3F7FA ≈ 0.92, 02070A ≈ 0.003
    // Contrast ratio ≈ (0.92 + 0.05) / (0.003 + 0.05) ≈ 18.3:1  → WCAG AAA ✓

    @Test
    fun `primary text on dark canvas meets WCAG AA (contrast gt 4_5)`() {
        val textLum = relativeLuminance(TextPrimaryDark)
        val bgLum = relativeLuminance(MidnightBlack)
        val ratio = contrastRatio(textLum, bgLum)
        assertTrue("Contrast ratio $ratio should be >= 4.5", ratio >= 4.5)
    }

    @Test
    fun `secondary text on surface1 meets WCAG AA (contrast gt 4_5)`() {
        val textLum = relativeLuminance(TextSecondaryDark)
        val bgLum = relativeLuminance(SurfaceLevel1)
        val ratio = contrastRatio(textLum, bgLum)
        assertTrue("Contrast ratio $ratio should be >= 4.5", ratio >= 4.5)
    }

    // ── Helpers ─────────────────────────────────────────────────

    private fun relativeLuminance(c: Color): Double {
        fun linearize(v: Float): Double {
            return if (v <= 0.03928) v / 12.92 else Math.pow(((v + 0.055) / 1.055).toDouble(), 2.4)
        }
        val r = linearize(c.red)
        val g = linearize(c.green)
        val b = linearize(c.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun contrastRatio(l1: Double, l2: Double): Double {
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }
}
