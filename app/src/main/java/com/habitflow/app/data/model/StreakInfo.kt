package com.habitflow.app.data.model

/**
 * Aggregated streak and statistics data for a single habit.
 *
 * @param currentStreak Number of consecutive scheduled days completed up to today/yesterday.
 * @param bestStreak Longest ever streak of consecutive completions.
 * @param totalCompletions Total number of days the habit was marked complete.
 * @param runningDays Total number of days since the habit was created (including today).
 * @param completionRate Completion percentage (0f–1f) over the habit's lifetime on scheduled days.
 * @param strength Weighted score (0f–1f) emphasizing recent consistency (last 30 days).
 */
data class StreakInfo(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val runningDays: Int = 0,
    val completionRate: Float = 0f,
    val strength: Float = 0f
)
