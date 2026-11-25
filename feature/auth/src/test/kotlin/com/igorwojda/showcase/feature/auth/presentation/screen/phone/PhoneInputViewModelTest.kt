package com.igorwojda.showcase.feature.auth.presentation.screen.phone

import com.igorwojda.showcase.feature.auth.domain.model.SendOtpResult
import com.igorwojda.showcase.feature.auth.domain.usecase.SendOtpUseCase
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class PhoneInputViewModelTest {
    private val sendOtpUseCase: SendOtpUseCase = mockk()
    private val sut = PhoneInputViewModel(sendOtpUseCase)

    @Test
    fun `invalid phone produces validation error`() =
        runTest {
            sut.onPhoneChanged("098765432")
            sut.submitPhone { _, _ -> }

            assertNotNull(sut.uiStateFlow.value.phoneError)
        }

    @Test
    fun `plus 84 phone normalizes to 0 prefix and sends otp`() =
        runTest {
            coEvery { sendOtpUseCase("0987654321") } returns Result.Success(SendOtpResult("0987654321", 300))
            var sentPhone: String? = null
            var sentExpires: Int? = null

            sut.onPhoneChanged("+84 987 654 321")
            sut.submitPhone { phone, expires ->
                sentPhone = phone
                sentExpires = expires
            }
            advanceUntilIdle()

            assertEquals("0987654321", sentPhone)
            assertEquals(300, sentExpires)
            assertNull(sut.uiStateFlow.value.phoneError)
        }
}
