package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository

/** Marks a single notification as read. */
class MarkNotificationReadUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(notificationId: String): Result<Unit> = notificationRepository.markAsRead(notificationId)
}
