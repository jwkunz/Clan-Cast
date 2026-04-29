package com.privatefamily.broadcastsms.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privatefamily.broadcastsms.permissions.AppPermission
import com.privatefamily.broadcastsms.permissions.PermissionHelper
import com.privatefamily.broadcastsms.ui.viewmodel.ContactPickerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerScreen(
    viewModel: ContactPickerViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val permissionState = activity?.let { PermissionHelper.state(it, AppPermission.Contacts) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.loadContacts()
        }
    }

    LaunchedEffect(permissionState?.isGranted) {
        if (permissionState?.isGranted == true) {
            viewModel.loadContacts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Contact") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (permissionState?.isGranted != true) {
                SectionCard(title = "Contacts Permission Needed") {
                    Text("Clan Cast needs access to device contacts before you can build a distribution list.")
                    Button(onClick = {
                        launcher.launch(AppPermission.Contacts.manifestValue)
                    }) {
                        Text("Grant Contacts Permission")
                    }
                    if (permissionState?.requiresSettings == true) {
                        TextButton(onClick = { PermissionHelper.openAppSettings(context) }) {
                            Text("Open App Settings")
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search contacts or numbers") },
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.filteredContacts, key = { it.contactId }) { contact ->
                            SectionCard(title = contact.displayName) {
                                contact.phoneNumbers.forEachIndexed { index, phone ->
                                    Button(
                                        onClick = { viewModel.addContact(contact, index) },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("${phone.typeLabel ?: "Phone"}: ${phone.number}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
