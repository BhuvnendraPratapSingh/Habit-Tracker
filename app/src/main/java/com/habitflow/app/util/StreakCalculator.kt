package com.habitflow.app.util

import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.model.StreakInfo
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

/**
 * Calculates streak information for a habit based on its completion dates.
 * The algorithm respects the habit's frequency schedule — only scheduled days
 * count toward or break a streak.
 */
object StreakCalculator {

    /**
     * Computes full streak info for a habit given its sorted completion dates.
     *
     * @param habit The habit entity.
     * @param completionDates Sorted list of "yyyy-MM-dd" dates where habit was completed (descending).
     */
    fun calculate(habit: Habit, completionDates: List<String>): StreakInfo {
        if (completionDates.isEmpty()) {
            return StreakInfo(
                runningDays = DateUtils.daysSinceCreation(habit.createdAt)
            )
        }

        val completedSet = completionDates.toSet()
        val today = DateUtils.todayDate()
        val createdDate = java.time.Instant.ofEpochMilli(habit.createdAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()

        val runningDays = DateUtils.daysSinceCreation(habit.createdAt)
        val totalCompletions = completionDates.size

        // Build list of all scheduled days from creation to today
        val scheduledDays = mutableListOf<LocalDate>()
        var cursor = today
        while (!cursor.isBefore(createdDate)) {
            val dateStr = DateUtils.format(cursor)
            if (DateUtils.isScheduledDay(dateStr, habit.frequencyType, habit.targetDays)) {
                scheduledDays.add(cursor)
            }
            cursor = cursor.minusDays(1)
        }
        // scheduledDays is now in descending order (today first)

        // Calculate current streak (walking backwards from today/yesterday)
        var currentStreak = 0
        for (day in scheduledDays) {
            val dateStr = DateUtils.format(day)
            if (dateStr in completedSet) {
                currentStreak++
            } else if (day == today) {
                // Today hasn't been completed yet but day isn't over — don't break streak
                continue
            } else {
                break
            }
        }

        // Calculate best streak (scan all scheduled days)
        var bestStreak = 0
        var tempStreak = 0
        // Process in ascending order for best streak
        for (day in scheduledDays.reversed()) {
            val dateStr = DateUtils.format(day)
            if (dateStr in completedSet) {
                tempStreak++
                bestStreak = max(bestStreak, tempStreak)
            } else {
                tempStreak = 0
            }
        }

        // Completion rate: completions / total scheduled days
        val totalScheduled = scheduledDays.size.coerceAtLeast(1)
        val completionRate = totalCompletions.toFloat() / totalScheduled.toFloat()

        // Habit strength: weighted average over last 30 days with exponential decay
        val strength = calculateStrength(habit, completedSet, today, createdDate)

        return StreakInfo(
            currentStreak = currentStreak,
            bestStreak = max(bestStreak, currentStreak),
            totalCompletions = totalCompletions,
            runningDays = runningDays,
            completionRate = min(completionRate, 1f),
            strength = strength
        )
    }

    /**
     * Calculates a "habit strength" score (0f–1f).
     * Uses exponential weighted average: recent days matter more.
     * Looking at the last 30 scheduled days.
     */
    private fun calculateStrength(
        habit: Habit,
        completedSet: Set<String>,
        today: LocalDate,
        createdDate: LocalDate
    ): Float {
        var weightedSum = 0.0
        var weightTotal = 0.0
        var scheduledCount = 0
        var cursor = today

        while (!cursor.isBefore(createdDate) && scheduledCount < 30) {
            val dateStr = DateUtils.format(cursor)
            if (DateUtils.isScheduledDay(dateStr, habit.frequencyType, habit.targetDays)) {
                // Exponential decay: weight = 0.95^(days from most recent scheduled day)
                val weight = Math.pow(0.95, scheduledCount.toDouble())
                weightTotal += weight
                if (dateStr in completedSet) {
                    weightedSum += weight
                }
                scheduledCount++
            }
            cursor = cursor.minusDays(1)
        }

        return if (weightTotal > 0) (weightedSum / weightTotal).toFloat() else 0f
    }
}
