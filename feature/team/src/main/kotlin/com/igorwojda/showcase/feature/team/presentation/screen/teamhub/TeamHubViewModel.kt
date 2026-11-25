package com.igorwojda.showcase.feature.team.presentation.screen.teamhub

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.team.domain.model.MyInvitation
import com.igorwojda.showcase.feature.team.domain.model.MyTeam
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.model.TeamSummary
import com.igorwojda.showcase.feature.team.domain.usecase.AcceptInvitationUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetMyInvitationsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetMyTeamsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetSystemTeamsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.RejectInvitationUseCase
import kotlinx.coroutines.launch

enum class TeamHubScope(
    val label: String,
) {
    MY("Đội của tôi"),
    SYSTEM("Đội hệ thống"),
}

data class TeamHubFilter(
    val scope: TeamHubScope = TeamHubScope.MY,
    val status: String = "ALL",
    val skillLabel: String = "ALL",
) {
    val activeCount: Int
        get() = listOf(status != "ALL", skillLabel != "ALL").count { it }
}

data class TeamHubUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true,
    val scope: TeamHubScope = TeamHubScope.MY,
    val query: String = "",
    val filter: TeamHubFilter = TeamHubFilter(),
    val myTeams: List<MyTeam> = emptyList(),
    val systemTeams: List<TeamSummary> = emptyList(),
    val pendingInvitations: List<MyInvitation> = emptyList(),
    val errorMessage: String? = null,
    val bannerMessage: String? = null,
) : BaseState {
    val filteredMyTeams: List<MyTeam>
        get() =
            myTeams.filter { team ->
                matchesQuery(team.teamName) &&
                    matchesStatus(team.status) &&
                    matchesSkill(team.skillLevels)
            }

    val filteredSystemTeams: List<TeamSummary>
        get() =
            systemTeams.filter { team ->
                matchesQuery(team.teamName) &&
                    matchesStatus(team.status) &&
                    matchesSkill(team.skillLevels)
            }

    private fun matchesQuery(name: String): Boolean = query.isBlank() || name.contains(query.trim(), ignoreCase = true)

    private fun matchesStatus(teamStatus: TeamStatus): Boolean = filter.status == "ALL" || teamStatus.name == filter.status

    private fun matchesSkill(skills: List<String>): Boolean =
        filter.skillLabel == "ALL" || skills.any { it.equals(filter.skillLabel, ignoreCase = true) }
}

sealed interface TeamHubAction : BaseAction<TeamHubUiState> {
    data object Loading : TeamHubAction {
        override fun reduce(state: TeamHubUiState): TeamHubUiState = state.copy(isLoading = true, errorMessage = null)
    }

    data class Loaded(
        val isLoggedIn: Boolean,
        val myTeams: List<MyTeam>,
        val systemTeams: List<TeamSummary>,
        val pendingInvitations: List<MyInvitation>,
    ) : TeamHubAction {
        override fun reduce(state: TeamHubUiState): TeamHubUiState =
            state.copy(
                isLoading = false,
                isLoggedIn = isLoggedIn,
                myTeams = myTeams,
                systemTeams = systemTeams,
                pendingInvitations = pendingInvitations,
                errorMessage = null,
            )
    }

    data class Error(
        val message: String,
    ) : TeamHubAction {
        override fun reduce(state: TeamHubUiState): TeamHubUiState = state.copy(isLoading = false, errorMessage = message)
    }

    data class SetScope(
        val scope: TeamHubScope,
    ) : TeamHubAction {
        override fun reduce(state: TeamHubUiState): TeamHubUiState = state.copy(scope = scope, filter = state.filter.copy(scope = scope))
    }

    data class SetQuery(
        val query: String,
    ) : TeamHubAction {
        override fun reduce(state: TeamHubUiState): TeamHubUiState = state.copy(query = query)
    }

    data class ApplyFilter(
        val filter: TeamHubFilter,
    ) : TeamHubAction {
        override fun reduce(state: TeamHubUiState): TeamHubUiState = state.copy(scope = filter.scope, filter = filter)
    }

