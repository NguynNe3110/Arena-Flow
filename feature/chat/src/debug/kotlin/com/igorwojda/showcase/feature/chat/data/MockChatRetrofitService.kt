@file:Suppress("CognitiveComplexMethod", "LabeledExpression", "MagicNumber", "ReturnCount", "StringLiteralDuplication", "TooManyFunctions")

package com.igorwojda.showcase.feature.chat.data

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.CursorListDto
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.FieldValidationErrorDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreConversation
import com.igorwojda.showcase.feature.base.data.store.StoreMessage
import com.igorwojda.showcase.feature.base.data.store.StoreReport
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ChatMessageDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ChatRoomDetailDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ChatRoomSummaryDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ReportChatRequestDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.SendMessageRequestDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.service.ChatRetrofitService
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessageSenderType
import java.util.UUID

/** Debug-only mock implementation of [ChatRetrofitService] backed by [ArenaInMemoryStore]. */
internal class MockChatRetrofitService(
    private val store: ArenaInMemoryStore,
) : ChatRetrofitService {
    override suspend fun getChatRooms(contextType: String?): ApiResult<ApiEnvelope<List<ChatRoomSummaryDto>>> =
        store.executeApi(CHATS_PATH) { snapshot ->
            val uid =
                resolveActiveUserId<List<ChatRoomSummaryDto>>(snapshot, CHATS_PATH)
                    ?: return@executeApi unauthorizedError(CHATS_PATH)

            val rooms =
                snapshot.conversations
                    .filter { uid in it.participantIds }
                    .map { conv -> conv.toSummaryDto(snapshot, uid) }
                    .filter { dto -> contextType == null || dto.contextType.equals(contextType, ignoreCase = true) }
                    .sortedByDescending { it.lastMessageAt }

            ApiResult.Success(ApiEnvelope(rooms))
        }

    override suspend fun getChatRoomDetail(roomId: String): ApiResult<ApiEnvelope<ChatRoomDetailDto>> =
        store.executeApi("$CHATS_PATH/$roomId") { snapshot ->
            val path = "$CHATS_PATH/$roomId"
            val uid =
                resolveActiveUserId<ChatRoomDetailDto>(snapshot, path)
                    ?: return@executeApi unauthorizedError(path)

            val conv =
                snapshot.conversations.firstOrNull { it.id == roomId }
                    ?: return@executeApi roomNotFoundError(path)

            if (uid !in conv.participantIds) {
                return@executeApi forbiddenRoomError(path)
            }

            ApiResult.Success(ApiEnvelope(conv.toDetailDto(snapshot, uid)))
        }

    override suspend fun getMessages(
        roomId: String,
        cursor: String?,
        limit: Int,
    ): ApiResult<ApiEnvelope<CursorListDto<ChatMessageDto>>> =
        store.executeApi("$CHATS_PATH/$roomId/messages") { snapshot ->
            val path = "$CHATS_PATH/$roomId/messages"
            val uid =
                resolveActiveUserId<CursorListDto<ChatMessageDto>>(snapshot, path)
                    ?: return@executeApi unauthorizedError(path)

            val conv =
                snapshot.conversations.firstOrNull { it.id == roomId }
                    ?: return@executeApi roomNotFoundError(path)

            if (uid !in conv.participantIds) {
                return@executeApi forbiddenRoomError(path)
            }

            val safeLimit = limit.coerceIn(1, MAX_PAGE_LIMIT)
            val ordered = conv.messages.sortedBy { it.timestamp }
            val endExclusive =
                if (cursor.isNullOrBlank()) {
                    ordered.size
                } else {
                    val cursorIndex = ordered.indexOfFirst { it.id == cursor }
                    if (cursorIndex == -1) ordered.size else cursorIndex
                }
            val startInclusive = (endExclusive - safeLimit).coerceAtLeast(0)
            val pageItems =
                ordered
                    .subList(startInclusive, endExclusive)
                    .map { msg -> msg.toMessageDto(roomId = conv.id, snapshot = snapshot, viewerId = uid) }

            ApiResult.Success(
                ApiEnvelope(
                    CursorListDto(
                        items = pageItems,
                        hasMore = startInclusive > 0,
                    ),
                ),
            )
        }

    override suspend fun sendMessage(
        roomId: String,
        request: SendMessageRequestDto,
    ): ApiResult<ApiEnvelope<ChatMessageDto>> =
        store.executeApi("$CHATS_PATH/$roomId/messages") { snapshot ->
            val path = "$CHATS_PATH/$roomId/messages"
            val uid =
                resolveActiveUserId<ChatMessageDto>(snapshot, path)
                    ?: return@executeApi unauthorizedError(path)

            val conv =
                snapshot.conversations.firstOrNull { it.id == roomId }
                    ?: return@executeApi roomNotFoundError(path)

            if (uid !in conv.participantIds) {
                return@executeApi forbiddenRoomError(path)
            }

            if (!conv.canSend) {
                return@executeApi store.apiError(
                    httpStatus = HTTP_CONFLICT,
                    code = "CHAT_004",
                    message = conv.readOnlyReason ?: "Cuộc trò chuyện hiện chỉ còn chế độ xem.",
                    path = path,
                )
            }

            val trimmed = request.content.trim()
            if (trimmed.isEmpty() || trimmed.length > MAX_MESSAGE_LENGTH) {
                return@executeApi store.apiError(
                    httpStatus = HTTP_BAD_REQUEST,
                    code = "VALIDATION_ERROR",
                    message = "Nội dung tin nhắn phải từ 1 đến 1000 ký tự.",
                    path = path,
                    validationErrors =
                        listOf(
                            FieldValidationErrorDto(
                                field = "content",
                                message = "Nội dung tin nhắn phải từ 1 đến 1000 ký tự.",
                            ),
                        ),
                )
            }

            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val newMessage =
                StoreMessage(
                    id = "msg-${UUID.randomUUID().toString().take(8)}",
                    senderId = uid,
                    text = trimmed,
                    timestamp = nowIso,
                    readBy = listOf(uid),
                )

            store.update { current ->
                current.copy(
                    conversations =
                        current.conversations.map { item ->
                            if (item.id == roomId) item.copy(messages = item.messages + newMessage) else item
                        },
                )
            }

            ApiResult.Success(
                ApiEnvelope(
                    newMessage.toMessageDto(roomId = conv.id, snapshot = store.current, viewerId = uid),
                ),
            )
        }

    override suspend fun markAsRead(roomId: String): ApiResult<Unit> =
        store.executeApi("$CHATS_PATH/$roomId/read") { snapshot ->
            val path = "$CHATS_PATH/$roomId/read"
            val uid =
                snapshot.currentUserId
                    ?: return@executeApi unauthorizedError(path)

            val conv =
                snapshot.conversations.firstOrNull { it.id == roomId }
                    ?: return@executeApi roomNotFoundError(path)

            if (uid !in conv.participantIds) {
                return@executeApi forbiddenRoomError(path)
            }

            store.update { current ->
                current.copy(
                    conversations =
                        current.conversations.map { item ->
                            if (item.id != roomId) {
                                item
                            } else {
                                item.copy(
                                    messages =
                                        item.messages.map { msg ->
                                            if (uid in msg.readBy) msg else msg.copy(readBy = msg.readBy + uid)
                                        },
                                )
                            }
                        },
                )
            }

            ApiResult.Success(Unit)
        }

    override suspend fun reportChat(
        roomId: String,
        request: ReportChatRequestDto,
    ): ApiResult<Unit> =
        store.executeApi("$CHATS_PATH/$roomId/reports") { snapshot ->
            val path = "$CHATS_PATH/$roomId/reports"
            val uid =
                snapshot.currentUserId
                    ?: return@executeApi unauthorizedError(path)

            val conv =
                snapshot.conversations.firstOrNull { it.id == roomId }
                    ?: return@executeApi roomNotFoundError(path)

            if (uid !in conv.participantIds) {
                return@executeApi forbiddenRoomError(path)
            }

            if (conv.messages.isEmpty()) {
                return@executeApi store.apiError(
                    httpStatus = HTTP_CONFLICT,
                    code = "REPORT_010",
                    message = "Cuộc trò chuyện chưa có tin nhắn nào để báo cáo.",
                    path = path,
                )
            }

            val alreadyReported =
                snapshot.reports.any {
                    it.reporterId == uid && it.reportedId == roomId && it.type == REPORT_TYPE_CHAT
                }
            if (alreadyReported) {
                return@executeApi store.apiError(
                    httpStatus = HTTP_CONFLICT,
                    code = "REPORT_001",
                    message = "Bạn đã báo cáo cuộc trò chuyện này trước đó.",
                    path = path,
                )
            }

            val report =
                StoreReport(
                    id = "rep-chat-${UUID.randomUUID().toString().take(8)}",
                    reporterType = "USER",
                    reporterId = uid,
                    reportedId = roomId,
                    type = REPORT_TYPE_CHAT,
                    reason = "${request.reason}: ${request.details.trim()}".trimEnd(':', ' '),
                    status = "PENDING",
                    createdAt = VietnamDateTime.nowIso(snapshot.mockClockIso),
                )

            store.update { current ->
                current.copy(reports = current.reports + report)
            }

            ApiResult.Success(Unit)
        }

    private fun StoreConversation.toSummaryDto(
        snapshot: ArenaStoreSnapshot,
        viewerId: String,
    ): ChatRoomSummaryDto {
        val resolvedType = resolveContextType(snapshot)
        val otherParty = resolveOtherParty(snapshot, viewerId, resolvedType)
        val lastMsg = messages.maxByOrNull { it.timestamp }
        val unreadCount =
            messages.count { msg ->
                msg.senderId != viewerId &&
                    msg.senderId != SYSTEM_SENDER_ID &&
                    viewerId !in msg.readBy
            }

        return ChatRoomSummaryDto(
            roomId = id,
            title = title,
            contextType = resolvedType.apiValue,
            contextId = contextId,
            otherPartyName = otherParty.first,
            otherPartyShortName = otherParty.second,
            lastMessagePreview = lastMsg?.text,
            lastMessageAt = lastMsg?.timestamp ?: snapshot.mockClockIso,
            unreadCount = unreadCount,
            canSend = canSend,
        )
    }

    private fun StoreConversation.toDetailDto(
        snapshot: ArenaStoreSnapshot,
        viewerId: String,
    ): ChatRoomDetailDto {
        val resolvedType = resolveContextType(snapshot)
        val otherParty = resolveOtherParty(snapshot, viewerId, resolvedType)
        val viewerTeam = snapshot.teams.firstOrNull { it.captainId == viewerId }
        val viewerName = viewerTeam?.name ?: store.displayUserName(viewerId)

        val request = snapshot.matchRequests.firstOrNull { it.id == contextId || it.conversationId == id }
        val pairing = snapshot.pairings.firstOrNull { it.id == contextId || it.conversationId == id }
        val application = snapshot.applications.firstOrNull { it.id == contextId || it.conversationId == id }

        val matchId = request?.matchId ?: pairing?.matchId
        val subtitle =
            when (resolvedType) {
                ChatContextType.MATCH -> "Hội thoại ghép kèo"
                ChatContextType.PAIRING -> "Phòng thương lượng Kèo B"
                ChatContextType.MERCENARY -> "Hội thoại tuyển đá lẻ"
            }

        return ChatRoomDetailDto(
            roomId = id,
            title = title,
            contextType = resolvedType.apiValue,
            contextId = contextId,
            matchId = matchId,
            pairingId = pairing?.id,
            applicationId = application?.id,
            viewerPartyName = viewerName,
            otherPartyName = otherParty.first,
            otherPartySubtitle = subtitle,
            canSend = canSend,
            readOnlyReason = readOnlyReason,
        )
    }

    private fun StoreConversation.resolveContextType(snapshot: ArenaStoreSnapshot): ChatContextType {
        if (contextType.equals("MERCENARY_APPLICATION", ignoreCase = true) ||
            contextType.equals("MERCENARY", ignoreCase = true)
        ) {
            return ChatContextType.MERCENARY
        }
        val isPairing =
            contextId.startsWith("pairing") ||
                snapshot.pairings.any { it.id == contextId || it.conversationId == id }
        return if (isPairing) ChatContextType.PAIRING else ChatContextType.MATCH
    }

    private fun StoreConversation.resolveOtherParty(
        snapshot: ArenaStoreSnapshot,
        viewerId: String,
        contextType: ChatContextType,
    ): Pair<String, String> {
        val otherUserId = participantIds.firstOrNull { it != viewerId }
        if (contextType != ChatContextType.MERCENARY && otherUserId != null) {
            val team = snapshot.teams.firstOrNull { it.captainId == otherUserId }
            if (team != null) return team.name to team.shortName
        }
        val userName = store.displayUserName(otherUserId)
        val initials =
            userName
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifBlank { "AF" }
        return userName to initials
    }

    @Suppress("UnusedParameter")
    private fun StoreMessage.toMessageDto(
        roomId: String,
        snapshot: ArenaStoreSnapshot,
        viewerId: String,
    ): ChatMessageDto {
        val senderType =
            when (senderId) {
                SYSTEM_SENDER_ID -> ChatMessageSenderType.SYSTEM
                viewerId -> ChatMessageSenderType.MINE
                else -> ChatMessageSenderType.OTHER
            }
        val senderName =
            if (senderType == ChatMessageSenderType.SYSTEM) {
                "Hệ thống"
            } else {
                store.displayUserName(senderId)
            }
        return ChatMessageDto(
            messageId = id,
            roomId = roomId,
            senderId = senderId,
            senderName = senderName,
            content = text,
            createdAt = timestamp,
            isRead = readBy.any { it != senderId },
            senderType = senderType.name,
        )
    }

    @Suppress("UnusedParameter")
    private fun <T> resolveActiveUserId(
        snapshot: ArenaStoreSnapshot,
        path: String,
    ): String? {
        val uid = snapshot.currentUserId ?: return null
        val user = snapshot.users[uid] ?: return null
        if (user.isDeleted) return null
        return uid
    }

    private fun <T> unauthorizedError(path: String): ApiResult<T> =
        store.apiError(
            httpStatus = HTTP_UNAUTHORIZED,
            code = "AUTH_001",
            message = "Vui lòng đăng nhập để xem tin nhắn.",
            path = path,
        )

    private fun <T> roomNotFoundError(path: String): ApiResult<T> =
        store.apiError(
            httpStatus = HTTP_NOT_FOUND,
            code = "CHAT_002",
            message = "Không tìm thấy cuộc trò chuyện.",
            path = path,
        )

    private fun <T> forbiddenRoomError(path: String): ApiResult<T> =
        store.apiError(
            httpStatus = HTTP_FORBIDDEN,
            code = "CHAT_003",
            message = "Bạn không có quyền truy cập cuộc trò chuyện này.",
            path = path,
        )

    private companion object {
        const val CHATS_PATH = "/api/v1/chats"
        const val SYSTEM_SENDER_ID = "system"
        const val REPORT_TYPE_CHAT = "CHAT"
        const val MAX_PAGE_LIMIT = 100
        const val MAX_MESSAGE_LENGTH = 1000
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
    }
}
