@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import org.koin.androidx.compose.koinViewModel

/** Entry screen for the ArenaFlow Admin Console (S14, ADM-01..09). */
@Composable
fun AdminConsoleScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: AdminConsoleViewModel = koinViewModel()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    AdminConsoleContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
internal fun AdminConsoleContent(
    state: AdminConsoleUiState,
    onEvent: (AdminConsoleEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = "Quản trị ArenaFlow",
                showBack = true,
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            AdminTabBar(
                selectedTab = state.selectedTab,
                onSelectTab = { onEvent(AdminConsoleEvent.SelectTab(it)) },
            )

            state.feedbackMessage?.let { message ->
                FeedbackBanner(
                    message = message,
                    onDismiss = { onEvent(AdminConsoleEvent.DismissFeedback) },
                )
            }

            AdminConsoleBody(
                state = state,
                onEvent = onEvent,
                onBackClick = onBackClick,
            )
        }
    }

    state.activeDialog?.let { dialog ->
        AdminConsoleDialogHost(
            dialog = dialog,
            isSubmitting = state.isSubmitting,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun AdminTabBar(
    selectedTab: AdminConsoleTab,
    onSelectTab: (AdminConsoleTab) -> Unit,
) {
    val tabs = AdminConsoleTab.entries
    val selectedIndex = tabs.indexOf(selectedTab)
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.White,
        contentColor = ArenaColors.Primary,
        edgePadding = 12.dp,
        indicator = { tabPositions ->
            if (selectedIndex in tabPositions.indices) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = ArenaColors.Primary,
                )
            }
        },
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            Tab(
                selected = selected,
                onClick = { onSelectTab(tab) },
                text = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) ArenaColors.Primary else ArenaColors.TextMuted,
                    )
                },
            )
        }
    }
}

@Composable
private fun FeedbackBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(ArenaColors.SuccessBg)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = ArenaColors.Success,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onDismiss) {
            Text(text = "Đóng", color = ArenaColors.Success)
        }
    }
}

@Composable
private fun AdminConsoleBody(
    state: AdminConsoleUiState,
    onEvent: (AdminConsoleEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArenaColors.Primary)
            }
        }

        state.error != null -> {
            val (title, message, code) = resolveErrorCopy(state.error)
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                ArenaErrorState(
                    title = title,
                    message = message,
                    errorCode = code,
                    onBackClick = onBackClick,
                    onRetryClick = { onEvent(AdminConsoleEvent.Refresh) },
                )
            }
        }

        else -> {
            when (state.selectedTab) {
                AdminConsoleTab.REPORTS -> ReportsQueueTab(state = state, onEvent = onEvent)
                AdminConsoleTab.VENUES -> PendingVenuesTab(venues = state.pendingVenues, onEvent = onEvent)
                AdminConsoleTab.USERS -> UsersSanctionsTab(state = state, onEvent = onEvent)
                AdminConsoleTab.AUDIT_LOGS -> AuditLogsTab(logs = state.auditLogs)
            }
        }
    }
}

private fun resolveErrorCopy(error: AppError): Triple<String, String, String?> =
    when (error) {
        AppError.Unauthorized -> {
            Triple("Không có quyền truy cập", "Chỉ quản trị viên (ADMIN) mới có thể mở trang quản trị.", "AUTH_004")
        }

        AppError.NotFound -> {
            Triple("Không tìm thấy dữ liệu", "Tài nguyên quản trị không tồn tại hoặc đã bị xóa.", "SYS_001")
        }

        AppError.Network -> {
            Triple("Mất kết nối mạng", "Vui lòng kiểm tra kết nối và thử lại.", null)
        }

        AppError.Server -> {
            Triple("Lỗi hệ thống", "Máy chủ tạm thời không phản hồi.", "SYS_005")
        }

        is AppError.DomainError -> {
            Triple("Không thể thực hiện", error.message, error.code)
        }

        AppError.Unknown -> {
            Triple("Đã xảy ra lỗi", "Không thể tải dữ liệu quản trị.", null)
        }
    }
