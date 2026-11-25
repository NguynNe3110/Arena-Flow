@file:Suppress("TooManyFunctions")

package com.igorwojda.showcase.feature.match.data.mapper

import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.match.data.datasource.api.model.AcceptMatchRequestResponseDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CancelMatchResponseDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CostAgreementResponseDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CreateMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CreateMatchTimeOptionDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchCostAgreementDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchCostItemDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchDetailDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchSummaryDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchTeamSummaryDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchTimeOptionDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchVenueDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.SportDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.UpdateMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.WithdrawMatchRequestResponseDto
import com.igorwojda.showcase.feature.match.domain.model.AcceptRequestResult
import com.igorwojda.showcase.feature.match.domain.model.CancelMatchResult
import com.igorwojda.showcase.feature.match.domain.model.MatchCostAgreement
import com.igorwojda.showcase.feature.match.domain.model.MatchCostItem
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft
import com.igorwojda.showcase.feature.match.domain.model.MatchPage
import com.igorwojda.showcase.feature.match.domain.model.MatchRequest
import com.igorwojda.showcase.feature.match.domain.model.MatchSummary
import com.igorwojda.showcase.feature.match.domain.model.MatchTeamSummary
import com.igorwojda.showcase.feature.match.domain.model.MatchTimeSlot
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.domain.model.MatchVenue
import com.igorwojda.showcase.feature.match.domain.model.SportOption
import com.igorwojda.showcase.feature.match.domain.model.WithdrawRequestResult

internal fun MatchSummaryDto.toDomain(): MatchSummary =
    MatchSummary(
        matchId = matchId,
        type = if (type.equals(MatchType.NEED_VENUE.name, ignoreCase = true)) MatchType.NEED_VENUE else MatchType.HAS_VENUE,
        status = status,
        sportId = sportId,
        sportName = sportName,
        format = format,
        skillLevels = skillLevels,
        startsAt = startsAt,
        endsAt = endsAt,
        expiresAt = expiresAt,
        authorTeamId = authorTeamId,
        authorTeamName = authorTeamName,
        authorTeamReliabilityScore = authorTeamReliabilityScore,
        opponentTeamId = opponentTeamId,
        opponentTeamName = opponentTeamName,
        venueId = venueId,
        venueName = venueName,
        venueAddress = venueAddress,
        wardId = wardId,
        wardName = wardName,
        costSplitLabel = costSplitLabel,
        venueOptionCount = venueOptionCount ?: 0,
        timeOptionCount = timeOptionCount ?: 0,
        distanceKm = distanceKm,
        createdAt = createdAt,
    )

internal fun PaginatedEnvelope<MatchSummaryDto>.toDomain(): MatchPage =
    MatchPage(
        items = data.map { it.toDomain() },
        page = pagination.page,
        pageSize = pagination.pageSize,
        totalPages = pagination.totalPages,
        totalItems = pagination.totalItems,
    )

internal fun MatchTeamSummaryDto.toDomain(): MatchTeamSummary =
    MatchTeamSummary(
        teamId = teamId,
        teamName = teamName,
        avatarUrl = avatarUrl,
        reliabilityScore = reliabilityScore,
    )

internal fun MatchVenueDto.toDomain(): MatchVenue =
    MatchVenue(
        venueId = venueId,
        venueName = venueName,
        address = address,
        latitude = latitude,
        longitude = longitude,
        venueStatus = venueStatus,
        primaryImageUrl = primaryImageUrl,
    )

internal fun MatchTimeOptionDto.toDomain(): MatchTimeSlot =
    MatchTimeSlot(
        timeOptionId = timeOptionId,
        startsAt = startsAt,
        endsAt = endsAt,
    )

internal fun MatchCostItemDto.toDomain(): MatchCostItem =
    MatchCostItem(
        itemId = itemId,
        itemName = itemName,
        amount = amount,
    )

internal fun MatchCostAgreementDto.toDomain(): MatchCostAgreement =
    MatchCostAgreement(
        costType = costType,
        costSplit = costSplit,
        venueAmount = venueAmount,
        drinksAmount = drinksAmount,
        depositAmount = depositAmount,
        depositHolder = depositHolder,
        customItems = customItems.map { it.toDomain() },
    )

internal fun CostAgreementResponseDto.toDomain(): MatchCostAgreement =
    MatchCostAgreement(
        costType = costType,
        costSplit = costSplit,
        venueAmount = venueAmount,
        drinksAmount = drinksAmount,
        customItems = customItems.map { it.toDomain() },
    )

