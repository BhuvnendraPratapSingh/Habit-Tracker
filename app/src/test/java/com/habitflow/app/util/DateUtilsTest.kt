package com.habitflow.app.util

import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Unit tests for DateUtils.
 * Pure Kotlin — no Android dependencies.
 */
class DateUtilsTest {

    // ── Format / Parse ──────────────────────────────────────────

    @Test
    fun `today returns ISO format yyyy-MM-dd`() {
        val today = DateUtils.today()
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `format and parse are inverse operations`() {
        val date = LocalDate.of(2026, 10, 30)
        val formatted = DateUtils.format(date)
        val parsed = DateUtils.parse(formatted)
        assertEquals(date, parsed)
    }

    @Test
    fun `daysAgo returns correct date string`() {
        val threeDaysAgo = DateUtils.daysAgo(3)
        val expected = DateUtils.format(LocalDate.now().minusDays(3))
        assertEquals(expected, threeDaysAgo)
    }

    // ── daysBetween ─────────────────────────────────────────────

    @Test
    fun `daysBetween same date returns 0`() {
        assertEquals(0L, DateUtils.daysBetween("2026-10-30", "2026-10-30"))
    }

    @Test
    fun `daysBetween consecutive dates returns 1`() {
        assertEquals(1L, DateUtils.daysBetween("2026-10-29", "2026-10-30"))
    }

    @Test
    fun `daysBetween week apart returns 7`() {
        assertEquals(7L, DateUtils.daysBetween("2026-10-23", "2026-10-30"))
    }

    // ── lastNDays ───────────────────────────────────────────────

    @Test
    fun `lastNDays returns correct count`() {
        val days = DateUtils.lastNDays(7)
        assertEquals(7, days.size)
    }

    @Test
    fun `lastNDays last element is today`() {
        val days = DateUtils.lastNDays(7)
        assertEquals(DateUtils.today(), days.last())
    }

    @Test
    fun `lastNDays first element is 6 days ago for count 7`() {
        val days = DateUtils.lastNDays(7)
        val expected = DateUtils.format(LocalDate.now().minusDays(6))
        assertEquals(expected, days.first())
    }

    // ── isScheduledDay ──────────────────────────────────────────

    @Test
    fun `daily frequency is always scheduled`() {
        val dates = DateUtils.lastNDays(7)
        dates.forEach { date ->
            assertTrue(
                "Date $date should be scheduled for daily",
                DateUtils.isScheduledDay(date, "daily", "")
            )
        }
    }

    @Test
    fun `weekly frequency is always scheduled`() {
        assertTrue(DateUtils.isScheduledDay(DateUtils.today(), "weekly", ""))
    }

    @Test
    fun `specific_days only matches target days`() {
        // Monday = 1
        val monday = findNextDayOfWeek(DayOfWeek.MONDAY)
        val mondayStr = DateUtils.format(monday)

        assertTrue(
            "Monday should be scheduled for [1,3,5]",
            DateUtils.isScheduledDay(mondayStr, "specific_days", "[1,3,5]")
        )

        // Tuesday = 2
        val tuesday = findNextDayOfWeek(DayOfWeek.TUESDAY)
        val tuesdayStr = DateUtils.format(tuesday)

        assertFalse(
            "Tuesday should NOT be scheduled for [1,3,5]",
            DateUtils.isScheduledDay(tuesdayStr, "specific_days", "[1,3,5]")
        )
    }

    @Test
    fun `specific_days with all days acts like daily`() {
        val dates = DateUtils.lastNDays(7)
        dates.forEach { date ->
            assertTrue(DateUtils.isScheduledDay(date, "specific_days", "[1,2,3,4,5,6,7]"))
        }
    }

    @Test
    fun `empty target days with specific_days matches nothing`() {
        assertFalse(DateUtils.isScheduledDay(DateUtils.today(), "specific_days", "[]"))
    }

    // ── dayOfWeek ───────────────────────────────────────────────

    @Test
    fun `dayOfWeek returns 1-7 for Monday-Sunday`() {
        val monday = findNextDayOfWeek(DayOfWeek.MONDAY)
        assertEquals(1, DateUtils.dayOfWeek(DateUtils.format(monday)))

        val sunday = findNextDayOfWeek(DayOfWeek.SUNDAY)
        assertEquals(7, DateUtils.dayOfWeek(DateUtils.format(sunday)))
    }

    // ── startOfWeek ─────────────────────────────────────────────

    @Test
    fun `startOfWeek returns Monday`() {
        val start = DateUtils.startOfWeek()
        assertEquals(DayOfWeek.MONDAY, start.dayOfWeek)
    }

    // ── friendlyDate ────────────────────────────────────────────

    @Test
    fun `friendlyDate today returns Today`() {
        assertEquals("Today", DateUtils.friendlyDate(DateUtils.today()))
    }

    @Test
    fun `friendlyDate yesterday returns Yesterday`() {
        val yesterday = DateUtils.format(LocalDate.now().minusDays(1))
        assertEquals("Yesterday", DateUtils.friendlyDate(yesterday))
    }

    // ── daysSinceCreation ───────────────────────────────────────

    @Test
    fun `daysSinceCreation today is 1`() {
        val nowMillis = System.currentTimeMillis()
        assertEquals(1, DateUtils.daysSinceCreation(nowMillis))
    }

    @Test
    fun `daysSinceCreation 7 days ago is 8`() {
        val sevenDaysAgo = LocalDate.now().minusDays(7)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        assertEquals(8, DateUtils.daysSinceCreation(sevenDaysAgo))
    }

    // ── Helpers ─────────────────────────────────────────────────

    private fun findNextDayOfWeek(target: DayOfWeek): LocalDate {
        var date = LocalDate.now()
        while (date.dayOfWeek != target) {
            date = date.plusDays(1)
        }
        return date
    }
}
