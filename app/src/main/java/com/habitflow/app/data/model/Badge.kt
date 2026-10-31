package com.habitflow.app.data.model

/**
 * All earnable badges in the app.
 * Badges are computed from existing streak/completion data — no extra DB table needed.
 */
enum class Badge(
    val emoji: String,
    val title: String,
    val description: String,
    val tier: BadgeTier
) {
    // Streak-based
    FIRST_FLAME("\uD83D\uDD25", "First Flame", "Complete a habit 3 days in a row", BadgeTier.BRONZE),
    WEEK_WARRIOR("\u2694\uFE0F", "Week Warrior", "7-day streak on any habit", BadgeTier.BRONZE),
    TWO_WEEK_TITAN("\u26A1", "Two-Week Titan", "14-day streak on any habit", BadgeTier.SILVER),
    HABIT_FORMED("\uD83E\uDDE0", "Habit Formed", "21-day streak — science says it's a habit now", BadgeTier.SILVER),
    MONTHLY_MASTER("\uD83C\uDFC6", "Monthly Master", "30-day streak on any habit", BadgeTier.GOLD),
    FIFTY_DAYS("\u2B50", "Fifty & Fabulous", "50-day streak", BadgeTier.GOLD),
    CENTURION("\uD83D\uDC8E", "Centurion", "100-day streak — you're unstoppable", BadgeTier.DIAMOND),

    // Completion-based
    FIRST_STEP("\uD83D\uDC63", "First Step", "Complete your first habit ever", BadgeTier.BRONZE),
    TEN_DOWN("\uD83C\uDFAF", "Ten Down", "Complete 10 total habits", BadgeTier.BRONZE),
    FIFTY_CLUB("\uD83C\uDF1F", "Fifty Club", "Complete 50 total habits", BadgeTier.SILVER),
    CENTURY_MARK("\uD83D\uDCAF", "Century Mark", "Complete 100 total habits", BadgeTier.SILVER),
    FIVE_HUNDRED("\uD83D\uDE80", "Rocket Rider", "Complete 500 total habits", BadgeTier.GOLD),
    THOUSAND("\uD83D\uDC51", "Thousand Strong", "Complete 1000 total habits", BadgeTier.DIAMOND),

    // Perfect day based
    PERFECT_DAY("\u2728", "Perfect Day", "Complete all habits in a single day", BadgeTier.BRONZE),
    PERFECT_WEEK("\uD83C\uDF1E", "Perfect Week", "7 perfect days in a row", BadgeTier.GOLD),

    // Multi-habit
    MULTI_TRACKER("\uD83D\uDCCB", "Multi Tracker", "Track 3 or more habits at once", BadgeTier.BRONZE),
    HABIT_COLLECTOR("\uD83C\uDFA8", "Habit Collector", "Track 5 or more habits at once", BadgeTier.SILVER);
}

enum class BadgeTier(val label: String, val colorHex: Long) {
    BRONZE("Bronze", 0xFFCD7F32),
    SILVER("Silver", 0xFFC0C0C0),
    GOLD("Gold", 0xFFFFD700),
    DIAMOND("Diamond", 0xFFB9F2FF)
}

data class EarnedBadge(
    val badge: Badge,
    val isEarned: Boolean
)

/**
 * Computes which badges a user has earned based on their stats.
 * Pure function — no DB dependency. Pass in the aggregated data.
 */
object BadgeEvaluator {

    fun evaluate(
        bestStreakAcrossHabits: Int,
        totalCompletionsAcrossHabits: Int,
        activeHabitCount: Int,
        perfectDaysInRow: Int,
        hasPerfectDay: Boolean
    ): List<EarnedBadge> {
        return Badge.entries.map { badge ->
            val earned = when (badge) {
                // Streak
                Badge.FIRST_FLAME -> bestStreakAcrossHabits >= 3
                Badge.WEEK_WARRIOR -> bestStreakAcrossHabits >= 7
                Badge.TWO_WEEK_TITAN -> bestStreakAcrossHabits >= 14
                Badge.HABIT_FORMED -> bestStreakAcrossHabits >= 21
                Badge.MONTHLY_MASTER -> bestStreakAcrossHabits >= 30
                Badge.FIFTY_DAYS -> bestStreakAcrossHabits >= 50
                Badge.CENTURION -> bestStreakAcrossHabits >= 100
                // Completions
                Badge.FIRST_STEP -> totalCompletionsAcrossHabits >= 1
                Badge.TEN_DOWN -> totalCompletionsAcrossHabits >= 10
                Badge.FIFTY_CLUB -> totalCompletionsAcrossHabits >= 50
                Badge.CENTURY_MARK -> totalCompletionsAcrossHabits >= 100
                Badge.FIVE_HUNDRED -> totalCompletionsAcrossHabits >= 500
                Badge.THOUSAND -> totalCompletionsAcrossHabits >= 1000
                // Perfect
                Badge.PERFECT_DAY -> hasPerfectDay
                Badge.PERFECT_WEEK -> perfectDaysInRow >= 7
                // Multi
                Badge.MULTI_TRACKER -> activeHabitCount >= 3
                Badge.HABIT_COLLECTOR -> activeHabitCount >= 5
            }
            EarnedBadge(badge, earned)
        }
    }
}
