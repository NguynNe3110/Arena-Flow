package com.igorwojda.showcase.feature.auth.data.datasource.api.service

import com.igorwojda.showcase.feature.auth.data.datasource.api.model.AuthResponseDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.OAuthRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.RefreshRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.SendOtpRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.SendOtpResponseDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.VerifyOtpRequestDto
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import retrofit2.http.Body
import retrofit2.http.POST

/** Retrofit API service for authentication endpoints (USER-001 §2.1-2.4). */
interface AuthRetrofitService {
    @POST("auth/oauth")
    suspend fun signInWithOAuth(
        @Body request: OAuthRequestDto,
    ): ApiResult<ApiEnvelope<AuthResponseDto>>

    @POST("auth/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequestDto,
    ): ApiResult<ApiEnvelope<SendOtpResponseDto>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequestDto,
    ): ApiResult<ApiEnvelope<AuthResponseDto>>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshRequestDto,
    ): ApiResult<ApiEnvelope<AuthResponseDto>>
}
