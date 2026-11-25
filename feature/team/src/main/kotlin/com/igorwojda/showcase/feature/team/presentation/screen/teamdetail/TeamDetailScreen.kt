@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LongMethod", "LongParameterList", "MagicNumber", "UnusedImports")

package com.igorwojda.showcase.feature.team.presentation.screen.teamdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.KeyValueRow
import com.igorwojda.showcase.feature.base.presentation.compose.composable.TeamAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.team.domain.model.CaptainHistoryEntry
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.domain.model.TeamRole
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.presentation.composable.AppDangerButton
import com.igorwojda.showcase.feature.team.presentation.composable.SkillLabelChips
import com.igorwojda.showcase.feature.team.presentation.composable.TeamStatusBadge
import com.igorwojda.showcase.feature.team.presentation.composable.formatIsoDateShort
import com.igorwojda.showcase.feature.team.presentation.composable.formatIsoDateTimeShort
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamDetailScreen(
    teamId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onEditTeamClick: (String) -> Unit = {},
    onManageTeamClick: (String) -> Unit = {},
    onHistoryClick: (String) -> Unit = {},
    onReputationClick: (String) -> Unit = {},
    onScheduleClick: (String) -> Unit = {},
    onExternalMatchesClick: (String) -> Unit = {},
    onRecruitmentsClick: (String) -> Unit = {},
) {
    val viewModel: TeamDetailViewModel = koinViewModel()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(teamId) {
        viewModel.load(teamId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = state.team?.teamName ?: "Chi tiết đội",
                showBack = true,
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                ArenaLoadingState(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }

            state.errorMessage != null || state.team == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                    ArenaErrorState(
                        title = "Không tìm thấy đội",
                        message = state.errorMessage ?: "Đội không tồn tại.",
                        onBackClick = onBackClick,
                        onRetryClick = viewModel::refresh,
                    )
                }
            }

            else -> {
                val team = state.team!!
                TeamDetailBody(
                    team = team,
                    state = state,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onSelectTab = viewModel::selectTab,
                    onOpenSheet = viewModel::openSheet,
                    onEditTeamClick = { onEditTeamClick(team.teamId) },
                    onManageTeamClick = { onManageTeamClick(team.teamId) },
                    onHistoryClick = { onHistoryClick(team.teamId) },
                    onReputationClick = { onReputationClick(team.teamId) },
                    onScheduleClick = { onScheduleClick(team.teamId) },
                    onExternalMatchesClick = { onExternalMatchesClick(team.teamId) },
                    onRecruitmentsClick = { onRecruitmentsClick(team.teamId) },
                )

                when (state.activeSheet) {
                    TeamDetailSheet.INVITE_MEMBER -> {
                        InviteMemberSheet(
                            teamName = team.teamName,
                            state = state,
                            onQueryChange = viewModel::onCandidateQueryChange,
                            onInviteClick = viewModel::inviteMember,
                            onDismiss = { viewModel.openSheet(null) },
                        )
                    }

                    TeamDetailSheet.TRANSFER_CAPTAIN -> {
                        TransferCaptainSheet(
                            teamName = team.teamName,
                            members = state.transferableMembers,
                            isSubmitting = state.isSubmittingAction,
                            onConfirm = viewModel::transferCaptain,
                            onDismiss = { viewModel.openSheet(null) },
                        )
                    }

                    TeamDetailSheet.LEAVE_TEAM -> {
                        LeaveTeamSheet(
                            teamName = team.teamName,
                            isCaptain = state.isCaptain,
                            isSubmitting = state.isSubmittingAction,
                            onOpenTransferCaptain = { viewModel.openSheet(TeamDetailSheet.TRANSFER_CAPTAIN) },
                            onConfirmLeave = { viewModel.leaveTeam(onBackClick) },
                            onDismiss = { viewModel.openSheet(null) },
                        )
                    }

                    TeamDetailSheet.DISBAND_TEAM -> {
                        DisbandTeamSheet(
                            team = team,
                            isSubmitting = state.isSubmittingAction,
                            onConfirmDisband = viewModel::disbandTeam,
                            onDismiss = { viewModel.openSheet(null) },
                        )
                    }

                    null -> {
                        // No sheet open.
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamDetailBody(
    team: TeamDetail,
    state: TeamDetailUiState,
    onSelectTab: (TeamDetailTab) -> Unit,
    onOpenSheet: (TeamDetailSheet) -> Unit,
    onEditTeamClick: () -> Unit,
    onManageTeamClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onReputationClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onExternalMatchesClick: () -> Unit,
    onRecruitmentsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!state.actionMessage.isNullOrBlank()) {
            item { InlineNotice(text = state.actionMessage) }
        }

        if (team.status != TeamStatus.ACTIVE) {
            item {
                val statusNotice =
                    if (team.status == TeamStatus.INACTIVE) {
                        "Đội đang tạm ngừng hoạt động (INACTIVE). Lý do: ${team.inactiveReason ?: "Bị tạm khóa"}"
                    } else {
                        "Đội đã giải tán ngày ${formatIsoDateShort(
                            team.disbandedAt,
                        )}. Lý do: ${team.disbandReason ?: "Đội trưởng giải tán đội"}"
                    }
                InlineNotice(text = statusNotice)
            }
        }

        item {
            TeamHeroCard(
                team = team,
                isCaptain = state.isCaptain,
                isMember = state.isMember,
                onInviteClick = { onOpenSheet(TeamDetailSheet.INVITE_MEMBER) },
                onEditClick = onEditTeamClick,
                onManageClick = onManageTeamClick,
                onLeaveClick = { onOpenSheet(TeamDetailSheet.LEAVE_TEAM) },
                onDisbandClick = { onOpenSheet(TeamDetailSheet.DISBAND_TEAM) },
            )
        }

        item {
            TeamQuickLinksRow(
                onReputationClick = onReputationClick,
                onScheduleClick = onScheduleClick,
                onExternalMatchesClick = onExternalMatchesClick,
                onRecruitmentsClick = onRecruitmentsClick,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TeamDetailTab.entries.forEach { tab ->
                    val label =
                        if (tab == TeamDetailTab.MEMBERS) "${tab.label} (${state.members.size})" else tab.label
                    AppFilterChip(
                        text = label,
                        selected = state.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                    )
                }
            }
        }

        when (state.selectedTab) {
            TeamDetailTab.OVERVIEW -> {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Thông tin đội", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        KeyValueRow("Trạng thái", team.status.label)
                        KeyValueRow(
                            label = if (team.status == TeamStatus.DISBANDED) "Người giải tán" else "Đội trưởng",
                            value = (if (team.status == TeamStatus.DISBANDED) team.disbandedByName else team.captainName) ?: "—",
                        )
                        KeyValueRow("Thành viên", "${team.memberCount}")
                        KeyValueRow("Ngày tạo", formatIsoDateShort(team.createdAt))
                        if (!team.description.isNullOrBlank()) {
                            Text(team.description, style = MaterialTheme.typography.bodyMedium, color = ArenaColors.TextMuted)
                        }
                        AppSecondaryButton(
                            text = "Xem uy tín & đánh giá (REP-04)",
                            onClick = onReputationClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            TeamDetailTab.MEMBERS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Thành viên đang hoạt động", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onHistoryClick, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("Lịch sử thành viên")
                        }
                    }
                }
                if (state.members.isEmpty()) {
                    item {
                        ArenaEmptyState(
                            title = "Không còn thành viên hoạt động",
                            description = "Đội hiện không có thành viên đang hoạt động.",
                        )
                    }
                } else {
                    items(state.members, key = { it.memberId }) { member ->
                        MemberItemCard(
                            member = member,
                            isMe = member.userId == state.currentUserId,
                        )
                    }
                }
            }

            TeamDetailTab.HISTORY -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Nhật ký ban cán sự", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onHistoryClick, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("Xem đầy đủ")
                        }
                    }
                }
                if (state.captainHistory.isEmpty()) {
                    item {
                        ArenaEmptyState(
                            title = "Chưa có lịch sử",
                            description = "Chưa ghi nhận thay đổi đội trưởng.",
                        )
                    }
                } else {
                    items(state.captainHistory, key = { it.historyId }) { entry ->
                        CaptainHistoryItemCard(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamHeroCard(
    team: TeamDetail,
    isCaptain: Boolean,
    isMember: Boolean,
    onInviteClick: () -> Unit,
    onEditClick: () -> Unit,
    onManageClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onDisbandClick: () -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TeamAvatar(name = team.teamName, size = 72.dp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(team.teamName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TeamStatusBadge(status = team.status)
            }
            SkillLabelChips(labels = team.skillLevels)
            if (!team.description.isNullOrBlank()) {
                Text(
                    text = team.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
        }

        if (team.status == TeamStatus.ACTIVE && isCaptain) {
            AppButton(
                text = "Mời thành viên",
                onClick = onInviteClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppSecondaryButton(
                    text = "Sửa thông tin",
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onManageClick,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Quản lý đội")
                }
            }
        } else if (team.status == TeamStatus.INACTIVE && isCaptain) {
            AppDangerButton(
                text = "Giải tán đội",
                onClick = onDisbandClick,
                modifier = Modifier.fillMaxWidth(),
            )
        } else if (team.status != TeamStatus.DISBANDED && isMember) {
            AppDangerButton(
                text = "Rời đội",
                onClick = onLeaveClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TeamQuickLinksRow(
    onReputationClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onExternalMatchesClick: () -> Unit,
    onRecruitmentsClick: () -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            QuickLinkItem(icon = Icons.Outlined.StarOutline, label = "Uy tín", onClick = onReputationClick)
            QuickLinkItem(icon = Icons.Outlined.CalendarMonth, label = "Lịch đấu", onClick = onScheduleClick)
            QuickLinkItem(icon = Icons.Outlined.EventNote, label = "Trận ngoài", onClick = onExternalMatchesClick)
            QuickLinkItem(icon = Icons.Outlined.PersonSearch, label = "Tuyển lẻ", onClick = onRecruitmentsClick)
        }
    }
}

@Composable
private fun QuickLinkItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = ArenaShapes.Button,
        modifier = Modifier.heightIn(min = 48.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = label, tint = ArenaColors.Primary, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = ArenaColors.TextPrimary)
        }
    }
}

@Composable
private fun MemberItemCard(
    member: TeamMember,
    isMe: Boolean,
) {
    val isDeleted = member.displayName == "Người dùng đã xóa"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        color = Color.White,
        border = BorderStroke(1.dp, ArenaColors.BorderSubtle),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isDeleted) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(ArenaColors.SurfaceContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.PersonOff, contentDescription = null, tint = ArenaColors.TextMuted)
                    }
                } else {
                    ArenaAvatar(name = member.displayName, size = 40.dp)
                }
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = member.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isDeleted) FontWeight.Normal else FontWeight.Bold,
                            fontStyle = if (isDeleted) FontStyle.Italic else FontStyle.Normal,
                            color = if (isDeleted) ArenaColors.TextMuted else ArenaColors.TextHeading,
                        )
                        if (isMe) {
                            AppBadge(text = "Bạn", variant = BadgeVariant.Green)
                        }
                    }
                    Text(
                        text = "Tham gia ${formatIsoDateShort(member.joinedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.TextMuted,
                    )
                }
            }
            AppBadge(
                text = member.role.label,
                variant = if (member.role == TeamRole.CAPTAIN) BadgeVariant.Amber else BadgeVariant.Neutral,
            )
        }
    }
}

@Composable
private fun CaptainHistoryItemCard(entry: CaptainHistoryEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        color = Color.White,
        border = BorderStroke(1.dp, ArenaColors.BorderSubtle),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text =
                    if (entry.fromUserName == null) {
                        "Tạo đội — ${entry.toUserName} trở thành đội trưởng"
                    } else {
                        "${entry.fromUserName} → ${entry.toUserName}"
                    },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            Text(
                text = formatIsoDateTimeShort(entry.transferredAt),
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
            )
            if (!entry.reason.isNullOrBlank()) {
                HorizontalDivider(color = ArenaColors.BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Lý do: ${entry.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextPrimary,
                )
            }
        }
    }
}
