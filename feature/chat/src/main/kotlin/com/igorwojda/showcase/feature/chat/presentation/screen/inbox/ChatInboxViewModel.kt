package com.igorwojda.showcase.feature.chat.presentation.screen.inbox

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatInboxFilter
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomSummary
import com.igorwojda.showcase.feature.chat.domain.usecase.GetChatRoomsUseCase
import kotlinx.coroutines.launch

/** UI state for ChatInboxScreen (CHAT-01). */
internal data class ChatInboxUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedFilter: ChatInboxFilter = ChatInboxFilter.ALL,
    val allRooms: List<ChatRoomSummary> = emptyList(),
    val filteredRooms: List<ChatRoomSummary> = emptyList(),
    val error: AppError? = null,
) : BaseState

/** User intents for ChatInboxScreen. */
internal sealed interface ChatInboxEvent {
    data object Load : ChatInboxEvent

    data object Refresh : ChatInboxEvent

    data class SelectFilter(
        val filter: ChatInboxFilter,
    ) : ChatInboxEvent
}

/** State reducers for [ChatInboxViewModel]. */
internal sealed interface ChatInboxAction : BaseAction<ChatInboxUiState> {
    data class Loading(
        val isRefresh: Boolean,
    ) : ChatInboxAction {
        override fun reduce(state: ChatInboxUiState): ChatInboxUiState =
            state.copy(
                isLoading = !isRefresh && state.allRooms.isEmpty(),
                isRefreshing = isRefresh,
                error = null,
            )
    }

    data class Loaded(
        val rooms: List<ChatRoomSummary>,
    ) : ChatInboxAction {
        override fun reduce(state: ChatInboxUiState): ChatInboxUiState =
            state.copy(
                isLoading = false,
                isRefreshing = false,
                allRooms = rooms,
                filteredRooms = applyFilter(rooms, state.selectedFilter),
                error = null,
            )
    }

    data class Failed(
        val error: AppError,
    ) : ChatInboxAction {
        override fun reduce(state: ChatInboxUiState): ChatInboxUiState =
            state.copy(
                isLoading = false,
                isRefreshing = false,
                error = error,
            )
    }

    data class FilterChanged(
        val filter: ChatInboxFilter,
    ) : ChatInboxAction {
        override fun reduce(state: ChatInboxUiState): ChatInboxUiState =
            state.copy(
                selectedFilter = filter,
                filteredRooms = applyFilter(state.allRooms, filter),
            )
    }
}

private fun applyFilter(
    rooms: List<ChatRoomSummary>,
    filter: ChatInboxFilter,
): List<ChatRoomSummary> =
    when (filter) {
        ChatInboxFilter.ALL -> {
            rooms
        }

        ChatInboxFilter.TEAM_MATCH -> {
            rooms.filter { it.contextType == ChatContextType.MATCH || it.contextType == ChatContextType.PAIRING }
        }

        ChatInboxFilter.MERCENARY -> {
            rooms.filter { it.contextType == ChatContextType.MERCENARY }
        }
    }

/** ViewModel for ChatInboxScreen (CHAT-01). */
internal class ChatInboxViewModel(
    private val getChatRoomsUseCase: GetChatRoomsUseCase,
) : BaseViewModel<ChatInboxUiState, ChatInboxAction>(ChatInboxUiState()) {
    fun onEvent(event: ChatInboxEvent) {
        when (event) {
            ChatInboxEvent.Load -> loadRooms(isRefresh = false)
            ChatInboxEvent.Refresh -> loadRooms(isRefresh = true)
            is ChatInboxEvent.SelectFilter -> sendAction(ChatInboxAction.FilterChanged(event.filter))
        }
    }

    private fun loadRooms(isRefresh: Boolean) {
        sendAction(ChatInboxAction.Loading(isRefresh = isRefresh))
        viewModelScope.launch {
            when (val result = getChatRoomsUseCase()) {
                is Result.Success -> sendAction(ChatInboxAction.Loaded(result.value))
                is Result.Failure -> sendAction(ChatInboxAction.Failed(result.error))
            }
        }
    }
}
