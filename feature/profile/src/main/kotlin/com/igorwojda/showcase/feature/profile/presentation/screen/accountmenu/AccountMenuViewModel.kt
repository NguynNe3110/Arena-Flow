package com.igorwojda.showcase.feature.profile.presentation.screen.accountmenu

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.profile.domain.model.CaptainedTeam
import com.igorwojda.showcase.feature.profile.domain.usecase.DeleteAccountUseCase
import com.igorwojda.showcase.feature.profile.domain.usecase.GetCaptainedTeamsForDeleteUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class AccountMenuUiState(
    val displayName: String = "",
    val phone: String = "",
    val role: AccountRole = AccountRole.USER,
    val showLogoutSheet: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showBlockedCaptainSheet: Boolean = false,
    val blockingTeams: List<CaptainedTeam> = emptyList(),
    val isDeleting: Boolean = false,
    val isRecheckingTeams: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val errorMessage: String? = null,
) : BaseState

sealed interface AccountMenuAction : BaseAction<AccountMenuUiState> {
    data class SessionUpdated(
        val displayName: String,
        val phone: String,
        val role: AccountRole,
    ) : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState =
            state.copy(displayName = displayName, phone = phone, role = role)
    }

    data class SetLogoutSheet(
        val visible: Boolean,
    ) : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState = state.copy(showLogoutSheet = visible, errorMessage = null)
    }

    data class SetDeleteConfirmDialog(
        val visible: Boolean,
    ) : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState =
            state.copy(showDeleteConfirmDialog = visible, errorMessage = null)
    }

    data object DismissBlockedSheet : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState =
            state.copy(showBlockedCaptainSheet = false, errorMessage = null)
    }

    data object Deleting : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState = state.copy(isDeleting = true, errorMessage = null)
    }

    data object AccountDeleted : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState =
            state.copy(
                isDeleting = false,
                showDeleteConfirmDialog = false,
                showBlockedCaptainSheet = false,
                isAccountDeleted = true,
            )
    }

    data class BlockedByCaptaincy(
        val teams: List<CaptainedTeam>,
    ) : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState =
            state.copy(
                isDeleting = false,
                isRecheckingTeams = false,
                showDeleteConfirmDialog = false,
                showBlockedCaptainSheet = true,
                blockingTeams = teams,
            )
    }

    data object RecheckingTeams : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState = state.copy(isRecheckingTeams = true, errorMessage = null)
    }

    data object LoggedOut : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState = state.copy(showLogoutSheet = false, isLoggedOut = true)
    }

    data class Failed(
        val message: String,
    ) : AccountMenuAction {
        override fun reduce(state: AccountMenuUiState): AccountMenuUiState =
            state.copy(isDeleting = false, isRecheckingTeams = false, errorMessage = message)
    }
}

class AccountMenuViewModel(
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getCaptainedTeamsForDeleteUseCase: GetCaptainedTeamsForDeleteUseCase,
    private val sessionRepository: SessionRepository,
) : BaseViewModel<AccountMenuUiState, AccountMenuAction>(AccountMenuUiState()) {
    init {
        sessionRepository
            .observeSession()
            .onEach { session ->
                val user = session.currentUser
                if (user != null) {
                    sendAction(
                        AccountMenuAction.SessionUpdated(
                            displayName = user.displayName.ifBlank { user.phone },
                            phone = user.phone,
                            role = user.role,
                        ),
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun openLogoutSheet() = sendAction(AccountMenuAction.SetLogoutSheet(true))

    fun dismissLogoutSheet() = sendAction(AccountMenuAction.SetLogoutSheet(false))

    fun confirmLogout() {
        viewModelScope.launch {
            sessionRepository.logout()
            sendAction(AccountMenuAction.LoggedOut)
        }
    }

    fun openDeleteConfirmation() = sendAction(AccountMenuAction.SetDeleteConfirmDialog(true))

    fun dismissDeleteConfirmation() = sendAction(AccountMenuAction.SetDeleteConfirmDialog(false))

    fun dismissBlockedSheet() = sendAction(AccountMenuAction.DismissBlockedSheet)

    fun confirmDeleteAccount() {
        viewModelScope.launch {
            sendAction(AccountMenuAction.Deleting)
            when (val result = deleteAccountUseCase()) {
                is Result.Success -> {
                    sessionRepository.logout()
                    sendAction(AccountMenuAction.AccountDeleted)
                }

                is Result.Failure -> {
                    val domainError = result.error as? AppError.DomainError
                    if (domainError != null &&
                        (domainError.code == BLOCKED_ERROR_CODE || domainError.httpStatus == HTTP_UNPROCESSABLE)
                    ) {
                        val teams =
                            (getCaptainedTeamsForDeleteUseCase() as? Result.Success)?.value.orEmpty()
                        sendAction(AccountMenuAction.BlockedByCaptaincy(teams))
                    } else {
                        sendAction(
                            AccountMenuAction.Failed(
                                result.error.userMessage("Không thể xóa tài khoản, vui lòng thử lại."),
                            ),
                        )
                    }
                }
            }
        }
    }

    fun recheckBlockingTeams() {
        viewModelScope.launch {
            sendAction(AccountMenuAction.RecheckingTeams)
            when (val result = getCaptainedTeamsForDeleteUseCase()) {
                is Result.Success -> {
                    sendAction(AccountMenuAction.BlockedByCaptaincy(result.value))
                }

                is Result.Failure -> {
                    sendAction(
                        AccountMenuAction.Failed(
                            result.error.userMessage("Không thể kiểm tra danh sách đội."),
                        ),
                    )
                }
            }
        }
    }

    private companion object {
        const val BLOCKED_ERROR_CODE = "USER_003"
        const val HTTP_UNPROCESSABLE = 422
    }
}
