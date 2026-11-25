@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList", "MagicNumber", "TooManyFunctions")

package com.igorwojda.showcase.feature.chat.presentation.screen.room

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.chat.domain.model.ChatContextType
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessage
import com.igorwojda.showcase.feature.chat.domain.model.ChatMessageSenderType
import com.igorwojda.showcase.feature.chat.domain.model.ChatReportReason
import com.igorwojda.showcase.feature.chat.domain.model.ChatRoomDetail
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

private val QuickTemplates =
    listOf(
        "Xác nhận sân?",
        "Đủ người rồi",
        "Chốt giờ nhé",
    )

/** Chat room screen (S11 / CHAT-02..05). */
@Composable
fun ChatRoomScreen(
    roomId: String,
    onBackClick: () -> Unit,
    onNavigateToMatch: (String) -> Unit = {},
    onNavigateToPairing: (String) -> Unit = {},
    onNavigateToMercenary: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: ChatRoomViewModel = koinViewModel()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(roomId) {
        viewModel.onEvent(ChatRoomEvent.Load(roomId))
    }

    ChatRoomContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onContextClick = { room ->
            when (room.contextType) {
                ChatContextType.MATCH -> room.matchId?.let(onNavigateToMatch)
                ChatContextType.PAIRING -> room.pairingId?.let(onNavigateToPairing)
                ChatContextType.MERCENARY -> room.applicationId?.let(onNavigateToMercenary)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatRoomContent(
    state: ChatRoomUiState,
    onEvent: (ChatRoomEvent) -> Unit,
    onBackClick: () -> Unit,
    onContextClick: (ChatRoomDetail) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ArenaColors.Background),
    ) {
        ChatRoomTopBar(
            room = state.room,
            onBackClick = onBackClick,
            onContextClick = { state.room?.let(onContextClick) },
            onReportClick = { onEvent(ChatRoomEvent.OpenReportSheet) },
        )

        state.reportMessage?.let { confirmation ->
            InlineNotice(
                text = confirmation,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        when {
            state.isLoading -> {
                LoadingIndicator(modifier = Modifier.weight(1f))
            }

            state.error != null && state.room == null -> {
                RoomErrorState(
                    error = state.error,
                    onRetry = { onEvent(ChatRoomEvent.Load(state.roomId)) },
                    onBackClick = onBackClick,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                if (state.room?.canSend == false) {
                    InlineNotice(
                        text = state.room.readOnlyReason ?: "Cuộc trò chuyện này hiện chỉ còn chế độ xem.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (state.hasMore) {
                        item(key = "load_more") {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                TextButton(
                                    onClick = { onEvent(ChatRoomEvent.LoadMoreMessages) },
                                    enabled = !state.isLoadingMore,
                                ) {
                                    Text(if (state.isLoadingMore) "Đang tải..." else "Tải tin cũ hơn")
                                }
                            }
                        }
                    }

                    items(
                        items = state.messages,
                        key = { it.messageId },
                    ) { message ->
                        ChatMessageBubble(message = message)
                    }
                }

                ChatComposerBar(
                    draftText = state.draftText,
                    canSend = state.room?.canSend == true,
                    isSending = state.isSending,
                    sendError = state.sendError,
                    onDraftChange = { onEvent(ChatRoomEvent.UpdateDraft(it)) },
                    onQuickTemplateClick = { onEvent(ChatRoomEvent.SelectQuickTemplate(it)) },
                    onSendClick = { onEvent(ChatRoomEvent.SendMessage) },
                )
            }
        }
    }

    if (state.showReportSheet) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(ChatRoomEvent.DismissReportSheet) },
            containerColor = ArenaColors.CardBg,
        ) {
            ReportChatSheetContent(
                isSubmitting = state.isSubmittingReport,
                errorMessage = state.reportError,
                onSubmit = { reason, details ->
                    onEvent(ChatRoomEvent.SubmitReport(reason = reason, details = details))
                },
                onDismiss = { onEvent(ChatRoomEvent.DismissReportSheet) },
            )
        }
    }
}

@Composable
private fun ChatRoomTopBar(
    room: ChatRoomDetail?,
    onBackClick: () -> Unit,
    onContextClick: () -> Unit,
    onReportClick: () -> Unit,
) {
    Surface(
        color = ArenaColors.CardBg,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = ArenaColors.TextHeading,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room?.title ?: "Phòng chat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArenaColors.TextHeading,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (room != null) {
                        Text(
                            text = room.otherPartySubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = ArenaColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (room != null) {
                    ContextJumpPill(
                        contextType = room.contextType,
                        onClick = onContextClick,
                    )
                    IconButton(onClick = onReportClick) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = "Báo cáo hội thoại",
                            tint = ArenaColors.TextMuted,
                        )
                    }
                }
            }
            HorizontalDivider(color = ArenaColors.BorderSubtle)
        }
    }
}

