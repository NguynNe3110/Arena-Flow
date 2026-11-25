package com.igorwojda.showcase.feature.reputation.presentation.screen.reputationhistory

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.usecase.DisputeReportUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.GetTeamReportsUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.GetTeamReputationUseCase
import kotlinx.coroutines.launch

/** ViewModel for Team Reputation Summary, Trust Tier & Report/Dispute History (REP-04, REP-07..10). */
class ReputationHistoryViewModel(
    private val disputeReportUseCase: DisputeReportUseCase,
    private val getTeamReportsUseCase: GetTeamReportsUseCase,
    private val getTeamReputationUseCase: GetTeamReputationUseCase,
) : BaseViewModel<ReputationHistoryUiState, ReputationHistoryAction>(ReputationHistoryUiState()) {
    fun onEvent(event: ReputationHistoryEvent) {
        when (event) {
            is ReputationHistoryEvent.Load -> {
                loadReputationAndReports(event.teamId, uiStateFlow.value.direction)
            }

            is ReputationHistoryEvent.SelectDirection -> {
                loadReputationAndReports(uiStateFlow.value.teamId, event.direction)
            }

            is ReputationHistoryEvent.OpenDisputeSheet -> {
                sendAction(ReputationHistoryAction.OpenDispute(event.reportId))
            }

            ReputationHistoryEvent.DismissDisputeSheet -> {
                sendAction(ReputationHistoryAction.OpenDispute(null))
            }

            is ReputationHistoryEvent.UpdateDisputeEvidence -> {
                sendAction(ReputationHistoryAction.UpdateEvidence(event.evidence.take(MAX_EVIDENCE_LENGTH)))
            }

            ReputationHistoryEvent.SubmitDispute -> {
                submitDispute()
            }
        }
    }

    private fun loadReputationAndReports(
        teamId: String,
        direction: ReportDirection,
    ) {
        if (teamId.isBlank()) return
        sendAction(ReputationHistoryAction.Loading(teamId = teamId, direction = direction))
        viewModelScope.launch {
            val reputationResult = getTeamReputationUseCase(teamId)
            val reportsResult = getTeamReportsUseCase(teamId = teamId, direction = direction)

            when {
                reputationResult is Result.Failure -> {
                    sendAction(ReputationHistoryAction.LoadError(reputationResult.error))
                }

                reportsResult is Result.Failure -> {
                    sendAction(ReputationHistoryAction.LoadError(reportsResult.error))
                }

                reputationResult is Result.Success && reportsResult is Result.Success -> {
                    sendAction(
                        ReputationHistoryAction.Loaded(
                            reputation = reputationResult.value,
                            reports = reportsResult.value.items,
                        ),
                    )
                }
            }
        }
    }

    private fun submitDispute() {
        val state = uiStateFlow.value
        val reportId = state.disputeTargetReportId ?: return
        val evidence = state.disputeEvidence.trim()
        if (state.isSubmittingDispute || evidence.isEmpty()) return

        sendAction(ReputationHistoryAction.SubmittingDispute)
        viewModelScope.launch {
            when (val result = disputeReportUseCase(reportId = reportId, evidence = evidence)) {
                is Result.Success -> {
                    sendAction(
                        ReputationHistoryAction.DisputeSubmitted(
                            message = "Đã gửi khiếu nại. Admin sẽ xem xét trong thời gian sớm nhất.",
                        ),
                    )
                    loadReputationAndReports(state.teamId, state.direction)
                }

                is Result.Failure -> {
                    sendAction(ReputationHistoryAction.DisputeError(result.error))
                }
            }
        }
    }

    private companion object {
        const val MAX_EVIDENCE_LENGTH = 1000
    }
}
