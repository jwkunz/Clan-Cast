package com.privatefamily.broadcastsms.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privatefamily.broadcastsms.ui.viewmodel.ComposeBroadcastViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeBroadcastScreen(
    viewModel: ComposeBroadcastViewModel,
    onBack: () -> Unit,
    onContinueToPreflight: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compose Broadcast") },
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
                SectionCard(title = "Distribution List") {
                    if (uiState.lists.isEmpty()) {
                        Text("Create a list before composing a broadcast.")
                    } else {
                        uiState.lists.forEach { list ->
                            Button(
                                onClick = { viewModel.selectList(list.id) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                val selected = if (uiState.draft.distributionListId == list.id) "Selected" else "Select"
                                Text("$selected: ${list.name}")
                            }
                        }
                    }
                }
            }
            item {
                SectionCard(title = "Template") {
                    TextButton(onClick = { viewModel.selectTemplate(null) }) {
                        Text("Keep current message body")
                    }
                    uiState.templates.forEach { template ->
                        TextButton(onClick = { viewModel.selectTemplate(template) }) {
                            Text(template.name)
                        }
                    }
                }
            }
            item {
                SectionCard(title = "Broadcast Name") {
                    OutlinedTextField(
                        value = uiState.draft.broadcastName,
                        onValueChange = viewModel::updateBroadcastName,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Optional custom name") },
                        supportingText = {
                            Text("Leave blank to use the default timestamped broadcast name.")
                        },
                    )
                }
            }
            item {
                SectionCard(title = "Message Body") {
                    OutlinedTextField(
                        value = uiState.draft.messageBody,
                        onValueChange = viewModel::updateMessageBody,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        label = { Text("Broadcast message") },
                    )
                }
            }
            item {
                SectionCard(title = "Draft Validation") {
                    if (uiState.validationIssues.isEmpty()) {
                        Text("Draft is ready for preflight.")
                    } else {
                        uiState.validationIssues.forEach { issue ->
                            Text("- $issue")
                        }
                    }
                    Text("Selected recipients: ${uiState.selectedListMemberCount}")
                    Text("Selected template: ${uiState.selectedTemplate?.name ?: "None"}")
                    Text(
                        "Broadcast name: ${
                            uiState.draft.broadcastName.ifBlank { "Broadcast - YYYY-MM-DD HH:mm" }
                        }",
                    )
                    Button(
                        onClick = onContinueToPreflight,
                        enabled = uiState.canContinueToPreflight,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Continue To Preflight")
                    }
                }
            }
        }
    }
}
