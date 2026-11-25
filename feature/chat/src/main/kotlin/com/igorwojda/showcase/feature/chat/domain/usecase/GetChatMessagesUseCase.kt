package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessagePage
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository

/** Loads cursor-paginated messages for a chat room. */
class GetChatMessagesUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        roomId: String,
        cursor: String? = null,
        limit: Int = ChatRepository.DEFAULT_PAGE_LIMIT,
    ): Result<ChatMessagePage> = chatRepository.getMessages(roomId = roomId, cursor = cursor, limit = limit)
}
