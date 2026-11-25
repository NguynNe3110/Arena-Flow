@file:Suppress("ReturnCount", "StringLiteralDuplication")

package com.igorwojda.showcase.app.data.retrofit.interceptor

import com.igorwojda.showcase.app.BuildConfig
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.repository.AuthTokenStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

/**
 * Synchronized OkHttp Authenticator that refreshes the access token once via `POST /api/v1/auth/refresh`
 * when receiving 401 Unauthorized per USER-001 §2.4.
 */
class TokenRefreshAuthenticator(
    private val authTokenStore: AuthTokenStore,
) : Authenticator {
    private val json = Json { ignoreUnknownKeys = true }
    private val refreshClient = OkHttpClient()

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        if (responseCount(response) >= MAX_RETRIES) return null
        if (response.request.url.encodedPath
                .contains("/auth/")
        ) {
            return null
        }

        synchronized(this) {
            val currentSnapshot = authTokenStore.current()
            val requestAuth = response.request.header("Authorization")
            val latestToken = currentSnapshot.accessToken
            if (!latestToken.isNullOrBlank() && requestAuth != "Bearer $latestToken") {
                return response.request
                    .newBuilder()
                    .header("Authorization", "Bearer $latestToken")
                    .build()
            }

            val refreshToken = currentSnapshot.refreshToken ?: return null
            val refreshPayload = json.encodeToString(RefreshRequestDto.serializer(), RefreshRequestDto(refreshToken))
            val refreshUrl = "${BuildConfig.GRADLE_API_BASE_URL.trimEnd('/')}/auth/refresh"
            val refreshRequest =
                Request
                    .Builder()
                    .url(refreshUrl)
                    .post(refreshPayload.toRequestBody("application/json".toMediaType()))
                    .build()

            val refreshResponse = runCatching { refreshClient.newCall(refreshRequest).execute() }.getOrNull() ?: return null
            refreshResponse.use { resp ->
                if (!resp.isSuccessful) {
                    if (resp.code == HTTP_UNAUTHORIZED || resp.code == HTTP_FORBIDDEN) {
                        authTokenStore.clearSession()
                    }
                    return null
                }
                val bodyString = resp.body?.string() ?: return null
                val envelope = runCatching { json.decodeFromString<ApiEnvelope<RefreshResponseDto>>(bodyString) }.getOrNull() ?: return null
                val data = envelope.data
                val role = runCatching { AccountRole.valueOf(data.role) }.getOrDefault(AccountRole.USER)
                authTokenStore.saveTokens(
                    userId = data.userId,
                    role = role,
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken,
                )
                return response.request
                    .newBuilder()
                    .header("Authorization", "Bearer ${data.accessToken}")
                    .build()
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    @Serializable
    private data class RefreshRequestDto(
        val refreshToken: String,
    )

    @Serializable
    private data class RefreshResponseDto(
        val userId: String,
        val role: String,
        val accessToken: String,
        val refreshToken: String,
    )

    private companion object {
        const val MAX_RETRIES = 2
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
    }
}
