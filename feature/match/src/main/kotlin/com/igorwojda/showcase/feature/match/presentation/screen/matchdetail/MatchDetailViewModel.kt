package com.igorwojda.showcase.feature.match.presentation.screen.matchdetail

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.domain.usecase.AcceptMatchRequestUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.CancelMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.ForfeitMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchDetailUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchRequestsUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.RejectMatchRequestUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.SendMatchRequestUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.UpdateCostAgreementUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.WithdrawMatchRequestUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** ViewModel managing S4 Match Detail, join requests, cost agreement, and lifecycle actions. */
@Suppress("LongParameterList", "TooManyFunctions")
internal class MatchDetailViewModel(
    private val acceptMatchRequestUseCase: AcceptMatchRequestUseCase,
    private val cancelMatchUseCase: CancelMatchUseCase,
    private val forfeitMatchUseCase: ForfeitMatchUseCase,
    private val getMatchDetailUseCase: GetMatchDetailUseCase,
    private val getMatchRequestsUseCase: GetMatchRequestsUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val rejectMatchRequestUseCase: RejectMatchRequestUseCase,
    private val sendMatchRequestUseCase: SendMatchRequestUseCase,
    private val updateCostAgreementUseCase: UpdateCostAgreementUseCase,
    private val withdrawMatchRequestUseCase: WithdrawMatchRequestUseCase,
) : BaseViewModel<MatchDetailUiState, MatchDetailAction>(MatchDetailUiState()) {
    init {
        getSessionStateUseCase()
            .onEach { session -> sendAction(MatchDetailAction.SessionUpdated(session.actingTeam)) }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: MatchDetailEvent) {
        when (event) {
            is MatchDetailEvent.Load -> loadDetail(event.matchId)
            MatchDetailEvent.Retry -> loadDetail(uiStateFlow.value.matchId)
            is MatchDetailEvent.SendRequest -> sendRequest(event.message)
            is MatchDetailEvent.AcceptRequest -> acceptRequest(event.requestId)
            is MatchDetailEvent.RejectRequest -> rejectRequest(event.requestId, event.reason)
            is MatchDetailEvent.WithdrawRequest -> withdrawRequest(event.requestId, event.expectedStatus, event.reason)
            is MatchDetailEvent.CancelMatch -> cancelMatch(event.expectedStatus, event.reason)
            is MatchDetailEvent.ForfeitMatch -> forfeitMatch(event.reason)
            is MatchDetailEvent.SaveCostAgreement -> saveCostAgreement(event.costAgreement)
            MatchDetailEvent.ConsumeActionMessage -> sendAction(MatchDetailAction.ActionFeedback(null))
        }
    }

    private fun loadDetail(matchId: String) {
        if (matchId.isBlank()) return
        sendAction(MatchDetailAction.Loading(matchId))
        viewModelScope.launch {
            when (val detailResult = getMatchDetailUseCase(matchId)) {
                is Result.Success -> {
                    val detail = detailResult.value
                    val requests =
                        if (detail.type == MatchType.HAS_VENUE) {
                            when (val reqResult = getMatchRequestsUseCase(matchId = matchId, status = "ALL")) {
                                is Result.Success -> reqResult.value
                                is Result.Failure -> emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    sendAction(MatchDetailAction.Loaded(detail = detail, requests = requests))
                }

                is Result.Failure -> {
                    sendAction(MatchDetailAction.LoadError(detailResult.error))
                }
            }
        }
    }

    private fun sendRequest(message: String?) {
        val teamId = uiStateFlow.value.actingTeam?.id ?: return showNoTeamError()
        val matchId = uiStateFlow.value.matchId
        executeAction {
            when (val res = sendMatchRequestUseCase(matchId = matchId, teamId = teamId, message = message)) {
                is Result.Success -> {
                    sendAction(MatchDetailAction.ActionFeedback("Đã gửi yêu cầu ghép kèo thành công!"))
                    loadDetail(matchId)
                }

                is Result.Failure -> {
                    sendAction(MatchDetailAction.ActionFeedback(res.error.userMessage()))
                }
            }
        }
    }

    private fun acceptRequest(requestId: String) {
        val matchId = uiStateFlow.value.matchId
        executeAction {
            when (val res = acceptMatchRequestUseCase(requestId)) {
                is Result.Success -> {
                    sendAction(MatchDetailAction.ActionFeedback("Đã chấp nhận đối thủ ${res.value.opponentTeamName}!"))
                    loadDetail(matchId)
                }

                is Result.Failure -> {
                    sendAction(MatchDetailAction.ActionFeedback(res.error.userMessage()))
                }
            }
        }
    }

    private fun rejectRequest(
        requestId: String,
        reason: String?,
    ) {
        val matchId = uiStateFlow.value.matchId
        executeAction {
            when (val res = rejectMatchRequestUseCase(requestId = requestId, reason = reason)) {
                is Result.Success -> {
                    sendAction(MatchDetailAction.ActionFeedback("Đã từ chối yêu cầu."))
                    loadDetail(matchId)
                }

                is Result.Failure -> {
                    sendAction(MatchDetailAction.ActionFeedback(res.error.userMessage()))
                }
            }
        }
    }

    private fun withdrawRequest(
        requestId: String,
        expectedStatus: String,
        reason: String?,
    ) {
        val matchId = uiStateFlow.value.matchId
        executeAction {
            when (val res = withdrawMatchRequestUseCase(requestId = requestId, expectedStatus = expectedStatus, reason = reason)) {
                is Result.Success -> {
                    val penaltyText = if (res.value.reliabilityPenalty < 0) " (trừ ${-res.value.reliabilityPenalty} điểm uy tín)" else ""
                    sendAction(MatchDetailAction.ActionFeedback("Đã rút yêu cầu$penaltyText."))
                    loadDetail(matchId)
                }

                is Result.Failure -> {
                    sendAction(MatchDetailAction.ActionFeedback(res.error.userMessage()))
                }
            }
        }
    }

    private fun cancelMatch(
        expectedStatus: String,
        reason: String?,
    ) {
        val teamId = uiStateFlow.value.actingTeam?.id ?: return showNoTeamError()
        val matchId = uiStateFlow.value.matchId
        executeAction {
            when (val res = cancelMatchUseCase(matchId = matchId, teamId = teamId, expectedStatus = expectedStatus, reason = reason)) {
                is Result.Success -> {
                    val penaltyText = if (res.value.reliabilityPenalty < 0) " (trừ ${-res.value.reliabilityPenalty} điểm uy tín)" else ""
                    sendAction(MatchDetailAction.ActionFeedback("Đã hủy kèo$penaltyText."))
                    loadDetail(matchId)
                }

                is Result.Failure -> {
                    sendAction(MatchDetailAction.ActionFeedback(res.error.userMessage()))
                }
            }
        }
    }

    private fun forfeitMatch(reason: String?) {
        val teamId = uiStateFlow.value.actingTeam?.id ?: return showNoTeamError()
        val matchId = uiStateFlow.value.matchId
        executeAction {
            when (val res = forfeitMatchUseCase(matchId = matchId, teamId = teamId, reason = reason)) {
                is Result.Success -> {
                    sendAction(MatchDetailAction.ActionFeedback("Đã báo bỏ cuộc trận đấu."))
                    loadDetail(matchId)
                }

                is Result.Failure -> {
                    sendAction(MatchDetailAction.ActionFeedback(res.error.userMessage()))
                }
            }
        }
    }

    private fun saveCostAgreement(costAgreement: com.igorwojda.showcase.feature.match.domain.model.MatchCostAgreement) {
        val matchId = uiStateFlow.value.matchId
        executeAction {
            when (val res = updateCostAgreementUseCase(matchId = matchId, costAgreement = costAgreement)) {
                is Result.Success -> {
                    sendAction(MatchDetailAction.ActionFeedback("Đã cập nhật thỏa thuận chi phí."))
                    loadDetail(matchId)
                }

                is Result.Failure -> {
                    sendAction(MatchDetailAction.ActionFeedback(res.error.userMessage()))
                }
            }
        }
    }

    private fun showNoTeamError() {
        sendAction(MatchDetailAction.ActionFeedback("Bạn cần chọn đội đang thao tác để thực hiện."))
    }

    private fun executeAction(block: suspend () -> Unit) {
        sendAction(MatchDetailAction.Submitting(true))
        viewModelScope.launch { block() }
    }
}
