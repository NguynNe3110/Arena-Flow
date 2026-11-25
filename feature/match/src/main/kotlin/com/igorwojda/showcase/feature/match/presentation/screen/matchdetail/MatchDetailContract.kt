package com.igorwojda.showcase.feature.match.presentation.screen.matchdetail

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.ActingTeamOption
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.match.domain.model.MatchCostAgreement
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchRequest

/** UI state for S4 Match Detail screen (MATCH-03, 04, 09, 10, 11, 12, 13, 14). */
internal data class MatchDetailUiState(
    val matchId: String = "",
    val detail: MatchDetail? = null,
    val requests: List<MatchRequest> = emptyList(),
    val actingTeam: ActingTeamOption? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: AppError? = null,
    val actionMessage: String? = null,
) : BaseState {
    val isHostTeam: Boolean
        get() = detail != null && actingTeam != null && detail.authorTeam.teamId == actingTeam.id

    val isOpponentTeam: Boolean
        get() = detail != null && actingTeam != null && detail.opponentTeam?.teamId == actingTeam.id

    val myTeamRequest: MatchRequest?
        get() = actingTeam?.let { team -> requests.firstOrNull { it.requesterTeamId == team.id } }
}

/** User intents dispatched to MatchDetailViewModel. */
internal sealed interface MatchDetailEvent {
    data class Load(
        val matchId: String,
    ) : MatchDetailEvent

    data object Retry : MatchDetailEvent

    data class SendRequest(
        val message: String?,
    ) : MatchDetailEvent

    data class AcceptRequest(
        val requestId: String,
    ) : MatchDetailEvent

    data class RejectRequest(
        val requestId: String,
        val reason: String?,
    ) : MatchDetailEvent

    data class WithdrawRequest(
        val requestId: String,
        val expectedStatus: String,
        val reason: String?,
    ) : MatchDetailEvent

    data class CancelMatch(
        val expectedStatus: String,
        val reason: String?,
    ) : MatchDetailEvent

    data class ForfeitMatch(
        val reason: String?,
    ) : MatchDetailEvent

    data class SaveCostAgreement(
        val costAgreement: MatchCostAgreement,
    ) : MatchDetailEvent

    data object ConsumeActionMessage : MatchDetailEvent
}

/** State reducers for MatchDetailViewModel. */
internal sealed interface MatchDetailAction : BaseAction<MatchDetailUiState> {
    data class SessionUpdated(
        val actingTeam: ActingTeamOption?,
    ) : MatchDetailAction {
        override fun reduce(state: MatchDetailUiState): MatchDetailUiState = state.copy(actingTeam = actingTeam)
    }

    data class Loading(
        val matchId: String,
    ) : MatchDetailAction {
        override fun reduce(state: MatchDetailUiState): MatchDetailUiState =
            state.copy(matchId = matchId, isLoading = state.detail == null, error = null)
    }

    data class Loaded(
        val detail: MatchDetail,
        val requests: List<MatchRequest>,
    ) : MatchDetailAction {
        override fun reduce(state: MatchDetailUiState): MatchDetailUiState =
            state.copy(detail = detail, requests = requests, isLoading = false, isSubmitting = false, error = null)
    }

    data class LoadError(
        val error: AppError,
    ) : MatchDetailAction {
        override fun reduce(state: MatchDetailUiState): MatchDetailUiState =
            state.copy(isLoading = false, isSubmitting = false, error = error)
    }

    data class Submitting(
        val submitting: Boolean,
    ) : MatchDetailAction {
        override fun reduce(state: MatchDetailUiState): MatchDetailUiState = state.copy(isSubmitting = submitting)
    }

    data class ActionFeedback(
        val message: String?,
    ) : MatchDetailAction {
        override fun reduce(state: MatchDetailUiState): MatchDetailUiState = state.copy(isSubmitting = false, actionMessage = message)
    }
}
