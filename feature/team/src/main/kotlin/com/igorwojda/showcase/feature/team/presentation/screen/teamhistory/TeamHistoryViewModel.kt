package com.igorwojda.showcase.feature.team.presentation.screen.teamhistory

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.team.domain.model.CaptainHistoryEntry
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.domain.usecase.GetCaptainHistoryUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamDetailUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamMembersUseCase
import kotlinx.coroutines.launch

enum class TeamHistoryTab(
    val label: String,
) {
    CAPTAIN("Lịch sử đội trưởng"),
    MEMBERS("Lịch sử thành viên"),
}

data class TeamHistoryUiState(
    val isLoading: Boolean = true,
    val team: TeamDetail? = null,
    val captainHistory: List<CaptainHistoryEntry> = emptyList(),
    val members: List<TeamMember> = emptyList(),
    val selectedTab: TeamHistoryTab = TeamHistoryTab.CAPTAIN,
    val errorMessage: String? = null,
) : BaseState

sealed interface TeamHistoryAction : BaseAction<TeamHistoryUiState> {
    data object Loading : TeamHistoryAction {
        override fun reduce(state: TeamHistoryUiState): TeamHistoryUiState = state.copy(isLoading = true, errorMessage = null)
    }

    data class Loaded(
        val team: TeamDetail,
        val captainHistory: List<CaptainHistoryEntry>,
        val members: List<TeamMember>,
    ) : TeamHistoryAction {
        override fun reduce(state: TeamHistoryUiState): TeamHistoryUiState =
            state.copy(
                isLoading = false,
                team = team,
                captainHistory = captainHistory,
                members = members,
                errorMessage = null,
            )
    }

    data class Error(
        val message: String,
    ) : TeamHistoryAction {
        override fun reduce(state: TeamHistoryUiState): TeamHistoryUiState = state.copy(isLoading = false, errorMessage = message)
    }

    data class SelectTab(
        val tab: TeamHistoryTab,
    ) : TeamHistoryAction {
        override fun reduce(state: TeamHistoryUiState): TeamHistoryUiState = state.copy(selectedTab = tab)
    }
}

internal class TeamHistoryViewModel(
    private val getCaptainHistoryUseCase: GetCaptainHistoryUseCase,
    private val getTeamDetailUseCase: GetTeamDetailUseCase,
    private val getTeamMembersUseCase: GetTeamMembersUseCase,
) : BaseViewModel<TeamHistoryUiState, TeamHistoryAction>(TeamHistoryUiState()) {
    private var currentTeamId: String = ""

    fun load(teamId: String) {
        currentTeamId = teamId
        viewModelScope.launch {
            sendAction(TeamHistoryAction.Loading)
            when (val detailResult = getTeamDetailUseCase(teamId)) {
                is Result.Failure -> {
                    sendAction(TeamHistoryAction.Error(detailResult.error.userMessage()))
                }

                is Result.Success -> {
                    val historyResult = getCaptainHistoryUseCase(teamId)
                    val membersResult = getTeamMembersUseCase(teamId)
                    sendAction(
                        TeamHistoryAction.Loaded(
                            team = detailResult.value,
                            captainHistory = (historyResult as? Result.Success)?.value.orEmpty(),
                            members = (membersResult as? Result.Success)?.value.orEmpty(),
                        ),
                    )
                }
            }
        }
    }

    fun refresh() {
        if (currentTeamId.isNotBlank()) load(currentTeamId)
    }

    fun selectTab(tab: TeamHistoryTab) {
        sendAction(TeamHistoryAction.SelectTab(tab))
    }
}
