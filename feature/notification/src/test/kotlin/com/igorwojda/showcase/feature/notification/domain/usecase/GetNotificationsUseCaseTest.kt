package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.model.NotificationCategory
import com.igorwojda.showcase.feature.notification.domain.model.NotificationItem
import com.igorwojda.showcase.feature.notification.domain.model.NotificationPage
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetNotificationsUseCaseTest {
    private val notificationRepository: NotificationRepository = mockk()
    private val sut = GetNotificationsUseCase(notificationRepository)

    @Test
    fun `when invoked then returns notification page from repository`() =
        runTest {
            val expected =
                NotificationPage(
                    items =
                        listOf(
                            NotificationItem(
                                id = "notif-1",
                                type = "MATCH_REQUEST_RECEIVED",
                                category = NotificationCategory.MATCH,
                                title = "Yêu cầu ghép kèo mới",
                                body = "Đội bạn có yêu cầu mới.",
                                referenceType = "MATCH",
                                referenceId = "match-1",
                                isRead = false,
                                createdAt = "2026-11-18T16:30:00",
                            ),
                        ),
                    page = 1,
                    pageSize = 20,
                    totalPages = 1,
                    totalItems = 1,
                )
            coEvery { notificationRepository.getNotifications(page = 1, pageSize = 20, unreadOnly = true) } returns
                Result.Success(expected)

            val actual = sut(page = 1, pageSize = 20, unreadOnly = true)

            actual shouldBeEqualTo Result.Success(expected)
        }
}
