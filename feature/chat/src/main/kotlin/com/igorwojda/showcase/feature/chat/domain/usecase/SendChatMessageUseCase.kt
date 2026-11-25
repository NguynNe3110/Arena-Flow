package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessage
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository

/** Sends a text message to a chat room. */
class SendChatMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        roomId: String,
        content: String,
    ): Result<ChatMessage> = chatRepository.sendMessage(roomId = roomId, content = content)
}
