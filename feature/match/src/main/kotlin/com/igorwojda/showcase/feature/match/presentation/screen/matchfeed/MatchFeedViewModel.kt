package com.igorwojda.showcase.feature.match.presentation.screen.matchfeed

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.domain.usecase.UpdateActingTeamUseCase
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.match.domain.model.MatchFilter
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchesUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMyMatchesUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.SendMatchRequestUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** ViewModel managing S3 Home Match Feed discovery, filters, and quick match requests. */
internal class MatchFeedViewModel(
    private val getMatchesUseCase: GetMatchesUseCase,
    private val getMyMatchesUseCase: GetMyMatchesUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val sendMatchRequestUseCase: SendMatchRequestUseCase,
    private val sessionRepository: SessionRepository,
    private val updateActingTeamUseCase: UpdateActingTeamUseCase,
) : BaseViewModel<MatchFeedUiState, MatchFeedAction>(MatchFeedUiState()) {
    init {
        observeSession()
        loadMatches(refreshing = false)
    }

    fun onEvent(event: MatchFeedEvent) {
        when (event) {
            MatchFeedEvent.Refresh -> {
                loadMatches(refreshing = true)
            }

            is MatchFeedEvent.SelectTab -> {
                sendAction(MatchFeedAction.TabChanged(event.tab))
                loadMatches(refreshing = false)
            }

            is MatchFeedEvent.QueryChanged -> {
                sendAction(MatchFeedAction.FilterUpdated(uiStateFlow.value.filter.copy(query = event.query)))
            }

            is MatchFeedEvent.ApplyFilter -> {
                sendAction(MatchFeedAction.FilterUpdated(event.filter))
                loadMatches(refreshing = false)
            }

            is MatchFeedEvent.SelectQuickType -> {
                val updated = uiStateFlow.value.filter.copy(type = event.type)
                sendAction(MatchFeedAction.FilterUpdated(updated))
                loadMatches(refreshing = false)
            }

            is MatchFeedEvent.SelectQuickFormat -> {
                val updated = uiStateFlow.value.filter.copy(format = event.format)
                sendAction(MatchFeedAction.FilterUpdated(updated))
                loadMatches(refreshing = false)
            }

            MatchFeedEvent.ResetFilter -> {
                sendAction(MatchFeedAction.FilterUpdated(MatchFilter()))
                loadMatches(refreshing = false)
            }

            is MatchFeedEvent.SelectActingTeam -> {
                viewModelScope.launch {
                    updateActingTeamUseCase(event.teamId)
                    loadMatches(refreshing = false)
                }
            }

            is MatchFeedEvent.QuickSendRequest -> {
                sendQuickRequest(event.matchId)
            }

            MatchFeedEvent.ConsumeSnackbar -> {
                sendAction(MatchFeedAction.Snackbar(null))
            }
        }
    }

    private fun observeSession() {
        getSessionStateUseCase()
            .onEach { session ->
                sendAction(
                    MatchFeedAction.SessionUpdated(
                        actingTeam = session.actingTeam,
                        captainedTeams = session.captainedTeams,
                        wards = sessionRepository.getWards(),
                        unreadNotifications = session.unreadNotifications,
                    ),
                )
            }.launchIn(viewModelScope)
    }

    private fun loadMatches(refreshing: Boolean) {
        val state = uiStateFlow.value
        sendAction(MatchFeedAction.Loading(refreshing = refreshing))
        viewModelScope.launch {
            if (state.activeTab == MatchFeedTab.DISCOVER) {
                when (val result = getMatchesUseCase(state.filter)) {
                    is Result.Success -> sendAction(MatchFeedAction.FeedLoaded(result.value.items))
                    is Result.Failure -> sendAction(MatchFeedAction.LoadError(result.error.userMessage()))
                }
            } else {
                when (val result = getMyMatchesUseCase(teamId = state.actingTeam?.id)) {
                    is Result.Success -> sendAction(MatchFeedAction.MyMatchesLoaded(result.value.items))
                    is Result.Failure -> sendAction(MatchFeedAction.LoadError(result.error.userMessage()))
                }
            }
        }
    }

    private fun sendQuickRequest(matchId: String) {
        val teamId = uiStateFlow.value.actingTeam?.id
        if (teamId.isNullOrBlank()) {
            sendAction(MatchFeedAction.Snackbar("Bạn cần làm đội trưởng một đội đang hoạt động để xin kèo."))
            return
        }
        viewModelScope.launch {
            when (val result = sendMatchRequestUseCase(matchId = matchId, teamId = teamId)) {
                is Result.Success -> sendAction(MatchFeedAction.Snackbar("Đã gửi yêu cầu ghép kèo thành công!"))
                is Result.Failure -> sendAction(MatchFeedAction.Snackbar(result.error.userMessage()))
            }
        }
    }
}
