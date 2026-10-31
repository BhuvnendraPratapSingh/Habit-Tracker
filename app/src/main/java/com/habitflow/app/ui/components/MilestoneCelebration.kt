package com.habitflow.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class MilestoneEvent(
    val streakDays: Int,
    val habitName: String
)

/**
 * Returns milestone info if the streak count is a milestone, null otherwise.
 */
fun getMilestoneTitle(streakDays: Int): Pair<String, String>? {
    return when (streakDays) {
        7 -> "\uD83D\uDD25" to "7-Day Spark!"
        14 -> "\u26A1" to "2-Week Warrior!"
        21 -> "\uD83D\uDCAA" to "21-Day Habit Formed!"
        30 -> "\uD83C\uDFC6" to "30-Day Champion!"
        50 -> "\u2B50" to "50-Day Legend!"
        100 -> "\uD83D\uDC8E" to "100-Day Diamond!"
        365 -> "\uD83D\uDC51" to "1 Year Unstoppable!"
        else -> null
    }
}

/**
 * Full-screen milestone celebration dialog with confetti.
 *
 * BUG-11 FIX: Uses Animatable starting at 0f → 1f with a spring,
 * so the card actually bounces in instead of popping at scale 1f.
 */
@Composable
fun MilestoneCelebration(
    milestone: MilestoneEvent?,
    onDismiss: () -> Unit
) {
    if (milestone == null) return
    val info = getMilestoneTitle(milestone.streakDays) ?: return

    // Animatable starts at 0 and springs to 1 — visible bounce-in
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade in + scale up with a bouncy spring
        alpha.animateTo(1f, tween(200))
    }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Confetti behind the card
            ConfettiOverlay(trigger = true)

            // Celebration card with bounce-in
            Card(
                modifier = Modifier
                    .padding(32.dp)
                    .scale(scale.value)
                    .alpha(alpha.value),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF7ED),
                                    Color(0xFFFFFBEB),
                                    Color(0xFFFFFFFF)
                                )
                            )
                        )
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = info.first,
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = info.second,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFB45309)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${milestone.streakDays} days of ${milestone.habitName}",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF78716C)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Consistency is your superpower.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFA8A29E)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    FilledTonalButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Keep Going \uD83D\uDE80")
                    }
                }
            }
        }
    }
}
