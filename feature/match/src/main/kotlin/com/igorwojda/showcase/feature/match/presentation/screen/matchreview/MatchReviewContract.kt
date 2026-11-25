package com.igorwojda.showcase.feature.match.presentation.screen.matchreview

import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft

/** UI state for S6 Match Review screen (MATCH-06). */
internal data class MatchReviewUiState(
    val draft: MatchDraft = MatchDraft(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val postedMatchId: String? = null,
) : BaseState

/** User intents dispatched to MatchReviewViewModel. */
internal sealed interface MatchReviewEvent {
    data object ConfirmPost : MatchReviewEvent
}

/** State reducers for MatchReviewViewModel. */
internal sealed interface MatchReviewAction : BaseAction<MatchReviewUiState> {
    data class DraftChanged(
        val draft: MatchDraft,
    ) : MatchReviewAction {
        override fun reduce(state: MatchReviewUiState): MatchReviewUiState = state.copy(draft = draft)
    }

    data class Submitting(
        val submitting: Boolean,
    ) : MatchReviewAction {
        override fun reduce(state: MatchReviewUiState): MatchReviewUiState = state.copy(isSubmitting = submitting, errorMessage = null)
    }

    data class Error(
        val message: String,
    ) : MatchReviewAction {
        override fun reduce(state: MatchReviewUiState): MatchReviewUiState = state.copy(isSubmitting = false, errorMessage = message)
    }

    data class Posted(
        val matchId: String,
    ) : MatchReviewAction {
        override fun reduce(state: MatchReviewUiState): MatchReviewUiState = state.copy(isSubmitting = false, postedMatchId = matchId)
    }
}
