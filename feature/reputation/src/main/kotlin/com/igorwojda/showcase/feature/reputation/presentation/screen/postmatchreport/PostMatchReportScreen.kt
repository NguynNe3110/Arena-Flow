@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LongMethod", "MagicNumber")

package com.igorwojda.showcase.feature.reputation.presentation.screen.postmatchreport

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRating
import com.igorwojda.showcase.feature.reputation.domain.model.PostMatchOutcome
import com.igorwojda.showcase.feature.reputation.domain.model.ReportType
import org.koin.androidx.compose.koinViewModel

private val FAIR_PLAY_TAGS = listOf("Đá đẹp", "Đúng giờ", "Fair play", "Nhiệt tình", "Tôn trọng đối thủ")

/** S13 Post-Match Report & Rating Screen (REP-01, REP-02, REP-06). */
@Composable
fun PostMatchReportScreen(
    matchId: String,
    teamId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostMatchReportViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    LaunchedEffect(matchId, teamId) {
        viewModel.onEvent(PostMatchReportEvent.Load(matchId = matchId, teamId = teamId))
    }

    PostMatchReportContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PostMatchReportContent(
    uiState: PostMatchReportUiState,
    onEvent: (PostMatchReportEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ArenaTopBar(
                title = "Báo cáo sau trận",
                showBack = true,
                onBackClick = onBackClick,
            )
        },
        containerColor = ArenaColors.Background,
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                ArenaLoadingState(modifier = Modifier.padding(innerPadding).fillMaxSize())
            }

            uiState.error != null && uiState.existingRatings == null -> {
                ArenaErrorState(
                    title = "Không thể tải dữ liệu trận đấu",
                    message = uiState.error.toString(),
                    onBackClick = onBackClick,
                    onRetryClick = {
                        onEvent(PostMatchReportEvent.Load(matchId = uiState.matchId, teamId = uiState.teamId))
                    },
                    modifier = Modifier.padding(innerPadding).padding(16.dp),
                )
            }

            else -> {
                Column(
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    val given = uiState.submittedRating ?: uiState.existingRatings?.given
                    if (given != null) {
                        SubmittedRatingSummaryCard(
                            given = given,
                            received = uiState.existingRatings?.received,
                        )
                    } else {
                        OutcomeSelectorCard(uiState = uiState, onEvent = onEvent)

                        if (uiState.outcome == PostMatchOutcome.COMPLETED) {
                            StarAndTagsCard(uiState = uiState, onEvent = onEvent)
                            ScoreAndCommentCard(uiState = uiState, onEvent = onEvent)
                        } else if (uiState.outcome == PostMatchOutcome.OPPONENT_NO_SHOW) {
                            ScoreAndCommentCard(uiState = uiState, onEvent = onEvent, showScoreInputs = false)
                        }
                    }

                    AbuseReportSummaryCard(uiState = uiState, onEvent = onEvent)

                    uiState.validationMessage?.let { message ->
                        InlineNotice(text = message)
                    }
                    uiState.error?.let { error ->
                        InlineNotice(text = error.toString())
                    }
                    uiState.submittedReport?.let { report ->
                        InlineNotice(
                            text = "Đã gửi báo cáo (${report.reportType.label}). Trạng thái: ${report.status.label}.",
                        )
                    }

                    if (given == null || uiState.includeAbuseReport) {
                        AppButton(
                            text = if (uiState.isSubmitting) "Đang gửi..." else "Gửi báo cáo sau trận",
                            onClick = { onEvent(PostMatchReportEvent.Submit) },
                            isLoading = uiState.isSubmitting,
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (uiState.isAbuseReportSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(PostMatchReportEvent.SetAbuseReportSheetOpen(false)) },
            containerColor = ArenaColors.CardBg,
        ) {
            AbuseReportSheetContent(uiState = uiState, onEvent = onEvent)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OutcomeSelectorCard(
    uiState: PostMatchReportUiState,
    onEvent: (PostMatchReportEvent) -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Kết quả trận đấu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PostMatchOutcome.entries.forEach { outcome ->
                    AppFilterChip(
                        text = outcome.label,
                        selected = uiState.outcome == outcome,
                        onClick = { onEvent(PostMatchReportEvent.SelectOutcome(outcome)) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StarAndTagsCard(
    uiState: PostMatchReportUiState,
    onEvent: (PostMatchReportEvent) -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Đánh giá đối thủ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (star in 1..5) {
                    val selected = star <= uiState.starRating
                    Icon(
                        imageVector = if (selected) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = "$star sao",
                        tint = if (selected) ArenaColors.Warning else ArenaColors.TextMuted,
                        modifier =
                            Modifier
                                .size(36.dp)
                                .clickable { onEvent(PostMatchReportEvent.SelectStarRating(star)) },
                    )
                }
                Text(
                    text = "${uiState.starRating}/5 sao",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArenaColors.TextPrimary,
                )
            }
            Text(
                text = "Thẻ tinh thần thi đấu (Fair-play)",
                style = MaterialTheme.typography.labelMedium,
                color = ArenaColors.TextMuted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FAIR_PLAY_TAGS.forEach { tag ->
                    AppFilterChip(
                        text = tag,
                        selected = tag in uiState.selectedTags,
                        onClick = { onEvent(PostMatchReportEvent.ToggleFairPlayTag(tag)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreAndCommentCard(
    uiState: PostMatchReportUiState,
    onEvent: (PostMatchReportEvent) -> Unit,
    showScoreInputs: Boolean = true,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (showScoreInputs) {
                Text(
                    text = "Tỷ số trận đấu (tùy chọn)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ArenaColors.TextHeading,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.raterTeamScoreInput,
                        onValueChange = { onEvent(PostMatchReportEvent.UpdateRaterScore(it)) },
                        label = { Text("Đội mình") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = uiState.rateeTeamScoreInput,
                        onValueChange = { onEvent(PostMatchReportEvent.UpdateRateeScore(it)) },
                        label = { Text("Đối thủ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            OutlinedTextField(
                value = uiState.comment,
                onValueChange = { onEvent(PostMatchReportEvent.UpdateComment(it)) },
                label = { Text("Nhận xét thêm (tối đa 500 ký tự)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AbuseReportSummaryCard(
    uiState: PostMatchReportUiState,
    onEvent: (PostMatchReportEvent) -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = null,
                        tint = ArenaColors.Error,
                    )
                    Text(
                        text = "Báo cáo vi phạm (tùy chọn)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ArenaColors.TextHeading,
                    )
                }
                if (uiState.includeAbuseReport) {
                    AppBadge(text = uiState.abuseReportType.label, variant = BadgeVariant.Error)
                }
            }
            if (uiState.includeAbuseReport && uiState.abuseReason.isNotBlank()) {
                Text(
                    text = uiState.abuseReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextPrimary,
                )
            }
            AppSecondaryButton(
                text = if (uiState.includeAbuseReport) "Chỉnh sửa báo cáo vi phạm" else "Thêm báo cáo vi phạm",
                onClick = { onEvent(PostMatchReportEvent.SetAbuseReportSheetOpen(true)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SubmittedRatingSummaryCard(
    given: MatchRating,
    received: MatchRating?,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Đánh giá của đội bạn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                )
                AppBadge(
                    text = if (given.isNoShow) "Không đến" else "${given.score ?: 0}/5 sao",
                    variant = if (given.isNoShow) BadgeVariant.Warning else BadgeVariant.Success,
                )
            }
            given.comment?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = ArenaColors.TextPrimary)
            }
            if (received != null) {
                InlineNotice(
                    text =
                        if (received.isNoShow) {
                            "Đối thủ đánh giá: Đội không đến."
                        } else {
                            "Đối thủ đánh giá đội bạn: ${received.score ?: 0}/5 sao. ${received.comment.orEmpty()}"
                        },
                )
            } else {
                ArenaEmptyState(
                    title = "Đánh giá ẩn hai chiều",
                    description = "Đánh giá của đối thủ sẽ hiển thị sau khi đối thủ hoàn tất đánh giá.",
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AbuseReportSheetContent(
    uiState: PostMatchReportUiState,
    onEvent: (PostMatchReportEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Báo cáo vi phạm đối thủ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReportType.entries.forEach { type ->
                AppFilterChip(
                    text = type.label,
                    selected = uiState.abuseReportType == type,
                    onClick = { onEvent(PostMatchReportEvent.SelectAbuseReportType(type)) },
                )
            }
        }
        OutlinedTextField(
            value = uiState.abuseReason,
            onValueChange = { onEvent(PostMatchReportEvent.UpdateAbuseReason(it)) },
            label = { Text("Lý do chi tiết (bắt buộc)") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppSecondaryButton(
                text = "Bỏ báo cáo",
                onClick = { onEvent(PostMatchReportEvent.ClearAbuseReportDraft) },
                modifier = Modifier.weight(1f),
            )
            AppButton(
                text = "Xác nhận",
                onClick = { onEvent(PostMatchReportEvent.ConfirmAbuseReportDraft) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
