package com.habitflow.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.model.StreakInfo
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        observeStats()
    }

    fun setSelectedRange(range: Int) {
        _uiState.value = _uiState.value.copy(selectedRange = range)
        loadStats()
    }

    /** Reactively reload stats when habits change. */
    private fun observeStats() {
        viewModelScope.launch {
            repository.getActiveHabits().collect { _ ->
                loadStats()
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val habits = repository.getActiveHabitsSnapshot()
            if (habits.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    habitStats = emptyList(),
                    dailyCompletions = emptyList(),
                    isLoading = false
                )
                return@launch
            }

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
            val totalHabitsCount = habits.size

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
                    add(DayCompletion(dateStr, dayLabel, dailyMap[dateStr] ?: 0, totalHabitsCount))
                    cursor = cursor.plusDays(1)
                }
            }

            val totalCompletions = dailyMap.values.sum()
            val totalScheduledDays = dailyCompletions.size
            val overallRate = if (totalScheduledDays > 0 && totalHabitsCount > 0)
                totalCompletions.toFloat() / (totalScheduledDays * totalHabitsCount)
            else 0f

            val bestDayEntry = dailyMap.maxByOrNull { it.value }
            val bestDay = bestDayEntry?.let {
                DateUtils.friendlyDate(it.key) + " (${it.value})"
            } ?: "—"

            val perfectDays = dailyCompletions.count { it.completedCount >= totalHabitsCount }

            _uiState.value = StatsUiState(
                selectedRange = range,
                overallCompletionRate = overallRate.coerceIn(0f, 1f),
                totalCompletions = totalCompletions,
                perfectDays = perfectDays,
                totalTrackedDays = totalScheduledDays,
                bestDay = bestDay,
                habitStats = habitStats,
                dailyCompletions = dailyCompletions,
                isLoading = false
            )
        }
    }
}
