package com.igorwojda.showcase.feature.chat.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatReportReason
import com.igorwojda.showcase.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReportChatUseCaseTest {
    private val chatRepository: ChatRepository = mockk()
    private val sut = ReportChatUseCase(chatRepository)

    @Test
    fun `invoke submits chat report to repository`() =
        runTest {
            coEvery {
                chatRepository.reportChat("conv-1", ChatReportReason.BAD_LANGUAGE, "Chửi thề")
            } returns Result.Success(Unit)

            val result = sut("conv-1", ChatReportReason.BAD_LANGUAGE, "Chửi thề")

            assertEquals(Result.Success(Unit), result)
            coVerify(exactly = 1) {
                chatRepository.reportChat("conv-1", ChatReportReason.BAD_LANGUAGE, "Chửi thề")
            }
        }
}
