package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository

/** Marks all unread messages from the other party in a chat room as read. */
class MarkChatReadUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(roomId: String): Result<Unit> = chatRepository.markAsRead(roomId)
}
