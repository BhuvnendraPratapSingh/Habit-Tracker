package com.habitflow.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.model.BadgeEvaluator
import com.habitflow.app.data.model.EarnedBadge
import com.habitflow.app.data.model.StreakInfo
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitStatItem(
    val habit: Habit,
    val streakInfo: StreakInfo,
    val completionRate: Float
)

data class DayCompletion(
    val date: String,
    val dayLabel: String,
    val completedCount: Int,
    val totalHabits: Int
)

data class StatsUiState(
    val selectedRange: Int = 0,
    val overallCompletionRate: Float = 0f,
    val totalCompletions: Int = 0,
    val perfectDays: Int = 0,
    val totalTrackedDays: Int = 0,
    val bestDay: String = "",
    val habitStats: List<HabitStatItem> = emptyList(),
    val dailyCompletions: List<DayCompletion> = emptyList(),
    val badges: List<EarnedBadge> = emptyList(),
    val earnedBadgeCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    /**
     * BUG-12 FIX: Track the current loading job so we can cancel it
     * before starting a new one. Prevents two concurrent loadStats()
     * from overwriting each other's results.
     */
    private var loadingJob: Job? = null

    init {
        observeStats()
    }

    fun setSelectedRange(range: Int) {
        _uiState.value = _uiState.value.copy(selectedRange = range)
        loadStats()
    }

    private fun observeStats() {
        viewModelScope.launch {
            repository.getActiveHabits().collect { _ ->
                loadStats()
            }
        }
    }

    private fun loadStats() {
        // Cancel any in-flight load before starting a new one
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            val habits = repository.getActiveHabitsSnapshot()
            if (habits.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    habitStats = emptyList(),
                    dailyCompletions = emptyList(),
                    badges = BadgeEvaluator.evaluate(0, 0, 0, 0, false),
                    earnedBadgeCount = 0,
                    isLoading = false
                )
                return@launch
            }

            // Read range from state ONCE at the top — stable for this load
            val range = _uiState.value.selectedRange
            val today = LocalDate.now()
            val (startDate, endDate) = when (range) {
                0 -> DateUtils.format(today.minusDays(6)) to DateUtils.today()
                1 -> DateUtils.format(today.minusDays(29)) to DateUtils.today()
                2 -> DateUtils.format(today.minusDays(364)) to DateUtils.today()
                else -> DateUtils.format(today.minusDays(6)) to DateUtils.today()
            }

            val habitStats = habits.map { habit ->
                val streakInfo = repository.getStreakInfo(habit)
                val rate = repository.getCompletionRateForRange(habit, startDate, endDate)
                HabitStatItem(habit, streakInfo, rate)
            }

            val dailyMap = repository.getDailyCompletionCounts(startDate, endDate)

            val dailyCompletions = buildList {
                var cursor = DateUtils.parse(startDate)
                val end = DateUtils.parse(endDate)
                while (!cursor.isAfter(end)) {
                    val dateStr = DateUtils.format(cursor)
                    val dayLabel = when (range) {
                        0 -> cursor.dayOfWeek.name.take(3)
                        1 -> "${cursor.dayOfMonth}"
                        2 -> cursor.month.name.take(3)
                        else -> "${cursor.dayOfMonth}"
                    }
                    val scheduledForDay = habits.count { habit ->
                        DateUtils.isScheduledDay(dateStr, habit.frequencyType, habit.targetDays)
                    }
                    add(DayCompletion(dateStr, dayLabel, dailyMap[dateStr] ?: 0, scheduledForDay))
                    cursor = cursor.plusDays(1)
                }
            }

            val totalCompletions = dailyMap.values.sum()
            val totalScheduledDays = dailyCompletions.size
            val totalScheduledHabitDays = dailyCompletions.sumOf { it.totalHabits }
            val overallRate = if (totalScheduledHabitDays > 0)
                totalCompletions.toFloat() / totalScheduledHabitDays
            else 0f

            val bestDayEntry = dailyMap.maxByOrNull { it.value }
            val bestDay = bestDayEntry?.let {
                DateUtils.friendlyDate(it.key) + " (${it.value})"
            } ?: "\u2014"

            val perfectDays = dailyCompletions.count { day ->
                day.totalHabits > 0 && day.completedCount >= day.totalHabits
            }

            val bestStreakAcross = habitStats.maxOfOrNull { it.streakInfo.bestStreak } ?: 0
            val totalCompletionsAcross = habitStats.sumOf { it.streakInfo.totalCompletions }
            val hasPerfectDay = dailyCompletions.any { day ->
                day.totalHabits > 0 && day.completedCount >= day.totalHabits
            }

            val allDailyMap = repository.getDailyCompletionCounts(
                DateUtils.format(today.minusDays(364)),
                DateUtils.today()
            )

            val todayScheduled = habits.count { habit ->
                DateUtils.isScheduledDay(DateUtils.today(), habit.frequencyType, habit.targetDays)
            }
            val todayCount = allDailyMap[DateUtils.today()] ?: 0
            val todayAllDone = todayScheduled > 0 && todayCount >= todayScheduled

            var consecutivePerfect = 0
            var checkDate = if (todayAllDone) today else today.minusDays(1)
            while (true) {
                val dateStr = DateUtils.format(checkDate)
                val scheduled = habits.count { habit ->
                    DateUtils.isScheduledDay(dateStr, habit.frequencyType, habit.targetDays)
                }
                val count = allDailyMap[dateStr] ?: 0
                if (scheduled > 0 && count >= scheduled) {
                    consecutivePerfect++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }

            val badges = BadgeEvaluator.evaluate(
                bestStreakAcrossHabits = bestStreakAcross,
                totalCompletionsAcrossHabits = totalCompletionsAcross,
                activeHabitCount = habits.size,
                perfectDaysInRow = consecutivePerfect,
                hasPerfectDay = hasPerfectDay
            )

            _uiState.value = StatsUiState(
                selectedRange = range,
                overallCompletionRate = overallRate.coerceIn(0f, 1f),
                totalCompletions = totalCompletions,
                perfectDays = perfectDays,
                totalTrackedDays = totalScheduledDays,
                bestDay = bestDay,
                habitStats = habitStats,
                dailyCompletions = dailyCompletions,
                badges = badges,
                earnedBadgeCount = badges.count { it.isEarned },
                isLoading = false
            )
        }
    }
}
