package com.habitflow.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habitflow.app.BuildConfig
import com.habitflow.app.ui.components.HFCard

/**
 * BUG-23 FIX: Export and Backup buttons now show a "Coming soon" snackbar
 * instead of doing nothing silently when tapped.
 */
@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onPrivacyPolicy: () -> Unit = {}
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingSnackbar by remember { mutableStateOf<String?>(null) }

    // Show snackbar when a "coming soon" action is tapped
    LaunchedEffect(pendingSnackbar) {
        pendingSnackbar?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            pendingSnackbar = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // ── Appearance ──────────────────────────────────────
            SectionHeader("Appearance")
            HFCard {
                SettingsToggleRow(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = darkMode,
                    onCheckedChange = onDarkModeChange
                )
            }

            // ── Data ────────────────────────────────────────────
            SectionHeader("Data")
            HFCard {
                Column {
                    SettingsClickRow(
                        icon = Icons.Rounded.FileDownload,
                        title = "Export to CSV",
                        subtitle = "Export your habit data as a spreadsheet",
                        onClick = { pendingSnackbar = "CSV export coming in a future update" }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsClickRow(
                        icon = Icons.Rounded.Backup,
                        title = "Backup & Restore",
                        subtitle = "Save or restore your habit data",
                        onClick = { pendingSnackbar = "Backup & restore coming in a future update" }
                    )
                }
            }

            // ── Legal ───────────────────────────────────────────
            SectionHeader("Legal")
            HFCard {
                SettingsClickRow(
                    icon = Icons.Rounded.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "How your data is handled",
                    onClick = onPrivacyPolicy
                )
            }

            // ── About ───────────────────────────────────────────
            SectionHeader("About")
            HFCard {
                Column {
                    SettingsInfoRow(
                        icon = Icons.Rounded.Info,
                        title = "Version",
                        value = BuildConfig.VERSION_NAME
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsClickRow(
                        icon = Icons.Rounded.Description,
                        title = "About Habit Tracker",
                        subtitle = "Learn more about this app",
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            Text(
                text = "Made with \u2764\uFE0F by Bhuvnendra",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Habit Tracker", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Habit Tracker helps you build lasting habits with beautiful tracking, " +
                            "streaks, and insights. Stay consistent, stay motivated.\n\n" +
                            "Version ${BuildConfig.VERSION_NAME}"
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsClickRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
