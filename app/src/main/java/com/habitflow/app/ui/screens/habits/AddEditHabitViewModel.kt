package com.habitflow.app.ui.screens.habits

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.util.Constants
import com.habitflow.app.util.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditHabitState(
    val habitId: Long? = null,
    val name: String = "",
    val description: String = "",
    val frequencyType: String = "daily",
    val frequencyValue: Int = 3,
    val selectedDays: Set<Int> = setOf(1, 2, 3, 4, 5),  // Mon–Fri default
    val colorHex: String = Constants.HABIT_COLORS[0],
    val iconName: String = Constants.HABIT_ICONS[0],
    val reminderHour: Int = -1,
    val reminderMinute: Int = 0,
    val isEdit: Boolean = false
)

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditHabitState())
    val uiState: StateFlow<AddEditHabitState> = _uiState.asStateFlow()

    private var existingHabit: Habit? = null

    fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            existingHabit = habit

            val selectedDays = habit.targetDays
                .removeSurrounding("[", "]")
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()

            _uiState.value = AddEditHabitState(
                habitId = habit.id,
                name = habit.name,
                description = habit.description,
                frequencyType = habit.frequencyType,
                frequencyValue = habit.frequencyValue,
                selectedDays = selectedDays,
                colorHex = habit.colorHex,
                iconName = habit.iconName,
                reminderHour = habit.reminderHour,
                reminderMinute = habit.reminderMinute,
                isEdit = true
            )
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateFrequencyType(type: String) {
        _uiState.value = _uiState.value.copy(frequencyType = type)
    }

    fun updateFrequencyValue(value: Int) {
        _uiState.value = _uiState.value.copy(frequencyValue = value)
    }

    fun toggleDay(day: Int) {
        val current = _uiState.value.selectedDays.toMutableSet()
        if (day in current) {
            if (current.size > 1) current.remove(day)
        } else {
            current.add(day)
        }
        _uiState.value = _uiState.value.copy(selectedDays = current)
    }

    fun updateColor(colorHex: String) {
        _uiState.value = _uiState.value.copy(colorHex = colorHex)
    }

    fun updateIcon(iconName: String) {
        _uiState.value = _uiState.value.copy(iconName = iconName)
    }

    fun updateReminder(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(reminderHour = hour, reminderMinute = minute)
    }

    fun saveHabit() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        val targetDays = if (state.frequencyType == "specific_days") {
            "[${state.selectedDays.sorted().joinToString(",")}]"
        } else ""

        viewModelScope.launch {
            val habit: Habit
            if (state.isEdit && existingHabit != null) {
                habit = existingHabit!!.copy(
                    name = state.name,
                    description = state.description,
                    frequencyType = state.frequencyType,
                    frequencyValue = state.frequencyValue,
                    targetDays = targetDays,
                    colorHex = state.colorHex,
                    iconName = state.iconName,
                    reminderHour = state.reminderHour,
                    reminderMinute = state.reminderMinute
                )
                repository.updateHabit(habit)
            } else {
                habit = Habit(
                    name = state.name,
                    description = state.description,
                    frequencyType = state.frequencyType,
                    frequencyValue = state.frequencyValue,
                    targetDays = targetDays,
                    colorHex = state.colorHex,
                    iconName = state.iconName,
                    reminderHour = state.reminderHour,
                    reminderMinute = state.reminderMinute
                )
                val id = repository.insertHabit(habit)
                // Schedule reminder for the newly created habit
                if (state.reminderHour >= 0) {
                    ReminderScheduler.scheduleReminder(appContext, habit.copy(id = id))
                }
                return@launch
            }

            // Update reminder scheduling
            if (state.reminderHour >= 0) {
                ReminderScheduler.scheduleReminder(appContext, habit)
            } else {
                ReminderScheduler.cancelReminder(appContext, habit.id)
            }
        }
    }
}
