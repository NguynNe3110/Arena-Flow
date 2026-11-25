package com.igorwojda.showcase.feature.chat.data.datasource.api.model

import com.igorwojda.showcase.feature.base.data.retrofit.envelope.CursorListDto
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessagePage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessageSenderType
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomDetail
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomSummary
import kotlinx.serialization.Serializable

/** Network DTO for a chat room summary item. */
@Serializable
data class ChatRoomSummaryDto(
    val roomId: String,
    val title: String,
    val contextType: String,
    val contextId: String,
    val otherPartyName: String,
    val otherPartyShortName: String,
    val lastMessagePreview: String? = null,
    val lastMessageAt: String,
    val unreadCount: Int = 0,
    val canSend: Boolean = true,
)

/** Network DTO for chat room header and permissions. */
@Serializable
data class ChatRoomDetailDto(
    val roomId: String,
    val title: String,
    val contextType: String,
    val contextId: String,
    val matchId: String? = null,
    val pairingId: String? = null,
    val applicationId: String? = null,
    val viewerPartyName: String,
    val otherPartyName: String,
    val otherPartySubtitle: String,
    val canSend: Boolean = true,
    val readOnlyReason: String? = null,
)

/** Network DTO for a single chat message. */
@Serializable
data class ChatMessageDto(
    val messageId: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val createdAt: String,
    val isRead: Boolean = false,
    val senderType: String = "OTHER",
)

/** Request body for sending a chat message. */
@Serializable
data class SendMessageRequestDto(
    val content: String,
)

/** Request body for reporting a chat conversation. */
@Serializable
data class ReportChatRequestDto(
    val reason: String,
    val details: String,
)

internal fun ChatRoomSummaryDto.toDomain(): ChatRoomSummary =
    ChatRoomSummary(
        roomId = roomId,
        title = title,
        contextType = ChatContextType.fromApiValue(contextType),
        contextId = contextId,
        otherPartyName = otherPartyName,
        otherPartyShortName = otherPartyShortName,
        lastMessagePreview = lastMessagePreview,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        canSend = canSend,
    )

internal fun ChatRoomDetailDto.toDomain(): ChatRoomDetail =
    ChatRoomDetail(
        roomId = roomId,
        title = title,
        contextType = ChatContextType.fromApiValue(contextType),
        contextId = contextId,
        matchId = matchId,
        pairingId = pairingId,
        applicationId = applicationId,
        viewerPartyName = viewerPartyName,
        otherPartyName = otherPartyName,
        otherPartySubtitle = otherPartySubtitle,
        canSend = canSend,
        readOnlyReason = readOnlyReason,
    )

internal fun ChatMessageDto.toDomain(): ChatMessage =
    ChatMessage(
        messageId = messageId,
        roomId = roomId,
        senderId = senderId,
        senderName = senderName,
        content = content,
        createdAt = createdAt,
        isRead = isRead,
        senderType = ChatMessageSenderType.fromApiValue(senderType),
    )

internal fun CursorListDto<ChatMessageDto>.toDomain(): ChatMessagePage =
    ChatMessagePage(
        items = items.map { it.toDomain() },
        hasMore = hasMore,
    )
