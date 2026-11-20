package com.uzuu.match.presentation.screen.matchcreate

import com.uzuu.match.domain.model.MatchDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class MatchDraftHolder {
    private val _draftFlow = MutableStateFlow(MatchDraft())
    val draftFlow = _draftFlow.asStateFlow()

    fun updateDraft(draft: MatchDraft) {
        _draftFlow.value = draft
    }

    fun reset() {
        _draftFlow.value = MatchDraft()
    }
}
