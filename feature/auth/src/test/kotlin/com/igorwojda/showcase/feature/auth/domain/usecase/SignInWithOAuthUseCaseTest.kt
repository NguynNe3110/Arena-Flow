package com.igorwojda.showcase.feature.auth.domain.usecase

import com.igorwojda.showcase.feature.auth.domain.model.AuthSession
import com.igorwojda.showcase.feature.auth.domain.model.OAuthProvider
import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SignInWithOAuthUseCaseTest {
    private val authRepository: AuthRepository = mockk()
    private val sut = SignInWithOAuthUseCase(authRepository)

    @Test
    fun `when invoked then returns oauth session from repository`() =
        runTest {
            val expected =
                Result.Success(
                    AuthSession(
                        userId = "user-1",
                        role = AccountRole.USER,
                        isNewUser = true,
                        accessToken = "access",
                        refreshToken = "refresh",
                        expiresInSeconds = 3600,
                    ),
                )
            coEvery { authRepository.signInWithOAuth(OAuthProvider.GOOGLE, "token", "a@b.com") } returns expected

            val actual = sut(OAuthProvider.GOOGLE, "token", "a@b.com")

            actual shouldBeEqualTo expected
        }
}
