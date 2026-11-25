package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomDetail
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetChatRoomDetailUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = GetChatRoomDetailUseCase(chatRepository)

    @Test
    fun `invoke returns room detail from repository`() =
        runTest {
            val detail =
                ChatRoomDetail(
                    roomId = "conv-1",
                    title = "FC Sao Mai × Sài Gòn United",
                    contextType = ChatContextType.MATCH,
                    contextId = "req-1",
                    matchId = "match-1",
                    viewerPartyName = "FC Sao Mai",
                    otherPartyName = "Sài Gòn United",
                    otherPartySubtitle = "Hội thoại ghép kèo",
                    canSend = true,
                )
            coEvery { chatRepository.getChatRoomDetail("conv-1") } returns Result.Success(detail)

            val result = sut("conv-1")

            assertEquals(Result.Success(detail), result)
            coVerify(exactly = 1) { chatRepository.getChatRoomDetail("conv-1") }
        }
}
