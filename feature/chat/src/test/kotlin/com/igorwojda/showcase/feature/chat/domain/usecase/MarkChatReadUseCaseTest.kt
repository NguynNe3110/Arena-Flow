package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MarkChatReadUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = MarkChatReadUseCase(chatRepository)

    @Test
    fun `invoke calls markAsRead on repository`() =
        runTest {
            coEvery { chatRepository.markAsRead("conv-1") } returns Result.Success(Unit)

            val result = sut("conv-1")

            assertEquals(Result.Success(Unit), result)
            coVerify(exactly = 1) { chatRepository.markAsRead("conv-1") }
        }
}
