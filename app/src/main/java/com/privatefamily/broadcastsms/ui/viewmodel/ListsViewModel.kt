package com.privatefamily.broadcastsms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.privatefamily.broadcastsms.data.model.DistributionListEntity
import com.privatefamily.broadcastsms.data.repository.DistributionListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListsViewModel(
    private val repository: DistributionListRepository,
) : ViewModel() {
    val lists: StateFlow<List<DistributionListEntity>> = repository.observeLists().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun createList(name: String) {
        viewModelScope.launch {
            repository.createList(name)
        }
    }

    fun renameList(listId: String, name: String) {
        viewModelScope.launch {
            repository.renameList(listId, name)
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            repository.deleteList(listId)
        }
    }
}

class ListsViewModelFactory(
    private val repository: DistributionListRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListsViewModel(repository) as T
    }
}
