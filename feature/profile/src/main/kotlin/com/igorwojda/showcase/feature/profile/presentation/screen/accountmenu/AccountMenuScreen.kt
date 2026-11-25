@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.profile.presentation.screen.accountmenu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppOutlinedButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.profile.domain.model.CaptainedTeam
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountMenuScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onReputationClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
    onMyTeamsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    onOpenTeam: (String) -> Unit = {},
    onLogoutComplete: () -> Unit = {},
    onAccountDeleteComplete: () -> Unit = {},
    viewModel: AccountMenuViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val currentOnLoggedOut by androidx.compose.runtime.rememberUpdatedState(onLogoutComplete)
    val currentOnAccountDeleted by androidx.compose.runtime.rememberUpdatedState(onAccountDeleteComplete)

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) currentOnLoggedOut()
    }
    LaunchedEffect(state.isAccountDeleted) {
        if (state.isAccountDeleted) currentOnAccountDeleted()
    }

    AccountMenuContent(
        modifier = modifier,
        state = state,
        onBackClick = onBackClick,
        onEditProfileClick = onEditProfileClick,
        onReputationClick = onReputationClick,
        onReportsClick = onReportsClick,
        onMyTeamsClick = onMyTeamsClick,
        onNotificationsClick = onNotificationsClick,
        onAdminClick = onAdminClick,
        onOpenLogoutSheet = viewModel::openLogoutSheet,
        onDismissLogoutSheet = viewModel::dismissLogoutSheet,
        onConfirmLogout = viewModel::confirmLogout,
        onOpenDeleteConfirmation = viewModel::openDeleteConfirmation,
        onDismissDeleteConfirmation = viewModel::dismissDeleteConfirmation,
        onConfirmDeleteAccount = viewModel::confirmDeleteAccount,
        onDismissBlockedSheet = viewModel::dismissBlockedSheet,
        onOpenTeam = onOpenTeam,
        onRecheckTeams = viewModel::recheckBlockingTeams,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountMenuContent(
    state: AccountMenuUiState,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onReputationClick: () -> Unit,
    onReportsClick: () -> Unit,
    onMyTeamsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onOpenLogoutSheet: () -> Unit,
    onDismissLogoutSheet: () -> Unit,
    onConfirmLogout: () -> Unit,
    onOpenDeleteConfirmation: () -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    onConfirmDeleteAccount: () -> Unit,
    onDismissBlockedSheet: () -> Unit,
    onOpenTeam: (String) -> Unit,
    onRecheckTeams: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ArenaColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimen.spaceL, vertical = Dimen.spaceL),
        verticalArrangement = Arrangement.spacedBy(Dimen.stackGap),
    ) {
        Text(
            text = "Tài khoản & Cài đặt",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ArenaAvatar(name = state.displayName.ifBlank { "AF" }, size = 48.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.displayName.ifBlank { "Tài khoản ArenaFlow" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArenaColors.TextHeading,
                    )
                    val roleLabel = if (state.role == AccountRole.ADMIN) "Quản trị viên" else "Thành viên"
                    Text(
                        text = listOfNotNull(state.phone.takeIf { it.isNotBlank() }, roleLabel).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.TextMuted,
                    )
                }
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Xem hồ sơ", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            AccountMenuRow(
                icon = Icons.Outlined.Edit,
                label = "Sửa thông tin cá nhân",
                onClick = onEditProfileClick,
            )
            HorizontalDivider(color = ArenaColors.BorderSubtle)
            AccountMenuRow(
                icon = Icons.Outlined.MilitaryTech,
                label = "Uy tín cá nhân",
                onClick = onReputationClick,
            )
            HorizontalDivider(color = ArenaColors.BorderSubtle)
            AccountMenuRow(
                icon = Icons.Outlined.Flag,
                label = "Báo cáo & Tranh chấp liên quan",
                onClick = onReportsClick,
            )
            HorizontalDivider(color = ArenaColors.BorderSubtle)
            AccountMenuRow(
                icon = Icons.Outlined.Groups,
                label = "Đội bóng của tôi",
                onClick = onMyTeamsClick,
            )
            HorizontalDivider(color = ArenaColors.BorderSubtle)
            AccountMenuRow(
                icon = Icons.Outlined.Notifications,
                label = "Trung tâm thông báo",
                onClick = onNotificationsClick,
            )
            if (state.role == AccountRole.ADMIN) {
                HorizontalDivider(color = ArenaColors.BorderSubtle)
                AccountMenuRow(
                    icon = Icons.Outlined.AdminPanelSettings,
                    label = "Khu vực Quản trị (Admin)",
                    tint = ArenaColors.PitchGreen,
                    onClick = onAdminClick,
                )
            }
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            AccountMenuRow(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                label = "Hỗ trợ: support@arenaflow.vn",
                tint = ArenaColors.TextMuted,
                showChevron = false,
                onClick = {},
            )
            HorizontalDivider(color = ArenaColors.BorderSubtle)
            AccountMenuRow(
                icon = Icons.AutoMirrored.Outlined.Logout,
                label = "Đăng xuất",
                onClick = onOpenLogoutSheet,
            )
            HorizontalDivider(color = ArenaColors.BorderSubtle)
            AccountMenuRow(
                icon = Icons.Outlined.DeleteForever,
                label = "Xoá tài khoản",
                tint = ArenaColors.ErrorBright,
                textColor = ArenaColors.ErrorBright,
                onClick = onOpenDeleteConfirmation,
            )
        }

        state.errorMessage?.let { err ->
            Surface(
                shape = ArenaShapes.Button,
                color = ArenaColors.ErrorBg,
                border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.ErrorBright,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }

    if (state.showLogoutSheet) {
        ModalBottomSheet(onDismissRequest = onDismissLogoutSheet) {
            LogoutConfirmationSheetContent(
                onConfirm = onConfirmLogout,
                onCancel = onDismissLogoutSheet,
            )
        }
    }

    if (state.showDeleteConfirmDialog) {
        DeleteAccountConfirmDialog(
            isDeleting = state.isDeleting,
            onConfirm = onConfirmDeleteAccount,
            onDismiss = onDismissDeleteConfirmation,
        )
    }

    if (state.showBlockedCaptainSheet) {
        ModalBottomSheet(onDismissRequest = onDismissBlockedSheet) {
            BlockedCaptainDeleteSheetContent(
                teams = state.blockingTeams,
                isRechecking = state.isRecheckingTeams,
                onOpenTeam = onOpenTeam,
                onRecheck = onRecheckTeams,
                onProceedDelete = onConfirmDeleteAccount,
                onDismiss = onDismissBlockedSheet,
            )
        }
    }
}

