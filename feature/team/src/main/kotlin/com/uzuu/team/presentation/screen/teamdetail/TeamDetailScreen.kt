@file:Suppress("TooManyFunctions", "MagicNumber")

package com.uzuu.team.presentation.screen.teamdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.uzuu.team.domain.model.TeamDetail
import com.uzuu.team.domain.model.TeamMatchSchedule
import com.uzuu.team.domain.model.TeamMember
import com.uzuu.team.presentation.composable.ControlShape
import com.uzuu.team.presentation.composable.MatchShape
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamDetailScreen(
    onCreateTeamClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: TeamDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeamDetailContent(
        uiState = uiState,
        onTabSelect = viewModel::selectTab,
        onShowInviteSheet = viewModel::showInviteSheet,
        onDismissInviteSheet = viewModel::dismissInviteSheet,
        onInviteNameChange = viewModel::updateInviteName,
        onInvitePositionChange = viewModel::updateInvitePosition,
        onSubmitInvite = viewModel::submitInvite,
        onClearToast = viewModel::clearToast,
        onCreateTeamClick = onCreateTeamClick,
        onBackClick = onBackClick,
        onRetry = viewModel::loadTeam,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TeamDetailContent(
    uiState: TeamDetailUiState,
    onTabSelect: (Int) -> Unit,
    onShowInviteSheet: () -> Unit,
    onDismissInviteSheet: () -> Unit,
    onInviteNameChange: (String) -> Unit,
    onInvitePositionChange: (String) -> Unit,
    onSubmitInvite: () -> Unit,
    onClearToast: () -> Unit,
    onCreateTeamClick: () -> Unit,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnClearToast by rememberUpdatedState(onClearToast)

    LaunchedEffect(uiState.inviteSuccessMessage) {
        uiState.inviteSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            currentOnClearToast()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }

                uiState.error != null -> {
                    AppErrorContent(error = uiState.error, onRetry = onRetry)
                }

                uiState.team != null -> {
                    TeamDetailBody(
                        team = uiState.team,
                        selectedTab = uiState.selectedTab,
                        onTabSelect = onTabSelect,
                        onShowInviteSheet = onShowInviteSheet,
                        onCreateTeamClick = onCreateTeamClick,
                    )
                }
            }
        }
    }

    if (uiState.isInviteSheetVisible) {
        InviteMemberBottomSheet(
            name = uiState.inviteName,
            position = uiState.invitePosition,
            isInviting = uiState.isInviting,
            onNameChange = onInviteNameChange,
            onPositionChange = onInvitePositionChange,
            onSubmit = onSubmitInvite,
            onDismiss = onDismissInviteSheet,
        )
    }
}

