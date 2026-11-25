package com.igorwojda.showcase.feature.auth.data.datasource.api.model

import kotlinx.serialization.Serializable

/** Request payload for `POST /api/v1/auth/oauth`. */
@Serializable
data class OAuthRequestDto(
    val provider: String,
    val oauthToken: String,
    val email: String,
)

/** Request payload for `POST /api/v1/auth/send-otp`. */
@Serializable
data class SendOtpRequestDto(
    val phone: String,
)

/** Response payload for `POST /api/v1/auth/send-otp`. */
@Serializable
data class SendOtpResponseDto(
    val phone: String,
    val expiresIn: Int,
)

/** Request payload for `POST /api/v1/auth/verify-otp`. */
@Serializable
data class VerifyOtpRequestDto(
    val phone: String,
    val otp: String,
)

/** Request payload for `POST /api/v1/auth/refresh`. */
@Serializable
data class RefreshRequestDto(
    val refreshToken: String,
)

/** Response payload for OAuth, OTP verification, and token refresh endpoints. */
@Serializable
data class AuthResponseDto(
    val userId: String,
    val role: String,
    val isNewUser: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
)
