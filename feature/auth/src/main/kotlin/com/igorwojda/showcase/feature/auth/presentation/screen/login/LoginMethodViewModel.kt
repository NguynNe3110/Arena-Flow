package com.igorwojda.showcase.feature.auth.presentation.screen.login

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.auth.domain.model.OAuthProvider
import com.igorwojda.showcase.feature.auth.domain.usecase.SignInWithOAuthUseCase
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/** UI state for the login method selection screen (AUTH-01/S1, AUTH-05, AUTH-06). */
data class LoginMethodUiState(
    val loadingProvider: OAuthProvider? = null,
    val errorMessage: String? = null,
    val isDeletedAccountError: Boolean = false,
    val isSessionExpired: Boolean = false,
) : BaseState

/** State transitions for [LoginMethodViewModel]. */
sealed interface LoginMethodAction : BaseAction<LoginMethodUiState> {
    data class StartOAuth(
        val provider: OAuthProvider,
    ) : LoginMethodAction {
        override fun reduce(state: LoginMethodUiState): LoginMethodUiState =
            state.copy(
                loadingProvider = provider,
                errorMessage = null,
                isDeletedAccountError = false,
            )
    }

    data object OAuthSuccess : LoginMethodAction {
        override fun reduce(state: LoginMethodUiState): LoginMethodUiState =
            state.copy(loadingProvider = null, errorMessage = null, isDeletedAccountError = false)
    }

    data class OAuthFailure(
        val message: String,
        val isDeletedAccount: Boolean,
    ) : LoginMethodAction {
        override fun reduce(state: LoginMethodUiState): LoginMethodUiState =
            state.copy(
                loadingProvider = null,
                errorMessage = message,
                isDeletedAccountError = isDeletedAccount,
            )
    }

    data class SetSessionExpired(
        val expired: Boolean,
    ) : LoginMethodAction {
        override fun reduce(state: LoginMethodUiState): LoginMethodUiState = state.copy(isSessionExpired = expired)
    }

    data object ClearError : LoginMethodAction {
        override fun reduce(state: LoginMethodUiState): LoginMethodUiState = state.copy(errorMessage = null, isDeletedAccountError = false)
    }
}

/** ViewModel handling Google/Facebook OAuth login and AUTH-05/AUTH-06 states. */
class LoginMethodViewModel(
    private val signInWithOAuthUseCase: SignInWithOAuthUseCase,
) : BaseViewModel<LoginMethodUiState, LoginMethodAction>(LoginMethodUiState()) {
    fun setSessionExpired(expired: Boolean) {
        sendAction(LoginMethodAction.SetSessionExpired(expired))
    }

    fun clearError() {
        sendAction(LoginMethodAction.ClearError)
    }

    fun signInWithOAuth(
        provider: OAuthProvider,
        oauthToken: String,
        email: String,
        onAuthSuccess: (isNewUser: Boolean) -> Unit,
    ) {
        if (uiStateFlow.value.loadingProvider != null) return
        sendAction(LoginMethodAction.StartOAuth(provider))
        viewModelScope.launch {
            when (val result = signInWithOAuthUseCase(provider, oauthToken, email)) {
                is Result.Success -> {
                    sendAction(LoginMethodAction.OAuthSuccess)
                    onAuthSuccess(result.value.isNewUser)
                }

                is Result.Failure -> {
                    val isDeleted =
                        result.error is AppError.DomainError &&
                            (result.error as AppError.DomainError).code == AUTH_DELETED_CODE
                    sendAction(
                        LoginMethodAction.OAuthFailure(
                            message = result.error.userMessage("Đăng nhập thất bại, vui lòng thử lại."),
                            isDeletedAccount = isDeleted,
                        ),
                    )
                }
            }
        }
    }

    private companion object {
        const val AUTH_DELETED_CODE = "AUTH_003"
    }
}
