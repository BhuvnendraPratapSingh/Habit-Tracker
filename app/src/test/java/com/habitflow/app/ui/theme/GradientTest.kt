package com.habitflow.app.ui.theme

import androidx.compose.ui.graphics.Brush
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for gradient brush definitions.
 * Verifies that gradient objects are created without errors.
 */
class GradientTest {

    @Test
    fun `NeonHorizonBrush is a valid Brush`() {
        val brush: Brush = NeonHorizonBrush
        assertNotNull(brush)
    }

    @Test
    fun `IceGlowBrush is a valid Brush`() {
        val brush: Brush = IceGlowBrush
        assertNotNull(brush)
    }

    @Test
    fun `AmberBrush is a valid Brush`() {
        val brush: Brush = AmberBrush
        assertNotNull(brush)
    }
}
