package com.habitflow.app.ui.screens.habits

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.data.db.entity.Habit
import com.habitflow.app.data.model.HabitWithStats
import com.habitflow.app.ui.components.HFCard
import com.habitflow.app.ui.components.HFSegmentedControl
import com.habitflow.app.ui.components.StreakBadge
import com.habitflow.app.ui.components.getHabitIcon
import com.habitflow.app.ui.theme.parseHexColor

@Composable
fun HabitsScreen(
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    // BUG-13 FIX: Refresh streaks when this screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Column {
                    Spacer(modifier = Modifier.statusBarsPadding())
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "My Habits",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Segmented control
            item {
                HFSegmentedControl(
                    items = listOf("Active", "Archived"),
                    selectedIndex = state.selectedTab,
                    onItemSelected = { viewModel.setSelectedTab(it) }
                )
            }

            // Content based on tab
            if (state.selectedTab == 0) {
                // Active habits
                if (state.activeHabits.isEmpty() && !state.isLoading) {
                    item {
                        HFCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("âœ¨", style = MaterialTheme.typography.displayLarge)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No active habits",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Create one to get started",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = state.activeHabits,
                        key = { it.habit.id }
                    ) { habitWithStats ->
                        HabitManageCard(
                            habitWithStats = habitWithStats,
                            onEdit = { onEditHabit(habitWithStats.habit.id) },
                            onArchive = { viewModel.archiveHabit(habitWithStats.habit.id) },
                            onDelete = { habitToDelete = habitWithStats.habit }
                        )
                    }
                }
            } else {
                // Archived habits
                if (state.archivedHabits.isEmpty()) {
                    item {
                        HFCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("ðŸ“¦", style = MaterialTheme.typography.displayLarge)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No archived habits",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = state.archivedHabits,
                        key = { it.id }
                    ) { habit ->
                        ArchivedHabitCard(
                            habit = habit,
                            onUnarchive = { viewModel.unarchiveHabit(habit.id) },
                            onDelete = { habitToDelete = habit }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddHabit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Habit", modifier = Modifier.size(28.dp))
        }

        // Delete confirmation dialog
        if (habitToDelete != null) {
            AlertDialog(
                onDismissRequest = { habitToDelete = null },
                title = { Text("Delete Habit") },
                text = {
                    Text("Are you sure you want to delete \"${habitToDelete?.name}\"? This will remove all history for this habit.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        habitToDelete?.let { viewModel.deleteHabit(it) }
                        habitToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { habitToDelete = null }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun HabitManageCard(
    habitWithStats: HabitWithStats,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = habitWithStats.habit
    val habitColor = parseHexColor(habit.colorHex)

    HFCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(habitColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getHabitIcon(habit.iconName),
                    contentDescription = null,
                    tint = habitColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append(when (habit.frequencyType) {
                            "daily" -> "Every day"
                            "weekly" -> "${habit.frequencyValue}Ã— per week"
                            "specific_days" -> "Specific days"
                            else -> "Daily"
                        })
                        append(" â€¢ ")
                        append("${habitWithStats.streakInfo.totalCompletions} completions")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StreakBadge(
                streakDays = habitWithStats.streakInfo.currentStreak,
                compact = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            // More menu
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { menuExpanded = false; onEdit() },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Archive") },
                        onClick = { menuExpanded = false; onArchive() },
                        leadingIcon = { Icon(Icons.Rounded.Archive, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onDelete() },
                        leadingIcon = {
                            Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchivedHabitCard(
    habit: Habit,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit
) {
    val habitColor = parseHexColor(habit.colorHex)

    HFCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(habitColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getHabitIcon(habit.iconName),
                    contentDescription = null,
                    tint = habitColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onUnarchive) {
                Icon(Icons.Rounded.Unarchive, "Unarchive", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}


