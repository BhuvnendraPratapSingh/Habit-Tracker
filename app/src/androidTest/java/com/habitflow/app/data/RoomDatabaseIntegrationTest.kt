package com.habitflow.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.habitflow.app.data.db.AppDatabase
import com.habitflow.app.data.db.dao.HabitDao
import com.habitflow.app.data.db.dao.HabitEntryDao
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.db.entity.HabitEntry
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Integration tests using Room in-memory database.
 * Runs on Android device/emulator — validates DAOs, Repository, and data flow end-to-end.
 */
@RunWith(AndroidJUnit4::class)
class RoomDatabaseIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var habitDao: HabitDao
    private lateinit var entryDao: HabitEntryDao
    private lateinit var repository: HabitRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habitDao = db.habitDao()
        entryDao = db.habitEntryDao()
        repository = HabitRepository(habitDao, entryDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── Habit CRUD ──────────────────────────────────────────────

    @Test
    fun insertAndRetrieveHabit() = runTest {
        val habit = Habit(name = "Exercise", frequencyType = "daily")
        val id = habitDao.insert(habit)

        val retrieved = habitDao.getById(id)
        assertNotNull(retrieved)
        assertEquals("Exercise", retrieved!!.name)
        assertEquals("daily", retrieved.frequencyType)
    }

    @Test
    fun activeHabitsExcludesArchived() = runTest {
        val h1 = habitDao.insert(Habit(name = "Read"))
        val h2 = habitDao.insert(Habit(name = "Meditate"))
        habitDao.archive(h2)

        val active = habitDao.getActiveHabits().first()
        assertEquals(1, active.size)
        assertEquals("Read", active[0].name)
    }

    @Test
    fun deleteHabitCascadesEntries() = runTest {
        val id = habitDao.insert(Habit(name = "Temp"))
        entryDao.insert(HabitEntry(habitId = id, date = DateUtils.today()))

        val habit = habitDao.getById(id)!!
        habitDao.delete(habit)

        val entries = entryDao.getAllEntriesForHabit(id)
        assertTrue(entries.isEmpty())
    }

    @Test
    fun archiveAndUnarchiveHabit() = runTest {
        val id = habitDao.insert(Habit(name = "Yoga"))
        habitDao.archive(id)

        val archived = habitDao.getArchivedHabits().first()
        assertEquals(1, archived.size)

        habitDao.unarchive(id)
        val active = habitDao.getActiveHabits().first()
        assertEquals(1, active.size)
    }

    // ── Entry / Toggle ──────────────────────────────────────────

    @Test
    fun toggleCreatesEntry() = runTest {
        val id = habitDao.insert(Habit(name = "Read"))
        val today = DateUtils.today()

        val wasChecked = repository.toggleHabitForDate(id, today)
        assertTrue(wasChecked)

        val entry = entryDao.getEntry(id, today)
        assertNotNull(entry)
        assertEquals(1, entry!!.value)
    }

    @Test
    fun toggleTwiceRemovesEntry() = runTest {
        val id = habitDao.insert(Habit(name = "Read"))
        val today = DateUtils.today()

        repository.toggleHabitForDate(id, today) // Check
        repository.toggleHabitForDate(id, today) // Uncheck

        val entry = entryDao.getEntry(id, today)
        assertNull(entry)
    }

    @Test
    fun isCompletedForDateReturnsCorrectly() = runTest {
        val id = habitDao.insert(Habit(name = "Read"))
        val today = DateUtils.today()

        assertFalse(repository.isCompletedForDate(id, today))

        repository.toggleHabitForDate(id, today)
        assertTrue(repository.isCompletedForDate(id, today))
    }

    @Test
    fun entriesAreDateSpecific() = runTest {
        val id = habitDao.insert(Habit(name = "Read"))
        val today = DateUtils.today()
        val yesterday = DateUtils.format(LocalDate.now().minusDays(1))

        repository.toggleHabitForDate(id, today)

        assertTrue(repository.isCompletedForDate(id, today))
        assertFalse(repository.isCompletedForDate(id, yesterday))
    }

    // ── Streak computation ──────────────────────────────────────

    @Test
    fun streakInfoWithNoEntries() = runTest {
        val id = habitDao.insert(Habit(name = "Read"))
        val habit = habitDao.getById(id)!!

        val info = repository.getStreakInfo(habit)
        assertEquals(0, info.currentStreak)
        assertEquals(0, info.bestStreak)
        assertEquals(0, info.totalCompletions)
    }

    @Test
    fun streakInfoWithConsecutiveDays() = runTest {
        val id = habitDao.insert(Habit(name = "Read", createdAt = System.currentTimeMillis() - 86400000L * 10))
        val habit = habitDao.getById(id)!!

        // Complete today + 2 previous days
        for (i in 0..2) {
            val date = DateUtils.format(LocalDate.now().minusDays(i.toLong()))
            entryDao.insert(HabitEntry(habitId = id, date = date))
        }

        val info = repository.getStreakInfo(habit)
        assertEquals(3, info.currentStreak)
        assertEquals(3, info.totalCompletions)
    }

    @Test
    fun streakBreaksAtGap() = runTest {
        val id = habitDao.insert(Habit(name = "Read", createdAt = System.currentTimeMillis() - 86400000L * 10))
        val habit = habitDao.getById(id)!!

        // Today + yesterday
        entryDao.insert(HabitEntry(habitId = id, date = DateUtils.today()))
        entryDao.insert(HabitEntry(habitId = id, date = DateUtils.format(LocalDate.now().minusDays(1))))
        // Skip day 2, then day 3
        entryDao.insert(HabitEntry(habitId = id, date = DateUtils.format(LocalDate.now().minusDays(3))))

        val info = repository.getStreakInfo(habit)
        assertEquals(2, info.currentStreak)  // Today + yesterday only
    }

    // ── HabitWithStats ──────────────────────────────────────────

    @Test
    fun habitWithStatsReturnsCompletionState() = runTest {
        val id = habitDao.insert(Habit(name = "Read", createdAt = System.currentTimeMillis() - 86400000L * 10))
        val habit = habitDao.getById(id)!!

        val statsBefore = repository.getHabitWithStats(habit)
        assertFalse(statsBefore.isCompletedToday)

        repository.toggleHabitForDate(id, DateUtils.today())

        val statsAfter = repository.getHabitWithStats(habit)
        assertTrue(statsAfter.isCompletedToday)
    }

    @Test
    fun habitWithStatsDetectsYesterdayMissed() = runTest {
        val id = habitDao.insert(Habit(name = "Read", frequencyType = "daily", createdAt = System.currentTimeMillis() - 86400000L * 10))
        val habit = habitDao.getById(id)!!

        // Don't complete yesterday
        val stats = repository.getHabitWithStats(habit)
        assertTrue(stats.yesterdayMissed)
    }

    @Test
    fun habitWithStatsYesterdayNotMissedWhenCompleted() = runTest {
        val id = habitDao.insert(Habit(name = "Read", frequencyType = "daily", createdAt = System.currentTimeMillis() - 86400000L * 10))
        val habit = habitDao.getById(id)!!
        val yesterday = DateUtils.format(LocalDate.now().minusDays(1))

        entryDao.insert(HabitEntry(habitId = id, date = yesterday))

        val stats = repository.getHabitWithStats(habit)
        assertFalse(stats.yesterdayMissed)
    }

    @Test
    fun habitWithStatsLast7DaysHas7Elements() = runTest {
        val id = habitDao.insert(Habit(name = "Read", createdAt = System.currentTimeMillis() - 86400000L * 10))
        val habit = habitDao.getById(id)!!

        val stats = repository.getHabitWithStats(habit)
        assertEquals(7, stats.last7Days.size)
    }

    // ── Daily completion counts ─────────────────────────────────

    @Test
    fun dailyCompletionCountsAggregatesCorrectly() = runTest {
        val h1 = habitDao.insert(Habit(name = "Read"))
        val h2 = habitDao.insert(Habit(name = "Exercise"))
        val today = DateUtils.today()

        entryDao.insert(HabitEntry(habitId = h1, date = today))
        entryDao.insert(HabitEntry(habitId = h2, date = today))

        val counts = repository.getDailyCompletionCounts(today, today)
        assertEquals(2, counts[today])
    }

    @Test
    fun dailyCompletionCountsEmptyRange() = runTest {
        val counts = repository.getDailyCompletionCounts(DateUtils.today(), DateUtils.today())
        assertTrue(counts.isEmpty())
    }

    // ── Completion rate ─────────────────────────────────────────

    @Test
    fun completionRateFullWeek() = runTest {
        val id = habitDao.insert(Habit(name = "Read", frequencyType = "daily", createdAt = System.currentTimeMillis() - 86400000L * 10))
        val habit = habitDao.getById(id)!!

        // Complete all 7 days
        for (i in 0..6) {
            val date = DateUtils.format(LocalDate.now().minusDays(i.toLong()))
            entryDao.insert(HabitEntry(habitId = id, date = date))
        }

        val startDate = DateUtils.format(LocalDate.now().minusDays(6))
        val rate = repository.getCompletionRateForRange(habit, startDate, DateUtils.today())
        assertEquals(1.0f, rate, 0.001f)
    }

    @Test
    fun completionRatePartial() = runTest {
        val id = habitDao.insert(Habit(name = "Read", frequencyType = "daily", createdAt = System.currentTimeMillis() - 86400000L * 10))
        val habit = habitDao.getById(id)!!

        // Complete only today
        entryDao.insert(HabitEntry(habitId = id, date = DateUtils.today()))

        val startDate = DateUtils.format(LocalDate.now().minusDays(6))
        val rate = repository.getCompletionRateForRange(habit, startDate, DateUtils.today())
        assertTrue(rate > 0f)
        assertTrue(rate < 1f)
    }

    @Test
    fun completionRateZeroWithNoEntries() = runTest {
        val id = habitDao.insert(Habit(name = "Read", frequencyType = "daily", createdAt = System.currentTimeMillis() - 86400000L * 10))
        val habit = habitDao.getById(id)!!

        val startDate = DateUtils.format(LocalDate.now().minusDays(6))
        val rate = repository.getCompletionRateForRange(habit, startDate, DateUtils.today())
        assertEquals(0f, rate, 0.001f)
    }

    // ── Unique constraint ───────────────────────────────────────

    @Test
    fun duplicateEntryReplacesExisting() = runTest {
        val id = habitDao.insert(Habit(name = "Read"))
        val today = DateUtils.today()

        entryDao.insert(HabitEntry(habitId = id, date = today, value = 1))
        entryDao.insert(HabitEntry(habitId = id, date = today, value = 1))

        // Should only have 1 entry due to unique constraint + REPLACE strategy
        val entries = entryDao.getAllEntriesForHabit(id)
        assertEquals(1, entries.size)
    }

    // ── Active count ────────────────────────────────────────────

    @Test
    fun activeCountReflectsState() = runTest {
        habitDao.insert(Habit(name = "A"))
        habitDao.insert(Habit(name = "B"))
        val h3 = habitDao.insert(Habit(name = "C"))
        habitDao.archive(h3)

        assertEquals(2, repository.getActiveCount())
    }
}
