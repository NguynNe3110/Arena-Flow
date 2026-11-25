package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomSummary
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository

/** Loads chat rooms for the current user, optionally filtered by context type. */
class GetChatRoomsUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(contextType: ChatContextType? = null): Result<List<ChatRoomSummary>> =
        chatRepository.getChatRooms(contextType)
}
