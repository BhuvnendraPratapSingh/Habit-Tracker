package com.habitflow.app.ui.screens.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.ui.components.BadgeCard
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Daily Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.dailyCompletions.sumOf { it.completedCount }} completed this period",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CompletionBarChart(
                            data = state.dailyCompletions,
                            selectedRange = state.selectedRange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }
        }

        // Badges section
        if (state.badges.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StreakGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${state.earnedBadgeCount}/${state.badges.size}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = StreakGold
                        )
                    }
                }
            }

            // Earned badges first, then locked — horizontal scroll
            item {
                val sorted = state.badges.sortedByDescending { it.isEarned }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sorted) { badge ->
                        BadgeCard(
                            earnedBadge = badge,
                            modifier = Modifier.width(130.dp)
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
                        Text("\uD83D\uDCCA", style = MaterialTheme.typography.displayLarge)
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
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
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
    selectedRange: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val insideLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val density = LocalDensity.current

    val isYearView = selectedRange == 2
    val isWeekView = selectedRange == 0

    val displayData = if (isYearView) {
        data.groupBy { it.date.substring(0, 7) }
            .map { (month, days) ->
                val monthLabel = days.first().dayLabel
                DayCompletion(
                    date = month,
                    dayLabel = monthLabel.take(3).lowercase()
                        .replaceFirstChar { it.uppercase() },
                    completedCount = days.sumOf { it.completedCount },
                    totalHabits = days.sumOf { it.totalHabits }.coerceAtLeast(1)
                )
            }
    } else {
        data
    }

    val maxValue = displayData.maxOfOrNull {
        it.totalHabits.coerceAtLeast(1)
    }?.toFloat() ?: 1f

    val labels = displayData.map { item ->
        when {
            isWeekView -> item.dayLabel.take(1)
            isYearView -> item.dayLabel
            else -> item.dayLabel
        }
    }

    Canvas(modifier = modifier) {
        val bottomLabelHeight = if (!isYearView) 20.dp.toPx() else 0f
        val chartHeight = size.height - bottomLabelHeight
        val barCount = displayData.size

        val totalGapRatio = 0.35f
        val barUnitWidth = size.width / (barCount + (barCount - 1) * totalGapRatio)
        val gapWidth = barUnitWidth * totalGapRatio

        val bottomLabelPaint = android.graphics.Paint().apply {
            color = labelColor.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val insideLabelPaint = android.graphics.Paint().apply {
            color = insideLabelColor.toArgb()
            textSize = with(density) { 9.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }

        displayData.forEachIndexed { index, item ->
            val x = index * (barUnitWidth + gapWidth)
            val barHeight = if (maxValue > 0)
                (item.completedCount.toFloat() / maxValue) * chartHeight
            else 0f

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(x, 0f),
                size = Size(barUnitWidth, chartHeight),
                cornerRadius = CornerRadius(barUnitWidth / 2, barUnitWidth / 2)
            )

            if (barHeight > 0) {
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barUnitWidth, barHeight),
                    cornerRadius = CornerRadius(barUnitWidth / 2, barUnitWidth / 2)
                )
            }

            val barCenterX = x + barUnitWidth / 2

            if (isYearView) {
                val label = labels[index]
                val textWidth = insideLabelPaint.measureText(label)
                val textY = chartHeight - 8.dp.toPx()

                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(
                    -90f, barCenterX, textY - textWidth / 2
                )
                drawContext.canvas.nativeCanvas.drawText(
                    label, barCenterX, textY, insideLabelPaint
                )
                drawContext.canvas.nativeCanvas.restore()
            } else {
                val label = labels[index]
                val shouldDrawLabel = when {
                    isWeekView -> true
                    displayData.size <= 14 -> true
                    else -> index % 5 == 0 || index == displayData.size - 1
                }
                if (shouldDrawLabel) {
                    drawContext.canvas.nativeCanvas.drawText(
                        label, barCenterX, chartHeight + 16.dp.toPx(), bottomLabelPaint
                    )
                }
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
                        text = "\uD83D\uDD25 ${item.streakInfo.currentStreak}d streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " \u2022 ${item.streakInfo.totalCompletions} total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
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
            Text(
                text = "${(item.completionRate * 100).roundToInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = habitColor
            )
        }
    }
}
