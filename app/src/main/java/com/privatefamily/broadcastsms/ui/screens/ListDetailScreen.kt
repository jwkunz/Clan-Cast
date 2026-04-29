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
import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import com.privatefamily.broadcastsms.ui.viewmodel.ListDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    viewModel: ListDetailViewModel,
    onBack: () -> Unit,
    onAddMember: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editTarget by remember { mutableStateOf<DistributionListMemberEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.list?.name ?: "List Detail") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMember) {
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
            items(uiState.members, key = { it.id }) { member ->
                SectionCard(title = member.displayName) {
                    Text(member.selectedPhoneNumber)
                    Text(member.phoneTypeLabel ?: "Unknown phone type")
                    Text("First: ${member.firstName}")
                    Text("Last: ${member.lastName}")
                    Text("Relationship: ${member.relationship.orEmpty()}")
                    Text("Nickname: ${member.nickname.orEmpty()}")
                    TextButton(onClick = { editTarget = member }) { Text("Edit Metadata") }
                    TextButton(onClick = { viewModel.removeMember(member.id) }) { Text("Remove") }
                }
            }
        }
    }

    editTarget?.let { target ->
        MemberEditorDialog(
            member = target,
            onDismiss = { editTarget = null },
            onSave = {
                viewModel.updateMember(it)
                editTarget = null
            },
        )
    }
}

@Composable
private fun MemberEditorDialog(
    member: DistributionListMemberEntity,
    onDismiss: () -> Unit,
    onSave: (DistributionListMemberEntity) -> Unit,
) {
    var firstName by rememberSaveable(member.id) { mutableStateOf(member.firstName) }
    var lastName by rememberSaveable(member.id + "-last") { mutableStateOf(member.lastName) }
    var relationship by rememberSaveable(member.id + "-rel") { mutableStateOf(member.relationship.orEmpty()) }
    var nickname by rememberSaveable(member.id + "-nick") { mutableStateOf(member.nickname.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Display name: ${member.displayName}")
                Text("Selected number: ${member.selectedPhoneNumber}")
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        member.copy(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            relationship = relationship.trim().ifBlank { null },
                            nickname = nickname.trim().ifBlank { null },
                        ),
                    )
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
