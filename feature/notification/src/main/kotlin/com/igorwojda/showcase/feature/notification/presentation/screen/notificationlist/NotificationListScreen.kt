@file:Suppress("CognitiveComplexMethod", "LabeledExpression", "LongMethod", "LongParameterList", "MagicNumber", "UnusedImports")

package com.igorwojda.showcase.feature.notification.presentation.screen.notificationlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.notification.domain.model.NotificationCategory
import com.igorwojda.showcase.feature.notification.domain.model.NotificationItem
import org.koin.androidx.compose.koinViewModel

/** Route wrapper for Notification Center (S12 / NOTIF-01..03). */
@Composable
fun NotificationListRoute(
    onBackClick: () -> Unit,
    onNavigateByTarget: (targetType: String, targetId: String) -> Unit,
    modifier: Modifier = Modifier,
    userName: String = "ArenaFlow",
    onProfileClick: () -> Unit = {},
    viewModel: NotificationListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val currentOnNavigate by androidx.compose.runtime.rememberUpdatedState(onNavigateByTarget)

    LaunchedEffect(uiState.pendingNavigation) {
        val target = uiState.pendingNavigation ?: return@LaunchedEffect
        currentOnNavigate(target.targetType, target.targetId)
        viewModel.onEvent(NotificationListEvent.ConsumeNavigation)
    }

    NotificationListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onProfileClick = onProfileClick,
        userName = userName,
        modifier = modifier,
    )
}

