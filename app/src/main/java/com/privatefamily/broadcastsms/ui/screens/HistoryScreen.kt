package com.privatefamily.broadcastsms.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privatefamily.broadcastsms.ui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onBroadcastSelected: (String) -> Unit,
) {
    val broadcasts by viewModel.broadcasts.collectAsStateWithLifecycle()
    var pendingDeleteBroadcastId by remember { mutableStateOf<String?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    val pendingDeleteBroadcast = broadcasts.firstOrNull { it.id == pendingDeleteBroadcastId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Broadcast History") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    if (broadcasts.isNotEmpty()) {
                        TextButton(onClick = { showClearAllDialog = true }) {
                            Text("Clear All")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (broadcasts.isEmpty()) {
                item {
                    SectionCard(title = "No Broadcasts Yet") {
                        Text("Create a broadcast from preflight to see its snapshot here.")
                    }
                }
            }
            items(broadcasts, key = { it.id }) { broadcast ->
                SectionCard(title = broadcast.name) {
                    Text("Status: ${broadcast.status}")
                    Text("Recipients: ${broadcast.totalRecipients}")
                    Text("Created: ${formatTimestamp(broadcast.createdAtEpochMillis)}")
                    Button(
                        onClick = { onBroadcastSelected(broadcast.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Open Broadcast")
                    }
                    Button(
                        onClick = { pendingDeleteBroadcastId = broadcast.id },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Delete Broadcast")
                    }
                }
            }
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear Broadcast History") },
            text = {
                Text("This removes every saved broadcast snapshot and recipient result from history.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllBroadcasts()
                        showClearAllDialog = false
                    },
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Keep History")
                }
            },
        )
    }

    if (pendingDeleteBroadcast != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteBroadcastId = null },
            title = { Text("Delete Broadcast") },
            text = {
                Text("Delete ${pendingDeleteBroadcast.name} and its saved recipient results from history?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBroadcast(pendingDeleteBroadcast.id)
                        pendingDeleteBroadcastId = null
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteBroadcastId = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}
