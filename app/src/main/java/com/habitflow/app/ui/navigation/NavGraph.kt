package com.habitflow.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habitflow.app.ui.screens.habits.AddEditHabitScreen
import com.habitflow.app.ui.screens.habits.HabitsScreen
import com.habitflow.app.ui.screens.settings.PrivacyPolicyScreen
import com.habitflow.app.ui.screens.settings.SettingsScreen
import com.habitflow.app.ui.screens.stats.StatsScreen
import com.habitflow.app.ui.screens.today.TodayScreen

@Composable
fun HabitFlowNavHost(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in BottomNavItem.entries.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                HFBottomNavBar(
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(Screen.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            composable(Screen.Today.route) {
                TodayScreen(
                    onAddHabit = {
                        navController.navigate(Screen.AddEditHabit.createRoute())
                    },
                    onHabitClick = { habitId ->
                        navController.navigate(Screen.AddEditHabit.createRoute(habitId))
                    }
                )
            }

            composable(Screen.Habits.route) {
                HabitsScreen(
                    onAddHabit = {
                        navController.navigate(Screen.AddEditHabit.createRoute())
                    },
                    onEditHabit = { habitId ->
                        navController.navigate(Screen.AddEditHabit.createRoute(habitId))
                    }
                )
            }

            composable(Screen.Stats.route) {
                StatsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                    onPrivacyPolicy = {
                        navController.navigate(Screen.PrivacyPolicy.route)
                    }
                )
            }

            composable(
                route = Screen.AddEditHabit.route,
                arguments = listOf(
                    navArgument("habitId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                ),
                enterTransition = {
                    slideInVertically(tween(350)) { it } + fadeIn(tween(350))
                },
                exitTransition = { fadeOut(tween(200)) },
                popExitTransition = {
                    slideOutVertically(tween(350)) { it } + fadeOut(tween(350))
                }
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
                AddEditHabitScreen(
                    habitId = if (habitId == -1L) null else habitId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PrivacyPolicy.route,
                enterTransition = {
                    slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
                }
            ) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
