package com.igorwojda.showcase.feature.team.presentation.screen.teammanage

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamDetailUseCase
import kotlinx.coroutines.launch

data class TeamManageUiState(
    val isLoading: Boolean = true,
    val team: TeamDetail? = null,
    val errorMessage: String? = null,
) : BaseState

sealed interface TeamManageAction : BaseAction<TeamManageUiState> {
    data object Loading : TeamManageAction {
        override fun reduce(state: TeamManageUiState): TeamManageUiState = state.copy(isLoading = true, errorMessage = null)
    }

    data class Loaded(
        val team: TeamDetail,
    ) : TeamManageAction {
        override fun reduce(state: TeamManageUiState): TeamManageUiState = state.copy(isLoading = false, team = team, errorMessage = null)
    }

    data class Error(
        val message: String,
    ) : TeamManageAction {
        override fun reduce(state: TeamManageUiState): TeamManageUiState = state.copy(isLoading = false, errorMessage = message)
    }
}

internal class TeamManageViewModel(
    private val getTeamDetailUseCase: GetTeamDetailUseCase,
) : BaseViewModel<TeamManageUiState, TeamManageAction>(TeamManageUiState()) {
    private var currentTeamId: String = ""

    fun load(teamId: String) {
        currentTeamId = teamId
        viewModelScope.launch {
            sendAction(TeamManageAction.Loading)
            when (val result = getTeamDetailUseCase(teamId)) {
                is Result.Success -> sendAction(TeamManageAction.Loaded(result.value))
                is Result.Failure -> sendAction(TeamManageAction.Error(result.error.userMessage()))
            }
        }
    }

    fun refresh() {
        if (currentTeamId.isNotBlank()) load(currentTeamId)
    }
}
