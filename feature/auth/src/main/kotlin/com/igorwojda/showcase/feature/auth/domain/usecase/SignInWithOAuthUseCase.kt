package com.igorwojda.showcase.feature.auth.domain.usecase

import com.igorwojda.showcase.feature.auth.domain.model.AuthSession
import com.igorwojda.showcase.feature.auth.domain.model.OAuthProvider
import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Authenticates or registers a user via Google or Facebook OAuth. */
class SignInWithOAuthUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        provider: OAuthProvider,
        oauthToken: String,
        email: String,
    ): Result<AuthSession> = authRepository.signInWithOAuth(provider, oauthToken, email)
}
