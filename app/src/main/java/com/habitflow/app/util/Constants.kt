package com.habitflow.app.util

object Constants {

    // DataStore / SharedPreferences
    const val PREFS_NAME = "habitflow_prefs"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    const val KEY_FIRST_LAUNCH = "first_launch"

    // Notifications
    const val CHANNEL_REMINDERS = "reminders_channel"
    const val NOTIFICATION_ID_BASE = 5000

    // Habit colors — iOS-inspired pastels
    val HABIT_COLORS = listOf(
        "#A78BFA",  // Lavender
        "#F472B6",  // Pink
        "#FB923C",  // Orange
        "#FBBF24",  // Amber
        "#34D399",  // Emerald
        "#60A5FA",  // Blue
        "#818CF8",  // Indigo
        "#F87171",  // Red
        "#2DD4BF",  // Teal
        "#A3E635",  // Lime
        "#E879F9",  // Fuchsia
        "#38BDF8"   // Sky
    )

    // Habit icons (Material icon names)
    val HABIT_ICONS = listOf(
        "CheckCircle",
        "FitnessCenter",
        "MenuBook",
        "SelfImprovement",
        "WaterDrop",
        "DirectionsRun",
        "Bedtime",
        "Restaurant",
        "Code",
        "Brush",
        "MusicNote",
        "School",
        "Favorite",
        "EmojiNature",
        "SportsEsports",
        "Savings"
    )
}
