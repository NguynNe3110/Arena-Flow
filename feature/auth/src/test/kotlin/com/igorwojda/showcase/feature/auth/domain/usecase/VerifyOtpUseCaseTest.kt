package com.igorwojda.showcase.feature.auth.domain.usecase

import com.igorwojda.showcase.feature.auth.domain.model.AuthSession
import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class VerifyOtpUseCaseTest {
    private val authRepository: AuthRepository = mockk()
    private val sut = VerifyOtpUseCase(authRepository)

    @Test
    fun `when invoked then returns verified session from repository`() =
        runTest {
            val expected =
                Result.Success(
                    AuthSession(
                        userId = "user-1",
                        role = AccountRole.USER,
                        isNewUser = false,
                        accessToken = "access",
                        refreshToken = "refresh",
                        expiresInSeconds = 3600,
                    ),
                )
            coEvery { authRepository.verifyOtp("0987654321", "123456") } returns expected

            val actual = sut("0987654321", "123456")

            actual shouldBeEqualTo expected
        }
}
