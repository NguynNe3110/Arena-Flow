package com.igorwojda.showcase.feature.auth.domain.usecase

import com.igorwojda.showcase.feature.auth.domain.model.AuthSession
import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Verifies a 6-digit OTP code and establishes an authenticated session. */
class VerifyOtpUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        phone: String,
        otp: String,
    ): Result<AuthSession> = authRepository.verifyOtp(phone, otp)
}
