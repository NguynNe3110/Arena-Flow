package com.igorwojda.showcase.feature.auth.presentation.screen.phone

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.auth.domain.usecase.SendOtpUseCase
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/** UI state for phone number input and OTP request (AUTH-02/S2a). */
data class PhoneInputUiState(
    val phoneInput: String = "",
    val phoneError: String? = null,
    val isSubmitting: Boolean = false,
) : BaseState

/** State transitions for [PhoneInputViewModel]. */
sealed interface PhoneInputAction : BaseAction<PhoneInputUiState> {
    data class UpdatePhone(
        val value: String,
    ) : PhoneInputAction {
        override fun reduce(state: PhoneInputUiState): PhoneInputUiState = state.copy(phoneInput = value, phoneError = null)
    }

    data class ValidationError(
        val message: String,
    ) : PhoneInputAction {
        override fun reduce(state: PhoneInputUiState): PhoneInputUiState = state.copy(phoneError = message, isSubmitting = false)
    }

    data object SubmitStarted : PhoneInputAction {
        override fun reduce(state: PhoneInputUiState): PhoneInputUiState = state.copy(isSubmitting = true, phoneError = null)
    }

    data object SubmitSucceeded : PhoneInputAction {
        override fun reduce(state: PhoneInputUiState): PhoneInputUiState = state.copy(isSubmitting = false, phoneError = null)
    }
}

/** ViewModel normalizing Vietnamese phone numbers (`+84`/`0` -> `^0\d{9}$`) and requesting OTP. */
class PhoneInputViewModel(
    private val sendOtpUseCase: SendOtpUseCase,
) : BaseViewModel<PhoneInputUiState, PhoneInputAction>(PhoneInputUiState()) {
    fun onPhoneChanged(value: String) {
        sendAction(PhoneInputAction.UpdatePhone(value))
    }

    fun submitPhone(onOtpSent: (phone: String, expiresInSeconds: Int) -> Unit) {
        if (uiStateFlow.value.isSubmitting) return
        val normalized = normalizePhone(uiStateFlow.value.phoneInput)
        if (!PHONE_REGEX.matches(normalized)) {
            sendAction(PhoneInputAction.ValidationError(INVALID_PHONE_MSG))
            return
        }
        sendAction(PhoneInputAction.UpdatePhone(normalized))
        sendAction(PhoneInputAction.SubmitStarted)
        viewModelScope.launch {
            when (val result = sendOtpUseCase(normalized)) {
                is Result.Success -> {
                    sendAction(PhoneInputAction.SubmitSucceeded)
                    onOtpSent(result.value.phone, result.value.expiresInSeconds)
                }

                is Result.Failure -> {
                    val fieldMsg =
                        (result.error as? AppError.DomainError)?.fieldErrors?.get("phone")
                            ?: result.error.userMessage("Không thể gửi mã OTP, vui lòng thử lại.")
                    sendAction(PhoneInputAction.ValidationError(fieldMsg))
                }
            }
        }
    }

    internal fun normalizePhone(raw: String): String {
        val digitsOnly = raw.trim().replace(Regex("[\\s\\-().]"), "")
        return when {
            digitsOnly.startsWith("+84") -> "0" + digitsOnly.removePrefix("+84")
            digitsOnly.startsWith("84") && digitsOnly.length > MIN_84_LENGTH -> "0" + digitsOnly.removePrefix("84")
            else -> digitsOnly
        }
    }

    private companion object {
        const val MIN_84_LENGTH = 9
        const val INVALID_PHONE_MSG = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0"
        val PHONE_REGEX = Regex("^0\\d{9}$")
    }
}
