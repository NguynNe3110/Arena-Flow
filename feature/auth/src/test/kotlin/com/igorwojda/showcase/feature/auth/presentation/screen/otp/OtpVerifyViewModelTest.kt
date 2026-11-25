package com.igorwojda.showcase.feature.auth.presentation.screen.otp

import com.igorwojda.showcase.feature.auth.domain.model.AuthSession
import com.igorwojda.showcase.feature.auth.domain.model.SendOtpResult
import com.igorwojda.showcase.feature.auth.domain.usecase.SendOtpUseCase
import com.igorwojda.showcase.feature.auth.domain.usecase.VerifyOtpUseCase
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class OtpVerifyViewModelTest {
    private val sendOtpUseCase: SendOtpUseCase = mockk()
    private val verifyOtpUseCase: VerifyOtpUseCase = mockk()
    private val sut = OtpVerifyViewModel(sendOtpUseCase, verifyOtpUseCase)

    @Test
    fun `verifyOtp success invokes onAuthSuccess`() =
        runTest {
            val session =
                AuthSession(
                    userId = "u1",
                    role = AccountRole.USER,
                    isNewUser = false,
                    accessToken = "a",
                    refreshToken = "r",
                    expiresInSeconds = 3600,
                )
            coEvery { verifyOtpUseCase("0987654321", "123456") } returns Result.Success(session)
            var resultIsNew: Boolean? = null

            sut.initialize("0987654321", 0)
            sut.onOtpChanged("123456")
            sut.verifyOtp { resultIsNew = it }
            advanceUntilIdle()

            assertEquals(false, resultIsNew)
        }

    @Test
    fun `verifyOtp AUTH_003 sets deleted account error`() =
        runTest {
            coEvery {
                verifyOtpUseCase("0900000099", "123456")
            } returns Result.Failure(AppError.DomainError(403, "AUTH_003", "Tài khoản đã bị xóa"))

            sut.initialize("0900000099", 0)
            sut.onOtpChanged("123456")
            sut.verifyOtp {}
            advanceUntilIdle()

            assertTrue(sut.uiStateFlow.value.isDeletedAccountError)
        }

    @Test
    fun `countdown ticks down and resendOtp resets remainingSeconds`() =
        runTest {
            coEvery { sendOtpUseCase("0987654321") } returns Result.Success(SendOtpResult("0987654321", 45))

            sut.initialize("0987654321", 2)
            advanceTimeBy(2000L)
            runCurrent()
            assertEquals(0, sut.uiStateFlow.value.remainingSeconds)
            assertTrue(sut.uiStateFlow.value.canResend)

            sut.resendOtp()
            runCurrent()
            assertEquals(45, sut.uiStateFlow.value.remainingSeconds)
        }
}
