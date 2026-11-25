package com.igorwojda.showcase.feature.notification.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository

/** Registers the device FCM token for push notifications. */
class RegisterFcmTokenUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(
        token: String,
        platform: String = "ANDROID",
    ): Result<Unit> = notificationRepository.registerFcmToken(token = token, platform = platform)
}
