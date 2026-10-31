package com.habitflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WeeklySummary(
    val completedCount: Int,
    val totalScheduled: Int,
    val bestStreak: Int,
    val perfectDays: Int,
    val bestDayLabel: String
)

/**
 * A visually rich weekly summary card shown on the Today screen.
 */
@Composable
fun WeeklySummaryCard(
    summary: WeeklySummary,
    modifier: Modifier = Modifier
) {
    val completionRate = if (summary.totalScheduled > 0)
        (summary.completedCount.toFloat() / summary.totalScheduled * 100).toInt()
    else 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF7C3AED).copy(alpha = 0.08f),
                            Color(0xFF8B5CF6).copy(alpha = 0.04f),
                            Color(0xFFA78BFA).copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "This Week",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    icon = Icons.Rounded.TrendingUp,
                    value = "$completionRate%",
                    label = "Done",
                    color = Color(0xFF34D399)
                )
                SummaryItem(
                    icon = Icons.Rounded.LocalFireDepartment,
                    value = "${summary.bestStreak}d",
                    label = "Best Streak",
                    color = Color(0xFFFF6B35)
                )
                SummaryItem(
                    icon = Icons.Rounded.EmojiEvents,
                    value = "${summary.perfectDays}",
                    label = "Perfect Days",
                    color = Color(0xFFFFD700)
                )
            }

            if (summary.bestDayLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "\u2B50 Best day: ${summary.bestDayLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
