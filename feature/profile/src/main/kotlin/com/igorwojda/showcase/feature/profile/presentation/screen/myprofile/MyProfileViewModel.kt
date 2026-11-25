package com.igorwojda.showcase.feature.profile.presentation.screen.myprofile

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.ActingTeamOption
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import com.igorwojda.showcase.feature.profile.domain.usecase.GetMyProfileUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class MyProfileUiState(
    val isLoading: Boolean = true,
    val isGuest: Boolean = false,
    val profile: UserProfile? = null,
    val reputationScore: Int = 100,
    val matchCount: Int = 0,
    val myTeams: List<ActingTeamOption> = emptyList(),
    val errorMessage: String? = null,
    val errorCode: String? = null,
) : BaseState

sealed interface MyProfileAction : BaseAction<MyProfileUiState> {
    data object Loading : MyProfileAction {
        override fun reduce(state: MyProfileUiState): MyProfileUiState = state.copy(isLoading = true, errorMessage = null, errorCode = null)
    }

    data object Guest : MyProfileAction {
        override fun reduce(state: MyProfileUiState): MyProfileUiState =
            state.copy(
                isLoading = false,
                isGuest = true,
                profile = null,
                myTeams = emptyList(),
                errorMessage = null,
                errorCode = null,
            )
    }

    data class SessionUpdated(
        val reputationScore: Int,
        val matchCount: Int,
        val myTeams: List<ActingTeamOption>,
        val isGuest: Boolean,
    ) : MyProfileAction {
        override fun reduce(state: MyProfileUiState): MyProfileUiState =
            state.copy(
                reputationScore = reputationScore,
                matchCount = matchCount,
                myTeams = myTeams,
                isGuest = isGuest,
            )
    }

    data class Loaded(
        val profile: UserProfile,
    ) : MyProfileAction {
        override fun reduce(state: MyProfileUiState): MyProfileUiState =
            state.copy(
                isLoading = false,
                isGuest = false,
                profile = profile,
                errorMessage = null,
                errorCode = null,
            )
    }

    data class Failed(
        val message: String,
        val code: String?,
    ) : MyProfileAction {
        override fun reduce(state: MyProfileUiState): MyProfileUiState =
            state.copy(
                isLoading = false,
                errorMessage = message,
                errorCode = code,
            )
    }
}

class MyProfileViewModel(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
) : BaseViewModel<MyProfileUiState, MyProfileAction>(MyProfileUiState()) {
    init {
        observeSession()
        loadProfile()
    }

    private fun observeSession() {
        getSessionStateUseCase()
            .onEach { session ->
                val user = session.currentUser
                sendAction(
                    MyProfileAction.SessionUpdated(
                        reputationScore = user?.reputationScore ?: 100,
                        matchCount = user?.matchCount ?: 0,
                        myTeams = session.captainedTeams,
                        isGuest = user == null,
                    ),
                )
            }.launchIn(viewModelScope)
    }

    fun loadProfile() {
        viewModelScope.launch {
            sendAction(MyProfileAction.Loading)
            when (val result = getMyProfileUseCase()) {
                is Result.Success -> {
                    sendAction(MyProfileAction.Loaded(result.value))
                }

                is Result.Failure -> {
                    val err = result.error
                    if (err is AppError.Unauthorized || (err is AppError.DomainError && err.code == "AUTH_001")) {
                        sendAction(MyProfileAction.Guest)
                    } else {
                        val code = (err as? AppError.DomainError)?.code
                        sendAction(
                            MyProfileAction.Failed(
                                message = err.userMessage("Không thể tải hồ sơ cá nhân."),
                                code = code,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun isAdmin(): Boolean = uiStateFlow.value.profile?.role == AccountRole.ADMIN
}
