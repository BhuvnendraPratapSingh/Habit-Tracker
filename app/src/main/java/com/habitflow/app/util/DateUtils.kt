package com.habitflow.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Date utility functions used throughout the app.
 * All dates are stored as ISO "yyyy-MM-dd" strings.
 */
object DateUtils {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE  // yyyy-MM-dd

    fun today(): String = LocalDate.now().format(formatter)

    fun todayDate(): LocalDate = LocalDate.now()

    fun format(date: LocalDate): String = date.format(formatter)

    fun parse(dateStr: String): LocalDate = LocalDate.parse(dateStr, formatter)

    fun daysAgo(days: Int): String = LocalDate.now().minusDays(days.toLong()).format(formatter)

    fun daysBetween(start: String, end: String): Long {
        return ChronoUnit.DAYS.between(parse(start), parse(end))
    }

    fun daysSinceCreation(createdAtMillis: Long): Int {
        val createdDate = java.time.Instant.ofEpochMilli(createdAtMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return ChronoUnit.DAYS.between(createdDate, LocalDate.now()).toInt() + 1
    }

    /**
     * Returns the last [count] dates ending with today, formatted as strings.
     */
    fun lastNDays(count: Int): List<String> {
        val today = LocalDate.now()
        return (count - 1 downTo 0).map { today.minusDays(it.toLong()).format(formatter) }
    }

    /**
     * Returns start-of-week (Monday) for the given date.
     */
    fun startOfWeek(date: LocalDate = LocalDate.now()): LocalDate {
        return date.with(DayOfWeek.MONDAY)
    }

    /**
     * Returns the day-of-week as 1=Monday..7=Sunday for a date string.
     */
    fun dayOfWeek(dateStr: String): Int {
        return parse(dateStr).dayOfWeek.value
    }

    /**
     * Checks if a given date falls on one of the target days (1=Mon..7=Sun).
     */
    fun isScheduledDay(dateStr: String, frequencyType: String, targetDays: String): Boolean {
        return when (frequencyType) {
            "daily" -> true
            "specific_days" -> {
                val days = targetDays
                    .removeSurrounding("[", "]")
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toSet()
                dayOfWeek(dateStr) in days
            }
            "weekly" -> true  // Weekly habits can be done on any day
            else -> true
        }
    }

    /**
     * Short day name for display.
     */
    fun shortDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "M"
            2 -> "T"
            3 -> "W"
            4 -> "T"
            5 -> "F"
            6 -> "S"
            7 -> "S"
            else -> ""
        }
    }

    /**
     * Returns a friendly display for a date relative to today.
     */
    fun friendlyDate(dateStr: String): String {
        val date = parse(dateStr)
        val today = LocalDate.now()
        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            date == today.plusDays(1) -> "Tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
        }
    }
}
