package com.habitflow.app.data.db.dao

import androidx.room.*
import com.habitflow.app.data.db.entity.HabitEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HabitEntry): Long

    @Delete
    suspend fun delete(entry: HabitEntry)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId AND date = :date")
    suspend fun deleteByHabitAndDate(habitId: Long, date: String)

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getEntry(habitId: Long, date: String): HabitEntry?

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date LIMIT 1")
    fun getEntryFlow(habitId: Long, date: String): Flow<HabitEntry?>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getAllEntriesForHabit(habitId: Long): List<HabitEntry>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getEntriesForRange(habitId: Long, startDate: String, endDate: String): List<HabitEntry>

    @Query("SELECT * FROM habit_entries WHERE date = :date AND value > 0")
    suspend fun getCompletedEntriesForDate(date: String): List<HabitEntry>

    @Query("SELECT date FROM habit_entries WHERE habitId = :habitId AND value > 0 ORDER BY date DESC")
    suspend fun getCompletionDatesForHabit(habitId: Long): List<String>

    @Query("SELECT COUNT(*) FROM habit_entries WHERE habitId = :habitId AND value > 0")
    suspend fun getTotalCompletions(habitId: Long): Int

    @Query("SELECT COUNT(*) FROM habit_entries WHERE habitId = :habitId AND value > 0 AND date BETWEEN :startDate AND :endDate")
    suspend fun getCompletionCountForRange(habitId: Long, startDate: String, endDate: String): Int

    /** Count completions for ALL habits on a specific date */
    @Query("SELECT COUNT(*) FROM habit_entries WHERE date = :date AND value > 0")
    suspend fun getCompletionCountForDate(date: String): Int

    /** Get all entries across all habits for a date range */
    @Query("SELECT * FROM habit_entries WHERE date BETWEEN :startDate AND :endDate AND value > 0 ORDER BY date ASC")
    suspend fun getAllCompletedEntriesForRange(startDate: String, endDate: String): List<HabitEntry>
}
