package com.igorwojda.showcase.feature.auth.domain.usecase

import com.igorwojda.showcase.feature.auth.domain.model.SendOtpResult
import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Sends a 6-digit OTP verification code to the given phone number. */
class SendOtpUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phone: String): Result<SendOtpResult> = authRepository.sendOtp(phone)
}
