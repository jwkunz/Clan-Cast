package com.privatefamily.broadcastsms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.privatefamily.broadcastsms.ui.theme.LogoGreen
import com.privatefamily.broadcastsms.ui.theme.LogoNavy
import com.privatefamily.broadcastsms.ui.theme.LogoSky

@Composable
fun HomeScreen(
    onOpenLists: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenCompose: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LogoNavy)
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Clan Cast",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Private family SMS broadcasts with reusable lists, templates, and a controlled send flow.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.92f),
            )
            Button(onClick = onOpenLists, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Distribution Lists")
            }
            Button(onClick = onOpenTemplates, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Templates")
            }
            Button(onClick = onOpenCompose, modifier = Modifier.fillMaxWidth()) {
                Text("Compose Broadcast")
            }
            Button(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
                Text("Broadcast History")
            }
            Text(
                text = "Review recipients, preview every message, and send with pause, resume, and history controls.",
                style = MaterialTheme.typography.bodyMedium,
                color = LogoSky.copy(alpha = 0.95f),
            )
            Text(
                text = "Connect • Share • Stay Close",
                style = MaterialTheme.typography.bodyMedium,
                color = LogoGreen,
            )
        }
    }
}
