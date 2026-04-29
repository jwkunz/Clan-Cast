package com.privatefamily.broadcastsms.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privatefamily.broadcastsms.data.model.DuplicatePolicy
import com.privatefamily.broadcastsms.data.model.InvalidNumberPolicy
import com.privatefamily.broadcastsms.ui.viewmodel.PreflightViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreflightScreen(
    viewModel: PreflightViewModel,
    onBack: () -> Unit,
    onBroadcastCreated: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val result = uiState.result

    LaunchedEffect(uiState.createdBroadcastId) {
        val broadcastId = uiState.createdBroadcastId ?: return@LaunchedEffect
        onBroadcastCreated(broadcastId)
        viewModel.consumeCreatedBroadcast()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preflight") },
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
                    Text("Broadcast name: ${uiState.broadcastName.ifBlank { "Broadcast - YYYY-MM-DD HH:mm" }}")
                    Text("Total entries: ${result?.totalEntries ?: 0}")
                    Text("Unique normalized numbers: ${result?.uniquePhoneNumberCount ?: 0}")
                    Text("Ready recipients after policies: ${result?.validEntries?.size ?: 0}")
                }
            }

            item {
                SectionCard(title = "Blocking Errors") {
                    val hasBlocking = !result?.unsupportedTokenIssues.isNullOrEmpty() ||
                        !result?.missingFieldIssues.isNullOrEmpty()
                    if (!hasBlocking) {
                        Text("No blocking template errors found.")
                    } else {
                        result?.unsupportedTokenIssues?.forEach { token ->
                            Text("Unsupported token: {$token}")
                        }
                        result?.missingFieldIssues?.forEach { issue ->
                            Text("Missing ${issue.fieldName} for ${issue.displayName}")
                        }
                    }
                }
            }

            if (!result?.duplicateIssues.isNullOrEmpty()) {
                item {
                    SectionCard(title = "Duplicate Warnings") {
                        result?.duplicateIssues?.forEach { issue ->
                            Text("${issue.normalizedPhoneNumber} (${issue.displayNames.size} entries)")
                            issue.displayNames.forEach { entry ->
                                Text("- $entry")
                            }
                        }
                        Button(
                            onClick = { viewModel.selectDuplicatePolicy(DuplicatePolicy.DEDUPE) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (uiState.duplicatePolicy == DuplicatePolicy.DEDUPE) {
                                    "Selected: Deduplicate"
                                } else {
                                    "Deduplicate Numbers"
                                },
                            )
                        }
                        Button(
                            onClick = { viewModel.selectDuplicatePolicy(DuplicatePolicy.ALLOW_DUPLICATES) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (uiState.duplicatePolicy == DuplicatePolicy.ALLOW_DUPLICATES) {
                                    "Selected: Keep Duplicates"
                                } else {
                                    "Keep Duplicates"
                                },
                            )
                        }
                    }
                }
            }

            if (!result?.invalidNumberIssues.isNullOrEmpty()) {
                item {
                    SectionCard(title = "Invalid Number Warnings") {
                        result?.invalidNumberIssues?.forEach { issue ->
                            Text("${issue.displayName}: ${issue.phoneNumber}")
                            Text(issue.reason)
                        }
                        Button(
                            onClick = { viewModel.selectInvalidNumberPolicy(InvalidNumberPolicy.EXCLUDE_INVALID) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (uiState.invalidNumberPolicy == InvalidNumberPolicy.EXCLUDE_INVALID) {
                                    "Selected: Exclude Invalid Numbers"
                                } else {
                                    "Exclude Invalid Numbers"
                                },
                            )
                        }
                        Button(
                            onClick = { viewModel.selectInvalidNumberPolicy(InvalidNumberPolicy.INCLUDE_INVALID) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (uiState.invalidNumberPolicy == InvalidNumberPolicy.INCLUDE_INVALID) {
                                    "Selected: Include Invalid Numbers"
                                } else {
                                    "Include Invalid Numbers"
                                },
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Launch Readiness") {
                    if (uiState.launchIssues.isEmpty()) {
                        Text("Preflight requirements are satisfied for this draft.")
                    } else {
                        uiState.launchIssues.forEach { issue ->
                            Text("- $issue")
                        }
                    }
                    Button(
                        onClick = viewModel::createBroadcast,
                        enabled = uiState.canLaunch,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Create Broadcast Snapshot")
                    }
                }
            }

            item {
                SectionCard(title = "Message Preview") {
                    if (result?.validEntries.isNullOrEmpty()) {
                        Text("Previews will appear here after blocking issues are resolved and required policies are chosen.")
                    }
                }
            }

            items(result?.validEntries.orEmpty(), key = { it.distributionListMemberId }) { preview ->
                SectionCard(title = preview.displayName) {
                    Text(preview.phoneNumber)
                    Text(preview.renderedMessage)
                }
            }
        }
    }
}
