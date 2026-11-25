package com.igorwojda.showcase.feature.chat.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessagePage
import com.igorwojda.showcase.feature.chat.domain.model.ChatReportReason
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomDetail
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomSummary

/** Repository contract for chat rooms, messages, read receipts, and chat reports. */
interface ChatRepository {
    suspend fun getChatRooms(contextType: ChatContextType? = null): Result<List<ChatRoomSummary>>

    suspend fun getChatRoomDetail(roomId: String): Result<ChatRoomDetail>

    suspend fun getMessages(
        roomId: String,
        cursor: String? = null,
        limit: Int = DEFAULT_PAGE_LIMIT,
    ): Result<ChatMessagePage>

    suspend fun sendMessage(
        roomId: String,
        content: String,
    ): Result<ChatMessage>

    suspend fun markAsRead(roomId: String): Result<Unit>

    suspend fun reportChat(
        roomId: String,
        reason: ChatReportReason,
        details: String,
    ): Result<Unit>

    companion object {
        const val DEFAULT_PAGE_LIMIT = 50
    }
}
