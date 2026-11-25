@file:Suppress("LongMethod", "MagicNumber")

package com.igorwojda.showcase.feature.reputation.presentation.screen.reputationhistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.igorwojda.showcase.feature.base.presentation.compose.composable.KeyValueRow
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.reputation.domain.model.ReliabilityLevel
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.model.ReportStatus
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReputation
import org.koin.androidx.compose.koinViewModel

/** Screen displaying team reliability score summary, trust tier badge, and +/- delta report events. */
@Composable
fun ReputationHistoryScreen(
    teamId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReputationHistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    LaunchedEffect(teamId) {
        viewModel.onEvent(ReputationHistoryEvent.Load(teamId = teamId))
    }

    ReputationHistoryContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReputationHistoryContent(
    uiState: ReputationHistoryUiState,
    onEvent: (ReputationHistoryEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ArenaTopBar(
                title = "Uy tín & Lịch sử báo cáo",
                showBack = true,
                onBackClick = onBackClick,
            )
        },
        containerColor = ArenaColors.Background,
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.reputation == null -> {
                ArenaLoadingState(modifier = Modifier.padding(innerPadding).fillMaxSize())
            }

            uiState.error != null && uiState.reputation == null -> {
                ArenaErrorState(
                    title = "Không thể tải uy tín đội",
                    message = uiState.error.toString(),
                    onBackClick = onBackClick,
                    onRetryClick = { onEvent(ReputationHistoryEvent.Load(uiState.teamId)) },
                    modifier = Modifier.padding(innerPadding).padding(16.dp),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    uiState.reputation?.let { reputation ->
                        item { ReputationSummaryCard(reputation = reputation) }
                    }

                    uiState.disputeSuccessMessage?.let { message ->
                        item { InlineNotice(text = message) }
                    }

                    uiState.error?.let { error ->
                        item { InlineNotice(text = error.toString()) }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppFilterChip(
                                text = "Đội bị report",
                                selected = uiState.direction == ReportDirection.RECEIVED,
                                onClick = { onEvent(ReputationHistoryEvent.SelectDirection(ReportDirection.RECEIVED)) },
                            )
                            AppFilterChip(
                                text = "Đội đã report",
                                selected = uiState.direction == ReportDirection.SENT,
                                onClick = { onEvent(ReputationHistoryEvent.SelectDirection(ReportDirection.SENT)) },
                            )
                        }
                    }

                    if (uiState.reports.isEmpty()) {
                        item {
                            ArenaEmptyState(
                                title = "Chưa có sự kiện uy tín nào",
                                description = "Các báo cáo hoặc biến động điểm uy tín sẽ xuất hiện tại đây.",
                            )
                        }
                    } else {
                        items(uiState.reports, key = { it.reportId }) { report ->
                            ReputationEventCard(
                                report = report,
                                onDisputeClick = { onEvent(ReputationHistoryEvent.OpenDisputeSheet(report.reportId)) },
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (uiState.disputeTargetReportId != null) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(ReputationHistoryEvent.DismissDisputeSheet) },
            containerColor = ArenaColors.CardBg,
        ) {
            DisputeBottomSheetContent(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun ReputationSummaryCard(reputation: TeamReputation) {
    val tierVariant =
        when (reputation.reliabilityLevel) {
            ReliabilityLevel.EXCELLENT -> BadgeVariant.Success
            ReliabilityLevel.NORMAL -> BadgeVariant.Primary
            ReliabilityLevel.WARNING -> BadgeVariant.Warning
            ReliabilityLevel.CRITICAL -> BadgeVariant.Error
        }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Điểm uy tín đội",
                        style = MaterialTheme.typography.labelMedium,
                        color = ArenaColors.TextMuted,
                    )
                    Text(
                        text = "${reputation.reliabilityScore}/100",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArenaColors.TextHeading,
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppBadge(text = reputation.reliabilityLevel.label, variant = tierVariant)
                    if (reputation.verification.isVerified) {
                        AppBadge(text = "Đã xác minh", variant = BadgeVariant.Info)
                    }
                }
            }

            val completionText =
                if (reputation.completion.isNewcomer) {
                    "Mới tham gia ${reputation.completion.countedMatches}/5 trận"
                } else {
                    val completed = reputation.completion.completedMatches
                    val counted = reputation.completion.countedMatches
                    "${reputation.completion.completionRate ?: 0}% ($completed/$counted)"
                }
            KeyValueRow(label = "Tỉ lệ hoàn thành", value = completionText)

            val ratingText =
                reputation.rating.averageRating?.let { avg ->
                    "$avg ★ (${reputation.rating.ratingCount} đánh giá)"
                } ?: "Chưa có đánh giá"
            KeyValueRow(label = "Đánh giá trung bình", value = ratingText)
        }
    }
}

@Composable
private fun ReputationEventCard(
    report: TeamReport,
    onDisputeClick: () -> Unit,
) {
    val penalty = report.reliabilityPenalty
    val deltaLabel =
        when {
            report.status == ReportStatus.REJECTED && penalty != null -> "+$penalty (Hoàn điểm)"
            penalty != null && penalty > 0 -> "-$penalty điểm"
            else -> "0 điểm"
        }
    val statusVariant =
        when (report.status) {
            ReportStatus.AUTO_ACCEPTED -> BadgeVariant.Warning
            ReportStatus.DISPUTED -> BadgeVariant.Info
            ReportStatus.CONFIRMED -> BadgeVariant.Error
            ReportStatus.REJECTED -> BadgeVariant.Success
        }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${report.reportType.label} · ${report.counterpartTeam.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                    modifier = Modifier.weight(1f),
                )
                AppBadge(
                    text = deltaLabel,
                    variant = if (report.status == ReportStatus.REJECTED) BadgeVariant.Success else BadgeVariant.Error,
                )
            }
            Text(
                text = report.reason,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextPrimary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = report.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.TextMuted,
                )
                AppBadge(text = report.status.label, variant = statusVariant)
            }
            if (report.canDispute) {
                AppSecondaryButton(
                    text = "Tranh chấp (trong 24h)",
                    onClick = onDisputeClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DisputeBottomSheetContent(
    uiState: ReputationHistoryUiState,
    onEvent: (ReputationHistoryEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Khiếu nại báo cáo NO-SHOW",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        OutlinedTextField(
            value = uiState.disputeEvidence,
            onValueChange = { onEvent(ReputationHistoryEvent.UpdateDisputeEvidence(it)) },
            label = { Text("Bằng chứng (1..1000 ký tự)") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = if (uiState.isSubmittingDispute) "Đang gửi..." else "Gửi khiếu nại",
            onClick = { onEvent(ReputationHistoryEvent.SubmitDispute) },
            isLoading = uiState.isSubmittingDispute,
            enabled = !uiState.isSubmittingDispute && uiState.disputeEvidence.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
