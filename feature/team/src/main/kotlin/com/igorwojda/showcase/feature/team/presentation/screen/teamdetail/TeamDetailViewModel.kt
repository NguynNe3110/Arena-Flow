@file:Suppress("LongParameterList", "MagicNumber", "TooManyFunctions")

package com.igorwojda.showcase.feature.team.presentation.screen.teamdetail

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.team.domain.model.CaptainHistoryEntry
import com.igorwojda.showcase.feature.team.domain.model.InvitationCandidate
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.domain.model.TeamRole
import com.igorwojda.showcase.feature.team.domain.usecase.DisbandTeamUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetCaptainHistoryUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamDetailUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamMembersUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.InviteTeamMemberUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.LeaveTeamUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.SearchInvitationCandidatesUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.TransferCaptainUseCase
import kotlinx.coroutines.launch

enum class TeamDetailTab(
    val label: String,
) {
    OVERVIEW("Tổng quan"),
    MEMBERS("Thành viên"),
    HISTORY("Lịch sử đội trưởng"),
}

enum class TeamDetailSheet {
    INVITE_MEMBER,
    TRANSFER_CAPTAIN,
    LEAVE_TEAM,
    DISBAND_TEAM,
}

data class TeamDetailUiState(
    val isLoading: Boolean = true,
    val team: TeamDetail? = null,
    val members: List<TeamMember> = emptyList(),
    val captainHistory: List<CaptainHistoryEntry> = emptyList(),
    val currentUserId: String? = null,
    val selectedTab: TeamDetailTab = TeamDetailTab.OVERVIEW,
    val activeSheet: TeamDetailSheet? = null,
    val isSubmittingAction: Boolean = false,
    val candidateQuery: String = "",
    val candidates: List<InvitationCandidate> = emptyList(),
    val candidatesHasMore: Boolean = false,
    val isSearchingCandidates: Boolean = false,
    val candidateError: String? = null,
    val actionMessage: String? = null,
    val errorMessage: String? = null,
) : BaseState {
    val isCaptain: Boolean
        get() = currentUserId != null && team?.captainId == currentUserId

    val isMember: Boolean
        get() = currentUserId != null && members.any { it.userId == currentUserId }

    val transferableMembers: List<TeamMember>
        get() = members.filter { it.userId != team?.captainId && it.role != TeamRole.CAPTAIN }
}

sealed interface TeamDetailAction : BaseAction<TeamDetailUiState> {
    data object Loading : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState = state.copy(isLoading = true, errorMessage = null)
    }

    data class Loaded(
        val team: TeamDetail,
        val members: List<TeamMember>,
        val captainHistory: List<CaptainHistoryEntry>,
        val currentUserId: String?,
    ) : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState =
            state.copy(
                isLoading = false,
                team = team,
                members = members,
                captainHistory = captainHistory,
                currentUserId = currentUserId,
                errorMessage = null,
            )
    }

    data class Error(
        val message: String,
    ) : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState = state.copy(isLoading = false, errorMessage = message)
    }

    data class SelectTab(
        val tab: TeamDetailTab,
    ) : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState = state.copy(selectedTab = tab)
    }

    data class OpenSheet(
        val sheet: TeamDetailSheet?,
    ) : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState =
            state.copy(
                activeSheet = sheet,
                candidateQuery = if (sheet == TeamDetailSheet.INVITE_MEMBER) "" else state.candidateQuery,
                candidates = if (sheet == TeamDetailSheet.INVITE_MEMBER) emptyList() else state.candidates,
                candidateError = null,
            )
    }

    data class CandidateSearchLoading(
        val query: String,
    ) : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState =
            state.copy(candidateQuery = query, isSearchingCandidates = true, candidateError = null)
    }

    data class CandidateSearchLoaded(
        val items: List<InvitationCandidate>,
        val hasMore: Boolean,
    ) : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState =
            state.copy(
                isSearchingCandidates = false,
                candidates = items,
                candidatesHasMore = hasMore,
                candidateError = null,
            )
    }

    data class CandidateSearchError(
        val message: String,
    ) : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState =
            state.copy(isSearchingCandidates = false, candidates = emptyList(), candidateError = message)
    }

    data class Submitting(
        val isSubmitting: Boolean,
    ) : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState = state.copy(isSubmittingAction = isSubmitting)
    }

    data class ActionFeedback(
        val message: String?,
        val closeSheet: Boolean = false,
    ) : TeamDetailAction {
        override fun reduce(state: TeamDetailUiState): TeamDetailUiState =
            state.copy(
                isSubmittingAction = false,
                actionMessage = message,
                activeSheet = if (closeSheet) null else state.activeSheet,
            )
    }
}

