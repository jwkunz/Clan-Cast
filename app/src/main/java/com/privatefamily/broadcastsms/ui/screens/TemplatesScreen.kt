package com.privatefamily.broadcastsms.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.privatefamily.broadcastsms.data.model.MessageTemplateEntity
import com.privatefamily.broadcastsms.ui.viewmodel.TemplatesViewModel

private val supportedTokens = listOf(
    "{firstName}",
    "{lastName}",
    "{displayName}",
    "{phoneNumber}",
    "{relationship}",
    "{nickname}",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: TemplatesViewModel,
    onBack: () -> Unit,
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<MessageTemplateEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "Supported Tokens") {
                Text(supportedTokens.joinToString(separator = "\n"))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(templates, key = { it.id }) { template ->
                    SectionCard(title = template.name) {
                        Text(template.body)
                        TextButton(onClick = { editTarget = template }) { Text("Edit") }
                        TextButton(onClick = { viewModel.deleteTemplate(template) }) { Text("Delete") }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        TemplateEditorDialog(
            title = "Create Template",
            template = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, body ->
                viewModel.createTemplate(name, body)
                showCreateDialog = false
            },
        )
    }

    editTarget?.let { template ->
        TemplateEditorDialog(
            title = "Edit Template",
            template = template,
            onDismiss = { editTarget = null },
            onSave = { name, body ->
                viewModel.updateTemplate(template, name, body)
                editTarget = null
            },
        )
    }
}

@Composable
private fun TemplateEditorDialog(
    title: String,
    template: MessageTemplateEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var name by rememberSaveable(template?.id ?: "new-name") { mutableStateOf(template?.name.orEmpty()) }
    var body by rememberSaveable(template?.id ?: "new-body") { mutableStateOf(template?.body.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message body") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && body.isNotBlank()) {
                        onSave(name, body)
                    }
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
