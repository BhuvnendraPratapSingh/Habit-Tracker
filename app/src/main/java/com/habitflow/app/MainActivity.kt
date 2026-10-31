package com.habitflow.app

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.ui.navigation.HabitFlowNavHost
import com.habitflow.app.ui.theme.HabitFlowTheme
import com.habitflow.app.util.Constants
import com.habitflow.app.util.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var repository: HabitRepository

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* User's choice is respected — no further action needed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        rescheduleRemindersOnStart()

        setContent {
            /**
             * BUG-14 FIX: Dark mode follows system by default.
             * - If the user has never toggled the switch, follow system theme.
             * - Once they explicitly toggle it, use their preference.
             * - Changing system dark mode in Android Settings is reflected immediately.
             */
            val systemDark = isSystemInDarkTheme()
            val userHasExplicitChoice = prefs.contains(Constants.KEY_DARK_MODE)

            var darkMode by remember {
                mutableStateOf(
                    if (userHasExplicitChoice) prefs.getBoolean(Constants.KEY_DARK_MODE, false)
                    else systemDark
                )
            }

            // React to system theme changes when no explicit user choice
            if (!userHasExplicitChoice) {
                darkMode = systemDark
            }

            HabitFlowTheme(darkTheme = darkMode) {
                HabitFlowNavHost(
                    darkMode = darkMode,
                    onDarkModeChange = { enabled ->
                        darkMode = enabled
                        prefs.edit().putBoolean(Constants.KEY_DARK_MODE, enabled).apply()
                    }
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * BUG-15 partial FIX: Uses lifecycleScope instead of unscoped CoroutineScope.
     * Coroutine is cancelled if the Activity is destroyed.
     */
    private fun rescheduleRemindersOnStart() {
        lifecycleScope.launch(Dispatchers.IO) {
            val habits = repository.getHabitsWithReminders()
            ReminderScheduler.rescheduleAll(applicationContext, habits)
        }
    }
}
