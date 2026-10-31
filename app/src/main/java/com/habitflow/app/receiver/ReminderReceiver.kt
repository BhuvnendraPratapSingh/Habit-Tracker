package com.habitflow.app.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.habitflow.app.MainActivity
import com.habitflow.app.R
import com.habitflow.app.util.Constants
import com.habitflow.app.util.ReminderScheduler
import com.habitflow.app.data.db.entity.Habit

/**
 * Receives alarm broadcasts and shows a reminder notification for a habit.
 * After showing the notification, reschedules the alarm for the next day
 * (since we use one-shot exact alarms instead of inexact setRepeating).
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitName = intent.getStringExtra("habit_name") ?: return
        val habitId = intent.getLongExtra("habit_id", -1)
        if (habitId == -1L) return

        val reminderHour = intent.getIntExtra("reminder_hour", -1)
        val reminderMinute = intent.getIntExtra("reminder_minute", 0)

        // Show the notification
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode().and(0x7FFFFFFF),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_text, habitName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(
                    Constants.NOTIFICATION_ID_BASE + habitId.hashCode().and(0x7FFFFFFF),
                    notification
                )
        } catch (_: SecurityException) {
            // Notification permission not granted — silently ignore
        }

        // Reschedule for tomorrow (exact one-shot alarms don't repeat)
        if (reminderHour >= 0) {
            ReminderScheduler.scheduleReminder(
                context,
                Habit(
                    id = habitId,
                    name = habitName,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute
                )
            )
        }
    }
}
