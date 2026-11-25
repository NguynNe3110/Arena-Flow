@file:Suppress("CognitiveComplexMethod", "LabeledExpression", "LongMethod", "LongParameterList", "MagicNumber", "TooManyFunctions")

package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarydetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.KeyValueRow
import com.igorwojda.showcase.feature.base.presentation.compose.composable.TeamAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplicationStatus
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostDetail
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostStatus
import com.igorwojda.showcase.feature.mercenary.presentation.composable.ApplicationStatusBadge
import com.igorwojda.showcase.feature.mercenary.presentation.composable.MercenaryPostStatusBadge
import com.igorwojda.showcase.feature.mercenary.presentation.composable.formatAllowanceVnd
import com.igorwojda.showcase.feature.mercenary.presentation.composable.formatMatchDateTime
import org.koin.androidx.compose.koinViewModel

/** S8 Mercenary post detail screen with player apply/withdraw and captain moderation. */
@Composable
fun MercenaryDetailScreen(
    postId: String,
    onBackClick: () -> Unit = {},
    onEditPostClick: (String) -> Unit = {},
    onOpenChatClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: MercenaryDetailViewModel = koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(postId) {
        viewModel.onEvent(MercenaryDetailEvent.Load(postId))
    }

    MercenaryDetailContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onEditPostClick = { onEditPostClick(postId) },
        onOpenChatClick = onOpenChatClick,
        modifier = modifier,
    )
}

@Composable
internal fun MercenaryDetailContent(
    uiState: MercenaryDetailUiState,
    onEvent: (MercenaryDetailEvent) -> Unit,
    onBackClick: () -> Unit,
    onEditPostClick: () -> Unit,
    onOpenChatClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnEvent by androidx.compose.runtime.rememberUpdatedState(onEvent)

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        currentOnEvent(MercenaryDetailEvent.ClearSnackbar)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = "Chi tiết tin tuyển",
                showBack = true,
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        val post = uiState.post
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (uiState.isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = ArenaColors.Primary,
                    )
                }
            }

            if (uiState.error != null) {
                item {
                    ArenaErrorState(
                        title = "Không thể tải chi tiết tin",
                        message = uiState.error.userMessage(),
                        onBackClick = onBackClick,
                        onRetryClick = { onEvent(MercenaryDetailEvent.Load(uiState.postId)) },
                    )
                }
            } else if (post != null) {
                item { PostHeaderCard(post = post) }
                item { PostVenueCard(post = post) }
                if (post.viewerIsCaptain) {
                    item {
                        CaptainActionBar(
                            post = post,
                            isSubmitting = uiState.isSubmitting,
                            onEditClick = onEditPostClick,
                            onCloseClick = { onEvent(MercenaryDetailEvent.ClosePost) },
                        )
                    }
                    item {
                        Text(
                            text = "Danh sách ứng viên (${uiState.applications.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ArenaColors.TextHeading,
                        )
                    }
                    items(uiState.applications, key = { it.id }) { app ->
                        CaptainApplicationCard(
                            application = app,
                            isSubmitting = uiState.isSubmitting,
                            onAcceptClick = { onEvent(MercenaryDetailEvent.AcceptApplication(app.id)) },
                            onRejectClick = { onEvent(MercenaryDetailEvent.RejectApplication(app.id)) },
                            onChatClick = { app.conversationId?.let(onOpenChatClick) },
                        )
                    }
                } else {
                    item {
                        PlayerActionSection(
                            post = post,
                            isSubmitting = uiState.isSubmitting,
                            onApplyClick = { onEvent(MercenaryDetailEvent.OpenApplySheet) },
                            onWithdrawClick = { onEvent(MercenaryDetailEvent.WithdrawApplication) },
                            onOpenChatClick = onOpenChatClick,
                        )
                    }
                }
            }
        }
    }

    if (uiState.showApplySheet && uiState.post != null) {
        ApplyMercenaryBottomSheet(
            post = uiState.post,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { onEvent(MercenaryDetailEvent.DismissApplySheet) },
            onSubmit = { pos, note -> onEvent(MercenaryDetailEvent.SubmitApplication(pos, note)) },
        )
    }
}

