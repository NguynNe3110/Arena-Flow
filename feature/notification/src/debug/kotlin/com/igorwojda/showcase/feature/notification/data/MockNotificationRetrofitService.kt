@file:Suppress("CognitiveComplexMethod", "LabeledExpression", "MagicNumber")

package com.igorwojda.showcase.feature.notification.data

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.FieldValidationErrorDto
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreNotification
import com.igorwojda.showcase.feature.notification.data.datasource.api.model.NotificationItemDto
import com.igorwojda.showcase.feature.notification.data.datasource.api.model.RegisterFcmTokenRequestDto
import com.igorwojda.showcase.feature.notification.data.datasource.api.model.UnreadCountDto
import com.igorwojda.showcase.feature.notification.data.datasource.api.service.NotificationRetrofitService
import kotlin.math.ceil

/** Debug-only mock implementation of [NotificationRetrofitService] backed by [ArenaInMemoryStore]. */
internal class MockNotificationRetrofitService(
    private val store: ArenaInMemoryStore,
) : NotificationRetrofitService {
    override suspend fun getNotifications(
        page: Int,
        pageSize: Int,
        unreadOnly: Boolean?,
    ): ApiResult<PaginatedEnvelope<NotificationItemDto>> =
        store.executeApi(NOTIFICATIONS_PATH) { snapshot ->
            val userLookup =
                resolveActiveUserId<PaginatedEnvelope<NotificationItemDto>>(snapshot, NOTIFICATIONS_PATH)
            if (userLookup.error != null) return@executeApi userLookup.error
            val uid = requireNotNull(userLookup.userId)

            val allItems =
                snapshot.notifications
                    .filter { notif ->
                        notif.userId == uid && (unreadOnly != true || !notif.read)
                    }.sortedWith(
                        compareByDescending<StoreNotification> { it.createdAt }
                            .thenByDescending { it.id },
                    ).map { it.toDto() }

            val safePage = page.coerceAtLeast(1)
            val safeSize = pageSize.coerceIn(1, MAX_PAGE_SIZE)
            val fromIndex = (safePage - 1) * safeSize
            val paged =
                if (fromIndex >= allItems.size) {
                    emptyList()
                } else {
                    allItems.subList(fromIndex, (fromIndex + safeSize).coerceAtMost(allItems.size))
                }
            val totalPages = if (allItems.isEmpty()) 0 else ceil(allItems.size.toDouble() / safeSize).toInt()

            ApiResult.Success(
                PaginatedEnvelope(
                    data = paged,
                    pagination =
                        PaginationDto(
                            page = safePage,
                            pageSize = safeSize,
                            totalPages = totalPages,
                            totalItems = allItems.size,
                        ),
                ),
            )
        }

    override suspend fun getUnreadCount(): ApiResult<ApiEnvelope<UnreadCountDto>> =
        store.executeApi(UNREAD_COUNT_PATH) { snapshot ->
            val userLookup =
                resolveActiveUserId<ApiEnvelope<UnreadCountDto>>(snapshot, UNREAD_COUNT_PATH)
            if (userLookup.error != null) return@executeApi userLookup.error
            val uid = requireNotNull(userLookup.userId)

            val count = snapshot.notifications.count { it.userId == uid && !it.read }
            ApiResult.Success(ApiEnvelope(UnreadCountDto(unreadCount = count)))
        }

    override suspend fun markAsRead(notificationId: String): ApiResult<Unit> =
        store.executeApi("$NOTIFICATIONS_PATH/$notificationId/read") { snapshot ->
            val path = "$NOTIFICATIONS_PATH/$notificationId/read"
            val userLookup = resolveActiveUserId<Unit>(snapshot, path)
            if (userLookup.error != null) return@executeApi userLookup.error
            val uid = requireNotNull(userLookup.userId)

            val target = snapshot.notifications.firstOrNull { it.id == notificationId && it.userId == uid }
            if (target == null) {
                return@executeApi store.apiError(
                    httpStatus = 404,
                    code = "NOTIFICATION_001",
                    message = "Thông báo không tồn tại.",
                    path = path,
                )
            }

            if (!target.read) {
                store.update { current ->
                    current.copy(
                        notifications =
                            current.notifications.map { notif ->
                                if (notif.id == notificationId && notif.userId == uid) {
                                    notif.copy(read = true)
                                } else {
                                    notif
                                }
                            },
                    )
                }
            }

            ApiResult.Success(Unit)
        }

    override suspend fun markAllAsRead(): ApiResult<Unit> =
        store.executeApi(READ_ALL_PATH) { snapshot ->
            val userLookup = resolveActiveUserId<Unit>(snapshot, READ_ALL_PATH)
            if (userLookup.error != null) return@executeApi userLookup.error
            val uid = requireNotNull(userLookup.userId)

            store.update { current ->
                current.copy(
                    notifications =
                        current.notifications.map { notif ->
                            if (notif.userId == uid && !notif.read) {
                                notif.copy(read = true)
                            } else {
                                notif
                            }
                        },
                )
            }

            ApiResult.Success(Unit)
        }

    override suspend fun registerFcmToken(request: RegisterFcmTokenRequestDto): ApiResult<Unit> =
        store.executeApi(FCM_TOKEN_PATH) { snapshot ->
            val userLookup = resolveActiveUserId<Unit>(snapshot, FCM_TOKEN_PATH)
            if (userLookup.error != null) return@executeApi userLookup.error

            if (request.token.isBlank()) {
                return@executeApi store.apiError(
                    httpStatus = 400,
                    code = "VALIDATION_ERROR",
                    message = "Dữ liệu không hợp lệ",
                    path = FCM_TOKEN_PATH,
                    validationErrors =
                        listOf(
                            FieldValidationErrorDto(
                                field = "token",
                                message = "FCM token không được để trống",
                            ),
                        ),
                )
            }

            ApiResult.Success(Unit)
        }

    private fun <T> resolveActiveUserId(
        snapshot: ArenaStoreSnapshot,
        path: String,
    ): UserIdLookupResult<T> {
        val uid =
            snapshot.currentUserId
                ?: return UserIdLookupResult(
                    error =
                        store.apiError(
                            httpStatus = 401,
                            code = "AUTH_001",
                            message = "Vui lòng đăng nhập để tiếp tục.",
                            path = path,
                        ),
                )
        val user = snapshot.users[uid]
        if (user == null || user.isDeleted) {
            return UserIdLookupResult(
                error =
                    store.apiError(
                        httpStatus = 404,
                        code = "USER_001",
                        message = "Người dùng không tồn tại.",
                        path = path,
                    ),
            )
        }
        return UserIdLookupResult(userId = uid)
    }

    private fun StoreNotification.toDto(): NotificationItemDto =
        NotificationItemDto(
            notificationId = id,
            type = type,
            title = title,
            body = body,
            referenceType = referenceType,
            referenceId = referenceId,
            isRead = read,
            createdAt = createdAt,
        )

    private data class UserIdLookupResult<T>(
        val userId: String? = null,
        val error: ApiResult.Error<T>? = null,
    )

    private companion object {
        const val NOTIFICATIONS_PATH = "/api/v1/notifications"
        const val UNREAD_COUNT_PATH = "/api/v1/notifications/unread-count"
        const val READ_ALL_PATH = "/api/v1/notifications/read-all"
        const val FCM_TOKEN_PATH = "/api/v1/devices/fcm-token"
        const val MAX_PAGE_SIZE = 100
    }
}
