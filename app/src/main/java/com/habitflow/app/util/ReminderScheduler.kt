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

    fun scheduleReminder(context: Context, habit: Habit) {
        if (habit.reminderHour < 0) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", habit.id)
            putExtra("habit_name", habit.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, habit.reminderHour)
            set(Calendar.MINUTE, habit.reminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Use setRepeating for daily reminders — battery-friendly enough for a habit app
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, habitId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
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
