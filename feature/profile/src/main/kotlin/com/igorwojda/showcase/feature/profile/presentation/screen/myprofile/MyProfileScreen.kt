@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList", "MagicNumber", "StringLiteralDuplication", "UnusedImports")

package com.igorwojda.showcase.feature.profile.presentation.screen.myprofile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.ActingTeamOption
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppOutlinedButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.KeyValueRow
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyProfileScreen(
    modifier: Modifier = Modifier,
    onEditProfileClick: () -> Unit = {},
    onAccountMenuClick: () -> Unit = {},
    onReputationClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    onMyApplicationsClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: MyProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    MyProfileContent(
        modifier = modifier,
        state = state,
        onRetryClick = viewModel::loadProfile,
        onEditProfileClick = onEditProfileClick,
        onAccountMenuClick = onAccountMenuClick,
        onReputationClick = onReputationClick,
        onReportsClick = onReportsClick,
        onTeamClick = onTeamClick,
        onMyApplicationsClick = onMyApplicationsClick,
        onAdminClick = onAdminClick,
        onLoginClick = onLoginClick,
    )
}

@Composable
private fun MyProfileContent(
    state: MyProfileUiState,
    onRetryClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAccountMenuClick: () -> Unit,
    onReputationClick: () -> Unit,
    onReportsClick: () -> Unit,
    onTeamClick: (String) -> Unit,
    onMyApplicationsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLoginClick: () -> Unit,
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
        ProfileHeaderBar(onAccountMenuClick = onAccountMenuClick)

        when {
            state.isLoading && state.profile == null -> {
                ArenaLoadingState(message = "Đang tải hồ sơ cá nhân...")
            }

            state.isGuest -> {
                ArenaEmptyState(
                    title = "Bạn đang ở chế độ Khách",
                    description = "Đăng nhập để xem hồ sơ cá nhân, điểm uy tín và quản lý đội bóng.",
                    icon = Icons.Outlined.PersonOff,
                    actionLabel = "Đăng nhập ngay",
                    onAction = onLoginClick,
                )
            }

            state.errorMessage != null && state.profile == null -> {
                ArenaErrorState(
                    title = "Không thể tải hồ sơ",
                    message = state.errorMessage,
                    errorCode = state.errorCode,
                    onRetryClick = onRetryClick,
                )
            }

            state.profile != null -> {
                ProfileIdentityCard(
                    profile = state.profile,
                    reputationScore = state.reputationScore,
                    matchCount = state.matchCount,
                    onEditProfileClick = onEditProfileClick,
                    onReputationClick = onReputationClick,
                )

                ReputationSummaryCard(
                    reputationScore = state.reputationScore,
                    matchCount = state.matchCount,
                    onReputationClick = onReputationClick,
                    onReportsClick = onReportsClick,
                )

                MyTeamsQuickCard(
                    teams = state.myTeams,
                    onTeamClick = onTeamClick,
                )

                QuickShortcutRowCard(
                    icon = Icons.Outlined.SportsSoccer,
                    title = "Đơn đá lẻ của tôi",
                    subtitle = "Theo dõi các trận đã ứng tuyển & được duyệt",
                    onClick = onMyApplicationsClick,
                )

                if (state.profile.role == AccountRole.ADMIN) {
                    AdminPortalCard(onAdminClick = onAdminClick)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderBar(
    onAccountMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Hồ sơ cá nhân",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        Surface(
            onClick = onAccountMenuClick,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = ArenaShapes.Button,
            color = Color.White,
            border = BorderStroke(1.dp, ArenaColors.Border),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Tài khoản",
                    tint = ArenaColors.TextPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Tài khoản",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArenaColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun ProfileIdentityCard(
    profile: UserProfile,
    reputationScore: Int,
    matchCount: Int,
    onEditProfileClick: () -> Unit,
    onReputationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ArenaAvatar(
                name = profile.displayName.ifBlank { profile.phoneNumber ?: "AF" },
                size = 80.dp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = profile.displayName.ifBlank { "Chưa đặt tên" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            val contactSubtitle =
                profile.phoneNumber
                    ?: profile.providers.firstOrNull()?.providerUserId
                    ?: ""
            if (contactSubtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = contactSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                profile.preferredPosition?.let { pos ->
                    AppBadge(text = pos.label, variant = BadgeVariant.Primary)
                }
                AppBadge(text = "Uy tín ${reputationScore}đ", variant = BadgeVariant.Success)
                AppBadge(text = "$matchCount trận", variant = BadgeVariant.Neutral)
                if (profile.role == AccountRole.ADMIN) {
                    AppBadge(text = "ADMIN", variant = BadgeVariant.Dark)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppOutlinedButton(
                    text = "Sửa hồ sơ",
                    onClick = onEditProfileClick,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Uy tín cá nhân",
                    onClick = onReputationClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        HorizontalDivider(color = ArenaColors.BorderSubtle)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Tiểu sử",
                style = MaterialTheme.typography.labelSmall,
                color = ArenaColors.TextMuted,
            )
            Text(
                text = profile.bio?.takeIf { it.isNotBlank() } ?: "Chưa cập nhật",
                style = MaterialTheme.typography.bodyMedium,
                color = if (profile.bio.isNullOrBlank()) ArenaColors.TextMuted else ArenaColors.TextPrimary,
            )
        }

        KeyValueRow(
            label = "Vị trí ưa thích",
            value = profile.preferredPosition?.label ?: "Chưa cập nhật",
        )
        KeyValueRow(
            label = "Ngày sinh",
            value = formatDisplayDate(profile.dateOfBirth),
        )
        KeyValueRow(
            label = "Giới tính",
            value = profile.gender?.label ?: "Chưa cập nhật",
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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
                    tint = ArenaColors.TextMuted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Khu vực",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArenaColors.TextPrimary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = profile.ward?.fullLabel ?: "Chưa cập nhật",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (profile.ward == null) ArenaColors.TextMuted else ArenaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ReputationSummaryCard(
    reputationScore: Int,
    matchCount: Int,
    onReputationClick: () -> Unit,
    onReportsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Uy tín & Kỷ luật",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                )
                Text(
                    text = "$reputationScore điểm uy tín · $matchCount trận đã tham gia",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
            Icon(
                imageVector = Icons.Outlined.MilitaryTech,
                contentDescription = null,
                tint = ArenaColors.Primary,
            )
        }

        HorizontalDivider(color = ArenaColors.BorderSubtle)

        ProfileMenuRow(
            icon = Icons.Outlined.MilitaryTech,
            label = "Xem lịch sử điểm uy tín",
            onClick = onReputationClick,
        )
        HorizontalDivider(color = ArenaColors.BorderSubtle)
        ProfileMenuRow(
            icon = Icons.Outlined.Flag,
            label = "Báo cáo & Tranh chấp liên quan",
            onClick = onReportsClick,
        )
    }
}

@Composable
private fun MyTeamsQuickCard(
    teams: List<ActingTeamOption>,
    onTeamClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Đội bóng của tôi (${teams.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            Icon(
                imageVector = Icons.Outlined.Groups,
                contentDescription = null,
                tint = ArenaColors.Primary,
            )
        }

        if (teams.isEmpty()) {
            Text(
                text = "Bạn chưa làm đội trưởng đội nào.",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                teams.forEach { team ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(ArenaShapes.Button)
                                .background(ArenaColors.SurfaceContainerLow)
                                .clickable { onTeamClick(team.id) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            ArenaAvatar(name = team.name, size = 36.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = team.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ArenaColors.TextHeading,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${team.wardName} · Uy tín ${team.reputationScore}đ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArenaColors.TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = "Mở chi tiết đội",
                            tint = ArenaColors.TextMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickShortcutRowCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(ArenaShapes.Button)
                            .background(ArenaColors.SurfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ArenaColors.Primary,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = ArenaColors.TextHeading,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.TextMuted,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = ArenaColors.TextMuted,
            )
        }
    }
}

@Composable
private fun AdminPortalCard(
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onAdminClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = ArenaShapes.Card,
        color = ArenaColors.PitchGreen,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color.White,
                )
                Column {
                    Text(
                        text = "Khu vực Quản trị (Admin Portal)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Quản lý đội, báo cáo vi phạm & sân bóng",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Mở Admin Portal",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ArenaColors.Primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = ArenaColors.TextPrimary,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = ArenaColors.TextMuted,
        )
    }
}

private fun formatDisplayDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "Chưa cập nhật"
    val parts = isoDate.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else isoDate
}
