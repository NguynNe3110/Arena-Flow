@file:Suppress(
    "CognitiveComplexMethod",
    "LabeledExpression",
    "LongMethod",
    "LongParameterList",
    "MagicNumber",
    "StringLiteralDuplication",
    "TooManyFunctions",
)

package com.igorwojda.showcase.feature.match.presentation.screen.matchdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppOutlinedButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.KeyValueRow
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.composable.PitchBrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.composable.TeamAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.match.domain.model.CostSplit
import com.igorwojda.showcase.feature.match.domain.model.MatchCostAgreement
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchRequest
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.presentation.composable.MatchUiFormatters
import org.koin.androidx.compose.koinViewModel

/** S4 Match Detail screen (MATCH-03, 04, 09, 10, 11, 12, 13, 14). */
@Composable
fun MatchDetailScreen(
    matchId: String,
    onBackClick: () -> Unit,
    onEditMatchClick: (String) -> Unit = {},
    onOpenPairingClick: (String) -> Unit = {},
    onOpenChatClick: (String) -> Unit = {},
    onOpenReportClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: MatchDetailViewModel = koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(matchId) {
        viewModel.onEvent(MatchDetailEvent.Load(matchId))
    }

    MatchDetailContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onEditMatchClick = onEditMatchClick,
        onOpenPairingClick = onOpenPairingClick,
        onOpenChatClick = onOpenChatClick,
        onOpenReportClick = onOpenReportClick,
        modifier = modifier,
    )
}

@Composable
internal fun MatchDetailContent(
    uiState: MatchDetailUiState,
    onEvent: (MatchDetailEvent) -> Unit,
    onBackClick: () -> Unit,
    onEditMatchClick: (String) -> Unit,
    onOpenPairingClick: (String) -> Unit,
    onOpenChatClick: (String) -> Unit,
    onOpenReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnEvent by androidx.compose.runtime.rememberUpdatedState(onEvent)
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    var showCostDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.actionMessage) {
        val message = uiState.actionMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        currentOnEvent(MatchDetailEvent.ConsumeActionMessage)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PitchBrandHeader(
                compact = true,
                showBack = true,
                onBackClick = onBackClick,
                title = uiState.detail?.authorTeam?.teamName ?: "Chi tiết kèo",
                subtitle = uiState.detail?.let { MatchUiFormatters.formatDateTimeRange(it.startsAt, it.endsAt) }.orEmpty(),
            )

            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }

                uiState.error != null && uiState.detail == null -> {
                    AppErrorContent(
                        error = uiState.error,
                        onRetry = { onEvent(MatchDetailEvent.Retry) },
                    )
                }

                uiState.detail != null -> {
                    MatchDetailBody(
                        uiState = uiState,
                        detail = uiState.detail,
                        onEvent = onEvent,
                        onEditMatchClick = onEditMatchClick,
                        onOpenPairingClick = onOpenPairingClick,
                        onOpenChatClick = onOpenChatClick,
                        onOpenReportClick = onOpenReportClick,
                        onShowCancelDialog = { showCancelDialog = true },
                        onShowCostDialog = { showCostDialog = true },
                    )
                }
            }
        }
    }

    val detail = uiState.detail
    if (showCancelDialog && detail != null) {
        CancelMatchDialog(
            isConfirmed = detail.status == "CONFIRMED",
            onConfirm = { reason ->
                showCancelDialog = false
                onEvent(MatchDetailEvent.CancelMatch(expectedStatus = detail.status, reason = reason))
            },
            onDismiss = { showCancelDialog = false },
        )
    }

    if (showCostDialog && detail != null) {
        EditCostAgreementDialog(
            initial = detail.costAgreement,
            onSave = { updated ->
                showCostDialog = false
                onEvent(MatchDetailEvent.SaveCostAgreement(updated))
            },
            onDismiss = { showCostDialog = false },
        )
    }
}

