package com.privatefamily.broadcastsms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.privatefamily.broadcastsms.data.contacts.DeviceContact
import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import com.privatefamily.broadcastsms.data.repository.ContactRepository
import com.privatefamily.broadcastsms.data.repository.DistributionListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ContactPickerUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val contacts: List<DeviceContact> = emptyList(),
    val recentlyAddedKey: String? = null,
    val acknowledgementMessage: String? = null,
) {
    val filteredContacts: List<DeviceContact>
        get() {
            if (query.isBlank()) return contacts
            val needle = query.trim().lowercase()
            return contacts.filter { contact ->
                contact.displayName.lowercase().contains(needle) ||
                    contact.phoneNumbers.any { it.number.contains(needle) }
            }
        }
}

class ContactPickerViewModel(
    private val listId: String,
    private val distributionListRepository: DistributionListRepository,
    private val contactRepository: ContactRepository,
) : ViewModel() {
    private val state = MutableStateFlow(ContactPickerUiState())

    val uiState: StateFlow<ContactPickerUiState> = state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ContactPickerUiState(),
    )

    fun updateQuery(query: String) {
        state.update { it.copy(query = query) }
    }

    fun loadContacts() {
        if (state.value.isLoading || state.value.contacts.isNotEmpty()) return
        viewModelScope.launch {
            state.update { it.copy(isLoading = true) }
            val contacts = contactRepository.loadContacts()
            state.update { it.copy(isLoading = false, contacts = contacts) }
        }
    }

    fun addContact(contact: DeviceContact, phoneIndex: Int) {
        val phone = contact.phoneNumbers.getOrNull(phoneIndex) ?: return
        val addedKey = contact.contactId + ":" + (phone.phoneNumberId ?: phoneIndex.toString())
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            distributionListRepository.addMember(
                DistributionListMemberEntity(
                    id = UUID.randomUUID().toString(),
                    distributionListId = listId,
                    deviceContactId = contact.contactId,
                    devicePhoneNumberId = phone.phoneNumberId,
                    displayName = contact.displayName,
                    selectedPhoneNumber = phone.number,
                    normalizedPhoneNumber = phone.normalizedNumber,
                    phoneTypeLabel = phone.typeLabel,
                    firstName = contact.firstName,
                    lastName = contact.lastName,
                    relationship = null,
                    nickname = null,
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                ),
            )
            state.update {
                it.copy(
                    recentlyAddedKey = addedKey,
                    acknowledgementMessage = "Added ${contact.displayName} - ${phone.number}",
                )
            }
        }
    }

    fun consumeAcknowledgement() {
        state.update { it.copy(acknowledgementMessage = null) }
    }
}

class ContactPickerViewModelFactory(
    private val listId: String,
    private val distributionListRepository: DistributionListRepository,
    private val contactRepository: ContactRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactPickerViewModel(
            listId = listId,
            distributionListRepository = distributionListRepository,
            contactRepository = contactRepository,
        ) as T
    }
}
