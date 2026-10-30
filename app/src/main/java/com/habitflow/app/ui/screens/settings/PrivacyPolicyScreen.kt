package com.habitflow.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Privacy policy screen — required for Play Store submission.
 * Explains that Habit Tracker stores all data locally and collects nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PolicySection(
                title = "Overview",
                body = "Habit Tracker is a personal habit tracking application. Your privacy is important to us. This policy explains how Habit Tracker handles your data."
            )

            PolicySection(
                title = "Data Collection",
                body = "Habit Tracker does NOT collect, transmit, or share any personal data. All habit data, settings, and preferences are stored exclusively on your device in a local database."
            )

            PolicySection(
                title = "Data Storage",
                body = "All information you enter (habit names, descriptions, completion records, and settings) is stored locally on your device using an encrypted SQLite database. No data is sent to any server, cloud service, or third party."
            )

            PolicySection(
                title = "Internet Access",
                body = "Habit Tracker does not require internet access and does not make any network requests. The app functions entirely offline."
            )

            PolicySection(
                title = "Permissions",
                body = "• Notifications: Used solely to deliver habit reminders you configure.\n• Boot Completed: Used to reschedule your reminders after a device restart.\n\nNo other permissions are requested."
            )

            PolicySection(
                title = "Third-Party Services",
                body = "Habit Tracker does not integrate with any third-party analytics, advertising, or tracking services."
            )

            PolicySection(
                title = "Data Deletion",
                body = "You can delete individual habits and their history at any time from within the app. Uninstalling the app removes all data permanently."
            )

            PolicySection(
                title = "Children's Privacy",
                body = "Habit Tracker does not knowingly collect any information from children under 13. The app contains no age-restricted content."
            )

            PolicySection(
                title = "Changes to This Policy",
                body = "We may update this privacy policy from time to time. Any changes will be reflected in the app's Settings screen."
            )

            PolicySection(
                title = "Contact",
                body = "If you have questions about this privacy policy, please contact us through the app's listing on Google Play Store."
            )

            Text(
                text = "Last updated: October 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
