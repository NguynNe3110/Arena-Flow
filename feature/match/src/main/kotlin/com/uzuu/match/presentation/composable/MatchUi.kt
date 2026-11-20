package com.uzuu.match.presentation.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.uzuu.match.domain.model.MatchSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val MatchShape = RoundedCornerShape(12.dp)
internal val ControlShape = RoundedCornerShape(8.dp)

internal fun MatchSummary.formatLabel() = when (format) {
    "FIVE_A_SIDE" -> "Sân 5"
    "ELEVEN_A_SIDE" -> "Sân 11"
    else -> "Sân 7"
}

internal fun MatchSummary.statusLabel() = when (status) {
    "CONFIRMED" -> "Đã ghép kèo"
    "IN_PROGRESS" -> "Đang diễn ra"
    "COMPLETED", "ARCHIVED" -> "Kết thúc"
    "CANCELLED" -> "Đã hủy"
    "EXPIRED" -> "Hết hạn"
    else -> "Đang tìm đối"
}

internal fun localDateTime(value: String, pattern: String = "dd/MM/yyyy · HH:mm"): String =
    runCatching {
        DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("vi-VN"))
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
            .format(Instant.parse(value))
    }.getOrDefault("Chưa xác định")

internal fun MatchSummary.timeLabel() =
    if (startsAt.isBlank()) "Thời gian thỏa thuận"
    else "${localDateTime(startsAt)} – ${localDateTime(endsAt, "HH:mm")}"

@Composable
internal fun MatchBadge(text: String, modifier: Modifier = Modifier, highlighted: Boolean = false) {
    Surface(
        modifier = modifier,
        shape = ControlShape,
        color = if (highlighted) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (highlighted) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun MatchStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text) = when (status) {
        "CONFIRMED" -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, "✓ Đã ghép kèo")
        "IN_PROGRESS" -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, "● Đang diễn ra")
        "CANCELLED" -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "✕ Đã hủy")
        "EXPIRED" -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "⏰ Hết hạn")
        "COMPLETED", "ARCHIVED" -> Triple(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurfaceVariant, "Kết thúc")
        else -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "● Đang tìm đối")
    }
    Surface(
        modifier = modifier,
        shape = ControlShape,
        color = bgColor,
        contentColor = textColor,
    ) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun MatchSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MatchShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
internal fun MatchInfo(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
