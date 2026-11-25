package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessageSenderType
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SendChatMessageUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = SendChatMessageUseCase(chatRepository)

    @Test
    fun `invoke delegates sendMessage to repository`() =
        runTest {
            val message =
                ChatMessage(
                    messageId = "m-new",
                    roomId = "conv-1",
                    senderId = "u-me",
                    senderName = "Tôi",
                    content = "Xác nhận sân?",
                    createdAt = "2026-11-18T10:05:00",
                    isRead = false,
                    senderType = ChatMessageSenderType.MINE,
                )
            coEvery { chatRepository.sendMessage("conv-1", "Xác nhận sân?") } returns Result.Success(message)

            val result = sut("conv-1", "Xác nhận sân?")

            assertEquals(Result.Success(message), result)
            coVerify(exactly = 1) { chatRepository.sendMessage("conv-1", "Xác nhận sân?") }
        }
}
