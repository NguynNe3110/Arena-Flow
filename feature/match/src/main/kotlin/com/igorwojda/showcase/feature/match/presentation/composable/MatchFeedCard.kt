@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.match.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.SportsSoccer
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
import com.igorwojda.showcase.feature.match.domain.model.MatchSummary
import com.igorwojda.showcase.feature.match.domain.model.MatchType

/** Match feed card displaying team, ward, time, skill, venue, fee split, and quick action. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchFeedCard(
    match: MatchSummary,
    isOwnTeamMatch: Boolean,
    onClick: () -> Unit,
    onQuickRequestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFlowA = match.type == MatchType.HAS_VENUE
    val venueText =
        if (isFlowA) {
            listOfNotNull(match.venueName, match.wardName).joinToString(" · ").ifBlank { "Đã có sân" }
        } else {
            "Chưa có sân (${match.venueOptionCount} sân · ${match.timeOptionCount} khung giờ)"
        }

    AppCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MatchCardHeader(match = match)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MatchInfoLine(
                icon = Icons.Outlined.AccessTime,
                text = MatchUiFormatters.formatDateTimeRange(match.startsAt, match.endsAt),
            )
            MatchInfoLine(
                icon = Icons.Outlined.LocationOn,
                text = venueText,
            )
            MatchInfoLine(
                icon = Icons.Outlined.SportsSoccer,
                text =
                    listOfNotNull(
                        MatchUiFormatters.formatLabel(match.format),
                        match.skillLevels.firstOrNull() ?: "Mọi trình độ",
                    ).joinToString(" · "),
            )
            if (!match.costSplitLabel.isNullOrBlank()) {
                MatchInfoLine(
                    icon = Icons.Outlined.Payments,
                    text = MatchUiFormatters.costSplitLabel(match.costSplitLabel),
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AppBadge(
                text = if (isFlowA) "Đã có sân" else "Chưa có sân",
                variant = if (isFlowA) BadgeVariant.Green else BadgeVariant.Warning,
            )
            if (match.status != "OPEN") {
                AppBadge(
                    text = MatchUiFormatters.statusLabel(match.status),
                    variant = MatchUiFormatters.statusBadgeVariant(match.status),
                )
            }
            if (match.distanceKm != null) {
                AppBadge(
                    text = "${match.distanceKm} km",
                    variant = BadgeVariant.Neutral,
                )
            }
        }

        if (match.status == "OPEN" && !isOwnTeamMatch) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppSecondaryButton(
                    text = "Chi tiết",
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = if (isFlowA) "Xin kèo" else "Nhận kèo",
                    onClick = onQuickRequestClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MatchCardHeader(match: MatchSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = match.authorTeamName, size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.authorTeamName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!match.wardName.isNullOrBlank()) {
                    Text(
                        text = match.wardName,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        AppBadge(
            text = "Uy tín ${match.authorTeamReliabilityScore}",
            variant = if (match.authorTeamReliabilityScore >= 70) BadgeVariant.Success else BadgeVariant.Warning,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun MatchInfoLine(
    icon: ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ArenaColors.TextMuted,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
