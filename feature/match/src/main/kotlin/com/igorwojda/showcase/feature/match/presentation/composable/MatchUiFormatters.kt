@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.match.presentation.composable

import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.match.domain.model.CostSplit
import com.igorwojda.showcase.feature.match.domain.model.MatchStatus
import java.text.NumberFormat
import java.util.Locale

internal object MatchUiFormatters {
    fun formatLabel(format: String): String = PitchFormat.fromApiName(format)?.label ?: format

    fun statusLabel(status: String): String = MatchStatus.fromApiName(status).label

    fun statusBadgeVariant(status: String): BadgeVariant =
        when (status.uppercase()) {
            "OPEN" -> BadgeVariant.Primary
            "CONFIRMED" -> BadgeVariant.Success
            "ARCHIVED", "COMPLETED" -> BadgeVariant.Info
            "EXPIRED" -> BadgeVariant.Warning
            "CANCELLED" -> BadgeVariant.Error
            else -> BadgeVariant.Neutral
        }

    fun formatDateTimeRange(
        startsAt: String,
        endsAt: String,
    ): String {
        if (startsAt.length < 16) return startsAt
        val datePart = startsAt.substring(0, 10).split("-")
        val dateDisplay = if (datePart.size == 3) "${datePart[2]}/${datePart[1]}" else startsAt.take(10)
        val startHm = startsAt.substring(11, 16)
        val endHm = if (endsAt.length >= 16) endsAt.substring(11, 16) else ""
        return if (endHm.isNotBlank()) "$startHm - $endHm · $dateDisplay" else "$startHm · $dateDisplay"
    }

    fun formatVnd(amount: Int): String = "${NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount)}đ"

    fun costSplitLabel(costSplit: String?): String = CostSplit.labelFor(costSplit).ifBlank { "Thỏa thuận" }
}