@Composable
private fun MatchDetailBody(
    uiState: MatchDetailUiState,
    detail: MatchDetail,
    onEvent: (MatchDetailEvent) -> Unit,
    onEditMatchClick: (String) -> Unit,
    onOpenPairingClick: (String) -> Unit,
    onOpenChatClick: (String) -> Unit,
    onOpenReportClick: (String) -> Unit,
    onShowCancelDialog: () -> Unit,
    onShowCostDialog: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LifecycleStatusCard(detail = detail)
        TeamsCard(detail = detail)
        VenueAndTimeCard(detail = detail)
        CostAgreementCard(
            detail = detail,
            canEditCost = uiState.isHostTeam && detail.type == MatchType.HAS_VENUE && detail.status == "OPEN",
            onEditCostClick = onShowCostDialog,
        )

        if (detail.type == MatchType.HAS_VENUE) {
            RequestsSection(
                uiState = uiState,
                detail = detail,
                onEvent = onEvent,
                onOpenChatClick = onOpenChatClick,
            )
        } else if (detail.status == "OPEN" || detail.confirmedPairingId != null) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Thương lượng Kèo chưa có sân",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                )
                AppButton(
                    text = if (detail.confirmedPairingId != null) "Xem phòng thương lượng" else "Mở thương lượng / Nhận kèo",
                    onClick = { onOpenPairingClick(detail.confirmedPairingId ?: detail.matchId) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        HostAndParticipantActions(
            uiState = uiState,
            detail = detail,
            onEditMatchClick = onEditMatchClick,
            onShowCancelDialog = onShowCancelDialog,
            onForfeitClick = { onEvent(MatchDetailEvent.ForfeitMatch("Bỏ cuộc trước giờ thi đấu")) },
            onOpenReportClick = onOpenReportClick,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LifecycleStatusCard(detail: MatchDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBadge(
                text = if (detail.type == MatchType.HAS_VENUE) "Luồng A · Đã có sân" else "Luồng B · Chưa có sân",
                variant = if (detail.type == MatchType.HAS_VENUE) BadgeVariant.Green else BadgeVariant.Warning,
            )
            AppBadge(
                text = MatchUiFormatters.statusLabel(detail.status),
                variant = MatchUiFormatters.statusBadgeVariant(detail.status),
            )
        }

        if (!detail.cancelReason.isNullOrBlank()) {
            InlineNotice(text = "Lý do hủy: ${detail.cancelReason}")
        }
    }
}

@Composable
private fun TeamsCard(detail: MatchDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Hai đội tham gia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        TeamRow(roleLabel = "Đội chủ kèo", teamName = detail.authorTeam.teamName, score = detail.authorTeam.reliabilityScore)
        if (detail.opponentTeam != null) {
            HorizontalDivider(color = ArenaColors.BorderSubtle)
            TeamRow(roleLabel = "Đối thủ đã ghép", teamName = detail.opponentTeam.teamName, score = detail.opponentTeam.reliabilityScore)
        } else {
            Text(
                text = "Chưa có đối thủ được ghép.",
                style = MaterialTheme.typography.bodyMedium,
                color = ArenaColors.TextMuted,
            )
        }
    }
}

@Composable
private fun TeamRow(
    roleLabel: String,
    teamName: String,
    score: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = teamName, size = 40.dp)
            Column {
                Text(text = roleLabel, style = MaterialTheme.typography.bodySmall, color = ArenaColors.TextMuted)
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                )
            }
        }
        AppBadge(text = "Uy tín $score", variant = if (score >= 70) BadgeVariant.Success else BadgeVariant.Warning)
    }
}

@Composable
private fun VenueAndTimeCard(detail: MatchDetail) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Thông tin sân & giờ đá",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        KeyValueRow(label = "Thể thức", value = MatchUiFormatters.formatLabel(detail.format))
        KeyValueRow(label = "Trình độ", value = detail.skillLevels.joinToString(", ").ifBlank { "Mọi trình độ" })
        KeyValueRow(label = "Thời gian", value = MatchUiFormatters.formatDateTimeRange(detail.startsAt, detail.endsAt))
        if (detail.venue != null) {
            KeyValueRow(label = "Sân bóng", value = detail.venue.venueName)
            KeyValueRow(label = "Địa chỉ", value = detail.venue.address, showDivider = !detail.matchNature.isNullOrBlank())
        } else if (detail.venueOptions.isNotEmpty()) {
            detail.venueOptions.forEachIndexed { idx, venue ->
                KeyValueRow(label = "Sân đề xuất ${idx + 1}", value = venue.venueName)
            }
            detail.timeOptions.forEachIndexed { idx, slot ->
                KeyValueRow(
                    label = "Khung giờ ${idx + 1}",
                    value = MatchUiFormatters.formatDateTimeRange(slot.startsAt, slot.endsAt),
                    showDivider = idx < detail.timeOptions.lastIndex,
                )
            }
        }
        if (!detail.matchNature.isNullOrBlank()) {
            KeyValueRow(label = "Ghi chú", value = detail.matchNature, showDivider = false)
        }
    }
}

