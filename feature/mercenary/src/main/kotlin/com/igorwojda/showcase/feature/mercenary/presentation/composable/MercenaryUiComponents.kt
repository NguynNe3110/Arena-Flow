@file:Suppress("LongMethod", "MagicNumber")

package com.igorwojda.showcase.feature.mercenary.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.TeamAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplicationStatus
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostStatus
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostSummary
import java.text.NumberFormat
import java.util.Locale

/** Formats VND allowance into a concise Vietnamese currency label. */
internal fun formatAllowanceVnd(amount: Int): String {
    if (amount <= 0) return "Hỗ trợ nước uống"
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    return "${formatter.format(amount)}đ / trận"
}

/** Formats ISO-8601 Vietnam datetime into concise display form. */
internal fun formatMatchDateTime(iso: String): String {
    val datePart = iso.substringBefore("T")
    val timePart = iso.substringAfter("T", "").take(5)
    val segments = datePart.split("-")
    val shortDate =
        if (segments.size == 3) {
            "${segments[2]}/${segments[1]}"
        } else {
            datePart
        }
    return if (timePart.isNotBlank()) "$timePart · $shortDate" else shortDate
}

/** Card displaying a mercenary recruitment post summary with quick apply or detail action. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MercenaryPostCard(
    post: MercenaryPostSummary,
    onCardClick: () -> Unit,
    onQuickApplyClick: () -> Unit,
    modifier: Modifier = Modifier,
    showQuickApply: Boolean = true,
) {
    AppCard(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = post.teamName, size = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.teamName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${post.format.label} · Uy tín ${post.teamReputationScore}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
            MercenaryPostStatusBadge(status = post.status, slotsFilled = post.slotsFilled, slotsNeeded = post.slotsNeeded)
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            post.neededPositions.forEach { pos ->
                AppBadge(text = pos.label, variant = BadgeVariant.Primary)
            }
            AppBadge(text = post.skillLevel.label, variant = BadgeVariant.Neutral)
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MetaIconRow(
                icon = Icons.Outlined.Schedule,
                text = formatMatchDateTime(post.matchStartAt),
            )
            val distanceSuffix = post.distanceKm?.let { " (~$it km)" }.orEmpty()
            MetaIconRow(
                icon = Icons.Outlined.LocationOn,
                text = "${post.areaLabel}$distanceSuffix",
            )
            MetaIconRow(
                icon = Icons.Outlined.Payments,
                text = formatAllowanceVnd(post.allowanceVnd),
            )
        }

        if (post.notes.isNotBlank()) {
            Text(
                text = post.notes,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppSecondaryButton(
                text = "Xem chi tiết",
                onClick = onCardClick,
                modifier = Modifier.weight(1f),
            )
            if (showQuickApply) {
                val appStatus = post.myApplicationStatus
                val canApply = post.status == MercenaryPostStatus.OPEN && appStatus == null
                val buttonLabel =
                    when (appStatus) {
                        MercenaryApplicationStatus.PENDING -> "Đã gửi đơn"
                        MercenaryApplicationStatus.APPROVED -> "Đã duyệt"
                        MercenaryApplicationStatus.REJECTED -> "Bị từ chối"
                        MercenaryApplicationStatus.WITHDRAWN, null -> "Ứng tuyển"
                    }
                AppButton(
                    text = buttonLabel,
                    onClick = onQuickApplyClick,
                    enabled = canApply,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Card displaying one of the current user's mercenary applications. */
@Composable
internal fun MyApplicationCard(
    application: MercenaryApplication,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = application.postTitle.ifBlank { application.teamName },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Vị trí: ${application.position.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
            ApplicationStatusBadge(status = application.status)
        }

        MetaIconRow(
            icon = Icons.Outlined.Schedule,
            text = formatMatchDateTime(application.matchStartAt),
        )
        if (application.areaLabel.isNotBlank()) {
            MetaIconRow(
                icon = Icons.Outlined.LocationOn,
                text = application.areaLabel,
            )
        }
        if (application.note.isNotBlank()) {
            Text(
                text = "Ghi chú: ${application.note}",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Badge showing recruitment status and filled/needed slot counts. */
@Composable
internal fun MercenaryPostStatusBadge(
    status: MercenaryPostStatus,
    slotsFilled: Int,
    slotsNeeded: Int,
    modifier: Modifier = Modifier,
) {
    val variant =
        when (status) {
            MercenaryPostStatus.OPEN -> BadgeVariant.Success
            MercenaryPostStatus.FILLED -> BadgeVariant.Info
            MercenaryPostStatus.CLOSED, MercenaryPostStatus.EXPIRED -> BadgeVariant.Neutral
        }
    AppBadge(
        text = "${status.label} ($slotsFilled/$slotsNeeded)",
        variant = variant,
        modifier = modifier,
    )
}

/** Badge showing application lifecycle status. */
@Composable
internal fun ApplicationStatusBadge(
    status: MercenaryApplicationStatus,
    modifier: Modifier = Modifier,
) {
    val variant =
        when (status) {
            MercenaryApplicationStatus.PENDING -> BadgeVariant.Warning
            MercenaryApplicationStatus.APPROVED -> BadgeVariant.Success
            MercenaryApplicationStatus.REJECTED -> BadgeVariant.Error
            MercenaryApplicationStatus.WITHDRAWN -> BadgeVariant.Neutral
        }
    AppBadge(text = status.label, variant = variant, modifier = modifier)
}

@Composable
private fun MetaIconRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ArenaColors.TextMuted,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = ArenaColors.TextPrimary,
        )
    }
}
