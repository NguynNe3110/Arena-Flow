package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository

/** Fetches the unread notification count for the current user. */
class GetUnreadNotificationCountUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(): Result<Int> = notificationRepository.getUnreadCount()
}
