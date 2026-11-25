package com.igorwojda.showcase.feature.notification.domain.model

/** High-level notification categories for UI icon and badge styling. */
enum class NotificationCategory(
    val label: String,
) {
    MATCH("Kèo đấu"),
    PAIRING("Thương lượng"),
    MERCENARY("Đá lẻ"),
    REPUTATION("Uy tín"),
    SYSTEM("Hệ thống"),
}

/** In-app notification item displayed in the notification center. */
data class NotificationItem(
    val id: String,
    val type: String,
    val category: NotificationCategory,
    val title: String,
    val body: String,
    val referenceType: String?,
    val referenceId: String?,
    val isRead: Boolean,
    val createdAt: String,
)

/** Paginated notification list result. */
data class NotificationPage(
    val items: List<NotificationItem>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
)

/** Resolves a backend notification `type` string to a display [NotificationCategory]. */
fun resolveNotificationCategory(type: String): NotificationCategory =
    when {
        type.startsWith("MATCH_") || type.startsWith("REQUEST_") || type == "REMINDER_BEFORE_MATCH" -> {
            NotificationCategory.MATCH
        }

        type.startsWith("PAIRING_") -> {
            NotificationCategory.PAIRING
        }

        type.startsWith("MERCENARY_") -> {
            NotificationCategory.MERCENARY
        }

        type.startsWith("RATING_") ||
            type.startsWith("REPORT_") ||
            type.startsWith("DISPUTE_") ||
            type.startsWith("VERIFICATION_BADGE_") -> {
            NotificationCategory.REPUTATION
        }

        else -> {
            NotificationCategory.SYSTEM
        }
    }
