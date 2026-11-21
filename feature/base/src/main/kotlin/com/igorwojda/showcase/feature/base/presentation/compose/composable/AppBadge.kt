package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes

enum class BadgeVariant {
    Green,
    Amber,
    Neutral,
}

/**
 * Status and category badge aligned with docs/prototype/index.html:
 * - Green: bg #DFEFDF, text #155B39 (e.g. "Đã có sân", "Đang tìm đối", "Đã ghép")
 * - Amber: bg #FFF0CE, text #6B4A08 (e.g. "Chưa có sân", "Đang thương lượng")
 * - Neutral: bg #E7EEE6, text #52675D
 * - Radius: 7dp
 * - Font: 12sp Bold
 * - Padding: 5dp vertical, 9dp horizontal
 */
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
