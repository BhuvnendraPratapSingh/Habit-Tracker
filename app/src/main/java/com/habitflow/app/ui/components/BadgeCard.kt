package com.habitflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.data.model.EarnedBadge

/**
 * Compact badge display — emoji + title.
 * Greyed out if not yet earned.
 */
@Composable
fun BadgeCard(
    earnedBadge: EarnedBadge,
    modifier: Modifier = Modifier
) {
    val badge = earnedBadge.badge
    val isEarned = earnedBadge.isEarned
    val tierColor = Color(badge.tier.colorHex)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEarned)
                tierColor.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEarned) badge.emoji else "\uD83D\uDD12",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = badge.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isEarned) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isEarned)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = badge.description,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isEarned)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                lineHeight = 14.sp
            )
            if (isEarned) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tierColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badge.tier.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = tierColor
                    )
                }
            }
        }
    }
}