internal class TeamDetailViewModel(
    private val disbandTeamUseCase: DisbandTeamUseCase,
    private val getCaptainHistoryUseCase: GetCaptainHistoryUseCase,
    private val getTeamDetailUseCase: GetTeamDetailUseCase,
    private val getTeamMembersUseCase: GetTeamMembersUseCase,
    private val inviteTeamMemberUseCase: InviteTeamMemberUseCase,
    private val leaveTeamUseCase: LeaveTeamUseCase,
    private val searchInvitationCandidatesUseCase: SearchInvitationCandidatesUseCase,
    private val sessionRepository: SessionRepository,
    private val transferCaptainUseCase: TransferCaptainUseCase,
) : BaseViewModel<TeamDetailUiState, TeamDetailAction>(TeamDetailUiState()) {
    private var currentTeamId: String = ""
    private var latestUserId: String? = null

    init {
        viewModelScope.launch {
            sessionRepository.observeSession().collect { session ->
                latestUserId = session.currentUser?.id
            }
        }
    }

    fun load(teamId: String) {
        currentTeamId = teamId
        viewModelScope.launch {
            loadInternal(teamId)
        }
    }

    fun refresh() {
        if (currentTeamId.isNotBlank()) {
            viewModelScope.launch { loadInternal(currentTeamId) }
        }
    }

    private suspend fun loadInternal(teamId: String) {
        sendAction(TeamDetailAction.Loading)
        when (val detailResult = getTeamDetailUseCase(teamId)) {
            is Result.Failure -> {
                sendAction(TeamDetailAction.Error(detailResult.error.userMessage()))
            }

            is Result.Success -> {
                val membersResult = getTeamMembersUseCase(teamId)
                val historyResult = getCaptainHistoryUseCase(teamId)
                sendAction(
                    TeamDetailAction.Loaded(
                        team = detailResult.value,
                        members = (membersResult as? Result.Success)?.value.orEmpty(),
                        captainHistory = (historyResult as? Result.Success)?.value.orEmpty(),
                        currentUserId = latestUserId,
                    ),
                )
            }
        }
    }

    fun selectTab(tab: TeamDetailTab) {
        sendAction(TeamDetailAction.SelectTab(tab))
    }

    fun openSheet(sheet: TeamDetailSheet?) {
        sendAction(TeamDetailAction.OpenSheet(sheet))
    }

    fun onCandidateQueryChange(query: String) {
        sendAction(TeamDetailAction.CandidateSearchLoading(query))
        if (query.trim().length < 2) {
            sendAction(TeamDetailAction.CandidateSearchLoaded(emptyList(), hasMore = false))
            return
        }
        viewModelScope.launch {
            when (val result = searchInvitationCandidatesUseCase(currentTeamId, query)) {
                is Result.Success -> {
                    sendAction(TeamDetailAction.CandidateSearchLoaded(result.value.items, result.value.hasMore))
                }

                is Result.Failure -> {
                    sendAction(TeamDetailAction.CandidateSearchError(result.error.userMessage()))
                }
            }
        }
    }

    fun inviteMember(userId: String) {
        viewModelScope.launch {
            sendAction(TeamDetailAction.Submitting(true))
            when (val result = inviteTeamMemberUseCase(currentTeamId, userId)) {
                is Result.Success -> {
                    sendAction(TeamDetailAction.ActionFeedback("Đã gửi lời mời tham gia đội!", closeSheet = true))
                    loadInternal(currentTeamId)
                }

                is Result.Failure -> {
                    sendAction(TeamDetailAction.ActionFeedback(result.error.userMessage()))
                }
            }
        }
    }

    fun transferCaptain(
        newCaptainId: String,
        reason: String?,
    ) {
        viewModelScope.launch {
            sendAction(TeamDetailAction.Submitting(true))
            when (val result = transferCaptainUseCase(currentTeamId, newCaptainId, reason)) {
                is Result.Success -> {
                    sendAction(TeamDetailAction.ActionFeedback("Đã chuyển quyền đội trưởng thành công!", closeSheet = true))
                    loadInternal(currentTeamId)
                }

                is Result.Failure -> {
                    sendAction(TeamDetailAction.ActionFeedback(result.error.userMessage()))
                }
            }
        }
    }

    fun leaveTeam(onLeftTeam: () -> Unit) {
        viewModelScope.launch {
            sendAction(TeamDetailAction.Submitting(true))
            when (val result = leaveTeamUseCase(currentTeamId)) {
                is Result.Success -> {
                    sendAction(TeamDetailAction.ActionFeedback("Bạn đã rời khỏi đội.", closeSheet = true))
                    onLeftTeam()
                }

                is Result.Failure -> {
                    sendAction(TeamDetailAction.ActionFeedback(result.error.userMessage()))
                }
            }
        }
    }

    fun disbandTeam(reason: String?) {
        viewModelScope.launch {
            sendAction(TeamDetailAction.Submitting(true))
            when (val result = disbandTeamUseCase(currentTeamId, reason)) {
                is Result.Success -> {
                    sendAction(TeamDetailAction.ActionFeedback("Đội bóng đã được giải tán.", closeSheet = true))
                    loadInternal(currentTeamId)
                }

                is Result.Failure -> {
                    sendAction(TeamDetailAction.ActionFeedback(result.error.userMessage()))
                }
            }
        }
    }

    fun clearActionMessage() {
        sendAction(TeamDetailAction.ActionFeedback(null))
    }
}
