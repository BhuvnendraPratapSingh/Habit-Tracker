package com.habitflow.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.model.HabitWithStats
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.ui.components.MilestoneEvent
import com.habitflow.app.ui.components.WeeklySummary
import com.habitflow.app.ui.components.getMilestoneTitle
import com.habitflow.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TodayUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val completedCount: Int = 0,
    val totalScheduledCount: Int = 0,
    val greeting: String = "",
    val isLoading: Boolean = true,
    val weeklySummary: WeeklySummary? = null,
    val showAllDoneConfetti: Boolean = false,
    val loadedForDate: String = ""
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val _milestoneEvent = MutableStateFlow<MilestoneEvent?>(null)
    val milestoneEvent: StateFlow<MilestoneEvent?> = _milestoneEvent.asStateFlow()

    private val _habitCheckedEvent = MutableStateFlow(false)
    val habitCheckedEvent: StateFlow<Boolean> = _habitCheckedEvent.asStateFlow()

    private val refreshTrigger = MutableStateFlow(0L)

    private var previousCompletedCount = -1

    /**
     * BUG-8 FIX: Mutex prevents concurrent toggle operations.
     * Rapid double-taps queue up — the second tap waits for
     * the first to finish before toggling again.
     */
    private val toggleMutex = Mutex()

    init {
        observeData()
    }

    fun onScreenVisible() {
        refreshTrigger.value = System.currentTimeMillis()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getActiveHabits(),
                refreshTrigger
            ) { habits, _ -> habits }
                .collect { habits ->
                    loadForDate(habits)
                }
        }
    }

    private suspend fun loadForDate(habits: List<Habit>) {
        val today = DateUtils.today()

        val scheduledHabits = habits.filter { habit ->
            DateUtils.isScheduledDay(today, habit.frequencyType, habit.targetDays)
        }

        val habitsWithStats = scheduledHabits.map { habit ->
            repository.getHabitWithStats(habit)
        }

        val completedCount = habitsWithStats.count { it.isCompletedToday }
        val totalScheduled = scheduledHabits.size
        val summary = computeWeeklySummary(habits)

        val justCompletedAll = completedCount == totalScheduled &&
                totalScheduled > 0 &&
                previousCompletedCount in 0 until totalScheduled

        previousCompletedCount = completedCount

        _uiState.value = TodayUiState(
            habits = habitsWithStats,
            completedCount = completedCount,
            totalScheduledCount = totalScheduled,
            greeting = getGreeting(),
            isLoading = false,
            weeklySummary = summary,
            loadedForDate = today,
            showAllDoneConfetti = justCompletedAll
        )
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            // BUG-8 FIX: Lock so rapid taps don't fire concurrently.
            // Second tap waits for first to complete — no phantom milestones.
            toggleMutex.withLock {
                val today = DateUtils.today()
                val wasChecked = repository.toggleHabitForDate(habitId, today)

                if (wasChecked) {
                    _habitCheckedEvent.value = true

                    val habit = repository.getHabitById(habitId)
                    if (habit != null) {
                        val streakInfo = repository.getStreakInfo(habit)
                        val milestone = getMilestoneTitle(streakInfo.currentStreak)
                        if (milestone != null) {
                            _milestoneEvent.value = MilestoneEvent(
                                streakDays = streakInfo.currentStreak,
                                habitName = habit.name
                            )
                        }
                    }
                }

                refreshTrigger.value = System.currentTimeMillis()
            }
        }
    }

    fun clearMilestone() { _milestoneEvent.value = null }
    fun clearHapticEvent() { _habitCheckedEvent.value = false }
    fun clearAllDoneConfetti() { _uiState.value = _uiState.value.copy(showAllDoneConfetti = false) }

    /**
     * BUG-7 FIX: Perfect days now counts scheduled habits per day,
     * not total active habits. A day with 2/2 scheduled habits done
     * is perfect even if a 3rd habit isn't scheduled that day.
     */
    private suspend fun computeWeeklySummary(habits: List<Habit>): WeeklySummary? {
        if (habits.isEmpty()) return null

        val today = LocalDate.now()
        val weekStart = DateUtils.format(today.minusDays(6))
        val weekEnd = DateUtils.today()

        val dailyMap = repository.getDailyCompletionCounts(weekStart, weekEnd)
        val totalCompleted = dailyMap.values.sum()

        // Count actual scheduled habit-days in the 7-day window
        var totalScheduled = 0
        for (habit in habits) {
            var cursor = today.minusDays(6)
            while (!cursor.isAfter(today)) {
                val dateStr = DateUtils.format(cursor)
                if (DateUtils.isScheduledDay(dateStr, habit.frequencyType, habit.targetDays)) {
                    totalScheduled++
                }
                cursor = cursor.plusDays(1)
            }
        }

        var bestStreak = 0
        for (habit in habits) {
            val info = repository.getStreakInfo(habit)
            if (info.bestStreak > bestStreak) bestStreak = info.bestStreak
        }

        // BUG-7 FIX: Perfect day = all SCHEDULED habits for that day completed
        var perfectDays = 0
        var dayCursor = today.minusDays(6)
        while (!dayCursor.isAfter(today)) {
            val dateStr = DateUtils.format(dayCursor)
            val scheduledCount = habits.count { habit ->
                DateUtils.isScheduledDay(dateStr, habit.frequencyType, habit.targetDays)
            }
            val completedCount = dailyMap[dateStr] ?: 0
            if (scheduledCount > 0 && completedCount >= scheduledCount) {
                perfectDays++
            }
            dayCursor = dayCursor.plusDays(1)
        }

        val bestEntry = dailyMap.maxByOrNull { it.value }
        val bestDayLabel = bestEntry?.let {
            val date = DateUtils.parse(it.key)
            date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
        } ?: ""

        return WeeklySummary(
            completedCount = totalCompleted,
            totalScheduled = totalScheduled.coerceAtLeast(1),
            bestStreak = bestStreak,
            perfectDays = perfectDays,
            bestDayLabel = bestDayLabel
        )
    }

    private fun getGreeting(): String {
        val hour = LocalTime.now().hour
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}