internal fun MatchDetailDto.toDomain(): MatchDetail =
    MatchDetail(
        matchId = matchId,
        type = if (type.equals(MatchType.NEED_VENUE.name, ignoreCase = true)) MatchType.NEED_VENUE else MatchType.HAS_VENUE,
        status = status,
        sportId = sportId,
        sportName = sportName,
        format = format,
        skillLevels = skillLevels,
        ageNote = ageNote,
        matchNature = matchNature,
        startsAt = startsAt,
        endsAt = endsAt,
        expiresAt = expiresAt,
        authorTeam = authorTeam.toDomain(),
        opponentTeam = opponentTeam?.toDomain(),
        venue = venue?.toDomain(),
        venueOptions = venueOptions.map { it.toDomain() },
        timeOptions = timeOptions.map { it.toDomain() },
        confirmedPairingId = confirmedPairingId,
        costAgreement = costAgreement.toDomain(),
        cancelledByTeamId = cancelledByTeamId,
        cancelledAt = cancelledAt,
        cancelReason = cancelReason,
        minReputationScore = minReputationScore,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun MatchRequestDto.toDomain(): MatchRequest =
    MatchRequest(
        requestId = requestId,
        matchId = matchId,
        conversationId = conversationId,
        requesterTeamId = requesterTeamId,
        requesterTeamName = requesterTeamName,
        requesterTeamReliabilityScore = requesterTeamReliabilityScore,
        status = status,
        reason = reason,
        requestedAt = requestedAt,
        respondedAt = respondedAt,
        withdrawnAt = withdrawnAt,
        startsAt = startsAt,
        authorTeamId = authorTeamId,
        authorTeamName = authorTeamName,
    )

internal fun CancelMatchResponseDto.toDomain(): CancelMatchResult =
    CancelMatchResult(
        matchId = matchId,
        status = status,
        cancelledByTeamId = cancelledByTeamId,
        cancelledAt = cancelledAt,
        cancelReason = cancelReason,
        reliabilityPenalty = reliabilityPenalty,
    )

internal fun AcceptMatchRequestResponseDto.toDomain(): AcceptRequestResult =
    AcceptRequestResult(
        matchId = matchId,
        matchStatus = matchStatus,
        requestId = requestId,
        requestStatus = requestStatus,
        opponentTeamId = opponentTeamId,
        opponentTeamName = opponentTeamName,
        respondedAt = respondedAt,
    )

internal fun WithdrawMatchRequestResponseDto.toDomain(): WithdrawRequestResult =
    WithdrawRequestResult(
        requestId = requestId,
        matchId = matchId,
        status = status,
        withdrawnAt = withdrawnAt,
        matchStatus = matchStatus,
        reliabilityPenalty = reliabilityPenalty,
    )

internal fun SportDto.toDomain(): SportOption =
    SportOption(
        sportId = sportId,
        sportName = sportName,
        isActive = isActive,
    )

internal fun MatchDraft.toCreateDto(): CreateMatchRequestDto {
    val isHasVenue = kind == MatchType.HAS_VENUE
    val normalizedStart = "${matchDate}T${normalizeTime(startTime)}"
    val normalizedEnd = "${matchDate}T${normalizeTime(endTime)}"
    val normalizedExpires = expiresAt.ifBlank { normalizedStart }
    return CreateMatchRequestDto(
        type = kind.name,
        teamId = hostTeamId,
        sportId = sportId,
        venueId = if (isHasVenue) venueId else null,
        venueOptionIds = if (!isHasVenue) proposedVenues.map { it.venueId } else null,
        format = format,
        startsAt = if (isHasVenue) normalizedStart else null,
        endsAt = if (isHasVenue) normalizedEnd else null,
        timeOptions =
            if (!isHasVenue) {
                proposedTimeSlots.map { CreateMatchTimeOptionDto(it.startsAt, it.endsAt) }
            } else {
                null
            },
        expiresAt = normalizedExpires,
        skillLevels = listOfNotNull(skillLevel.takeIf { it.isNotBlank() }),
        ageNote = ageNote?.takeIf { it.isNotBlank() },
        matchNature = notes.takeIf { it.isNotBlank() },
        costType = costType,
        costSplit = costSplit.takeIf { it.isNotBlank() },
        venueAmount = venueAmount.takeIf { it > 0 },
        drinksAmount = drinksAmount?.takeIf { it > 0 },
        depositAmount = depositAmount?.takeIf { it > 0 },
        depositHolder = depositHolder?.takeIf { it.isNotBlank() },
        minReputationScore = minReputationScore,
    )
}

internal fun MatchDraft.toUpdateDto(): UpdateMatchRequestDto {
    val normalizedStart = "${matchDate}T${normalizeTime(startTime)}"
    val normalizedEnd = "${matchDate}T${normalizeTime(endTime)}"
    return UpdateMatchRequestDto(
        format = format,
        skillLevels = listOfNotNull(skillLevel.takeIf { it.isNotBlank() }),
        venueId = venueId,
        startsAt = normalizedStart,
        endsAt = normalizedEnd,
        expiresAt = expiresAt.takeIf { it.isNotBlank() },
        ageNote = ageNote?.takeIf { it.isNotBlank() },
        matchNature = notes.takeIf { it.isNotBlank() },
        costType = costType,
        costSplit = costSplit.takeIf { it.isNotBlank() },
        venueAmount = venueAmount.takeIf { it > 0 },
        drinksAmount = drinksAmount?.takeIf { it > 0 },
        minReputationScore = minReputationScore,
    )
}

private fun normalizeTime(raw: String): String {
    val trimmed = raw.trim()
    return when {
        trimmed.length == TIME_HH_MM_LENGTH -> "$trimmed:00"
        trimmed.isBlank() -> "19:00:00"
        else -> trimmed
    }
}

private const val TIME_HH_MM_LENGTH = 5
