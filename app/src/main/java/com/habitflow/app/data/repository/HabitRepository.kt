package com.habitflow.app.data.repository

import com.habitflow.app.data.db.dao.HabitDao
import com.habitflow.app.data.db.dao.HabitEntryDao
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.db.entity.HabitEntry
import com.habitflow.app.data.model.HabitWithStats
import com.habitflow.app.data.model.StreakInfo
import com.habitflow.app.util.DateUtils
import com.habitflow.app.util.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val entryDao: HabitEntryDao
) {
    // ── Habits ──────────────────────────────────────────────

    fun getActiveHabits(): Flow<List<Habit>> = habitDao.getActiveHabits()

    /** Non-reactive snapshot for one-off reads. */
    suspend fun getActiveHabitsSnapshot(): List<Habit> = habitDao.getActiveHabits().first()

    fun getArchivedHabits(): Flow<List<Habit>> = habitDao.getArchivedHabits()

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getById(id)

    suspend fun insertHabit(habit: Habit): Long = habitDao.insert(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.update(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.delete(habit)

    suspend fun archiveHabit(id: Long) = habitDao.archive(id)

    suspend fun unarchiveHabit(id: Long) = habitDao.unarchive(id)

    suspend fun getActiveCount(): Int = habitDao.getActiveCount()

    // ── Entries ──────────────────────────────────────────────

    suspend fun toggleHabitForDate(habitId: Long, date: String): Boolean {
        val existing = entryDao.getEntry(habitId, date)
        return if (existing != null) {
            entryDao.deleteByHabitAndDate(habitId, date)
            false  // Now unchecked
        } else {
            entryDao.insert(HabitEntry(habitId = habitId, date = date))
            true   // Now checked
        }
    }

    suspend fun isCompletedForDate(habitId: Long, date: String): Boolean {
        return entryDao.getEntry(habitId, date) != null
    }

    suspend fun getCompletionDates(habitId: Long): List<String> {
        return entryDao.getCompletionDatesForHabit(habitId)
    }

    suspend fun getCompletedCountForDate(date: String): Int {
        return entryDao.getCompletionCountForDate(date)
    }

    // ── Stats & Streaks ─────────────────────────────────────

    suspend fun getStreakInfo(habit: Habit): StreakInfo {
        val dates = entryDao.getCompletionDatesForHabit(habit.id)
        return StreakCalculator.calculate(habit, dates)
    }

    suspend fun getHabitWithStats(habit: Habit): HabitWithStats {
        val today = DateUtils.today()
        val streakInfo = getStreakInfo(habit)
        val isCompletedToday = isCompletedForDate(habit.id, today)

        // Last 7 days completion status
        val last7Dates = DateUtils.lastNDays(7)
        val entries = entryDao.getEntriesForRange(habit.id, last7Dates.first(), last7Dates.last())
        val completedDatesSet = entries.filter { it.value > 0 }.map { it.date }.toSet()
        val last7Days = last7Dates.map { it in completedDatesSet }

        // Completions this week
        val weekStart = DateUtils.format(DateUtils.startOfWeek())
        val completionsThisWeek = entryDao.getCompletionCountForRange(habit.id, weekStart, today)

        return HabitWithStats(
            habit = habit,
            streakInfo = streakInfo,
            isCompletedToday = isCompletedToday,
            completionsThisWeek = completionsThisWeek,
            last7Days = last7Days
        )
    }

    /**
     * Get daily completion counts for a date range (for chart data).
     * Returns a map of date -> number of habits completed.
     */
    suspend fun getDailyCompletionCounts(startDate: String, endDate: String): Map<String, Int> {
        val entries = entryDao.getAllCompletedEntriesForRange(startDate, endDate)
        return entries.groupBy { it.date }.mapValues { it.value.size }
    }

    /**
     * Get completion rate for a habit over a date range.
     */
    suspend fun getCompletionRateForRange(habit: Habit, startDate: String, endDate: String): Float {
        val start = DateUtils.parse(startDate)
        val end = DateUtils.parse(endDate)
        var scheduled = 0
        var completed = 0
        var cursor = start
        val completions = entryDao.getEntriesForRange(habit.id, startDate, endDate)
            .filter { it.value > 0 }
            .map { it.date }
            .toSet()

        while (!cursor.isAfter(end)) {
            val dateStr = DateUtils.format(cursor)
            if (DateUtils.isScheduledDay(dateStr, habit.frequencyType, habit.targetDays)) {
                scheduled++
                if (dateStr in completions) completed++
            }
            cursor = cursor.plusDays(1)
        }

        return if (scheduled > 0) completed.toFloat() / scheduled else 0f
    }

    /**
     * Get habits that have reminders set (for scheduling).
     */
    suspend fun getHabitsWithReminders(): List<Habit> {
        return habitDao.getHabitsWithReminders()
    }
}
