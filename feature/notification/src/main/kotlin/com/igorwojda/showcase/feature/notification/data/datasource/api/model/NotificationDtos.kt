package com.igorwojda.showcase.feature.notification.data.datasource.api.model

import com.igorwojda.showcase.feature.notification.domain.model.NotificationItem
import com.igorwojda.showcase.feature.notification.domain.model.resolveNotificationCategory
import kotlinx.serialization.Serializable

/** Notification item DTO returned by `GET notifications`. */
@Serializable
data class NotificationItemDto(
    val notificationId: String,
    val type: String,
    val title: String,
    val body: String,
    val referenceType: String? = null,
    val referenceId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String,
)

/** Unread notification count DTO returned by `GET notifications/unread-count`. */
@Serializable
data class UnreadCountDto(
    val unreadCount: Int,
)

/** Request body DTO for `POST devices/fcm-token`. */
@Serializable
data class RegisterFcmTokenRequestDto(
    val token: String,
    val platform: String = "ANDROID",
)

/** Maps [NotificationItemDto] to domain [NotificationItem]. */
fun NotificationItemDto.toDomain(): NotificationItem =
    NotificationItem(
        id = notificationId,
        type = type,
        category = resolveNotificationCategory(type),
        title = title,
        body = body,
        referenceType = referenceType,
        referenceId = referenceId,
        isRead = isRead,
        createdAt = createdAt,
    )
