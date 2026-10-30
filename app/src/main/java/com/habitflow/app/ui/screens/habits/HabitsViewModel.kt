package com.habitflow.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.model.HabitWithStats
import com.habitflow.app.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitsUiState(
    val activeHabits: List<HabitWithStats> = emptyList(),
    val archivedHabits: List<Habit> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0   // 0 = Active, 1 = Archived
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            repository.getActiveHabits().collect { activeHabits ->
                val habitsWithStats = activeHabits.map { repository.getHabitWithStats(it) }
                _uiState.update { it.copy(
                    activeHabits = habitsWithStats,
                    isLoading = false
                ) }
            }
        }

        viewModelScope.launch {
            repository.getArchivedHabits().collect { archived ->
                _uiState.update { it.copy(archivedHabits = archived) }
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            repository.archiveHabit(habitId)
        }
    }

    fun unarchiveHabit(habitId: Long) {
        viewModelScope.launch {
            repository.unarchiveHabit(habitId)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }
}