@Composable
private fun TeamDetailBody(
    team: TeamDetail,
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    onShowInviteSheet: () -> Unit,
    onCreateTeamClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Prototype Team Header
        item {
            TeamHeaderSection(team = team)
        }

        // Tabs Row
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(ArenaShapes.Input),
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabSelect(0) },
                    text = { Text("Lịch kèo", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabSelect(1) },
                    text = { Text("Thành viên (${team.members.size})", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { onTabSelect(2) },
                    text = { Text("Lịch sử đấu", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                items(team.schedules) { schedule ->
                    TeamScheduleCard(schedule = schedule)
                }
            }

            1 -> {
                items(team.members) { member ->
                    TeamMemberCard(member = member)
                }
                item {
                    OutlinedButton(
                        onClick = onShowInviteSheet,
                        shape = ControlShape,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GroupAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thêm thành viên / Mời tham gia đội",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            2 -> {
                item {
                    TeamHistorySection(team = team)
                }
            }
        }
    }
}

@Composable
private fun TeamHeaderSection(
    team: TeamDetail,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        val roleBadge =
            if (team.role.equals("captain", ignoreCase = true) || team.role == "Đội trưởng") {
                "Đội của bạn · Đội trưởng"
            } else {
                "Đội của bạn · Thành viên"
            }
        AppBadge(text = roleBadge, variant = BadgeVariant.Green)

        Spacer(Modifier.height(8.dp))

        Text(
            text = team.name,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextPrimary,
            letterSpacing = (-0.035).sp,
            lineHeight = 36.sp,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Bóng đá 7 người · ${team.area}",
            fontSize = 15.sp,
            color = ArenaColors.TextMuted,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Uy tín đội",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextPrimary,
        )

        Spacer(Modifier.height(10.dp))

        // Line 1: Điểm uy tín
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Điểm uy tín",
                fontSize = 15.sp,
                color = ArenaColors.TextPrimary,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${team.reputationScore} ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ArenaColors.TextPrimary,
                )
                Text(
                    text = "/ 100",
                    fontSize = 13.sp,
                    color = ArenaColors.TextMuted,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
        }
        HorizontalDivider(color = ArenaColors.BorderSubtle, thickness = 1.dp)

        // Line 2: Đánh giá trung bình
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Đánh giá trung bình",
                fontSize = 15.sp,
                color = ArenaColors.TextPrimary,
            )
            Text(
                text = "%.1f / 5".format(java.util.Locale.US, team.rating).replace('.', ','),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextPrimary,
            )
        }
        HorizontalDivider(color = ArenaColors.BorderSubtle, thickness = 1.dp)

        // Line 3: Tỉ lệ hoàn thành
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tỉ lệ hoàn thành",
                fontSize = 15.sp,
                color = ArenaColors.TextPrimary,
            )
            Text(
                text = "${team.completionRate}% · ${team.matchCount} trận",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextPrimary,
            )
        }
        HorizontalDivider(color = ArenaColors.BorderSubtle, thickness = 1.dp)

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Điểm đội độc lập với uy tín cá nhân khi đá lẻ.",
            fontSize = 13.sp,
            color = ArenaColors.TextMuted,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun TeamScheduleCard(schedule: TeamMatchSchedule) {
    Surface(
        shape = MatchShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = schedule.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = schedule.statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            Text(
                text =
                    if (schedule.isRecruitingMercenary) {
                        schedule.mercenaryRole ?: "Tuyển người đá lẻ"
                    } else {
                        "VS ${schedule.opponentName} (${schedule.opponentShortName})"
                    },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = schedule.venueName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Chi tiết",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamMemberCard(member: TeamMember) {
    Surface(
        shape = MatchShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (member.isCurrentUser) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = member.displayName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color =
                            if (member.isCurrentUser) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = member.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (member.isCurrentUser) {
                            Surface(
                                shape = ControlShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                            ) {
                                Text(
                                    text = "Bạn",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = "${member.role} • ${member.position}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                shape = ControlShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = "${member.reputationScore} điểm",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun TeamHistorySection(team: TeamDetail) {
    Surface(
        shape = MatchShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Lịch sử thi đấu gần nhất",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "• 18:00 • 15/11/2026: Bình Minh FC 3 - 2 FC Cơn Lốc (Sân Chuyên Việt) - Thắng",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "• 19:30 • 08/11/2026: Bình Minh FC 2 - 2 Sài Gòn Utd (Sân Thảo Điền) - Hòa",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "• 17:30 • 01/11/2026: Bình Minh FC 4 - 1 An Phú FC (Sân An Phú) - Thắng",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteMemberBottomSheet(
    name: String,
    position: String,
    isInviting: Boolean,
    onNameChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Mời thành viên mới vào đội",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text =
                    "Nhập tên hoặc số điện thoại cầu thủ muốn mời. " +
                        "Cầu thủ sẽ nhận được thông báo phản hồi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Tên hoặc SĐT cầu thủ *") },
                placeholder = { Text("VD: 0912345678 hoặc Nguyễn Văn A") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Vị trí sở trường:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Thủ môn", "Hậu vệ", "Tiền vệ", "Tiền đạo").forEach { pos ->
                        FilterChip(
                            selected = position == pos,
                            onClick = { onPositionChange(pos) },
                            label = { Text(pos, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        )
                    }
                }
            }

            Button(
                onClick = onSubmit,
                enabled = name.isNotBlank() && !isInviting,
                shape = ControlShape,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
            ) {
                if (isInviting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = "Gửi lời mời tham gia",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
