package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomSummary
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetChatRoomsUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = GetChatRoomsUseCase(chatRepository)

    @Test
    fun `invoke delegates to chatRepository and returns rooms`() =
        runTest {
            val expected =
                listOf(
                    ChatRoomSummary(
                        roomId = "conv-1",
                        title = "FC Sao Mai × Sài Gòn United",
                        contextType = ChatContextType.MATCH,
                        contextId = "req-1",
                        otherPartyName = "Sài Gòn United",
                        otherPartyShortName = "SGU",
                        lastMessagePreview = "Ok bạn",
                        lastMessageAt = "2026-11-18T16:33:00",
                        unreadCount = 1,
                        canSend = true,
                    ),
                )
            coEvery { chatRepository.getChatRooms(ChatContextType.MATCH) } returns Result.Success(expected)

            val result = sut(ChatContextType.MATCH)

            assertEquals(Result.Success(expected), result)
            coVerify(exactly = 1) { chatRepository.getChatRooms(ChatContextType.MATCH) }
        }
}
