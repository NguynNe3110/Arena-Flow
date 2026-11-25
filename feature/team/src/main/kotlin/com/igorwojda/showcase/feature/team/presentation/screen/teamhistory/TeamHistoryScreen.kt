@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LongMethod", "MagicNumber")

package com.igorwojda.showcase.feature.team.presentation.screen.teamhistory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.TeamAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.team.domain.model.TeamRole
import com.igorwojda.showcase.feature.team.presentation.composable.TeamStatusBadge
import com.igorwojda.showcase.feature.team.presentation.composable.formatIsoDateShort
import com.igorwojda.showcase.feature.team.presentation.composable.formatIsoDateTimeShort
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamHistoryScreen(
    teamId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val viewModel: TeamHistoryViewModel = koinViewModel()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(teamId) {
        viewModel.load(teamId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = "Lịch sử đội",
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
                        title = "Không thể tải lịch sử",
                        message = state.errorMessage ?: "Đội không tồn tại.",
                        onBackClick = onBackClick,
                        onRetryClick = viewModel::refresh,
                    )
                }
            }

            else -> {
                val team = state.team!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TeamAvatar(name = team.teamName, size = 40.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(team.teamName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Theo dõi lịch sử ban cán sự & thành viên",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArenaColors.TextMuted,
                                    )
                                }
                                TeamStatusBadge(status = team.status)
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TeamHistoryTab.entries.forEach { tab ->
                                AppFilterChip(
                                    text = tab.label,
                                    selected = state.selectedTab == tab,
                                    onClick = { viewModel.selectTab(tab) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    if (state.selectedTab == TeamHistoryTab.CAPTAIN) {
                        if (state.captainHistory.isEmpty()) {
                            item {
                                ArenaEmptyState(
                                    title = "Chưa có lịch sử đội trưởng",
                                    description = "Chưa ghi nhận thay đổi đội trưởng nào.",
                                )
                            }
                        } else {
                            items(state.captainHistory, key = { it.historyId }) { entry ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ArenaShapes.Card,
                                    color = Color.White,
                                    border = BorderStroke(1.dp, ArenaColors.BorderSubtle),
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
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
                                        )
                                        Text(
                                            text = formatIsoDateTimeShort(entry.transferredAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ArenaColors.TextMuted,
                                        )
                                        if (!entry.reason.isNullOrBlank()) {
                                            HorizontalDivider(
                                                color = ArenaColors.BorderSubtle,
                                                modifier = Modifier.padding(vertical = 4.dp),
                                            )
                                            Text("Lý do: ${entry.reason}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (state.members.isEmpty()) {
                            item {
                                ArenaEmptyState(
                                    title = "Không có thành viên hoạt động",
                                    description = "Đội hiện không có thành viên nào.",
                                )
                            }
                        } else {
                            items(state.members, key = { it.memberId }) { member ->
                                val isDeleted = member.displayName == "Người dùng đã xóa"
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ArenaShapes.Card,
                                    color = Color.White,
                                    border = BorderStroke(1.dp, ArenaColors.BorderSubtle),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
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
                                                    modifier =
                                                        Modifier
                                                            .size(
                                                                38.dp,
                                                            ).clip(CircleShape)
                                                            .background(ArenaColors.SurfaceContainer),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(Icons.Outlined.PersonOff, contentDescription = null, tint = ArenaColors.TextMuted)
                                                }
                                            } else {
                                                ArenaAvatar(name = member.displayName, size = 38.dp)
                                            }
                                            Column {
                                                Text(
                                                    text = member.displayName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isDeleted) FontWeight.Normal else FontWeight.SemiBold,
                                                    fontStyle = if (isDeleted) FontStyle.Italic else FontStyle.Normal,
                                                    color = if (isDeleted) ArenaColors.TextMuted else ArenaColors.TextHeading,
                                                )
                                                Text(
                                                    text = "Tham gia ${formatIsoDateShort(
                                                        member.joinedAt,
                                                    )}${member.leftAt?.let { " • Rời ${formatIsoDateShort(it)}" } ?: ""}",
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
                        }
                    }
                }
            }
        }
    }
}
