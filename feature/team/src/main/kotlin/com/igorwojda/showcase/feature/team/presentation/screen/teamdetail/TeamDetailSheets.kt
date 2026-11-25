@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.team.presentation.screen.teamdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.presentation.composable.AppDangerButton
import com.igorwojda.showcase.feature.team.presentation.composable.formatIsoDateShort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InviteMemberSheet(
    teamName: String,
    state: TeamDetailUiState,
    onQueryChange: (String) -> Unit,
    onInviteClick: (userId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Mời thành viên vào $teamName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Tìm theo tên hiển thị hoặc số điện thoại (nhập ít nhất 2 ký tự).",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
            )
            OutlinedTextField(
                value = state.candidateQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Nhập tên hoặc SĐT...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = ArenaShapes.Input,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArenaColors.Primary,
                        unfocusedBorderColor = ArenaColors.Border,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )

            if (!state.candidateError.isNullOrBlank()) {
                Text(
                    text = state.candidateError,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.Error,
                )
            }

            when {
                state.isSearchingCandidates -> {
                    ArenaLoadingState(modifier = Modifier.fillMaxWidth().padding(16.dp))
                }

                state.candidateQuery.trim().length >= 2 && state.candidates.isEmpty() && state.candidateError == null -> {
                    ArenaEmptyState(
                        title = "Không tìm thấy người dùng",
                        description = "Thử tìm bằng tên khác hoặc số điện thoại đầy đủ.",
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.candidates, key = { it.userId }) { candidate ->
                            Surface(
                                shape = ArenaShapes.Card,
                                color = ArenaColors.Background,
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
                                        ArenaAvatar(name = candidate.displayName, size = 40.dp)
                                        Text(
                                            text = candidate.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    when (candidate.membershipStatus) {
                                        "ACTIVE" -> {
                                            AppBadge(text = "Đã là thành viên", variant = BadgeVariant.Neutral)
                                        }

                                        "PENDING" -> {
                                            AppBadge(text = "Đã được mời", variant = BadgeVariant.Warning)
                                        }

                                        else -> {
                                            AppButton(
                                                text = "Mời",
                                                onClick = { onInviteClick(candidate.userId) },
                                                enabled = !state.isSubmittingAction,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (state.candidatesHasMore) {
                        Text(
                            text = "Nhập thêm để thu hẹp kết quả (hiển thị tối đa 20 gợi ý).",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArenaColors.TextMuted,
                        )
                    }
                }
            }

            AppSecondaryButton(
                text = "Đóng",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransferCaptainSheet(
    teamName: String,
    members: List<TeamMember>,
    isSubmitting: Boolean,
    onConfirm: (newCaptainId: String, reason: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedUserId by remember(members) { mutableStateOf(members.firstOrNull()?.userId.orEmpty()) }
    var reason by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Chuyển quyền đội trưởng ($teamName)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            InlineNotice(
                text =
                    "Người được chọn sẽ trở thành đội trưởng ngay lập tức và không thể từ chối. " +
                        "Bạn sẽ trở thành thành viên thường.",
            )

            if (members.isEmpty()) {
                ArenaEmptyState(
                    title = "Không có thành viên nào khác",
                    description = "Đội cần ít nhất 1 thành viên hoạt động khác để chuyển quyền đội trưởng.",
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(members, key = { it.userId }) { member ->
                        val selected = selectedUserId == member.userId
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { selectedUserId = member.userId },
                            shape = ArenaShapes.Card,
                            color = if (selected) ArenaColors.PrimaryContainer.copy(alpha = 0.4f) else Color.White,
                            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) ArenaColors.Primary else ArenaColors.Border),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    ArenaAvatar(name = member.displayName, size = 38.dp)
                                    Column {
                                        Text(
                                            text = member.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            text = "Tham gia ${formatIsoDateShort(member.joinedAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ArenaColors.TextMuted,
                                        )
                                    }
                                }
                                RadioButton(
                                    selected = selected,
                                    onClick = { selectedUserId = member.userId },
                                    colors = RadioButtonDefaults.colors(selectedColor = ArenaColors.Primary),
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it.take(500) },
                    label = { Text("Lý do (không bắt buộc)") },
                    placeholder = { Text("Ví dụ: Bận công tác dài ngày") },
                    maxLines = 3,
                    shape = ArenaShapes.Input,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppSecondaryButton(text = "Hủy", onClick = onDismiss, modifier = Modifier.weight(1f))
                    AppButton(
                        text = "Chuyển quyền",
                        onClick = { onConfirm(selectedUserId, reason.trim().ifBlank { null }) },
                        isLoading = isSubmitting,
                        enabled = selectedUserId.isNotBlank() && !isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LeaveTeamSheet(
    teamName: String,
    isCaptain: Boolean,
    isSubmitting: Boolean,
    onOpenTransferCaptain: () -> Unit,
    onConfirmLeave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Rời đội $teamName?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (isCaptain) {
                InlineNotice(
                    text =
                        "Đội trưởng không thể rời đội trực tiếp. " +
                            "Hãy chuyển quyền đội trưởng cho thành viên khác hoặc giải tán đội.",
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppSecondaryButton(text = "Đóng", onClick = onDismiss, modifier = Modifier.weight(1f))
                    AppButton(text = "Chuyển quyền", onClick = onOpenTransferCaptain, modifier = Modifier.weight(1f))
                }
            } else {
                Text(
                    text =
                        "Bạn sẽ không còn là thành viên của đội. " +
                            "Muốn quay lại, bạn cần được đội trưởng mời lại.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArenaColors.TextMuted,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppSecondaryButton(text = "Hủy", onClick = onDismiss, modifier = Modifier.weight(1f))
                    AppDangerButton(
                        text = "Rời đội",
                        onClick = onConfirmLeave,
                        isLoading = isSubmitting,
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DisbandTeamSheet(
    team: TeamDetail,
    isSubmitting: Boolean,
    onConfirmDisband: (reason: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confirmationName by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val isNameConfirmed = confirmationName.trim() == team.teamName

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Giải tán đội ${team.teamName}?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.Error,
            )
            InlineNotice(
                text =
                    "Giải tán là vĩnh viễn và không thể khôi phục. " +
                        "Mọi thành viên (kể cả bạn) sẽ rời đội, các lời mời đang chờ sẽ bị hủy.",
            )
            OutlinedTextField(
                value = confirmationName,
                onValueChange = { confirmationName = it },
                label = { Text("Nhập lại tên đội \"${team.teamName}\" để xác nhận") },
                singleLine = true,
                shape = ArenaShapes.Input,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it.take(100) },
                label = {
                    Text("Lý do giải tán (tối đa 100 ký tự)")
                },
                placeholder = { Text("Để trống sẽ ghi: Đội trưởng giải tán đội") },
                maxLines = 2,
                shape = ArenaShapes.Input,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppSecondaryButton(text = "Hủy", onClick = onDismiss, modifier = Modifier.weight(1f))
                AppDangerButton(
                    text = "Giải tán đội",
                    onClick = { onConfirmDisband(reason.trim().ifBlank { null }) },
                    isLoading = isSubmitting,
                    enabled = isNameConfirmed && !isSubmitting,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
