package com.igorwojda.showcase.feature.notification.presentation.screen.notificationlist

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.notification.domain.model.NotificationItem
import com.igorwojda.showcase.feature.notification.domain.usecase.GetNotificationsUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.GetUnreadNotificationCountUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.MarkAllNotificationsReadUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.MarkNotificationReadUseCase
import kotlinx.coroutines.launch

/** Filter tab for the notification center. */
enum class NotificationFilter(
    val label: String,
) {
    ALL("Tất cả"),
    UNREAD("Chưa đọc"),
}

/** Pending deep-link navigation target resolved from a tapped notification. */
data class NotificationNavigationTarget(
    val targetType: String,
    val targetId: String,
)

/** UI state for [NotificationListScreen]. */
data class NotificationListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val filter: NotificationFilter = NotificationFilter.ALL,
    val items: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val errorMessage: String? = null,
    val errorCode: String? = null,
    val actionMessage: String? = null,
    val pendingNavigation: NotificationNavigationTarget? = null,
) : BaseState

/** User events dispatched from [NotificationListScreen]. */
sealed interface NotificationListEvent {
    data object Load : NotificationListEvent

    data object Refresh : NotificationListEvent

    data class SelectFilter(
        val filter: NotificationFilter,
    ) : NotificationListEvent

    data class ClickNotification(
        val item: NotificationItem,
    ) : NotificationListEvent

    data object MarkAllRead : NotificationListEvent

    data object ConsumeNavigation : NotificationListEvent

    data object DismissActionMessage : NotificationListEvent
}

/** State reducer actions for [NotificationListViewModel]. */
sealed interface NotificationListAction : BaseAction<NotificationListUiState> {
    data class Loading(
        val isRefresh: Boolean,
    ) : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState =
            state.copy(
                isLoading = !isRefresh && state.items.isEmpty(),
                isRefreshing = isRefresh,
                errorMessage = null,
                errorCode = null,
            )
    }

    data class FilterSelected(
        val filter: NotificationFilter,
    ) : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState = state.copy(filter = filter)
    }

    data class Loaded(
        val items: List<NotificationItem>,
        val unreadCount: Int,
    ) : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState =
            state.copy(
                isLoading = false,
                isRefreshing = false,
                items = items,
                unreadCount = unreadCount,
                errorMessage = null,
                errorCode = null,
            )
    }

    data class Failed(
        val message: String,
        val code: String?,
    ) : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState =
            state.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = message,
                errorCode = code,
            )
    }

    data class MarkingAllRead(
        val inProgress: Boolean,
    ) : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState = state.copy(isMarkingAllRead = inProgress)
    }

    data object MarkedAllRead : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState {
            val updatedItems =
                if (state.filter == NotificationFilter.UNREAD) {
                    emptyList()
                } else {
                    state.items.map { it.copy(isRead = true) }
                }
            return state.copy(
                isMarkingAllRead = false,
                items = updatedItems,
                unreadCount = 0,
                actionMessage = "Đã đánh dấu tất cả thông báo là đã đọc.",
            )
        }
    }

    data class MarkedSingleRead(
        val notificationId: String,
    ) : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState {
            val wasUnread = state.items.firstOrNull { it.id == notificationId }?.isRead == false
            val updatedItems =
                if (state.filter == NotificationFilter.UNREAD) {
                    state.items.filterNot { it.id == notificationId }
                } else {
                    state.items.map { if (it.id == notificationId) it.copy(isRead = true) else it }
                }
            return state.copy(
                items = updatedItems,
                unreadCount = if (wasUnread) (state.unreadCount - 1).coerceAtLeast(0) else state.unreadCount,
            )
        }
    }

    data class NavigateToTarget(
        val target: NotificationNavigationTarget,
    ) : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState = state.copy(pendingNavigation = target)
    }

    data object NavigationConsumed : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState = state.copy(pendingNavigation = null)
    }

    data class ActionFeedback(
        val message: String?,
    ) : NotificationListAction {
        override fun reduce(state: NotificationListUiState): NotificationListUiState =
            state.copy(isMarkingAllRead = false, actionMessage = message)
    }
}

