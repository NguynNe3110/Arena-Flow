package com.igorwojda.showcase.feature.chat.data.repository

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.CursorListDto
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ChatMessageDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.ChatRoomSummaryDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.model.SendMessageRequestDto
import com.igorwojda.showcase.feature.chat.data.datasource.api.service.ChatRetrofitService
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessageSenderType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

class ChatRepositoryImplTest {
    private val service: ChatRetrofitService = mockk()
    private val sut = ChatRepositoryImpl(service)

    @Test
    fun `getChatRooms maps DTO list to domain models`() =
        runTest {
            val dto =
                ChatRoomSummaryDto(
                    roomId = "conv-1",
                    title = "FC Sao Mai × Sài Gòn United",
                    contextType = "PAIRING",
                    contextId = "p-1",
                    otherPartyName = "Sài Gòn United",
                    otherPartyShortName = "SGU",
                    lastMessagePreview = "Chốt giờ nhé",
                    lastMessageAt = "2026-11-18T10:00:00",
                    unreadCount = 2,
                    canSend = true,
                )
            coEvery { service.getChatRooms(null) } returns ApiResult.Success(ApiEnvelope(listOf(dto)))

            val result = sut.getChatRooms()

            assertTrue(result is Result.Success)
            val rooms = (result as Result.Success).value
            assertEquals(1, rooms.size)
            assertEquals(ChatContextType.PAIRING, rooms.first().contextType)
            assertEquals(2, rooms.first().unreadCount)
        }

    @Test
    fun `getMessages maps cursor list DTO to domain page`() =
        runTest {
            val msgDto =
                ChatMessageDto(
                    messageId = "msg-1",
                    roomId = "conv-1",
                    senderId = "u-1",
                    senderName = "Nguyễn Văn A",
                    content = "Đủ người rồi",
                    createdAt = "2026-11-18T10:00:00",
                    isRead = true,
                    senderType = "MINE",
                )
            coEvery { service.getMessages("conv-1", null, 50) } returns
                ApiResult.Success(ApiEnvelope(CursorListDto(items = listOf(msgDto), hasMore = true)))

            val result = sut.getMessages("conv-1")

            assertTrue(result is Result.Success)
            val page = (result as Result.Success).value
            assertTrue(page.hasMore)
            assertEquals(ChatMessageSenderType.MINE, page.items.first().senderType)
        }

    @Test
    fun `sendMessage maps IOException to AppError Network`() =
        runTest {
            coEvery { service.sendMessage("conv-1", SendMessageRequestDto("Xin chào")) } returns
                ApiResult.Exception(IOException("timeout"))

            val result = sut.sendMessage("conv-1", "Xin chào")

            assertEquals(Result.Failure(AppError.Network), result)
        }
}
