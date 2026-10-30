package com.habitflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.habitflow.app.data.model.HabitWithStats
import com.habitflow.app.ui.theme.parseHexColor

/**
 * Main habit card used on the Today screen.
 * Shows habit name, streak, mini heatmap, and a check button.
 */
@Composable
fun HabitCard(
    habitWithStats: HabitWithStats,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithStats.habit
    val streak = habitWithStats.streakInfo
    val isCompleted = habitWithStats.isCompletedToday
    val habitColor = parseHexColor(habit.colorHex)

    // Animated check scale
    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    val cardBg by animateColorAsState(
        targetValue = if (isCompleted)
            habitColor.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        habitColor.copy(alpha = 0.15f),
                        CircleShape
                    ),
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

            // Name + streak info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (streak.currentStreak > 0) {
                        StreakBadge(
                            streakDays = streak.currentStreak,
                            compact = true
                        )
                    }

                    WeeklyHeatmap(
                        last7Days = habitWithStats.last7Days,
                        colorHex = habit.colorHex
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Check button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(checkScale)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) habitColor
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = if (isCompleted) "Completed" else "Mark complete",
                    tint = if (isCompleted) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Maps icon name strings to Material Icons.
 */
fun getHabitIcon(iconName: String): ImageVector {
    return when (iconName) {
        "CheckCircle" -> Icons.Rounded.CheckCircle
        "FitnessCenter" -> Icons.Rounded.FitnessCenter
        "MenuBook" -> Icons.Rounded.MenuBook
        "SelfImprovement" -> Icons.Rounded.SelfImprovement
        "WaterDrop" -> Icons.Rounded.WaterDrop
        "DirectionsRun" -> Icons.Rounded.DirectionsRun
        "Bedtime" -> Icons.Rounded.Bedtime
        "Restaurant" -> Icons.Rounded.Restaurant
        "Code" -> Icons.Rounded.Code
        "Brush" -> Icons.Rounded.Brush
        "MusicNote" -> Icons.Rounded.MusicNote
        "School" -> Icons.Rounded.School
        "Favorite" -> Icons.Rounded.Favorite
        "EmojiNature" -> Icons.Rounded.EmojiNature
        "SportsEsports" -> Icons.Rounded.SportsEsports
        "Savings" -> Icons.Rounded.Savings
        else -> Icons.Rounded.CheckCircle
    }
}
