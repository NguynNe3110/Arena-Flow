package com.igorwojda.showcase.feature.chat.domain.model

/** Context category of a chat room. */
enum class ChatContextType(
    val apiValue: String,
    val label: String,
) {
    MATCH("MATCH", "Kèo đấu"),
    PAIRING("PAIRING", "Thương lượng"),
    MERCENARY("MERCENARY", "Đá lẻ"),
    ;

    companion object {
        fun fromApiValue(value: String?): ChatContextType = entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: MATCH
    }
}

/** Inbox filter chip options for CHAT-01. */
enum class ChatInboxFilter(
    val label: String,
) {
    ALL("Tất cả"),
    TEAM_MATCH("Kèo đấu"),
    MERCENARY("Đá lẻ"),
}

/** Sender classification for message bubble rendering. */
enum class ChatMessageSenderType {
    MINE,
    OTHER,
    SYSTEM,
    ;

    companion object {
        fun fromApiValue(value: String?): ChatMessageSenderType = entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}

/** Report reason enum for CHAT-05. */
enum class ChatReportReason(
    val apiValue: String,
    val label: String,
) {
    BAD_LANGUAGE("BAD_LANGUAGE", "Ngôn từ thiếu lịch sự"),
    SPAM("SPAM", "Spam / quảng cáo"),
    HARASSMENT("HARASSMENT", "Quấy rối / đe dọa"),
    SCAM("SCAM", "Dấu hiệu lừa đảo"),
}

/** Summary row displayed in ChatInboxScreen (CHAT-01). */
data class ChatRoomSummary(
    val roomId: String,
    val title: String,
    val contextType: ChatContextType,
    val contextId: String,
    val otherPartyName: String,
    val otherPartyShortName: String,
    val lastMessagePreview: String?,
    val lastMessageAt: String,
    val unreadCount: Int,
    val canSend: Boolean,
)

/** Room header & context metadata displayed in ChatRoomScreen (CHAT-02..04). */
data class ChatRoomDetail(
    val roomId: String,
    val title: String,
    val contextType: ChatContextType,
    val contextId: String,
    val matchId: String? = null,
    val pairingId: String? = null,
    val applicationId: String? = null,
    val viewerPartyName: String,
    val otherPartyName: String,
    val otherPartySubtitle: String,
    val canSend: Boolean,
    val readOnlyReason: String? = null,
)

/** Single message item inside a chat room. */
data class ChatMessage(
    val messageId: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val createdAt: String,
    val isRead: Boolean,
    val senderType: ChatMessageSenderType,
)

/** Cursor-paginated message list. */
data class ChatMessagePage(
    val items: List<ChatMessage>,
    val hasMore: Boolean,
)
