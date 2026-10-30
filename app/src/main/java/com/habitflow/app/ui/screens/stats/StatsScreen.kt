package com.habitflow.app.ui.screens.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.ui.components.HFCard
import com.habitflow.app.ui.components.HFSegmentedControl
import com.habitflow.app.ui.components.getHabitIcon
import com.habitflow.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

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
                    text = "Statistics",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Range selector
        item {
            HFSegmentedControl(
                items = listOf("Week", "Month", "Year"),
                selectedIndex = state.selectedRange,
                onItemSelected = { viewModel.setSelectedRange(it) }
            )
        }

        // Overview cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMiniCard(
                    icon = Icons.Rounded.TrendingUp,
                    label = "Completion",
                    value = "${(state.overallCompletionRate * 100).roundToInt()}%",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    icon = Icons.Rounded.EmojiEvents,
                    label = "Perfect Days",
                    value = "${state.perfectDays}",
                    color = StreakGold,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    icon = Icons.Rounded.LocalFireDepartment,
                    label = "Total Done",
                    value = "${state.totalCompletions}",
                    color = StreakFire,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Bar chart
        if (state.dailyCompletions.isNotEmpty()) {
            item {
                HFCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Daily Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        CompletionBarChart(
                            data = state.dailyCompletions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }
        }

        // Per-habit breakdown
        if (state.habitStats.isNotEmpty()) {
            item {
                Text(
                    text = "Per Habit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(state.habitStats) { item ->
                HabitStatCard(item)
            }
        }

        // Empty state
        if (state.habitStats.isEmpty() && !state.isLoading) {
            item {
                HFCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📊", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No data yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Complete habits to see your stats",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun StatMiniCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    HFCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompletionBarChart(
    data: List<DayCompletion>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    // For yearly view, aggregate by month
    val displayData = if (data.size > 60) {
        data.groupBy { it.date.substring(0, 7) }  // Group by "yyyy-MM"
            .map { (month, days) ->
                DayCompletion(
                    date = month,
                    dayLabel = days.first().dayLabel,
                    completedCount = days.sumOf { it.completedCount },
                    totalHabits = days.sumOf { it.totalHabits }.coerceAtLeast(1)
                )
            }
    } else if (data.size > 14) {
        // For month view, show every other day label
        data
    } else {
        data
    }

    val maxValue = displayData.maxOfOrNull {
        it.totalHabits.coerceAtLeast(1)
    }?.toFloat() ?: 1f

    Canvas(modifier = modifier) {
        val barWidth = (size.width / displayData.size) * 0.6f
        val gap = (size.width / displayData.size) * 0.4f
        val chartHeight = size.height - 24.dp.toPx()

        displayData.forEachIndexed { index, item ->
            val x = index * (barWidth + gap) + gap / 2
            val maxBarHeight = chartHeight
            val barHeight = if (maxValue > 0)
                (item.completedCount.toFloat() / maxValue) * maxBarHeight
            else 0f

            // Track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, maxBarHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )

            // Value bar
            if (barHeight > 0) {
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.8f),
                    topLeft = Offset(x, maxBarHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    }
}

@Composable
private fun HabitStatCard(item: HabitStatItem) {
    val habitColor = parseHexColor(item.habit.colorHex)
    val animatedRate by animateFloatAsState(
        targetValue = item.completionRate,
        animationSpec = tween(800),
        label = "rate"
    )

    HFCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    imageVector = getHabitIcon(item.habit.iconName),
                    contentDescription = null,
                    tint = habitColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔥 ${item.streakInfo.currentStreak}d streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ${item.streakInfo.totalCompletions} total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { animatedRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = habitColor,
                    trackColor = habitColor.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Percentage
            Text(
                text = "${(item.completionRate * 100).roundToInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = habitColor
            )
        }
    }
}
