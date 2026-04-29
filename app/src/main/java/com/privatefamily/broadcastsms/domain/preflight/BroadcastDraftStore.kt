package com.privatefamily.broadcastsms.domain.preflight

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BroadcastDraftStore {
    private val draftState = MutableStateFlow(BroadcastDraft())

    val draft: StateFlow<BroadcastDraft> = draftState.asStateFlow()

    fun update(transform: (BroadcastDraft) -> BroadcastDraft) {
        draftState.value = transform(draftState.value)
    }
}