@Composable
private fun CostAgreementCard(
    detail: MatchDetail,
    canEditCost: Boolean,
    onEditCostClick: () -> Unit,
) {
    val cost = detail.costAgreement
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Thỏa thuận chi phí",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            if (canEditCost) {
                TextButton(onClick = onEditCostClick) {
                    Text("Sửa chi phí", color = ArenaColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        KeyValueRow(label = "Tỷ lệ chia", value = MatchUiFormatters.costSplitLabel(cost.costSplit))
        if (cost.venueAmount != null) {
            KeyValueRow(label = "Tiền sân", value = MatchUiFormatters.formatVnd(cost.venueAmount), showDivider = cost.drinksAmount != null)
        }
        if (cost.drinksAmount != null) {
            KeyValueRow(label = "Tiền nước", value = MatchUiFormatters.formatVnd(cost.drinksAmount), showDivider = false)
        }
    }
}

@Composable
private fun RequestsSection(
    uiState: MatchDetailUiState,
    detail: MatchDetail,
    onEvent: (MatchDetailEvent) -> Unit,
    onOpenChatClick: (String) -> Unit,
) {
    if (uiState.isHostTeam) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Yêu cầu ghép kèo (${uiState.requests.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            if (uiState.requests.isEmpty()) {
                Text(
                    text = "Chưa có đội nào gửi yêu cầu ghép kèo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArenaColors.TextMuted,
                )
            } else {
                uiState.requests.forEach { req ->
                    HostRequestRow(
                        request = req,
                        canRespond = detail.status == "OPEN" && req.status == "PENDING",
                        isSubmitting = uiState.isSubmitting,
                        onAccept = { onEvent(MatchDetailEvent.AcceptRequest(req.requestId)) },
                        onReject = { onEvent(MatchDetailEvent.RejectRequest(req.requestId, null)) },
                        onChat = { onOpenChatClick(req.conversationId) },
                    )
                }
            }
        }
    } else {
        ChallengerActionCard(
            myRequest = uiState.myTeamRequest,
            matchOpen = detail.status == "OPEN",
            isSubmitting = uiState.isSubmitting,
            onSendRequest = { onEvent(MatchDetailEvent.SendRequest("Đội mình muốn xin kèo này!")) },
            onWithdraw = { req ->
                onEvent(
                    MatchDetailEvent.WithdrawRequest(
                        requestId = req.requestId,
                        expectedStatus = req.status,
                        reason = if (req.status == "ACCEPTED") "Đội bận đột xuất" else null,
                    ),
                )
            },
            onOpenChat = onOpenChatClick,
        )
    }
}

@Composable
private fun HostRequestRow(
    request: MatchRequest,
    canRespond: Boolean,
    isSubmitting: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onChat: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(color = ArenaColors.BorderSubtle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                TeamAvatar(name = request.requesterTeamName, size = 36.dp)
                Column {
                    Text(request.requesterTeamName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Uy tín ${request.requesterTeamReliabilityScore}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.TextMuted,
                    )
                }
            }
            AppBadge(text = request.status, variant = if (request.status == "ACCEPTED") BadgeVariant.Success else BadgeVariant.Neutral)
        }
        if (!request.reason.isNullOrBlank()) {
            Text(text = request.reason, style = MaterialTheme.typography.bodySmall, color = ArenaColors.TextPrimary)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppOutlinedButton(text = "Nhắn tin", onClick = onChat, modifier = Modifier.weight(1f))
            if (canRespond) {
                AppSecondaryButton(text = "Từ chối", onClick = onReject, enabled = !isSubmitting, modifier = Modifier.weight(1f))
                AppButton(text = "Chấp nhận", onClick = onAccept, enabled = !isSubmitting, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChallengerActionCard(
    myRequest: MatchRequest?,
    matchOpen: Boolean,
    isSubmitting: Boolean,
    onSendRequest: () -> Unit,
    onWithdraw: (MatchRequest) -> Unit,
    onOpenChat: (String) -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        when {
            myRequest == null && matchOpen -> {
                AppButton(
                    text = "Xin kèo ngay",
                    onClick = onSendRequest,
                    isLoading = isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            myRequest != null -> {
                Text(
                    text = "Trạng thái yêu cầu: ${myRequest.status}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppSecondaryButton(
                        text = "Nhắn tin",
                        onClick = { onOpenChat(myRequest.conversationId) },
                        modifier = Modifier.weight(1f),
                    )
                    if (myRequest.status in listOf("PENDING", "ACCEPTED")) {
                        AppOutlinedButton(
                            text = "Rút yêu cầu",
                            onClick = { onWithdraw(myRequest) },
                            isLoading = isSubmitting,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HostAndParticipantActions(
    uiState: MatchDetailUiState,
    detail: MatchDetail,
    onEditMatchClick: (String) -> Unit,
    onShowCancelDialog: () -> Unit,
    onForfeitClick: () -> Unit,
    onOpenReportClick: (String) -> Unit,
) {
    val canCancel =
        (uiState.isHostTeam && detail.status in listOf("OPEN", "CONFIRMED")) ||
            (uiState.isOpponentTeam && detail.status == "CONFIRMED")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (uiState.isHostTeam && detail.status == "OPEN") {
            AppSecondaryButton(
                text = "Chỉnh sửa kèo",
                onClick = { onEditMatchClick(detail.matchId) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (canCancel) {
            AppOutlinedButton(
                text = "Hủy kèo",
                onClick = onShowCancelDialog,
                isLoading = uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if ((uiState.isHostTeam || uiState.isOpponentTeam) && detail.status == "CONFIRMED") {
            AppOutlinedButton(
                text = "Báo bỏ cuộc",
                onClick = onForfeitClick,
                isLoading = uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if ((uiState.isHostTeam || uiState.isOpponentTeam) && detail.status in listOf("ARCHIVED", "COMPLETED")) {
            AppButton(
                text = "Đánh giá / Báo cáo sau trận",
                onClick = { onOpenReportClick(detail.matchId) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CancelMatchDialog(
    isConfirmed: Boolean,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận hủy kèo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text =
                        if (isConfirmed) {
                            "Kèo đã ghép khi hủy sẽ bị trừ điểm uy tín theo quy định. Vui lòng nhập lý do."
                        } else {
                            "Bạn chắc chắn muốn hủy kèo đang mở này?"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(if (isConfirmed) "Lý do hủy (bắt buộc)" else "Lý do (tùy chọn)") },
                    shape = ArenaShapes.Input,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason.takeIf { it.isNotBlank() }) }) {
                Text("Xác nhận hủy", color = ArenaColors.Error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditCostAgreementDialog(
    initial: MatchCostAgreement,
    onSave: (MatchCostAgreement) -> Unit,
    onDismiss: () -> Unit,
) {
    var costSplit by rememberSaveable { mutableStateOf(initial.costSplit ?: CostSplit.FIFTY_FIFTY.name) }
    var venueAmountText by rememberSaveable { mutableStateOf(initial.venueAmount?.toString().orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa thỏa thuận chi phí", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CostSplit.entries.forEach { split ->
                        AppFilterChip(
                            text = split.label,
                            selected = costSplit == split.name,
                            onClick = { costSplit = split.name },
                        )
                    }
                }
                OutlinedTextField(
                    value = venueAmountText,
                    onValueChange = { value -> venueAmountText = value.filter { it.isDigit() } },
                    label = { Text("Tiền sân (VND)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = ArenaShapes.Input,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = venueAmountText.toIntOrNull()
                    onSave(
                        initial.copy(
                            costType = if (amount != null && amount > 0) "VENUE_ONLY" else null,
                            costSplit = costSplit,
                            venueAmount = amount,
                        ),
                    )
                },
            ) {
                Text("Lưu", color = ArenaColors.Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        },
    )
}
