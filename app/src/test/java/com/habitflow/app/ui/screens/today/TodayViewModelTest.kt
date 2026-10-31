package com.habitflow.app.ui.screens.today

import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.model.HabitWithStats
import com.habitflow.app.data.model.StreakInfo
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.util.DateUtils
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TodayViewModel.
 * Uses MockK to mock the repository and coroutines-test for dispatchers.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: HabitRepository
    private lateinit var viewModel: TodayViewModel

    private val testHabit = Habit(
        id = 1L,
        name = "Read",
        frequencyType = "daily",
        createdAt = System.currentTimeMillis() - 86400000L * 10
    )

    private val testHabitWithStats = HabitWithStats(
        habit = testHabit,
        streakInfo = StreakInfo(currentStreak = 5, bestStreak = 5, totalCompletions = 10),
        isCompletedToday = false,
        yesterdayMissed = false,
        completionsThisWeek = 3,
        last7Days = listOf(true, true, true, false, false, true, false)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        // Default stubs
        every { repository.getActiveHabits() } returns flowOf(listOf(testHabit))
        coEvery { repository.getHabitWithStats(any()) } returns testHabitWithStats
        coEvery { repository.getDailyCompletionCounts(any(), any()) } returns emptyMap()
        coEvery { repository.getStreakInfo(any()) } returns StreakInfo(currentStreak = 5)

        viewModel = TodayViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial load ────────────────────────────────────────────

    @Test
    fun `init loads habits for today`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.habits.size)
        assertEquals("Read", state.habits[0].habit.name)
    }

    @Test
    fun `greeting matches time of day`() = runTest {
        val greeting = viewModel.uiState.value.greeting
        assertTrue(
            greeting in listOf("Good Morning", "Good Afternoon", "Good Evening")
        )
    }

    @Test
    fun `loaded date matches today`() = runTest {
        assertEquals(DateUtils.today(), viewModel.uiState.value.loadedForDate)
    }

    @Test
    fun `completion count is correct`() = runTest {
        // testHabitWithStats has isCompletedToday = false
        assertEquals(0, viewModel.uiState.value.completedCount)
        assertEquals(1, viewModel.uiState.value.totalScheduledCount)
    }

    // ── Toggle habit ────────────────────────────────────────────

    @Test
    fun `toggleHabit calls repository`() = runTest {
        coEvery { repository.toggleHabitForDate(1L, any()) } returns true
        coEvery { repository.getHabitById(1L) } returns testHabit
        coEvery { repository.getActiveHabits() } returns flowOf(listOf(testHabit))

        viewModel.toggleHabit(1L)

        coVerify { repository.toggleHabitForDate(1L, DateUtils.today()) }
    }

    @Test
    fun `toggleHabit fires haptic event on check`() = runTest {
        coEvery { repository.toggleHabitForDate(1L, any()) } returns true
        coEvery { repository.getHabitById(1L) } returns testHabit

        viewModel.toggleHabit(1L)

        assertTrue(viewModel.habitCheckedEvent.value)
    }

    @Test
    fun `toggleHabit does NOT fire haptic on uncheck`() = runTest {
        coEvery { repository.toggleHabitForDate(1L, any()) } returns false

        viewModel.toggleHabit(1L)

        assertFalse(viewModel.habitCheckedEvent.value)
    }

    // ── Milestone detection ─────────────────────────────────────

    @Test
    fun `milestone fires at 7-day streak`() = runTest {
        coEvery { repository.toggleHabitForDate(1L, any()) } returns true
        coEvery { repository.getHabitById(1L) } returns testHabit
        coEvery { repository.getStreakInfo(testHabit) } returns StreakInfo(currentStreak = 7)

        viewModel.toggleHabit(1L)

        val milestone = viewModel.milestoneEvent.value
        assertNotNull(milestone)
        assertEquals(7, milestone!!.streakDays)
        assertEquals("Read", milestone.habitName)
    }

    @Test
    fun `no milestone at non-milestone streak count`() = runTest {
        coEvery { repository.toggleHabitForDate(1L, any()) } returns true
        coEvery { repository.getHabitById(1L) } returns testHabit
        coEvery { repository.getStreakInfo(testHabit) } returns StreakInfo(currentStreak = 5)

        viewModel.toggleHabit(1L)

        assertNull(viewModel.milestoneEvent.value)
    }

    // ── Clear events ────────────────────────────────────────────

    @Test
    fun `clearMilestone resets event`() {
        viewModel.clearMilestone()
        assertNull(viewModel.milestoneEvent.value)
    }

    @Test
    fun `clearHapticEvent resets event`() {
        viewModel.clearHapticEvent()
        assertFalse(viewModel.habitCheckedEvent.value)
    }

    @Test
    fun `clearAllDoneConfetti resets flag`() {
        viewModel.clearAllDoneConfetti()
        assertFalse(viewModel.uiState.value.showAllDoneConfetti)
    }

    // ── Screen visibility ───────────────────────────────────────

    @Test
    fun `onScreenVisible triggers refresh`() = runTest {
        // Should not crash and should update state
        viewModel.onScreenVisible()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── All-done confetti ───────────────────────────────────────

    @Test
    fun `confetti triggers when last habit is checked`() = runTest {
        val completedStats = testHabitWithStats.copy(isCompletedToday = true)

        // First load with incomplete
        coEvery { repository.getHabitWithStats(any()) } returns testHabitWithStats
        viewModel.onScreenVisible()

        // Now toggle — after refresh, habit is complete
        coEvery { repository.toggleHabitForDate(1L, any()) } returns true
        coEvery { repository.getHabitById(1L) } returns testHabit
        coEvery { repository.getHabitWithStats(any()) } returns completedStats

        viewModel.toggleHabit(1L)

        // After refresh, completedCount == totalScheduledCount == 1
        assertEquals(1, viewModel.uiState.value.completedCount)
        assertEquals(1, viewModel.uiState.value.totalScheduledCount)
        assertTrue(viewModel.uiState.value.showAllDoneConfetti)
    }

    // ── Weekly summary ──────────────────────────────────────────

    @Test
    fun `weekly summary is computed`() = runTest {
        val state = viewModel.uiState.value
        assertNotNull(state.weeklySummary)
    }

    // ── Empty state ─────────────────────────────────────────────

    @Test
    fun `empty habits shows empty list`() = runTest {
        every { repository.getActiveHabits() } returns flowOf(emptyList())
        val emptyVm = TodayViewModel(repository)

        val state = emptyVm.uiState.value
        assertTrue(state.habits.isEmpty())
        assertEquals(0, state.completedCount)
    }
}
