@file:Suppress("MagicNumber", "MatchingDeclarationName")

package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors

enum class BadgeVariant {
    Green,
    Primary,
    Success,
    Amber,
    Warning,
    Error,
    Info,
    Dark,
    Neutral,
}

/** Status and category badge aligned with Prototype v2 (ArenaUI.badge). */
@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Green,
) {
    val (backgroundColor, textColor, borderColor) =
        when (variant) {
            BadgeVariant.Green, BadgeVariant.Primary -> {
                Triple(ArenaColors.BadgeGreenBg, ArenaColors.BadgeGreenText, Color(0xFFAFE1B1))
            }

            BadgeVariant.Success -> {
                Triple(ArenaColors.SuccessBg, ArenaColors.Success, ArenaColors.SuccessBorder)
            }

            BadgeVariant.Amber, BadgeVariant.Warning -> {
                Triple(ArenaColors.WarningBg, ArenaColors.Warning, ArenaColors.WarningBorder)
            }

            BadgeVariant.Error -> {
                Triple(ArenaColors.ErrorBg, ArenaColors.ErrorBright, ArenaColors.ErrorBorder)
            }

            BadgeVariant.Info -> {
                Triple(ArenaColors.InfoBg, ArenaColors.Info, ArenaColors.InfoBorder)
            }

            BadgeVariant.Dark -> {
                Triple(ArenaColors.PitchGreen, Color.White, ArenaColors.PitchGreen)
            }

            BadgeVariant.Neutral -> {
                Triple(ArenaColors.SurfaceContainer, ArenaColors.TextMuted, ArenaColors.Border)
            }
        }

    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.dp, borderColor, CircleShape)
                .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** Helper that resolves standard match status text to the corresponding badge variant. */
@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    val variant =
        when (text) {
            "Chưa có sân", "Đang thương lượng", "Chờ duyệt", "PENDING", "INACTIVE" -> BadgeVariant.Warning
            "Đã hủy", "Từ chối", "DISBANDED", "REJECTED", "CANCELLED" -> BadgeVariant.Error
            else -> BadgeVariant.Primary
        }
    AppBadge(text = text, modifier = modifier, variant = variant)
}
