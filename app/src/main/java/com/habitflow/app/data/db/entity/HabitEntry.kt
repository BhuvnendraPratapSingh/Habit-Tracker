package com.habitflow.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records a single completion (check-in) of a habit on a specific date.
 * The combination of habitId + date is unique — one entry per habit per day.
 *
 * @param date ISO date string "yyyy-MM-dd"
 * @param value 1 = completed, 0 = skipped/missed. Could extend to numeric values later.
 */
@Entity(
    tableName = "habit_entries",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index(value = ["habitId"]),
        Index(value = ["date"])
    ]
)
data class HabitEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String,           // "yyyy-MM-dd"
    val value: Int = 1,         // 1 = done, 0 = skipped
    val timestamp: Long = System.currentTimeMillis()
)
