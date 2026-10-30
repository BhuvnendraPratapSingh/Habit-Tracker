package com.habitflow.app.data.model

import com.habitflow.app.data.db.entity.Habit

/**
 * Combines a habit entity with its computed streak/stats info,
 * plus whether it's completed today. Used by the UI layer.
 */
data class HabitWithStats(
    val habit: Habit,
    val streakInfo: StreakInfo,
    val isCompletedToday: Boolean = false,
    val completionsThisWeek: Int = 0,
    val last7Days: List<Boolean> = emptyList()   // Most recent 7 days, index 0 = 6 days ago
)
