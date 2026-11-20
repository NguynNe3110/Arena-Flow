package com.uzuu.match.presentation.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType

@Composable
internal fun MatchCard (
    match: MatchSummary,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = when (match.type) {
                MatchType.HAS_VENUE -> "Đã có sân"
                MatchType.NEED_VENUE -> "Thương lượng sân và giờ"
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = match.authorTeamName,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = match.venueName ?: "Sân đang được thỏa thuận",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = if (match.skillLevels.isEmpty()) {
                "Mọi trình độ"
            } else {
                match.skillLevels.joinToString(", ")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
