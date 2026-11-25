package com.igorwojda.showcase.feature.auth.data.repository

import com.igorwojda.showcase.feature.auth.data.datasource.api.model.AuthResponseDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.OAuthRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.RefreshRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.SendOtpRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.model.VerifyOtpRequestDto
import com.igorwojda.showcase.feature.auth.data.datasource.api.service.AuthRetrofitService
import com.igorwojda.showcase.feature.auth.domain.model.AuthSession
import com.igorwojda.showcase.feature.auth.domain.model.OAuthProvider
import com.igorwojda.showcase.feature.auth.domain.model.SendOtpResult
import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository
import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.repository.AuthTokenStore
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Repository implementation backed by [AuthRetrofitService] and [AuthTokenStore]. */
internal class AuthRepositoryImpl(
    private val authRetrofitService: AuthRetrofitService,
    private val authTokenStore: AuthTokenStore,
) : AuthRepository {
    override suspend fun signInWithOAuth(
        provider: OAuthProvider,
        oauthToken: String,
        email: String,
    ): Result<AuthSession> =
        executeAuthCall {
            authRetrofitService.signInWithOAuth(
                OAuthRequestDto(
                    provider = provider.name,
                    oauthToken = oauthToken,
                    email = email,
                ),
            )
        }

    override suspend fun sendOtp(phone: String): Result<SendOtpResult> =
        dataResult {
            when (val response = authRetrofitService.sendOtp(SendOtpRequestDto(phone = phone))) {
                is ApiResult.Success -> {
                    Result.Success(
                        SendOtpResult(
                            phone = response.data.data.phone,
                            expiresInSeconds = response.data.data.expiresIn,
                        ),
                    )
                }

                is ApiResult.Error -> {
                    Result.Failure(apiErrorToAppError(response.code, response.errorBody))
                }

                is ApiResult.Exception -> {
                    Result.Failure(mapThrowable(response.throwable))
                }
            }
        }

    override suspend fun verifyOtp(
        phone: String,
        otp: String,
    ): Result<AuthSession> =
        executeAuthCall {
            authRetrofitService.verifyOtp(
                VerifyOtpRequestDto(
                    phone = phone,
                    otp = otp,
                ),
            )
        }

    override suspend fun refreshToken(refreshToken: String): Result<AuthSession> =
        executeAuthCall {
            authRetrofitService.refreshToken(
                RefreshRequestDto(refreshToken = refreshToken),
            )
        }

    override fun completeOnboarding() {
        authTokenStore.setHasSeenOnboarding(true)
    }

    private suspend fun executeAuthCall(call: suspend () -> ApiResult<ApiEnvelope<AuthResponseDto>>): Result<AuthSession> =
        dataResult {
            when (val response = call()) {
                is ApiResult.Success -> {
                    val session = response.data.data.toDomain()
                    authTokenStore.saveTokens(
                        userId = session.userId,
                        role = session.role,
                        accessToken = session.accessToken,
                        refreshToken = session.refreshToken,
                    )
                    Result.Success(session)
                }

                is ApiResult.Error -> {
                    Result.Failure(apiErrorToAppError(response.code, response.errorBody))
                }

                is ApiResult.Exception -> {
                    Result.Failure(mapThrowable(response.throwable))
                }
            }
        }

    private fun AuthResponseDto.toDomain(): AuthSession =
        AuthSession(
            userId = userId,
            role = AccountRole.entries.firstOrNull { it.name.equals(role, ignoreCase = true) } ?: AccountRole.USER,
            isNewUser = isNewUser,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresInSeconds = expiresIn,
        )

    private fun mapThrowable(throwable: Throwable): AppError = (throwable as? Exception)?.toAppError() ?: AppError.Unknown
}
