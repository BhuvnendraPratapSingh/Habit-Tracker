package com.habitflow.app.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for BadgeEvaluator.
 * Pure Kotlin — no Android dependencies.
 */
class BadgeEvaluatorTest {

    private fun evaluate(
        bestStreak: Int = 0,
        totalCompletions: Int = 0,
        activeHabits: Int = 1,
        perfectDaysInRow: Int = 0,
        hasPerfectDay: Boolean = false
    ): List<EarnedBadge> {
        return BadgeEvaluator.evaluate(
            bestStreakAcrossHabits = bestStreak,
            totalCompletionsAcrossHabits = totalCompletions,
            activeHabitCount = activeHabits,
            perfectDaysInRow = perfectDaysInRow,
            hasPerfectDay = hasPerfectDay
        )
    }

    private fun List<EarnedBadge>.isEarned(badge: Badge): Boolean =
        first { it.badge == badge }.isEarned

    // ── Zero state ──────────────────────────────────────────────

    @Test
    fun `fresh user has no badges earned`() {
        val badges = evaluate()
        val earnedCount = badges.count { it.isEarned }
        assertEquals(0, earnedCount)
    }

    @Test
    fun `evaluate returns all badge types`() {
        val badges = evaluate()
        assertEquals(Badge.entries.size, badges.size)
    }

    // ── Streak badges ───────────────────────────────────────────

    @Test
    fun `3-day streak earns First Flame`() {
        val badges = evaluate(bestStreak = 3)
        assertTrue(badges.isEarned(Badge.FIRST_FLAME))
        assertFalse(badges.isEarned(Badge.WEEK_WARRIOR))
    }

    @Test
    fun `7-day streak earns Week Warrior and First Flame`() {
        val badges = evaluate(bestStreak = 7)
        assertTrue(badges.isEarned(Badge.FIRST_FLAME))
        assertTrue(badges.isEarned(Badge.WEEK_WARRIOR))
        assertFalse(badges.isEarned(Badge.TWO_WEEK_TITAN))
    }

    @Test
    fun `14-day streak earns up to Two Week Titan`() {
        val badges = evaluate(bestStreak = 14)
        assertTrue(badges.isEarned(Badge.FIRST_FLAME))
        assertTrue(badges.isEarned(Badge.WEEK_WARRIOR))
        assertTrue(badges.isEarned(Badge.TWO_WEEK_TITAN))
        assertFalse(badges.isEarned(Badge.HABIT_FORMED))
    }

    @Test
    fun `21-day streak earns Habit Formed`() {
        val badges = evaluate(bestStreak = 21)
        assertTrue(badges.isEarned(Badge.HABIT_FORMED))
    }

    @Test
    fun `30-day streak earns Monthly Master`() {
        val badges = evaluate(bestStreak = 30)
        assertTrue(badges.isEarned(Badge.MONTHLY_MASTER))
    }

    @Test
    fun `100-day streak earns Centurion`() {
        val badges = evaluate(bestStreak = 100)
        assertTrue(badges.isEarned(Badge.CENTURION))
        assertTrue(badges.isEarned(Badge.FIFTY_DAYS))
        assertTrue(badges.isEarned(Badge.MONTHLY_MASTER))
    }

    // ── Completion badges ───────────────────────────────────────

    @Test
    fun `1 completion earns First Step`() {
        val badges = evaluate(totalCompletions = 1)
        assertTrue(badges.isEarned(Badge.FIRST_STEP))
        assertFalse(badges.isEarned(Badge.TEN_DOWN))
    }

    @Test
    fun `10 completions earns Ten Down`() {
        val badges = evaluate(totalCompletions = 10)
        assertTrue(badges.isEarned(Badge.FIRST_STEP))
        assertTrue(badges.isEarned(Badge.TEN_DOWN))
    }

    @Test
    fun `50 completions earns Fifty Club`() {
        assertTrue(evaluate(totalCompletions = 50).isEarned(Badge.FIFTY_CLUB))
    }

    @Test
    fun `100 completions earns Century Mark`() {
        assertTrue(evaluate(totalCompletions = 100).isEarned(Badge.CENTURY_MARK))
    }

    @Test
    fun `500 completions earns Rocket Rider`() {
        assertTrue(evaluate(totalCompletions = 500).isEarned(Badge.FIVE_HUNDRED))
    }

    @Test
    fun `1000 completions earns Thousand Strong`() {
        assertTrue(evaluate(totalCompletions = 1000).isEarned(Badge.THOUSAND))
    }

    @Test
    fun `49 completions does NOT earn Fifty Club`() {
        assertFalse(evaluate(totalCompletions = 49).isEarned(Badge.FIFTY_CLUB))
    }

    // ── Perfect day / week ──────────────────────────────────────

    @Test
    fun `perfect day badge earned when hasPerfectDay is true`() {
        assertTrue(evaluate(hasPerfectDay = true).isEarned(Badge.PERFECT_DAY))
    }

    @Test
    fun `perfect day badge NOT earned when hasPerfectDay is false`() {
        assertFalse(evaluate(hasPerfectDay = false).isEarned(Badge.PERFECT_DAY))
    }

    @Test
    fun `7 consecutive perfect days earns Perfect Week`() {
        assertTrue(evaluate(perfectDaysInRow = 7).isEarned(Badge.PERFECT_WEEK))
    }

    @Test
    fun `6 consecutive perfect days does NOT earn Perfect Week`() {
        assertFalse(evaluate(perfectDaysInRow = 6).isEarned(Badge.PERFECT_WEEK))
    }

    // ── Multi-habit badges ──────────────────────────────────────

    @Test
    fun `3 active habits earns Multi Tracker`() {
        assertTrue(evaluate(activeHabits = 3).isEarned(Badge.MULTI_TRACKER))
    }

    @Test
    fun `2 active habits does NOT earn Multi Tracker`() {
        assertFalse(evaluate(activeHabits = 2).isEarned(Badge.MULTI_TRACKER))
    }

    @Test
    fun `5 active habits earns Habit Collector`() {
        val badges = evaluate(activeHabits = 5)
        assertTrue(badges.isEarned(Badge.MULTI_TRACKER))
        assertTrue(badges.isEarned(Badge.HABIT_COLLECTOR))
    }

    // ── Badge tiers ─────────────────────────────────────────────

    @Test
    fun `all badges have a tier assigned`() {
        Badge.entries.forEach { badge ->
            assertNotNull("Badge ${badge.name} should have a tier", badge.tier)
        }
    }

    @Test
    fun `First Flame is Bronze tier`() {
        assertEquals(BadgeTier.BRONZE, Badge.FIRST_FLAME.tier)
    }

    @Test
    fun `Centurion is Diamond tier`() {
        assertEquals(BadgeTier.DIAMOND, Badge.CENTURION.tier)
    }

    // ── Cumulative / edge cases ─────────────────────────────────

    @Test
    fun `power user earns many badges at once`() {
        val badges = evaluate(
            bestStreak = 50,
            totalCompletions = 200,
            activeHabits = 5,
            perfectDaysInRow = 10,
            hasPerfectDay = true
        )
        val earned = badges.filter { it.isEarned }
        // Should earn: FIRST_FLAME, WEEK_WARRIOR, TWO_WEEK_TITAN, HABIT_FORMED,
        // MONTHLY_MASTER, FIFTY_DAYS, FIRST_STEP, TEN_DOWN, FIFTY_CLUB,
        // CENTURY_MARK, PERFECT_DAY, PERFECT_WEEK, MULTI_TRACKER, HABIT_COLLECTOR
        assertTrue("Expected many badges, got ${earned.size}", earned.size >= 14)
    }
}
