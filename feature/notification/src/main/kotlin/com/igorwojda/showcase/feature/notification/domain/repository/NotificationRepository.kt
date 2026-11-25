package com.igorwojda.showcase.feature.notification.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.model.NotificationPage

/** Repository contract for in-app notifications and FCM token registration. */
interface NotificationRepository {
    /** Fetches paginated notifications for the current user. */
    suspend fun getNotifications(
        page: Int = 1,
        pageSize: Int = 20,
        unreadOnly: Boolean = false,
    ): Result<NotificationPage>

    /** Fetches the count of unread notifications for the current user. */
    suspend fun getUnreadCount(): Result<Int>

    /** Marks a single notification as read. */
    suspend fun markAsRead(notificationId: String): Result<Unit>

    /** Marks all notifications of the current user as read. */
    suspend fun markAllAsRead(): Result<Unit>

    /** Registers the device FCM token for push notifications. */
    suspend fun registerFcmToken(
        token: String,
        platform: String = "ANDROID",
    ): Result<Unit>
}