/** ViewModel for the Notification Center screen (NOTIF-01..03). */
class NotificationListViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
) : BaseViewModel<NotificationListUiState, NotificationListAction>(NotificationListUiState()) {
    init {
        loadNotifications(isRefresh = false)
    }

    /** Handles all user events from the UI. */
    fun onEvent(event: NotificationListEvent) {
        when (event) {
            NotificationListEvent.Load -> {
                loadNotifications(isRefresh = false)
            }

            NotificationListEvent.Refresh -> {
                loadNotifications(isRefresh = true)
            }

            is NotificationListEvent.SelectFilter -> {
                if (uiStateFlow.value.filter != event.filter) {
                    sendAction(NotificationListAction.FilterSelected(event.filter))
                    loadNotifications(isRefresh = true)
                }
            }

            is NotificationListEvent.ClickNotification -> {
                handleNotificationClick(event.item)
            }

            NotificationListEvent.MarkAllRead -> {
                markAllRead()
            }

            NotificationListEvent.ConsumeNavigation -> {
                sendAction(NotificationListAction.NavigationConsumed)
            }

            NotificationListEvent.DismissActionMessage -> {
                sendAction(NotificationListAction.ActionFeedback(null))
            }
        }
    }

    private fun loadNotifications(isRefresh: Boolean) {
        viewModelScope.launch {
            sendAction(NotificationListAction.Loading(isRefresh = isRefresh))
            val unreadOnly = uiStateFlow.value.filter == NotificationFilter.UNREAD
            val listResult = getNotificationsUseCase(page = 1, pageSize = PAGE_SIZE, unreadOnly = unreadOnly)
            val countResult = getUnreadNotificationCountUseCase()

            when (listResult) {
                is Result.Success -> {
                    val unreadCount =
                        when (countResult) {
                            is Result.Success -> countResult.value
                            is Result.Failure -> listResult.value.items.count { !it.isRead }
                        }
                    sendAction(
                        NotificationListAction.Loaded(
                            items = listResult.value.items,
                            unreadCount = unreadCount,
                        ),
                    )
                }

                is Result.Failure -> {
                    val err = listResult.error
                    val code = (err as? AppError.DomainError)?.code
                    sendAction(
                        NotificationListAction.Failed(
                            message = err.userMessage("Không thể tải danh sách thông báo."),
                            code = code,
                        ),
                    )
                }
            }
        }
    }

    private fun handleNotificationClick(item: NotificationItem) {
        viewModelScope.launch {
            if (!item.isRead) {
                when (markNotificationReadUseCase(item.id)) {
                    is Result.Success -> {
                        sendAction(NotificationListAction.MarkedSingleRead(item.id))
                    }

                    is Result.Failure -> {
                        // Safe recovery: still allow deep-link navigation even if mark-read failed.
                    }
                }
            }

            val targetType = item.referenceType
            val targetId = item.referenceId
            if (!targetType.isNullOrBlank() && !targetId.isNullOrBlank()) {
                sendAction(
                    NotificationListAction.NavigateToTarget(
                        NotificationNavigationTarget(
                            targetType = targetType,
                            targetId = targetId,
                        ),
                    ),
                )
            } else {
                sendAction(
                    NotificationListAction.ActionFeedback(
                        "Thông báo này không có màn hình chi tiết liên kết.",
                    ),
                )
            }
        }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            sendAction(NotificationListAction.MarkingAllRead(inProgress = true))
            when (val result = markAllNotificationsReadUseCase()) {
                is Result.Success -> {
                    sendAction(NotificationListAction.MarkedAllRead)
                }

                is Result.Failure -> {
                    sendAction(
                        NotificationListAction.ActionFeedback(
                            result.error.userMessage("Không thể đánh dấu tất cả đã đọc."),
                        ),
                    )
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}
