package com.igorwojda.showcase.feature.reputation.presentation.screen.reputationhistory

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReputation

data class ReputationHistoryUiState(
    val isLoading: Boolean = false,
    val teamId: String = "",
    val reputation: TeamReputation? = null,
    val direction: ReportDirection = ReportDirection.RECEIVED,
    val reports: List<TeamReport> = emptyList(),
    val disputeTargetReportId: String? = null,
    val disputeEvidence: String = "",
    val isSubmittingDispute: Boolean = false,
    val disputeSuccessMessage: String? = null,
    val error: AppError? = null,
) : BaseState

sealed interface ReputationHistoryEvent {
    data class Load(
        val teamId: String,
    ) : ReputationHistoryEvent

    data class SelectDirection(
        val direction: ReportDirection,
    ) : ReputationHistoryEvent

    data class OpenDisputeSheet(
        val reportId: String,
    ) : ReputationHistoryEvent

    data object DismissDisputeSheet : ReputationHistoryEvent

    data class UpdateDisputeEvidence(
        val evidence: String,
    ) : ReputationHistoryEvent

    data object SubmitDispute : ReputationHistoryEvent
}

sealed interface ReputationHistoryAction : BaseAction<ReputationHistoryUiState> {
    data class Loading(
        val teamId: String,
        val direction: ReportDirection,
    ) : ReputationHistoryAction {
        override fun reduce(state: ReputationHistoryUiState): ReputationHistoryUiState =
            state.copy(
                isLoading = true,
                teamId = teamId,
                direction = direction,
                error = null,
                disputeSuccessMessage = null,
            )
    }

    data class Loaded(
        val reputation: TeamReputation,
        val reports: List<TeamReport>,
    ) : ReputationHistoryAction {
        override fun reduce(state: ReputationHistoryUiState): ReputationHistoryUiState =
            state.copy(
                isLoading = false,
                reputation = reputation,
                reports = reports,
                error = null,
            )
    }

    data class LoadError(
        val error: AppError,
    ) : ReputationHistoryAction {
        override fun reduce(state: ReputationHistoryUiState): ReputationHistoryUiState = state.copy(isLoading = false, error = error)
    }

    data class OpenDispute(
        val reportId: String?,
    ) : ReputationHistoryAction {
        override fun reduce(state: ReputationHistoryUiState): ReputationHistoryUiState =
            state.copy(disputeTargetReportId = reportId, disputeEvidence = "", error = null)
    }

    data class UpdateEvidence(
        val evidence: String,
    ) : ReputationHistoryAction {
        override fun reduce(state: ReputationHistoryUiState): ReputationHistoryUiState = state.copy(disputeEvidence = evidence)
    }

    data object SubmittingDispute : ReputationHistoryAction {
        override fun reduce(state: ReputationHistoryUiState): ReputationHistoryUiState =
            state.copy(isSubmittingDispute = true, error = null)
    }

    data class DisputeSubmitted(
        val message: String,
    ) : ReputationHistoryAction {
        override fun reduce(state: ReputationHistoryUiState): ReputationHistoryUiState =
            state.copy(
                isSubmittingDispute = false,
                disputeTargetReportId = null,
                disputeEvidence = "",
                disputeSuccessMessage = message,
            )
    }

    data class DisputeError(
        val error: AppError,
    ) : ReputationHistoryAction {
        override fun reduce(state: ReputationHistoryUiState): ReputationHistoryUiState =
            state.copy(isSubmittingDispute = false, error = error)
    }
}
