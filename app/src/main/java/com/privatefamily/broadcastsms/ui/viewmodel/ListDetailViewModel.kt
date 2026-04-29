package com.privatefamily.broadcastsms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.privatefamily.broadcastsms.data.model.DistributionListEntity
import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import com.privatefamily.broadcastsms.data.repository.DistributionListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ListDetailUiState(
    val list: DistributionListEntity? = null,
    val members: List<DistributionListMemberEntity> = emptyList(),
)

class ListDetailViewModel(
    listId: String,
    private val repository: DistributionListRepository,
) : ViewModel() {
    val uiState: StateFlow<ListDetailUiState> = combine(
        repository.observeList(listId),
        repository.observeMembers(listId),
    ) { list, members ->
        ListDetailUiState(list = list, members = members)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListDetailUiState(),
    )

    fun updateMember(member: DistributionListMemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun removeMember(memberId: String) {
        viewModelScope.launch {
            repository.removeMember(memberId)
        }
    }
}

class ListDetailViewModelFactory(
    private val listId: String,
    private val repository: DistributionListRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListDetailViewModel(listId, repository) as T
    }
}
