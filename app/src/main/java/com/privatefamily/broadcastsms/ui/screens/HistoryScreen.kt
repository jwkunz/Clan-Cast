package com.privatefamily.broadcastsms.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Broadcast History") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    if (broadcasts.isNotEmpty()) {
                        TextButton(onClick = viewModel::deleteAllBroadcasts) {
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
                    Button(
                        onClick = { onBroadcastSelected(broadcast.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Open Broadcast")
                    }
                    Button(
                        onClick = { viewModel.deleteBroadcast(broadcast.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Delete Broadcast")
                    }
                }
            }
        }
    }
}
