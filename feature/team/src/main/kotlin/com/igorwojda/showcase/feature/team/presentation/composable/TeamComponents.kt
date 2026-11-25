@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.team.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import java.time.format.DateTimeFormatter

private val dateShortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val dateTimeShortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

internal fun formatIsoDateShort(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return VietnamDateTime.parseToLocalDateTime(iso)?.format(dateShortFormatter) ?: iso.take(10)
}

internal fun formatIsoDateTimeShort(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return VietnamDateTime.parseToLocalDateTime(iso)?.format(dateTimeShortFormatter) ?: iso
}

@Composable
internal fun TeamStatusBadge(
    status: TeamStatus,
    modifier: Modifier = Modifier,
) {
    val (label, variant) =
        when (status) {
            TeamStatus.ACTIVE -> "• Hoạt động" to BadgeVariant.Success
            TeamStatus.INACTIVE -> "• Bị khóa" to BadgeVariant.Warning
            TeamStatus.DISBANDED -> "Đã giải tán" to BadgeVariant.Neutral
        }
    AppBadge(text = label, modifier = modifier, variant = variant)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SkillLabelChips(
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    if (labels.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        labels.forEach { label ->
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ArenaColors.SurfaceContainer)
                        .border(1.dp, ArenaColors.BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
internal fun AppDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled && !isLoading,
        shape = ArenaShapes.Button,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = ArenaColors.Error,
                contentColor = Color.White,
                disabledContainerColor = ArenaColors.Error.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.8f),
            ),
        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 12.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
