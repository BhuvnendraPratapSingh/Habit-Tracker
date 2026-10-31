package com.habitflow.app.util

import com.habitflow.app.data.db.entity.Habit
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for StreakCalculator.
 * Pure Kotlin — no Android dependencies.
 */
class StreakCalculatorTest {

    private fun habit(
        frequencyType: String = "daily",
        targetDays: String = "",
        createdDaysAgo: Int = 30
    ): Habit {
        val createdAt = LocalDate.now().minusDays(createdDaysAgo.toLong())
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        return Habit(
            id = 1L,
            name = "Test Habit",
            frequencyType = frequencyType,
            targetDays = targetDays,
            createdAt = createdAt
        )
    }

    private fun datesFromToday(vararg daysAgo: Int): List<String> {
        return daysAgo.map { DateUtils.format(LocalDate.now().minusDays(it.toLong())) }
            .sortedDescending()
    }

    // ── Empty state ─────────────────────────────────────────────

    @Test
    fun `no completions returns zero streak`() {
        val result = StreakCalculator.calculate(habit(), emptyList())
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.bestStreak)
        assertEquals(0, result.totalCompletions)
        assertEquals(0f, result.completionRate, 0.001f)
    }

    // ── Current streak ──────────────────────────────────────────

    @Test
    fun `consecutive days from yesterday gives correct streak`() {
        // Yesterday, day before, day before that = 3-day streak
        val dates = datesFromToday(1, 2, 3)
        val result = StreakCalculator.calculate(habit(), dates)
        assertEquals(3, result.currentStreak)
    }

    @Test
    fun `today completed extends streak`() {
        val dates = datesFromToday(0, 1, 2)
        val result = StreakCalculator.calculate(habit(), dates)
        assertEquals(3, result.currentStreak)
    }

    @Test
    fun `today not completed does not break streak`() {
        // Yesterday and day before completed, today not
        val dates = datesFromToday(1, 2)
        val result = StreakCalculator.calculate(habit(), dates)
        assertEquals(2, result.currentStreak)
    }

    @Test
    fun `gap in completions breaks current streak`() {
        // Today, yesterday, then skip a day, then day 3-4
        val dates = datesFromToday(0, 1, 3, 4)
        val result = StreakCalculator.calculate(habit(), dates)
        assertEquals(2, result.currentStreak)  // Only today + yesterday
    }

    @Test
    fun `single completion today gives streak of 1`() {
        val dates = datesFromToday(0)
        val result = StreakCalculator.calculate(habit(), dates)
        assertEquals(1, result.currentStreak)
    }

    @Test
    fun `old completions with gap from today gives zero streak`() {
        // Completed 5 and 6 days ago, nothing since
        val dates = datesFromToday(5, 6)
        val result = StreakCalculator.calculate(habit(), dates)
        assertEquals(0, result.currentStreak)
    }

    // ── Best streak ─────────────────────────────────────────────

    @Test
    fun `best streak tracks the longest run`() {
        // Current: 2 days (today, yesterday)
        // Historical: 4 days (10,9,8,7 days ago)
        val dates = datesFromToday(0, 1, 7, 8, 9, 10)
        val result = StreakCalculator.calculate(habit(), dates)
        assertEquals(2, result.currentStreak)
        assertEquals(4, result.bestStreak)
    }

    @Test
    fun `best streak equals current when current is longest`() {
        val dates = datesFromToday(0, 1, 2, 3, 4)
        val result = StreakCalculator.calculate(habit(), dates)
        assertEquals(5, result.currentStreak)
        assertEquals(5, result.bestStreak)
    }

    // ── Specific days frequency ─────────────────────────────────

    @Test
    fun `specific days only counts scheduled days for streak`() {
        // Habit is Mon, Wed, Fri (1, 3, 5)
        val today = LocalDate.now()
        val habit = habit(
            frequencyType = "specific_days",
            targetDays = "[1,3,5]"
        )

        // Build dates: find the last 3 scheduled days and mark them complete
        val scheduledDates = mutableListOf<String>()
        var cursor = today
        var count = 0
        while (count < 3) {
            val dateStr = DateUtils.format(cursor)
            if (DateUtils.isScheduledDay(dateStr, "specific_days", "[1,3,5]")) {
                scheduledDates.add(dateStr)
                count++
            }
            cursor = cursor.minusDays(1)
        }

        val result = StreakCalculator.calculate(habit, scheduledDates)
        // Should be at least 2 (might be 3 if today is scheduled and completed)
        assertTrue(result.currentStreak >= 2)
    }

    // ── Total completions ───────────────────────────────────────

    @Test
    fun `total completions counts all entries`() {
        val dates = datesFromToday(0, 3, 7, 14, 21)
        val result = StreakCalculator.calculate(habit(), dates)
        assertEquals(5, result.totalCompletions)
    }

    // ── Completion rate ─────────────────────────────────────────

    @Test
    fun `completion rate is between 0 and 1`() {
        val dates = datesFromToday(0, 1, 2)
        val result = StreakCalculator.calculate(habit(createdDaysAgo = 10), dates)
        assertTrue(result.completionRate > 0f)
        assertTrue(result.completionRate <= 1f)
    }

    // ── Strength ────────────────────────────────────────────────

    @Test
    fun `strength is 0 with no completions`() {
        val result = StreakCalculator.calculate(habit(), emptyList())
        assertEquals(0f, result.strength, 0.001f)
    }

    @Test
    fun `strength is high with all recent completions`() {
        // Complete every day for the last 7 days
        val dates = (0..6).map { DateUtils.format(LocalDate.now().minusDays(it.toLong())) }
        val result = StreakCalculator.calculate(habit(), dates)
        assertTrue("Strength should be > 0.5, was ${result.strength}", result.strength > 0.5f)
    }
}
