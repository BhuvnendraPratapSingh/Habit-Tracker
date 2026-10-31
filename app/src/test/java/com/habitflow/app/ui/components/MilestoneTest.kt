package com.habitflow.app.ui.components

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for milestone detection logic.
 */
class MilestoneTest {

    @Test
    fun `7-day streak is a milestone`() {
        val result = getMilestoneTitle(7)
        assertNotNull(result)
        assertEquals("7-Day Spark!", result!!.second)
    }

    @Test
    fun `14-day streak is a milestone`() {
        assertNotNull(getMilestoneTitle(14))
    }

    @Test
    fun `21-day streak is a milestone`() {
        val result = getMilestoneTitle(21)
        assertNotNull(result)
        assertTrue(result!!.second.contains("21"))
    }

    @Test
    fun `30-day streak is a milestone`() {
        assertNotNull(getMilestoneTitle(30))
    }

    @Test
    fun `50-day streak is a milestone`() {
        assertNotNull(getMilestoneTitle(50))
    }

    @Test
    fun `100-day streak is a milestone`() {
        assertNotNull(getMilestoneTitle(100))
    }

    @Test
    fun `365-day streak is a milestone`() {
        assertNotNull(getMilestoneTitle(365))
    }

    @Test
    fun `non-milestone streaks return null`() {
        listOf(1, 2, 3, 5, 6, 8, 10, 15, 20, 25, 31, 99, 200).forEach { days ->
            assertNull("Day $days should NOT be a milestone", getMilestoneTitle(days))
        }
    }

    @Test
    fun `milestone returns emoji and title pair`() {
        val result = getMilestoneTitle(7)!!
        assertTrue("Emoji should not be empty", result.first.isNotEmpty())
        assertTrue("Title should not be empty", result.second.isNotEmpty())
    }
}
