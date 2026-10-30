package com.habitflow.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a habit the user wants to track.
 *
 * @param frequencyType One of: "daily", "weekly", "specific_days"
 * @param frequencyValue For "weekly" → times per week (e.g., 3). Ignored for "daily".
 * @param targetDays JSON array of day indices (1=Mon..7=Sun) for "specific_days". Empty for others.
 * @param colorHex Hex color string like "#A78BFA"
 * @param iconName Material icon name, e.g., "FitnessCenter"
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequencyType: String = "daily",
    val frequencyValue: Int = 1,
    val targetDays: String = "",       // JSON array: [1,2,3,4,5] for weekdays
    val colorHex: String = "#A78BFA",
    val iconName: String = "CheckCircle",
    val reminderHour: Int = -1,        // -1 means no reminder
    val reminderMinute: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long = 0,          // 0 means not archived
    val position: Int = 0
) {
    val isArchived: Boolean get() = archivedAt > 0
}
