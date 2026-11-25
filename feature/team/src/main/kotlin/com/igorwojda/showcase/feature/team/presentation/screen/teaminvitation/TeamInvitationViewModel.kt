package com.igorwojda.showcase.feature.team.presentation.screen.teaminvitation

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.team.domain.model.MyInvitation
import com.igorwojda.showcase.feature.team.domain.usecase.AcceptInvitationUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetMyInvitationsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.RejectInvitationUseCase
import kotlinx.coroutines.launch

data class TeamInvitationUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val invitations: List<MyInvitation> = emptyList(),
    val actionMessage: String? = null,
    val errorMessage: String? = null,
) : BaseState

sealed interface TeamInvitationAction : BaseAction<TeamInvitationUiState> {
    data object Loading : TeamInvitationAction {
        override fun reduce(state: TeamInvitationUiState): TeamInvitationUiState = state.copy(isLoading = true, errorMessage = null)
    }

    data class Loaded(
        val invitations: List<MyInvitation>,
    ) : TeamInvitationAction {
        override fun reduce(state: TeamInvitationUiState): TeamInvitationUiState =
            state.copy(isLoading = false, isSubmitting = false, invitations = invitations, errorMessage = null)
    }

    data class Error(
        val message: String,
    ) : TeamInvitationAction {
        override fun reduce(state: TeamInvitationUiState): TeamInvitationUiState =
            state.copy(isLoading = false, isSubmitting = false, errorMessage = message)
    }

    data object Submitting : TeamInvitationAction {
        override fun reduce(state: TeamInvitationUiState): TeamInvitationUiState = state.copy(isSubmitting = true)
    }

    data class Feedback(
        val message: String?,
    ) : TeamInvitationAction {
        override fun reduce(state: TeamInvitationUiState): TeamInvitationUiState = state.copy(isSubmitting = false, actionMessage = message)
    }
}

internal class TeamInvitationViewModel(
    private val acceptInvitationUseCase: AcceptInvitationUseCase,
    private val getMyInvitationsUseCase: GetMyInvitationsUseCase,
    private val rejectInvitationUseCase: RejectInvitationUseCase,
) : BaseViewModel<TeamInvitationUiState, TeamInvitationAction>(TeamInvitationUiState()) {
    fun load(focusInvitationId: String? = null) {
        viewModelScope.launch {
            sendAction(TeamInvitationAction.Loading)
            when (val result = getMyInvitationsUseCase()) {
                is Result.Success -> {
                    val sorted =
                        if (focusInvitationId.isNullOrBlank()) {
                            result.value
                        } else {
                            result.value.sortedByDescending { it.invitationId == focusInvitationId }
                        }
                    sendAction(TeamInvitationAction.Loaded(sorted))
                }

                is Result.Failure -> {
                    sendAction(TeamInvitationAction.Error(result.error.userMessage()))
                }
            }
        }
    }

    fun accept(
        invitationId: String,
        onAccepted: (teamId: String) -> Unit,
    ) {
        viewModelScope.launch {
            sendAction(TeamInvitationAction.Submitting)
            when (val result = acceptInvitationUseCase(invitationId)) {
                is Result.Success -> {
                    sendAction(TeamInvitationAction.Feedback("Đã gia nhập đội thành công!"))
                    onAccepted(result.value.teamId)
                }

                is Result.Failure -> {
                    sendAction(TeamInvitationAction.Feedback(result.error.userMessage()))
                }
            }
        }
    }

    fun reject(invitationId: String) {
        viewModelScope.launch {
            sendAction(TeamInvitationAction.Submitting)
            when (val result = rejectInvitationUseCase(invitationId)) {
                is Result.Success -> {
                    sendAction(TeamInvitationAction.Feedback("Đã từ chối lời mời vào đội."))
                    load()
                }

                is Result.Failure -> {
                    sendAction(TeamInvitationAction.Feedback(result.error.userMessage()))
                }
            }
        }
    }
}
