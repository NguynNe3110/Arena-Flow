@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList", "MagicNumber", "UnusedImports")

package com.igorwojda.showcase.feature.team.presentation.screen.teamhub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaSearchFilterBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.TeamAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.team.domain.model.MyInvitation
import com.igorwojda.showcase.feature.team.domain.model.MyTeam
import com.igorwojda.showcase.feature.team.domain.model.OfficialSkillLabels
import com.igorwojda.showcase.feature.team.domain.model.TeamRole
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.model.TeamSummary
import com.igorwojda.showcase.feature.team.presentation.composable.SkillLabelChips
import com.igorwojda.showcase.feature.team.presentation.composable.TeamStatusBadge
import com.igorwojda.showcase.feature.team.presentation.composable.formatIsoDateShort
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamHubScreen(
    modifier: Modifier = Modifier,
    onCreateTeamClick: () -> Unit = {},
    onTeamClick: (teamId: String) -> Unit = {},
    onOpenInvitationClick: (invitationId: String) -> Unit = {},
    onLoginClick: () -> Unit = {},
) {
    val viewModel: TeamHubViewModel = koinViewModel()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    TeamHubContent(
        state = state,
        modifier = modifier,
        onScopeSelect = viewModel::selectScope,
        onQueryChange = viewModel::onQueryChange,
        onApplyFilter = viewModel::applyFilter,
        onResetFilter = viewModel::resetFilter,
        onCreateTeamClick = onCreateTeamClick,
        onTeamClick = onTeamClick,
        onAcceptInvitation = { invId -> viewModel.acceptInvitation(invId, onTeamClick) },
        onRejectInvitation = viewModel::rejectInvitation,
        onOpenInvitationClick = onOpenInvitationClick,
        onLoginClick = onLoginClick,
        onRetryClick = viewModel::refresh,
    )
}

@Composable
internal fun TeamHubContent(
    state: TeamHubUiState,
    modifier: Modifier = Modifier,
    onScopeSelect: (TeamHubScope) -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onApplyFilter: (TeamHubFilter) -> Unit = {},
    onResetFilter: () -> Unit = {},
    onCreateTeamClick: () -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    onAcceptInvitation: (String) -> Unit = {},
    onRejectInvitation: (String) -> Unit = {},
    onOpenInvitationClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onRetryClick: () -> Unit = {},
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().background(ArenaColors.Background),
    ) {
        TeamHubHeader(
            state = state,
            onScopeSelect = onScopeSelect,
            onQueryChange = onQueryChange,
            onFilterClick = { showFilterSheet = true },
            onCreateTeamClick = onCreateTeamClick,
        )

        when {
            state.isLoading -> {
                ArenaLoadingState(modifier = Modifier.fillMaxSize())
            }

            state.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    ArenaErrorState(
                        title = "Không thể tải danh sách đội",
                        message = state.errorMessage,
                        onRetryClick = onRetryClick,
                    )
                }
            }

            else -> {
                TeamHubList(
                    state = state,
                    onResetFilter = onResetFilter,
                    onCreateTeamClick = onCreateTeamClick,
                    onTeamClick = onTeamClick,
                    onAcceptInvitation = onAcceptInvitation,
                    onRejectInvitation = onRejectInvitation,
                    onOpenInvitationClick = onOpenInvitationClick,
                    onLoginClick = onLoginClick,
                )
            }
        }
    }

    if (showFilterSheet) {
        TeamFilterSheet(
            initialFilter = state.filter,
            onDismiss = { showFilterSheet = false },
            onReset = {
                onResetFilter()
                showFilterSheet = false
            },
            onApply = { updated ->
                onApplyFilter(updated)
                showFilterSheet = false
            },
        )
    }
}

