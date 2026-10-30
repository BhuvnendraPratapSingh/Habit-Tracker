package com.habitflow.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.habitflow.app.data.db.dao.HabitDao
import com.habitflow.app.data.db.dao.HabitEntryDao
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.db.entity.HabitEntry

@Database(
    entities = [Habit::class, HabitEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao
}
