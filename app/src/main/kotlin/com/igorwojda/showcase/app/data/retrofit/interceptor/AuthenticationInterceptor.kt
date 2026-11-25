package com.igorwojda.showcase.app.data.retrofit.interceptor

import com.igorwojda.showcase.feature.base.domain.repository.AuthTokenStore
import okhttp3.Interceptor
import okhttp3.Response

/** Attaches `Authorization: Bearer <accessToken>` per USER-001 §1.1 & api-convention.md §9. */
class AuthenticationInterceptor(
    private val authTokenStore: AuthTokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = authTokenStore.current().accessToken
        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }
        val authorizedRequest =
            original
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        return chain.proceed(authorizedRequest)
    }
}
