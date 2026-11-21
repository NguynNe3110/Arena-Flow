package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors

/**
 * Page heading matching docs/prototype/index.html:
 * - Location / Tag line (small 13sp muted)
 * - Title h2 (30sp bold)
 * - Subtitle / description (14sp muted)
 */
@Composable
fun PageHeading(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    locationLabel: String? = null,
    tagLabel: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (locationLabel != null || tagLabel != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = locationLabel.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
                Text(
                    text = tagLabel.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextPrimary,
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ArenaColors.TextMuted,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
