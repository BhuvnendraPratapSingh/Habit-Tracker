package com.habitflow.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.model.HabitWithStats
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class TodayUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val completedCount: Int = 0,
    val totalScheduledCount: Int = 0,
    val greeting: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadTodayData()
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            repository.getActiveHabits().collect { habits ->
                val today = DateUtils.today()
                val scheduledHabits = habits.filter { habit ->
                    DateUtils.isScheduledDay(today, habit.frequencyType, habit.targetDays)
                }

                val habitsWithStats = scheduledHabits.map { habit ->
                    repository.getHabitWithStats(habit)
                }

                val completedCount = habitsWithStats.count { it.isCompletedToday }

                _uiState.value = TodayUiState(
                    habits = habitsWithStats,
                    completedCount = completedCount,
                    totalScheduledCount = scheduledHabits.size,
                    greeting = getGreeting(),
                    isLoading = false
                )
            }
        }
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val today = DateUtils.today()
            repository.toggleHabitForDate(habitId, today)
            // Refresh data
            refreshOnce()
        }
    }

    private suspend fun refreshOnce() {
        val habits = repository.getActiveHabits().first()
        val today = DateUtils.today()
        val scheduledHabits = habits.filter { habit ->
            DateUtils.isScheduledDay(today, habit.frequencyType, habit.targetDays)
        }
        val habitsWithStats = scheduledHabits.map { repository.getHabitWithStats(it) }
        val completedCount = habitsWithStats.count { it.isCompletedToday }

        _uiState.value = _uiState.value.copy(
            habits = habitsWithStats,
            completedCount = completedCount,
            totalScheduledCount = scheduledHabits.size
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