@Composable
private fun ContextJumpPill(
    contextType: ChatContextType,
    onClick: () -> Unit,
) {
    val label =
        when (contextType) {
            ChatContextType.MATCH -> "Xem kèo"
            ChatContextType.PAIRING -> "Thương lượng"
            ChatContextType.MERCENARY -> "Đơn đá lẻ"
        }
    Row(
        modifier =
            Modifier
                .clip(ArenaShapes.Chip)
                .background(ArenaColors.BadgeGreenBg)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ArenaColors.BadgeGreenText,
            fontWeight = FontWeight.SemiBold,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            tint = ArenaColors.BadgeGreenText,
            modifier = Modifier.size(13.dp),
        )
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    when (message.senderType) {
        ChatMessageSenderType.SYSTEM -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .clip(ArenaShapes.Chip)
                            .background(ArenaColors.SurfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        ChatMessageSenderType.MINE -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ) {
                Box(
                    modifier =
                        Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(14.dp, 14.dp, 4.dp, 14.dp))
                            .background(ArenaColors.BubbleMine)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatMessageTime(message.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.TextMuted,
                )
            }
        }

        ChatMessageSenderType.OTHER -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.TextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                )
                Box(
                    modifier =
                        Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(14.dp, 14.dp, 14.dp, 4.dp))
                            .background(ArenaColors.BubbleIncoming)
                            .border(1.dp, ArenaColors.BorderSubtle, RoundedCornerShape(14.dp, 14.dp, 14.dp, 4.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArenaColors.TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatMessageTime(message.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.TextMuted,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ChatComposerBar(
    draftText: String,
    canSend: Boolean,
    isSending: Boolean,
    sendError: String?,
    onDraftChange: (String) -> Unit,
    onQuickTemplateClick: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    Surface(
        color = ArenaColors.CardBg,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = ArenaColors.BorderSubtle)

            if (sendError != null) {
                Text(
                    text = sendError,
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.Error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (canSend) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuickTemplates.forEach { template ->
                        Box(
                            modifier =
                                Modifier
                                    .clip(ArenaShapes.Chip)
                                    .background(ArenaColors.SurfaceContainer)
                                    .border(1.dp, ArenaColors.Border, ArenaShapes.Chip)
                                    .clickable { onQuickTemplateClick(template) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = template,
                                style = MaterialTheme.typography.labelMedium,
                                color = ArenaColors.Primary,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draftText,
                    onValueChange = onDraftChange,
                    enabled = canSend && !isSending,
                    placeholder = {
                        Text(
                            text = if (canSend) "Nhập tin nhắn..." else "Không thể gửi tin nhắn",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    singleLine = false,
                    maxLines = 3,
                    shape = ArenaShapes.Input,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArenaColors.Primary,
                            unfocusedBorderColor = ArenaColors.InputBorder,
                        ),
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSendClick,
                    enabled = canSend && !isSending && draftText.isNotBlank(),
                    modifier =
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (canSend && draftText.isNotBlank()) ArenaColors.Primary else ArenaColors.SurfaceContainer,
                            ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Gửi tin nhắn",
                        tint = if (canSend && draftText.isNotBlank()) Color.White else ArenaColors.TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportChatSheetContent(
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: (ChatReportReason, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedReason by remember { mutableStateOf(ChatReportReason.BAD_LANGUAGE) }
    var details by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Báo cáo nội dung hội thoại",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChatReportReason.entries.forEach { reason ->
                val selected = reason == selectedReason
                FilterChip(
                    selected = selected,
                    onClick = { selectedReason = reason },
                    label = { Text(reason.label) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArenaColors.ChipActiveBg,
                            selectedLabelColor = ArenaColors.ChipActiveText,
                        ),
                )
            }
        }

        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            label = { Text("Mô tả thêm (tùy chọn)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.Error,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        ) {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Hủy")
            }
            Button(
                onClick = { onSubmit(selectedReason, details) },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.Error),
            ) {
                Text(if (isSubmitting) "Đang gửi..." else "Gửi báo cáo")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun RoomErrorState(
    error: AppError,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message =
        when (error) {
            is AppError.DomainError -> error.message
            AppError.Network -> "Không có kết nối mạng."
            AppError.Unauthorized -> "Bạn không có quyền truy cập phòng chat này."
            AppError.NotFound -> "Cuộc trò chuyện không tồn tại hoặc đã bị xóa."
            else -> "Không thể tải phòng chat."
        }
    Box(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ArenaColors.TextMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onBackClick) {
                    Text("Quay lại")
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.Primary),
                ) {
                    Text("Thử lại")
                }
            }
        }
    }
}

private fun formatMessageTime(iso: String): String {
    val parsed = VietnamDateTime.parseToLocalDateTime(iso) ?: return iso
    return parsed.format(DateTimeFormatter.ofPattern("HH:mm"))
}
