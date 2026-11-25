package com.igorwojda.showcase.feature.notification.presentation.screen.notificationlist

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.notification.domain.model.NotificationCategory
import com.igorwojda.showcase.feature.notification.domain.model.NotificationItem
import com.igorwojda.showcase.feature.notification.domain.model.NotificationPage
import com.igorwojda.showcase.feature.notification.domain.usecase.GetNotificationsUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.GetUnreadNotificationCountUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.MarkAllNotificationsReadUseCase
import com.igorwojda.showcase.feature.notification.domain.usecase.MarkNotificationReadUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationListViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val getNotificationsUseCase: GetNotificationsUseCase = mockk()
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase = mockk()
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase = mockk()
    private val markNotificationReadUseCase: MarkNotificationReadUseCase = mockk()

    private lateinit var sut: NotificationListViewModel

    private val unreadItem =
        NotificationItem(
            id = "notif-1",
            type = "MATCH_REQUEST_RECEIVED",
            category = NotificationCategory.MATCH,
            title = "Yêu cầu ghép kèo mới",
            body = "Đội A muốn ghép kèo.",
            referenceType = "MATCH",
            referenceId = "match-1",
            isRead = false,
            createdAt = "2026-11-18T16:30:00",
        )

    private val readItem =
        NotificationItem(
            id = "notif-2",
            type = "WELCOME",
            category = NotificationCategory.SYSTEM,
            title = "Chào mừng",
            body = "Chào mừng đến ArenaFlow.",
            referenceType = null,
            referenceId = null,
            isRead = true,
            createdAt = "2026-11-17T10:00:00",
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized then loads notifications and unread count`() =
        runTest {
            coEvery { getNotificationsUseCase(page = 1, pageSize = 50, unreadOnly = false) } returns
                Result.Success(
                    NotificationPage(
                        items = listOf(unreadItem, readItem),
                        page = 1,
                        pageSize = 50,
                        totalPages = 1,
                        totalItems = 2,
                    ),
                )
            coEvery { getUnreadNotificationCountUseCase() } returns Result.Success(1)

            sut = createSut()

            val state = sut.uiStateFlow.value
            state.isLoading shouldBeEqualTo false
            state.items shouldBeEqualTo listOf(unreadItem, readItem)
            state.unreadCount shouldBeEqualTo 1
            state.errorMessage shouldBeEqualTo null
        }

    @Test
    fun `when unread filter selected then reloads with unreadOnly true`() =
        runTest {
            coEvery { getNotificationsUseCase(page = 1, pageSize = 50, unreadOnly = false) } returns
                Result.Success(NotificationPage(listOf(unreadItem, readItem), 1, 50, 1, 2))
            coEvery { getNotificationsUseCase(page = 1, pageSize = 50, unreadOnly = true) } returns
                Result.Success(NotificationPage(listOf(unreadItem), 1, 50, 1, 1))
            coEvery { getUnreadNotificationCountUseCase() } returns Result.Success(1)

            sut = createSut()
            sut.onEvent(NotificationListEvent.SelectFilter(NotificationFilter.UNREAD))

            val state = sut.uiStateFlow.value
            state.filter shouldBeEqualTo NotificationFilter.UNREAD
            state.items shouldBeEqualTo listOf(unreadItem)
            coVerify { getNotificationsUseCase(page = 1, pageSize = 50, unreadOnly = true) }
        }

    @Test
    fun `when clicking unread notification with target then marks read and emits navigation`() =
        runTest {
            coEvery { getNotificationsUseCase(page = 1, pageSize = 50, unreadOnly = false) } returns
                Result.Success(NotificationPage(listOf(unreadItem), 1, 50, 1, 1))
            coEvery { getUnreadNotificationCountUseCase() } returns Result.Success(1)
            coEvery { markNotificationReadUseCase("notif-1") } returns Result.Success(Unit)

            sut = createSut()
            sut.onEvent(NotificationListEvent.ClickNotification(unreadItem))

            val state = sut.uiStateFlow.value
            state.items.first().isRead shouldBeEqualTo true
            state.unreadCount shouldBeEqualTo 0
            state.pendingNavigation shouldBeEqualTo NotificationNavigationTarget("MATCH", "match-1")

            sut.onEvent(NotificationListEvent.ConsumeNavigation)
            sut.uiStateFlow.value.pendingNavigation shouldBeEqualTo null
        }

    @Test
    fun `when mark all read succeeds then marks all items read and resets count`() =
        runTest {
            coEvery { getNotificationsUseCase(page = 1, pageSize = 50, unreadOnly = false) } returns
                Result.Success(NotificationPage(listOf(unreadItem, readItem), 1, 50, 1, 2))
            coEvery { getUnreadNotificationCountUseCase() } returns Result.Success(1)
            coEvery { markAllNotificationsReadUseCase() } returns Result.Success(Unit)

            sut = createSut()
            sut.onEvent(NotificationListEvent.MarkAllRead)

            val state = sut.uiStateFlow.value
            state.unreadCount shouldBeEqualTo 0
            state.items.all { it.isRead } shouldBeEqualTo true
        }

    @Test
    fun `when loading fails then exposes error message`() =
        runTest {
            coEvery { getNotificationsUseCase(page = 1, pageSize = 50, unreadOnly = false) } returns
                Result.Failure(AppError.Network)
            coEvery { getUnreadNotificationCountUseCase() } returns Result.Failure(AppError.Network)

            sut = createSut()

            val state = sut.uiStateFlow.value
            state.isLoading shouldBeEqualTo false
            state.errorMessage.shouldBeEqualTo(AppError.Network.userMessage())
        }

    private fun createSut(): NotificationListViewModel =
        NotificationListViewModel(
            getNotificationsUseCase = getNotificationsUseCase,
            getUnreadNotificationCountUseCase = getUnreadNotificationCountUseCase,
            markAllNotificationsReadUseCase = markAllNotificationsReadUseCase,
            markNotificationReadUseCase = markNotificationReadUseCase,
        )
}
