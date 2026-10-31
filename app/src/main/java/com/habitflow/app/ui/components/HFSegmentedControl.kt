package com.habitflow.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * iOS-style segmented control with animated sliding pill indicator.
 *
 * BUG-22 FIX: Pill width and offset are computed from the inner
 * content area (after padding), so the pill lands exactly on each
 * segment regardless of item count.
 */
@Composable
fun HFSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val innerPadding = 3.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(innerPadding)
    ) {
        // Available width after padding is maxWidth (BoxWithConstraints reports inner size)
        val itemWidth = maxWidth / items.size
        val offsetX by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = tween(250),
            label = "segmentOffset"
        )

        // Sliding indicator pill
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .width(itemWidth)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(10.dp)
                )
        )

        // Labels
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == selectedIndex) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