@Composable
private fun TeamHubHeader(
    state: TeamHubUiState,
    onScopeSelect: (TeamHubScope) -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onCreateTeamClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    if (state.scope == TeamHubScope.MY) {
                        "Đội của tôi (${state.myTeams.size})"
                    } else {
                        "Đội hệ thống (${state.systemTeams.size})"
                    },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            if (state.isLoggedIn) {
                Surface(
                    onClick = onCreateTeamClick,
                    shape = ArenaShapes.Button,
                    color = ArenaColors.Primary,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
                        Text(
                            text = "Tạo đội",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TeamHubScope.entries.forEach { scopeOption ->
                AppFilterChip(
                    text = scopeOption.label,
                    selected = state.scope == scopeOption,
                    onClick = { onScopeSelect(scopeOption) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ArenaSearchFilterBar(
            query = state.query,
            onQueryChange = onQueryChange,
            onFilterClick = onFilterClick,
            placeholder = "Tìm theo tên đội bóng...",
            activeFilterCount = state.filter.activeCount,
        )
    }
}

@Composable
private fun TeamHubList(
    state: TeamHubUiState,
    onResetFilter: () -> Unit,
    onCreateTeamClick: () -> Unit,
    onTeamClick: (String) -> Unit,
    onAcceptInvitation: (String) -> Unit,
    onRejectInvitation: (String) -> Unit,
    onOpenInvitationClick: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!state.bannerMessage.isNullOrBlank()) {
            item {
                InlineNotice(text = state.bannerMessage)
            }
        }

        if (state.scope == TeamHubScope.MY) {
            if (!state.isLoggedIn) {
                item {
                    ArenaEmptyState(
                        title = "Đăng nhập để quản lý đội",
                        description = "Bạn có thể xem danh sách đội hệ thống hoặc đăng nhập để tạo và tham gia đội bóng.",
                        icon = Icons.Outlined.Groups,
                        actionLabel = "Đăng nhập ngay",
                        onAction = onLoginClick,
                    )
                }
            } else {
                items(state.pendingInvitations, key = { "inv-${it.invitationId}" }) { inv ->
                    PendingInvitationBannerCard(
                        invitation = inv,
                        onOpen = { onOpenInvitationClick(inv.invitationId) },
                        onAccept = { onAcceptInvitation(inv.invitationId) },
                        onReject = { onRejectInvitation(inv.invitationId) },
                    )
                }

                val teams = state.filteredMyTeams
                if (teams.isEmpty()) {
                    item {
                        ArenaEmptyState(
                            title =
                                if (state.myTeams.isEmpty()) {
                                    "Bạn chưa tham gia đội nào"
                                } else {
                                    "Không có đội phù hợp bộ lọc"
                                },
                            description =
                                if (state.myTeams.isEmpty()) {
                                    "Hãy tạo đội bóng mới hoặc khám phá các đội trong hệ thống."
                                } else {
                                    "Thử đặt lại bộ lọc để xem toàn bộ đội của bạn."
                                },
                            icon = Icons.Outlined.GroupAdd,
                            actionLabel = if (state.myTeams.isEmpty()) "Tạo đội mới" else "Đặt lại bộ lọc",
                            onAction = if (state.myTeams.isEmpty()) onCreateTeamClick else onResetFilter,
                        )
                    }
                } else {
                    items(teams, key = { it.teamId }) { team ->
                        MyTeamCard(team = team, onClick = { onTeamClick(team.teamId) })
                    }
                }
            }
        } else {
            val teams = state.filteredSystemTeams
            if (teams.isEmpty()) {
                item {
                    ArenaEmptyState(
                        title = "Không tìm thấy đội phù hợp",
                        description = "Thử từ khóa khác hoặc đặt lại bộ lọc.",
                        icon = Icons.Outlined.SearchOff,
                        actionLabel = "Đặt lại bộ lọc",
                        onAction = onResetFilter,
                    )
                }
            } else {
                items(teams, key = { it.teamId }) { team ->
                    SystemTeamCard(team = team, onClick = { onTeamClick(team.teamId) })
                }
            }
        }
    }
}

@Composable
private fun PendingInvitationBannerCard(
    invitation: MyInvitation,
    onOpen: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        color = Color.White,
        border = BorderStroke(1.dp, ArenaColors.SuccessBorder),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAvatar(name = invitation.team.teamName, size = 44.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppBadge(text = "Lời mời đang chờ", variant = BadgeVariant.Green)
                    Text(
                        text = "${invitation.team.teamName} mời bạn vào đội",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArenaColors.TextHeading,
                    )
                    Text(
                        text = "Mời ngày ${formatIsoDateShort(invitation.invitedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.TextMuted,
                    )
                }
            }
            if (invitation.team.status == TeamStatus.INACTIVE) {
                Text(
                    text = "Đội đang tạm ngưng — chưa thể chấp nhận hoặc từ chối.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.Warning,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppSecondaryButton(
                        text = "Từ chối",
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                    )
                    AppButton(
                        text = "Chấp nhận",
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MyTeamCard(
    team: MyTeam,
    onClick: () -> Unit,
) {
    val isDisbanded = team.status == TeamStatus.DISBANDED
    AppCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAvatar(name = team.teamName, size = 44.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team.teamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDisbanded) ArenaColors.TextMuted else ArenaColors.TextHeading,
                        textDecoration = if (isDisbanded) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Tham gia ${formatIsoDateShort(team.joinedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.TextMuted,
                    )
                }
            }
            TeamStatusBadge(status = team.status)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (team.role == TeamRole.CAPTAIN) {
                AppBadge(text = "Bạn là Đội trưởng", variant = BadgeVariant.Amber)
            }
            SkillLabelChips(labels = team.skillLevels)
        }

        HorizontalDivider(color = ArenaColors.BorderSubtle)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Đội trưởng: ${team.captainName ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
            )
            Text(
                text = "${team.memberCount} thành viên",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = ArenaColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun SystemTeamCard(
    team: TeamSummary,
    onClick: () -> Unit,
) {
    AppCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAvatar(name = team.teamName, size = 44.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team.teamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArenaColors.TextHeading,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Tạo ngày ${formatIsoDateShort(team.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.TextMuted,
                    )
                }
            }
            TeamStatusBadge(status = team.status)
        }

        SkillLabelChips(labels = team.skillLevels)

        HorizontalDivider(color = ArenaColors.BorderSubtle)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val leadLabel =
                if (team.status == TeamStatus.DISBANDED) {
                    "Người giải tán: ${team.disbandedByName ?: "—"}"
                } else {
                    "Đội trưởng: ${team.captainName ?: "—"}"
                }
            Text(
                text = leadLabel,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
            )
            Text(
                text = "${team.memberCount} thành viên",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = ArenaColors.TextPrimary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TeamFilterSheet(
    initialFilter: TeamHubFilter,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onApply: (TeamHubFilter) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember(initialFilter) { mutableStateOf(initialFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Bộ lọc đội bóng",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Phạm vi danh sách", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TeamHubScope.entries.forEach { scopeOpt ->
                        AppFilterChip(
                            text = scopeOpt.label,
                            selected = draft.scope == scopeOpt,
                            onClick = { draft = draft.copy(scope = scopeOpt) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Trạng thái đội", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        "ALL" to "Tất cả",
                        "ACTIVE" to "Đang hoạt động",
                        "INACTIVE" to "Bị khóa",
                        "DISBANDED" to "Đã giải tán",
                    ).forEach { (key, label) ->
                        AppFilterChip(
                            text = label,
                            selected = draft.status == key,
                            onClick = { draft = draft.copy(status = key) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Trình độ / Phong cách", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (listOf("ALL" to "Tất cả") + OfficialSkillLabels.all.map { it to it }).forEach { (key, label) ->
                        AppFilterChip(
                            text = label,
                            selected = draft.skillLabel == key,
                            onClick = { draft = draft.copy(skillLabel = key) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppSecondaryButton(text = "Đặt lại", onClick = onReset, modifier = Modifier.weight(1f))
                AppButton(text = "Áp dụng", onClick = { onApply(draft) }, modifier = Modifier.weight(1f))
            }
        }
    }
}
