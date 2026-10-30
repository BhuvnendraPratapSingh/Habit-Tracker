package com.habitflow.app.data.model

/**
 * Parsed representation of a habit's schedule.
 */
sealed class HabitFrequency {
    /** Habit should be done every day. */
    data object Daily : HabitFrequency()

    /** Habit should be done [timesPerWeek] times in any 7-day window. */
    data class Weekly(val timesPerWeek: Int) : HabitFrequency()

    /**
     * Habit is done on specific days of the week.
     * @param days Set of day-of-week indices: 1=Monday, 7=Sunday
     */
    data class SpecificDays(val days: Set<Int>) : HabitFrequency()

    companion object {
        fun fromHabit(frequencyType: String, frequencyValue: Int, targetDays: String): HabitFrequency {
            return when (frequencyType) {
                "weekly" -> Weekly(frequencyValue)
                "specific_days" -> {
                    val days = targetDays
                        .removeSurrounding("[", "]")
                        .split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .toSet()
                    SpecificDays(days)
                }
                else -> Daily
            }
        }
    }
}
