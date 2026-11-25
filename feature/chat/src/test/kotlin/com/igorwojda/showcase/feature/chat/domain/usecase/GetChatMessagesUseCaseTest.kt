package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessagePage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessageSenderType
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetChatMessagesUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = GetChatMessagesUseCase(chatRepository)

    @Test
    fun `invoke returns cursor message page`() =
        runTest {
            val page =
                ChatMessagePage(
                    items =
                        listOf(
                            ChatMessage(
                                messageId = "m1",
                                roomId = "conv-1",
                                senderId = "u1",
                                senderName = "Nam",
                                content = "Chào bạn",
                                createdAt = "2026-11-18T10:00:00",
                                isRead = true,
                                senderType = ChatMessageSenderType.OTHER,
                            ),
                        ),
                    hasMore = false,
                )
            coEvery { chatRepository.getMessages("conv-1", "m2", 20) } returns Result.Success(page)

            val result = sut(roomId = "conv-1", cursor = "m2", limit = 20)

            assertEquals(Result.Success(page), result)
            coVerify(exactly = 1) { chatRepository.getMessages("conv-1", "m2", 20) }
        }
}
