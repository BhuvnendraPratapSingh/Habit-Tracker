package com.habitflow.app.data.db.dao

import androidx.room.*
import com.habitflow.app.data.db.entity.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE archivedAt = 0 ORDER BY position ASC, createdAt DESC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE archivedAt > 0 ORDER BY archivedAt DESC")
    fun getArchivedHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY position ASC, createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("UPDATE habits SET archivedAt = :timestamp WHERE id = :id")
    suspend fun archive(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE habits SET archivedAt = 0 WHERE id = :id")
    suspend fun unarchive(id: Long)

    @Query("SELECT COUNT(*) FROM habits WHERE archivedAt = 0")
    suspend fun getActiveCount(): Int

    @Query("SELECT * FROM habits WHERE reminderHour >= 0 AND archivedAt = 0")
    suspend fun getHabitsWithReminders(): List<Habit>
}
