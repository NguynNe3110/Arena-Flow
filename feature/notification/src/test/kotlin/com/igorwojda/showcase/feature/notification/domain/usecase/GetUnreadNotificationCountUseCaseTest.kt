package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetUnreadNotificationCountUseCaseTest {
    private val notificationRepository: NotificationRepository = mockk()
    private val sut = GetUnreadNotificationCountUseCase(notificationRepository)

    @Test
    fun `when invoked then returns unread count from repository`() =
        runTest {
            coEvery { notificationRepository.getUnreadCount() } returns Result.Success(4)

            val actual = sut()

            actual shouldBeEqualTo Result.Success(4)
        }
}
