package com.igorwojda.showcase.feature.chat.presentation.screen.room

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessagePage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessageSenderType
import com.igorwojda.showcase.feature.chat.domain.model.ChatReportReason
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomDetail
import com.igorwojda.showcase.feature.chat.domain.usecase.GetChatMessagesUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.GetChatRoomDetailUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.MarkChatReadUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.ReportChatUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.SendChatMessageUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class ChatRoomViewModelTest {
    private val getChatMessagesUseCase: GetChatMessagesUseCase = mockk()
    private val getChatRoomDetailUseCase: GetChatRoomDetailUseCase = mockk()
    private val markChatReadUseCase: MarkChatReadUseCase = mockk()
    private val reportChatUseCase: ReportChatUseCase = mockk()
    private val sendChatMessageUseCase: SendChatMessageUseCase = mockk()

    private val sut =
        ChatRoomViewModel(
            getChatMessagesUseCase = getChatMessagesUseCase,
            getChatRoomDetailUseCase = getChatRoomDetailUseCase,
            markChatReadUseCase = markChatReadUseCase,
            reportChatUseCase = reportChatUseCase,
            sendChatMessageUseCase = sendChatMessageUseCase,
        )

    @Test
    fun `Load fetches room detail, messages, and marks room as read`() =
        runTest {
            val detail = sampleDetail()
            val initialMsg = sampleMessage("m-1", "Chào đội bạn")
            coEvery { getChatRoomDetailUseCase("conv-1") } returns Result.Success(detail)
            coEvery { getChatMessagesUseCase("conv-1", null, 50) } returns
                Result.Success(ChatMessagePage(items = listOf(initialMsg), hasMore = false))
            coEvery { markChatReadUseCase("conv-1") } returns Result.Success(Unit)

            sut.onEvent(ChatRoomEvent.Load("conv-1"))
            advanceUntilIdle()

            assertFalse(sut.uiStateFlow.value.isLoading)
            assertEquals(detail, sut.uiStateFlow.value.room)
            assertEquals(listOf(initialMsg), sut.uiStateFlow.value.messages)
            coVerify(exactly = 1) { markChatReadUseCase("conv-1") }
        }

    @Test
    fun `SelectQuickTemplate and SendMessage appends new message and clears draft`() =
        runTest {
            val detail = sampleDetail()
            val sentMsg = sampleMessage("m-2", "Xác nhận sân?")
            coEvery { getChatRoomDetailUseCase("conv-1") } returns Result.Success(detail)
            coEvery { getChatMessagesUseCase("conv-1", null, 50) } returns
                Result.Success(ChatMessagePage(items = emptyList(), hasMore = false))
            coEvery { markChatReadUseCase("conv-1") } returns Result.Success(Unit)
            coEvery { sendChatMessageUseCase("conv-1", "Xác nhận sân?") } returns Result.Success(sentMsg)

            sut.onEvent(ChatRoomEvent.Load("conv-1"))
            advanceUntilIdle()

            sut.onEvent(ChatRoomEvent.SelectQuickTemplate("Xác nhận sân?"))
            assertEquals("Xác nhận sân?", sut.uiStateFlow.value.draftText)

            sut.onEvent(ChatRoomEvent.SendMessage)
            advanceUntilIdle()

            assertEquals("", sut.uiStateFlow.value.draftText)
            assertEquals(listOf(sentMsg), sut.uiStateFlow.value.messages)
        }

    @Test
    fun `SubmitReport closes sheet and sets confirmation message`() =
        runTest {
            val detail = sampleDetail()
            coEvery { getChatRoomDetailUseCase("conv-1") } returns Result.Success(detail)
            coEvery { getChatMessagesUseCase("conv-1", null, 50) } returns
                Result.Success(ChatMessagePage(items = emptyList(), hasMore = false))
            coEvery { markChatReadUseCase("conv-1") } returns Result.Success(Unit)
            coEvery {
                reportChatUseCase("conv-1", ChatReportReason.SPAM, "Spam nhiều lần")
            } returns Result.Success(Unit)

            sut.onEvent(ChatRoomEvent.Load("conv-1"))
            advanceUntilIdle()

            sut.onEvent(ChatRoomEvent.OpenReportSheet)
            assertTrue(sut.uiStateFlow.value.showReportSheet)

            sut.onEvent(ChatRoomEvent.SubmitReport(ChatReportReason.SPAM, "Spam nhiều lần"))
            advanceUntilIdle()

            assertFalse(sut.uiStateFlow.value.showReportSheet)
            assertTrue(sut.uiStateFlow.value.reportMessage != null)
        }

    private fun sampleDetail() =
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

    private fun sampleMessage(
        id: String,
        content: String,
    ) = ChatMessage(
        messageId = id,
        roomId = "conv-1",
        senderId = "user-cap-alpha",
        senderName = "Nguyễn Văn A",
        content = content,
        createdAt = "2026-11-18T10:00:00",
        isRead = false,
        senderType = ChatMessageSenderType.MINE,
    )
}
