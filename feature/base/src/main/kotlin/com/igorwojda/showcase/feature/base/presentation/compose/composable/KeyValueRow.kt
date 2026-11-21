package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors

/**
 * Key-value row with bottom divider matching docs/prototype/index.html:
 * - Padding: 13dp vertical
 * - Border bottom: 1px solid #E2E7DE
 */
@Composable
fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val dividerColor = ArenaColors.BorderSubtle

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (showDivider) {
                        Modifier.drawBehind {
                            drawLine(
                                color = dividerColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }
                    } else {
                        Modifier
                    },
                ).padding(vertical = 13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = ArenaColors.TextPrimary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextPrimary,
            )
        }
    }
}
