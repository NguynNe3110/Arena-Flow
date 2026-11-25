package com.igorwojda.showcase.feature.match.presentation.screen.matchcreate

import com.igorwojda.showcase.feature.match.domain.model.MatchDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Shared in-memory holder bridging MatchCreateScreen and MatchReviewScreen. */
internal class MatchDraftHolder {
    private val mutableDraft = MutableStateFlow(MatchDraft())
    val draft: StateFlow<MatchDraft> = mutableDraft.asStateFlow()

    fun update(transform: (MatchDraft) -> MatchDraft) {
        mutableDraft.update(transform)
    }

    fun set(newDraft: MatchDraft) {
        mutableDraft.value = newDraft
    }

    fun reset() {
        mutableDraft.value = MatchDraft()
    }
}
