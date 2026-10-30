package com.habitflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.theme.StreakFire
import com.habitflow.app.ui.theme.StreakGold

/**
 * A pill-shaped streak badge showing the flame icon and day count.
 * Pulses gently when the streak is active (>0).
 */
@Composable
fun StreakBadge(
    streakDays: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isActive = streakDays > 0

    val pulseAnim = rememberInfiniteTransition(label = "streakPulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            streakDays >= 30 -> StreakGold.copy(alpha = 0.15f)
            streakDays >= 7 -> StreakFire.copy(alpha = 0.12f)
            streakDays > 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        label = "bgColor"
    )

    val iconColor = when {
        streakDays >= 30 -> StreakGold
        streakDays >= 7 -> StreakFire
        streakDays > 0 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Row(
        modifier = modifier
            .scale(scale)
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 4.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = "Streak",
            tint = iconColor,
            modifier = Modifier.size(if (compact) 14.dp else 18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$streakDays",
            style = if (compact) MaterialTheme.typography.labelMedium
            else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
