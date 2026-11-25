@file:Suppress("MagicNumber", "LongMethod")

package com.igorwojda.showcase.feature.team.presentation.screen.teaminvitation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.TeamAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.presentation.composable.SkillLabelChips
import com.igorwojda.showcase.feature.team.presentation.composable.TeamStatusBadge
import com.igorwojda.showcase.feature.team.presentation.composable.formatIsoDateShort
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamInvitationScreen(
    modifier: Modifier = Modifier,
    invitationId: String? = null,
    onBackClick: () -> Unit = {},
    onAcceptComplete: (teamId: String) -> Unit = {},
) {
    val viewModel: TeamInvitationViewModel = koinViewModel()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(invitationId) {
        viewModel.load(invitationId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = "Lời mời vào đội",
                showBack = true,
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                ArenaLoadingState(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }

            state.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                    ArenaErrorState(
                        title = "Không thể tải lời mời",
                        message = state.errorMessage!!,
                        onBackClick = onBackClick,
                        onRetryClick = { viewModel.load(invitationId) },
                    )
                }
            }

            state.invitations.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                    ArenaEmptyState(
                        title = "Bạn chưa có lời mời nào",
                        description = "Khi có đội trưởng mời bạn vào đội, lời mời sẽ hiển thị tại đây.",
                        icon = Icons.Outlined.MailOutline,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!state.actionMessage.isNullOrBlank()) {
                        item { InlineNotice(text = state.actionMessage!!) }
                    }
                    items(state.invitations, key = { it.invitationId }) { inv ->
                        val isInactive = inv.team.status == TeamStatus.INACTIVE
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TeamAvatar(name = inv.team.teamName, size = 48.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = inv.team.teamName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Đội trưởng: ${inv.team.captainName} • ${inv.team.memberCount} thành viên",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArenaColors.TextMuted,
                                    )
                                    Text(
                                        text = "Mời ngày ${formatIsoDateShort(inv.invitedAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArenaColors.TextMuted,
                                    )
                                }
                                TeamStatusBadge(status = inv.team.status)
                            }

                            SkillLabelChips(labels = inv.team.skillLevels)

                            if (isInactive) {
                                InlineNotice(text = "Đội đang tạm ngưng — chưa thể chấp nhận hoặc từ chối.")
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    AppSecondaryButton(
                                        text = "Từ chối",
                                        onClick = { viewModel.reject(inv.invitationId) },
                                        enabled = !state.isSubmitting,
                                        modifier = Modifier.weight(1f),
                                    )
                                    AppButton(
                                        text = "Chấp nhận",
                                        onClick = { viewModel.accept(inv.invitationId, onAcceptComplete) },
                                        isLoading = state.isSubmitting,
                                        enabled = !state.isSubmitting,
                                        modifier = Modifier.weight(1f),
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
