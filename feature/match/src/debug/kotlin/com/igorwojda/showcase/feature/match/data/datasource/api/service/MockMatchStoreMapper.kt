@file:Suppress("MagicNumber", "StringLiteralDuplication", "TooManyFunctions")

package com.igorwojda.showcase.feature.match.data.datasource.api.service

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreMatch
import com.igorwojda.showcase.feature.base.data.store.StoreMatchRequest
import com.igorwojda.showcase.feature.base.data.store.StoreTeam
import com.igorwojda.showcase.feature.base.data.store.StoreVenue
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchCostAgreementDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchDetailDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchSummaryDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchTeamSummaryDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchTimeOptionDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchVenueDto
import java.time.Duration

internal const val MIN_REPUTATION_SCORE = 40
internal const val MAX_ACTIVE_MATCHES = 5

internal fun StoreMatch.effectiveStatus(snapshot: ArenaStoreSnapshot): String {
    if (status == "OPEN") {
        val nowMs = VietnamDateTime.currentEpochMillis(snapshot.mockClockIso)
        val expMs = VietnamDateTime.parseToEpochMillis(expiresAt)
        if (expMs != null && expMs <= nowMs) {
            return "EXPIRED"
        }
    }
    return status
}

internal fun StoreMatch.effectiveStartsAt(): String {
    if (!startTime.isNullOrBlank()) {
        return "${matchDate}T${normalizeTime(startTime!!)}"
    }
    val firstSlot = proposedTimeSlots.firstOrNull()?.substringBefore("-")?.trim()
    return "${matchDate}T${normalizeTime(firstSlot ?: "18:00")}"
}

internal fun StoreMatch.effectiveEndsAt(): String {
    if (!endTime.isNullOrBlank()) {
        return "${matchDate}T${normalizeTime(endTime!!)}"
    }
    val firstSlot = proposedTimeSlots.firstOrNull()?.substringAfter("-")?.trim()
    return "${matchDate}T${normalizeTime(firstSlot ?: "20:00")}"
}

internal fun StoreMatch.toSummaryDto(snapshot: ArenaStoreSnapshot): MatchSummaryDto {
    val host = snapshot.teams.firstOrNull { it.id == hostTeamId }
    val guest = guestTeamId?.let { gid -> snapshot.teams.firstOrNull { it.id == gid } }
    val venue = venueId?.let { vid -> snapshot.venues.firstOrNull { it.id == vid } }
    val primaryWardId =
        venue?.wardId ?: proposedVenues.firstOrNull()?.let { pid ->
            snapshot.venues.firstOrNull { it.id == pid }?.wardId
        }
    val wardName = primaryWardId?.let { wid -> snapshot.wards.firstOrNull { it.id == wid }?.fullLabel }

    return MatchSummaryDto(
        matchId = id,
        type = kind,
        status = effectiveStatus(snapshot),
        sportId = "sport-football",
        sportName = "Bóng đá phong trào (Sân 5 / 7 / 11)",
        format = format.name,
        skillLevels = listOf(skillLevel.label),
        startsAt = effectiveStartsAt(),
        endsAt = effectiveEndsAt(),
        expiresAt = expiresAt,
        authorTeamId = hostTeamId,
        authorTeamName = host?.name ?: "Đội chủ nhà",
        authorTeamReliabilityScore = host?.reputationScore ?: 100,
        opponentTeamId = guestTeamId,
        opponentTeamName = guest?.name,
        venueId = venue?.id,
        venueName = venue?.name,
        venueAddress = venue?.address,
        wardId = primaryWardId,
        wardName = wardName,
        costSplitLabel = costSharing,
        venueOptionCount = if (kind == "NEED_VENUE") proposedVenues.size else null,
        timeOptionCount = if (kind == "NEED_VENUE") proposedTimeSlots.size else null,
        distanceKm = venue?.distanceKm,
        createdAt = createdAt,
    )
}

