package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class RegisterFcmTokenUseCaseTest {
    private val notificationRepository: NotificationRepository = mockk()
    private val sut = RegisterFcmTokenUseCase(notificationRepository)

    @Test
    fun `when invoked then delegates registerFcmToken to repository`() =
        runTest {
            coEvery { notificationRepository.registerFcmToken(token = "fcm-123", platform = "ANDROID") } returns
                Result.Success(Unit)

            val actual = sut(token = "fcm-123")

            actual shouldBeEqualTo Result.Success(Unit)
        }
}
