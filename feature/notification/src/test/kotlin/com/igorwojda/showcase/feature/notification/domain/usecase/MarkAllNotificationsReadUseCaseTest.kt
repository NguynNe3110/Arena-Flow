package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class MarkAllNotificationsReadUseCaseTest {
    private val notificationRepository: NotificationRepository = mockk()
    private val sut = MarkAllNotificationsReadUseCase(notificationRepository)

    @Test
    fun `when invoked then delegates markAllAsRead to repository`() =
        runTest {
            coEvery { notificationRepository.markAllAsRead() } returns Result.Success(Unit)

            val actual = sut()

            actual shouldBeEqualTo Result.Success(Unit)
        }
}