internal fun StoreMatch.toDetailDto(snapshot: ArenaStoreSnapshot): MatchDetailDto {
    val host = snapshot.teams.firstOrNull { it.id == hostTeamId }
    val guest = guestTeamId?.let { gid -> snapshot.teams.firstOrNull { it.id == gid } }
    val venue = venueId?.let { vid -> snapshot.venues.firstOrNull { it.id == vid } }
    val venueOpts = proposedVenues.mapNotNull { pid -> snapshot.venues.firstOrNull { it.id == pid }?.toMatchVenueDto() }
    val timeOpts =
        proposedTimeSlots.mapIndexed { idx, slot ->
            val parts = slot.split("-").map { it.trim() }
            val start = "${matchDate}T${normalizeTime(parts.getOrElse(0) { "18:00" })}"
            val end = "${matchDate}T${normalizeTime(parts.getOrElse(1) { "19:30" })}"
            MatchTimeOptionDto(timeOptionId = "$id-slot-$idx", startsAt = start, endsAt = end)
        }
    val confirmedPairing = snapshot.pairings.firstOrNull { it.matchId == id && it.status == "CONFIRMED" }

    return MatchDetailDto(
        matchId = id,
        type = kind,
        status = effectiveStatus(snapshot),
        sportId = "sport-football",
        sportName = "Bóng đá phong trào (Sân 5 / 7 / 11)",
        format = format.name,
        skillLevels = listOf(skillLevel.label),
        matchNature = notes.takeIf { it.isNotBlank() },
        startsAt = effectiveStartsAt(),
        endsAt = effectiveEndsAt(),
        expiresAt = expiresAt,
        authorTeam = host.toTeamSummaryDto(hostTeamId),
        opponentTeam = guest?.toTeamSummaryDto(guest.id),
        venue = venue?.toMatchVenueDto(),
        venueOptions = venueOpts,
        timeOptions = timeOpts,
        confirmedPairingId = confirmedPairing?.id,
        costAgreement =
            MatchCostAgreementDto(
                costType = if (totalCost > 0) "VENUE_ONLY" else null,
                costSplit = costSharing.takeIf { it.isNotBlank() },
                venueAmount = totalCost.takeIf { it > 0 },
                depositAmount = depositAmount.takeIf { it > 0 },
                depositHolder = depositHolder,
            ),
        createdAt = createdAt,
        updatedAt = createdAt,
    )
}

internal fun StoreMatchRequest.toDto(snapshot: ArenaStoreSnapshot): MatchRequestDto {
    val requester = snapshot.teams.firstOrNull { it.id == requesterTeamId }
    val match = snapshot.matches.firstOrNull { it.id == matchId }
    val author = match?.let { m -> snapshot.teams.firstOrNull { it.id == m.hostTeamId } }
    val displayStatus =
        if (status == "PENDING" && match?.effectiveStatus(snapshot) == "EXPIRED") {
            "EXPIRED"
        } else {
            status
        }
    return MatchRequestDto(
        requestId = id,
        matchId = matchId,
        conversationId = conversationId,
        requesterTeamId = requesterTeamId,
        requesterTeamName = requester?.name ?: "Đội khách",
        requesterTeamReliabilityScore = requester?.reputationScore ?: 100,
        status = displayStatus,
        reason = message.takeIf { it.isNotBlank() },
        requestedAt = createdAt,
        startsAt = match?.effectiveStartsAt(),
        authorTeamId = match?.hostTeamId,
        authorTeamName = author?.name,
    )
}

internal fun StoreVenue.toMatchVenueDto(): MatchVenueDto =
    MatchVenueDto(
        venueId = id,
        venueName = name,
        address = address,
        latitude = lat,
        longitude = lng,
        venueStatus = status,
    )

internal fun StoreTeam?.toTeamSummaryDto(fallbackId: String): MatchTeamSummaryDto =
    MatchTeamSummaryDto(
        teamId = this?.id ?: fallbackId,
        teamName = this?.name ?: "Đội bóng",
        reliabilityScore = this?.reputationScore ?: 100,
    )

internal fun computeReliabilityPenalty(
    startsAt: String,
    nowIso: String,
): Int {
    val startDt = VietnamDateTime.parseToLocalDateTime(startsAt) ?: return -2
    val nowDt = VietnamDateTime.parseToLocalDateTime(nowIso) ?: return -2
    val hours = Duration.between(nowDt, startDt).toHours()
    return when {
        hours >= 24 -> -2
        hours >= 4 -> -5
        else -> -10
    }
}

internal fun isMatchStarted(
    startsAt: String,
    nowIso: String,
): Boolean {
    val startMs = VietnamDateTime.parseToEpochMillis(startsAt) ?: return false
    val nowMs = VietnamDateTime.currentEpochMillis(nowIso)
    return nowMs >= startMs
}

private fun normalizeTime(raw: String): String {
    val trimmed = raw.trim()
    return when {
        trimmed.length == 5 -> "$trimmed:00"
        trimmed.isBlank() -> "18:00:00"
        else -> trimmed
    }
}
