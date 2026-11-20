package com.uzuu.match.presentation.screen.teamdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.usecase.GetMyTeamUseCase
import com.uzuu.match.domain.usecase.InviteTeamMemberUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class TeamDetailUiState(
    val isLoading: Boolean = true,
    val team: TeamDetail? = null,
    val error: AppError? = null,
    val selectedTab: Int = 0, // 0: Lịch kèo, 1: Thành viên, 2: Lịch sử đấu
    val isInviteSheetVisible: Boolean = false,
    val inviteName: String = "",
    val invitePosition: String = "Tiền vệ",
    val isInviting: Boolean = false,
    val inviteSuccessMessage: String? = null,
)

internal class TeamDetailViewModel(
    private val getMyTeamUseCase: GetMyTeamUseCase,
    private val inviteTeamMemberUseCase: InviteTeamMemberUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(TeamDetailUiState())
    val uiState = mutableState.asStateFlow()

    init {
        loadTeam()
    }

    fun loadTeam() {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            when (val result = getMyTeamUseCase()) {
                is Result.Success -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isLoading = false,
                            team = result.value,
                        )
                }

                is Result.Failure -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isLoading = false,
                            error = result.error,
                        )
                }
            }
        }
    }

    fun selectTab(index: Int) {
        mutableState.value = mutableState.value.copy(selectedTab = index)
    }

    fun showInviteSheet() {
        mutableState.value =
            mutableState.value.copy(
                isInviteSheetVisible = true,
                inviteName = "",
                invitePosition = "Tiền vệ",
                inviteSuccessMessage = null,
            )
    }

    fun dismissInviteSheet() {
        mutableState.value = mutableState.value.copy(isInviteSheetVisible = false)
    }

    fun updateInviteName(name: String) {
        mutableState.value = mutableState.value.copy(inviteName = name)
    }

    fun updateInvitePosition(position: String) {
        mutableState.value = mutableState.value.copy(invitePosition = position)
    }

    fun submitInvite() {
        val currentTeam = mutableState.value.team ?: return
        val name = mutableState.value.inviteName.trim()
        val position = mutableState.value.invitePosition
        if (name.isBlank()) return

        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isInviting = true)
            when (val result = inviteTeamMemberUseCase(currentTeam.id, name, position)) {
                is Result.Success -> {
                    val updatedMembers = currentTeam.members + result.value
                    mutableState.value =
                        mutableState.value.copy(
                            isInviting = false,
                            isInviteSheetVisible = false,
                            team = currentTeam.copy(members = updatedMembers),
                            inviteSuccessMessage = "Đã gửi lời mời tới ${result.value.name} thành công!",
                        )
                }

                is Result.Failure -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isInviting = false,
                            error = result.error,
                        )
                }
            }
        }
    }

    fun clearToast() {
        mutableState.value = mutableState.value.copy(inviteSuccessMessage = null)
    }
}
