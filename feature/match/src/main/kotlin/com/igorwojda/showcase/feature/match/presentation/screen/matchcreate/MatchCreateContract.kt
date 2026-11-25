package com.igorwojda.showcase.feature.match.presentation.screen.matchcreate

import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft

internal const val MATCH_VENUE_PICKER_REQUEST_KEY = "match-create-venue"

/** UI state for S5 Match Create / Edit screen (MATCH-05). */
internal data class MatchCreateUiState(
    val draft: MatchDraft = MatchDraft(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val validationError: String? = null,
    val savedMatchId: String? = null,
) : BaseState

/** User intents dispatched to MatchCreateViewModel. */
internal sealed interface MatchCreateEvent {
    data class Init(
        val matchId: String?,
    ) : MatchCreateEvent

    data object ConsumePendingVenue : MatchCreateEvent

    data class UpdateDraft(
        val transform: (MatchDraft) -> MatchDraft,
    ) : MatchCreateEvent

    data object Submit : MatchCreateEvent

    data object ValidateForReview : MatchCreateEvent

    data object ConsumeValidationError : MatchCreateEvent
}

/** State reducers for MatchCreateViewModel. */
internal sealed interface MatchCreateAction : BaseAction<MatchCreateUiState> {
    data class DraftLoaded(
        val draft: MatchDraft,
        val isEditMode: Boolean,
    ) : MatchCreateAction {
        override fun reduce(state: MatchCreateUiState): MatchCreateUiState =
            state.copy(draft = draft, isEditMode = isEditMode, isLoading = false)
    }

    data class DraftUpdated(
        val draft: MatchDraft,
    ) : MatchCreateAction {
        override fun reduce(state: MatchCreateUiState): MatchCreateUiState = state.copy(draft = draft, validationError = null)
    }

    data class Submitting(
        val submitting: Boolean,
    ) : MatchCreateAction {
        override fun reduce(state: MatchCreateUiState): MatchCreateUiState = state.copy(isSubmitting = submitting, validationError = null)
    }

    data class ValidationError(
        val message: String?,
    ) : MatchCreateAction {
        override fun reduce(state: MatchCreateUiState): MatchCreateUiState = state.copy(isSubmitting = false, validationError = message)
    }

    data class Saved(
        val matchId: String,
    ) : MatchCreateAction {
        override fun reduce(state: MatchCreateUiState): MatchCreateUiState = state.copy(isSubmitting = false, savedMatchId = matchId)
    }
}
