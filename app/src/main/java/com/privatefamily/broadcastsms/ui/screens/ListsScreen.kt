package com.privatefamily.broadcastsms.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.privatefamily.broadcastsms.data.model.DistributionListEntity
import com.privatefamily.broadcastsms.ui.viewmodel.ListsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    viewModel: ListsViewModel,
    onBack: () -> Unit,
    onListSelected: (String) -> Unit,
) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<DistributionListEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<DistributionListEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Distribution Lists") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Text("+")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (lists.isEmpty()) {
                item {
                    SectionCard(title = "No Lists Yet") {
                        Text("Create your first distribution list to start building broadcast recipients.")
                    }
                }
            }
            items(lists, key = { it.id }) { list ->
                SectionCard(
                    title = list.name,
                    modifier = Modifier.clickable { onListSelected(list.id) },
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { renameTarget = list }) { Text("Rename") }
                        TextButton(onClick = { deleteTarget = list }) { Text("Delete") }
                        TextButton(onClick = { onListSelected(list.id) }) { Text("Open") }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        NameEditorDialog(
            title = "Create List",
            initialValue = "",
            onDismiss = { showCreateDialog = false },
            onConfirm = {
                if (it.isNotBlank()) {
                    viewModel.createList(it)
                }
                showCreateDialog = false
            },
        )
    }

    renameTarget?.let { target ->
        NameEditorDialog(
            title = "Rename List",
            initialValue = target.name,
            onDismiss = { renameTarget = null },
            onConfirm = {
                if (it.isNotBlank()) {
                    viewModel.renameList(target.id, it)
                }
                renameTarget = null
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete List") },
            text = {
                Text("Delete ${target.name}? Broadcast history stays intact, but this list and its members will be removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteList(target.id)
                        deleteTarget = null
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun NameEditorDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by rememberSaveable(initialValue) { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
