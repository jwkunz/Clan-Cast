package com.privatefamily.broadcastsms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity
import com.privatefamily.broadcastsms.data.model.RecipientStatus
import com.privatefamily.broadcastsms.data.repository.BroadcastRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: BroadcastRepository,
) : ViewModel() {
    val broadcasts: StateFlow<List<BroadcastEntity>> = repository.observeBroadcasts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun deleteBroadcast(broadcastId: String) {
        viewModelScope.launch {
            repository.deleteBroadcast(broadcastId)
        }
    }

    fun deleteAllBroadcasts() {
        viewModelScope.launch {
            repository.deleteAllBroadcasts()
        }
    }
}

data class BroadcastDetailUiState(
    val broadcast: BroadcastEntity? = null,
    val recipients: List<BroadcastRecipientEntity> = emptyList(),
) {
    val sentCount: Int
        get() = recipients.count { it.status == RecipientStatus.SENT }

    val failedCount: Int
        get() = recipients.count { it.status == RecipientStatus.FAILED }

    val cancelledCount: Int
        get() = recipients.count { it.status == RecipientStatus.CANCELLED }

    val pendingCount: Int
        get() = recipients.count { it.status == RecipientStatus.PENDING }

    val processedCount: Int
        get() = sentCount + failedCount + cancelledCount

    val completionSummary: String
        get() = when (broadcast?.status) {
            com.privatefamily.broadcastsms.data.model.BroadcastStatus.COMPLETED ->
                "Completed: $sentCount delivered to the SMS app, $failedCount failed."
            com.privatefamily.broadcastsms.data.model.BroadcastStatus.CANCELLED ->
                "Cancelled: $processedCount already processed, $cancelledCount remaining recipient(s) skipped."
            com.privatefamily.broadcastsms.data.model.BroadcastStatus.FAILED ->
                "Broadcast stopped unexpectedly after $processedCount processed recipient(s)."
            else -> ""
        }
}

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
