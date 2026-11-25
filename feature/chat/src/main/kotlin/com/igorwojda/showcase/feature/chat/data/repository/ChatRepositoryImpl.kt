package com.igorwojda.showcase.feature.chat.data.repository

import com.igorwojda.showcase.feature.base.data.error.apiErrorToAppError
import com.igorwojda.showcase.feature.base.data.error.dataResult
import com.igorwojda.showcase.feature.base.data.error.toAppError
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ReportChatRequestDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.SendMessageRequestDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.toDomain
import com.igorwojda.showcase.feature.chat.data.datasource.api.service.ChatRetrofitService
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessagePage
import com.igorwojda.showcase.feature.chat.domain.model.ChatReportReason
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomDetail
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomSummary
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository

internal class ChatRepositoryImpl(
    private val service: ChatRetrofitService,
) : ChatRepository {
    override suspend fun getChatRooms(contextType: ChatContextType?): Result<List<ChatRoomSummary>> =
        executeApi { service.getChatRooms(contextType = contextType?.apiValue) }
            .map { envelope -> envelope.data.map { it.toDomain() } }

    override suspend fun getChatRoomDetail(roomId: String): Result<ChatRoomDetail> =
        executeApi { service.getChatRoomDetail(roomId) }
            .map { envelope -> envelope.data.toDomain() }

    override suspend fun getMessages(
        roomId: String,
        cursor: String?,
        limit: Int,
    ): Result<ChatMessagePage> =
        executeApi { service.getMessages(roomId = roomId, cursor = cursor, limit = limit) }
            .map { envelope -> envelope.data.toDomain() }

    override suspend fun sendMessage(
        roomId: String,
        content: String,
    ): Result<ChatMessage> =
        executeApi {
            service.sendMessage(
                roomId = roomId,
                request = SendMessageRequestDto(content = content),
            )
        }.map { envelope -> envelope.data.toDomain() }

    override suspend fun markAsRead(roomId: String): Result<Unit> = executeApi { service.markAsRead(roomId) }

    override suspend fun reportChat(
        roomId: String,
        reason: ChatReportReason,
        details: String,
    ): Result<Unit> =
        executeApi {
            service.reportChat(
                roomId = roomId,
                request = ReportChatRequestDto(reason = reason.apiValue, details = details),
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
