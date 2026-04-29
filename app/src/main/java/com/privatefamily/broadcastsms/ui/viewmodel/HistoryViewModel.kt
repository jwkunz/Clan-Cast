package com.privatefamily.broadcastsms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity
import com.privatefamily.broadcastsms.data.repository.BroadcastRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    private val repository: BroadcastRepository,
) : ViewModel() {
    val broadcasts: StateFlow<List<BroadcastEntity>> = repository.observeBroadcasts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
}

data class BroadcastDetailUiState(
    val broadcast: BroadcastEntity? = null,
    val recipients: List<BroadcastRecipientEntity> = emptyList(),
)

class BroadcastDetailViewModel(
    broadcastId: String,
    private val repository: BroadcastRepository,
) : ViewModel() {
    val uiState: StateFlow<BroadcastDetailUiState> = combine(
        repository.observeBroadcast(broadcastId),
        repository.observeRecipients(broadcastId),
    ) { broadcast, recipients ->
        BroadcastDetailUiState(
            broadcast = broadcast,
            recipients = recipients,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BroadcastDetailUiState(),
    )
}

class HistoryViewModelFactory(
    private val repository: BroadcastRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(repository) as T
    }
}

class BroadcastDetailViewModelFactory(
    private val broadcastId: String,
    private val repository: BroadcastRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BroadcastDetailViewModel(broadcastId, repository) as T
    }
}
