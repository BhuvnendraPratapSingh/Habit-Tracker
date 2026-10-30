package com.habitflow.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitflow.app.data.db.AppDatabase
import com.habitflow.app.util.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules all habit reminders after device reboot.
 * Alarms are lost when the device powers off, so we re-register them here.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitflow_db"
                ).build()
                val habits = db.habitDao().getHabitsWithReminders()
                ReminderScheduler.rescheduleAll(context.applicationContext, habits)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
