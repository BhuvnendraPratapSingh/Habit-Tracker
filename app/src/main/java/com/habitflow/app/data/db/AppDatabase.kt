package com.habitflow.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habitflow.app.data.db.dao.HabitDao
import com.habitflow.app.data.db.dao.HabitEntryDao
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.db.entity.HabitEntry

@Database(
    entities = [Habit::class, HabitEntry::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao

    companion object {
        /**
         * All database migrations live here.
         * When bumping the version, add a MIGRATION_X_Y object below
         * and include it in [ALL_MIGRATIONS].
         *
         * Example for v1 → v2:
         *   val MIGRATION_1_2 = object : Migration(1, 2) {
         *       override fun migrate(db: SupportSQLiteDatabase) {
         *           db.execSQL("ALTER TABLE habits ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
         *       }
         *   }
         *
         * Then add it: val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
         */
        val ALL_MIGRATIONS: Array<Migration> = arrayOf(
            // No migrations yet — app is on version 1.
            // Add them here as the schema evolves.
        )
    }
}
