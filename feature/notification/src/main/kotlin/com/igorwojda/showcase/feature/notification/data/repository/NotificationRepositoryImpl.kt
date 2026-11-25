package com.igorwojda.showcase.feature.notification.data.repository

import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.data.datasource.api.model.RegisterFcmTokenRequestDto
import com.igorwojda.showcase.feature.notification.data.datasource.api.model.toDomain
import com.igorwojda.showcase.feature.notification.data.datasource.api.service.NotificationRetrofitService
import com.igorwojda.showcase.feature.notification.domain.model.NotificationPage
import com.igorwojda.showcase.feature.notification.domain.repository.NotificationRepository

/** Production implementation of [NotificationRepository] backed by [NotificationRetrofitService]. */
internal class NotificationRepositoryImpl(
    private val service: NotificationRetrofitService,
) : NotificationRepository {
    override suspend fun getNotifications(
        page: Int,
        pageSize: Int,
        unreadOnly: Boolean,
    ): Result<NotificationPage> =
        executeApi {
            service.getNotifications(
                page = page,
                pageSize = pageSize,
                unreadOnly = if (unreadOnly) true else null,
            )
        }.map { envelope ->
            NotificationPage(
                items = envelope.data.map { it.toDomain() },
                page = envelope.pagination.page,
                pageSize = envelope.pagination.pageSize,
                totalPages = envelope.pagination.totalPages,
                totalItems = envelope.pagination.totalItems,
            )
        }

    override suspend fun getUnreadCount(): Result<Int> = executeApi { service.getUnreadCount() }.map { it.data.unreadCount }

    override suspend fun markAsRead(notificationId: String): Result<Unit> = executeApi { service.markAsRead(notificationId) }

    override suspend fun markAllAsRead(): Result<Unit> = executeApi { service.markAllAsRead() }

    override suspend fun registerFcmToken(
        token: String,
        platform: String,
    ): Result<Unit> =
        executeApi {
            service.registerFcmToken(
                RegisterFcmTokenRequestDto(
                    token = token,
                    platform = platform,
                ),
            )
        }

    private suspend fun <T> executeApi(call: suspend () -> ApiResult<T>): Result<T> =
        dataResult {
            when (val result = call()) {
                is ApiResult.Success -> {
                    Result.Success(result.data)
                }

                is ApiResult.Error -> {
                    Result.Failure(apiErrorToAppError(result.code, result.errorBody))
                }

                is ApiResult.Exception -> {
                    Result.Failure((result.throwable as? Exception)?.toAppError() ?: AppError.Unknown)
                }
            }
        }

    private inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
        when (this) {
            is Result.Success -> Result.Success(transform(value))
            is Result.Failure -> this
        }
}
