package com.privatefamily.broadcastsms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.privatefamily.broadcastsms.data.model.DistributionListEntity
import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import com.privatefamily.broadcastsms.data.model.MessageTemplateEntity
import com.privatefamily.broadcastsms.data.repository.DistributionListRepository
import com.privatefamily.broadcastsms.data.repository.TemplateRepository
import com.privatefamily.broadcastsms.domain.preflight.BroadcastDraft
import com.privatefamily.broadcastsms.domain.preflight.BroadcastDraftStore
import com.privatefamily.broadcastsms.domain.template.TemplateParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class ComposeBroadcastUiState(
    val draft: BroadcastDraft = BroadcastDraft(),
    val lists: List<DistributionListEntity> = emptyList(),
    val templates: List<MessageTemplateEntity> = emptyList(),
    val selectedListMemberCount: Int = 0,
    val selectedTemplate: MessageTemplateEntity? = null,
    val unsupportedTokens: Set<String> = emptySet(),
) {
    val validationIssues: List<String>
        get() = buildList {
            if (draft.distributionListId.isNullOrBlank()) {
                add("Choose a distribution list.")
            }
            if (draft.distributionListId != null && selectedListMemberCount == 0) {
                add("The selected distribution list is empty.")
            }
            if (draft.messageBody.isBlank()) {
                add("Enter a message body.")
            }
            if (unsupportedTokens.isNotEmpty()) {
                add("Unsupported tokens: ${unsupportedTokens.joinToString()}")
            }
        }

    val canContinueToPreflight: Boolean
        get() = validationIssues.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeBroadcastViewModel(
    private val distributionListRepository: DistributionListRepository,
    private val templateRepository: TemplateRepository,
    private val draftStore: BroadcastDraftStore,
    private val templateParser: TemplateParser = TemplateParser(),
) : ViewModel() {
    private val draft = draftStore.draft

    private val selectedMembers = draft.flatMapLatest { currentDraft ->
        currentDraft.distributionListId?.let(distributionListRepository::observeMembers)
            ?: flowOf<List<DistributionListMemberEntity>>(emptyList())
    }

    val uiState: StateFlow<ComposeBroadcastUiState> = combine(
        draft,
        distributionListRepository.observeLists(),
        templateRepository.observeTemplates(),
        selectedMembers,
    ) { currentDraft, lists, templates, members ->
        val parseResult = templateParser.parse(currentDraft.messageBody)
        ComposeBroadcastUiState(
            draft = currentDraft,
            lists = lists,
            templates = templates,
            selectedListMemberCount = members.size,
            selectedTemplate = templates.firstOrNull { it.id == currentDraft.templateId },
            unsupportedTokens = parseResult.unsupportedTokens,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ComposeBroadcastUiState(),
    )

    fun selectList(listId: String) {
        draftStore.update { it.copy(distributionListId = listId) }
    }

    fun selectTemplate(template: MessageTemplateEntity?) {
        draftStore.update {
            it.copy(
                templateId = template?.id,
                messageBody = template?.body ?: it.messageBody,
            )
        }
    }

    fun updateMessageBody(messageBody: String) {
        draftStore.update { it.copy(messageBody = messageBody) }
    }
}

class ComposeBroadcastViewModelFactory(
    private val distributionListRepository: DistributionListRepository,
    private val templateRepository: TemplateRepository,
    private val draftStore: BroadcastDraftStore,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ComposeBroadcastViewModel(
            distributionListRepository = distributionListRepository,
            templateRepository = templateRepository,
            draftStore = draftStore,
        ) as T
    }
}
