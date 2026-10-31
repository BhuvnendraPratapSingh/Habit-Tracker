package com.habitflow.app.ui.screens.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.ui.components.HFCard
import com.habitflow.app.ui.components.HFSegmentedControl
import com.habitflow.app.ui.components.getHabitIcon
import com.habitflow.app.ui.theme.parseHexColor
import com.habitflow.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    habitId: Long?,
    onBack: () -> Unit,
    viewModel: AddEditHabitViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.loadHabit(habitId)
        }
    }

    val isEdit = habitId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Edit Habit" else "New Habit",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveHabit()
                            onBack()
                        },
                        enabled = state.name.isNotBlank()
                    ) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.Bold,
                            color = if (state.name.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Name ────────────────────────────────────────
            HFCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Habit Name",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.updateName(it) },
                        placeholder = { Text("e.g., Read for 30 minutes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // ── Description ─────────────────────────────────
            HFCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Description (optional)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        placeholder = { Text("Why is this habit important?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // ── Frequency ───────────────────────────────────
            HFCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Frequency",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    HFSegmentedControl(
                        items = listOf("Daily", "Weekly", "Specific Days"),
                        selectedIndex = when (state.frequencyType) {
                            "daily" -> 0
                            "weekly" -> 1
                            "specific_days" -> 2
                            else -> 0
                        },
                        onItemSelected = { index ->
                            viewModel.updateFrequencyType(
                                when (index) {
                                    0 -> "daily"
                                    1 -> "weekly"
                                    2 -> "specific_days"
                                    else -> "daily"
                                }
                            )
                        }
                    )

                    // Weekly: times per week slider
                    if (state.frequencyType == "weekly") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "${state.frequencyValue} times per week",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = state.frequencyValue.toFloat(),
                            onValueChange = { viewModel.updateFrequencyValue(it.toInt()) },
                            valueRange = 1f..6f,
                            steps = 4
                        )
                    }

                    // Specific days: day chips
                    if (state.frequencyType == "specific_days") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                            dayLabels.forEachIndexed { index, label ->
                                val dayNum = index + 1
                                val isSelected = dayNum in state.selectedDays
                                val bgColor by animateColorAsState(
                                    targetValue = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    label = "dayBg"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .clickable { viewModel.toggleDay(dayNum) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Color ───────────────────────────────────────
            HFCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Color",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.height(88.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(Constants.HABIT_COLORS) { colorHex ->
                            val color = parseHexColor(colorHex)
                            val isSelected = state.colorHex == colorHex

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .clickable { viewModel.updateColor(colorHex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Icon ────────────────────────────────────────
            HFCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Icon",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        modifier = Modifier.height(88.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(Constants.HABIT_ICONS) { iconName ->
                            val isSelected = state.iconName == iconName
                            val habitColor = parseHexColor(state.colorHex)

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) habitColor.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(
                                            2.dp,
                                            habitColor,
                                            RoundedCornerShape(12.dp)
                                        ) else Modifier
                                    )
                                    .clickable { viewModel.updateIcon(iconName) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getHabitIcon(iconName),
                                    contentDescription = iconName,
                                    tint = if (isSelected) habitColor
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Reminder ────────────────────────────────────
            HFCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Reminder",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (state.reminderHour >= 0)
                                    String.format("%02d:%02d", state.reminderHour, state.reminderMinute)
                                else "No reminder",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Switch(
                            checked = state.reminderHour >= 0,
                            onCheckedChange = { enabled ->
                                if (enabled) viewModel.updateReminder(9, 0)
                                else viewModel.updateReminder(-1, 0)
                            }
                        )
                    }

                    if (state.reminderHour >= 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // BUG-21 FIX: Display is always from ViewModel state (formatted).
                            // Input is filtered to digits-only and clamped before updating VM.
                            // No flash of invalid values — the TextField value is always the
                            // coerced state, not the raw user input.
                            OutlinedTextField(
                                value = state.reminderHour.toString().padStart(2, '0'),
                                onValueChange = { raw ->
                                    val digits = raw.filter { c -> c.isDigit() }.take(2)
                                    if (digits.isEmpty()) return@OutlinedTextField
                                    val h = digits.toIntOrNull()?.coerceIn(0, 23) ?: return@OutlinedTextField
                                    viewModel.updateReminder(h, state.reminderMinute)
                                },
                                label = { Text("Hour (0-23)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.reminderMinute.toString().padStart(2, '0'),
                                onValueChange = { raw ->
                                    val digits = raw.filter { c -> c.isDigit() }.take(2)
                                    if (digits.isEmpty()) return@OutlinedTextField
                                    val m = digits.toIntOrNull()?.coerceIn(0, 59) ?: return@OutlinedTextField
                                    viewModel.updateReminder(state.reminderHour, m)
                                },
                                label = { Text("Min (0-59)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