@Composable
private fun AccountMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = ArenaColors.Primary,
    textColor: Color = ArenaColors.TextPrimary,
    showChevron: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = ArenaColors.TextMuted,
            )
        }
    }
}

@Composable
private fun LogoutConfirmationSheetContent(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ArenaColors.SurfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = ArenaColors.Primary,
            )
        }
        Text(
            text = "Đăng xuất khỏi ArenaFlow?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        Text(
            text = "Phiên đăng nhập hiện tại và bộ nhớ đệm của tài khoản sẽ được xóa an toàn trên thiết bị này.",
            style = MaterialTheme.typography.bodySmall,
            color = ArenaColors.TextMuted,
            textAlign = TextAlign.Center,
        )
        AppButton(
            text = "Đăng xuất",
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
        )
        AppOutlinedButton(
            text = "Huỷ",
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DeleteAccountConfirmDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = ArenaColors.ErrorBright,
            )
        },
        title = {
            Text(
                text = "Xoá tài khoản?",
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
        },
        text = {
            Text(
                text =
                    "Bạn sẽ bị đăng xuất và rời mọi đội đang tham gia. Các lời mời vào đội đang chờ sẽ bị từ chối. " +
                        "Ở lịch sử các đội, tên bạn sẽ hiển thị là “Người dùng đã xóa”.",
                style = MaterialTheme.typography.bodyMedium,
                color = ArenaColors.TextPrimary,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = ArenaShapes.Button,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = ArenaColors.ErrorBright,
                        contentColor = Color.White,
                    ),
            ) {
                Text(if (isDeleting) "Đang xử lý..." else "Xoá tài khoản")
            }
        },
        dismissButton = {
            AppOutlinedButton(
                text = "Huỷ",
                onClick = onDismiss,
                enabled = !isDeleting,
            )
        },
    )
}

@Composable
private fun BlockedCaptainDeleteSheetContent(
    teams: List<CaptainedTeam>,
    isRechecking: Boolean,
    onOpenTeam: (String) -> Unit,
    onRecheck: () -> Unit,
    onProceedDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = ArenaShapes.Card,
            color = ArenaColors.ErrorBg,
            border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Block,
                    contentDescription = null,
                    tint = ArenaColors.ErrorBright,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Không thể xoá tài khoản (422 USER_003)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArenaColors.ErrorBright,
                    )
                    Text(
                        text =
                            "Bạn đang làm đội trưởng của ${teams.size} đội chưa giải tán. " +
                                "Hãy chuyển quyền đội trưởng hoặc giải tán từng đội trước.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.ErrorBright,
                    )
                }
            }
        }

        if (teams.isEmpty()) {
            ArenaEmptyState(
                title = "Đã đủ điều kiện xóa tài khoản",
                description = "Bạn không còn giữ vai trò đội trưởng ở đội nào.",
                icon = Icons.Outlined.CheckCircle,
                actionLabel = "Tiếp tục xoá tài khoản",
                onAction = onProceedDelete,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                teams.forEach { team ->
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                ArenaAvatar(name = team.teamName, size = 40.dp)
                                Column {
                                    Text(
                                        text = team.teamName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ArenaColors.TextHeading,
                                    )
                                    Text(
                                        text = "${team.memberCount} thành viên",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArenaColors.TextMuted,
                                    )
                                }
                            }
                            AppBadge(
                                text = if (team.status == "INACTIVE") "Bị khoá" else "Hoạt động",
                                variant = if (team.status == "INACTIVE") BadgeVariant.Warning else BadgeVariant.Success,
                            )
                        }
                        AppButton(
                            text = "Mở đội để chuyển quyền / giải tán",
                            onClick = { onOpenTeam(team.teamId) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppOutlinedButton(
                text = "Đóng",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            AppButton(
                text = "Kiểm tra lại",
                onClick = onRecheck,
                isLoading = isRechecking,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
