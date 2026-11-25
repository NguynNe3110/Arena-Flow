@file:Suppress("MagicNumber", "LongMethod", "LongParameterList")

package com.igorwojda.showcase.feature.team.presentation.screen.teammanage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.TeamAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.presentation.composable.TeamStatusBadge
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamManageScreen(
    teamId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onEditTeamClick: (String) -> Unit = {},
    onOpenTeamDetailClick: (String) -> Unit = {},
    onHistoryClick: (String) -> Unit = {},
    onScheduleClick: (String) -> Unit = {},
    onExternalMatchesClick: (String) -> Unit = {},
    onRecruitmentsClick: (String) -> Unit = {},
) {
    val viewModel: TeamManageViewModel = koinViewModel()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(teamId) {
        viewModel.load(teamId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = "Quản lý đội",
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
                        title = "Không thể tải đội",
                        message = state.errorMessage ?: "Đội không tồn tại.",
                        onBackClick = onBackClick,
                        onRetryClick = viewModel::refresh,
                    )
                }
            }

            else -> {
                val team = state.team!!
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TeamAvatar(name = team.teamName, size = 46.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(team.teamName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "Bảng điều khiển của Đội trưởng",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArenaColors.TextMuted,
                                )
                            }
                            TeamStatusBadge(status = team.status)
                        }
                    }

                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        if (team.status == TeamStatus.ACTIVE) {
                            ManageMenuRow(Icons.Outlined.Edit, "Sửa thông tin đội", onClick = { onEditTeamClick(teamId) })
                            HorizontalDivider(color = ArenaColors.BorderSubtle)
                            ManageMenuRow(Icons.Outlined.PersonAdd, "Mời thành viên mới", onClick = { onOpenTeamDetailClick(teamId) })
                            HorizontalDivider(color = ArenaColors.BorderSubtle)
                            ManageMenuRow(Icons.Outlined.SwapHoriz, "Chuyển quyền đội trưởng", onClick = { onOpenTeamDetailClick(teamId) })
                            HorizontalDivider(color = ArenaColors.BorderSubtle)
                        }
                        ManageMenuRow(Icons.Outlined.History, "Lịch sử đội trưởng & thành viên", onClick = { onHistoryClick(teamId) })
                        HorizontalDivider(color = ArenaColors.BorderSubtle)
                        ManageMenuRow(Icons.Outlined.CalendarMonth, "Lịch thi đấu đội", onClick = { onScheduleClick(teamId) })
                        HorizontalDivider(color = ArenaColors.BorderSubtle)
                        ManageMenuRow(Icons.Outlined.EventNote, "Trận đấu ngoài hệ thống", onClick = { onExternalMatchesClick(teamId) })
                        HorizontalDivider(color = ArenaColors.BorderSubtle)
                        ManageMenuRow(Icons.Outlined.PersonSearch, "Tuyển người đá lẻ", onClick = { onRecruitmentsClick(teamId) })

                        if (team.status != TeamStatus.DISBANDED) {
                            HorizontalDivider(color = ArenaColors.BorderSubtle)
                            ManageMenuRow(
                                icon = Icons.Outlined.DeleteForever,
                                label = "Giải tán đội",
                                tint = ArenaColors.Error,
                                onClick = { onOpenTeamDetailClick(teamId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageMenuRow(
    icon: ImageVector,
    label: String,
    tint: Color = ArenaColors.Primary,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (tint == ArenaColors.Error) ArenaColors.Error else ArenaColors.TextHeading,
            )
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = ArenaColors.TextMuted)
    }
}
