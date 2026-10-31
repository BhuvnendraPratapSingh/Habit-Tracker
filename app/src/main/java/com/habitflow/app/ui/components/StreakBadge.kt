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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.habitflow.app.ui.theme.ErrorRed
import com.habitflow.app.ui.theme.StreakFire
import com.habitflow.app.ui.theme.StreakGold

/**
 * A pill-shaped streak badge with 3 visual states:
 *
 * 1. NORMAL — solid display with gentle pulse when streak > 0
 * 2. BLINK  — fast blink when today's habit is NOT yet checked (streak at risk)
 * 3. STRIKETHROUGH — streak text has strikethrough when yesterday was missed (streak broken)
 *
 * Day boundary: midnight 12:00 AM (uses LocalDate.now() which resets at 00:00)
 */
@Composable
fun StreakBadge(
    streakDays: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    isCompletedToday: Boolean = true,
    yesterdayMissed: Boolean = false
) {
    val isActive = streakDays > 0
    val shouldBlink = isActive && !isCompletedToday && !yesterdayMissed
    val isStreakBroken = yesterdayMissed && !isCompletedToday

    val infiniteTransition = rememberInfiniteTransition(label = "streakAnim")

    // Blink alpha — fast pulse between 0.3 and 1.0 when not checked today
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldBlink) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (shouldBlink) 600 else 1200,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    // Gentle scale pulse for active streaks
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = when {
            shouldBlink -> 1.08f   // Slightly bigger pulse when blinking
            isActive -> 1.04f      // Normal gentle pulse
            else -> 1f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (shouldBlink) 600 else 1200,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isStreakBroken -> ErrorRed.copy(alpha = 0.1f)
            shouldBlink -> StreakFire.copy(alpha = 0.15f)
            streakDays >= 30 -> StreakGold.copy(alpha = 0.15f)
            streakDays >= 7 -> StreakFire.copy(alpha = 0.12f)
            streakDays > 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        label = "bgColor"
    )

    val iconColor = when {
        isStreakBroken -> ErrorRed.copy(alpha = 0.5f)
        shouldBlink -> StreakFire
        streakDays >= 30 -> StreakGold
        streakDays >= 7 -> StreakFire
        streakDays > 0 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    val textColor = when {
        isStreakBroken -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        isActive -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val textDecoration = if (isStreakBroken) TextDecoration.LineThrough else TextDecoration.None

    Row(
        modifier = modifier
            .scale(scale)
            .alpha(blinkAlpha)
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 6.dp
            ),
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
            color = textColor,
            textDecoration = textDecoration
        )
    }
}
