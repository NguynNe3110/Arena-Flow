@file:Suppress("LongMethod", "MagicNumber", "StringLiteralDuplication", "UnusedImports")

package com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.AdminReportStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.model.AuditLogEntry
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChipRow
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes

@Composable
internal fun ReportsQueueTab(
    state: AdminConsoleUiState,
    onEvent: (AdminConsoleEvent) -> Unit,
) {
    val filterOptions = listOf("Tất cả") + AdminReportStatus.entries.map { it.label }
    val selectedLabel = state.reportFilter?.label ?: "Tất cả"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppFilterChipRow(
                items = filterOptions,
                selectedItem = selectedLabel,
                onSelect = { label ->
                    val status = AdminReportStatus.entries.firstOrNull { it.label == label }
                    onEvent(AdminConsoleEvent.FilterReportsByStatus(status))
                },
            )
        }
        if (state.reports.isEmpty()) {
            item {
                ArenaEmptyState(
                    title = "Không có báo cáo nào",
                    description = "Hàng chờ báo cáo vi phạm hiện đang trống.",
                    icon = Icons.Outlined.ReportProblem,
                )
            }
        } else {
            items(state.reports, key = { it.reportId }) { report ->
                AdminReportCard(
                    report = report,
                    onResolveClick = { onEvent(AdminConsoleEvent.OpenResolveReportDialog(report)) },
                )
            }
        }
    }
}

@Composable
private fun AdminReportCard(
    report: AdminReport,
    onResolveClick: () -> Unit,
) {
    val badgeVariant =
        when (report.status) {
            AdminReportStatus.PENDING, AdminReportStatus.DISPUTED -> BadgeVariant.Warning
            AdminReportStatus.CONFIRMED -> BadgeVariant.Error
            AdminReportStatus.REJECTED -> BadgeVariant.Neutral
            AdminReportStatus.AUTO_ACCEPTED -> BadgeVariant.Info
        }
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${report.reporterType} · ${report.type}",
                style = MaterialTheme.typography.labelMedium,
                color = ArenaColors.TextMuted,
                fontWeight = FontWeight.SemiBold,
            )
            AppBadge(text = report.status.label, variant = badgeVariant)
        }
        Text(
            text = "${report.reporterName} → ${report.reportedName}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        Text(
            text = report.reason,
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaColors.TextPrimary,
        )
        report.adminDecisionNote?.let { note ->
            Text(
                text = "Ghi chú xử lý: $note",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
            )
        }
        Text(
            text = "Tạo lúc: ${report.createdAt}",
            style = MaterialTheme.typography.labelSmall,
            color = ArenaColors.TextMuted,
        )
        AppButton(
            text = "Xem xét & Quyết định",
            onClick = onResolveClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun PendingVenuesTab(
    venues: List<AdminPendingVenue>,
    onEvent: (AdminConsoleEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (venues.isEmpty()) {
            item {
                ArenaEmptyState(
                    title = "Không có sân chờ duyệt",
                    description = "Tất cả địa điểm sân mới đã được xử lý.",
                    icon = Icons.Outlined.LocationOn,
                )
            }
        } else {
            items(venues, key = { it.venueId }) { venue ->
                PendingVenueCard(
                    venue = venue,
                    onApprove = { onEvent(AdminConsoleEvent.ApproveVenue(venue.venueId)) },
                    onReject = { onEvent(AdminConsoleEvent.OpenRejectVenueDialog(venue)) },
                )
            }
        }
    }
}

@Composable
private fun PendingVenueCard(
    venue: AdminPendingVenue,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = venue.venueName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
                modifier = Modifier.weight(1f),
            )
            AppBadge(text = venue.venueType, variant = BadgeVariant.Info)
        }
        Text(
            text = venue.address,
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaColors.TextPrimary,
        )
        Text(
            text = "Người tạo: ${venue.createdByName}${if (venue.isCreatedByDeleted) " (Đã xóa)" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = ArenaColors.TextMuted,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppSecondaryButton(
                text = "Từ chối",
                onClick = onReject,
                modifier = Modifier.weight(1f),
            )
            AppButton(
                text = "Phê duyệt",
                onClick = onApprove,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun UsersSanctionsTab(
    state: AdminConsoleUiState,
    onEvent: (AdminConsoleEvent) -> Unit,
) {
    val statusOptions = listOf("Tất cả") + AdminUserStatus.entries.map { it.label }
    val selectedLabel = state.userFilter?.label ?: "Tất cả"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.userQuery,
                    onValueChange = { onEvent(AdminConsoleEvent.SearchUsers(it)) },
                    placeholder = { Text("Tìm theo tên hoặc số điện thoại...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = ArenaShapes.Input,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
                AppFilterChipRow(
                    items = statusOptions,
                    selectedItem = selectedLabel,
                    onSelect = { label ->
                        val status = AdminUserStatus.entries.firstOrNull { it.label == label }
                        onEvent(AdminConsoleEvent.FilterUsersByStatus(status))
                    },
                )
            }
        }
        if (state.users.isEmpty()) {
            item {
                ArenaEmptyState(
                    title = "Không tìm thấy người dùng",
                    description = "Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc trạng thái.",
                    icon = Icons.Outlined.People,
                )
            }
        } else {
            items(state.users, key = { it.userId }) { user ->
                AdminUserCard(user = user, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun AdminUserCard(
    user: AdminUserSummary,
    onEvent: (AdminConsoleEvent) -> Unit,
) {
    val statusVariant =
        when (user.status) {
            AdminUserStatus.ACTIVE -> BadgeVariant.Success
            AdminUserStatus.BANNED -> BadgeVariant.Error
            AdminUserStatus.DELETED -> BadgeVariant.Neutral
        }
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                )
                Text(
                    text = "${user.phone} · ${user.role}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
            AppBadge(text = user.status.label, variant = statusVariant)
        }
        Text(
            text = "Uy tín: ${user.reputationScore}/100 · Số trận: ${user.matchCount}",
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaColors.TextPrimary,
        )
        user.banReason?.let { reason ->
            Text(
                text = "Lý do khóa: $reason",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.Error,
            )
        }
        if (user.status != AdminUserStatus.DELETED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppSecondaryButton(
                    text = "Chỉnh uy tín",
                    onClick = { onEvent(AdminConsoleEvent.OpenAdjustReputationDialog(user)) },
                    modifier = Modifier.weight(1f),
                )
                if (user.status == AdminUserStatus.BANNED) {
                    AppButton(
                        text = "Mở khóa",
                        onClick = { onEvent(AdminConsoleEvent.UnbanUser(user.userId)) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    AppButton(
                        text = "Khóa tài khoản",
                        onClick = { onEvent(AdminConsoleEvent.OpenBanUserDialog(user)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun AuditLogsTab(logs: List<AuditLogEntry>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (logs.isEmpty()) {
            item {
                ArenaEmptyState(
                    title = "Chưa có nhật ký quản trị",
                    description = "Các thao tác kiểm duyệt sẽ được ghi lại tại đây.",
                    icon = Icons.Outlined.History,
                )
            }
        } else {
            items(logs, key = { it.logId }) { entry ->
                AuditLogCard(entry = entry)
            }
        }
    }
}

@Composable
private fun AuditLogCard(entry: AuditLogEntry) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBadge(text = entry.action, variant = BadgeVariant.Dark)
            Text(
                text = entry.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = ArenaColors.TextMuted,
            )
        }
        Text(
            text = "${entry.actorName} · ${entry.targetLabel}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        Text(
            text = entry.detail,
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaColors.TextPrimary,
        )
    }
}
