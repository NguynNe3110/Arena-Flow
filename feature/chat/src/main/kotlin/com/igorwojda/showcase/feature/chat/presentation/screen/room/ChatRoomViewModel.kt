package com.igorwojda.showcase.feature.chat.presentation.screen.room

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessage
import com.igorwojda.showcase.feature.chat.domain.model.ChatReportReason
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomDetail
import com.igorwojda.showcase.feature.chat.domain.usecase.GetChatMessagesUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.GetChatRoomDetailUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.MarkChatReadUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.ReportChatUseCase
import com.igorwojda.showcase.feature.chat.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.launch

/** UI state for ChatRoomScreen (CHAT-02..05). */
internal data class ChatRoomUiState(
    val roomId: String = "",
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isSending: Boolean = false,
    val room: ChatRoomDetail? = null,
    val messages: List<ChatMessage> = emptyList(),
    val hasMore: Boolean = false,
    val draftText: String = "",
    val error: AppError? = null,
    val sendError: String? = null,
    val showReportSheet: Boolean = false,
    val isSubmittingReport: Boolean = false,
    val reportMessage: String? = null,
    val reportError: String? = null,
) : BaseState

/** User intents for ChatRoomScreen. */
internal sealed interface ChatRoomEvent {
    data class Load(
        val roomId: String,
    ) : ChatRoomEvent

    data class UpdateDraft(
        val text: String,
    ) : ChatRoomEvent

    data class SelectQuickTemplate(
        val template: String,
    ) : ChatRoomEvent

    data object SendMessage : ChatRoomEvent

    data object LoadMoreMessages : ChatRoomEvent

    data object OpenReportSheet : ChatRoomEvent

    data object DismissReportSheet : ChatRoomEvent

    data class SubmitReport(
        val reason: ChatReportReason,
        val details: String,
    ) : ChatRoomEvent
}

/** State reducers for [ChatRoomViewModel]. */
internal sealed interface ChatRoomAction : BaseAction<ChatRoomUiState> {
    data class Loading(
        val roomId: String,
    ) : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState =
            state.copy(
                roomId = roomId,
                isLoading = true,
                error = null,
                sendError = null,
            )
    }

    data class Loaded(
        val room: ChatRoomDetail,
        val messages: List<ChatMessage>,
        val hasMore: Boolean,
    ) : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState =
            state.copy(
                isLoading = false,
                room = room,
                messages = messages,
                hasMore = hasMore,
                error = null,
            )
    }

    data class LoadFailed(
        val error: AppError,
    ) : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState =
            state.copy(
                isLoading = false,
                error = error,
            )
    }

    data object LoadingMore : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState = state.copy(isLoadingMore = true)
    }

    data class PrependedMessages(
        val olderMessages: List<ChatMessage>,
        val hasMore: Boolean,
    ) : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState {
            val existingIds = state.messages.mapTo(HashSet()) { it.messageId }
            val uniqueOlder = olderMessages.filterNot { it.messageId in existingIds }
            return state.copy(
                isLoadingMore = false,
                messages = uniqueOlder + state.messages,
                hasMore = hasMore,
            )
        }
    }

    data class DraftUpdated(
        val text: String,
    ) : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState = state.copy(draftText = text, sendError = null)
    }

    data object Sending : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState = state.copy(isSending = true, sendError = null)
    }

    data class MessageSent(
        val message: ChatMessage,
    ) : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState =
            state.copy(
                isSending = false,
                draftText = "",
                messages = state.messages + message,
                sendError = null,
            )
    }

    data class SendFailed(
        val message: String,
    ) : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState = state.copy(isSending = false, sendError = message)
    }

    data class ReportSheetVisibility(
        val visible: Boolean,
    ) : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState =
            state.copy(
                showReportSheet = visible,
                reportError = if (visible) null else state.reportError,
            )
    }

    data object SubmittingReport : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState = state.copy(isSubmittingReport = true, reportError = null)
    }

    data object ReportSucceeded : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState =
            state.copy(
                isSubmittingReport = false,
                showReportSheet = false,
                reportMessage = "Đã gửi báo cáo hội thoại. Đội ngũ sẽ xem xét.",
                reportError = null,
            )
    }

    data class ReportFailed(
        val message: String,
    ) : ChatRoomAction {
        override fun reduce(state: ChatRoomUiState): ChatRoomUiState = state.copy(isSubmittingReport = false, reportError = message)
    }
}

