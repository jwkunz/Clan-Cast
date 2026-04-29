package com.privatefamily.broadcastsms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import com.privatefamily.broadcastsms.data.model.DuplicatePolicy
import com.privatefamily.broadcastsms.data.model.InvalidNumberPolicy
import com.privatefamily.broadcastsms.data.repository.BroadcastRepository
import com.privatefamily.broadcastsms.data.repository.DistributionListRepository
import com.privatefamily.broadcastsms.domain.preflight.BroadcastDraftStore
import com.privatefamily.broadcastsms.domain.preflight.BroadcastCreator
import com.privatefamily.broadcastsms.domain.preflight.PreflightRequest
import com.privatefamily.broadcastsms.domain.preflight.PreflightResult
import com.privatefamily.broadcastsms.domain.preflight.PreflightValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreflightUiState(
    val listId: String? = null,
    val messageBody: String = "",
    val duplicatePolicy: DuplicatePolicy? = null,
    val invalidNumberPolicy: InvalidNumberPolicy? = null,
    val result: PreflightResult? = null,
    val createdBroadcastId: String? = null,
) {
    val launchCap = 50

    val launchIssues: List<String>
        get() = buildList {
            val currentResult = result
            if (listId.isNullOrBlank()) {
                add("No distribution list selected.")
            }
            if (messageBody.isBlank()) {
                add("Message body is blank.")
            }
            if (currentResult != null) {
                if (currentResult.totalEntries > launchCap) {
                    add("Recipient cap exceeded: ${currentResult.totalEntries} / $launchCap")
                }
                if (currentResult.duplicateIssues.isNotEmpty() && duplicatePolicy == null) {
                    add("Choose a duplicate handling policy.")
                }
                if (currentResult.invalidNumberIssues.isNotEmpty() && invalidNumberPolicy == null) {
                    add("Choose an invalid-number handling policy.")
                }
            }
        }

    val canLaunch: Boolean
        get() = result?.canLaunch == true && launchIssues.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class PreflightViewModel(
    private val draftStore: BroadcastDraftStore,
    private val distributionListRepository: DistributionListRepository,
    private val broadcastCreator: BroadcastCreator,
    private val validator: PreflightValidator = PreflightValidator(),
) : ViewModel() {
    private val policies = MutableStateFlow(PreflightPolicies())

    private val membersFlow = draftStore.draft.flatMapLatest { draft ->
        draft.distributionListId?.let(distributionListRepository::observeMembers)
            ?: flowOf<List<DistributionListMemberEntity>>(emptyList())
    }

    val uiState: StateFlow<PreflightUiState> = combine(
        draftStore.draft,
        policies,
        membersFlow,
    ) { draft, selectedPolicies, members ->
        val request = draft.distributionListId?.let {
            PreflightRequest(
                distributionListId = it,
                messageBody = draft.messageBody,
                duplicatePolicy = selectedPolicies.duplicatePolicy,
                invalidNumberPolicy = selectedPolicies.invalidNumberPolicy,
            )
        }
        val result = request?.let { validator.validate(it, members) }
        PreflightUiState(
            listId = draft.distributionListId,
            messageBody = draft.messageBody,
            duplicatePolicy = selectedPolicies.duplicatePolicy,
            invalidNumberPolicy = selectedPolicies.invalidNumberPolicy,
            result = result,
            createdBroadcastId = selectedPolicies.createdBroadcastId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PreflightUiState(),
    )

    fun selectDuplicatePolicy(policy: DuplicatePolicy) {
        policies.update { it.copy(duplicatePolicy = policy) }
    }

    fun selectInvalidNumberPolicy(policy: InvalidNumberPolicy) {
        policies.update { it.copy(invalidNumberPolicy = policy) }
    }

    fun createBroadcast() {
        val currentState = uiState.value
        val currentResult = currentState.result ?: return
        val duplicatePolicy = currentState.duplicatePolicy ?: return
        val invalidNumberPolicy = currentState.invalidNumberPolicy ?: return
        if (!currentState.canLaunch) return

        viewModelScope.launch {
            val broadcastId = broadcastCreator.create(
                draft = draftStore.draft.value,
                preflightResult = currentResult,
                duplicatePolicyName = duplicatePolicy.name,
                invalidNumberPolicyName = invalidNumberPolicy.name,
            )
            policies.update { it.copy(createdBroadcastId = broadcastId) }
        }
    }

    fun consumeCreatedBroadcast() {
        policies.update { it.copy(createdBroadcastId = null) }
    }
}

private data class PreflightPolicies(
    val duplicatePolicy: DuplicatePolicy? = null,
    val invalidNumberPolicy: InvalidNumberPolicy? = null,
    val createdBroadcastId: String? = null,
)

class PreflightViewModelFactory(
    private val draftStore: BroadcastDraftStore,
    private val distributionListRepository: DistributionListRepository,
    private val broadcastRepository: BroadcastRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PreflightViewModel(
            draftStore = draftStore,
            distributionListRepository = distributionListRepository,
            broadcastCreator = BroadcastCreator(broadcastRepository),
        ) as T
    }
}
