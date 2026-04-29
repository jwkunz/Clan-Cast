package com.privatefamily.broadcastsms.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.privatefamily.broadcastsms.ui.viewmodel.BroadcastDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastDetailScreen(
    viewModel: BroadcastDetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val broadcast = uiState.broadcast

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(broadcast?.name ?: "Broadcast Detail") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
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
            item {
                SectionCard(title = "Summary") {
                    Text("Status: ${broadcast?.status ?: "Unknown"}")
                    Text("Message body: ${broadcast?.messageBody.orEmpty()}")
                    Text("Recipients: ${broadcast?.totalRecipients ?: 0}")
                    Text("Duplicate policy: ${broadcast?.duplicatePolicy ?: "Unknown"}")
                    Text("Invalid number policy: ${broadcast?.invalidNumberPolicy ?: "Unknown"}")
                }
            }
            items(uiState.recipients, key = { it.id }) { recipient ->
                SectionCard(title = recipient.displayName) {
                    Text(recipient.phoneNumber)
                    Text("Status: ${recipient.status}")
                    Text(recipient.renderedMessage)
                }
            }
        }
    }
}
