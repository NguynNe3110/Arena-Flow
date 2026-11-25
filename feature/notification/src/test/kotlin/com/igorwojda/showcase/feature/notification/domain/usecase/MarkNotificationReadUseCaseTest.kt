package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class MarkNotificationReadUseCaseTest {
    private val notificationRepository: NotificationRepository = mockk()
    private val sut = MarkNotificationReadUseCase(notificationRepository)

    @Test
    fun `when invoked then delegates markAsRead to repository`() =
        runTest {
            coEvery { notificationRepository.markAsRead("notif-1") } returns Result.Success(Unit)

            val actual = sut("notif-1")

            actual shouldBeEqualTo Result.Success(Unit)
        }
}
