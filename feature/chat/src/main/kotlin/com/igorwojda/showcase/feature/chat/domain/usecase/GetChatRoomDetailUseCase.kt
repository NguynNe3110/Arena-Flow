package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomDetail
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository

/** Loads header and permission details for a specific chat room. */
class GetChatRoomDetailUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(roomId: String): Result<ChatRoomDetail> = chatRepository.getChatRoomDetail(roomId)
}
