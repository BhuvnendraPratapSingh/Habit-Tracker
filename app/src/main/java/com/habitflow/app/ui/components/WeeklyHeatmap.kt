package com.habitflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habitflow.app.ui.theme.parseHexColor

/**
 * Shows the last 7 days as small colored dots/squares.
 * Completed days show the habit's color, missed days are gray.
 */
@Composable
fun WeeklyHeatmap(
    last7Days: List<Boolean>,
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val habitColor = parseHexColor(colorHex)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        last7Days.forEachIndexed { index, completed ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (completed) habitColor.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}
