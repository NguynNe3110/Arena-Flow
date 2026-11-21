@file:Suppress("TooManyFunctions", "MagicNumber")

package com.uzuu.match.presentation.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DEFAULT_TIME_NEED_VENUE = "21/11 · Từ 18:00"
private const val DEFAULT_TIME_HAS_VENUE = "21/11 · 19:00–20:30"
private const val TAG_SEVEN_PLAYERS = "7 người"
private const val TAG_FIVE_PLAYERS = "5 người"
private const val TAG_ELEVEN_PLAYERS = "11 người"
private const val LEVEL_MEDIUM = "Trung bình"
private const val DEFAULT_VENUE_NAME = "Sân An Phú"
private const val DEFAULT_VENUE_COUNT = "2 địa điểm"
private const val DEFAULT_TIME_COUNT = "2 khung giờ đề xuất"
private const val PREVIEW_STARTS_AT_A = "2026-11-21T19:00:00Z"
private const val PREVIEW_ENDS_AT_A = "2026-11-21T20:30:00Z"
private const val PREVIEW_STARTS_AT_B = "2026-11-21T18:00:00Z"
private const val DATE_PATTERN_DAY_MONTH = "dd/MM"
private const val TIME_PATTERN_HOUR_MINUTE = "HH:mm"
private const val LOCALE_VIETNAMESE = "vi-VN"
private const val ZONE_VIETNAM = "Asia/Ho_Chi_Minh"

@Composable
internal fun MatchCard(
    match: MatchSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isNeedVenue = match.type == MatchType.NEED_VENUE

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFDCE3D8)),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MatchCardHeader(isNeedVenue = isNeedVenue, formatTag = match.formatTag())
            MatchCardTeam(
                authorTeamName = match.authorTeamName,
                reliabilityScore = match.reliabilityScore,
            )
            Text(
                text = formatMatchTime(match),
                color = Color(0xFF172E27),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            MatchCardDetails(
                areaText = matchAreaText(match),
                levelText = "Trình độ: ${match.skillLevels.firstOrNull() ?: LEVEL_MEDIUM}",
            )
            MatchCardAction(isNeedVenue = isNeedVenue, onClick = onClick)
        }
    }
}

@Composable
private fun MatchCardHeader(
    isNeedVenue: Boolean,
    formatTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(7.dp),
            color = if (isNeedVenue) Color(0xFFFFF0CE) else Color(0xFFDFEFDF),
        ) {
            Text(
                text = if (isNeedVenue) "Chưa có sân" else "Đã có sân",
                color = if (isNeedVenue) Color(0xFF6B4A08) else Color(0xFF155B39),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            )
        }
        Text(
            text = formatTag,
            color = Color(0xFF52675D),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun MatchCardTeam(
    authorTeamName: String,
    reliabilityScore: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
        ) {
            Text(
                text = authorTeamName,
                color = Color(0xFF172E27),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Uy tín $reliabilityScore/100 · 4,8/5",
                color = Color(0xFF52675D),
                fontSize = 13.sp,
            )
        }

        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF203D31)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = extractTeamInitials(authorTeamName),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun MatchCardDetails(
    areaText: String,
    levelText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = areaText,
            color = Color(0xFF52675D),
            fontSize = 14.sp,
        )
        Text(
            text = levelText,
            color = Color(0xFF52675D),
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun MatchCardAction(
    isNeedVenue: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE7EEE6),
                contentColor = Color(0xFF172E27),
            ),
    ) {
        Text(
            text = if (isNeedVenue) "Xem lựa chọn & nhận kèo" else "Xem kèo",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun matchAreaText(match: MatchSummary): String =
    if (match.type == MatchType.NEED_VENUE) {
        val venues = if (match.venueOptionCount > 0) "${match.venueOptionCount} địa điểm" else DEFAULT_VENUE_COUNT
        val times = if (match.timeOptionCount > 0) "${match.timeOptionCount} khung giờ đề xuất" else DEFAULT_TIME_COUNT
        "$venues · $times"
    } else {
        val venue = match.venueName ?: DEFAULT_VENUE_NAME
        if (!match.venueAddress.isNullOrBlank()) "$venue · ${match.venueAddress}" else venue
    }

internal fun MatchSummary.formatTag(): String =
    when (format) {
        "FIVE_A_SIDE", "5" -> TAG_FIVE_PLAYERS
        "ELEVEN_A_SIDE", "11" -> TAG_ELEVEN_PLAYERS
        else -> TAG_SEVEN_PLAYERS
    }

internal fun extractTeamInitials(teamName: String): String {
    val clean = teamName.trim()
    if (clean.length < 2) return clean.uppercase().ifEmpty { "AF" }
    return clean.take(2).uppercase()
}

internal fun formatMatchTime(match: MatchSummary): String {
    if (match.startsAt.isBlank()) {
        return if (match.type == MatchType.NEED_VENUE) DEFAULT_TIME_NEED_VENUE else DEFAULT_TIME_HAS_VENUE
    }
    return runCatching {
        val startInstant = Instant.parse(match.startsAt)
        val zone = ZoneId.of(ZONE_VIETNAM)
        val startZdt = startInstant.atZone(zone)
        val locale = Locale.forLanguageTag(LOCALE_VIETNAMESE)
        val dateStr = DateTimeFormatter.ofPattern(DATE_PATTERN_DAY_MONTH, locale).format(startZdt)
        val startTimeStr = DateTimeFormatter.ofPattern(TIME_PATTERN_HOUR_MINUTE, locale).format(startZdt)
        if (match.type == MatchType.NEED_VENUE && match.timeOptionCount > 1) {
            "$dateStr · Từ $startTimeStr"
        } else if (match.endsAt.isNotBlank()) {
            val endInstant = Instant.parse(match.endsAt)
            val endZdt = endInstant.atZone(zone)
            val endTimeStr = DateTimeFormatter.ofPattern(TIME_PATTERN_HOUR_MINUTE, locale).format(endZdt)
            "$dateStr · $startTimeStr–$endTimeStr"
        } else {
            "$dateStr · $startTimeStr"
        }
    }.getOrElse {
        if (match.type == MatchType.NEED_VENUE) DEFAULT_TIME_NEED_VENUE else DEFAULT_TIME_HAS_VENUE
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchCardTypeAPreview() {
    MatchCard(
        match =
            MatchSummary(
                matchId = "preview-1",
                type = MatchType.HAS_VENUE,
                authorTeamName = "Thảo Điền FC",
                venueName = DEFAULT_VENUE_NAME,
                venueAddress = "Phường An Khánh",
                skillLevels = listOf(LEVEL_MEDIUM),
                reliabilityScore = 92,
                startsAt = PREVIEW_STARTS_AT_A,
                endsAt = PREVIEW_ENDS_AT_A,
            ),
        onClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun MatchCardTypeBPreview() {
    MatchCard(
        match =
            MatchSummary(
                matchId = "preview-2",
                type = MatchType.NEED_VENUE,
                authorTeamName = "Sài Gòn United",
                venueName = null,
                venueOptionCount = 2,
                timeOptionCount = 2,
                skillLevels = listOf(LEVEL_MEDIUM),
                reliabilityScore = 92,
                startsAt = PREVIEW_STARTS_AT_B,
            ),
        onClick = {},
    )
}
