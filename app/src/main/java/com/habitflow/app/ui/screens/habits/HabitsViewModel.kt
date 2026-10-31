package com.habitflow.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.model.HabitWithStats
import com.habitflow.app.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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

    /**
     * BUG-13 FIX: Refresh trigger so streaks update when entries change.
     * Combined with the habits Flow — any change to either causes a reload.
     */
    private val refreshTrigger = MutableStateFlow(0L)

    init {
        observeActiveHabits()
        observeArchivedHabits()
    }

    /** Called from UI when Habits screen becomes visible. */
    fun onScreenVisible() {
        refreshTrigger.value = System.currentTimeMillis()
    }

    private fun observeActiveHabits() {
        viewModelScope.launch {
            combine(
                repository.getActiveHabits(),
                refreshTrigger
            ) { habits, _ -> habits }
                .collect { activeHabits ->
                    val habitsWithStats = activeHabits.map { repository.getHabitWithStats(it) }
                    _uiState.update {
                        it.copy(activeHabits = habitsWithStats, isLoading = false)
                    }
                }
        }
    }

    private fun observeArchivedHabits() {
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
            refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun unarchiveHabit(habitId: Long) {
        viewModelScope.launch {
            repository.unarchiveHabit(habitId)
            refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }
}
