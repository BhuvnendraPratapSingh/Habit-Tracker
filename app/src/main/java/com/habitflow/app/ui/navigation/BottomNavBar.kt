package com.habitflow.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS-style bottom navigation bar with smooth animations and pill indicators.
 *
 * BUG-20 FIX: Each nav item has proper semantics for TalkBack:
 * - contentDescription announces "Today, tab, selected" etc.
 * - role = Tab so screen readers treat it as a tab bar
 * - selected state is announced
 */
@Composable
fun HFBottomNavBar(
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem.entries.forEach { item ->
            val isSelected = currentRoute == item.route

            val tintColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "navTint"
            )
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "navScale"
            )

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .semantics(mergeDescendants = true) {
                        contentDescription = "${item.label} tab"
                        role = Role.Tab
                        selected = isSelected
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClickLabel = "Navigate to ${item.label}"
                    ) { onItemSelected(item) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(width = 56.dp, height = 32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )
                    }
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null, // Handled by parent semantics
                        tint = tintColor,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scale)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = tintColor
                )
            }
        }
    }
}