/** ViewModel for ChatRoomScreen (CHAT-02..05). */
internal class ChatRoomViewModel(
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val getChatRoomDetailUseCase: GetChatRoomDetailUseCase,
    private val markChatReadUseCase: MarkChatReadUseCase,
    private val reportChatUseCase: ReportChatUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
) : BaseViewModel<ChatRoomUiState, ChatRoomAction>(ChatRoomUiState()) {
    fun onEvent(event: ChatRoomEvent) {
        when (event) {
            is ChatRoomEvent.Load -> loadRoom(event.roomId)
            is ChatRoomEvent.UpdateDraft -> sendAction(ChatRoomAction.DraftUpdated(event.text))
            is ChatRoomEvent.SelectQuickTemplate -> sendAction(ChatRoomAction.DraftUpdated(event.template))
            ChatRoomEvent.SendMessage -> submitMessage()
            ChatRoomEvent.LoadMoreMessages -> loadMoreMessages()
            ChatRoomEvent.OpenReportSheet -> sendAction(ChatRoomAction.ReportSheetVisibility(visible = true))
            ChatRoomEvent.DismissReportSheet -> sendAction(ChatRoomAction.ReportSheetVisibility(visible = false))
            is ChatRoomEvent.SubmitReport -> submitReport(event.reason, event.details)
        }
    }

    private fun loadRoom(roomId: String) {
        sendAction(ChatRoomAction.Loading(roomId))
        viewModelScope.launch {
            when (val detailResult = getChatRoomDetailUseCase(roomId)) {
                is Result.Failure -> {
                    sendAction(ChatRoomAction.LoadFailed(detailResult.error))
                }

                is Result.Success -> {
                    when (val msgResult = getChatMessagesUseCase(roomId = roomId)) {
                        is Result.Failure -> {
                            sendAction(ChatRoomAction.LoadFailed(msgResult.error))
                        }

                        is Result.Success -> {
                            sendAction(
                                ChatRoomAction.Loaded(
                                    room = detailResult.value,
                                    messages = msgResult.value.items,
                                    hasMore = msgResult.value.hasMore,
                                ),
                            )
                            markChatReadUseCase(roomId)
                        }
                    }
                }
            }
        }
    }

    private fun loadMoreMessages() {
        val current = uiStateFlow.value
        val firstMessageId = current.messages.firstOrNull()?.messageId ?: return
        if (current.isLoadingMore || !current.hasMore) return

        sendAction(ChatRoomAction.LoadingMore)
        viewModelScope.launch {
            when (val result = getChatMessagesUseCase(roomId = current.roomId, cursor = firstMessageId)) {
                is Result.Success -> {
                    sendAction(
                        ChatRoomAction.PrependedMessages(
                            olderMessages = result.value.items,
                            hasMore = result.value.hasMore,
                        ),
                    )
                }

                is Result.Failure -> {
                    sendAction(ChatRoomAction.PrependedMessages(olderMessages = emptyList(), hasMore = false))
                }
            }
        }
    }

    private fun submitMessage() {
        val current = uiStateFlow.value
        val content = current.draftText.trim()
        if (content.isEmpty() || current.isSending || current.room?.canSend != true) return

        sendAction(ChatRoomAction.Sending)
        viewModelScope.launch {
            when (val result = sendChatMessageUseCase(roomId = current.roomId, content = content)) {
                is Result.Success -> sendAction(ChatRoomAction.MessageSent(result.value))
                is Result.Failure -> sendAction(ChatRoomAction.SendFailed(result.error.toReadableMessage()))
            }
        }
    }

    private fun submitReport(
        reason: ChatReportReason,
        details: String,
    ) {
        val current = uiStateFlow.value
        if (current.isSubmittingReport || current.roomId.isBlank()) return

        sendAction(ChatRoomAction.SubmittingReport)
        viewModelScope.launch {
            when (val result = reportChatUseCase(roomId = current.roomId, reason = reason, details = details)) {
                is Result.Success -> sendAction(ChatRoomAction.ReportSucceeded)
                is Result.Failure -> sendAction(ChatRoomAction.ReportFailed(result.error.toReadableMessage()))
            }
        }
    }

    private fun AppError.toReadableMessage(): String =
        when (this) {
            is AppError.DomainError -> message
            AppError.Network -> "Mất kết nối mạng, vui lòng thử lại."
            AppError.Unauthorized -> "Bạn không có quyền thực hiện thao tác này."
            else -> "Đã xảy ra lỗi, vui lòng thử lại."
        }
}
