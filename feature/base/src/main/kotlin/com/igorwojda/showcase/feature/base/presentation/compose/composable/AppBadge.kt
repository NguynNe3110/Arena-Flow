@file:Suppress("MatchingDeclarationName")

package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes

enum class BadgeVariant {
    Green,
    Amber,
    Neutral,
}

/** Status and category badge aligned with docs/prototype/index.html. */
@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Green,
) {
    val (backgroundColor, textColor) =
        when (variant) {
            BadgeVariant.Green -> ArenaColors.BadgeGreenBg to ArenaColors.BadgeGreenText
            BadgeVariant.Amber -> ArenaColors.BadgeAmberBg to ArenaColors.BadgeAmberText
            BadgeVariant.Neutral -> ArenaColors.SecondaryButtonBg to ArenaColors.TextMuted
        }

    Box(
        modifier =
            modifier
                .clip(ArenaShapes.Badge)
                .background(backgroundColor)
                .padding(horizontal = 9.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
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
            "Chưa có sân", "Đang thương lượng" -> BadgeVariant.Amber
            else -> BadgeVariant.Green
        }
    AppBadge(text = text, modifier = modifier, variant = variant)
}
