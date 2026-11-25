package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository

/** Marks all notifications of the current user as read. */
class MarkAllNotificationsReadUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(): Result<Unit> = notificationRepository.markAllAsRead()
}
