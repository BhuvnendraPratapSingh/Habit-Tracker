package com.habitflow.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.receiver.ReminderReceiver
import java.util.Calendar

/**
 * Schedules and cancels daily reminders for habits using AlarmManager.
 * Each habit gets a unique PendingIntent keyed by its ID.
 */
object ReminderScheduler {

    /**
     * BUG-10 FIX: Safe Int request code from Long habitId.
     * Uses absolute value of hashCode to avoid negative values
     * and collisions from Long.toInt() overflow.
     */
    private fun safeRequestCode(habitId: Long): Int {
        return habitId.hashCode().and(0x7FFFFFFF) // Always positive
    }

    fun scheduleReminder(context: Context, habit: Habit) {
        if (habit.reminderHour < 0) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", habit.id)
            putExtra("habit_name", habit.name)
            putExtra("reminder_hour", habit.reminderHour)
            putExtra("reminder_minute", habit.reminderMinute)
        }
        val requestCode = safeRequestCode(habit.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, habit.reminderHour)
            set(Calendar.MINUTE, habit.reminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // BUG-9 FIX: Use exact alarms instead of inexact setRepeating().
        // setRepeating() on API 26+ is batched by the OS (up to 30 min late).
        // We schedule one exact alarm and reschedule the next day in ReminderReceiver.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+: Check if exact alarm permission is granted
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // Fallback: inexact but better than nothing
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            // API 26–30: setExactAndAllowWhileIdle is available without permission
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, habitId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val requestCode = safeRequestCode(habitId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Reschedules all active habit reminders. Called after boot and on app start.
     */
    suspend fun rescheduleAll(context: Context, habits: List<Habit>) {
        habits.filter { it.reminderHour >= 0 && !it.isArchived }.forEach { habit ->
            scheduleReminder(context, habit)
        }
    }
}
