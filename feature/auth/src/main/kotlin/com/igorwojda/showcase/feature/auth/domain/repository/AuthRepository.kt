package com.igorwojda.showcase.feature.auth.domain.repository

import com.igorwojda.showcase.feature.auth.domain.model.AuthSession
import com.igorwojda.showcase.feature.auth.domain.model.OAuthProvider
import com.igorwojda.showcase.feature.auth.domain.model.SendOtpResult
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Repository contract for authentication and onboarding state. */
interface AuthRepository {
    suspend fun signInWithOAuth(
        provider: OAuthProvider,
        oauthToken: String,
        email: String,
    ): Result<AuthSession>

    suspend fun sendOtp(phone: String): Result<SendOtpResult>

    suspend fun verifyOtp(
        phone: String,
        otp: String,
    ): Result<AuthSession>

    suspend fun refreshToken(refreshToken: String): Result<AuthSession>

    fun completeOnboarding()
}
