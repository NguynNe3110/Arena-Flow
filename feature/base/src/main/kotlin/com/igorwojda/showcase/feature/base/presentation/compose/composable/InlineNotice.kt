package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes

/**
 * Inline callout notice matching docs/prototype/index.html:
 * - Background: #F1F0DF
 * - Text: #172E27
 * - Radius: 12dp
 * - Padding: 14dp
 * - Line height: 1.6
 */
@Composable
fun InlineNotice(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(ArenaShapes.Notice)
                .background(ArenaColors.NoticeBg)
                .padding(14.dp),
    ) {
        Text(
            text = text,
            color = ArenaColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        )
    }
}
