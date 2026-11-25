package com.igorwojda.showcase.feature.auth.presentation.screen.otp

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.auth.domain.usecase.SendOtpUseCase
import com.igorwojda.showcase.feature.auth.domain.usecase.VerifyOtpUseCase
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** UI state for 6-digit OTP verification and countdown timer (AUTH-03/S2b, AUTH-04). */
data class OtpVerifyUiState(
    val phone: String = "",
    val otpCode: String = "",
    val remainingSeconds: Int = DEFAULT_EXPIRES_SECONDS,
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null,
    val isDeletedAccountError: Boolean = false,
) : BaseState {
    val canResend: Boolean get() = remainingSeconds <= 0 && !isResending && !isSubmitting

    private companion object {
        const val DEFAULT_EXPIRES_SECONDS = 300
    }
}

/** State transitions for [OtpVerifyViewModel]. */
sealed interface OtpVerifyAction : BaseAction<OtpVerifyUiState> {
    data class Initialize(
        val phone: String,
        val expiresInSeconds: Int,
    ) : OtpVerifyAction {
        override fun reduce(state: OtpVerifyUiState): OtpVerifyUiState =
            state.copy(
                phone = phone,
                otpCode = "",
                remainingSeconds = expiresInSeconds.coerceAtLeast(0),
                errorMessage = null,
                isDeletedAccountError = false,
            )
    }

    data class UpdateOtp(
        val code: String,
    ) : OtpVerifyAction {
        override fun reduce(state: OtpVerifyUiState): OtpVerifyUiState =
            state.copy(otpCode = code, errorMessage = null, isDeletedAccountError = false)
    }

    data object TickCountdown : OtpVerifyAction {
        override fun reduce(state: OtpVerifyUiState): OtpVerifyUiState =
            state.copy(remainingSeconds = (state.remainingSeconds - 1).coerceAtLeast(0))
    }

    data object SubmitStarted : OtpVerifyAction {
        override fun reduce(state: OtpVerifyUiState): OtpVerifyUiState =
            state.copy(isSubmitting = true, errorMessage = null, isDeletedAccountError = false)
    }

    data object SubmitSucceeded : OtpVerifyAction {
        override fun reduce(state: OtpVerifyUiState): OtpVerifyUiState = state.copy(isSubmitting = false, errorMessage = null)
    }

    data class SubmitFailed(
        val message: String,
        val isDeletedAccount: Boolean,
    ) : OtpVerifyAction {
        override fun reduce(state: OtpVerifyUiState): OtpVerifyUiState =
            state.copy(
                isSubmitting = false,
                errorMessage = message,
                isDeletedAccountError = isDeletedAccount,
            )
    }

    data object ResendStarted : OtpVerifyAction {
        override fun reduce(state: OtpVerifyUiState): OtpVerifyUiState = state.copy(isResending = true, errorMessage = null)
    }

    data class ResendSucceeded(
        val expiresInSeconds: Int,
    ) : OtpVerifyAction {
        override fun reduce(state: OtpVerifyUiState): OtpVerifyUiState =
            state.copy(
                isResending = false,
                remainingSeconds = expiresInSeconds.coerceAtLeast(0),
                otpCode = "",
                errorMessage = null,
            )
    }

    data class ResendFailed(
        val message: String,
    ) : OtpVerifyAction {
        override fun reduce(state: OtpVerifyUiState): OtpVerifyUiState = state.copy(isResending = false, errorMessage = message)
    }
}

/** ViewModel managing OTP input, countdown timer, resend, and verification routing. */
class OtpVerifyViewModel(
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
) : BaseViewModel<OtpVerifyUiState, OtpVerifyAction>(OtpVerifyUiState()) {
    private var countdownJob: Job? = null

    fun initialize(
        phone: String,
        expiresInSeconds: Int,
    ) {
        sendAction(OtpVerifyAction.Initialize(phone = phone, expiresInSeconds = expiresInSeconds))
        startCountdown(expiresInSeconds)
    }

    fun onOtpChanged(value: String) {
        val sanitized = value.filter { it.isDigit() }.take(OTP_LENGTH)
        sendAction(OtpVerifyAction.UpdateOtp(sanitized))
    }

    fun verifyOtp(onAuthSuccess: (isNewUser: Boolean) -> Unit) {
        val current = uiStateFlow.value
        if (current.isSubmitting) return
        if (current.otpCode.length < OTP_LENGTH) {
            sendAction(OtpVerifyAction.SubmitFailed(INVALID_OTP_MSG, isDeletedAccount = false))
            return
        }
        sendAction(OtpVerifyAction.SubmitStarted)
        viewModelScope.launch {
            when (val result = verifyOtpUseCase(current.phone, current.otpCode)) {
                is Result.Success -> {
                    sendAction(OtpVerifyAction.SubmitSucceeded)
                    onAuthSuccess(result.value.isNewUser)
                }

                is Result.Failure -> {
                    val isDeleted =
                        result.error is AppError.DomainError &&
                            (result.error as AppError.DomainError).code == AUTH_DELETED_CODE
                    sendAction(
                        OtpVerifyAction.SubmitFailed(
                            message = result.error.userMessage("Mã xác thực không đúng hoặc đã hết hạn."),
                            isDeletedAccount = isDeleted,
                        ),
                    )
                }
            }
        }
    }

    fun resendOtp() {
        val current = uiStateFlow.value
        if (!current.canResend || current.phone.isBlank()) return
        sendAction(OtpVerifyAction.ResendStarted)
        viewModelScope.launch {
            when (val result = sendOtpUseCase(current.phone)) {
                is Result.Success -> {
                    sendAction(OtpVerifyAction.ResendSucceeded(result.value.expiresInSeconds))
                    startCountdown(result.value.expiresInSeconds)
                }

                is Result.Failure -> {
                    sendAction(
                        OtpVerifyAction.ResendFailed(
                            result.error.userMessage("Không thể gửi lại mã OTP, vui lòng thử lại."),
                        ),
                    )
                }
            }
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        if (seconds <= 0) return
        countdownJob =
            viewModelScope.launch {
                repeat(seconds) {
                    delay(ONE_SECOND_MS)
                    sendAction(OtpVerifyAction.TickCountdown)
                }
            }
    }

    private companion object {
        const val OTP_LENGTH = 6
        const val ONE_SECOND_MS = 1000L
        const val AUTH_DELETED_CODE = "AUTH_003"
        const val INVALID_OTP_MSG = "Vui lòng nhập đủ 6 chữ số OTP"
    }
}
