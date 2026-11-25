package com.igorwojda.showcase.feature.auth.domain.model

import com.igorwojda.showcase.feature.base.domain.model.AccountRole

/** Supported OAuth identity providers. */
enum class OAuthProvider {
    GOOGLE,
    FACEBOOK,
}

/** Authenticated session tokens and account metadata returned by auth endpoints. */
data class AuthSession(
    val userId: String,
    val role: AccountRole,
    val isNewUser: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Int,
)

/** Result of requesting an OTP code for a phone number. */
data class SendOtpResult(
    val phone: String,
    val expiresInSeconds: Int,
)
