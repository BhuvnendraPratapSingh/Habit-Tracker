package com.habitflow.app.ui.screens.today

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.habitflow.app.ui.components.*

@Composable
fun TodayScreen(
    onAddHabit: () -> Unit,
    onHabitClick: (Long) -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val milestoneEvent by viewModel.milestoneEvent.collectAsState()
    val habitChecked by viewModel.habitCheckedEvent.collectAsState()
    val view = LocalView.current

    // Use the Activity lifecycle so ON_RESUME fires when app comes from background
    val activity = LocalContext.current as? androidx.activity.ComponentActivity
    val activityLifecycle = activity?.lifecycle

    DisposableEffect(activityLifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenVisible()
            }
        }
        activityLifecycle?.addObserver(observer)
        onDispose {
            activityLifecycle?.removeObserver(observer)
        }
    }

    // Also refresh when this composable enters composition (tab navigation)
    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    // BUG-1 FIX: Haptic feedback with API level guard
    LaunchedEffect(habitChecked) {
        if (habitChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            view.playSoundEffect(SoundEffectConstants.CLICK)
            viewModel.clearHapticEvent()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Spacer(modifier = Modifier.statusBarsPadding())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.greeting,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Let's build great habits today",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                HFCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgress(
                            completed = state.completedCount,
                            total = state.totalScheduledCount,
                            color = MaterialTheme.colorScheme.primary,
                            size = 120.dp,
                            strokeWidth = 12.dp
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${state.completedCount} of ${state.totalScheduledCount} habits done",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.completedCount == state.totalScheduledCount && state.totalScheduledCount > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "All done! \uD83C\uDF89",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            state.weeklySummary?.let { summary ->
                item { WeeklySummaryCard(summary = summary) }
            }

            if (state.habits.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Habits",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${state.completedCount} of ${state.totalScheduledCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.habits.isEmpty() && !state.isLoading) {
                item { EmptyState(onAddHabit = onAddHabit) }
            } else {
                items(items = state.habits, key = { it.habit.id }) { habitWithStats ->
                    HabitCard(
                        habitWithStats = habitWithStats,
                        onToggle = { viewModel.toggleHabit(habitWithStats.habit.id) },
                        onClick = { onHabitClick(habitWithStats.habit.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

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

        if (state.showAllDoneConfetti) {
            ConfettiOverlay(trigger = true, onFinished = { viewModel.clearAllDoneConfetti() })
        }

        MilestoneCelebration(milestone = milestoneEvent, onDismiss = { viewModel.clearMilestone() })
    }
}

@Composable
private fun EmptyState(onAddHabit: () -> Unit) {
    HFCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "\uD83C\uDF31", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No habits yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Start your journey by creating your first habit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))
            FilledTonalButton(onClick = onAddHabit, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Habit")
            }
        }
    }
}
