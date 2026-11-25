@file:Suppress("CognitiveComplexMethod", "LongMethod", "MagicNumber")

package com.igorwojda.showcase.feature.chat.presentation.screen.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.composable.PitchBrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.composable.TeamAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatInboxFilter
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomSummary
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

/** Chat inbox tab screen (S10 / CHAT-01). */
@Composable
fun ChatInboxScreen(
    onRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ChatInboxViewModel = koinViewModel()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatInboxEvent.Load)
    }

    ChatInboxContent(
        state = state,
        onEvent = viewModel::onEvent,
        onRoomClick = onRoomClick,
        modifier = modifier,
    )
}

@Composable
internal fun ChatInboxContent(
    state: ChatInboxUiState,
    onEvent: (ChatInboxEvent) -> Unit,
    onRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ArenaColors.Background),
    ) {
        PitchBrandHeader(
            title = "Tin nhắn",
            compact = true,
            trailingContent = {
                IconButton(
                    onClick = { onEvent(ChatInboxEvent.Refresh) },
                    enabled = !state.isRefreshing,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Làm mới",
                        tint = Color.White,
                    )
                }
            },
        )

        InboxFilterRow(
            selectedFilter = state.selectedFilter,
            onSelectFilter = { onEvent(ChatInboxEvent.SelectFilter(it)) },
        )

        when {
            state.isLoading -> {
                LoadingIndicator(modifier = Modifier.weight(1f))
            }

            state.error != null && state.allRooms.isEmpty() -> {
                InboxErrorState(
                    error = state.error,
                    onRetry = { onEvent(ChatInboxEvent.Load) },
                    modifier = Modifier.weight(1f),
                )
            }

            state.filteredRooms.isEmpty() -> {
                InboxEmptyState(
                    hasAnyRooms = state.allRooms.isNotEmpty(),
                    onResetFilter = { onEvent(ChatInboxEvent.SelectFilter(ChatInboxFilter.ALL)) },
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = state.filteredRooms,
                        key = { it.roomId },
                    ) { room ->
                        ChatRoomCard(
                            room = room,
                            onClick = { onRoomClick(room.roomId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InboxFilterRow(
    selectedFilter: ChatInboxFilter,
    onSelectFilter: (ChatInboxFilter) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChatInboxFilter.entries.forEach { filter ->
            val selected = filter == selectedFilter
            FilterChip(
                selected = selected,
                onClick = { onSelectFilter(filter) },
                label = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                },
                shape = ArenaShapes.Chip,
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArenaColors.ChipActiveBg,
                        selectedLabelColor = ArenaColors.ChipActiveText,
                        containerColor = ArenaColors.ChipInactiveBg,
                        labelColor = ArenaColors.ChipInactiveText,
                    ),
                border = null,
            )
        }
    }
}

@Composable
private fun ChatRoomCard(
    room: ChatRoomSummary,
    onClick: () -> Unit,
) {
    val hasUnread = room.unreadCount > 0
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(ArenaShapes.Card)
                .border(
                    width = 1.dp,
                    color = if (hasUnread) ArenaColors.Primary.copy(alpha = 0.35f) else ArenaColors.Border,
                    shape = ArenaShapes.Card,
                ).clickable(onClick = onClick),
        shape = ArenaShapes.Card,
        color = ArenaColors.CardBg,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(
                name = room.otherPartyShortName.ifBlank { room.otherPartyName },
                size = 46.dp,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = room.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = ArenaColors.TextHeading,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatChatTimestamp(room.lastMessageAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasUnread) ArenaColors.Primary else ArenaColors.TextMuted,
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = room.lastMessagePreview ?: "Chưa có tin nhắn nào",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasUnread) ArenaColors.TextPrimary else ArenaColors.TextMuted,
                        fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (hasUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        UnreadBadge(count = room.unreadCount)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContextTagPill(contextType = room.contextType)
                    if (!room.canSend) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = ArenaColors.TextMuted,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = "Chỉ xem",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArenaColors.TextMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val label = if (count > 99) "99+" else count.toString()
    Box(
        modifier =
            Modifier
                .defaultMinSizeBadge()
                .clip(CircleShape)
                .background(ArenaColors.ErrorBright)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun Modifier.defaultMinSizeBadge(): Modifier = this.size(width = 22.dp, height = 20.dp)

@Composable
private fun ContextTagPill(contextType: ChatContextType) {
    val (bg, fg) =
        when (contextType) {
            ChatContextType.MATCH -> ArenaColors.BadgeGreenBg to ArenaColors.BadgeGreenText
            ChatContextType.PAIRING -> ArenaColors.InfoBg to ArenaColors.Info
            ChatContextType.MERCENARY -> ArenaColors.BadgeAmberBg to ArenaColors.BadgeAmberText
        }
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = contextType.label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun InboxEmptyState(
    hasAnyRooms: Boolean,
    onResetFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ArenaColors.SurfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Forum,
                    contentDescription = null,
                    tint = ArenaColors.Primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (hasAnyRooms) "Không có hội thoại phù hợp" else "Chưa có tin nhắn nào",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text =
                    if (hasAnyRooms) {
                        "Thử chuyển sang bộ lọc Tất cả để xem các cuộc trò chuyện khác."
                    } else {
                        "Khi bạn ghép kèo hoặc ứng tuyển đá lẻ, cuộc trò chuyện sẽ xuất hiện tại đây."
                    },
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
                textAlign = TextAlign.Center,
            )
            if (hasAnyRooms) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(onClick = onResetFilter) {
                    Text("Xem tất cả")
                }
            }
        }
    }
}

@Composable
private fun InboxErrorState(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message =
        when (error) {
            is AppError.DomainError -> error.message
            AppError.Network -> "Không có kết nối mạng. Vui lòng thử lại."
            AppError.Unauthorized -> "Bạn cần đăng nhập để xem tin nhắn."
            else -> "Không thể tải danh sách tin nhắn."
        }
    Box(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Không thể tải hội thoại",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.Error,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.Primary),
            ) {
                Text("Thử lại")
            }
        }
    }
}

private fun formatChatTimestamp(iso: String): String {
    val parsed = VietnamDateTime.parseToLocalDateTime(iso) ?: return iso
    return parsed.format(DateTimeFormatter.ofPattern("HH:mm · dd/MM"))
}
