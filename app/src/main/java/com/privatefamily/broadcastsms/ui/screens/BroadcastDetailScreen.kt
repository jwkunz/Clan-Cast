package com.privatefamily.broadcastsms.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privatefamily.broadcastsms.data.model.BroadcastStatus
import com.privatefamily.broadcastsms.permissions.AppPermission
import com.privatefamily.broadcastsms.permissions.PermissionHelper
import com.privatefamily.broadcastsms.service.BroadcastSendService
import com.privatefamily.broadcastsms.ui.viewmodel.BroadcastDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastDetailScreen(
    viewModel: BroadcastDetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val broadcast = uiState.broadcast
    val context = LocalContext.current
    val activity = context as? Activity
    val permissionState = activity?.let { PermissionHelper.state(it, AppPermission.SendSms) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && broadcast != null) {
            BroadcastSendService.start(context, broadcast.id)
        }
    }

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
                    Text("Processed: ${uiState.processedCount}")
                    Text("Pending: ${uiState.pendingCount}")
                    Text("Sent: ${uiState.sentCount}")
                    Text("Failed: ${uiState.failedCount}")
                    Text("Cancelled: ${uiState.cancelledCount}")
                    Text("Duplicate policy: ${broadcast?.duplicatePolicy ?: "Unknown"}")
                    Text("Invalid number policy: ${broadcast?.invalidNumberPolicy ?: "Unknown"}")
                    if (uiState.completionSummary.isNotBlank()) {
                        Text(
                            text = uiState.completionSummary,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    when (broadcast?.status) {
                        BroadcastStatus.PENDING -> {
                            Button(
                                onClick = {
                                    if (permissionState?.isGranted == true) {
                                        BroadcastSendService.start(context, broadcast.id)
                                    } else {
                                        permissionLauncher.launch(AppPermission.SendSms.manifestValue)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Start Sending")
                            }
                        }

                        BroadcastStatus.RUNNING -> {
                            Button(
                                onClick = { BroadcastSendService.pause(context, broadcast.id) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Pause Broadcast")
                            }
                            Button(
                                onClick = { BroadcastSendService.cancel(context, broadcast.id) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Cancel Broadcast")
                            }
                        }

                        BroadcastStatus.PAUSED -> {
                            Button(
                                onClick = {
                                    if (permissionState?.isGranted == true) {
                                        BroadcastSendService.resume(context, broadcast.id)
                                    } else {
                                        permissionLauncher.launch(AppPermission.SendSms.manifestValue)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Resume Broadcast")
                            }
                            Button(
                                onClick = { BroadcastSendService.cancel(context, broadcast.id) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Cancel Broadcast")
                            }
                        }

                        else -> Unit
                    }
                    if (permissionState?.isGranted == false && permissionState.requiresSettings) {
                        TextButton(onClick = { PermissionHelper.openAppSettings(context) }) {
                            Text("Open App Settings")
                        }
                    }
                }
            }
            items(uiState.recipients, key = { it.id }) { recipient ->
                SectionCard(title = recipient.displayName) {
                    Text(recipient.phoneNumber)
                    Text("Status: ${recipient.status}")
                    recipient.errorMessage?.let {
                        Text(
                            text = "Error: $it",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text(recipient.renderedMessage)
                }
            }
        }
    }
}