@Composable
private fun PostHeaderCard(post: MercenaryPostDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = post.teamName, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.teamName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                )
                Text(
                    text = "Uy tín đội: ${post.teamReputationScore} · ${post.format.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
            MercenaryPostStatusBadge(
                status = post.status,
                slotsFilled = post.slotsFilled,
                slotsNeeded = post.slotsNeeded,
            )
        }
        KeyValueRow(label = "Vị trí cần", value = post.neededPositions.joinToString { it.label })
        KeyValueRow(label = "Thời gian", value = formatMatchDateTime(post.matchStartAt))
        KeyValueRow(label = "Trình độ", value = post.skillLevel.label)
        KeyValueRow(
            label = "Hỗ trợ",
            value = formatAllowanceVnd(post.allowanceVnd),
            showDivider = post.notes.isNotBlank(),
        )
        if (post.notes.isNotBlank()) {
            Text(
                text = post.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = ArenaColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun PostVenueCard(post: MercenaryPostDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Địa điểm thi đấu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        if (!post.venueName.isNullOrBlank()) {
            KeyValueRow(label = "Sân bóng", value = post.venueName)
            KeyValueRow(
                label = "Địa chỉ",
                value = post.venueAddress ?: post.areaLabel,
                showDivider = post.captainName != null,
            )
            post.captainName?.let { captain ->
                KeyValueRow(label = "Đội trưởng", value = captain, showDivider = false)
            }
        } else {
            KeyValueRow(label = "Khu vực", value = post.areaLabel, showDivider = false)
            InlineNotice(
                text = "Tên sân, địa chỉ và liên hệ đội trưởng chỉ hiển thị sau khi đơn được duyệt.",
            )
        }
    }
}

@Composable
private fun PlayerActionSection(
    post: MercenaryPostDetail,
    isSubmitting: Boolean,
    onApplyClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onOpenChatClick: (String) -> Unit,
) {
    val myApp = post.myApplication
    AppCard(modifier = Modifier.fillMaxWidth()) {
        if (myApp == null) {
            AppButton(
                text = "Ứng tuyển ngay",
                onClick = onApplyClick,
                enabled = post.status == MercenaryPostStatus.OPEN && !isSubmitting,
                isLoading = isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Trạng thái đơn của bạn",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                ApplicationStatusBadge(status = myApp.status)
            }
            if (myApp.status == MercenaryApplicationStatus.APPROVED) {
                InlineNotice(
                    text = "Rút lui sau khi đã được duyệt sẽ bị trừ điểm uy tín cá nhân.",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val convId = myApp.conversationId
                if (convId != null) {
                    AppSecondaryButton(
                        text = "Nhắn tin",
                        onClick = { onOpenChatClick(convId) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (myApp.status == MercenaryApplicationStatus.PENDING ||
                    myApp.status == MercenaryApplicationStatus.APPROVED
                ) {
                    AppButton(
                        text = "Rút đơn",
                        onClick = onWithdrawClick,
                        enabled = !isSubmitting,
                        isLoading = isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptainActionBar(
    post: MercenaryPostDetail,
    isSubmitting: Boolean,
    onEditClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val canModify = post.status == MercenaryPostStatus.OPEN || post.status == MercenaryPostStatus.FILLED
    if (!canModify) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppSecondaryButton(
            text = "Chỉnh sửa tin",
            onClick = onEditClick,
            enabled = !isSubmitting,
            modifier = Modifier.weight(1f),
        )
        AppButton(
            text = "Đóng tin",
            onClick = onCloseClick,
            enabled = !isSubmitting,
            isLoading = isSubmitting,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CaptainApplicationCard(
    application: MercenaryApplication,
    isSubmitting: Boolean,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onChatClick: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = application.applicantName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Uy tín ${application.applicantReputationScore} · ${application.position.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
            ApplicationStatusBadge(status = application.status)
        }
        if (application.note.isNotBlank()) {
            Text(
                text = application.note,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextPrimary,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (application.conversationId != null) {
                AppSecondaryButton(
                    text = "Chat",
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f),
                )
            }
            if (application.status == MercenaryApplicationStatus.PENDING) {
                AppSecondaryButton(
                    text = "Từ chối",
                    onClick = onRejectClick,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Duyệt",
                    onClick = onAcceptClick,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplyMercenaryBottomSheet(
    post: MercenaryPostDetail,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (PlayerPosition, String) -> Unit,
) {
    var selectedPos by remember { mutableStateOf(post.position) }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Ứng tuyển đá lẻ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            AppBadge(text = post.teamName, variant = BadgeVariant.Primary)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                post.neededPositions.forEach { pos ->
                    FilterChip(
                        selected = selectedPos == pos,
                        onClick = { selectedPos = pos },
                        label = { Text(pos.label) },
                    )
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Lời nhắn cho đội trưởng") },
                placeholder = { Text("Kinh nghiệm, sở trường của bạn...") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Gửi đơn ứng tuyển",
                onClick = { onSubmit(selectedPos, note) },
                isLoading = isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
