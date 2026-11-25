package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.model.NotificationPage
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository

/** Fetches paginated in-app notifications for the current user. */
class GetNotificationsUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 20,
        unreadOnly: Boolean = false,
    ): Result<NotificationPage> =
        notificationRepository.getNotifications(
            page = page,
            pageSize = pageSize,
            unreadOnly = unreadOnly,
        )
}