    data class Banner(
        val message: String?,
    ) : TeamHubAction {
        override fun reduce(state: TeamHubUiState): TeamHubUiState = state.copy(bannerMessage = message)
    }
}

internal class TeamHubViewModel(
    private val acceptInvitationUseCase: AcceptInvitationUseCase,
    private val getMyInvitationsUseCase: GetMyInvitationsUseCase,
    private val getMyTeamsUseCase: GetMyTeamsUseCase,
    private val getSystemTeamsUseCase: GetSystemTeamsUseCase,
    private val rejectInvitationUseCase: RejectInvitationUseCase,
    private val sessionRepository: SessionRepository,
) : BaseViewModel<TeamHubUiState, TeamHubAction>(TeamHubUiState()) {
    init {
        observeSessionAndLoad()
    }

    private fun observeSessionAndLoad() {
        viewModelScope.launch {
            sessionRepository.observeSession().collect { session ->
                loadData(isLoggedIn = session.currentUser != null)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadData(isLoggedIn = uiStateFlow.value.isLoggedIn)
        }
    }

    private suspend fun loadData(isLoggedIn: Boolean) {
        sendAction(TeamHubAction.Loading)
        val systemResult = getSystemTeamsUseCase(status = "ALL")
        val myTeamsResult = if (isLoggedIn) getMyTeamsUseCase() else Result.Success(emptyList())
        val invitationsResult = if (isLoggedIn) getMyInvitationsUseCase() else Result.Success(emptyList())

        val error =
            (systemResult as? Result.Failure)?.error
                ?: (myTeamsResult as? Result.Failure)?.error
                ?: (invitationsResult as? Result.Failure)?.error

        if (error != null && systemResult is Result.Failure) {
            sendAction(TeamHubAction.Error(error.userMessage()))
            return
        }

        sendAction(
            TeamHubAction.Loaded(
                isLoggedIn = isLoggedIn,
                myTeams = (myTeamsResult as? Result.Success)?.value.orEmpty(),
                systemTeams = (systemResult as? Result.Success)?.value.orEmpty(),
                pendingInvitations = (invitationsResult as? Result.Success)?.value.orEmpty(),
            ),
        )
    }

    fun selectScope(scope: TeamHubScope) {
        sendAction(TeamHubAction.SetScope(scope))
    }

    fun onQueryChange(query: String) {
        sendAction(TeamHubAction.SetQuery(query))
    }

    fun applyFilter(filter: TeamHubFilter) {
        sendAction(TeamHubAction.ApplyFilter(filter))
    }

    fun resetFilter() {
        sendAction(TeamHubAction.ApplyFilter(TeamHubFilter(scope = uiStateFlow.value.scope)))
    }

    fun acceptInvitation(
        invitationId: String,
        onAccepted: (teamId: String) -> Unit,
    ) {
        viewModelScope.launch {
            when (val result = acceptInvitationUseCase(invitationId)) {
                is Result.Success -> {
                    sendAction(TeamHubAction.Banner("Đã gia nhập đội thành công!"))
                    loadData(isLoggedIn = uiStateFlow.value.isLoggedIn)
                    onAccepted(result.value.teamId)
                }

                is Result.Failure -> {
                    sendAction(TeamHubAction.Banner(result.error.userMessage()))
                }
            }
        }
    }

    fun rejectInvitation(invitationId: String) {
        viewModelScope.launch {
            when (val result = rejectInvitationUseCase(invitationId)) {
                is Result.Success -> {
                    sendAction(TeamHubAction.Banner("Đã từ chối lời mời vào đội."))
                    loadData(isLoggedIn = uiStateFlow.value.isLoggedIn)
                }

                is Result.Failure -> {
                    sendAction(TeamHubAction.Banner(result.error.userMessage()))
                }
            }
        }
    }

    fun dismissBanner() {
        sendAction(TeamHubAction.Banner(null))
    }
}
