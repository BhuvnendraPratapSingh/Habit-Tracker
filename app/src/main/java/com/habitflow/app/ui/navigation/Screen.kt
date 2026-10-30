package com.habitflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * All navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    data object Today : Screen("today")
    data object Habits : Screen("habits")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
    data object AddEditHabit : Screen("add_edit_habit?habitId={habitId}") {
        fun createRoute(habitId: Long? = null): String {
            return if (habitId != null) "add_edit_habit?habitId=$habitId"
            else "add_edit_habit"
        }
    }
    data object PrivacyPolicy : Screen("privacy_policy")
}

/**
 * Bottom navigation tab items.
 */
enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Today("today", "Today", Icons.Rounded.CheckCircle),
    Habits("habits", "Habits", Icons.Rounded.List),
    Stats("stats", "Stats", Icons.Rounded.BarChart),
    Settings("settings", "Settings", Icons.Rounded.Settings)
}
