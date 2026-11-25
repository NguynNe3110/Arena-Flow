package com.igorwojda.showcase.feature.auth.domain.usecase

import com.igorwojda.showcase.feature.auth.domain.model.SendOtpResult
import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SendOtpUseCaseTest {
    private val authRepository: AuthRepository = mockk()
    private val sut = SendOtpUseCase(authRepository)

    @Test
    fun `when invoked then returns sendOtp result from repository`() =
        runTest {
            val expected = Result.Success(SendOtpResult(phone = "0987654321", expiresInSeconds = 300))
            coEvery { authRepository.sendOtp("0987654321") } returns expected

            val actual = sut("0987654321")

            actual shouldBeEqualTo expected
        }
}