/** Notification Center screen (S12) displaying in-app notifications with filter tabs and deep links. */
@Composable
fun NotificationListScreen(
    uiState: NotificationListUiState,
    onEvent: (NotificationListEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    userName: String = "ArenaFlow",
    onProfileClick: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnEvent by androidx.compose.runtime.rememberUpdatedState(onEvent)

    LaunchedEffect(uiState.actionMessage) {
        val message = uiState.actionMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        currentOnEvent(NotificationListEvent.DismissActionMessage)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ArenaTopBar(
                title = "Thông báo",
                showBack = true,
                userName = userName,
                unreadNotificationCount = uiState.unreadCount,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Dimen.spaceL),
        ) {
            NotificationHeaderActions(
                selectedFilter = uiState.filter,
                unreadCount = uiState.unreadCount,
                isMarkingAllRead = uiState.isMarkingAllRead,
                onSelectFilter = { onEvent(NotificationListEvent.SelectFilter(it)) },
                onMarkAllRead = { onEvent(NotificationListEvent.MarkAllRead) },
            )

            NotificationListBody(
                uiState = uiState,
                onEvent = onEvent,
                onBackClick = onBackClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NotificationHeaderActions(
    selectedFilter: NotificationFilter,
    unreadCount: Int,
    isMarkingAllRead: Boolean,
    onSelectFilter: (NotificationFilter) -> Unit,
    onMarkAllRead: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NotificationFilter.entries.forEach { filter ->
                val isSelected = filter == selectedFilter
                val label =
                    if (filter == NotificationFilter.UNREAD && unreadCount > 0) {
                        "${filter.label} ($unreadCount)"
                    } else {
                        filter.label
                    }
                NotificationFilterChip(
                    label = label,
                    selected = isSelected,
                    onClick = { onSelectFilter(filter) },
                )
            }
        }

        TextButton(
            onClick = onMarkAllRead,
            enabled = unreadCount > 0 && !isMarkingAllRead,
        ) {
            Icon(
                imageVector = Icons.Outlined.DoneAll,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "Đọc tất cả",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun NotificationFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .clip(ArenaShapes.Chip)
                .clickable(onClick = onClick),
        shape = ArenaShapes.Chip,
        color = if (selected) ArenaColors.ChipActiveBg else ArenaColors.ChipInactiveBg,
        border =
            BorderStroke(
                width = 1.dp,
                color = if (selected) ArenaColors.Primary else ArenaColors.Border,
            ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) ArenaColors.ChipActiveText else ArenaColors.ChipInactiveText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun NotificationListBody(
    uiState: NotificationListUiState,
    onEvent: (NotificationListEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                ArenaLoadingState(message = "Đang tải thông báo...")
            }
        }

        uiState.errorMessage != null && uiState.items.isEmpty() -> {
            Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                ArenaErrorState(
                    title = "Không thể tải thông báo",
                    message = uiState.errorMessage,
                    errorCode = uiState.errorCode,
                    onBackClick = onBackClick,
                    onRetryClick = { onEvent(NotificationListEvent.Refresh) },
                )
            }
        }

        uiState.items.isEmpty() -> {
            val isUnreadFilter = uiState.filter == NotificationFilter.UNREAD
            Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                ArenaEmptyState(
                    title = if (isUnreadFilter) "Không có thông báo chưa đọc" else "Chưa có thông báo nào",
                    description =
                        if (isUnreadFilter) {
                            "Bạn đã đọc hết các thông báo trong 30 ngày qua."
                        } else {
                            "Thông báo về kèo đấu, thương lượng, đá lẻ và uy tín sẽ xuất hiện tại đây."
                        },
                    icon = Icons.Outlined.NotificationsOff,
                    actionLabel = if (isUnreadFilter) "Xem tất cả" else null,
                    onAction = { onEvent(NotificationListEvent.SelectFilter(NotificationFilter.ALL)) },
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(key = "retention-notice") {
                    InlineNotice(
                        text = "Thông báo được lưu trữ trong 30 ngày gần nhất.",
                    )
                }

                items(
                    items = uiState.items,
                    key = { it.id },
                ) { item ->
                    NotificationCard(
                        item = item,
                        onClick = { onEvent(NotificationListEvent.ClickNotification(item)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit,
) {
    val containerColor = if (item.isRead) ArenaColors.CardBg else ArenaColors.NoticeBg
    val borderColor = if (item.isRead) ArenaColors.Border else ArenaColors.Primary.copy(alpha = 0.35f)
    val style = categoryStyle(item.category)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(style.iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = item.category.label,
                    tint = style.iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppBadge(
                        text = item.category.label,
                        variant = style.badgeVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = formatNotificationTimestamp(item.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = ArenaColors.TextMuted,
                        )
                        if (!item.isRead) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ArenaColors.Primary),
                            )
                        }
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextPrimary,
                )
            }
        }
    }
}

private data class CategoryVisualStyle(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBgColor: Color,
    val badgeVariant: BadgeVariant,
)

private fun categoryStyle(category: NotificationCategory): CategoryVisualStyle =
    when (category) {
        NotificationCategory.MATCH -> {
            CategoryVisualStyle(
                icon = Icons.Outlined.SportsSoccer,
                iconTint = ArenaColors.Primary,
                iconBgColor = ArenaColors.BadgeGreenBg,
                badgeVariant = BadgeVariant.Primary,
            )
        }

        NotificationCategory.PAIRING -> {
            CategoryVisualStyle(
                icon = Icons.Outlined.Handshake,
                iconTint = ArenaColors.Info,
                iconBgColor = ArenaColors.InfoBg,
                badgeVariant = BadgeVariant.Info,
            )
        }

        NotificationCategory.MERCENARY -> {
            CategoryVisualStyle(
                icon = Icons.Outlined.Groups,
                iconTint = ArenaColors.Warning,
                iconBgColor = ArenaColors.WarningBg,
                badgeVariant = BadgeVariant.Warning,
            )
        }

        NotificationCategory.REPUTATION -> {
            CategoryVisualStyle(
                icon = Icons.Outlined.VerifiedUser,
                iconTint = ArenaColors.Success,
                iconBgColor = ArenaColors.SuccessBg,
                badgeVariant = BadgeVariant.Success,
            )
        }

        NotificationCategory.SYSTEM -> {
            CategoryVisualStyle(
                icon = Icons.Outlined.NotificationsNone,
                iconTint = ArenaColors.TextMuted,
                iconBgColor = ArenaColors.SurfaceContainer,
                badgeVariant = BadgeVariant.Neutral,
            )
        }
    }

private fun formatNotificationTimestamp(createdAt: String): String {
    val clean = createdAt.trim()
    if (clean.length >= 16 && clean.contains("T")) {
        val date = clean.substring(0, 10)
        val time = clean.substring(11, 16)
        val parts = date.split("-")
        if (parts.size == 3) {
            return "$time · ${parts[2]}/${parts[1]}"
        }
    }
    return clean
}

@PreviewLightDark
@Composable
private fun NotificationListScreenPreview() {
    ShowcaseTheme {
        NotificationListScreen(
            uiState =
                NotificationListUiState(
                    isLoading = false,
                    unreadCount = 2,
                    items =
                        listOf(
                            NotificationItem(
                                id = "1",
                                type = "MATCH_REQUEST_RECEIVED",
                                category = NotificationCategory.MATCH,
                                title = "Yêu cầu ghép kèo mới",
                                body = "Sài Gòn United đã gửi yêu cầu cho kèo ngày 21/11 tại Sân An Phú.",
                                referenceType = "MATCH",
                                referenceId = "match-1",
                                isRead = false,
                                createdAt = "2026-11-18T16:30:00",
                            ),
                            NotificationItem(
                                id = "2",
                                type = "TEAM_INVITATION",
                                category = NotificationCategory.SYSTEM,
                                title = "Lời mời gia nhập đội",
                                body = "Bạn nhận được lời mời tham gia Thảo Điền FC.",
                                referenceType = "TEAM_INVITATION",
                                referenceId = "inv-1",
                                isRead = true,
                                createdAt = "2026-11-17T21:00:00",
                            ),
                        ),
                ),
            onEvent = {},
            onBackClick = {},
        )
    }
}
