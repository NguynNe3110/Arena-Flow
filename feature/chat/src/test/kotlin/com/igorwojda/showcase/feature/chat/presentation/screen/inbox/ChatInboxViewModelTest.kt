package com.igorwojda.showcase.feature.chat.presentation.screen.inbox

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatInboxFilter
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomSummary
import com.igorwojda.showcase.feature.chat.domain.usecase.GetChatRoomsUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class ChatInboxViewModelTest {
    private val getChatRoomsUseCase: GetChatRoomsUseCase = mockk()
    private val sut = ChatInboxViewModel(getChatRoomsUseCase)

    @Test
    fun `Load populates rooms and SelectFilter filters by context`() =
        runTest {
            val matchRoom = sampleRoom(roomId = "conv-1", contextType = ChatContextType.MATCH)
            val pairingRoom = sampleRoom(roomId = "conv-2", contextType = ChatContextType.PAIRING)
            val mercRoom = sampleRoom(roomId = "conv-3", contextType = ChatContextType.MERCENARY)
            coEvery { getChatRoomsUseCase(null) } returns Result.Success(listOf(matchRoom, pairingRoom, mercRoom))

            sut.onEvent(ChatInboxEvent.Load)
            advanceUntilIdle()

            assertFalse(sut.uiStateFlow.value.isLoading)
            assertEquals(3, sut.uiStateFlow.value.filteredRooms.size)

            sut.onEvent(ChatInboxEvent.SelectFilter(ChatInboxFilter.TEAM_MATCH))
            assertEquals(listOf(matchRoom, pairingRoom), sut.uiStateFlow.value.filteredRooms)

            sut.onEvent(ChatInboxEvent.SelectFilter(ChatInboxFilter.MERCENARY))
            assertEquals(listOf(mercRoom), sut.uiStateFlow.value.filteredRooms)
        }

    @Test
    fun `Load sets error state when use case fails`() =
        runTest {
            coEvery { getChatRoomsUseCase(null) } returns Result.Failure(AppError.Network)

            sut.onEvent(ChatInboxEvent.Load)
            advanceUntilIdle()

            assertFalse(sut.uiStateFlow.value.isLoading)
            assertEquals(AppError.Network, sut.uiStateFlow.value.error)
        }

    private fun sampleRoom(
        roomId: String,
        contextType: ChatContextType,
    ) = ChatRoomSummary(
        roomId = roomId,
        title = "Phòng $roomId",
        contextType = contextType,
        contextId = "ctx-$roomId",
        otherPartyName = "FC Beta",
        otherPartyShortName = "FB",
        lastMessagePreview = "Chào bạn",
        lastMessageAt = "2026-11-18T10:00:00",
        unreadCount = 1,
        canSend = true,
    )
}
